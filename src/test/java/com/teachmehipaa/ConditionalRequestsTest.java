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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.PreconditionFailedException;
import com.teachmehipaa.model.Course;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.WebhookSubscription;
import com.teachmehipaa.param.LearnerOffboardParams;
import com.teachmehipaa.param.LearnerUpdateParams;
import com.teachmehipaa.param.WebhookSubscriptionUpdateParams;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Conditional writes ({@code If-Match}) and reads ({@code If-None-Match}). */
class ConditionalRequestsTest {
  private static final String ORG = "/v1/organizations/org_acme";
  private static final String LEARNER_PATH = ORG + "/learners/" + LEARNER_ID;
  private static final String OFFBOARD_ROUTE = "POST " + LEARNER_PATH + ":offboard";
  private static final String SUBSCRIPTION_ID = "whs_01JD8D";
  private static final String SUBSCRIPTION_PATH = ORG + "/webhook-subscriptions/" + SUBSCRIPTION_ID;

  // Learners carry a quoted SHA-256 hex strong tag (api-sdk-surface.md §5).
  private static final String LEARNER_ETAG =
      "\"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08\"";
  // Rev-based resources carry a quoted revision.
  private static final String COURSE_ETAG = "\"7\"";
  private static final String COURSE_ROUTE = "GET /v1/courses/hipaa-2026";
  private static final String COURSE_JSON =
      """
      {"courseId": "hipaa-2026", "title": "HIPAA Training 2026", "courseVersion": "2026.1",
       "passingThreshold": 80}
      """;
  private static final String SUBSCRIPTION_JSON =
      """
      {"subscriptionId": "whs_01JD8D", "endpointUrl": "https://hr.example.com/hooks/tmh",
       "eventTypes": ["learner.certified"], "enabled": false, "description": null,
       "seatsLowThreshold": 5, "secretKeyId": "whk_1", "createdAt": "2026-11-01T09:00:00Z",
       "rev": 4}
      """;

  // As the API's conditional middleware sends them.
  private static final Reply PRECONDITION_FAILED =
      Reply.problem(
          412,
          """
          {"type": "https://docs.teachmehipaa.com/errors/precondition_failed",
           "title": "Precondition failed", "status": 412, "code": "precondition_failed",
           "detail": "The resource has changed since you read it."}
          """);
  private static final Reply PRECONDITION_REQUIRED =
      Reply.problem(
          428,
          """
          {"type": "https://docs.teachmehipaa.com/errors/precondition_required",
           "title": "Precondition required", "status": 428, "code": "precondition_required",
           "detail": "Send If-Match with the resource's current ETag."}
          """);

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

  // --- Conditional writes ----------------------------------------------------------------------

  @Test
  void anUpdateSendsTheEtagAsIfMatch() {
    FakeTransport api =
        api().on("PATCH " + LEARNER_PATH, Reply.json(LEARNER_JSON).withHeader("etag", "\"next\""));

    Learner learner =
        clientOn(api)
            .learners()
            .update(
                LEARNER_ID,
                LearnerUpdateParams.builder().ifMatch(LEARNER_ETAG).name("Ana Example").build());

    assertEquals("\"next\"", learner.getLastResponse().etag());
    assertEquals(LEARNER_ETAG, only(api, "PATCH " + LEARNER_PATH).headers().get("if-match"));
  }

  @Test
  void anUpdateTakesTheEtagFromALearnerTheApiReturned() {
    FakeTransport api =
        api()
            .on("GET " + LEARNER_PATH, Reply.json(LEARNER_JSON).withHeader("etag", LEARNER_ETAG))
            .on("PATCH " + LEARNER_PATH, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = clientOn(api);

    Learner learner = client.learners().retrieve(LEARNER_ID);
    client
        .learners()
        .update(
            LEARNER_ID, LearnerUpdateParams.builder().ifMatch(learner).clearExternalId().build());

    assertEquals(LEARNER_ETAG, only(api, "PATCH " + LEARNER_PATH).headers().get("if-match"));
  }

  @Test
  void anUpdateCannotBeBuiltWithoutIfMatch() {
    LearnerUpdateParams.Builder params = LearnerUpdateParams.builder().name("Ana Example");

    IllegalStateException missing = assertThrows(IllegalStateException.class, params::build);

    assertEquals("ifMatch is required", missing.getMessage());
  }

  @Test
  void anObjectWithoutAnEtagIsRefusedBeforeAnythingIsSent() {
    FakeTransport api = api().on("GET " + LEARNER_PATH, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = clientOn(api);
    Learner learner = client.learners().retrieve(LEARNER_ID);

    IllegalArgumentException refused =
        assertThrows(
            IllegalArgumentException.class, () -> LearnerOffboardParams.builder().ifMatch(learner));

    assertTrue(refused.getMessage().contains("ETag"), refused.getMessage());
    assertTrue(api.requestsTo(OFFBOARD_ROUTE).isEmpty());
  }

  @Test
  void anOptionalIfMatchIsLeftOutWhenNotGiven() {
    FakeTransport api = api().on(OFFBOARD_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api).learners().offboard(LEARNER_ID);

    assertFalse(only(api, OFFBOARD_ROUTE).headers().containsKey("if-match"));
  }

  @Test
  void anOffboardSendsAnIfMatchWhenGiven() {
    FakeTransport api = api().on(OFFBOARD_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api)
        .learners()
        .offboard(LEARNER_ID, LearnerOffboardParams.builder().ifMatch(LEARNER_ETAG).build());

    assertEquals(LEARNER_ETAG, only(api, OFFBOARD_ROUTE).headers().get("if-match"));
  }

  @Test
  void aStaleEtagRaisesPreconditionFailedAndIsNotRetried() {
    FakeTransport api = api().on("PATCH " + LEARNER_PATH, PRECONDITION_FAILED);

    PreconditionFailedException failed =
        assertThrows(
            PreconditionFailedException.class,
            () ->
                clientOn(api)
                    .learners()
                    .update(
                        LEARNER_ID,
                        LearnerUpdateParams.builder()
                            .ifMatch(LEARNER_ETAG)
                            .name("Ana Example")
                            .build()));

    assertEquals(412, failed.getStatusCode());
    assertEquals("precondition_failed", failed.getCode());
    assertEquals(1, api.requestsTo("PATCH " + LEARNER_PATH).size());
  }

  @Test
  void aPreconditionRequiredAnswerRaisesPreconditionFailed() {
    FakeTransport api = api().on("PATCH " + SUBSCRIPTION_PATH, PRECONDITION_REQUIRED);

    PreconditionFailedException failed =
        assertThrows(
            PreconditionFailedException.class,
            () ->
                clientOn(api)
                    .webhookSubscriptions()
                    .update(
                        SUBSCRIPTION_ID,
                        WebhookSubscriptionUpdateParams.builder()
                            .ifMatch("*")
                            .enabled(false)
                            .build()));

    assertEquals(428, failed.getStatusCode());
    assertEquals("precondition_required", failed.getCode());
  }

  @Test
  void aSubscriptionUpdateTakesTheEtagFromTheSubscriptionTheApiReturned() {
    FakeTransport api =
        api()
            .on(
                "GET " + SUBSCRIPTION_PATH,
                Reply.json(SUBSCRIPTION_JSON).withHeader("etag", "\"4\""))
            .on("PATCH " + SUBSCRIPTION_PATH, Reply.json(SUBSCRIPTION_JSON));
    TeachMeHipaaClient client = clientOn(api);

    WebhookSubscription subscription = client.webhookSubscriptions().retrieve(SUBSCRIPTION_ID);
    client
        .webhookSubscriptions()
        .update(
            SUBSCRIPTION_ID,
            WebhookSubscriptionUpdateParams.builder().ifMatch(subscription).enabled(true).build());

    assertEquals("\"4\"", only(api, "PATCH " + SUBSCRIPTION_PATH).headers().get("if-match"));
  }

  // --- Conditional reads -----------------------------------------------------------------------

  @Test
  void aCurrentCopyComesBackEmpty() {
    FakeTransport api =
        api()
            .on(
                COURSE_ROUTE,
                new Reply(304, Map.of("etag", COURSE_ETAG, "x-request-id", "req_304"), ""));

    Optional<Course> course = clientOn(api).courses().retrieveIfModified("hipaa-2026", COURSE_ETAG);

    assertTrue(course.isEmpty());
    assertEquals(COURSE_ETAG, only(api, COURSE_ROUTE).headers().get("if-none-match"));
  }

  @Test
  void aChangedObjectIsReturnedUsingTheHeldCopysEtag() {
    FakeTransport api =
        api()
            .on(COURSE_ROUTE, Reply.json(COURSE_JSON).withHeader("etag", COURSE_ETAG))
            .on(
                COURSE_ROUTE,
                Reply.json(COURSE_JSON.replace("2026.1", "2026.2")).withHeader("etag", "\"8\""));
    TeachMeHipaaClient client = clientOn(api);
    Course cached = client.courses().retrieve("hipaa-2026");

    Course current = client.courses().retrieveIfModified("hipaa-2026", cached).orElse(cached);

    assertEquals("2026.2", current.getCourseVersion());
    assertEquals("\"8\"", current.getLastResponse().etag());
    assertEquals(COURSE_ETAG, api.requestsTo(COURSE_ROUTE).get(1).headers().get("if-none-match"));
  }

  @Test
  void aPlainRetrieveSendsNoIfNoneMatch() {
    FakeTransport api = api().on(COURSE_ROUTE, Reply.json(COURSE_JSON));

    Course course = clientOn(api).courses().retrieve("hipaa-2026");

    assertEquals("HIPAA Training 2026", course.getTitle());
    assertFalse(only(api, COURSE_ROUTE).headers().containsKey("if-none-match"));
  }

  @Test
  void aCurrentSubscriptionComesBackEmptyAndAnAuthenticatedReadStillCarriesItsToken() {
    String route = "GET " + SUBSCRIPTION_PATH;
    FakeTransport api = api().on(route, new Reply(304, Map.of("etag", "\"4\""), ""));

    Optional<WebhookSubscription> subscription =
        clientOn(api)
            .webhookSubscriptions()
            .retrieveIfModified(SUBSCRIPTION_ID, "\"4\"", RequestOptions.builder().build());

    assertTrue(subscription.isEmpty());
    TransportRequest request = only(api, route);
    assertEquals("\"4\"", request.headers().get("if-none-match"));
    assertEquals("Bearer " + ACCESS_TOKEN, request.headers().get("authorization"));
  }

  @Test
  void aHeldCopyWithoutAnEtagIsRefusedBeforeAnythingIsSent() {
    FakeTransport api = api().on(COURSE_ROUTE, Reply.json(COURSE_JSON));
    TeachMeHipaaClient client = clientOn(api);
    Course cached = client.courses().retrieve("hipaa-2026");

    assertThrows(
        IllegalArgumentException.class,
        () -> client.courses().retrieveIfModified("hipaa-2026", cached));

    assertEquals(1, api.requestsTo(COURSE_ROUTE).size());
  }

  @Test
  void aConditionalReadOfAMissingObjectStillRaises() {
    FakeTransport api =
        api()
            .on(
                COURSE_ROUTE,
                Reply.problem(
                    404, "{\"title\": \"Not found\", \"status\": 404, \"code\": \"not_found\"}"));

    assertThrows(
        NotFoundException.class,
        () -> clientOn(api).courses().retrieveIfModified("hipaa-2026", COURSE_ETAG));
    assertEquals(1, api.requestsTo(COURSE_ROUTE).size());
  }
}
