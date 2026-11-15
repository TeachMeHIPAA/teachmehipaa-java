package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.CLIENT_ID;
import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.learnerRetrieveApi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.TransportRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ClientConfigurationTest {
  private static Set<String> originsCalled(FakeTransport api) {
    return api.requests().stream()
        .map(request -> request.uri().getScheme() + "://" + request.uri().getAuthority())
        .collect(Collectors.toSet());
  }

  @Test
  void defaultsToTheLiveEnvironment() {
    FakeTransport api = learnerRetrieveApi();
    TeachMeHipaaClient client = clientFor(api).build();

    client.learners().retrieve(LEARNER_ID);

    assertEquals(Environment.LIVE, client.getEnvironment());
    assertEquals("https://api.tme.so/v1", client.getBaseUrl());
    assertEquals(Set.of("https://api.tme.so"), originsCalled(api));
  }

  @Test
  void theSandboxEnvironmentCallsTheSandboxDeployment() {
    FakeTransport api = learnerRetrieveApi();
    TeachMeHipaaClient client = clientFor(api).environment(Environment.SANDBOX).build();

    client.learners().retrieve(LEARNER_ID);

    assertEquals("https://api-teachmehipaa-dev.web.app/v1", client.getBaseUrl());
    assertEquals(Set.of("https://api-teachmehipaa-dev.web.app"), originsCalled(api));
    assertEquals("/v1/auth/token", api.requests().get(0).uri().getPath());
  }

  @Test
  void baseUrlOverridesTheEnvironment() {
    FakeTransport api = learnerRetrieveApi("");
    TeachMeHipaaClient client =
        clientFor(api).environment(Environment.SANDBOX).baseUrl("http://localhost:4010/").build();

    client.learners().retrieve(LEARNER_ID);

    assertEquals("http://localhost:4010", client.getBaseUrl());
    assertEquals(Set.of("http://localhost:4010"), originsCalled(api));
    assertEquals("/auth/token", api.requests().get(0).uri().getPath());
  }

  @Test
  void configurationIsFixedWhenTheClientIsBuilt() {
    List<String> scopes = new ArrayList<>(List.of("learners:read"));
    TeachMeHipaaClient.Builder builder =
        clientFor(learnerRetrieveApi()).scopes(scopes).maxRetries(1);
    TeachMeHipaaClient client = builder.build();

    scopes.add("learners:write");
    builder.maxRetries(5).environment(Environment.SANDBOX).organizationId("org_other");

    assertEquals(List.of("learners:read"), client.getScopes());
    assertThrows(UnsupportedOperationException.class, () -> client.getScopes().add("x"));
    assertEquals(1, client.getMaxRetries());
    assertEquals(Environment.LIVE, client.getEnvironment());
    assertNull(client.getOrganizationId());
  }

  @Test
  void exposesRetryAndTimeoutSettings() {
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder().clientId(CLIENT_ID).clientSecret(CLIENT_SECRET).build();
    TeachMeHipaaClient tuned =
        TeachMeHipaaClient.builder()
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .maxRetries(5)
            .timeout(Duration.ofMillis(12_500))
            .build();

    assertEquals(2, client.getMaxRetries());
    assertEquals(Duration.ofSeconds(80), client.getTimeout());
    assertEquals(5, tuned.getMaxRetries());
    assertEquals(Duration.ofMillis(12_500), tuned.getTimeout());
  }

  @Test
  void sendsTheClientTimeoutWithEveryRequest() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api).timeout(Duration.ofSeconds(7)).build().learners().retrieve(LEARNER_ID);

    for (TransportRequest request : api.requests()) {
      assertEquals(Duration.ofSeconds(7), request.timeout());
    }
  }

  @Test
  void requestOptionsOverrideTheTimeoutAndSetTheRequestId() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api)
        .build()
        .learners()
        .retrieve(
            LEARNER_ID,
            RequestOptions.builder()
                .requestId("acme-sync-42")
                .timeout(Duration.ofSeconds(3))
                .build());

    TransportRequest request = api.requestsTo(LEARNER_ROUTE).get(0);
    assertEquals("acme-sync-42", request.headers().get("x-request-id"));
    assertEquals(Duration.ofSeconds(3), request.timeout());
  }

  @Test
  void identifiesTheSdkAndAppInTheUserAgent() {
    FakeTransport api = learnerRetrieveApi();

    clientFor(api)
        .appInfo(AppInfo.of("AcmeHR", "4.2", "https://acme.example"))
        .build()
        .learners()
        .retrieve(LEARNER_ID);

    assertEquals(3, api.requests().size());
    for (TransportRequest request : api.requests()) {
      String agent = request.headers().get("user-agent");
      assertTrue(agent.startsWith("teachmehipaa-java/"), agent);
      assertTrue(agent.contains(" java/" + Runtime.version()), agent);
      assertTrue(agent.endsWith(" AcmeHR/4.2 (https://acme.example)"), agent);
    }
  }

  @Test
  void theClientIdAndSecretGoTogether() {
    assertThrows(
        IllegalStateException.class,
        () -> TeachMeHipaaClient.builder().clientId(CLIENT_ID).build());
    assertThrows(
        IllegalStateException.class,
        () -> TeachMeHipaaClient.builder().clientSecret(CLIENT_SECRET).build());
  }

  @Test
  void rejectsInvalidSettings() {
    TeachMeHipaaClient.Builder builder = TeachMeHipaaClient.builder();

    assertThrows(IllegalArgumentException.class, () -> builder.clientId(" "));
    assertThrows(IllegalArgumentException.class, () -> builder.maxRetries(-1));
    assertThrows(IllegalArgumentException.class, () -> builder.timeout(Duration.ZERO));
    assertThrows(IllegalArgumentException.class, () -> builder.baseUrl("/v1"));
  }
}
