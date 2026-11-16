# Webhooks

A **Webhook subscription** makes TeachMeHIPAA POST the Events you select,
such as `learner.certified`, to an HTTPS endpoint you run. Each POST is a
**Delivery**. Every Delivery is signed with the subscription's **Signing
secret**, and a Delivery your endpoint does not acknowledge with a 2xx is
sent again until it succeeds or is dead-lettered.

Samples use a `client` built as in [Getting started](getting-started.md).
Managing subscriptions requires the `webhooks:manage` Scope.

## Creating a subscription

<!-- sample: webhook-subscription-create -->
```java
import com.teachmehipaa.model.EventType;
import com.teachmehipaa.model.WebhookSubscriptionCreated;
import com.teachmehipaa.param.WebhookSubscriptionCreateParams;
import java.util.List;

WebhookSubscriptionCreated subscription =
    client
        .webhookSubscriptions()
        .create(
            WebhookSubscriptionCreateParams.builder()
                .endpointUrl("https://hr.example.com/webhooks/tmh")
                .eventTypes(
                    List.of(
                        EventType.LEARNER_CERTIFIED,
                        EventType.LEARNER_EXPIRING,
                        EventType.INVITE_BOUNCED))
                .description("HR sync")
                .build());
System.out.println(subscription.getSubscriptionId() + " " + subscription.getSecretKeyId());
database.saveSigningSecret(subscription.getSecret());
```

`WebhookSubscriptionCreated.getSecret()` returns the Signing secret in this
response and never again. Write it to your secret store before doing
anything else; `database` stands for that store here.

`webhookSubscriptions()` also has `list`, `retrieve`, `update` and `delete`.
`update` requires the subscription's ETag (see
[Optimistic concurrency](optimistic-concurrency.md)); `enabled(false)` pauses
Deliveries without deleting the subscription.

## Verifying a Delivery

`Webhook.constructEvent` checks a Delivery's signature and returns a
`WebhookDelivery`: either `WebhookDelivery.EventDelivery`, holding the
`Event`, or `WebhookDelivery.TestDeliveryReceived`. The method is static and
needs no client or API credentials. Code that already holds a client can call
the same methods on `client.webhooks()`.

It takes four arguments, three of which must be exactly what arrived:

| Argument | Requirement |
| --- | --- |
| Body | The raw bytes, read before any framework parses them. Re-serialized JSON never verifies. |
| Headers | A `Map<String, String>`, or a lookup such as `HttpServletRequest::getHeader` or Spring's `HttpHeaders::getFirst` |
| Signing secret | The subscription's secret, from your store |
| Path | The request path including any query string, or the full URL. The signature covers it, so a dropped context path or query string fails every Delivery. |

A Delivery signed more than 5 minutes before or after your server's clock is
rejected, so keep the clock synchronized with NTP. Verification failures throw
`WebhookSignatureException`; its `getCode()` names the check that failed:

| `getCode()` | Constant | Cause |
| --- | --- | --- |
| `missing_header` | `MISSING_HEADER` | A signature header is absent |
| `invalid_timestamp` | `INVALID_TIMESTAMP` | `x-tmh-timestamp` is not an ISO 8601 date-time |
| `body_hash_mismatch` | `BODY_HASH_MISMATCH` | The body differs from the signed body, typically because something parsed it |
| `signature_mismatch` | `SIGNATURE_MISMATCH` | Wrong secret, wrong path, or a forged Delivery |
| `timestamp_outside_tolerance` | `TIMESTAMP_OUTSIDE_TOLERANCE` | Signed more than 5 minutes from now: a replay, or clock skew |

Answer a `WebhookSignatureException` with 400 and take no other action. The
exception carries neither body nor headers, which hold Learner details, so it
is safe to log.

### Spring MVC

Declare the body as `@RequestBody byte[]` so no message converter touches it,
and rebuild the path from the request URI and query string:

<!-- sample: webhook-spring -->
```java
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.exception.WebhookSignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TmhWebhookController {
  private final String signingSecret = System.getenv("TMH_WEBHOOK_SECRET");

  @PostMapping("/webhooks/tmh")
  public ResponseEntity<String> receive(
      @RequestBody byte[] body, @RequestHeader HttpHeaders headers, HttpServletRequest request) {
    String query = request.getQueryString();
    String path = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    try {
      WebhookDelivery delivery =
          Webhook.constructEvent(body, headers::getFirst, signingSecret, path);
      if (delivery instanceof WebhookDelivery.EventDelivery received) {
        return ResponseEntity.ok("received " + received.event().getEventType());
      }
      return ResponseEntity.ok("test received");
    } catch (WebhookSignatureException rejected) {
      return ResponseEntity.badRequest().body(rejected.getCode());
    }
  }
}
```

`getRequestURI()` includes the servlet context path. The Delivery was signed
for the full path, so keep it.

### Jakarta Servlet

Read `getInputStream()` before any filter or parameter lookup consumes the
body:

<!-- sample: webhook-servlet -->
```java
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.exception.WebhookSignatureException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/webhooks/tmh")
public class TmhWebhookServlet extends HttpServlet {
  private final String signingSecret = System.getenv("TMH_WEBHOOK_SECRET");

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    byte[] body = request.getInputStream().readAllBytes();
    String query = request.getQueryString();
    String path = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    response.setContentType("text/plain");
    try {
      WebhookDelivery delivery =
          Webhook.constructEvent(body, request::getHeader, signingSecret, path);
      if (delivery instanceof WebhookDelivery.EventDelivery received) {
        response.getWriter().write("received " + received.event().getEventType());
      } else {
        response.getWriter().write("test received");
      }
    } catch (WebhookSignatureException rejected) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(rejected.getCode());
    }
  }
}
```

### `com.sun.net.httpserver`

The JDK's built-in HTTP server needs no dependency beyond the library.
Register the handler with
`server.createContext("/webhooks/tmh", new TmhWebhookHandler())`:

<!-- sample: webhook-jdk-server -->
```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.exception.WebhookSignatureException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class TmhWebhookHandler implements HttpHandler {
  private final String signingSecret = System.getenv("TMH_WEBHOOK_SECRET");

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    byte[] body = exchange.getRequestBody().readAllBytes();
    URI target = exchange.getRequestURI();
    String path =
        target.getRawQuery() == null
            ? target.getRawPath()
            : target.getRawPath() + "?" + target.getRawQuery();
    int status = 200;
    String answer;
    try {
      WebhookDelivery delivery =
          Webhook.constructEvent(
              body, exchange.getRequestHeaders()::getFirst, signingSecret, path);
      answer =
          delivery instanceof WebhookDelivery.EventDelivery received
              ? "received " + received.event().getEventType()
              : "test received";
    } catch (WebhookSignatureException rejected) {
      status = 400;
      answer = rejected.getCode();
    }
    byte[] bytes = answer.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }
}
```

[`WebhookServer.java`](../../examples/WebhookServer.java) runs this handler
as a standalone program.

## Processing Events

| Rule | Reason |
| --- | --- |
| Return 2xx as soon as the Delivery verifies; queue slow work | A Delivery that times out is delivered again |
| Deduplicate on `getEventId()` | A redelivered Event keeps its id |
| Ignore Event types you don't handle | New types arrive as `UnknownEvent` without a library upgrade |
| Order by `getOccurredAt()`, or re-read state from the API | A retried Delivery can arrive after a later one |

`database` stands for your record of processed Event ids:

<!-- sample: webhook-dedupe -->
```java
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.model.Event;
import java.util.function.Function;

void handleDelivery(byte[] body, Function<String, String> headers, String path) {
  WebhookDelivery delivery =
      Webhook.constructEvent(body, headers, System.getenv("TMH_WEBHOOK_SECRET"), path);
  if (!(delivery instanceof WebhookDelivery.EventDelivery received)) {
    return;
  }
  Event event = received.event();
  if (database.isProcessed(event.getEventId())) {
    return;
  }
  System.out.println("handling " + event.getEventType() + " " + event.getEventId());
  database.markProcessed(event.getEventId());
}
```

After an outage, `webhookSubscriptions().redrive` sends the subscription's
failed Events again, and the [Event log](pagination.md#the-event-log) holds
every Event of the last 30 days.

## Test deliveries

`webhookSubscriptions().test` sends a signed **Test delivery** to the
endpoint, which exercises your verifier end to end. It returns a
`WebhookDeliveryOperation`. After `waitForCompletion()`,
`getDeliveryResult()` reports the outcome:

<!-- sample: webhook-delivery-operation -->
```java
import com.teachmehipaa.model.WebhookDeliveryOperation;
import com.teachmehipaa.model.WebhookDeliveryResult;

WebhookDeliveryOperation delivery =
    client.webhookSubscriptions().test("whs_01JD8D").waitForCompletion();
WebhookDeliveryResult result = delivery.getDeliveryResult();
if (result instanceof WebhookDeliveryResult.Sent sent) {
  System.out.println("endpoint answered " + sent.responseStatus());
} else if (result instanceof WebhookDeliveryResult.Failed failed) {
  System.out.println("delivery failed: " + failed.reason());
} else {
  System.out.println("delivery ended as " + result.tag());
}
```

`WebhookDeliveryResult` is a sealed interface: `Sent`, `Failed`, `Settled`
(the Delivery had already ended) and `Unknown` for an outcome this version
does not recognize. `redrive` returns the same type. A Test delivery carries
no Event, so `constructEvent` returns it as `TestDeliveryReceived`, whose
`testDelivery()` holds the subscription id.

## Signing Deliveries in tests

`Webhook.generateTestHeaders` produces the headers TeachMeHIPAA would send
for a payload and path. Use it to drive a handler in a unit test without a
real Delivery. Sign the exact bytes and path the test sends:

<!-- sample: webhook-test-headers -->
```java
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.model.EventLearnerCertified;
import java.util.Map;

String payload =
    """
    {"eventId": "evt_01JD8M", "eventType": "learner.certified", "orgId": "org_acme",
     "occurredAt": "2026-11-15T12:00:00.000Z",
     "data": {"learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG", "email": "ana@example.com",
              "externalId": "hr-123", "courseId": "hipaa-2026", "status": "certified",
              "occurredAt": "2026-11-15T12:00:00.000Z"}}
    """;
Map<String, String> headers =
    Webhook.generateTestHeaders(payload, "test-signing-secret", "/webhooks/tmh");

WebhookDelivery delivery =
    Webhook.constructEvent(payload, headers, "test-signing-secret", "/webhooks/tmh");
if (delivery instanceof WebhookDelivery.EventDelivery received
    && received.event() instanceof EventLearnerCertified certified) {
  System.out.println("certified " + certified.getData().getLearnerId());
}
```

`Webhook.TestHeaderOptions` signs as of another instant, or sets the Event
id, Event type and key id headers. `Webhook.VerifyOptions` changes the
tolerance, or verifies a Delivery your queue stored against the time it
arrived instead of now:

<!-- sample: webhook-verify-options -->
```java
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

String payload =
    """
    {"eventId": "evt_01JD8M", "eventType": "learner.certified", "orgId": "org_acme",
     "occurredAt": "2026-11-15T12:00:00.000Z",
     "data": {"learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG", "email": "ana@example.com",
              "externalId": "hr-123", "courseId": "hipaa-2026", "status": "certified",
              "occurredAt": "2026-11-15T12:00:00.000Z"}}
    """;
// Recorded by your queue when the Delivery arrived; verified later, from the queue.
Instant receivedAt = Instant.parse("2026-11-15T12:00:02Z");
Map<String, String> headers =
    Webhook.generateTestHeaders(
        payload,
        "test-signing-secret",
        "/webhooks/tmh",
        Webhook.TestHeaderOptions.builder()
            .timestamp(Instant.parse("2026-11-15T12:00:00Z"))
            .build());

WebhookDelivery delivery =
    Webhook.constructEvent(
        payload,
        headers,
        "test-signing-secret",
        "/webhooks/tmh",
        Webhook.VerifyOptions.builder()
            .receivedAt(receivedAt)
            .tolerance(Duration.ofMinutes(1))
            .build());
System.out.println("verified " + (delivery instanceof WebhookDelivery.EventDelivery));
```

[Testing](testing.md#test-your-webhook-handler) puts this in a JUnit test.

## Signature scheme

`constructEvent` implements this; the details help when a verification fails.

| Header | Value |
| --- | --- |
| `x-tmh-signature` | Hex HMAC-SHA256 of the canonical string |
| `x-tmh-timestamp` | Signing time, ISO 8601 |
| `x-tmh-body-hash` | Hex SHA-256 of the body |
| `x-tmh-event-id` | The Event id; a UUID for a Test delivery |
| `x-tmh-event-type` | The Event type |
| `x-tmh-key-id` | The id of the Signing secret that signed it |

The canonical string joins four lines with `\n`: `POST`, the path with its
query string, the timestamp and the body hash. The HMAC key is the Signing
secret's UTF-8 bytes, used as is (not base64-decoded).
