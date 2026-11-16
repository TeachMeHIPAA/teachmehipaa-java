# Webhooks

A **Webhook subscription** tells TeachMeHIPAA to POST Events of the types you
choose, such as `learner.certified`, to your HTTPS endpoint. Each POST is a
**Delivery**, signed with the subscription's **Signing secret** so you can
prove it came from TeachMeHIPAA. Failed Deliveries are retried until they
succeed or are dead-lettered.

Samples use a `client` built as in [Getting started](getting-started.md).

## Create a Webhook subscription

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

The response holds the Signing secret in `getSecret()` **only once**: store it
right away, in the place your handler reads it from (here `database` stands
in for your secret store). Managing subscriptions needs the `webhooks:manage`
Scope.

`webhookSubscriptions().list`, `retrieve`, `update` and `delete` manage
subscriptions; `update` needs the subscription's ETag (see
[Conditional requests](conditional-requests.md)), and `enabled(false)` pauses
Deliveries.

## Verify every Delivery

`Webhook.constructEvent` verifies a Delivery and returns a `WebhookDelivery`:
an `EventDelivery` holding the Event, or a `TestDeliveryReceived`. It needs
four things, three of them exactly as they arrived:

1. **The raw body.** Parsed and re-serialized JSON never verifies, so read
   the body as bytes before anything parses it.
2. **The headers.** A `Map`, or a lookup such as a servlet's
   `request::getHeader` or Spring's `HttpHeaders::getFirst`.
3. **The Signing secret** of the subscription.
4. **The request path, with its query string, or the full URL.** The
   signature covers the path the Delivery was sent to, so a verifier that
   drops the path, a context path or the query string rejects every
   Delivery.

Deliveries signed more than 5 minutes from your clock, in either direction,
are rejected, so keep your server's clock in sync. A failed check throws
`WebhookSignatureException`, whose `getCode()` says why:

| `getCode()`                   | Meaning                                                      |
| ----------------------------- | ------------------------------------------------------------ |
| `missing_header`              | A signature header is missing                                |
| `invalid_timestamp`           | `x-tmh-timestamp` isn't an ISO 8601 date-time                |
| `body_hash_mismatch`          | The body isn't the one that was signed (it was parsed?)      |
| `signature_mismatch`          | Wrong secret, wrong path, or a forged Delivery               |
| `timestamp_outside_tolerance` | Signed more than 5 minutes from now: a replay, or clock skew |

`WebhookSignatureException` has a constant for each, such as
`WebhookSignatureException.SIGNATURE_MISMATCH`. Answer it with 400 and do
nothing else. The exception never carries the body or headers, which hold
Learner details, so it is safe to log.

`Webhook` needs only the Signing secret, never API credentials, so a handler
doesn't need a client. Code that already holds one can call the same methods
on `client.webhooks()`.

### Spring Boot

Take the body as `@RequestBody byte[]` so Spring hands it over untouched, and
rebuild the path from the request URI and query string:

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

`getRequestURI()` includes the servlet context path, which is part of the
path the Delivery was sent to, so keep it.

### Servlets

With a plain servlet, read `request.getInputStream()` before anything else
touches the body:

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

### The JDK's HTTP server

`com.sun.net.httpserver` ships with the JDK, so this handler needs nothing
but the library. Register it with
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

The [webhook server example](../../examples/WebhookServer.java) runs this
handler as a whole program.

## Handle Events

- **Answer fast.** Return a 2xx as soon as the Delivery verifies, and do slow
  work afterwards, such as from a queue. A Delivery that times out or fails is
  delivered again.
- **Expect duplicates.** A retried Delivery carries the same Event, with the
  same `getEventId()`. Record the ids you've handled and skip repeats.
- **Expect new Event types.** Event types are added over time. One this
  version doesn't know arrives as an `UnknownEvent`, with its
  `getEventType()` and `getData()` as sent, so your handler keeps working;
  ignore types you don't use.
- **Don't rely on order.** A retried Delivery can arrive after a later one.
  Compare `getOccurredAt()`, or read the current state with the API.

Here `database` stands in for your own store of handled ids:

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

If your endpoint was down, `webhookSubscriptions().redrive` delivers the
subscription's failed Events again, and the
[Event log](pagination-and-events.md#the-event-log) has every Event of the
last 30 days.

## Test deliveries

`webhookSubscriptions().test` sends a signed **Test delivery** to the
subscription's endpoint, so you can check your verifier end to end. It
returns a `WebhookDeliveryOperation`; wait on it, then
`getDeliveryResult()` says how the Delivery ended:

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

The result is `Sent`, `Failed` or `Settled` (the Delivery had already ended).
Kinds this version doesn't know arrive as `WebhookDeliveryResult.Unknown`, so
keep the fallback branch. `redrive` returns the same type.

A Test delivery carries no Event: `constructEvent` returns it as a
`TestDeliveryReceived`, whose `testDelivery()` holds the subscription id.

## Sign Deliveries in your own tests

`Webhook.generateTestHeaders` returns the headers TeachMeHIPAA would send with
a payload, so you can test your handler without a real Delivery. Sign the
exact body and path you'll send, and match an `EventDelivery`'s Event against
the `Event…` types to read its data:

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

`Webhook.TestHeaderOptions` signs as of another time, or sets the Event id,
Event type and key id headers. `Webhook.VerifyOptions` allows a different
tolerance, or checks a Delivery your queue stored against when it arrived
rather than now:

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

[Testing](testing.md#test-your-webhook-handler) shows this in a handler test.

## Signature details

You don't need these to use `constructEvent`, but they help when debugging.
Each Delivery carries:

| Header             | Holds                                       |
| ------------------ | ------------------------------------------- |
| `x-tmh-signature`  | Hex HMAC-SHA256 of the canonical string     |
| `x-tmh-timestamp`  | When it was signed, ISO 8601                |
| `x-tmh-body-hash`  | Hex SHA-256 of the body                     |
| `x-tmh-event-id`   | The Event's id (a UUID for a Test delivery) |
| `x-tmh-event-type` | The Event type                              |
| `x-tmh-key-id`     | Which Signing secret signed it              |

The canonical string is `POST`, the path with its query string, the
timestamp and the body hash, joined by newlines. The HMAC key is the Signing
secret's UTF-8 bytes, used as is (not base64-decoded).
