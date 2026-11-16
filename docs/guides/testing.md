# Testing

## Use the Sandbox

The **Sandbox** Environment is a separate TeachMeHIPAA deployment for
development and tests. Learners there are never emailed, so you can create,
invite and offboard freely. Create a separate API client for it in the
console; Live credentials never work in the Sandbox, or the other way round.

<!-- sample: testing-sandbox -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.TokenIntrospection;
import com.teachmehipaa.model.TokenIntrospectionMode;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .build();

TokenIntrospection token = client.auth().introspect();
if (!TokenIntrospectionMode.TEST.equals(token.getMode())) {
  throw new IllegalStateException("these tests must run against the Sandbox");
}
System.out.println(token.getMode());
```

`auth().introspect()` reports the mode `test` for a Sandbox token, which makes
a cheap check that a test suite isn't pointed at Live.

## Fake the transport in unit tests

The client sends every request, the token request included, through an
`HttpTransport`: one method that takes the `TransportRequest` the library
built and returns a `TransportResponse`. Pass your own as `httpTransport` to
test your code without the network, and to assert on what it sent. This
JUnit 5 test fakes the Compliance summary:

<!-- sample: testing-fake-transport -->
```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.HttpTransport;
import com.teachmehipaa.core.StaticTokenProvider;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.core.TransportResponse;
import com.teachmehipaa.model.ComplianceSummary;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ComplianceReportTest {
  private final List<TransportRequest> sent = new ArrayList<>();

  private final HttpTransport api =
      request -> {
        sent.add(request);
        if (request.uri().getPath().equals("/v1/organizations/org_test/compliance")) {
          return json(
              200,
              """
              {"asOf": "2026-11-15T12:00:00.000Z", "courseId": "hipaa-2026",
               "totals": {"learners": 2, "compliant": 1, "expiringSoon": 0, "expired": 0,
                          "inProgress": 1, "notStarted": 0, "inviteBounced": 0},
               "compliantRate": 0.5}
              """);
        }
        return json(404, "{\"title\": \"Not Found\", \"status\": 404, \"code\": \"not_found\"}");
      };

  private final TeachMeHipaaClient client =
      TeachMeHipaaClient.builder()
          .tokenProvider(new StaticTokenProvider("test-token"))
          .organizationId("org_test")
          .httpTransport(api)
          .maxRetries(0)
          .build();

  @Test
  void readsTheComplianceRate() {
    ComplianceSummary summary = client.compliance().retrieve();

    assertEquals(0.5, summary.getCompliantRate());
    assertEquals("Bearer test-token", sent.get(0).headers().get("authorization"));
  }

  private static TransportResponse json(int status, String body) {
    HttpHeaders headers =
        HttpHeaders.of(Map.of("content-type", List.of("application/json")), (name, value) -> true);
    return new TransportResponse(status, headers, body.getBytes(StandardCharsets.UTF_8));
  }
}
```

Tips for tests:

- A **`StaticTokenProvider`**, as above, keeps token requests out of your
  fakes.
- Pass **`organizationId`** so the client doesn't introspect the token to find
  the Organization.
- Set **`maxRetries(0)`** to see a scripted failure at once, instead of after
  the client's retries and backoff.
- A **`clock`** (a `java.time.Clock`) and a **`sleeper`** replace real time.
  A `Sleeper` that returns at once makes retry backoff and
  `waitForCompletion` polling instant.
- The transport returns error statuses as responses; throw an `IOException`
  only to simulate a network failure, which the client raises as
  `ApiConnectionException`.
- `TransportRequest` headers are keyed by lower-case name, and `body()` is
  the exact bytes sent, or `null`.

The transport also suits a proxy or mTLS: wrap a configured `HttpClient` in a
`JdkHttpTransport` (see [Configuration](configuration.md#custom-http-client)).

## Run against a mock server

To check your requests against the Developer API's OpenAPI document, point
the client at a mock server, such as
[Prism](https://stoplight.io/open-source/prism), with `baseUrl`. Mock servers
usually can't issue Access tokens, so give the client a static token and its
Organization. Prism serves the document's paths without the `/v1` prefix, so
the base URL is the bare host:

<!-- sample: testing-mock-server -->
```java
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.StaticTokenProvider;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerListParams;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .baseUrl("http://127.0.0.1:4010")
        .tokenProvider(new StaticTokenProvider("mock-token"))
        .organizationId("org_acme")
        .build();

Page<Learner> page = client.learners().list(LearnerListParams.builder().limit(5).build());
System.out.println(page.items().size());
```

## Test your webhook handler

`Webhook.generateTestHeaders` signs a payload the way TeachMeHIPAA does. Use
it to send your handler a Delivery it should accept, and tampered ones it
should refuse. Here `TmhWebhook.handle` stands in for your handler, such as
one from [Webhooks](webhooks.md#verify-every-delivery):

<!-- sample: testing-webhook-handler -->
```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.teachmehipaa.Webhook;
import com.teachmehipaa.exception.WebhookSignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TmhWebhookTest {
  private static final String SECRET = "test-signing-secret";

  /** Your handler: verifies the Delivery and answers with an HTTP status. */
  static int handle(byte[] body, Map<String, String> headers, String path) {
    try {
      Webhook.constructEvent(body, headers, SECRET, path);
      return 200;
    } catch (WebhookSignatureException rejected) {
      return 400;
    }
  }

  @Test
  void acceptsASignedDeliveryAndRefusesTamperedOnes() {
    byte[] payload =
        "{\"subscriptionId\": \"whs_01JD8Q\", \"test\": true}".getBytes(StandardCharsets.UTF_8);
    Map<String, String> headers = Webhook.generateTestHeaders(payload, SECRET, "/webhooks/tmh");
    byte[] tampered =
        "{\"subscriptionId\": \"whs_01JD8Q\", \"test\": false}".getBytes(StandardCharsets.UTF_8);

    assertEquals(200, handle(payload, headers, "/webhooks/tmh"));
    assertEquals(400, handle(tampered, headers, "/webhooks/tmh"));
    assertEquals(400, handle(payload, headers, "/webhooks/other"));
  }
}
```

Sign exactly the body and path you send: a different path, or a body that
differs by one byte, fails verification just as it would in production. Pass
`Webhook.TestHeaderOptions` with a `timestamp` to sign as of another time,
such as to test that your handler rejects stale Deliveries.
