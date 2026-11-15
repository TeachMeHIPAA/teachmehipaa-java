package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.param.LearnerRetrieveParams;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

class LoggingTest {
  private static final String LEARNER_PATH = "/v1/organizations/org_acme/learners/" + LEARNER_ID;

  @Test
  void logsMethodPathStatusLatencyAndRequestIdAndNothingElse() {
    FakeClock clock = new FakeClock();
    List<RequestLogEntry> entries = new CopyOnWriteArrayList<>();
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                request -> {
                  clock.advance(Duration.ofMillis(40));
                  return Reply.json(tokenJson("jwt-secret-value"));
                })
            .on(
                LEARNER_ROUTE,
                request -> {
                  clock.advance(Duration.ofMillis(120));
                  return Reply.json(LEARNER_JSON).withHeader("x-request-id", "req_77");
                });

    clientFor(api)
        .organizationId(ORG_ID)
        .clock(clock)
        .logger(entries::add)
        .build()
        .learners()
        .retrieve(LEARNER_ID, LearnerRetrieveParams.builder().courseId("hipaa-2026").build());

    assertEquals(
        List.of(
            new RequestLogEntry("POST", "/v1/auth/token", 200, Duration.ofMillis(40), null),
            new RequestLogEntry("GET", LEARNER_PATH, 200, Duration.ofMillis(120), "req_77")),
        entries);
    String logged = entries.toString();
    for (String secret :
        List.of(CLIENT_SECRET, "jwt-secret-value", "ana@example.com", "hipaa-2026")) {
      assertFalse(logged.contains(secret), secret);
    }
  }

  @Test
  void logsAFailedResponseWithItsStatus() {
    List<RequestLogEntry> entries = new CopyOnWriteArrayList<>();
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson("jwt")))
            .on(
                LEARNER_ROUTE,
                Reply.problem(
                        404,
                        """
                        {"title": "Not Found", "status": 404, "code": "not_found"}
                        """)
                    .withHeader("x-request-id", "req_404"));
    TeachMeHipaaClient client =
        clientFor(api).organizationId(ORG_ID).clock(new FakeClock()).logger(entries::add).build();

    assertThrows(NotFoundException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(
        new RequestLogEntry("GET", LEARNER_PATH, 404, Duration.ZERO, "req_404"), entries.get(1));
  }
}
