package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.NotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmptyResponseTest {
  private static final String SUBSCRIPTION_ID = "whs_01JD8Z4N3QX5V7W9Y2B4C6D8EJ";
  private static final String DELETE_ROUTE =
      "DELETE /v1/organizations/org_acme/webhook-subscriptions/" + SUBSCRIPTION_ID;

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  @Test
  void aDeleteAnsweredWith204ReturnsNormally() {
    FakeTransport api = api().on(DELETE_ROUTE, Reply.empty(204));

    clientOn(api).webhookSubscriptions().delete(SUBSCRIPTION_ID);

    TransportRequest request = api.requestsTo(DELETE_ROUTE).get(0);
    assertNull(request.body());
    assertFalse(request.headers().containsKey("content-type"));
    assertFalse(request.headers().containsKey("idempotency-key"));
  }

  @Test
  void aDeleteIsNotRetried() {
    FakeTransport api =
        api()
            .on(
                DELETE_ROUTE,
                Reply.problem(
                    503,
                    """
                    {"title": "Service Unavailable", "status": 503,
                     "code": "temporarily_unavailable"}
                    """))
            .on(DELETE_ROUTE, Reply.empty(204));
    TeachMeHipaaClient client = clientOn(api);

    assertThrows(ApiException.class, () -> client.webhookSubscriptions().delete(SUBSCRIPTION_ID));

    assertEquals(1, api.requestsTo(DELETE_ROUTE).size());
  }

  @Test
  void aDeleteOfAnUnknownSubscriptionRaisesNotFound() {
    FakeTransport api =
        api()
            .on(
                DELETE_ROUTE,
                Reply.problem(
                    404,
                    """
                    {"title": "Not Found", "status": 404, "code": "not_found"}
                    """));
    TeachMeHipaaClient client = clientOn(api);

    assertThrows(
        NotFoundException.class, () -> client.webhookSubscriptions().delete(SUBSCRIPTION_ID));
  }

  @Test
  void anHtmlPageInsteadOfAnEmptyBodyRaisesAnApiException() {
    FakeTransport api =
        api().on(DELETE_ROUTE, new Reply(200, Map.of("content-type", "text/html"), "<html>"));
    TeachMeHipaaClient client = clientOn(api);

    ApiException error =
        assertThrows(
            ApiException.class, () -> client.webhookSubscriptions().delete(SUBSCRIPTION_ID));

    assertEquals(200, error.getStatusCode());
  }
}
