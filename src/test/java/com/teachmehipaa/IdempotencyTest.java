package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerUpdateParams;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IdempotencyTest {
  private static final String CREATE_ROUTE = "POST /v1/organizations/org_acme/learners";
  private static final String UPDATE_ROUTE =
      "PATCH /v1/organizations/org_acme/learners/" + LEARNER_ID;
  private static final Pattern UUID_V4 =
      Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  private static final ObjectMapper JSON = new ObjectMapper();

  private static final Reply UNAVAILABLE =
      Reply.problem(
          503,
          """
          {"title": "Service Unavailable", "status": 503, "code": "not_configured"}
          """);
  private static final Reply CREATED = Reply.json(201, LEARNER_JSON);

  private static final LearnerCreateParams ANA =
      LearnerCreateParams.builder().email("ana@example.com").build();

  private static FakeTransport.Responder networkFailure() {
    return request -> {
      throw new ConnectException("Connection refused");
    };
  }

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  private static JsonNode json(String text) throws IOException {
    return JSON.readTree(text);
  }

  private static JsonNode body(TransportRequest request) throws IOException {
    return JSON.readTree(request.body());
  }

  private static List<String> idempotencyKeys(FakeTransport api, String route) {
    return api.requestsTo(route).stream()
        .map(request -> request.headers().get("idempotency-key"))
        .toList();
  }

  @Test
  void sendsACreateAsJsonWithAGeneratedUuidV4IdempotencyKey() throws IOException {
    FakeTransport api = api().on(CREATE_ROUTE, CREATED);

    Learner learner =
        clientOn(api)
            .learners()
            .create(
                LearnerCreateParams.builder()
                    .email("ana@example.com")
                    .externalId("hr-123")
                    .build());

    assertEquals(201, learner.getLastResponse().statusCode());
    assertEquals(LEARNER_ID, learner.getLearnerId());
    TransportRequest request = api.requestsTo(CREATE_ROUTE).get(0);
    assertEquals("application/json", request.headers().get("content-type"));
    assertTrue(UUID_V4.matcher(request.headers().get("idempotency-key")).matches());
    assertEquals(
        json("{\"email\": \"ana@example.com\", \"externalId\": \"hr-123\"}"), body(request));
  }

  @Test
  void retriesACreateWithTheSameIdempotencyKeyAndBody() {
    FakeTransport api =
        api()
            .on(CREATE_ROUTE, UNAVAILABLE)
            .on(CREATE_ROUTE, networkFailure())
            .on(CREATE_ROUTE, CREATED);

    clientOn(api).learners().create(ANA);

    List<TransportRequest> attempts = api.requestsTo(CREATE_ROUTE);
    assertEquals(3, attempts.size());
    for (TransportRequest retry : attempts.subList(1, 3)) {
      assertEquals(
          attempts.get(0).headers().get("idempotency-key"), retry.headers().get("idempotency-key"));
      assertArrayEquals(attempts.get(0).body(), retry.body());
    }
  }

  @Test
  void generatesANewIdempotencyKeyForEachCall() {
    FakeTransport api = api().on(CREATE_ROUTE, CREATED);
    TeachMeHipaaClient client = clientOn(api);

    client.learners().create(ANA);
    client.learners().create(ANA);

    List<String> keys = idempotencyKeys(api, CREATE_ROUTE);
    assertEquals(2, keys.size());
    assertNotEquals(keys.get(0), keys.get(1));
  }

  @Test
  void usesACallerSuppliedIdempotencyKeyAsIsRetriesIncluded() {
    FakeTransport api = api().on(CREATE_ROUTE, UNAVAILABLE).on(CREATE_ROUTE, CREATED);

    clientOn(api)
        .learners()
        .create(ANA, RequestOptions.builder().idempotencyKey("hr-sync-2026-11-15-hr-123").build());

    assertEquals(
        List.of("hr-sync-2026-11-15-hr-123", "hr-sync-2026-11-15-hr-123"),
        idempotencyKeys(api, CREATE_ROUTE));
  }

  @Test
  void sendsNoIdempotencyKeyContentTypeOrBodyOnAGet() {
    FakeTransport api = api().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    clientOn(api).learners().retrieve(LEARNER_ID);

    TransportRequest request = api.requestsTo(LEARNER_ROUTE).get(0);
    assertFalse(request.headers().containsKey("idempotency-key"));
    assertFalse(request.headers().containsKey("content-type"));
    assertNull(request.body());
  }

  static Stream<Arguments> transientFailures() {
    return Stream.of(
        Arguments.of("a 5xx", (FakeTransport.Responder) request -> UNAVAILABLE, ApiException.class),
        Arguments.of("a network error", networkFailure(), ApiConnectionException.class));
  }

  @ParameterizedTest(name = "does not retry a PATCH after {0}")
  @MethodSource("transientFailures")
  void doesNotRetryAPatch(
      String name,
      FakeTransport.Responder failure,
      Class<? extends TeachMeHipaaException> exceptionClass) {
    FakeTransport api = api().on(UPDATE_ROUTE, failure).on(UPDATE_ROUTE, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = clientOn(api);

    assertThrows(
        exceptionClass,
        () ->
            client
                .learners()
                .update(LEARNER_ID, LearnerUpdateParams.builder().name("Ana Example").build()));

    List<TransportRequest> attempts = api.requestsTo(UPDATE_ROUTE);
    assertEquals(1, attempts.size());
    assertEquals("application/merge-patch+json", attempts.get(0).headers().get("content-type"));
    assertFalse(attempts.get(0).headers().containsKey("idempotency-key"));
  }

  @Test
  void aCallerSuppliedIdempotencyKeyDoesNotMakeAPatchRetryable() {
    FakeTransport api =
        api().on(UPDATE_ROUTE, UNAVAILABLE).on(UPDATE_ROUTE, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = clientOn(api);

    assertThrows(
        ApiException.class,
        () ->
            client
                .learners()
                .update(
                    LEARNER_ID,
                    LearnerUpdateParams.builder().name("Ana Example").build(),
                    RequestOptions.builder().idempotencyKey("patch-1").build()));

    assertEquals(List.of("patch-1"), idempotencyKeys(api, UPDATE_ROUTE));
  }

  @Test
  void anUpdateSendsOnlyTheFieldsGivenAndNullForAClearedOne() throws IOException {
    FakeTransport api = api().on(UPDATE_ROUTE, Reply.json(LEARNER_JSON));

    Learner learner =
        clientOn(api)
            .learners()
            .update(
                LEARNER_ID,
                LearnerUpdateParams.builder().name("Ana Lima").clearExternalId().build());

    assertEquals("Ana Lima", learner.getName());
    assertEquals(
        json("{\"name\": \"Ana Lima\", \"externalId\": null}"),
        body(api.requestsTo(UPDATE_ROUTE).get(0)));
  }
}
