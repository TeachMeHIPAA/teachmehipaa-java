package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.CLIENT_ID;
import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static com.teachmehipaa.Fixtures.ETAG;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.SECOND_ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.RequestLogEntry.ErrorKind;
import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.BinaryStreamResponse;
import com.teachmehipaa.core.EmptyResponse;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.AuthenticationException;
import com.teachmehipaa.exception.ConflictException;
import com.teachmehipaa.exception.IdempotencyException;
import com.teachmehipaa.exception.InvalidRequestException;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.OAuthException;
import com.teachmehipaa.exception.OperationFailedException;
import com.teachmehipaa.exception.OperationTimeoutException;
import com.teachmehipaa.exception.PermissionDeniedException;
import com.teachmehipaa.exception.PreconditionFailedException;
import com.teachmehipaa.exception.RateLimitException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Course;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.model.WebhookDeliveryOperation;
import com.teachmehipaa.model.WebhookDeliveryResult;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerListParams;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** The {@code …Async} methods, against the same fakes as the blocking ones (issue #2). */
class AsyncTest {
  private static final String ORG = "/v1/organizations/" + ORG_ID;
  private static final String LEARNERS_ROUTE = "GET " + ORG + "/learners";
  private static final String OPERATION_ID = "0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d";
  private static final String POLL_ROUTE = "GET " + ORG + "/operations/" + OPERATION_ID;
  private static final String SUBSCRIPTION_ID = "whs_01JD8K2M4N6P8R0T2V4X6Z8B0D";
  private static final String TEST_DELIVERY_ROUTE =
      "POST " + ORG + "/webhook-subscriptions/" + SUBSCRIPTION_ID + ":test";
  private static final String PENDING_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": false}
      """;
  private static final String SENT_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": true, "response": {"_tag": "sent", "responseStatus": 200}}
      """;
  private static final String FAILED_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": true,
       "error": {"title": "Temporarily unavailable", "status": 503,
                 "code": "temporarily_unavailable"}}
      """;
  private static final String COURSE_ROUTE = "GET /v1/courses/hipaa-2026";
  private static final String COURSE_JSON =
      """
      {"courseId": "hipaa-2026", "title": "HIPAA Essentials 2026", "courseVersion": "2026.1",
       "passingThreshold": 0.8}
      """;

  private final FakeClock clock = new FakeClock();
  private final FakeSleeper sleeper = new FakeSleeper(clock);
  private final FakeAsyncSleeper asyncSleeper = new FakeAsyncSleeper(clock);

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private TeachMeHipaaClient.Builder clientOn(com.teachmehipaa.core.HttpTransport api) {
    return TeachMeHipaaClient.builder()
        .clientId(CLIENT_ID)
        .clientSecret(CLIENT_SECRET)
        .organizationId(ORG_ID)
        .httpTransport(api)
        .clock(clock)
        .sleeper(sleeper)
        .asyncSleeper(asyncSleeper);
  }

  private static Reply problem(int status, String code) {
    return Reply.problem(
        status,
        """
        {"title": "Problem", "status": %d, "code": "%s"}
        """
            .formatted(status, code));
  }

  /** What the future failed with, as {@code handle} sees it. */
  private static Throwable failureOf(CompletableFuture<?> future) {
    return future.handle((value, failure) -> failure).orTimeout(5, TimeUnit.SECONDS).join();
  }

  private static <T> T await(CompletableFuture<T> future) {
    return future.orTimeout(5, TimeUnit.SECONDS).join();
  }

  // Each kind of generated method.

  @Test
  void retrieveAsyncReadsTheSameObjectAsRetrieve() {
    FakeTransport api = api().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON).withHeader("etag", ETAG));
    TeachMeHipaaClient client = clientOn(api).build();

    Learner learner = await(client.learners().retrieveAsync(LEARNER_ID));
    Learner blocking = client.learners().retrieve(LEARNER_ID);

    assertEquals(
        List.of(blocking.getLearnerId(), blocking.getEmail(), blocking.getName()),
        List.of(learner.getLearnerId(), learner.getEmail(), learner.getName()));
    assertEquals(blocking.getStatus(), learner.getStatus());
    assertEquals(blocking.getDueAt(), learner.getDueAt());
    assertEquals(ETAG, learner.getLastResponse().etag());
  }

  @Test
  void createAsyncReportsWhetherTheLearnerWasCreated() {
    FakeTransport api = api().on("POST " + ORG + "/learners", Reply.json(201, LEARNER_JSON));

    LearnerWriteResult result =
        await(
            clientOn(api)
                .build()
                .learners()
                .createAsync(
                    LearnerCreateParams.builder()
                        .email("ana@example.com")
                        .courseId("hipaa-2026")
                        .build()));

    assertTrue(result.isCreated());
    assertNotNull(
        api.requestsTo("POST " + ORG + "/learners").get(0).headers().get("idempotency-key"));
  }

  @Test
  void deleteAsyncReturnsTheResponseMetadata() {
    String route = "DELETE " + ORG + "/webhook-subscriptions/" + SUBSCRIPTION_ID;
    FakeTransport api = api().on(route, Reply.empty(204));

    EmptyResponse deleted =
        await(clientOn(api).build().webhookSubscriptions().deleteAsync(SUBSCRIPTION_ID));

    assertEquals(204, deleted.getLastResponse().statusCode());
  }

  @Test
  void downloadAsyncAndStreamAsyncReturnTheFile() throws IOException {
    byte[] pdf = "%PDF-1.7 certificate".getBytes(StandardCharsets.UTF_8);
    String route = "GET " + ORG + "/certificates/cert_1/content";
    FakeTransport api =
        api().on(route, new Reply(200, java.util.Map.of("content-type", "application/pdf"), pdf));
    TeachMeHipaaClient client = clientOn(api).build();

    BinaryResponse whole = await(client.certificates().downloadPdfAsync("cert_1"));
    try (BinaryStreamResponse streamed =
        await(client.certificates().downloadPdfStreamAsync("cert_1"))) {
      assertArrayEquals(pdf, streamed.inputStream().readAllBytes());
    }

    assertArrayEquals(pdf, whole.data());
  }

  @Test
  void aStreamedDownloadThatFailsReadsTheProblemAndFailsTheFuture() {
    String route = "GET " + ORG + "/certificates/cert_1/content";
    FakeTransport api = api().on(route, problem(404, "not_found"));

    Throwable failure =
        failureOf(clientOn(api).build().certificates().downloadPdfStreamAsync("cert_1"));

    assertInstanceOf(NotFoundException.class, failure);
  }

  @Test
  void retrieveIfModifiedAsyncIsEmptyOn304() {
    FakeTransport api = api().on(COURSE_ROUTE, Reply.empty(304));

    Optional<Course> course =
        await(clientOn(api).build().courses().retrieveIfModifiedAsync("hipaa-2026", ETAG));

    assertEquals(Optional.empty(), course);
    assertEquals(ETAG, api.requestsTo(COURSE_ROUTE).get(0).headers().get("if-none-match"));
  }

  @Test
  void aPublicMethodSendsNoTokenAndNeedsNoCredentials() {
    FakeTransport api = new FakeTransport().on(COURSE_ROUTE, Reply.json(COURSE_JSON));

    Course course =
        await(
            TeachMeHipaaClient.builder()
                .httpTransport(api)
                .asyncSleeper(asyncSleeper)
                .build()
                .courses()
                .retrieveAsync("hipaa-2026"));

    assertEquals("hipaa-2026", course.getCourseId());
    assertEquals(List.of(), api.requestsTo(TOKEN_ROUTE));
  }

  @Test
  void aClientWithoutCredentialsFailsTheFutureOfAnAuthenticatedCall() {
    Throwable failure =
        failureOf(
            TeachMeHipaaClient.builder()
                .organizationId(ORG_ID)
                .httpTransport(new PendingTransport())
                .build()
                .learners()
                .retrieveAsync(LEARNER_ID));

    assertInstanceOf(IllegalStateException.class, failure);
  }

  // No blocking.

  @Test
  void noCallerThreadBlocksAndTheResponseArrivesOnTheTransportsThread()
      throws InterruptedException {
    PendingTransport api = new PendingTransport();
    TeachMeHipaaClient client = clientOn(api).organizationId(ORG_ID).build();
    AtomicReference<String> completedOn = new AtomicReference<>();

    // PendingTransport fails any blocking send, and leaves every response to this test, so these
    // calls return only because nothing waited on the network.
    CompletableFuture<Learner> learner =
        client
            .learners()
            .retrieveAsync(LEARNER_ID)
            .whenComplete((value, failure) -> completedOn.set(Thread.currentThread().getName()));

    assertFalse(learner.isDone());
    assertEquals(List.of(TOKEN_ROUTE), api.exchanges().stream().map(e -> e.route()).toList());
    api.answer(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
    assertFalse(learner.isDone());
    api.answer(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    assertEquals(LEARNER_ID, await(learner).getLearnerId());
    assertEquals("responder", completedOn.get());
    assertEquals(
        "Bearer " + ACCESS_TOKEN,
        api.to(LEARNER_ROUTE).get(0).request().headers().get("authorization"));
  }

  @Test
  void aTransportThatOnlyBlocksIsCalledOffTheCallersThread() {
    String caller = Thread.currentThread().getName();
    List<String> sentFrom = new CopyOnWriteArrayList<>();
    FakeTransport api =
        api()
            .on(
                LEARNER_ROUTE,
                request -> {
                  sentFrom.add(Thread.currentThread().getName());
                  return Reply.json(LEARNER_JSON);
                });

    await(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertEquals(1, sentFrom.size());
    assertNotEquals(caller, sentFrom.get(0));
    assertTrue(sentFrom.get(0).startsWith("teachmehipaa-blocking-io"), sentFrom.get(0));
  }

  // Retries.

  @Test
  void retriesOnTheSynchronousScheduleWithoutWaitingOrBlocking() {
    FakeTransport api =
        api()
            .on(LEARNER_ROUTE, problem(503, "temporarily_unavailable"))
            .on(LEARNER_ROUTE, problem(500, "internal_error"))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));
    long started = System.nanoTime();

    Learner learner = await(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertEquals(LEARNER_ID, learner.getLearnerId());
    assertEquals(3, api.requestsTo(LEARNER_ROUTE).size());
    List<Duration> waits = asyncSleeper.sleeps();
    assertEquals(2, waits.size());
    // Ceilings 0.5 s then 1 s, jittered to 50-100 % and never below 0.5 s.
    assertEquals(Duration.ofMillis(500), waits.get(0));
    assertTrue(waits.get(1).toMillis() >= 500 && waits.get(1).toMillis() <= 1000, "" + waits);
    assertEquals(List.of(), sleeper.sleeps(), "the blocking sleeper was used");
    assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2));
  }

  @ParameterizedTest(name = "Retry-After {0} waits {1} s")
  @MethodSource("retryAfters")
  void honorsRetryAfterUpToSixtySeconds(String retryAfter, long seconds) {
    FakeTransport api =
        api()
            .on(LEARNER_ROUTE, problem(429, "rate_limited").withHeader("retry-after", retryAfter))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));
    long started = System.nanoTime();

    await(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertEquals(List.of(Duration.ofSeconds(seconds)), asyncSleeper.sleeps());
    assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2));
  }

  static Stream<Arguments> retryAfters() {
    return Stream.of(Arguments.of("7", 7L), Arguments.of("120", 60L));
  }

  @Test
  void givesUpAfterMaxRetriesWithTheLastError() {
    FakeTransport api = api().on(LEARNER_ROUTE, problem(503, "temporarily_unavailable"));

    Throwable failure = failureOf(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertInstanceOf(ApiException.class, failure);
    assertEquals(503, ((TeachMeHipaaException) failure).getStatusCode());
    assertEquals(3, api.requestsTo(LEARNER_ROUTE).size());
    assertEquals(2, asyncSleeper.sleeps().size());
  }

  @Test
  void neverRetriesAWriteThatIsNotSafeToReplay() {
    String route = "PATCH " + ORG + "/learners/" + LEARNER_ID;
    FakeTransport api = api().on(route, problem(503, "temporarily_unavailable"));

    Throwable failure =
        failureOf(
            clientOn(api)
                .build()
                .learners()
                .updateAsync(
                    LEARNER_ID,
                    com.teachmehipaa.param.LearnerUpdateParams.builder()
                        .ifMatch(ETAG)
                        .name("Ana")
                        .build()));

    assertInstanceOf(ApiException.class, failure);
    assertEquals(1, api.requestsTo(route).size());
    assertEquals(List.of(), asyncSleeper.sleeps());
  }

  @Test
  void retriesAFailedMintOnTheSameSchedule() {
    FakeTransport api =
        new FakeTransport()
            .fail(TOKEN_ROUTE, new ConnectException("Connection refused"))
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    await(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(List.of(Duration.ofMillis(500)), asyncSleeper.sleeps());
  }

  // Tokens.

  @Test
  void concurrentCallsWithAColdCacheShareOneMint() throws InterruptedException {
    PendingTransport api = new PendingTransport();
    TeachMeHipaaClient client = clientOn(api).build();

    List<CompletableFuture<Learner>> calls =
        IntStream.range(0, 16).mapToObj(i -> client.learners().retrieveAsync(LEARNER_ID)).toList();

    assertEquals(1, api.to(TOKEN_ROUTE).size());
    api.answer(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
    List<PendingTransport.Exchange> retrieves = api.to(LEARNER_ROUTE);
    assertEquals(16, retrieves.size());
    retrieves.forEach(
        exchange -> exchange.response().complete(Reply.json(LEARNER_JSON).toResponse()));
    calls.forEach(call -> assertEquals(LEARNER_ID, await(call).getLearnerId()));
    assertEquals(1, api.to(TOKEN_ROUTE).size());
  }

  @Test
  void concurrentCallsWithNoOrganizationShareOneLookup() throws InterruptedException {
    PendingTransport api = new PendingTransport();
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .httpTransport(api)
            .asyncSleeper(asyncSleeper)
            .build();

    List<CompletableFuture<Learner>> calls =
        IntStream.range(0, 8).mapToObj(i -> client.learners().retrieveAsync(LEARNER_ID)).toList();
    api.answer(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
    api.answer(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")));
    api.to(LEARNER_ROUTE)
        .forEach(exchange -> exchange.response().complete(Reply.json(LEARNER_JSON).toResponse()));

    calls.forEach(call -> assertEquals(LEARNER_ID, await(call).getLearnerId()));
    assertEquals(1, api.to(INTROSPECTION_ROUTE).size());
    assertEquals(8, api.to(LEARNER_ROUTE).size());
  }

  @Test
  void aFailedSharedMintFailsEveryWaiterAndIsNotCached() throws InterruptedException {
    PendingTransport api = new PendingTransport();
    TeachMeHipaaClient client = clientOn(api).maxRetries(0).build();
    String refusal =
        """
        {"error": "invalid_client", "error_description": "Client authentication failed"}
        """;

    CompletableFuture<Learner> first = client.learners().retrieveAsync(LEARNER_ID);
    CompletableFuture<Learner> second = client.learners().retrieveAsync(LEARNER_ID);
    api.answer(TOKEN_ROUTE, Reply.json(401, refusal));

    assertInstanceOf(OAuthException.class, failureOf(first));
    assertInstanceOf(OAuthException.class, failureOf(second));
    client.learners().retrieveAsync(LEARNER_ID);
    assertEquals(2, api.to(TOKEN_ROUTE).size());
  }

  @Test
  void remintsSixtySecondsBeforeExpiry() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(TOKEN_ROUTE, Reply.json(tokenJson(SECOND_ACCESS_TOKEN)))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = clientOn(api).build();

    await(client.learners().retrieveAsync(LEARNER_ID));
    // The token lives 900 s; at 839 s it is still more than 60 s from expiry.
    clock.advance(Duration.ofSeconds(839));
    await(client.learners().retrieveAsync(LEARNER_ID));
    clock.advance(Duration.ofSeconds(1));
    await(client.learners().retrieveAsync(LEARNER_ID));

    assertEquals(
        List.of(
            "Bearer " + ACCESS_TOKEN, "Bearer " + ACCESS_TOKEN, "Bearer " + SECOND_ACCESS_TOKEN),
        api.requestsTo(LEARNER_ROUTE).stream()
            .map(request -> request.headers().get("authorization"))
            .toList());
  }

  @Test
  void aRefused401TokenIsInvalidatedReMintedAndReplayedOnce() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(TOKEN_ROUTE, Reply.json(tokenJson(SECOND_ACCESS_TOKEN)))
            .on(LEARNER_ROUTE, problem(401, "unauthorized"))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    await(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertEquals(2, api.requestsTo(TOKEN_ROUTE).size());
    assertEquals(
        List.of("Bearer " + ACCESS_TOKEN, "Bearer " + SECOND_ACCESS_TOKEN),
        api.requestsTo(LEARNER_ROUTE).stream()
            .map(request -> request.headers().get("authorization"))
            .toList());
    assertEquals(List.of(), asyncSleeper.sleeps());
  }

  @Test
  void aSecond401FailsWithAuthenticationException() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(TOKEN_ROUTE, Reply.json(tokenJson(SECOND_ACCESS_TOKEN)))
            .on(LEARNER_ROUTE, problem(401, "unauthorized"));

    Throwable failure = failureOf(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertInstanceOf(AuthenticationException.class, failure);
    assertEquals(2, api.requestsTo(LEARNER_ROUTE).size());
  }

  // Errors.

  static Stream<Arguments> failures() {
    return Stream.of(
        Arguments.of(problem(400, "invalid_request"), InvalidRequestException.class),
        Arguments.of(problem(403, "forbidden"), PermissionDeniedException.class),
        Arguments.of(problem(404, "not_found"), NotFoundException.class),
        Arguments.of(problem(409, "already_exists"), ConflictException.class),
        Arguments.of(problem(412, "precondition_failed"), PreconditionFailedException.class),
        Arguments.of(problem(422, "idempotency_key_reuse"), IdempotencyException.class),
        Arguments.of(problem(429, "rate_limited"), RateLimitException.class),
        Arguments.of(problem(500, "internal_error"), ApiException.class),
        Arguments.of(
            new Reply(200, java.util.Map.of("content-type", "text/html"), "<html></html>"),
            ApiException.class));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("failures")
  void failsWithTheSameExceptionAsTheBlockingCallNeverACompletionException(
      Reply reply, Class<? extends TeachMeHipaaException> expected) {
    FakeTransport api = api().on(LEARNER_ROUTE, reply);
    TeachMeHipaaClient client = clientOn(api).build();
    TeachMeHipaaException blocking =
        assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));

    CompletableFuture<Learner> future = client.learners().retrieveAsync(LEARNER_ID);

    Throwable handled = failureOf(future);
    assertSame(expected, handled.getClass());
    assertSame(blocking.getClass(), handled.getClass());
    assertEquals(blocking.getMessage(), handled.getMessage());
    ExecutionException viaGet = assertThrows(ExecutionException.class, future::get);
    assertSame(handled, viaGet.getCause());
    CompletionException viaJoin = assertThrows(CompletionException.class, future::join);
    assertSame(handled, viaJoin.getCause());
    AtomicReference<Throwable> recovered = new AtomicReference<>();
    future
        .exceptionally(
            failure -> {
              recovered.set(failure);
              return null;
            })
        .join();
    assertSame(handled, recovered.get());
  }

  @Test
  void aConnectionThatNeverAnswersFailsWithApiConnectionException() {
    FakeTransport api = api().fail(LEARNER_ROUTE, new ConnectException("Connection refused"));

    Throwable failure = failureOf(clientOn(api).build().learners().retrieveAsync(LEARNER_ID));

    assertInstanceOf(ApiConnectionException.class, failure);
    assertEquals(3, api.requestsTo(LEARNER_ROUTE).size());
  }

  // Cancellation.

  @Test
  void cancellingDuringARetryWaitCancelsTheWaitAndSendsNothingMore() {
    FakeAsyncSleeper holding = FakeAsyncSleeper.holding();
    FakeTransport api =
        api()
            .on(LEARNER_ROUTE, problem(503, "temporarily_unavailable"))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));
    CompletableFuture<Learner> learner =
        clientOn(api).asyncSleeper(holding).build().learners().retrieveAsync(LEARNER_ID);
    await(waitFor(() -> holding.waits().size() == 1));

    assertTrue(learner.cancel(true));
    holding.release();

    assertTrue(holding.waits().get(0).isCancelled());
    assertTrue(learner.isCancelled());
    assertEquals(1, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void cancellingDuringARequestCancelsTheTransportsFuture() throws InterruptedException {
    PendingTransport api = new PendingTransport();
    TeachMeHipaaClient client = clientOn(api).build();
    CompletableFuture<Learner> learner = client.learners().retrieveAsync(LEARNER_ID);
    api.answer(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));

    learner.cancel(true);

    assertTrue(api.to(LEARNER_ROUTE).get(0).response().isCancelled());
    assertThrows(CancellationException.class, learner::join);
  }

  @Test
  void cancellingOneCallerLeavesTheSharedMintRunningForTheOthers() throws InterruptedException {
    PendingTransport api = new PendingTransport();
    TeachMeHipaaClient client = clientOn(api).build();
    CompletableFuture<Learner> cancelled = client.learners().retrieveAsync(LEARNER_ID);
    CompletableFuture<Learner> kept = client.learners().retrieveAsync(LEARNER_ID);

    cancelled.cancel(true);
    assertFalse(api.to(TOKEN_ROUTE).get(0).response().isDone());
    api.answer(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
    api.answer(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    assertEquals(LEARNER_ID, await(kept).getLearnerId());
    assertEquals(1, api.to(LEARNER_ROUTE).size());
  }

  @Test
  void cancellingAWaitForCompletionStopsThePolls() {
    FakeAsyncSleeper holding = FakeAsyncSleeper.holding();
    FakeTransport api =
        api()
            .on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON))
            .on(POLL_ROUTE, Reply.json(PENDING_OPERATION_JSON));
    WebhookDeliveryOperation pending =
        clientOn(api).asyncSleeper(holding).build().webhookSubscriptions().test(SUBSCRIPTION_ID);

    CompletableFuture<WebhookDeliveryOperation> done = pending.waitForCompletionAsync();
    assertEquals(1, holding.waits().size());
    done.cancel(true);
    holding.release();

    assertTrue(holding.waits().get(0).isCancelled());
    assertEquals(List.of(), api.requestsTo(POLL_ROUTE));
  }

  // Logging.

  @Test
  void logsEveryAttemptExactlyAsTheBlockingPathDoes() {
    List<RequestLogEntry> blocking = new CopyOnWriteArrayList<>();
    List<RequestLogEntry> async = new CopyOnWriteArrayList<>();
    Function<List<RequestLogEntry>, TeachMeHipaaClient> client =
        entries ->
            clientOn(
                    api()
                        .fail(LEARNER_ROUTE, new HttpTimeoutException("request timed out"))
                        .on(LEARNER_ROUTE, problem(503, "temporarily_unavailable"))
                        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON)))
                .logger(entries::add)
                .build();

    client.apply(blocking).learners().retrieve(LEARNER_ID);
    await(client.apply(async).learners().retrieveAsync(LEARNER_ID));

    assertEquals(
        List.of(
            new RequestLogEntry("POST", "/v1/auth/token", 200, Duration.ZERO, null),
            new RequestLogEntry(
                "GET",
                ORG + "/learners/" + LEARNER_ID,
                null,
                Duration.ZERO,
                null,
                ErrorKind.TIMEOUT),
            new RequestLogEntry("GET", ORG + "/learners/" + LEARNER_ID, 503, Duration.ZERO, null),
            new RequestLogEntry("GET", ORG + "/learners/" + LEARNER_ID, 200, Duration.ZERO, null)),
        async);
    assertEquals(blocking, async);
  }

  // Pages.

  @Test
  void nextPageAsyncFetchesThePageAfterWithTheSameParameters() {
    FakeTransport api =
        api()
            .on(
                LEARNERS_ROUTE,
                Reply.json("{\"items\": [" + LEARNER_JSON + "], \"nextCursor\": \"c2\"}"))
            .on(LEARNERS_ROUTE, Reply.json("{\"items\": [], \"nextCursor\": null}"));
    TeachMeHipaaClient client = clientOn(api).build();

    Page<Learner> first =
        await(client.learners().listAsync(LearnerListParams.builder().limit(1L).build()));
    Page<Learner> second = await(first.nextPageAsync());

    assertEquals(1, first.items().size());
    assertFalse(second.hasNextPage());
    assertEquals(
        List.of("limit=1", "limit=1&cursor=c2"),
        api.requestsTo(LEARNERS_ROUTE).stream().map(r -> r.uri().getRawQuery()).toList());
    assertThrows(NoSuchElementException.class, second::nextPageAsync);
    assertEquals(2, api.requestsTo(LEARNERS_ROUTE).size());
  }

  @Test
  void aPageReadAsynchronouslyStillPagesBlockingAndTheOtherWayRound() {
    FakeTransport api =
        api()
            .on(
                LEARNERS_ROUTE,
                Reply.json("{\"items\": [" + LEARNER_JSON + "], \"nextCursor\": \"c2\"}"))
            .on(
                LEARNERS_ROUTE,
                Reply.json("{\"items\": [" + LEARNER_JSON + "], \"nextCursor\": null}"));
    TeachMeHipaaClient client = clientOn(api).build();

    Page<Learner> blockingFirst = client.learners().list();
    Page<Learner> asyncSecond = await(blockingFirst.nextPageAsync());

    assertEquals(1, asyncSecond.items().size());
    assertEquals(2, api.requestsTo(LEARNERS_ROUTE).size());
  }

  // Operations.

  @Test
  void waitForCompletionAsyncPollsWithTheBlockingBackoffAndKeepsTheOperationsClass() {
    FakeTransport api =
        api()
            .on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON))
            .on(POLL_ROUTE, Reply.json(PENDING_OPERATION_JSON))
            .on(POLL_ROUTE, Reply.json(PENDING_OPERATION_JSON))
            .on(POLL_ROUTE, Reply.json(SENT_OPERATION_JSON));

    WebhookDeliveryOperation done =
        await(
            clientOn(api)
                .build()
                .webhookSubscriptions()
                .testAsync(SUBSCRIPTION_ID)
                .thenCompose(WebhookDeliveryOperation::waitForCompletionAsync));

    assertEquals(new WebhookDeliveryResult.Sent(200), done.getDeliveryResult());
    List<Duration> waits = asyncSleeper.sleeps();
    assertEquals(3, waits.size());
    assertEquals(Duration.ofSeconds(1), waits.get(0));
    assertTrue(waits.get(1).toMillis() >= 1000 && waits.get(1).toMillis() <= 2000, "" + waits);
    assertTrue(waits.get(2).toMillis() >= 2000 && waits.get(2).toMillis() <= 4000, "" + waits);
    assertEquals(List.of(), sleeper.sleeps());
  }

  @Test
  void waitForCompletionAsyncFailsWithOperationFailedException() {
    FakeTransport api =
        api()
            .on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON))
            .on(POLL_ROUTE, Reply.json(FAILED_OPERATION_JSON));
    WebhookDeliveryOperation pending =
        clientOn(api).build().webhookSubscriptions().test(SUBSCRIPTION_ID);

    Throwable failure = failureOf(pending.waitForCompletionAsync());

    OperationFailedException failed = assertInstanceOf(OperationFailedException.class, failure);
    assertEquals("temporarily_unavailable", failed.getCode());
  }

  @Test
  void waitForCompletionAsyncTimesOutWithTheLastPollOnTheDeadline() {
    FakeTransport api =
        api()
            .on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON))
            .on(POLL_ROUTE, Reply.json(PENDING_OPERATION_JSON));
    WebhookDeliveryOperation pending =
        clientOn(api).build().webhookSubscriptions().test(SUBSCRIPTION_ID);

    Throwable failure =
        failureOf(pending.waitForCompletionAsync(Duration.ofSeconds(5), Duration.ofSeconds(1)));

    assertInstanceOf(OperationTimeoutException.class, failure);
    Duration waited = asyncSleeper.sleeps().stream().reduce(Duration.ZERO, Duration::plus);
    assertEquals(Duration.ofSeconds(5), waited);
    assertEquals(asyncSleeper.sleeps().size(), api.requestsTo(POLL_ROUTE).size());
  }

  @Test
  void aDoneOperationCompletesAtOnceWithoutARequest() {
    FakeTransport api = api().on(TEST_DELIVERY_ROUTE, Reply.json(202, SENT_OPERATION_JSON));
    Operation done = clientOn(api).build().webhookSubscriptions().test(SUBSCRIPTION_ID);

    CompletableFuture<? extends Operation> waited = done.waitForCompletionAsync();

    assertTrue(waited.isDone());
    assertSame(done, waited.join());
    assertEquals(List.of(), api.requestsTo(POLL_ROUTE));
  }

  @Test
  void waitForCompletionAsyncRejectsABadTimeoutBeforeSendingAnything() {
    FakeTransport api = api().on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON));
    Operation pending = clientOn(api).build().webhookSubscriptions().test(SUBSCRIPTION_ID);

    assertThrows(
        IllegalArgumentException.class,
        () -> pending.waitForCompletionAsync(Duration.ofSeconds(-1), Duration.ofSeconds(1)));
    assertEquals(List.of(), api.requestsTo(POLL_ROUTE));
  }

  /** Completes once {@code condition} holds; work on library threads may still be finishing. */
  private static CompletableFuture<Void> waitFor(java.util.function.BooleanSupplier condition) {
    return CompletableFuture.runAsync(
        () -> {
          long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
          while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
              throw new AssertionError("timed out");
            }
            Thread.onSpinWait();
          }
        });
  }
}
