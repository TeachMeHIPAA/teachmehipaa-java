package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.REQUEST_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.learnerRetrieveApi;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.exception.TeachMeHipaaException;
import java.net.ConnectException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ErrorsTest {
  private static TeachMeHipaaException retrieveFailing(FakeTransport api) {
    TeachMeHipaaClient client = clientFor(api).build();
    return assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));
  }

  @Test
  void aProblemResponseRaisesWithItsFields() {
    FakeTransport api =
        learnerRetrieveApi()
            .replace(
                LEARNER_ROUTE,
                Reply.problem(
                        404,
                        """
                        {"title": "Not Found", "status": 404, "code": "not_found",
                         "detail": "No such learner."}
                        """)
                    .withHeader("x-request-id", REQUEST_ID));

    TeachMeHipaaException error = retrieveFailing(api);

    assertEquals(404, error.getStatusCode());
    assertEquals("not_found", error.getCode());
    assertEquals("Not Found", error.getTitle());
    assertEquals("No such learner.", error.getDetail());
    assertEquals(REQUEST_ID, error.getRequestId());
    assertEquals(Optional.of(REQUEST_ID), error.getHeaders().firstValue("X-Request-Id"));
    assertEquals("Not Found: No such learner. (request id: req_7f3c2a)", error.getMessage());
  }

  @Test
  void anUnknownProblemCodeComesThroughAsIs() {
    FakeTransport api =
        learnerRetrieveApi()
            .replace(
                LEARNER_ROUTE,
                Reply.problem(
                    409,
                    """
                    {"title": "Conflict", "status": 409, "code": "learner_on_sabbatical"}
                    """));

    TeachMeHipaaException error = retrieveFailing(api);

    assertEquals(409, error.getStatusCode());
    assertEquals("learner_on_sabbatical", error.getCode());
    assertEquals("Conflict", error.getMessage());
  }

  @Test
  void anHtmlPageIsRaisedInsteadOfParsed() {
    FakeTransport api =
        learnerRetrieveApi()
            .replace(
                LEARNER_ROUTE,
                new Reply(200, Map.of("content-type", "text/html"), "<!doctype html><html>"));

    TeachMeHipaaException error = retrieveFailing(api);

    assertEquals(200, error.getStatusCode());
    assertNull(error.getCode());
    assertEquals("<!doctype html><html>", error.getRawBody());
    assertEquals(
        "Expected a JSON response but got text/html with HTTP 200; check the base URL",
        error.getMessage());
  }

  @Test
  void aNonProblemErrorBodyStillRaisesWithItsStatus() {
    FakeTransport api =
        learnerRetrieveApi()
            .replace(
                LEARNER_ROUTE,
                Reply.json(
                    503,
                    """
                    {"statusCode": 503, "statusMessage": "Service Unavailable"}
                    """));

    TeachMeHipaaException error = retrieveFailing(api);

    assertEquals(503, error.getStatusCode());
    assertEquals("TeachMeHIPAA API responded with HTTP 503", error.getMessage());
  }

  @Test
  void noResponseRaisesWithoutAStatus() {
    ConnectException refused = new ConnectException("Connection refused");
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")))
            .fail(LEARNER_ROUTE, refused);

    TeachMeHipaaException error = retrieveFailing(api);

    assertNull(error.getStatusCode());
    assertEquals(refused, error.getCause());
    assertTrue(error.getMessage().contains("api.tme.so"), error.getMessage());
    assertFalse(error.getMessage().contains(LEARNER_ID), error.getMessage());
  }
}
