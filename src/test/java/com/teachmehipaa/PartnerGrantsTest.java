package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.APP_BASIC_CREDENTIALS;
import static com.teachmehipaa.Fixtures.APP_ID;
import static com.teachmehipaa.Fixtures.APP_SECRET;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.REFRESH_TOKEN_1;
import static com.teachmehipaa.Fixtures.REFRESH_TOKEN_2;
import static com.teachmehipaa.Fixtures.REFRESH_TOKEN_3;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.grantTokenJson;
import static com.teachmehipaa.Fixtures.oauthError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.PartnerGrantTokenProvider;
import com.teachmehipaa.core.TokenStore;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.OAuthException;
import com.teachmehipaa.exception.TokenStoreException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
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

class PartnerGrantsTest {
  private static final Reply LEARNER = Reply.json(LEARNER_JSON);

  /**
   * A token store that records, at each save, which requests the API had already seen, so the
   * ordering is asserted on the wire.
   */
  static final class MemoryStore implements TokenStore {
    final List<String> saved = new ArrayList<>();
    final List<List<String>> routesSeenAtSave = new ArrayList<>();
    int loads;
    private final FakeTransport api;
    private final String initial;

    MemoryStore(FakeTransport api, String initial) {
      this.api = api;
      this.initial = initial;
    }

    MemoryStore(FakeTransport api) {
      this(api, REFRESH_TOKEN_1);
    }

    @Override
    public synchronized String load() {
      loads++;
      return saved.isEmpty() ? initial : saved.get(saved.size() - 1);
    }

    @Override
    public synchronized void save(String refreshToken) {
      routesSeenAtSave.add(
          api.requests().stream()
              .map(request -> request.method() + " " + request.uri().getRawPath())
              .toList());
      saved.add(refreshToken);
    }
  }

  /** Saves fail for the first {@code failures} calls. */
  static final class ReadOnlyStore implements TokenStore {
    final List<String> attempted = new ArrayList<>();
    private final int failures;

    ReadOnlyStore(int failures) {
      this.failures = failures;
    }

    @Override
    public String load() {
      return REFRESH_TOKEN_1;
    }

    @Override
    public void save(String refreshToken) {
      attempted.add(refreshToken);
      if (attempted.size() <= failures) {
        throw new UncheckedIOException(new java.io.IOException("database is read-only"));
      }
    }
  }

  private static PartnerGrantTokenProvider.Builder provider(FakeTransport api, TokenStore store) {
    return PartnerGrantTokenProvider.builder()
        .clientId(APP_ID)
        .clientSecret(APP_SECRET)
        .tokenStore(store)
        .httpTransport(api);
  }

  private static TeachMeHipaaClient client(FakeTransport api, PartnerGrantTokenProvider tokens) {
    return TeachMeHipaaClient.builder()
        .tokenProvider(tokens)
        .organizationId(ORG_ID)
        .httpTransport(api)
        .sleeper(new FakeSleeper())
        .build();
  }

  private static TeachMeHipaaClient client(FakeTransport api, TokenStore store) {
    return client(api, provider(api, store).build());
  }

  private static String form(TransportRequest request) {
    return new String(request.body(), StandardCharsets.UTF_8);
  }

  private static List<String> refreshTokensSent(FakeTransport api) {
    return api.requestsTo(TOKEN_ROUTE).stream()
        .map(PartnerGrantsTest::form)
        .map(form -> form.replaceAll(".*refresh_token=([^&]*).*", "$1"))
        .map(token -> java.net.URLDecoder.decode(token, StandardCharsets.UTF_8))
        .toList();
  }

  private static List<String> bearers(FakeTransport api) {
    return api.requestsTo(LEARNER_ROUTE).stream()
        .map(request -> request.headers().get("authorization"))
        .toList();
  }

  @Test
  void theRotatedRefreshTokenIsSavedBeforeTheApiRequestIsSent() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_2)))
            .on(LEARNER_ROUTE, LEARNER);
    MemoryStore store = new MemoryStore(api);

    client(api, store).learners().retrieve(LEARNER_ID);

    assertEquals(List.of(REFRESH_TOKEN_2), store.saved);
    assertEquals(List.of(List.of(TOKEN_ROUTE)), store.routesSeenAtSave);
    TransportRequest exchange = api.requestsTo(TOKEN_ROUTE).get(0);
    assertEquals("Basic " + APP_BASIC_CREDENTIALS, exchange.headers().get("authorization"));
    assertEquals(
        "grant_type=refresh_token&refresh_token=tmh_rt_grt_01JD8Z4N3QX5V7W9Y2B4C6D8EP.first",
        form(exchange));
    assertEquals(List.of("Bearer access-org"), bearers(api));
  }

  @Test
  void aFailedSaveAbortsTheCallWithoutUsingTheAccessToken() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_2)))
            .on(LEARNER_ROUTE, LEARNER);

    TokenStoreException error =
        assertThrows(
            TokenStoreException.class,
            () -> client(api, new ReadOnlyStore(1)).learners().retrieve(LEARNER_ID));

    assertTrue(error.getMessage().contains("tokenStore.save() failed"), error.getMessage());
    assertTrue(error.getCause() instanceof UncheckedIOException);
    assertFalse(error.getMessage().contains(REFRESH_TOKEN_2), error.getMessage());
    assertFalse(error.getMessage().contains("access-org"), error.getMessage());
    assertEquals(List.of(), api.requestsTo(LEARNER_ROUTE));
  }

  @Test
  void afterAFailedSaveTheRotatedRefreshTokenIsPresented() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-1", REFRESH_TOKEN_2)))
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-2", REFRESH_TOKEN_3)))
            .on(LEARNER_ROUTE, LEARNER);
    ReadOnlyStore store = new ReadOnlyStore(1);
    TeachMeHipaaClient client = client(api, store);

    assertThrows(TokenStoreException.class, () -> client.learners().retrieve(LEARNER_ID));
    client.learners().retrieve(LEARNER_ID);

    assertEquals(List.of(REFRESH_TOKEN_1, REFRESH_TOKEN_2), refreshTokensSent(api));
    assertEquals(List.of(REFRESH_TOKEN_2, REFRESH_TOKEN_3), store.attempted);
    assertEquals(List.of("Bearer access-2"), bearers(api));
  }

  @Test
  void aNetworkErrorDuringRefreshMakesExactlyOneExchangeAttempt() {
    ConnectException refused = new ConnectException("Connection refused");
    FakeTransport api = new FakeTransport().fail(TOKEN_ROUTE, refused);
    MemoryStore store = new MemoryStore(api);

    ApiConnectionException error =
        assertThrows(
            ApiConnectionException.class, () -> client(api, store).learners().retrieve(LEARNER_ID));

    assertTrue(error.getMessage().contains("not retried"), error.getMessage());
    assertTrue(error.getMessage().contains("re-run consent"), error.getMessage());
    assertEquals(refused, error.getCause());
    assertEquals(1, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(List.of(), store.saved);
  }

  @Test
  void aServerErrorDuringRefreshIsNotRetried() {
    FakeTransport api =
        new FakeTransport().on(TOKEN_ROUTE, oauthError(503, "temporarily_unavailable", "Later"));

    assertThrows(
        OAuthException.class,
        () -> client(api, new MemoryStore(api)).learners().retrieve(LEARNER_ID));

    assertEquals(1, api.requestsTo(TOKEN_ROUTE).size());
  }

  @Test
  void invalidGrantSaysTheGrantWasRevokedOrItsTokenReused() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, oauthError(400, "invalid_grant", "Refresh token is invalid"));

    OAuthException error =
        assertThrows(
            OAuthException.class,
            () -> client(api, new MemoryStore(api)).learners().retrieve(LEARNER_ID));

    assertEquals("invalid_grant", error.getCode());
    assertEquals(400, error.getStatusCode());
    assertTrue(error.getMessage().contains("revoked"), error.getMessage());
    assertTrue(error.getMessage().contains("reused"), error.getMessage());
    assertTrue(error.getMessage().contains("Re-run consent"), error.getMessage());
    assertEquals(1, api.requestsTo(TOKEN_ROUTE).size());
  }

  @Test
  void concurrentCallsExchangeTheRefreshTokenOnce() throws Exception {
    AtomicInteger exchanges = new AtomicInteger();
    CountDownLatch secondExchange = new CountDownLatch(1);
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                request -> {
                  if (exchanges.incrementAndGet() > 1) {
                    secondExchange.countDown();
                  }
                  // Hold the first exchange open long enough for the other callers to queue
                  // behind it, or until a duplicate exchange proves they didn't.
                  await(secondExchange);
                  return Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_2));
                })
            .on(LEARNER_ROUTE, LEARNER);
    MemoryStore store = new MemoryStore(api);
    TeachMeHipaaClient client = client(api, store);
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

    assertEquals(1, exchanges.get());
    assertEquals(List.of(REFRESH_TOKEN_2), store.saved);
    assertEquals(4, bearers(api).size());
  }

  @Test
  void reMintsSixtySecondsBeforeExpiryWithTheRotatedRefreshToken() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-1", REFRESH_TOKEN_2)))
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-2", REFRESH_TOKEN_3)))
            .on(LEARNER_ROUTE, LEARNER);
    MemoryStore store = new MemoryStore(api);
    FakeClock clock = new FakeClock();
    TeachMeHipaaClient client = client(api, provider(api, store).clock(clock).build());

    client.learners().retrieve(LEARNER_ID);
    clock.advance(Duration.ofSeconds(839)); // expires_in 900, so 61 s of life left
    client.learners().retrieve(LEARNER_ID);
    clock.advance(Duration.ofSeconds(1)); // 60 s left: renew now
    client.learners().retrieve(LEARNER_ID);

    assertEquals(List.of(REFRESH_TOKEN_1, REFRESH_TOKEN_2), refreshTokensSent(api));
    assertEquals(List.of(REFRESH_TOKEN_2, REFRESH_TOKEN_3), store.saved);
    assertEquals(List.of("Bearer access-1", "Bearer access-1", "Bearer access-2"), bearers(api));
  }

  @Test
  void a401ReMintsOnceAndReplaysTheRequest() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-1", REFRESH_TOKEN_2)))
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-2", REFRESH_TOKEN_3)))
            .on(
                LEARNER_ROUTE,
                Reply.problem(
                    401,
                    """
                    {"title": "Unauthorized", "status": 401, "code": "unauthorized"}
                    """))
            .on(LEARNER_ROUTE, LEARNER);
    MemoryStore store = new MemoryStore(api);

    client(api, store).learners().retrieve(LEARNER_ID);

    assertEquals(List.of(REFRESH_TOKEN_1, REFRESH_TOKEN_2), refreshTokensSent(api));
    assertEquals(List.of(REFRESH_TOKEN_2, REFRESH_TOKEN_3), store.saved);
    assertEquals(List.of("Bearer access-1", "Bearer access-2"), bearers(api));
  }

  @Test
  void aRefreshTokenGivenUpFrontIsUsedForTheFirstExchange() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_3)))
            .on(LEARNER_ROUTE, LEARNER);
    MemoryStore store = new MemoryStore(api, "tmh_rt_never_loaded");

    client(api, provider(api, store).refreshToken(REFRESH_TOKEN_2).build())
        .learners()
        .retrieve(LEARNER_ID);

    assertEquals(List.of(REFRESH_TOKEN_2), refreshTokensSent(api));
    assertEquals(0, store.loads);
    assertEquals(List.of(REFRESH_TOKEN_3), store.saved);
  }

  @Test
  void scopesNarrowTheAccessToken() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_2)))
            .on(LEARNER_ROUTE, LEARNER);

    client(api, provider(api, new MemoryStore(api)).scopes("learners:read").build())
        .learners()
        .retrieve(LEARNER_ID);

    assertTrue(
        form(api.requestsTo(TOKEN_ROUTE).get(0)).endsWith("&scope=learners%3Aread"),
        form(api.requestsTo(TOKEN_ROUTE).get(0)));
  }

  @Test
  void anEmptyTokenStoreRaisesBeforeAnyExchange() {
    FakeTransport api = new FakeTransport();
    TokenStore empty =
        new TokenStore() {
          @Override
          public String load() {
            return null;
          }

          @Override
          public void save(String refreshToken) {}
        };

    assertThrows(
        TokenStoreException.class, () -> client(api, empty).learners().retrieve(LEARNER_ID));

    assertEquals(List.of(), api.requests());
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await(300, TimeUnit.MILLISECONDS);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
