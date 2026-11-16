package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.ConflictException;
import com.teachmehipaa.exception.InvalidRequestException;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.PreconditionFailedException;
import com.teachmehipaa.exception.RateLimitException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Learner;
import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RetriesTest {
  private final FakeSleeper sleeper = new FakeSleeper();

  private static Reply problem(int status, String code) {
    return Reply.problem(
        status,
        """
        {"title": "Problem", "status": %d, "code": "%s"}
        """
            .formatted(status, code));
  }

  private static FakeTransport.Responder networkFailure() {
    return request -> {
      throw new ConnectException("Connection refused");
    };
  }

  /** The Learner route answers with each step in turn. */
  private static FakeTransport api(FakeTransport.Responder... steps) {
    FakeTransport api = new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
    for (FakeTransport.Responder step : steps) {
      api.on(LEARNER_ROUTE, step);
    }
    return api;
  }

  private static FakeTransport.Responder reply(Reply reply) {
    return request -> reply;
  }

  private static FakeTransport.Responder learner() {
    return reply(Reply.json(LEARNER_JSON));
  }

  private TeachMeHipaaClient.Builder clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).sleeper(sleeper);
  }

  static Stream<Arguments> transientFailures() {
    return Stream.of(
        Arguments.of("a network error", networkFailure()),
        Arguments.of("409 conflict (request in flight)", reply(problem(409, "conflict"))),
        Arguments.of("429 rate_limited", reply(problem(429, "rate_limited"))),
        Arguments.of("500", reply(problem(500, "internal_error"))),
        Arguments.of("502 refund_failed", reply(problem(502, "refund_failed"))),
        Arguments.of("503", reply(problem(503, "temporarily_unavailable"))));
  }

  @ParameterizedTest(name = "retries a GET after {0}")
  @MethodSource("transientFailures")
  void retriesAGetAfterATransientFailure(String name, FakeTransport.Responder failure) {
    FakeTransport api = api(failure, learner());

    Learner learner = clientOn(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(LEARNER_ID, learner.getLearnerId());
    assertEquals(2, api.requestsTo(LEARNER_ROUTE).size());
    // The first ceiling is 0.5 s and nothing waits less than 0.5 s.
    assertEquals(List.of(Duration.ofMillis(500)), sleeper.sleeps());
  }

  @Test
  void givesUpAfterTwoRetriesByDefaultAndRaisesTheLastError() {
    FakeTransport api =
        api(
            reply(problem(503, "temporarily_unavailable")),
            reply(problem(503, "temporarily_unavailable")),
            reply(problem(500, "internal_error")));
    TeachMeHipaaClient client = clientOn(api).build();

    TeachMeHipaaException error =
        assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertInstanceOf(ApiException.class, error);
    assertEquals(500, error.getStatusCode());
    assertEquals("internal_error", error.getCode());
    assertEquals(3, api.requestsTo(LEARNER_ROUTE).size());
    assertEquals(2, sleeper.sleeps().size());
  }

  @Test
  void raisesAnApiConnectionExceptionOnceNetworkRetriesRunOut() {
    FakeTransport api = api(networkFailure());
    TeachMeHipaaClient client = clientOn(api).build();

    TeachMeHipaaException error =
        assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertSame(ApiConnectionException.class, error.getClass());
    assertEquals(3, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void doublesTheBackoffWithJitterUpToAFiveSecondCap() {
    FakeTransport.Responder unavailable = reply(problem(503, "temporarily_unavailable"));
    FakeTransport api =
        api(
            unavailable,
            unavailable,
            unavailable,
            unavailable,
            unavailable,
            unavailable,
            unavailable,
            learner());

    clientOn(api).maxRetries(7).build().learners().retrieve(LEARNER_ID);

    // Ceilings 0.5, 1, 2, 4, 5, 5, 5 s; jitter keeps each within 50-100 % of its ceiling and
    // never below 0.5 s.
    long[][] rangesMillis = {
      {500, 500}, {500, 1000}, {1000, 2000}, {2000, 4000}, {2500, 5000}, {2500, 5000}, {2500, 5000}
    };
    List<Duration> sleeps = sleeper.sleeps();
    assertEquals(rangesMillis.length, sleeps.size());
    for (int i = 0; i < rangesMillis.length; i++) {
      long millis = sleeps.get(i).toMillis();
      assertTrue(
          millis >= rangesMillis[i][0] && millis <= rangesMillis[i][1],
          "sleep " + i + " was " + millis + " ms");
    }
  }

  @Test
  void waitsAsLongAsRetryAfterSaysInSeconds() {
    FakeTransport api =
        api(reply(problem(429, "rate_limited").withHeader("retry-after", "3")), learner());

    clientOn(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(List.of(Duration.ofSeconds(3)), sleeper.sleeps());
  }

  @Test
  void waitsUntilARetryAfterHttpDate() {
    // FakeClock starts at 2026-11-15T12:00:00Z.
    FakeTransport api =
        api(
            reply(
                problem(503, "temporarily_unavailable")
                    .withHeader("retry-after", "Sun, 15 Nov 2026 12:00:10 GMT")),
            learner());

    clientOn(api).clock(new FakeClock()).build().learners().retrieve(LEARNER_ID);

    assertEquals(List.of(Duration.ofSeconds(10)), sleeper.sleeps());
  }

  @Test
  void waitsAtMostSixtySecondsHoweverLongRetryAfterAsksFor() {
    FakeTransport api =
        api(reply(problem(429, "rate_limited").withHeader("retry-after", "3600")), learner());

    clientOn(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(List.of(Duration.ofSeconds(60)), sleeper.sleeps());
  }

  static Stream<Arguments> permanentFailures() {
    return Stream.of(
        Arguments.of(problem(409, "seat_unavailable"), ConflictException.class),
        Arguments.of(problem(404, "not_found"), NotFoundException.class),
        Arguments.of(problem(400, "validation_failed"), InvalidRequestException.class),
        Arguments.of(problem(412, "precondition_failed"), PreconditionFailedException.class));
  }

  @ParameterizedTest(name = "does not retry {0}")
  @MethodSource("permanentFailures")
  void doesNotRetryAPermanentFailure(
      Reply failure, Class<? extends TeachMeHipaaException> exceptionClass) {
    FakeTransport api = api(reply(failure));
    TeachMeHipaaClient client = clientOn(api).build();

    assertThrows(exceptionClass, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(1, api.requestsTo(LEARNER_ROUTE).size());
    assertEquals(List.of(), sleeper.sleeps());
  }

  @Test
  void doesNotRetryWhenTheClientSetsMaxRetriesToZero() {
    FakeTransport api = api(reply(problem(429, "rate_limited")));
    TeachMeHipaaClient client = clientOn(api).maxRetries(0).build();

    assertThrows(RateLimitException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(1, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void oneRequestCanOverrideTheClientsMaxRetries() {
    FakeTransport api =
        api(
            networkFailure(),
            networkFailure(),
            networkFailure(),
            learner(),
            reply(problem(503, "temporarily_unavailable")));
    TeachMeHipaaClient client = clientOn(api).maxRetries(0).build();

    Learner learner =
        client.learners().retrieve(LEARNER_ID, RequestOptions.builder().maxRetries(3).build());
    assertEquals(200, learner.getLastResponse().statusCode());
    assertEquals(4, api.requestsTo(LEARNER_ROUTE).size());

    assertThrows(ApiException.class, () -> client.learners().retrieve(LEARNER_ID));
    assertEquals(5, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void sendsEachRetryWithThePerRequestTimeout() {
    FakeTransport api = api(networkFailure(), learner());

    clientOn(api)
        .timeout(Duration.ofSeconds(30))
        .build()
        .learners()
        .retrieve(LEARNER_ID, RequestOptions.builder().timeout(Duration.ofSeconds(2)).build());

    assertEquals(
        List.of(Duration.ofSeconds(2), Duration.ofSeconds(2)),
        api.requestsTo(LEARNER_ROUTE).stream().map(request -> request.timeout()).toList());
  }

  @Test
  void rejectsANegativeMaxRetriesOption() {
    assertThrows(IllegalArgumentException.class, () -> RequestOptions.builder().maxRetries(-1));
  }
}
