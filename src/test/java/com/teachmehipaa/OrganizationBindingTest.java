package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.learnerRetrieveApi;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Learner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OrganizationBindingTest {
  @Test
  void discoversTheOrganizationOnceAndReusesIt() {
    FakeTransport api = learnerRetrieveApi();
    TeachMeHipaaClient client = clientFor(api).build();

    client.learners().retrieve(LEARNER_ID);
    client.learners().retrieve(LEARNER_ID);

    List<TransportRequest> introspections = api.requestsTo(INTROSPECTION_ROUTE);
    assertEquals(1, introspections.size());
    assertEquals("Bearer " + ACCESS_TOKEN, introspections.get(0).headers().get("authorization"));
    assertEquals(2, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void anExplicitOrganizationIdSkipsDiscovery() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on("GET /v1/organizations/org_given/learners/" + LEARNER_ID, Reply.json(LEARNER_JSON));

    Learner learner =
        clientFor(api).organizationId("org_given").build().learners().retrieve(LEARNER_ID);

    assertEquals(LEARNER_ID, learner.getLearnerId());
    assertEquals(List.of(), api.requestsTo(INTROSPECTION_ROUTE));
  }

  @Test
  void concurrentCallsDiscoverTheOrganizationOnce() throws Exception {
    AtomicInteger introspections = new AtomicInteger();
    CountDownLatch secondIntrospection = new CountDownLatch(1);
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON))
            .on(
                INTROSPECTION_ROUTE,
                request -> {
                  if (introspections.incrementAndGet() > 1) {
                    secondIntrospection.countDown();
                  }
                  await(secondIntrospection);
                  return Reply.json(introspectionJson("\"org_acme\""));
                });
    TeachMeHipaaClient client = clientFor(api).build();
    ExecutorService callers = Executors.newFixedThreadPool(4);

    try {
      List<Future<?>> calls = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        calls.add(callers.submit(() -> client.learners().retrieve(LEARNER_ID)));
      }
      for (Future<?> call : calls) {
        call.get(5, TimeUnit.SECONDS);
      }
    } finally {
      callers.shutdownNow();
    }

    assertEquals(1, api.requestsTo(INTROSPECTION_ROUTE).size());
    assertEquals(4, api.requestsTo(LEARNER_ROUTE).size());
  }

  @Test
  void aTokenBoundToNoOrganizationRaises() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
            .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("null")));
    TeachMeHipaaClient client = clientFor(api).build();

    TeachMeHipaaException error =
        assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertTrue(error.getMessage().contains("not bound to an Organization"), error.getMessage());
    assertEquals(List.of(), api.requestsTo(LEARNER_ROUTE));
  }

  @Test
  void aFailedDiscoveryIsRetriedOnTheNextCall() {
    FakeTransport api =
        learnerRetrieveApi()
            .replace(
                INTROSPECTION_ROUTE,
                Reply.problem(
                    503,
                    """
                    {"title": "Service Unavailable", "status": 503,
                     "code": "temporarily_unavailable"}
                    """))
            .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")));
    TeachMeHipaaClient client = clientFor(api).maxRetries(0).build();

    assertThrows(TeachMeHipaaException.class, () -> client.learners().retrieve(LEARNER_ID));
    Learner learner = client.learners().retrieve(LEARNER_ID);

    assertEquals(LEARNER_ID, learner.getLearnerId());
    assertEquals(2, api.requestsTo(INTROSPECTION_ROUTE).size());
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await(300, TimeUnit.MILLISECONDS);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
