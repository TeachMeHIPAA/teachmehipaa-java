package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerCreateRequestDelivery;
import com.teachmehipaa.model.LearnerCreatedDelivery;
import com.teachmehipaa.model.LearnerUpsertRequestDelivery;
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerUpdateParams;
import com.teachmehipaa.param.LearnerUpsertByExternalIdParams;
import com.teachmehipaa.param.WebhookSubscriptionUpdateParams;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Learner updates as merge patches, and the write results of Learner create and upsert. */
class LearnerWritesTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String ORG = "/v1/organizations/org_acme";
  private static final String LEARNER_ROUTE = "PATCH " + ORG + "/learners/" + LEARNER_ID;
  private static final String CREATE_ROUTE = "POST " + ORG + "/learners";
  private static final String UPSERT_ROUTE = "PUT " + ORG + "/learners/by-external-id/hr-123";
  private static final String SUBSCRIPTION_ROUTE = "PATCH " + ORG + "/webhook-subscriptions/whs_1";
  private static final String ETAG = "\"9f86d081\"";

  // dev#/components/schemas/LearnerCreated with delivery "none".
  private static final String CREATED_JSON =
      """
      {
        "learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG",
        "email": "ana@example.com",
        "name": "Ana Lima",
        "externalId": "hr-123",
        "status": "not_started",
        "course": {"courseId": "hipaa-essentials", "title": "HIPAA Essentials"},
        "invitedAt": "2026-11-01T09:00:00Z",
        "startedAt": null,
        "completedAt": null,
        "dueAt": "2026-12-01T00:00:00Z",
        "certificate": null,
        "delivery": "none",
        "joinUrl": "https://tme.so/j/7QX2"
      }
      """;
  private static final String SUBSCRIPTION_JSON =
      """
      {"subscriptionId": "whs_1", "endpointUrl": "https://hr.example.com/hooks/tmh",
       "eventTypes": ["learner.certified"], "enabled": true, "description": null,
       "seatsLowThreshold": 5, "secretKeyId": "whk_1", "createdAt": "2026-11-01T09:00:00Z",
       "rev": 4}
      """;

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  private static TransportRequest only(FakeTransport api, String route) {
    assertEquals(1, api.requestsTo(route).size(), route);
    return api.requestsTo(route).get(0);
  }

  private static JsonNode sentJson(TransportRequest request) throws IOException {
    return JSON.readTree(request.body());
  }

  // --- Merge patches ---------------------------------------------------------------------------

  @Test
  void aLearnerUpdateLeavesUnsetFieldsOutAndSendsNullToClear() {
    FakeTransport api =
        api().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON.replace("\"hr-123\"", "null")));

    Learner learner =
        clientOn(api)
            .learners()
            .update(
                LEARNER_ID, LearnerUpdateParams.builder().ifMatch(ETAG).clearExternalId().build());

    assertNull(learner.getExternalId());
    TransportRequest request = only(api, LEARNER_ROUTE);
    assertEquals("application/merge-patch+json", request.headers().get("content-type"));
    assertEquals("{\"externalId\":null}", new String(request.body(), StandardCharsets.UTF_8));
  }

  @Test
  void aLearnerUpdateSendsTheValuesSetAndKeepsTheCourseQueryOutOfThePatch() throws IOException {
    FakeTransport api = api().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api)
        .learners()
        .update(
            LEARNER_ID,
            LearnerUpdateParams.builder()
                .ifMatch(ETAG)
                .name("Ana B. Example")
                .externalId("hr-456")
                .courseId("hipaa-2026")
                .build());

    TransportRequest request = only(api, LEARNER_ROUTE);
    assertEquals("courseId=hipaa-2026", request.uri().getRawQuery());
    assertEquals(
        JSON.readTree("{\"name\": \"Ana B. Example\", \"externalId\": \"hr-456\"}"),
        sentJson(request));
  }

  @Test
  void aLearnerUpdateWithNothingSetSendsAnEmptyPatch() {
    FakeTransport api = api().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api)
        .learners()
        .update(LEARNER_ID, LearnerUpdateParams.builder().ifMatch(ETAG).build());

    assertEquals("{}", new String(only(api, LEARNER_ROUTE).body(), StandardCharsets.UTF_8));
  }

  @Test
  void aSubscriptionUpdateSendsOnlyWhatWasGivenAndNullForWhatWasCleared() throws IOException {
    FakeTransport api = api().on(SUBSCRIPTION_ROUTE, Reply.json(SUBSCRIPTION_JSON));

    clientOn(api)
        .webhookSubscriptions()
        .update(
            "whs_1",
            WebhookSubscriptionUpdateParams.builder()
                .ifMatch("\"4\"")
                .seatsLowThreshold(10)
                .clearDescription()
                .build());

    TransportRequest request = only(api, SUBSCRIPTION_ROUTE);
    assertEquals("application/merge-patch+json", request.headers().get("content-type"));
    assertEquals(
        JSON.readTree("{\"seatsLowThreshold\": 10, \"description\": null}"), sentJson(request));
  }

  @Test
  void aValueSetAfterAClearIsSentInsteadOfNull() throws IOException {
    FakeTransport api = api().on(SUBSCRIPTION_ROUTE, Reply.json(SUBSCRIPTION_JSON));

    clientOn(api)
        .webhookSubscriptions()
        .update(
            "whs_1",
            WebhookSubscriptionUpdateParams.builder()
                .ifMatch("\"4\"")
                .clearDescription()
                .description("HR sync")
                .build());

    assertEquals(
        JSON.readTree("{\"description\": \"HR sync\"}"), sentJson(only(api, SUBSCRIPTION_ROUTE)));
  }

  // --- Learner write results -------------------------------------------------------------------

  @Test
  void creatingALearnerReportsCreatedWithItsDeliveryDetails() throws IOException {
    FakeTransport api =
        api().on(CREATE_ROUTE, Reply.json(201, CREATED_JSON).withHeader("etag", ETAG));

    LearnerWriteResult result =
        clientOn(api)
            .learners()
            .create(
                LearnerCreateParams.builder()
                    .email("ana@example.com")
                    .delivery(LearnerCreateRequestDelivery.NONE)
                    .build());

    assertTrue(result.isCreated());
    assertEquals(LearnerCreatedDelivery.NONE, result.getDelivery());
    assertEquals("https://tme.so/j/7QX2", result.getJoinUrl());
    assertEquals(LEARNER_ID, result.getLearnerId());
    assertEquals(201, result.getLastResponse().statusCode());
    assertEquals(ETAG, result.getLastResponse().etag());
    TransportRequest request = only(api, CREATE_ROUTE);
    assertEquals(
        JSON.readTree("{\"email\": \"ana@example.com\", \"delivery\": \"none\"}"),
        sentJson(request));
    assertTrue(request.headers().containsKey("idempotency-key"));
  }

  @Test
  void aLearnerAlreadyThereIsNotCreatedAndHasNoDeliveryDetails() {
    FakeTransport api = api().on(CREATE_ROUTE, Reply.json(200, LEARNER_JSON));

    LearnerWriteResult result =
        clientOn(api)
            .learners()
            .create(LearnerCreateParams.builder().email("ana@example.com").build());

    assertFalse(result.isCreated());
    assertEquals(LEARNER_ID, result.getLearnerId());
    assertNull(result.getDelivery());
    assertNull(result.getJoinUrl());
    assertFalse(result.getExtraFields().containsKey("created"));
  }

  @Test
  void aReplayedUpsertIsToldApartFromTheFirst() {
    FakeTransport api =
        api()
            .on(UPSERT_ROUTE, Reply.json(201, CREATED_JSON))
            .on(UPSERT_ROUTE, Reply.json(200, LEARNER_JSON));
    TeachMeHipaaClient client = clientOn(api);
    LearnerUpsertByExternalIdParams params =
        LearnerUpsertByExternalIdParams.builder()
            .email("ana@example.com")
            .delivery(LearnerUpsertRequestDelivery.NONE)
            .build();

    LearnerWriteResult first = client.learners().upsertByExternalId("hr-123", params);
    LearnerWriteResult replay = client.learners().upsertByExternalId("hr-123", params);

    assertTrue(first.isCreated());
    assertEquals("https://tme.so/j/7QX2", first.getJoinUrl());
    assertEquals(LearnerCreatedDelivery.NONE, first.getDelivery());
    assertFalse(replay.isCreated());
    assertNull(replay.getJoinUrl());
    assertNull(replay.getDelivery());
  }
}
