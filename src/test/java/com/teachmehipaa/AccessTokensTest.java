package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.BASIC_CREDENTIALS;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.SECOND_ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.learnerRetrieveApi;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AccessTokensTest {
  private static List<String> bearerTokensSent(FakeTransport api) {
    return api.requestsTo(LEARNER_ROUTE).stream()
        .map(request -> request.headers().get("authorization"))
        .toList();
  }

  private static String body(TransportRequest request) {
    return new String(request.body(), StandardCharsets.UTF_8);
  }

  @Test
  void mintsATokenWithHttpBasicClientCredentials() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api).build().learners().retrieve(LEARNER_ID);

    List<TransportRequest> mints = api.requestsTo(TOKEN_ROUTE);
    assertEquals(1, mints.size());
    TransportRequest mint = mints.get(0);
    assertEquals("Basic " + BASIC_CREDENTIALS, mint.headers().get("authorization"));
    assertEquals("application/x-www-form-urlencoded", mint.headers().get("content-type"));
    assertEquals("grant_type=client_credentials", body(mint));
  }

  @Test
  void requestsOnlyTheScopesGiven() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api)
        .scopes("learners:read", "compliance:read")
        .build()
        .learners()
        .retrieve(LEARNER_ID);

    TransportRequest mint = api.requestsTo(TOKEN_ROUTE).get(0);
    assertEquals(
        "grant_type=client_credentials&scope=learners%3Aread+compliance%3Aread", body(mint));
  }

  @Test
  void reusesACachedTokenAcrossCalls() {
    FakeTransport api = learnerRetrieveApi();
    TeachMeHipaaClient client = clientFor(api).build();

    client.learners().retrieve(LEARNER_ID);
    client.learners().retrieve(LEARNER_ID);

    assertEquals(1, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(
        List.of("Bearer " + ACCESS_TOKEN, "Bearer " + ACCESS_TOKEN), bearerTokensSent(api));
  }

  @Test
  void reMintsATokenSixtySecondsBeforeItExpires() {
    FakeTransport api =
        learnerRetrieveApi().on(TOKEN_ROUTE, Reply.json(tokenJson(SECOND_ACCESS_TOKEN)));
    FakeClock clock = new FakeClock();
    TeachMeHipaaClient client = clientFor(api).organizationId(ORG_ID).clock(clock).build();

    client.learners().retrieve(LEARNER_ID);
    clock.advance(Duration.ofSeconds(839)); // expires_in 900, so 61 s of life left
    client.learners().retrieve(LEARNER_ID);
    clock.advance(Duration.ofSeconds(1)); // 60 s left: renew now
    client.learners().retrieve(LEARNER_ID);

    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(
        List.of(
            "Bearer " + ACCESS_TOKEN, "Bearer " + ACCESS_TOKEN, "Bearer " + SECOND_ACCESS_TOKEN),
        bearerTokensSent(api));
  }

  @Test
  void concurrentCallsMintExactlyOneToken() throws Exception {
    AtomicInteger mints = new AtomicInteger();
    CountDownLatch secondMint = new CountDownLatch(1);
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                request -> {
                  if (mints.incrementAndGet() > 1) {
                    secondMint.countDown();
                  }
                  // Hold the first mint open long enough for the other callers to queue behind
                  // it, or until a duplicate mint proves they didn't.
                  await(secondMint);
                  return Reply.json(tokenJson(ACCESS_TOKEN));
                })
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = clientFor(api).organizationId(ORG_ID).build();
    ExecutorService callers = Executors.newFixedThreadPool(4);

    try {
      List<Future<?>> calls = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        calls.add(callers.submit(() -> client.learners().retrieve(LEARNER_ID)));
      }
      for (Future<?> call : calls) {
        call.get(5, TimeUnit.SECONDS);
      }
    } finally {
      callers.shutdownNow();
    }

    assertEquals(1, mints.get());
    assertEquals(4, bearerTokensSent(api).size());
  }

  @Test
  void aRejectedCredentialRaisesWithTheOAuthError() {
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                Reply.json(
                        401,
                        """
                        {"error": "invalid_client", "error_description": "Unknown client."}
                        """)
                    .withHeader("x-request-id", "req_auth"));
    TeachMeHipaaClient client = clientFor(api).clientSecret("tmh_cs_wrong").build();

    TeachMeHipaaException error =
        assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(401, error.getStatusCode());
    assertEquals("invalid_client", error.getCode());
    assertEquals("Unknown client.", error.getDetail());
    assertEquals("req_auth", error.getRequestId());
    assertEquals(List.of(), api.requestsTo(LEARNER_ROUTE));
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await(300, TimeUnit.MILLISECONDS);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
