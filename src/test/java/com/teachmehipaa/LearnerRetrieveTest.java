package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.CERTIFIED_LEARNER_JSON;
import static com.teachmehipaa.Fixtures.ETAG;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.REQUEST_ID;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.learnerRetrieveApi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teachmehipaa.core.LastResponse;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerCertificate;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.param.LearnerRetrieveParams;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LearnerRetrieveTest {
  @Test
  void clientRetrievesALearnerById() {
    FakeTransport api = learnerRetrieveApi();

    Learner learner = clientFor(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(LEARNER_ID, learner.getLearnerId());
    assertEquals("ana@example.com", learner.getEmail());
    assertEquals("Ana Lima", learner.getName());
    assertEquals("hr-123", learner.getExternalId());
    assertEquals(LearnerStatus.IN_PROGRESS, learner.getStatus());
    assertEquals("hipaa-essentials", learner.getCourse().getCourseId());
    assertEquals("HIPAA Essentials", learner.getCourse().getTitle());
    assertEquals(OffsetDateTime.parse("2026-11-01T09:00:00Z"), learner.getInvitedAt());
    assertEquals(OffsetDateTime.parse("2026-11-02T10:30:00Z"), learner.getStartedAt());
    assertNull(learner.getCompletedAt());
    assertEquals(OffsetDateTime.parse("2026-12-01T00:00:00Z"), learner.getDueAt());
    assertNull(learner.getCertificate());
    List<TransportRequest> sent = api.requestsTo(LEARNER_ROUTE);
    assertEquals(1, sent.size());
    assertEquals("Bearer " + ACCESS_TOKEN, sent.get(0).headers().get("authorization"));
    assertEquals("application/json", sent.get(0).headers().get("accept"));
    assertNull(sent.get(0).body());
  }

  @Test
  void theReturnedLearnerExposesItsResponseMetadata() {
    Learner learner = clientFor(learnerRetrieveApi()).build().learners().retrieve(LEARNER_ID);

    LastResponse response = learner.getLastResponse();
    assertEquals(200, response.statusCode());
    assertEquals(REQUEST_ID, response.requestId());
    assertEquals(ETAG, response.etag());
    assertEquals(Optional.of("application/json"), response.headers().firstValue("Content-Type"));
  }

  @Test
  void readsALearnersCertificate() {
    FakeTransport api =
        learnerRetrieveApi().replace(LEARNER_ROUTE, Reply.json(CERTIFIED_LEARNER_JSON));

    Learner learner = clientFor(api).build().learners().retrieve(LEARNER_ID);

    assertEquals(LearnerStatus.COMPLIANT, learner.getStatus());
    assertNull(learner.getName());
    assertNull(learner.getExternalId());
    LearnerCertificate certificate = learner.getCertificate();
    assertEquals("cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH", certificate.getCertificateId());
    assertEquals(OffsetDateTime.parse("2026-11-03T15:45:00Z"), certificate.getIssuedAt());
    assertEquals(OffsetDateTime.parse("2027-11-03T15:45:00Z"), certificate.getExpiresAt());
    assertEquals(
        "https://teachmehipaa.com/verify/cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH",
        certificate.getVerifyUrl());
    assertEquals(
        "/v1/organizations/org_acme/certificates/cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH/pdf",
        certificate.getPdfUrl());
  }

  @Test
  void passesTheCourseAsAQueryParameter() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api)
        .build()
        .learners()
        .retrieve(LEARNER_ID, LearnerRetrieveParams.builder().courseId("hipaa advanced").build());

    assertEquals(
        "courseId=hipaa%20advanced", api.requestsTo(LEARNER_ROUTE).get(0).uri().getRawQuery());
  }

  @Test
  void omitsTheCourseWhenNotGiven() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api).build().learners().retrieve(LEARNER_ID);

    assertNull(api.requestsTo(LEARNER_ROUTE).get(0).uri().getRawQuery());
  }

  @Test
  void escapesTheLearnerIdInThePath() {
    FakeTransport api =
        learnerRetrieveApi()
            .on("GET /v1/organizations/org_acme/learners/a%2Fb%3Fc", Reply.json(LEARNER_JSON));

    clientFor(api).build().learners().retrieve("a/b?c");

    assertEquals(1, api.requestsTo("GET /v1/organizations/org_acme/learners/a%2Fb%3Fc").size());
  }

  @Test
  void keepsUnknownFieldsAndNewStatusValues() {
    FakeTransport api =
        learnerRetrieveApi()
            .replace(
                LEARNER_ROUTE,
                Reply.json(
                    LEARNER_JSON
                        .replace("\"in_progress\"", "\"on_leave\"")
                        .replace(
                            "\"certificate\": null",
                            "\"certificate\": null, \"department\": \"Billing\"")
                        .replace(
                            "\"title\": \"HIPAA Essentials\"",
                            "\"title\": \"HIPAA Essentials\", \"hours\": 2")));

    Learner learner = clientFor(api).build().learners().retrieve(LEARNER_ID);

    assertEquals("on_leave", learner.getStatus().value());
    assertFalse(learner.getStatus().isKnown());
    assertEquals(LearnerStatus.of("on_leave"), learner.getStatus());
    assertEquals(Map.of("department", "Billing"), learner.getExtraFields());
    assertEquals(Map.of("hours", 2), learner.getCourse().getExtraFields());
    assertThrows(UnsupportedOperationException.class, () -> learner.getExtraFields().put("x", "y"));
  }
}
