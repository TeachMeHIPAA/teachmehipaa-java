package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.CLIENT_ID;
import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.FieldError;
import com.teachmehipaa.exception.OperationFailedException;
import com.teachmehipaa.exception.OperationInterruptedException;
import com.teachmehipaa.exception.OperationTimeoutException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.CertificatesExportRequestFormat;
import com.teachmehipaa.model.EnrollmentsExportRequestFormat;
import com.teachmehipaa.model.ExportResult;
import com.teachmehipaa.model.LearnersExportRequestFormat;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.model.WebhookDeliveryOperation;
import com.teachmehipaa.model.WebhookDeliveryResult;
import com.teachmehipaa.param.CertificateExportParams;
import com.teachmehipaa.param.EnrollmentExportParams;
import com.teachmehipaa.param.LearnerExportParams;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Operations: waiting for completion and reading export CSVs, through the public client. */
class OperationsTest {
  private static final String ORG = "/v1/organizations/" + ORG_ID;
  private static final String OPERATION_ID = "0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d";
  // The Developer API names an Operation organizations/{orgId}/operations/{operationId}.
  private static final String OPERATION_NAME =
      "organizations/" + ORG_ID + "/operations/" + OPERATION_ID;
  private static final String POLL_ROUTE = "GET " + ORG + "/operations/" + OPERATION_ID;
  private static final String SUBSCRIPTION_ID = "whs_01JD8K2M4N6P8R0T2V4X6Z8B0D";
  private static final String TEST_DELIVERY_ROUTE =
      "POST " + ORG + "/webhook-subscriptions/" + SUBSCRIPTION_ID + ":test";
  private static final String ENROLLMENTS_EXPORT_ROUTE = "POST " + ORG + "/enrollments:export";

  // An export's 202: a done Operation whose response is the monorepo's ExportArtifact
  // (api-contracts bulk.schemas.ts), the CSV in base64.
  private static final String EXPORT_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": true,
       "metadata": {"kind": "enrollments:export"},
       "response": {
         "format": "csv",
         "contentType": "text/csv; charset=utf-8",
         "filename": "enrollments-org_acme.csv",
         "rowCount": 2,
         "dataBase64": "bGVhcm5lcklkLGVtYWlsLG5hbWUNCnN1Yl8wMUpEODBBQjJDM0Q0RTVGNkc3SDhKOUswTSxhbmFAZXhhbXBsZS5jb20sIkV4YW1wbGUsIEFuYSINCnN1Yl8wMUpEODBBQjJDM0Q0RTVGNkc3SDhKOUswTixqb3NlQGV4YW1wbGUuY29tLEpvc8OpIE7DusOxZXoNCg=="
       }}
      """;

  // What EXPORT_OPERATION_JSON's dataBase64 decodes to: RFC 4180, CRLF, UTF-8.
  private static final String EXPORTED_CSV =
      "learnerId,email,name\r\n"
          + "sub_01JD80AB2C3D4E5F6G7H8J9K0M,ana@example.com,\"Example, Ana\"\r\n"
          + "sub_01JD80AB2C3D4E5F6G7H8J9K0N,jose@example.com,José Núñez\r\n";

  // A Test delivery's 202: pending until the webhook worker settles it.
  private static final String PENDING_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": false}
      """;
  // The worker's settle on a 2xx from the endpoint (§8: response._tag).
  private static final String SENT_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": true, "response": {"_tag": "sent", "responseStatus": 200}}
      """;

  // dev#/components/schemas/Operation: a done Operation with a problem.
  private static final String FAILED_OPERATION_JSON =
      """
      {"name": "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
       "done": true,
       "error": {
         "title": "Temporarily unavailable",
         "status": 503,
         "code": "temporarily_unavailable",
         "detail": "Webhook delivery is paused. Try again later.",
         "errors": [{"path": "url", "message": "unreachable"}]
       }}
      """;

  private final FakeClock clock = new FakeClock();
  private final FakeSleeper sleeper = new FakeSleeper(clock);

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private TeachMeHipaaClient clientOn(FakeTransport api) {
    return TeachMeHipaaClient.builder()
        .clientId(CLIENT_ID)
        .clientSecret(CLIENT_SECRET)
        .organizationId(ORG_ID)
        .httpTransport(api)
        .clock(clock)
        .sleeper(sleeper)
        .maxRetries(0)
        .build();
  }

  /** A Test delivery that starts pending, then polls through {@code replies}. */
  private static FakeTransport pendingThen(Reply... replies) {
    FakeTransport api = api().on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON));
    for (Reply reply : replies) {
      api.on(POLL_ROUTE, reply);
    }
    return api;
  }

  private static Reply pending() {
    return Reply.json(PENDING_OPERATION_JSON);
  }

  @Test
  void aDoneOperationReturnsFromWaitForCompletionWithoutARequest() {
    FakeTransport api = api().on(ENROLLMENTS_EXPORT_ROUTE, Reply.json(202, EXPORT_OPERATION_JSON));

    Operation operation =
        clientOn(api)
            .enrollments()
            .export(
                EnrollmentExportParams.builder()
                    .format(EnrollmentsExportRequestFormat.CSV)
                    .build());
    Operation finished = operation.waitForCompletion();

    assertSame(operation, finished);
    assertTrue(finished.isDone());
    assertEquals(OPERATION_NAME, finished.getName());
    assertEquals(202, finished.getLastResponse().statusCode());
    assertEquals(2, api.requests().size());
    assertEquals(0, sleeper.sleeps().size());
  }

  @Test
  void aPendingOperationPollsUntilItIsDone() {
    FakeTransport api =
        pendingThen(
            pending(),
            pending(),
            Reply.json(SENT_OPERATION_JSON).withHeader("x-request-id", "req_poll_3"));

    WebhookDeliveryOperation operation = clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID);
    WebhookDeliveryOperation finished = operation.waitForCompletion();

    assertFalse(operation.isDone());
    assertTrue(finished.isDone());
    assertEquals("req_poll_3", finished.getLastResponse().requestId());
    List<TransportRequest> polls = api.requestsTo(POLL_ROUTE);
    assertEquals(3, polls.size());
    assertEquals("Bearer " + ACCESS_TOKEN, polls.get(0).headers().get("authorization"));
    assertEquals(3, sleeper.sleeps().size());
  }

  @Test
  void pollsBackOffFromOneSecondDoublingWithJitterToATenSecondCap() {
    FakeTransport api =
        pendingThen(
            pending(),
            pending(),
            pending(),
            pending(),
            pending(),
            pending(),
            pending(),
            Reply.json(SENT_OPERATION_JSON));

    clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID).waitForCompletion();

    // Ceilings 1, 2, 4, 8, 10, 10, 10, 10 s; jitter keeps each within 50-100 % of its ceiling,
    // and never below the 1 s interval.
    long[][] rangesMillis = {
      {1000, 1000}, {1000, 2000}, {2000, 4000}, {4000, 8000},
      {5000, 10000}, {5000, 10000}, {5000, 10000}, {5000, 10000}
    };
    List<Duration> sleeps = sleeper.sleeps();
    assertEquals(rangesMillis.length, sleeps.size());
    for (int i = 0; i < rangesMillis.length; i++) {
      long waited = sleeps.get(i).toMillis();
      assertTrue(
          rangesMillis[i][0] <= waited && waited <= rangesMillis[i][1],
          "wait " + (i + 1) + " was " + waited + " ms");
    }
  }

  @Test
  void pollsStartFromTheIntervalGiven() {
    FakeTransport api = pendingThen(pending(), Reply.json(SENT_OPERATION_JSON));

    clientOn(api)
        .webhookSubscriptions()
        .test(SUBSCRIPTION_ID)
        .waitForCompletion(Duration.ofMinutes(5), Duration.ofMillis(250));

    List<Duration> sleeps = sleeper.sleeps();
    assertEquals(Duration.ofMillis(250), sleeps.get(0));
    assertTrue(sleeps.get(1).toMillis() >= 250 && sleeps.get(1).toMillis() <= 500);
  }

  @Test
  void timesOutWithTheLastOperationAfterAFinalPollAtTheDeadline() {
    FakeTransport api = pendingThen(pending().withHeader("x-request-id", "req_poll_last"));
    WebhookDeliveryOperation operation = clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID);

    OperationTimeoutException error =
        assertThrows(
            OperationTimeoutException.class,
            () -> operation.waitForCompletion(Duration.ofSeconds(5), Duration.ofSeconds(1)));

    assertTrue(error instanceof TeachMeHipaaException);
    assertEquals(
        "Operation " + OPERATION_NAME + " was not done after 5 s (request id: req_poll_last)",
        error.getMessage());
    assertFalse(error.getOperation().isDone());
    assertEquals(OPERATION_NAME, error.getOperation().getName());
    assertEquals("req_poll_last", error.getRequestId());
    Duration waited = sleeper.sleeps().stream().reduce(Duration.ZERO, Duration::plus);
    assertEquals(Duration.ofSeconds(5), waited);
    assertEquals(sleeper.sleeps().size(), api.requestsTo(POLL_ROUTE).size());
  }

  @Test
  void aFailedOperationRaisesOperationFailedExceptionWithTheProblem() {
    FakeTransport api =
        pendingThen(Reply.json(FAILED_OPERATION_JSON).withHeader("x-request-id", "req_poll_1"));
    WebhookDeliveryOperation operation = clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID);

    OperationFailedException error =
        assertThrows(OperationFailedException.class, operation::waitForCompletion);

    assertTrue(error instanceof TeachMeHipaaException);
    assertEquals(
        "Temporarily unavailable: Webhook delivery is paused. Try again later."
            + " (request id: req_poll_1)",
        error.getMessage());
    assertEquals(503, error.getStatusCode());
    assertEquals("temporarily_unavailable", error.getCode());
    assertEquals("Temporarily unavailable", error.getTitle());
    assertEquals("Webhook delivery is paused. Try again later.", error.getDetail());
    assertEquals(List.of(new FieldError("url", "unreachable")), error.getErrors());
    assertEquals("req_poll_1", error.getRequestId());
    assertTrue(error.getOperation().isDone());
  }

  @Test
  void anOperationAlreadyDoneWithAnErrorRaisesWithoutARequest() {
    FakeTransport api = api().on(TEST_DELIVERY_ROUTE, Reply.json(202, FAILED_OPERATION_JSON));
    WebhookDeliveryOperation operation = clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID);

    assertThrows(OperationFailedException.class, operation::waitForCompletion);

    assertEquals(List.of(), api.requestsTo(POLL_ROUTE));
  }

  @Test
  void anInterruptStopsTheWaitWithoutAPollAndKeepsTheInterruptFlag() {
    FakeTransport api = pendingThen(Reply.json(SENT_OPERATION_JSON));
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .organizationId(ORG_ID)
            .httpTransport(api)
            .sleeper(
                duration -> {
                  throw new InterruptedException();
                })
            .build();
    WebhookDeliveryOperation operation = client.webhookSubscriptions().test(SUBSCRIPTION_ID);

    try {
      OperationInterruptedException error =
          assertThrows(OperationInterruptedException.class, operation::waitForCompletion);

      assertTrue(Thread.currentThread().isInterrupted());
      assertTrue(error instanceof TeachMeHipaaException);
      assertTrue(error.getCause() instanceof InterruptedException);
      assertSame(operation, error.getOperation());
      assertEquals(List.of(), api.requestsTo(POLL_ROUTE));
    } finally {
      Thread.interrupted();
    }
  }

  static Stream<Arguments> exports() {
    return Stream.of(
        Arguments.of(
            "enrollments",
            (Function<TeachMeHipaaClient, Operation>)
                client ->
                    client
                        .enrollments()
                        .export(
                            EnrollmentExportParams.builder()
                                .format(EnrollmentsExportRequestFormat.CSV)
                                .build())),
        Arguments.of(
            "certificates",
            (Function<TeachMeHipaaClient, Operation>)
                client ->
                    client
                        .certificates()
                        .export(
                            CertificateExportParams.builder()
                                .format(CertificatesExportRequestFormat.CSV)
                                .build())),
        Arguments.of(
            "learners",
            (Function<TeachMeHipaaClient, Operation>)
                client ->
                    client
                        .learners()
                        .export(
                            LearnerExportParams.builder()
                                .format(LearnersExportRequestFormat.CSV)
                                .courseId("hipaa-2026")
                                .build())));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exports")
  void anExportResultIsTheCsvTextFileNameAndRowCount(
      String resource, Function<TeachMeHipaaClient, Operation> export) {
    FakeTransport api =
        api()
            .on("POST " + ORG + "/" + resource + ":export", Reply.json(202, EXPORT_OPERATION_JSON));

    ExportResult result = export.apply(clientOn(api)).getExportResult();

    assertEquals(EXPORTED_CSV, result.csv());
    assertEquals("enrollments-org_acme.csv", result.filename());
    assertEquals(2L, result.rowCount());
  }

  @Test
  void anExportRetrievedByIdReadsTheSameWay() {
    FakeTransport api = api().on(POLL_ROUTE, Reply.json(EXPORT_OPERATION_JSON));

    Operation operation = clientOn(api).operations().retrieve(OPERATION_ID);

    assertEquals(EXPORTED_CSV, operation.getExportResult().csv());
  }

  @Test
  void anExportResultLeavesTheCsvOutOfToString() {
    FakeTransport api = api().on(POLL_ROUTE, Reply.json(EXPORT_OPERATION_JSON));

    ExportResult result = clientOn(api).operations().retrieve(OPERATION_ID).getExportResult();

    assertFalse(result.toString().contains("ana@example.com"), result.toString());
    assertTrue(result.toString().contains("enrollments-org_acme.csv"), result.toString());
  }

  @Test
  void anExportResultRefusesAnOperationThatIsNotDone() {
    FakeTransport api = api().on(POLL_ROUTE, pending());
    Operation operation = clientOn(api).operations().retrieve(OPERATION_ID);

    IllegalStateException error =
        assertThrows(IllegalStateException.class, operation::getExportResult);

    assertEquals(
        "Operation " + OPERATION_NAME + " is not done; call waitForCompletion() first",
        error.getMessage());
  }

  @Test
  void anExportResultRefusesAnOperationThatIsNotAnExport() {
    FakeTransport api = api().on(POLL_ROUTE, Reply.json(SENT_OPERATION_JSON));
    Operation operation = clientOn(api).operations().retrieve(OPERATION_ID);

    IllegalStateException error =
        assertThrows(IllegalStateException.class, operation::getExportResult);

    assertEquals(
        "Operation " + OPERATION_NAME + " did not produce a CSV export", error.getMessage());
  }

  @Test
  void anExportResultOfAFailedExportRaisesOperationFailedException() {
    FakeTransport api = api().on(POLL_ROUTE, Reply.json(FAILED_OPERATION_JSON));
    Operation operation = clientOn(api).operations().retrieve(OPERATION_ID);

    assertThrows(OperationFailedException.class, operation::getExportResult);
  }

  @Test
  void aTestDeliveryReadsAsASentDeliveryResultOnceDone() {
    FakeTransport api = pendingThen(Reply.json(SENT_OPERATION_JSON));

    WebhookDeliveryResult result =
        clientOn(api)
            .webhookSubscriptions()
            .test(SUBSCRIPTION_ID)
            .waitForCompletion()
            .getDeliveryResult();

    assertEquals(new WebhookDeliveryResult.Sent(200), result);
    assertEquals("sent", result.tag());
  }

  static Stream<Arguments> deliveryResults() {
    return Stream.of(
        Arguments.of(
            "{\"_tag\": \"failed\", \"reason\": \"endpoint_gone\", \"responseStatus\": 410}",
            new WebhookDeliveryResult.Failed("endpoint_gone", 410)),
        Arguments.of(
            "{\"_tag\": \"failed\", \"reason\": \"connection_refused\"}",
            new WebhookDeliveryResult.Failed("connection_refused", null)),
        Arguments.of(
            "{\"_tag\": \"settled\", \"status\": \"delivered\"}",
            new WebhookDeliveryResult.Settled("delivered")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("deliveryResults")
  void aRedriveReadsEachDeliveryResultByTag(String response, WebhookDeliveryResult expected) {
    String done =
        "{\"name\": \"" + OPERATION_NAME + "\", \"done\": true, \"response\": " + response + "}";
    FakeTransport api =
        api()
            .on(
                "POST " + ORG + "/webhook-subscriptions/" + SUBSCRIPTION_ID + ":redrive",
                Reply.json(202, done));

    WebhookDeliveryOperation operation =
        clientOn(api).webhookSubscriptions().redrive(SUBSCRIPTION_ID);

    assertEquals(expected, operation.getDeliveryResult());
  }

  @Test
  void aDeliveryResultWithAnUnknownTagIsKeptNotRejected() throws IOException {
    String unknown = "{\"_tag\": \"skipped\", \"why\": \"nothing to deliver\"}";
    FakeTransport api =
        pendingThen(
            Reply.json(
                "{\"name\": \""
                    + OPERATION_NAME
                    + "\", \"done\": true, \"response\": "
                    + unknown
                    + "}"));

    WebhookDeliveryResult result =
        clientOn(api)
            .webhookSubscriptions()
            .test(SUBSCRIPTION_ID)
            .waitForCompletion()
            .getDeliveryResult();

    WebhookDeliveryResult.Unknown kept =
        assertInstanceOf(WebhookDeliveryResult.Unknown.class, result);
    assertEquals("skipped", kept.tag());
    assertEquals(new ObjectMapper().readTree(unknown), kept.response());
  }

  @Test
  void aDeliveryResultRefusesAnOperationThatIsNotDone() {
    FakeTransport api = api().on(TEST_DELIVERY_ROUTE, Reply.json(202, PENDING_OPERATION_JSON));
    WebhookDeliveryOperation operation = clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID);

    assertThrows(IllegalStateException.class, operation::getDeliveryResult);
  }

  @Test
  void aDeliveryResultOfAFailedOperationRaisesOperationFailedException() {
    FakeTransport api = api().on(TEST_DELIVERY_ROUTE, Reply.json(202, FAILED_OPERATION_JSON));
    WebhookDeliveryOperation operation = clientOn(api).webhookSubscriptions().test(SUBSCRIPTION_ID);

    assertThrows(OperationFailedException.class, operation::getDeliveryResult);
  }

  @Test
  void operationIdFromNameReadsTheIdThatOperationsRetrieveTakes() {
    assertEquals(
        "0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
        Operation.operationIdFromName(
            "organizations/org_acme/operations/0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0b7d4a8e-5c1f-4e2a-9d3b-6f8a1c2e4b7d",
        "organizations/org_acme/operations/",
        "organizations//operations/op_1",
        "organizations/org_acme/learners/sub_01JD80AB2C3D4E5F6G7H8J9K0M",
        "organizations/org_acme/operations/op_1/extra",
        "/organizations/org_acme/operations/op_1",
        "organizations/org acme/operations/op_1"
      })
  void operationIdFromNameRejectsAnythingButAnOperationName(String name) {
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> Operation.operationIdFromName(name));

    assertTrue(error.getMessage().contains("is not an Operation name"), error.getMessage());
  }
}
