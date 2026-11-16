package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.REQUEST_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.tokenJson;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.teachmehipaa.core.TransportRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/** The samples in {@code docs/guides/*.md} (see {@link DocSampleTest}), one check each. */
class GuidesTest extends DocSampleTest {
  private static final String STATIC_TOKEN = "eyJhbGciOiJIUzI1NiJ9.static.sig";
  private static final String ORG_LEARNER = "GET " + ORG + "/learners/";

  @Override
  List<Path> docs() {
    return markdownIn("docs/guides");
  }

  @Override
  Map<String, Check> checks() {
    return Map.ofEntries(
        // getting-started.md
        Map.entry("getting-started-first-call", this::gettingStartedFirstCall),
        Map.entry("getting-started-invite", this::gettingStartedInvite),
        Map.entry("getting-started-upsert", this::gettingStartedUpsert),
        Map.entry("getting-started-errors", this::gettingStartedErrors),
        // authentication.md
        Map.entry("auth-client-credentials", this::authClientCredentials),
        Map.entry("auth-scopes", this::authScopes),
        Map.entry("organization", this::organization),
        Map.entry("auth-static-token", this::authStaticToken),
        Map.entry("public-lookups", this::publicLookups),
        // errors-and-retries.md
        Map.entry("errors", this::errors),
        Map.entry("errors-code", this::errorsCode),
        Map.entry("request-options", this::requestOptions),
        // pagination-and-events.md
        Map.entry("pagination", this::pagination),
        Map.entry("pagination-pages", this::paginationPages),
        Map.entry("events-list", this::eventsList),
        Map.entry("events-tail", this::eventsTail),
        // webhooks.md
        Map.entry("webhook-subscription-create", this::webhookSubscriptionCreate),
        Map.entry("webhook-spring", this::webhookSpring),
        Map.entry("webhook-servlet", this::webhookServlet),
        Map.entry("webhook-jdk-server", this::webhookJdkServer),
        Map.entry("webhook-dedupe", this::webhookDedupe),
        Map.entry("webhook-delivery-operation", this::webhookDeliveryOperation),
        Map.entry("webhook-test-headers", this::webhookTestHeaders),
        Map.entry("webhook-verify-options", this::webhookVerifyOptions),
        // operations-and-files.md
        Map.entry("operations", this::operations),
        Map.entry("operations-resume", this::operationsResume),
        Map.entry("csv-import", this::csvImport),
        Map.entry("pdf-download", this::pdfDownload),
        Map.entry("bulk-changes", this::bulkChanges),
        // conditional-requests.md
        Map.entry("conditional-write", this::conditionalWrite),
        Map.entry("conditional-update-subscription", this::conditionalUpdateSubscription),
        Map.entry("not-modified", this::notModified),
        // testing.md
        Map.entry("testing-sandbox", this::testingSandbox),
        Map.entry("testing-fake-transport", this::testingFakeTransport),
        Map.entry("testing-mock-server", this::testingMockServer),
        Map.entry("testing-webhook-handler", this::testingWebhookHandler),
        // configuration.md
        Map.entry("configuration", this::configuration),
        Map.entry("configuration-per-call", this::configurationPerCall),
        Map.entry("configuration-http-client", this::configurationHttpClient),
        Map.entry("logging", this::logging),
        // versioning.md
        Map.entry("versioning-open-enums", this::versioningOpenEnums),
        // async.md
        Map.entry("async-retrieve", this::asyncRetrieve),
        Map.entry("async-fan-out", this::asyncFanOut),
        Map.entry("async-errors", this::asyncErrors),
        Map.entry("async-timeout", this::asyncTimeout),
        Map.entry("async-pages", this::asyncPages),
        Map.entry("async-operation", this::asyncOperation));
  }

  private FakeTransport signedIn() {
    return api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  // getting-started.md

  private void gettingStartedFirstCall() throws Exception {
    signedIn()
        .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")))
        .on("GET " + ORG + "/compliance", Reply.json(COMPLIANCE));

    run("getting-started-first-call");

    assertEquals(List.of("3 of 4 compliant"), printed());
    assertEquals(1, api.requestsTo(INTROSPECTION_ROUTE).size());
  }

  private void gettingStartedInvite() throws Exception {
    String route = "POST " + ORG + "/learners";
    signedIn().on(route, Reply.json(201, LEARNER_JSON));

    run("getting-started-invite");

    assertEquals(List.of(LEARNER_ID + " invited"), printed());
    assertEquals(
        JSON.readTree(
            """
            {"email": "ana@example.com", "name": "Ana Example", "courseId": "hipaa-2026"}
            """),
        body(api.requestsTo(route).get(0)));
  }

  private void gettingStartedUpsert() throws Exception {
    signedIn().on("PUT " + ORG + "/learners/by-external-id/hr-123", Reply.json(LEARNER_JSON));

    run("getting-started-upsert");

    assertEquals(List.of(LEARNER_ID + " false"), printed());
  }

  private void gettingStartedErrors() throws Exception {
    String route = "POST " + ORG + "/learners";
    signedIn()
        .on(
            route,
            Reply.problem(
                409,
                """
                {"title": "Conflict", "status": 409, "code": "seat_unavailable",
                 "detail": "No seat is available."}
                """));

    run("getting-started-errors");

    assertEquals(List.of("No Seat is free for this Learner"), printed());
    assertEquals(1, api.requestsTo(route).size(), "seat_unavailable is not retried");
    assertEquals(
        "require_available", body(api.requestsTo(route).get(0)).path("seatPolicy").asText());
  }

  // authentication.md

  private void authClientCredentials() throws Exception {
    signedIn().on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")));

    run("auth-client-credentials");

    assertEquals(List.of("org_acme [learners:read, learners:write]"), printed());
  }

  private void authScopes() throws Exception {
    signedIn().on("GET " + ORG + "/learners", page(LEARNER_JSON));

    run("auth-scopes");

    assertEquals(List.of("1"), printed());
    assertEquals(
        "grant_type=client_credentials&scope=learners%3Aread+compliance%3Aread",
        new String(api.requestsTo(TOKEN_ROUTE).get(0).body(), UTF_8));
    assertEquals(List.of(), api.requestsTo(INTROSPECTION_ROUTE));
  }

  private void organization() throws Exception {
    signedIn()
        .on(
            "GET " + ORG + "/compliance",
            Reply.json(COMPLIANCE).withHeader("x-request-id", REQUEST_ID));

    run("organization");

    assertEquals(List.of("0.75 " + REQUEST_ID), printed());
    assertEquals(List.of(), api.requestsTo(INTROSPECTION_ROUTE));
  }

  private void authStaticToken() throws Exception {
    String route = "GET " + ORG + "/members/" + LEARNER_ID;
    api.on(
        route,
        Reply.json(
            """
            {"subjectId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG", "email": "ana@example.com",
             "name": "Ana Lima", "role": "admin", "isDisabled": false}
            """));

    run("auth-static-token");

    assertEquals(List.of("200"), printed());
    assertEquals(List.of(), api.requestsTo(TOKEN_ROUTE));
    assertEquals(
        "Bearer " + STATIC_TOKEN, api.requestsTo(route).get(0).headers().get("authorization"));
  }

  private void publicLookups() throws Exception {
    api.on("GET /v1/certificate-verifications/aB3dE9", Reply.json(VERIFICATION))
        .on("GET /v1/courses", page(COURSE));

    run("public-lookups");

    assertEquals(List.of("valid Ana Lima", "HIPAA Essentials 2026"), printed());
    assertEquals(List.of(), api.requestsTo(TOKEN_ROUTE));
    assertTrue(
        api.requests().stream()
            .noneMatch(request -> request.headers().containsKey("authorization")));
  }

  // errors-and-retries.md

  private void errors() throws Exception {
    signedIn()
        .on(
            LEARNER_ROUTE,
            Reply.problem(
                    403,
                    """
                    {"title": "Forbidden", "status": 403, "code": "insufficient_scope",
                     "detail": "This call needs the learners:read Scope."}
                    """)
                .withHeader("x-request-id", REQUEST_ID));

    run("errors");

    assertEquals(
        List.of("Missing a Scope: This call needs the learners:read Scope. (request req_7f3c2a)"),
        printed());
  }

  private void errorsCode() throws Exception {
    String route = "PATCH " + ORG + "/learners/" + LEARNER_ID;
    signedIn()
        .on(
            route,
            Reply.problem(
                409,
                """
                {"title": "Conflict", "status": 409, "code": "external_id_conflict",
                 "detail": "Another learner has this externalId."}
                """));

    run("errors-code");

    assertEquals(List.of("Another Learner already has that External ID"), printed());
    TransportRequest update = api.requestsTo(route).get(0);
    assertEquals("*", update.headers().get("if-match"));
    assertEquals(JSON.readTree("{\"externalId\": \"hr-123\"}"), body(update));
  }

  private void requestOptions() throws Exception {
    String route = "POST " + ORG + "/enrollments";
    signedIn()
        .on(
            route,
            Reply.problem(
                503, "{\"title\": \"Unavailable\", \"status\": 503, \"code\": \"unavailable\"}"))
        .on(route, Reply.json(201, enrollment(1)).withHeader("x-request-id", "req_e1"));

    run("request-options");

    List<TransportRequest> attempts = api.requestsTo(route);
    assertEquals(2, attempts.size());
    for (TransportRequest attempt : attempts) {
      assertEquals("hr-sync-2026-11-15-ana", attempt.headers().get("idempotency-key"));
      assertEquals("hr-sync-2026-11-15-ana", attempt.headers().get("x-request-id"));
      assertEquals(Duration.ofSeconds(10), attempt.timeout());
    }
    assertEquals(1, sleeper.sleeps().size());
    assertEquals(List.of(enrollmentId(1) + " req_e1"), printed());
  }

  // pagination-and-events.md

  private void pagination() throws Exception {
    signedIn()
        .on(
            "GET " + ORG + "/learners",
            request ->
                "cursor-2".equals(query(request).get("cursor"))
                    ? page(EXPIRED_LEARNER_JSON)
                    : pageWithNext("cursor-2", LEARNER_JSON))
        .on(
            "GET " + ORG + "/enrollments",
            request ->
                "cursor-e2".equals(query(request).get("cursor"))
                    ? page(enrollment(2), enrollment(3))
                    : pageWithNext("cursor-e2", enrollment(1)));

    run("pagination");

    assertEquals(List.of(LEARNER_ID, EXPIRED_LEARNER_ID, "[bo@example.com]", "1", "2"), printed());
    assertEquals(
        Map.of("limit", "100", "cursor", "cursor-2"),
        query(api.requestsTo("GET " + ORG + "/learners").get(1)));
  }

  private void paginationPages() throws Exception {
    signedIn()
        .on(
            "GET " + ORG + "/certificates",
            request ->
                "cursor-c2".equals(query(request).get("cursor"))
                    ? page(certificate(2)).withHeader("x-request-id", "req_c2")
                    : pageWithNext("cursor-c2", certificate(1))
                        .withHeader("x-request-id", "req_c1"));

    run("pagination-pages");

    assertEquals(List.of("1 req_c1", "1 req_c2"), printed());
    assertEquals(
        Map.of("limit", "100", "cursor", "cursor-c2"),
        query(api.requestsTo("GET " + ORG + "/certificates").get(1)));
  }

  private void eventsList() throws Exception {
    String seatsLow =
        """
        {"eventId": "evt_03", "eventType": "seats.low", "orgId": "org_acme",
         "occurredAt": "2026-11-15T12:05:00.000Z", "data": {"available": 3, "threshold": 5}}
        """;
    String unknown =
        """
        {"eventId": "evt_04", "eventType": "learner.hibernated", "orgId": "org_acme",
         "occurredAt": "2026-11-15T12:06:00.000Z", "data": {"learnerId": "sub_01"}}
        """;
    signedIn().on("GET " + ORG + "/events", page(event("evt_02"), seatsLow, unknown));

    run("events-list");

    assertEquals(
        List.of("certified ana@example.com", "Seats left: 3", "other learner.hibernated"),
        printed());
    assertEquals("evt_01", query(api.requestsTo("GET " + ORG + "/events").get(0)).get("after"));
  }

  private void eventsTail() throws Exception {
    storage.eventId = "evt_01";
    signedIn()
        .on("GET " + ORG + "/events", page(event("evt_02"), event("evt_03")))
        .on(
            "GET " + ORG + "/events",
            request -> {
              // How a service ends the tail: shutdownNow() interrupts the thread running it.
              Thread.currentThread().interrupt();
              return page();
            });

    try {
      run("events-tail");
    } finally {
      assertTrue(Thread.interrupted(), "the tail ended on the interrupt and kept the flag");
    }

    assertEquals(
        List.of("evt_01", "evt_03"),
        api.requestsTo("GET " + ORG + "/events").stream()
            .map(poll -> query(poll).get("after"))
            .toList());
    assertEquals(List.of("evt_02", "evt_03"), storage.savedEventIds);
    assertEquals(List.of("learner.certified evt_02", "learner.certified evt_03"), printed());
  }

  // webhooks.md

  private void webhookSubscriptionCreate() throws Exception {
    String route = "POST " + ORG + "/webhook-subscriptions";
    signedIn()
        .on(
            route,
            Reply.json(
                201,
                """
                {"subscriptionId": "whs_01JD8Q", "endpointUrl": "https://hr.example.com/webhooks/tmh",
                 "eventTypes": ["learner.certified", "learner.expiring", "invite.bounced"],
                 "enabled": true, "description": "HR sync", "seatsLowThreshold": 5,
                 "secretKeyId": "key_01", "createdAt": "2026-11-15T12:00:00.000Z", "rev": 1,
                 "secret": "whsec_k3Jq9Zx8LmN2pR4t"}
                """));

    run("webhook-subscription-create");

    assertEquals(List.of("whs_01JD8Q key_01"), printed());
    assertEquals("whsec_k3Jq9Zx8LmN2pR4t", storage.signingSecret);
    assertEquals(
        JSON.readTree(
            """
            {"endpointUrl": "https://hr.example.com/webhooks/tmh",
             "eventTypes": ["learner.certified", "learner.expiring", "invite.bounced"],
             "description": "HR sync"}
            """),
        body(api.requestsTo(route).get(0)));
  }

  private void webhookSpring() throws Exception {
    Object controller = construct("webhook-spring");
    Method receive =
        controller
            .getClass()
            .getMethod(
                "receive",
                byte[].class,
                HttpHeaders.class,
                jakarta.servlet.http.HttpServletRequest.class);

    webhookOutcomes(
        (target, payload, signed) -> {
          HttpHeaders headers = new HttpHeaders();
          signed.forEach(headers::add);
          ResponseEntity<?> response =
              (ResponseEntity<?>)
                  receive.invoke(
                      controller, payload, headers, servletRequest(target, payload, signed));
          return response.getStatusCode().value() + " " + response.getBody();
        });
  }

  private void webhookServlet() throws Exception {
    HttpServlet servlet = (HttpServlet) construct("webhook-servlet");

    webhookOutcomes(
        (target, payload, signed) -> {
          StringWriter written = new StringWriter();
          int[] status = {200};
          HttpServletResponse response =
              proxy(
                  HttpServletResponse.class,
                  (method, args) ->
                      switch (method) {
                        case "setStatus" -> {
                          status[0] = (Integer) args[0];
                          yield null;
                        }
                        case "setContentType" -> null;
                        case "getWriter" -> new PrintWriter(written, true);
                        default -> throw new UnsupportedOperationException(method);
                      });
          servlet.service(servletRequest(target, payload, signed), response);
          return status[0] + " " + written;
        });
  }

  /** The handler behind a real JDK server on a free loopback port, posted to over HTTP. */
  private void webhookJdkServer() throws Exception {
    HttpServer server =
        HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/webhooks/tmh", (HttpHandler) construct("webhook-jdk-server"));
    server.start();
    try {
      HttpClient http = HttpClient.newHttpClient();
      String origin = "http://127.0.0.1:" + server.getAddress().getPort();
      webhookOutcomes(
          (target, payload, signed) -> {
            HttpRequest.Builder post =
                HttpRequest.newBuilder(URI.create(origin + target))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload));
            signed.forEach(post::header);
            HttpResponse<String> response =
                http.send(post.build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() + " " + response.body();
          });
    } finally {
      server.stop(0);
    }
  }

  private void webhookDedupe() throws Exception {
    Object sample = construct("webhook-dedupe");
    byte[] delivery = event("evt_01JD8M").getBytes(UTF_8);
    byte[] test = "{\"subscriptionId\": \"whs_01JD8Q\", \"test\": true}".getBytes(UTF_8);
    for (byte[] payload : List.of(delivery, test, delivery)) {
      Map<String, String> signed = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      signed.putAll(Webhook.generateTestHeaders(payload, WEBHOOK_SECRET, "/webhooks/tmh"));
      Function<String, String> headers = signed::get;
      call(sample, "handleDelivery", payload, headers, "/webhooks/tmh");
    }

    assertEquals(List.of("handling learner.certified evt_01JD8M"), printed());
    assertEquals(List.of("evt_01JD8M"), storage.handled);
  }

  private void webhookDeliveryOperation() throws Exception {
    signedIn()
        .on(
            "POST " + ORG + "/webhook-subscriptions/whs_01JD8D:test",
            Reply.json(202, operation(false, null)))
        .on(
            "GET " + ORG + "/operations/" + OPERATION_ID,
            Reply.json(operation(true, "{\"_tag\": \"sent\", \"responseStatus\": 200}")));

    run("webhook-delivery-operation");

    assertEquals(List.of("endpoint answered 200"), printed());
    assertEquals(List.of(Duration.ofSeconds(1)), sleeper.sleeps());
  }

  private void webhookTestHeaders() throws Exception {
    run("webhook-test-headers");

    assertEquals(List.of("certified " + LEARNER_ID), printed());
  }

  private void webhookVerifyOptions() throws Exception {
    run("webhook-verify-options");

    assertEquals(List.of("verified true"), printed());
  }

  // operations-and-files.md

  // base64 of EXPORTED_CSV, computed outside the SDK.
  private static final String EXPORT_ARTIFACT =
      """
      {"format": "csv", "contentType": "text/csv; charset=utf-8",
       "filename": "learners-org_acme.csv", "rowCount": 1,
       "dataBase64": "bGVhcm5lcklkLGVtYWlsDQpzdWJfMDEsYW5hQGV4YW1wbGUuY29tDQo="}
      """;
  private static final String EXPORTED_CSV = "learnerId,email\r\nsub_01,ana@example.com\r\n";

  private void operations() throws Exception {
    signedIn()
        .on("POST " + ORG + "/learners:export", Reply.json(202, operation(true, EXPORT_ARTIFACT)));

    run("operations");

    assertEquals(List.of("learners-org_acme.csv 1 " + EXPORTED_CSV.length()), printed());
    assertEquals(List.of(), sleeper.sleeps());
  }

  private void operationsResume() throws Exception {
    signedIn()
        .on(
            "POST " + ORG + "/certificates:export",
            Reply.json(202, operation(true, EXPORT_ARTIFACT)))
        .on(
            "GET " + ORG + "/operations/" + OPERATION_ID,
            Reply.json(operation(true, EXPORT_ARTIFACT)));

    run("operations-resume");

    assertEquals("organizations/org_acme/operations/" + OPERATION_ID, storage.operationName);
    assertEquals(List.of("1"), printed());
    assertEquals(1, api.requestsTo("GET " + ORG + "/operations/" + OPERATION_ID).size());
  }

  private void csvImport() throws Exception {
    byte[] csv = "email,name\r\nana@example.com,Ana Example\r\n".getBytes(UTF_8);
    Path roster = Files.write(work.resolve("roster.csv"), csv);
    // A preview is an import with validateOnly set, so both go to the one route.
    String importRoute = "POST " + ORG + "/enrollments:import";
    signedIn()
        .on(
            importRoute,
            Reply.json(
                """
                {"totalRows": 1, "validRows": 1, "seatsRequired": 1, "seatsAvailable": 9,
                 "rows": [{"rowNumber": 2, "email": "ana@example.com", "courseId": "hipaa-2026",
                           "status": "valid"}]}
                """))
        .on(importRoute, Reply.json(202, operation(true, null)));

    call(construct("csv-import"), "importRoster", roster);

    assertEquals(List.of("1 of 1 rows are valid"), printed());
    List<TransportRequest> uploads = api.requestsTo(importRoute);
    assertEquals(2, uploads.size());
    for (TransportRequest upload : uploads) {
      assertTrue(contains(upload.body(), csv), "the CSV went out in the multipart body");
      assertTrue(contains(upload.body(), "hipaa-2026".getBytes(UTF_8)), "with the options");
    }
  }

  private void pdfDownload() throws Exception {
    byte[] pdf = "%PDF-1.7 certificate".getBytes(UTF_8);
    signedIn()
        .on(
            "GET " + ORG + "/certificates/cert_01JD8F/content",
            new Reply(
                200,
                Map.of(
                    "content-type",
                    "application/pdf",
                    "content-disposition",
                    "attachment; filename=\"ana.pdf\""),
                pdf));

    call(construct("pdf-download"), "saveCertificate", work);

    assertArrayEquals(pdf, Files.readAllBytes(work.resolve("ana.pdf")));
    assertArrayEquals(pdf, Files.readAllBytes(work.resolve("certificate-copy.pdf")));
  }

  private void bulkChanges() throws Exception {
    String route = "POST " + ORG + "/enrollments:batch";
    signedIn()
        .on(
            route,
            request ->
                body(request).path("select").has("filter")
                    ? Reply.json(202, operation(false, null))
                    : Reply.json(
                        """
                        {"operation": "remind",
                         "results": [{"enrollmentId": "enr_01", "outcome": "succeeded"},
                                     {"enrollmentId": "enr_02", "outcome": "skipped"}]}
                        """))
        .on(
            "GET " + ORG + "/operations/" + OPERATION_ID,
            Reply.json(operation(true, "{\"succeeded\": 4}")));

    run("bulk-changes");

    assertEquals(List.of("enr_01 succeeded", "enr_02 skipped"), printed());
    List<TransportRequest> batches = api.requestsTo(route);
    assertEquals(
        JSON.readTree(
            """
            {"operation": "remind", "select": {"enrollmentIds": ["enr_01", "enr_02"]}}
            """),
        body(batches.get(0)));
    assertEquals(
        JSON.readTree(
            """
            {"operation": "recertify",
             "select": {"filter": {"status": "EXPIRED", "courseId": "hipaa-2026"}}}
            """),
        body(batches.get(1)));
    assertEquals(1, api.requestsTo("GET " + ORG + "/operations/" + OPERATION_ID).size());
  }

  // conditional-requests.md

  private void conditionalWrite() throws Exception {
    String patchRoute = "PATCH " + ORG + "/learners/" + LEARNER_ID;
    signedIn()
        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON).withHeader("etag", "\"7\""))
        .on(
            patchRoute,
            Reply.problem(
                412,
                "{\"title\": \"Precondition Failed\", \"status\": 412, \"code\": \"precondition_failed\"}"));

    run("conditional-write");

    TransportRequest update = api.requestsTo(patchRoute).get(0);
    assertEquals("\"7\"", update.headers().get("if-match"));
    assertEquals("application/merge-patch+json", update.headers().get("content-type"));
    assertEquals(
        JSON.readTree("{\"name\": \"Ana Q. Example\", \"externalId\": null}"), body(update));
    assertEquals(List.of("Someone else changed this Learner; read it again"), printed());
  }

  private void conditionalUpdateSubscription() throws Exception {
    String route = ORG + "/webhook-subscriptions/whs_01JD8Q";
    String subscription =
        """
        {"subscriptionId": "whs_01JD8Q", "endpointUrl": "https://hr.example.com/webhooks/tmh",
         "eventTypes": ["learner.certified"], "enabled": %s, "description": %s,
         "seatsLowThreshold": 5, "secretKeyId": "key_01",
         "createdAt": "2026-11-15T12:00:00.000Z", "rev": %d}
        """;
    signedIn()
        .on(
            "GET " + route,
            Reply.json(subscription.formatted(true, "\"HR sync\"", 1)).withHeader("etag", "\"s1\""))
        .on(
            "PATCH " + route,
            Reply.json(subscription.formatted(false, "null", 2)).withHeader("etag", "\"s2\""));

    run("conditional-update-subscription");

    assertEquals(List.of("false \"s2\""), printed());
    TransportRequest update = api.requestsTo("PATCH " + route).get(0);
    assertEquals("\"s1\"", update.headers().get("if-match"));
    assertEquals(JSON.readTree("{\"enabled\": false, \"description\": null}"), body(update));
  }

  private void notModified() throws Exception {
    String route = "GET /v1/courses/hipaa-2026";
    api.on(route, Reply.json(COURSE).withHeader("etag", "\"c1\"")).on(route, Reply.empty(304));

    run("not-modified");

    assertEquals("\"c1\"", api.requestsTo(route).get(1).headers().get("if-none-match"));
    assertEquals(List.of("still current", "HIPAA Essentials 2026"), printed());
  }

  // testing.md

  private void testingSandbox() throws Exception {
    signedIn().on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")));

    run("testing-sandbox");

    assertEquals(List.of("test"), printed());
  }

  private void testingFakeTransport() throws Exception {
    call(construct("testing-fake-transport"), "readsTheComplianceRate");

    assertEquals(List.of(), api.requests(), "the sample's own fake answered");
  }

  private void testingMockServer() throws Exception {
    api.on("GET /organizations/org_acme/learners", page(LEARNER_JSON));

    run("testing-mock-server");

    assertEquals(List.of("1"), printed());
    TransportRequest list = api.requests().get(0);
    assertEquals(
        URI.create("http://127.0.0.1:4010/organizations/org_acme/learners?limit=5"), list.uri());
    assertEquals("Bearer mock-token", list.headers().get("authorization"));
  }

  private void testingWebhookHandler() throws Exception {
    call(construct("testing-webhook-handler"), "acceptsASignedDeliveryAndRefusesTamperedOnes");
  }

  // configuration.md

  private void configuration() throws Exception {
    run("configuration");

    assertEquals(List.of("SANDBOX https://api-teachmehipaa-dev.web.app/v1 3"), printed());
    assertEquals(List.of(), api.requests());
  }

  private void configurationPerCall() throws Exception {
    signedIn().on("GET " + ORG + "/learners", page(LEARNER_JSON));

    run("configuration-per-call");

    assertEquals(List.of("1"), printed());
    TransportRequest list = api.requestsTo("GET " + ORG + "/learners").get(0);
    assertEquals(Duration.ofSeconds(5), list.timeout());
    assertEquals(Map.of("limit", "100"), query(list));
  }

  private void configurationHttpClient() throws Exception {
    run("configuration-http-client");

    assertEquals(List.of("https://api-teachmehipaa-dev.web.app/v1"), printed());
    assertEquals(List.of(), api.requests());
  }

  private void logging() throws Exception {
    signedIn().on("GET " + ORG + "/learners", page().withHeader("x-request-id", "req_l"));

    run("logging");

    assertEquals(
        List.of(
            "POST /v1/auth/token 200 in N ms (request id null)",
            "GET /v1/organizations/org_acme/learners 200 in N ms (request id req_l)"),
        printed().stream().map(line -> line.replaceAll("\\d+ ms", "N ms")).toList());
  }

  // versioning.md

  private void versioningOpenEnums() throws Exception {
    signedIn()
        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON.replace("\"in_progress\"", "\"on_leave\"")));

    run("versioning-open-enums");

    assertEquals(List.of("needs attention: on_leave (new)"), printed());
  }

  /** dev#/components/schemas/Certificate. */
  private static String certificate(int n) {
    return """
        {"certificateId": "cert_0%d", "subjectId": "%s", "holderStatus": "active",
         "courseId": "hipaa-2026", "courseVersion": "2026.1",
         "issuedAt": "2026-11-03T15:45:00.000Z", "expiresAt": "2027-11-03T15:45:00.000Z",
         "status": "valid", "revokedAt": null}
        """
        .formatted(n, LEARNER_ID);
  }

  // async.md

  private void asyncRetrieve() throws Exception {
    signedIn().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    run("async-retrieve");

    assertEquals(List.of("Ana Lima in_progress"), printed());
  }

  private void asyncFanOut() throws Exception {
    signedIn()
        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON))
        .on(ORG_LEARNER + EXPIRED_LEARNER_ID, Reply.json(EXPIRED_LEARNER_JSON));

    run("async-fan-out");

    assertEquals(List.of("ana@example.com", "bo@example.com"), printed());
    assertEquals(1, api.requestsTo(TOKEN_ROUTE).size());
  }

  private void asyncErrors() throws Exception {
    signedIn()
        .on(
            ORG_LEARNER + EXPIRED_LEARNER_ID,
            Reply.problem(
                404,
                """
                {"title": "Not found", "status": 404, "code": "not_found"}
                """));

    run("async-errors");

    assertEquals(List.of("no such Learner"), printed());
  }

  private void asyncTimeout() throws Exception {
    signedIn().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    run("async-timeout");

    assertEquals(List.of("ana@example.com"), printed());
  }

  private void asyncPages() throws Exception {
    signedIn()
        .on(
            "GET " + ORG + "/learners",
            request ->
                "cursor-2".equals(query(request).get("cursor"))
                    ? page(EXPIRED_LEARNER_JSON)
                    : pageWithNext("cursor-2", LEARNER_JSON, LEARNER_JSON));

    Object sample = construct("async-pages");
    @SuppressWarnings("unchecked")
    CompletableFuture<Integer> count = (CompletableFuture<Integer>) call(sample, "countLearners");

    assertEquals(3, count.get(5, TimeUnit.SECONDS));
    assertEquals(2, api.requestsTo("GET " + ORG + "/learners").size());
  }

  private void asyncOperation() throws Exception {
    signedIn()
        .on(
            "POST " + ORG + "/certificates:export",
            Reply.json(202, operation(true, EXPORT_ARTIFACT)));

    run("async-operation");

    assertEquals(List.of("learners-org_acme.csv 1"), printed());
    assertEquals(List.of(), asyncSleeper.sleeps());
  }
}
