# Testing

Three levels of test fit an integration: unit tests against a fake
`HttpTransport`, contract tests against a mock server built from the OpenAPI
document, and end-to-end tests against the Sandbox.

## Use the Sandbox

The Sandbox is a separate TeachMeHIPAA deployment with its own API clients.
It accepts every call Live does and never emails a Learner. Live credentials
fail in the Sandbox and Sandbox credentials fail in Live.

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

`auth().introspect().getMode()` returns `TEST` for a Sandbox token. Checking
it in a test fixture stops a misconfigured suite before it touches Live data.

## Fake the transport in unit tests

Every request the client sends, the token request included, passes through
one `HttpTransport`. `send` receives a complete `TransportRequest` and
returns a `TransportResponse`. Because `HttpTransport` is a functional
interface, a lambda serves as a fake, and the requests it records are what
your code sent. This JUnit 5 test fakes the Compliance summary:

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

Builder settings that make unit tests deterministic:

| Setting | Effect |
| --- | --- |
| `tokenProvider(new StaticTokenProvider("test-token"))` | No token requests reach the fake |
| `organizationId("org_test")` | No Organization lookup reaches the fake |
| `maxRetries(0)` | A scripted failure surfaces on the first attempt |
| `clock(Clock)` | Fixes token expiry and Operation deadlines |
| `sleeper(Sleeper)` | A `Sleeper` that returns at once makes blocking backoff and `waitForCompletion` instant |
| `asyncSleeper(AsyncSleeper)` | `duration -> CompletableFuture.completedFuture(null)` does the same for [async calls](async.md) |

The fake returns 4xx and 5xx as ordinary responses; the client maps them to
exceptions. Throw `IOException` only to simulate a network failure, which the
client raises as `ApiConnectionException`. `TransportRequest.headers()` is
keyed by lower-case name, and `body()` holds the exact bytes sent, or `null`.

A fake that implements only `send` also serves the `…Async` methods: the
default `sendAsync` runs it on a library thread.

## Run against a mock server

To validate requests against the Developer API's OpenAPI document, point the
client at a mock server such as [Prism](https://stoplight.io/open-source/prism)
with `baseUrl`. Mock servers cannot mint Access tokens, so supply a static
token and the Organization. Prism serves the document's paths without the
`/v1` prefix, so the base URL is the bare origin:

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

This repository runs its own contract suite this way: `./gradlew contractTest`
starts Prism through `npx` and fails on any request that violates the
document.

## Test your webhook handler

`Webhook.generateTestHeaders` signs a payload as TeachMeHIPAA would. A test
sends the handler a correctly signed Delivery, then tampered ones it must
refuse. `TmhWebhookTest.handle` stands for your handler, such as one from
[Webhooks](webhooks.md#verifying-a-delivery):

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

Sign the exact body and path the test sends; one changed byte or a different
path fails verification as it would in production. Pass
`Webhook.TestHeaderOptions` with a `timestamp` to sign as of another instant
and check that your handler rejects stale Deliveries.
