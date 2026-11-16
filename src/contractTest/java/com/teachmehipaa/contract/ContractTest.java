package com.teachmehipaa.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.BinaryStreamResponse;
import com.teachmehipaa.core.EmptyResponse;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.StaticTokenProvider;
import com.teachmehipaa.exception.InvalidRequestException;
import com.teachmehipaa.model.CancelEnrollmentResponse;
import com.teachmehipaa.model.Certificate;
import com.teachmehipaa.model.CertificateVerification;
import com.teachmehipaa.model.Course;
import com.teachmehipaa.model.Event;
import com.teachmehipaa.model.EventType;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerCreateRequestDelivery;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.model.TokenIntrospection;
import com.teachmehipaa.model.WebhookSubscriptionCreated;
import com.teachmehipaa.param.CourseListParams;
import com.teachmehipaa.param.EnrollmentCancelParams;
import com.teachmehipaa.param.EventListParams;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerListParams;
import com.teachmehipaa.param.LearnerRetrieveParams;
import com.teachmehipaa.param.LearnerUpdateParams;
import com.teachmehipaa.param.WebhookSubscriptionCreateParams;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The public client against a Prism mock of the vendored developer spec: one representative call
 * per resource family. A request that breaks the spec fails the call (Prism answers 4xx) or the
 * test (a violation in {@code sl-violations}).
 */
@ExtendWith(PrismExtension.class)
class ContractTest {
  // Values that satisfy the spec's path-parameter patterns.
  private static final String ORG_ID = "org_acme";
  private static final String LEARNER_ID = "sub_01JD80AB2C3D4E5F6G7H8J9K0M";
  private static final String ENROLLMENT_ID = "sub_01JD80AB2C3D4E5F6G7H8J9K0M__hipaa-2026";
  private static final String CERTIFICATE_ID = "cert_01JD8F";
  private static final String COURSE_ID = "hipaa-2026";
  private static final String SUBSCRIPTION_ID = "whs_01JD8D";
  private static final String OPERATION_ID = "op-01JD8H";
  private static final String TINY_ID = "cAbCdEfGhIjKl";
  private static final String TOKEN = "contract-test-token";

  private final ViolationRecorder violations = new ViolationRecorder();
  private TeachMeHipaaClient client;

  @BeforeEach
  void buildClient(PrismServer prism) {
    // Prism can't mint tokens from the form-encoded /auth/token, so the client sends a static one
    // and skips Organization discovery.
    client =
        TeachMeHipaaClient.builder()
            .tokenProvider(new StaticTokenProvider(TOKEN))
            .organizationId(ORG_ID)
            .baseUrl(prism.baseUrl())
            .maxRetries(0)
            .httpTransport(violations)
            .build();
  }

  @AfterEach
  void prismReportedNoRequestViolations() {
    assertEquals(List.of(), violations.found(), "Prism reported request violations");
  }

  @Test
  void theHarnessCatchesARequestThatBreaksTheSpec() {
    assertThrows(
        InvalidRequestException.class,
        () ->
            client
                .webhookSubscriptions()
                .create(
                    WebhookSubscriptionCreateParams.builder()
                        .endpointUrl("https://hr.example.com/hooks/tmh")
                        .eventTypes(List.of(EventType.SEATS_LOW))
                        .seatsLowThreshold(-1)
                        .build()));

    List<String> found = violations.found();
    assertEquals(1, found.size(), found.toString());
    assertTrue(found.get(0).contains("seatsLowThreshold"), found.get(0));
    violations.clear();
  }

  @Test
  void listsCoursesAsAPage() {
    Page<Course> page = client.courses().list(CourseListParams.builder().limit(5).build());

    for (Course course : page.items()) {
      assertNotNull(course.getCourseId());
    }
    assertEquals(200, page.getLastResponse().statusCode());
  }

  @Test
  void retrievesACourse() {
    Course course = client.courses().retrieve(COURSE_ID);

    assertNotNull(course.getTitle());
  }

  @Test
  void retrievesACertificateVerification() {
    CertificateVerification verification = client.certificateVerifications().retrieve(TINY_ID);

    assertNotNull(verification.getLearnerName());
    assertNotNull(verification.getIssuer());
  }

  @Test
  void createsALearnerAndReportsWhetherItWasCreated() {
    LearnerWriteResult result =
        client
            .learners()
            .create(
                LearnerCreateParams.builder()
                    .email("ana@example.com")
                    .name("Ana Example")
                    .externalId("hr-123")
                    .courseId(COURSE_ID)
                    .delivery(LearnerCreateRequestDelivery.NONE)
                    .build());

    assertNotNull(result.getLearnerId());
    assertTrue(List.of(200, 201).contains(result.getLastResponse().statusCode()));
  }

  @Test
  void listsLearnersWithFilters() {
    Page<Learner> page =
        client
            .learners()
            .list(
                LearnerListParams.builder()
                    .limit(10)
                    .status(LearnerStatus.IN_PROGRESS)
                    .email("ana@example.com")
                    .build());

    for (Learner learner : page.items()) {
      assertNotNull(learner.getEmail());
    }
  }

  @Test
  void retrievesALearner() {
    Learner learner =
        client
            .learners()
            .retrieve(LEARNER_ID, LearnerRetrieveParams.builder().courseId(COURSE_ID).build());

    assertNotNull(learner.getLearnerId());
    assertNotNull(learner.getCourse().getCourseId());
  }

  @Test
  void updatesALearnerAsAMergePatchWithIfMatch() {
    Learner learner =
        client
            .learners()
            .update(
                LEARNER_ID,
                LearnerUpdateParams.builder()
                    .ifMatch("\"7\"")
                    .name("Ana Q. Example")
                    .clearExternalId()
                    .build());

    assertNotNull(learner.getLearnerId());
  }

  @Test
  void cancelsAnEnrollment() {
    CancelEnrollmentResponse result =
        client
            .enrollments()
            .cancel(ENROLLMENT_ID, EnrollmentCancelParams.builder().refund(false).build());

    assertEquals(200, result.getLastResponse().statusCode());
  }

  @Test
  void retrievesACertificate() {
    Certificate certificate = client.certificates().retrieve(CERTIFICATE_ID);

    assertNotNull(certificate.getCertificateId());
  }

  @Test
  void downloadsACertificatePdf() {
    BinaryResponse pdf = client.certificates().downloadPdf(CERTIFICATE_ID);

    assertTrue(pdf.contentType().startsWith("application/pdf"), pdf.contentType());
  }

  @Test
  void streamsACertificatePdf() throws IOException {
    try (BinaryStreamResponse pdf = client.certificates().downloadPdfStream(CERTIFICATE_ID)) {
      pdf.inputStream().readAllBytes();

      assertTrue(pdf.contentType().startsWith("application/pdf"), pdf.contentType());
    }
  }

  @Test
  void createsAWebhookSubscriptionWithItsSigningSecret() {
    WebhookSubscriptionCreated subscription =
        client
            .webhookSubscriptions()
            .create(
                WebhookSubscriptionCreateParams.builder()
                    .endpointUrl("https://hr.example.com/hooks/tmh")
                    .eventTypes(List.of(EventType.LEARNER_CERTIFIED, EventType.SEATS_LOW))
                    .description("HR sync")
                    .seatsLowThreshold(5)
                    .build());

    assertNotNull(subscription.getSubscriptionId());
    assertFalse(subscription.getSecret().isEmpty());
  }

  @Test
  void deletesAWebhookSubscription() {
    EmptyResponse deleted = client.webhookSubscriptions().delete(SUBSCRIPTION_ID);

    assertEquals(204, deleted.getLastResponse().statusCode());
  }

  @Test
  void listsTheEventLog() {
    Page<Event> page = client.events().list(EventListParams.builder().limit(25).build());

    for (Event event : page.items()) {
      assertNotNull(event.getEventType());
    }
  }

  @Test
  void retrievesAnOperation() {
    Operation operation = client.operations().retrieve(OPERATION_ID);

    assertNotNull(operation.getName());
  }

  @Test
  void introspectsTheAccessToken() {
    TokenIntrospection introspection = client.auth().introspect();

    assertNotNull(introspection.getActorType());
  }
}
