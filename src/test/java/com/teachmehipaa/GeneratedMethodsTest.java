package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.ETAG;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.EmptyResponse;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.model.AuditEvent;
import com.teachmehipaa.model.AuditEventActorUser;
import com.teachmehipaa.model.AuditEventLearnerUpdated;
import com.teachmehipaa.model.CancelEnrollmentResponse;
import com.teachmehipaa.model.CertificateVerification;
import com.teachmehipaa.model.CertificateVerificationStatus;
import com.teachmehipaa.model.Course;
import com.teachmehipaa.model.Enrollment;
import com.teachmehipaa.model.EnrollmentActionAccepted;
import com.teachmehipaa.model.EnrollmentActionAcceptedAction;
import com.teachmehipaa.model.EnrollmentCancelRefunded;
import com.teachmehipaa.model.ImportPreview;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerCreatedDelivery;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.model.LearnerUpsertRequestDelivery;
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.model.PartnerOrganization;
import com.teachmehipaa.model.UnknownAuditEvent;
import com.teachmehipaa.model.UnknownAuditEventActor;
import com.teachmehipaa.model.WebhookSubscription;
import com.teachmehipaa.param.AuditEventListParams;
import com.teachmehipaa.param.EnrollmentCancelParams;
import com.teachmehipaa.param.EnrollmentPreviewImportParams;
import com.teachmehipaa.param.ImportOptions;
import com.teachmehipaa.param.LearnerUpsertByExternalIdParams;
import com.teachmehipaa.param.PartnerOrganizationListParams;
import com.teachmehipaa.param.WebhookSubscriptionUpdateParams;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class GeneratedMethodsTest {
  private static final Pattern UUID_V4 =
      Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String ORG = "/v1/organizations/org_acme";
  private static final String ENROLLMENT_ID = "enr_01JD8Z4N3QX5V7W9Y2B4C6D8EK";
  private static final String SUBSCRIPTION_ID = "whs_01JD8Z4N3QX5V7W9Y2B4C6D8EJ";

  private static final String ENROLLMENT_JSON =
      """
      {"enrollmentId": "enr_01JD8Z4N3QX5V7W9Y2B4C6D8EK", "subjectId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG",
       "courseId": "hipaa-essentials", "orgId": "org_acme", "status": "CANCELLED",
       "effectiveStatus": "CANCELLED", "archived": false, "cycle": 1, "rev": 4,
       "invitedAt": "2026-11-01T09:00:00Z", "startedAt": null, "completedAt": null,
       "certifiedAt": null, "expiresAt": null, "certificateId": null%s}
      """;

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  private static JsonNode json(String text) throws IOException {
    return JSON.readTree(text);
  }

  private static TransportRequest only(FakeTransport api, String route) {
    assertEquals(1, api.requestsTo(route).size(), route);
    return api.requestsTo(route).get(0);
  }

  @Test
  void aCustomMethodPostsToItsVerbWithAGeneratedIdempotencyKeyAndNoBody() {
    String route = "POST " + ORG + "/enrollments/" + ENROLLMENT_ID + ":remind";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    202,
                    """
                    {"enrollmentId": "enr_01JD8Z4N3QX5V7W9Y2B4C6D8EK", "action": "remind"}
                    """));

    EnrollmentActionAccepted accepted = clientOn(api).enrollments().remind(ENROLLMENT_ID);

    assertEquals(EnrollmentActionAcceptedAction.REMIND, accepted.getAction());
    assertEquals(202, accepted.getLastResponse().statusCode());
    TransportRequest request = only(api, route);
    assertTrue(UUID_V4.matcher(request.headers().get("idempotency-key")).matches());
    assertNull(request.body());
  }

  @Test
  void aPartnerMethodTakesThePartnerIdAndNeverLooksUpTheOrganization() {
    String route = "GET /v1/partners/ptr_01JD8Z4N3QX5V7W9Y2B4C6D8EM/organizations";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    """
                    {"items": [{"orgId": "org_child", "name": "Child Clinic",
                                "createdAt": "2026-10-01T12:00:00Z", "grantActive": true}],
                     "nextCursor": "c_2"}
                    """));
    TeachMeHipaaClient client = clientFor(api).build();

    Page<PartnerOrganization> page =
        client
            .partners()
            .organizations()
            .list(
                "ptr_01JD8Z4N3QX5V7W9Y2B4C6D8EM",
                PartnerOrganizationListParams.builder().limit(10).cursor("c_1").build());

    assertEquals("org_child", page.items().get(0).getOrgId());
    assertTrue(page.items().get(0).isGrantActive());
    assertEquals(
        OffsetDateTime.of(2026, 10, 1, 12, 0, 0, 0, ZoneOffset.UTC),
        page.items().get(0).getCreatedAt());
    assertEquals("c_2", page.nextCursor());
    assertEquals(200, page.getLastResponse().statusCode());
    assertEquals("limit=10&cursor=c_1", only(api, route).uri().getRawQuery());
    assertTrue(api.requestsTo(INTROSPECTION_ROUTE).isEmpty());
  }

  @Test
  void anUpdateSendsAMergePatchWithIfMatchAndNullForAClearedField() throws IOException {
    String route = "PATCH " + ORG + "/webhook-subscriptions/" + SUBSCRIPTION_ID;
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    """
                    {"subscriptionId": "whs_01JD8Z4N3QX5V7W9Y2B4C6D8EJ",
                     "endpointUrl": "https://hooks.example.com/tmh", "eventTypes": ["learner.certified"],
                     "enabled": false, "description": null, "seatsLowThreshold": 5,
                     "secretKeyId": "key_1", "createdAt": "2026-11-01T09:00:00Z", "rev": 3}
                    """));

    WebhookSubscription subscription =
        clientOn(api)
            .webhookSubscriptions()
            .update(
                SUBSCRIPTION_ID,
                WebhookSubscriptionUpdateParams.builder()
                    .ifMatch(ETAG)
                    .enabled(false)
                    .clearDescription()
                    .build());

    assertFalse(subscription.isEnabled());
    TransportRequest request = only(api, route);
    assertEquals("application/merge-patch+json", request.headers().get("content-type"));
    assertEquals(ETAG, request.headers().get("if-match"));
    assertEquals(
        json("{\"enabled\": false, \"description\": null}"), JSON.readTree(request.body()));
  }

  @Test
  void aDeleteAnsweredWith204ReturnsItsResponseMetadata() {
    String route = "DELETE " + ORG + "/billing/payment-method";
    FakeTransport api = api().on(route, Reply.empty(204).withHeader("x-request-id", "req_9"));

    EmptyResponse response = clientOn(api).paymentMethod().delete();

    assertEquals(204, response.getLastResponse().statusCode());
    assertEquals("req_9", response.getLastResponse().requestId());
  }

  @Test
  void anUpsertThatCreatesTheLearnerSaysSoAndCarriesTheJoinUrl() throws IOException {
    String route = "PUT " + ORG + "/learners/by-external-id/hr-123";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    201,
                    LEARNER_JSON.replaceFirst(
                        "\\{",
                        "{\"delivery\": \"none\", \"joinUrl\": \"https://teachmehipaa.com/j/abc\",")));

    LearnerWriteResult result =
        clientOn(api)
            .learners()
            .upsertByExternalId(
                "hr-123",
                LearnerUpsertByExternalIdParams.builder()
                    .email("ana@example.com")
                    .delivery(LearnerUpsertRequestDelivery.NONE)
                    .build());

    assertTrue(result.isCreated());
    assertEquals(LearnerCreatedDelivery.NONE, result.getDelivery());
    assertEquals("https://teachmehipaa.com/j/abc", result.getJoinUrl());
    assertEquals(LEARNER_ID, result.getLearnerId());
    TransportRequest request = only(api, route);
    assertTrue(UUID_V4.matcher(request.headers().get("idempotency-key")).matches());
    assertEquals(
        json("{\"email\": \"ana@example.com\", \"delivery\": \"none\"}"),
        JSON.readTree(request.body()));
  }

  @Test
  void anUpsertOfAnExistingLearnerIsNotCreated() {
    String route = "PUT " + ORG + "/learners/by-external-id/hr-123";
    FakeTransport api = api().on(route, Reply.json(200, LEARNER_JSON));

    LearnerWriteResult result =
        clientOn(api)
            .learners()
            .upsertByExternalId(
                "hr-123",
                LearnerUpsertByExternalIdParams.builder().email("ana@example.com").build());

    assertFalse(result.isCreated());
    assertNull(result.getJoinUrl());
  }

  @Test
  void anUnknownEnumValueAndAnUnknownFieldAreKept() {
    FakeTransport api =
        api()
            .on(
                LEARNER_ROUTE,
                Reply.json(
                    LEARNER_JSON
                        .replace("\"in_progress\"", "\"on_leave\"")
                        .replaceFirst("\\{", "{\"department\": \"Billing\",")));

    Learner learner = clientOn(api).learners().retrieve(LEARNER_ID);

    assertEquals("on_leave", learner.getStatus().value());
    assertFalse(learner.getStatus().isKnown());
    assertEquals(LearnerStatus.of("on_leave"), learner.getStatus());
    assertEquals(Map.of("department", "Billing"), learner.getExtraFields());
  }

  @Test
  void anUnknownUnionVariantReadsAsItsFallbackWithEveryField() {
    String route = "GET " + ORG + "/audit-events";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    """
                    {"items": [
                      {"eventId": "aud_1", "at": "2026-11-02T10:00:00Z", "type": "learner.updated",
                       "actorSubjectId": "sub_admin",
                       "actor": {"type": "user", "subjectId": "sub_admin"},
                       "subjectId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG", "fields": ["name"]},
                      {"eventId": "aud_2", "at": "2026-11-02T11:00:00Z", "type": "learner.archived",
                       "actor": {"type": "robot", "robotId": "rbt_7"}, "reason": "inactive"}
                    ],
                     "nextCursor": null}
                    """));

    Page<AuditEvent> page =
        clientOn(api)
            .auditEvents()
            .list(
                AuditEventListParams.builder()
                    .since(OffsetDateTime.of(2026, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                    .build());

    AuditEventLearnerUpdated known =
        assertInstanceOf(AuditEventLearnerUpdated.class, page.items().get(0));
    assertEquals(
        "sub_admin", assertInstanceOf(AuditEventActorUser.class, known.getActor()).getSubjectId());
    UnknownAuditEvent unknown = assertInstanceOf(UnknownAuditEvent.class, page.items().get(1));
    assertEquals("learner.archived", unknown.getType());
    assertEquals("inactive", unknown.getExtraFields().get("reason"));
    assertEquals(
        Map.of("type", "robot", "robotId", "rbt_7"), unknown.getExtraFields().get("actor"));
    assertNull(page.nextCursor());
    assertEquals("since=2026-11-01T00%3A00%3A00Z", only(api, route).uri().getRawQuery());
  }

  @Test
  void anUnknownNestedVariantReadsAsItsFallback() {
    String route = "GET " + ORG + "/audit-events";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    """
                    {"items": [{"eventId": "aud_1", "at": "2026-11-02T10:00:00Z",
                       "type": "organization.closed", "actorSubjectId": "sub_admin",
                       "actor": {"type": "robot", "robotId": "rbt_7"}}],
                     "nextCursor": null}
                    """));

    AuditEvent event = clientOn(api).auditEvents().list().items().get(0);

    UnknownAuditEventActor actor =
        assertInstanceOf(
            UnknownAuditEventActor.class,
            assertInstanceOf(com.teachmehipaa.model.AuditEventOrganizationClosed.class, event)
                .getActor());
    assertEquals("robot", actor.getType());
    assertEquals(Map.of("robotId", "rbt_7"), actor.getExtraFields());
  }

  @Test
  void aCancelWithARefundReadsAsTheRefundedVariantAndOneWithoutAsAnEnrollment() {
    String route = "POST " + ORG + "/enrollments/" + ENROLLMENT_ID + ":cancel";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    ENROLLMENT_JSON.formatted(
                        ", \"refund\": {\"refundId\": \"re_1\", \"amountCents\": 2900,"
                            + " \"currency\": \"usd\"}")))
            .on(route, Reply.json(ENROLLMENT_JSON.formatted("")));
    TeachMeHipaaClient client = clientOn(api);

    CancelEnrollmentResponse refunded =
        client
            .enrollments()
            .cancel(ENROLLMENT_ID, EnrollmentCancelParams.builder().refund(true).build());
    CancelEnrollmentResponse cancelled = client.enrollments().cancel(ENROLLMENT_ID);

    assertEquals(
        2900,
        assertInstanceOf(EnrollmentCancelRefunded.class, refunded).getRefund().getAmountCents());
    assertEquals(200, refunded.getLastResponse().statusCode());
    assertEquals(ENROLLMENT_ID, assertInstanceOf(Enrollment.class, cancelled).getEnrollmentId());
  }

  @Test
  void aPublicEndpointSendsNoAccessToken() {
    String route = "GET /v1/certificate-verifications/Xy7Kq2";
    // No token route is scripted: minting one would fail the test.
    FakeTransport api =
        new FakeTransport()
            .on(
                route,
                Reply.json(
                    """
                    {"status": "valid", "learnerName": "Ana Lima", "courseTitle": "HIPAA Essentials",
                     "issuedAt": "2026-11-03T15:45:00Z", "expiresAt": "2027-11-03T15:45:00Z",
                     "issuer": "TeachMeHIPAA"}
                    """))
            .on(
                "GET /v1/courses",
                Reply.json(
                    """
                    {"items": [{"courseId": "hipaa-essentials", "title": "HIPAA Essentials",
                                "courseVersion": "2026.1", "passingThreshold": 0.8}],
                     "nextCursor": null}
                    """));
    TeachMeHipaaClient client = clientFor(api).build();

    CertificateVerification verification = client.certificateVerifications().retrieve("Xy7Kq2");
    Page<Course> courses = client.courses().list();

    assertEquals(CertificateVerificationStatus.VALID, verification.getStatus());
    assertEquals(0.8, courses.items().get(0).getPassingThreshold());
    assertTrue(api.requests().stream().noneMatch(r -> r.headers().containsKey("authorization")));
    assertEquals(2, api.requests().size());
  }

  @Test
  void aPreviewImportSendsTheCsvAndItsOptionsAsMultipartWithValidateOnly() {
    String route = "POST " + ORG + "/enrollments:import";
    FakeTransport api =
        api()
            .on(
                route,
                Reply.json(
                    """
                    {"totalRows": 1, "validRows": 1, "seatsRequired": 1, "seatsAvailable": 4,
                     "rows": [{"rowNumber": 1, "email": "ana@example.com",
                               "courseId": "hipaa-essentials", "status": "valid"}]}
                    """));
    byte[] csv = "email\r\nana@example.com\r\n".getBytes(StandardCharsets.UTF_8);

    ImportPreview preview =
        clientOn(api)
            .enrollments()
            .previewImport(
                EnrollmentPreviewImportParams.builder()
                    .file(FileUpload.of(csv, "roster.csv"))
                    .options(ImportOptions.builder().courseId("hipaa-essentials").build())
                    .build());

    assertEquals(4, preview.getSeatsAvailable());
    TransportRequest request = only(api, route);
    String contentType = request.headers().get("content-type");
    assertTrue(contentType.startsWith("multipart/form-data; boundary="), contentType);
    String boundary = contentType.substring(contentType.indexOf('=') + 1);
    String body = new String(request.body(), StandardCharsets.UTF_8);
    assertEquals(
        "--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"roster.csv\""
            + "\r\nContent-Type: text/csv\r\n\r\nemail\r\nana@example.com\r\n\r\n--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"options\""
            + "\r\nContent-Type: application/json\r\n\r\n"
            + "{\"courseId\":\"hipaa-essentials\",\"validateOnly\":true}\r\n--"
            + boundary
            + "--\r\n",
        body);
    assertTrue(UUID_V4.matcher(request.headers().get("idempotency-key")).matches());
  }
}
