package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.REQUEST_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.learnerRetrieveApi;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.ApiTimeoutException;
import com.teachmehipaa.exception.AuthenticationException;
import com.teachmehipaa.exception.ConflictException;
import com.teachmehipaa.exception.FieldError;
import com.teachmehipaa.exception.IdempotencyException;
import com.teachmehipaa.exception.InvalidRequestException;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.OAuthException;
import com.teachmehipaa.exception.PermissionDeniedException;
import com.teachmehipaa.exception.PreconditionFailedException;
import com.teachmehipaa.exception.RateLimitException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ErrorsTest {
  // Retries are off so each scripted failure is the one the caller sees.
  private static TeachMeHipaaException retrieveFailing(FakeTransport api) {
    TeachMeHipaaClient client = clientFor(api).maxRetries(0).build();
    return assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));
  }

  private static TeachMeHipaaException retrieveFailingWith(Reply reply) {
    return retrieveFailing(learnerRetrieveApi().replace(LEARNER_ROUTE, reply));
  }

  private static Reply problem(int status, String code) {
    return Reply.problem(
        status,
        """
        {"title": "Problem", "status": %d, "code": "%s", "detail": "A %s problem."}
        """
            .formatted(status, code, code));
  }

  @Test
  void aProblemResponseRaisesWithItsFields() {
    TeachMeHipaaException error =
        retrieveFailingWith(
            Reply.problem(
                    404,
                    """
                    {"title": "Not Found", "status": 404, "code": "not_found",
                     "detail": "No such learner."}
                    """)
                .withHeader("x-request-id", REQUEST_ID));

    assertInstanceOf(NotFoundException.class, error);
    assertEquals(404, error.getStatusCode());
    assertEquals("not_found", error.getCode());
    assertEquals("Not Found", error.getTitle());
    assertEquals("No such learner.", error.getDetail());
    assertEquals(List.of(), error.getErrors());
    assertEquals(REQUEST_ID, error.getRequestId());
    assertEquals(Optional.of(REQUEST_ID), error.getHeaders().firstValue("X-Request-Id"));
    assertEquals("Not Found: No such learner. (request id: req_7f3c2a)", error.getMessage());
  }

  static Stream<Arguments> statusesAndTheirExceptions() {
    return Stream.of(
        Arguments.of(400, "validation_failed", InvalidRequestException.class),
        Arguments.of(401, "unauthorized", AuthenticationException.class),
        Arguments.of(403, "forbidden", PermissionDeniedException.class),
        Arguments.of(403, "spend_limit_exceeded", PermissionDeniedException.class),
        Arguments.of(404, "not_found", NotFoundException.class),
        Arguments.of(405, "method_not_allowed", InvalidRequestException.class),
        Arguments.of(406, "not_acceptable", InvalidRequestException.class),
        Arguments.of(409, "seat_unavailable", ConflictException.class),
        Arguments.of(412, "precondition_failed", PreconditionFailedException.class),
        Arguments.of(415, "unsupported_media_type", InvalidRequestException.class),
        Arguments.of(422, "batch_too_large", InvalidRequestException.class),
        Arguments.of(428, "precondition_required", PreconditionFailedException.class),
        Arguments.of(429, "rate_limited", RateLimitException.class),
        Arguments.of(500, "internal_error", ApiException.class),
        Arguments.of(502, "refund_failed", ApiException.class),
        Arguments.of(503, "temporarily_unavailable", ApiException.class),
        Arguments.of(418, "brewing", ApiException.class));
  }

  @ParameterizedTest(name = "HTTP {0} {1} raises {2}")
  @MethodSource("statusesAndTheirExceptions")
  void eachStatusRaisesItsException(
      int status, String code, Class<? extends TeachMeHipaaException> exceptionClass) {
    TeachMeHipaaException error = retrieveFailingWith(problem(status, code));

    assertSame(exceptionClass, error.getClass());
    assertEquals(status, error.getStatusCode());
    assertEquals(code, error.getCode());
    assertEquals("A " + code + " problem.", error.getDetail());
  }

  @Test
  void idempotencyKeyReuseRaisesAnIdempotencyExceptionWhichIsAnInvalidRequest() {
    TeachMeHipaaException error = retrieveFailingWith(problem(422, "idempotency_key_reuse"));

    assertInstanceOf(IdempotencyException.class, error);
    assertInstanceOf(InvalidRequestException.class, error);
    assertEquals("idempotency_key_reuse", error.getCode());
  }

  @Test
  void anUnknownProblemCodeComesThroughAsIs() {
    TeachMeHipaaException error =
        retrieveFailingWith(
            Reply.problem(
                409,
                """
                {"title": "Conflict", "status": 409, "code": "learner_on_sabbatical"}
                """));

    assertInstanceOf(ConflictException.class, error);
    assertEquals("learner_on_sabbatical", error.getCode());
    assertEquals("Conflict", error.getMessage());
  }

  @Test
  void aRateLimitExceptionCarriesRetryAfter() {
    RateLimitException error =
        assertInstanceOf(
            RateLimitException.class,
            retrieveFailingWith(problem(429, "rate_limited").withHeader("retry-after", "7")));

    assertEquals(Duration.ofSeconds(7), error.getRetryAfter());
  }

  @Test
  void aRateLimitExceptionWithoutRetryAfterHasNone() {
    RateLimitException error =
        assertInstanceOf(RateLimitException.class, retrieveFailingWith(problem(429, "x")));

    assertNull(error.getRetryAfter());
  }

  @Test
  void aValidationProblemCarriesItsFieldErrors() {
    TeachMeHipaaException error =
        retrieveFailingWith(
            Reply.problem(
                422,
                """
                {"title": "Unprocessable Entity", "status": 422, "code": "validation_failed",
                 "errors": [{"path": "courseId", "message": "Too long"},
                            {"path": "Idempotency-Key", "message": "Required"}]}
                """));

    assertInstanceOf(InvalidRequestException.class, error);
    assertNull(error.getDetail());
    assertEquals(
        List.of(
            new FieldError("courseId", "Too long"), new FieldError("Idempotency-Key", "Required")),
        error.getErrors());
    assertThrows(UnsupportedOperationException.class, () -> error.getErrors().clear());
  }

  @Test
  void anHtmlPageIsRaisedAsAnApiExceptionInsteadOfParsed() {
    TeachMeHipaaException error =
        retrieveFailingWith(
            new Reply(200, Map.of("content-type", "text/html"), "<!doctype html><html>"));

    assertInstanceOf(ApiException.class, error);
    assertEquals(200, error.getStatusCode());
    assertNull(error.getCode());
    assertEquals("<!doctype html><html>", error.getRawBody());
    assertEquals(
        "Expected a JSON response but got text/html with HTTP 200; check the base URL",
        error.getMessage());
  }

  @Test
  void jsonThatDoesNotParseIsRaisedAsAnApiException() {
    TeachMeHipaaException error = retrieveFailingWith(Reply.json("{\"learnerId\": \"sub_"));

    assertInstanceOf(ApiException.class, error);
    assertEquals(200, error.getStatusCode());
    assertEquals("{\"learnerId\": \"sub_", error.getRawBody());
  }

  @Test
  void aJsonNullWhereALearnerWasExpectedIsRaisedAsAnApiException() {
    TeachMeHipaaException error = retrieveFailingWith(Reply.json("null"));

    assertInstanceOf(ApiException.class, error);
    assertEquals(200, error.getStatusCode());
  }

  @Test
  void anEmptySuccessWhereALearnerWasExpectedIsRaisedAsAnApiException() {
    TeachMeHipaaException error = retrieveFailingWith(Reply.empty(204));

    assertInstanceOf(ApiException.class, error);
    assertEquals(204, error.getStatusCode());
  }

  @Test
  void aNonProblemErrorBodyRaisesAnApiExceptionWithItsStatus() {
    TeachMeHipaaException error =
        retrieveFailingWith(
            Reply.json(
                503,
                """
                {"statusCode": 503, "statusMessage": "Service Unavailable"}
                """));

    assertInstanceOf(ApiException.class, error);
    assertEquals(503, error.getStatusCode());
    assertEquals("TeachMeHIPAA API responded with HTTP 503", error.getMessage());
  }

  @Test
  void noResponseRaisesAnApiConnectionExceptionWithoutAStatus() {
    ConnectException refused = new ConnectException("Connection refused");
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")))
            .fail(LEARNER_ROUTE, refused);

    TeachMeHipaaException error = retrieveFailing(api);

    assertSame(ApiConnectionException.class, error.getClass());
    assertNull(error.getStatusCode());
    assertNull(error.getRequestId());
    assertEquals(Map.of(), error.getHeaders().map());
    assertEquals(refused, error.getCause());
    assertTrue(error.getMessage().contains("api.tme.so"), error.getMessage());
    assertFalse(error.getMessage().contains(LEARNER_ID), error.getMessage());
  }

  @Test
  void aTimeoutRaisesAnApiTimeoutExceptionWhichIsAConnectionException() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .fail(LEARNER_ROUTE, new HttpTimeoutException("request timed out"));

    TeachMeHipaaException error =
        assertThrows(
            TeachMeHipaaException.class,
            () ->
                clientFor(api)
                    .organizationId(ORG_ID)
                    .maxRetries(0)
                    .build()
                    .learners()
                    .retrieve(LEARNER_ID));

    assertInstanceOf(ApiTimeoutException.class, error);
    assertInstanceOf(ApiConnectionException.class, error);
    assertNull(error.getStatusCode());
  }

  @Test
  void theTokenEndpointsErrorRaisesAnOAuthException() {
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                Reply.json(
                        400,
                        """
                        {"error": "invalid_scope",
                         "error_description": "The requested scope is not granted."}
                        """)
                    .withHeader("x-request-id", "req_token_9"));

    TeachMeHipaaException error = retrieveFailing(api);

    assertInstanceOf(OAuthException.class, error);
    assertEquals(400, error.getStatusCode());
    assertEquals("invalid_scope", error.getCode());
    assertEquals("The requested scope is not granted.", error.getDetail());
    assertEquals("req_token_9", error.getRequestId());
  }

  @Test
  void anHtmlPageFromTheTokenEndpointRaisesAnApiException() {
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                new Reply(200, Map.of("content-type", "text/html"), "<html>wrong host</html>"));

    TeachMeHipaaException error = retrieveFailing(api);

    assertSame(ApiException.class, error.getClass());
    assertEquals(200, error.getStatusCode());
  }
}
