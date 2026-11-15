package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.SECOND_ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.AuthenticationException;
import com.teachmehipaa.exception.OAuthException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerUpdateParams;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccessTokenReplayTest {
  private static final String UPDATE_ROUTE =
      "PATCH /v1/organizations/org_acme/learners/" + LEARNER_ID;

  /** What the API answers for a revoked or expired Access token. */
  private static final Reply UNAUTHORIZED =
      Reply.problem(
              401,
              """
              {"title": "Unauthorized", "status": 401, "code": "unauthorized"}
              """)
          .withHeader(
              "www-authenticate", "Bearer realm=\"TeachMeHIPAA API\", error=\"invalid_token\"");

  private static final Reply UNAVAILABLE =
      Reply.problem(
          503,
          """
          {"title": "Service Unavailable", "status": 503, "code": "temporarily_unavailable"}
          """);

  private static FakeTransport.Responder networkFailure() {
    return request -> {
      throw new ConnectException("Connection refused");
    };
  }

  /** Two Access tokens in turn from the token endpoint. */
  private static FakeTransport twoTokens() {
    return new FakeTransport()
        .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(TOKEN_ROUTE, Reply.json(tokenJson(SECOND_ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient.Builder clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID);
  }

  private static List<String> bearerTokensSent(FakeTransport api, String route) {
    return api.requestsTo(route).stream()
        .map(request -> request.headers().get("authorization"))
        .toList();
  }

  @Test
  void reMintsTheAccessTokenOnceAndReplaysTheRequest() {
    FakeTransport api =
        twoTokens().on(LEARNER_ROUTE, UNAUTHORIZED).on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    Learner learner = clientOn(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(200, learner.getLastResponse().statusCode());
    assertEquals(
        List.of("Bearer " + ACCESS_TOKEN, "Bearer " + SECOND_ACCESS_TOKEN),
        bearerTokensSent(api, LEARNER_ROUTE));
  }

  @Test
  void theReplayDoesNotUseUpARetry() {
    FakeTransport api =
        twoTokens().on(LEARNER_ROUTE, UNAUTHORIZED).on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    Learner learner = clientOn(api).maxRetries(0).build().learners().retrieve(LEARNER_ID);

    assertEquals(200, learner.getLastResponse().statusCode());
  }

  @Test
  void everyRetryIsStillLeftAfterAReplay() {
    FakeTransport api =
        twoTokens()
            .on(LEARNER_ROUTE, UNAUTHORIZED)
            .on(LEARNER_ROUTE, UNAVAILABLE)
            .on(LEARNER_ROUTE, UNAVAILABLE)
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(
        List.of(
            "Bearer " + ACCESS_TOKEN,
            "Bearer " + SECOND_ACCESS_TOKEN,
            "Bearer " + SECOND_ACCESS_TOKEN,
            "Bearer " + SECOND_ACCESS_TOKEN),
        bearerTokensSent(api, LEARNER_ROUTE));
  }

  @Test
  void replaysOnlyOnceAndThenRaisesAnAuthenticationException() {
    FakeTransport api = twoTokens().on(LEARNER_ROUTE, UNAUTHORIZED);
    TeachMeHipaaClient client = clientOn(api).build();

    assertThrows(AuthenticationException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(2, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void replaysAPatchWhichTheApiRefusedBeforeActingOnIt() {
    FakeTransport api =
        twoTokens().on(UPDATE_ROUTE, UNAUTHORIZED).on(UPDATE_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api)
        .build()
        .learners()
        .update(LEARNER_ID, LearnerUpdateParams.builder().name("Ana Lima").build());

    assertEquals(
        List.of("Bearer " + ACCESS_TOKEN, "Bearer " + SECOND_ACCESS_TOKEN),
        bearerTokensSent(api, UPDATE_ROUTE));
  }

  @Test
  void concurrentCallsRefusedWithTheSameTokenReMintOnce() throws Exception {
    AtomicInteger learnerCalls = new AtomicInteger();
    CountDownLatch bothRefusable = new CountDownLatch(2);
    FakeTransport api =
        twoTokens()
            .on(
                LEARNER_ROUTE,
                request -> {
                  if (learnerCalls.incrementAndGet() > 2) {
                    return Reply.json(LEARNER_JSON);
                  }
                  // Both first calls are in flight with the first token before either is refused.
                  bothRefusable.countDown();
                  await(bothRefusable);
                  return UNAUTHORIZED;
                });
    TeachMeHipaaClient client = clientOn(api).build();
    ExecutorService callers = Executors.newFixedThreadPool(2);
    List<Future<Learner>> calls = new ArrayList<>();

    try {
      calls.add(callers.submit(() -> client.learners().retrieve(LEARNER_ID)));
      calls.add(callers.submit(() -> client.learners().retrieve(LEARNER_ID)));
      for (Future<Learner> call : calls) {
        call.get(5, TimeUnit.SECONDS);
      }
    } finally {
      callers.shutdownNow();
    }

    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(
        List.of("Bearer " + ACCESS_TOKEN, "Bearer " + ACCESS_TOKEN),
        bearerTokensSent(api, LEARNER_ROUTE).subList(0, 2));
    assertEquals(
        List.of("Bearer " + SECOND_ACCESS_TOKEN, "Bearer " + SECOND_ACCESS_TOKEN),
        bearerTokensSent(api, LEARNER_ROUTE).subList(2, 4));
  }

  @Test
  void raisesTheOAuthExceptionWhenTheReMintIsRefused() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(
                TOKEN_ROUTE,
                Reply.json(
                    401,
                    """
                    {"error": "invalid_client", "error_description": "The API client was revoked."}
                    """))
            .on(LEARNER_ROUTE, UNAUTHORIZED);
    TeachMeHipaaClient client = clientOn(api).build();

    TeachMeHipaaException error =
        assertThrows(OAuthException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals("invalid_client", error.getCode());
    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
  }

  static Stream<Arguments> mintFailures() {
    return Stream.of(
        Arguments.of("a network error", networkFailure()),
        Arguments.of("a 5xx", (FakeTransport.Responder) request -> UNAVAILABLE));
  }

  @ParameterizedTest(name = "retries a client-credentials mint after {0}")
  @MethodSource("mintFailures")
  void retriesAClientCredentialsMintAfterATransientFailure(
      String name, FakeTransport.Responder failure) {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, failure)
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(List.of("Bearer " + ACCESS_TOKEN), bearerTokensSent(api, LEARNER_ROUTE));
  }

  @Test
  void doesNotRetryAMintTheTokenEndpointRefused() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(401, "{\"error\": \"invalid_client\"}"))
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
    TeachMeHipaaClient client = clientOn(api).build();

    assertThrows(OAuthException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(1, api.requestsTo(TOKEN_ROUTE).size());
  }

  @Test
  void raisesOnceMintRetriesRunOutWithoutRetryingTheRequestOnTop() {
    FakeTransport api = new FakeTransport().on(TOKEN_ROUTE, networkFailure());
    TeachMeHipaaClient client = clientOn(api).build();

    TeachMeHipaaException error =
        assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertSame(ApiConnectionException.class, error.getClass());
    assertInstanceOf(ConnectException.class, error.getCause());
    assertEquals(3, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(List.of(), api.requestsTo(LEARNER_ROUTE));
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
