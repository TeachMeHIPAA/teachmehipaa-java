# TeachMeHIPAA Java Library

The official Java library for the
[TeachMeHIPAA Developer API](https://api.tme.so/v1). It handles Access tokens,
the Organization binding, retries, idempotency keys, typed exceptions,
pagination, ETags, Operations and webhook verification, so your code only
calls methods.

> [!NOTE]
> The Developer API has not launched yet, and this library is not on Maven
> Central. Until it is, build it from a checkout with
> `./gradlew publishToMavenLocal` and add `mavenLocal()` to your repositories.

## Contents

- [Installation](#installation)
- [Quickstart](#quickstart)
- [The Organization binding](#the-organization-binding)
- [Public lookups without credentials](#public-lookups-without-credentials)
- [Creating Learners](#creating-learners)
- [Pagination](#pagination)
- [Exceptions](#exceptions)
- [Retries and idempotency](#retries-and-idempotency)
- [Per-call options](#per-call-options)
- [Conditional writes and reads](#conditional-writes-and-reads)
- [Operations](#operations)
- [Uploads and downloads](#uploads-and-downloads)
- [Webhooks](#webhooks)
- [Partner apps](#partner-apps)
- [Tailing the Event log](#tailing-the-event-log)
- [Configuration](#configuration)
- [Logging](#logging)
- [Requirements](#requirements)
- [Versioning](#versioning)
- [Support and security](#support-and-security)

## Installation

Gradle (Kotlin DSL):

```kotlin
dependencies {
  implementation("com.teachmehipaa:teachmehipaa-java:1.0.0")
}
```

Maven:

```xml
<dependency>
  <groupId>com.teachmehipaa</groupId>
  <artifactId>teachmehipaa-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

The library needs Java 17 or newer and depends only on Jackson
(`jackson-databind` and `jackson-datatype-jsr310`). It sends requests with the
JDK's own `java.net.http.HttpClient`.

## Quickstart

Create an API client in the TeachMeHIPAA console to get a client id
(`cli_…`) and secret (`tmh_cs_…`). Start in the Sandbox Environment, where no
real Learners are emailed and no real money moves.

<!-- sample: quickstart -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.param.LearnerListParams;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .build();

Learner learner = client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
System.out.println(learner.getName() + " " + learner.getStatus());

LearnerListParams inProgress =
    LearnerListParams.builder().status(LearnerStatus.IN_PROGRESS).limit(20).build();
for (Learner item : client.learners().list(inProgress).autoPagingIterable()) {
  System.out.println(item.getEmail());
}
```

The client exchanges your credentials for an Access token on the first call,
caches it and renews it 60 seconds before it expires. Concurrent calls share
one token request. If the API rejects a token mid-flight (401), the client
renews it once and replays the call; that replay doesn't count as a retry.

Build one client and share it: it is thread-safe, and its settings are fixed
once built. Services hang off it (`client.learners()`, `client.enrollments()`),
and methods take ids first and then a `…Params` object built with a builder.
Every object they return has `getLastResponse()`, with the HTTP
`statusCode()`, `headers()`, `requestId()` and `etag()`. To ask for fewer
Scopes than your API client holds, pass `.scopes("learners:read")`.

## The Organization binding

An API client belongs to one Organization, so you never pass an `orgId`. On
the first Organization-scoped call, the client asks `auth().introspect()`
which Organization the token is for, once, and remembers the answer. If you
already know the id, pass it and the client skips that lookup:

<!-- sample: organization -->
```java
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.ComplianceSummary;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .organizationId("org_acme")
        .build();

ComplianceSummary compliance = client.compliance().retrieve();
System.out.println(compliance.getCompliantRate() + " " + compliance.getLastResponse().requestId());
```

To act for another Organization, build another client. Partner-level calls
(`client.partners()`) are not Organization-scoped and take the `partnerId` as
their first argument.

## Public lookups without credentials

Public methods, `courses().list`, `courses().retrieve` and
`certificateVerifications().retrieve`, send no Access token at all. To call
only those, such as to check a Certificate someone showed you, build a client
without credentials:

<!-- sample: public-lookups -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.CertificateVerification;
import com.teachmehipaa.model.Course;

TeachMeHipaaClient client = TeachMeHipaaClient.builder().environment(Environment.SANDBOX).build();

CertificateVerification verification = client.certificateVerifications().retrieve("aB3dE9");
System.out.println(verification.getStatus() + " " + verification.getLearnerName());

for (Course course : client.courses().list().autoPagingIterable()) {
  System.out.println(course.getTitle());
}
```

Any other method on such a client throws `IllegalStateException` before it
sends anything.

## Creating Learners

`create` and `upsertByExternalId` say whether they created the Learner in
`isCreated()`. `upsertByExternalId` is safe to replay from a sync job: it
creates and invites the Learner the first time and returns them every time
after.

Samples from here on assume a `client` built as in the
[Quickstart](#quickstart).

<!-- sample: learner-writes -->
```java
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerUpsertByExternalIdParams;

LearnerWriteResult created =
    client
        .learners()
        .create(
            LearnerCreateParams.builder()
                .email("ana@example.com")
                .name("Ana Example")
                .courseId("hipaa-2026")
                .build());
System.out.println(created.isCreated() ? "invited" : "already enrolled");

LearnerWriteResult synced =
    client
        .learners()
        .upsertByExternalId(
            "hr-123",
            LearnerUpsertByExternalIdParams.builder()
                .email("ana@example.com")
                .name("Ana Example")
                .build());
System.out.println(synced.getLearnerId());
```

## Pagination

A list method returns one `Page`, with `items()` and `nextCursor()`.
`autoPagingIterable()` yields every item across all pages, fetching the next
page only when the loop reaches it, so a `break` stops fetching.
`autoPagingStream()` is the same as a lazy `Stream`: `findFirst`, `limit` and
`anyMatch` stop fetching too.

<!-- sample: pagination -->
```java
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.model.OrgEnrollmentListItem;
import com.teachmehipaa.param.EnrollmentListParams;
import com.teachmehipaa.param.LearnerListParams;
import java.util.List;

// Every Learner, page by page, fetched lazily.
LearnerListParams params = LearnerListParams.builder().limit(100).build();
for (Learner learner : client.learners().list(params).autoPagingIterable()) {
  System.out.println(learner.getLearnerId());
}

// The same items as a Stream.
List<String> expired =
    client.learners().list(params).autoPagingStream()
        .filter(learner -> LearnerStatus.EXPIRED.equals(learner.getStatus()))
        .map(Learner::getEmail)
        .toList();
System.out.println(expired);

// One page at a time.
Page<OrgEnrollmentListItem> page =
    client.enrollments().list(EnrollmentListParams.builder().limit(50).build());
System.out.println(page.items().size());
while (page.hasNextPage()) {
  page = page.nextPage();
  System.out.println(page.items().size());
}
```

`page.pages()` iterates the pages themselves.

## Exceptions

Every exception the library throws about a call is unchecked: a subclass of
`TeachMeHipaaException`, which extends `RuntimeException`. API errors are
chosen by HTTP status:

| Exception                     | When                                                       |
| ----------------------------- | ---------------------------------------------------------- |
| `InvalidRequestException`     | 400, 405, 406, 415 and 422                                 |
| `IdempotencyException`        | 422 `idempotency_key_reuse` (an `InvalidRequestException`) |
| `AuthenticationException`     | 401                                                        |
| `PermissionDeniedException`   | 403, such as a missing Scope                               |
| `NotFoundException`           | 404                                                        |
| `ConflictException`           | 409                                                        |
| `PreconditionFailedException` | 412 (stale ETag) and 428 (missing `ifMatch`)               |
| `RateLimitException`          | 429, with `getRetryAfter()`                                |
| `ApiException`                | 5xx, or a response that isn't JSON (such as an HTML page)  |
| `ApiConnectionException`      | No response arrived                                        |
| `ApiTimeoutException`         | No response within the timeout (an `ApiConnectionException`) |
| `OAuthException`              | The token endpoint refused to issue a token                |
| `TokenStoreException`         | Your `TokenStore` failed to load or save a Refresh token   |

They live in `com.teachmehipaa.exception`, with `OperationFailedException`,
`OperationTimeoutException`, `OperationInterruptedException` and
`WebhookSignatureException`. Each carries the problem's `getCode()` (branch on
it; new codes can appear at any time), `getTitle()`, `getDetail()`, field-level
`getErrors()`, `getStatusCode()`, the response `getHeaders()` and the
`getRequestId()` to quote to support.

<!-- sample: errors -->
```java
import com.teachmehipaa.exception.FieldError;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.PermissionDeniedException;
import com.teachmehipaa.exception.TeachMeHipaaException;

try {
  client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
} catch (NotFoundException e) {
  System.out.println("No such Learner");
} catch (PermissionDeniedException e) {
  System.out.println("Missing a Scope: " + e.getDetail() + " (request " + e.getRequestId() + ")");
} catch (TeachMeHipaaException e) {
  System.out.println(e.getCode() + " " + e.getDetail() + " " + e.getRequestId());
  for (FieldError field : e.getErrors()) {
    System.out.println(field.path() + ": " + field.message());
  }
}
```

## Retries and idempotency

The client retries network errors, 409 `conflict`, 429 and 5xx up to
`maxRetries` times (2 by default), backing off from 0.5 s up to 5 s with
jitter and honoring `Retry-After` up to 60 s. It only retries calls that are
safe to replay: reads, writes that carry an `Idempotency-Key`, and the few
writes that are idempotent by nature, such as `seatOrders().quote`. PATCH and
DELETE are never retried, and take no key: the API ignores one on them.

Every create and custom method gets a generated UUID `Idempotency-Key`,
reused by each of its retries, so a retry never enrolls a Learner twice or
charges twice. Pass your own to make a call safe to repeat across processes.

## Per-call options

Every method has an overload that takes `RequestOptions` last, overriding the
client's settings for that call: `idempotencyKey`, `maxRetries`, `timeout`
and `requestId`. `requestId` sends your own `x-request-id` on every attempt,
so the call can be found in your logs and TeachMeHIPAA's. The API echoes an
id that matches `^[A-Za-z0-9._-]{1,128}$` and replaces any other.

<!-- sample: request-options -->
```java
import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.model.Enrollment;
import com.teachmehipaa.param.EnrollmentCreateParams;
import java.time.Duration;

Enrollment enrollment =
    client
        .enrollments()
        .create(
            EnrollmentCreateParams.builder().email("ana@example.com").courseId("hipaa-2026").build(),
            RequestOptions.builder()
                .idempotencyKey("hr-sync-2026-11-15-ana")
                .maxRetries(5)
                .timeout(Duration.ofSeconds(10))
                .requestId("hr-sync-2026-11-15-ana")
                .build());
System.out.println(enrollment.getEnrollmentId() + " " + enrollment.getLastResponse().requestId());
```

## Conditional writes and reads

Updates that the API guards with ETags take `ifMatch`: the ETag string, or
the object you read, whose `getLastResponse().etag()` the client sends.
`learners().update` and `webhookSubscriptions().update` require it, since the
API refuses them without one; `build()` throws `IllegalStateException` if it
is missing. A stale ETag throws `PreconditionFailedException`.

Updates are merge patches. A field you don't set is not sent and stays
unchanged; a `clearX()` method sends JSON `null` and clears the field.

<!-- sample: conditional-write -->
```java
import com.teachmehipaa.exception.PreconditionFailedException;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerUpdateParams;

Learner learner = client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
try {
  Learner updated =
      client
          .learners()
          .update(
              learner.getLearnerId(),
              LearnerUpdateParams.builder()
                  .ifMatch(learner)
                  .name("Ana Q. Example")
                  .clearExternalId()
                  .build());
  System.out.println(updated.getName());
} catch (PreconditionFailedException e) {
  System.out.println("Someone else changed this Learner; read it again");
}
```

Retrieves that support ETags have a `retrieveIfModified` that sends
`If-None-Match` and returns an empty `Optional` when your copy is still
current (304), so an unchanged object costs no body:

<!-- sample: not-modified -->
```java
import com.teachmehipaa.model.Course;
import java.util.Optional;

Course course = client.courses().retrieve("hipaa-2026");

Optional<Course> changed = client.courses().retrieveIfModified("hipaa-2026", course);
if (changed.isEmpty()) {
  System.out.println("still current");
}
course = changed.orElse(course);
System.out.println(course.getTitle());
```

## Operations

Exports, imports, batch writes, Test deliveries and redrives return an
`Operation`. `waitForCompletion()` polls until it is done (waiting 1 s before
the first poll, backing off to 10 s) and throws `OperationTimeoutException`
after 5 minutes, or `OperationFailedException` if it finished with an error;
both carry the Operation. `waitForCompletion(timeout, interval)` sets both
waits. An Operation that is already done, as exports always are, returns
without a request. An export's `getExportResult()` hands back the CSV.

<!-- sample: operations -->
```java
import com.teachmehipaa.model.ExportResult;
import com.teachmehipaa.model.LearnersExportRequestFormat;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.LearnerExportParams;
import java.time.Duration;

Operation export =
    client
        .learners()
        .export(
            LearnerExportParams.builder()
                .format(LearnersExportRequestFormat.CSV)
                .courseId("hipaa-2026")
                .build());
ExportResult result =
    export.waitForCompletion(Duration.ofMinutes(1), Duration.ofSeconds(2)).getExportResult();
System.out.println(result.filename() + " " + result.rowCount() + " " + result.csv().length());
```

Test deliveries and redrives return a `WebhookDeliveryOperation`, whose
`getDeliveryResult()` says how the Delivery ended. Result kinds this version
doesn't know arrive as `WebhookDeliveryResult.Unknown`, so handle the
fallback:

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

`Operation.operationIdFromName(operation.getName())` gives the id that
`operations().retrieve` takes, to check on an Operation later.

## Uploads and downloads

An Enrollment import takes the CSV as a `FileUpload`: from a `Path`, bytes or
an `InputStream`. Each call reads it once, so a retry resends the same bytes.

Certificate PDFs come back whole from `downloadPdf` as a `BinaryResponse`
(`data()`, `contentType()`, `filename()`), or from `downloadPdfStream` as a
`BinaryStreamResponse` to read as it arrives. Close the stream, ideally with
try-with-resources, to release the connection.

<!-- sample: files -->
```java
import com.teachmehipaa.FileUpload;
import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.BinaryStreamResponse;
import com.teachmehipaa.model.ImportPreview;
import com.teachmehipaa.param.EnrollmentImportParams;
import com.teachmehipaa.param.EnrollmentPreviewImportParams;
import com.teachmehipaa.param.ImportOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

void importRoster(Path roster) {
  ImportPreview preview =
      client
          .enrollments()
          .previewImport(
              EnrollmentPreviewImportParams.builder()
                  .file(FileUpload.of(roster))
                  .options(ImportOptions.builder().courseId("hipaa-2026").build())
                  .build());
  System.out.println(preview.getValidRows() + " of " + preview.getTotalRows() + " rows are valid");

  client
      .enrollments()
      .importEnrollments(EnrollmentImportParams.builder().file(FileUpload.of(roster)).build())
      .waitForCompletion();
}

void saveCertificate(Path directory) throws IOException {
  BinaryResponse pdf = client.certificates().downloadPdf("cert_01JD8F");
  String filename = pdf.filename() != null ? pdf.filename() : "certificate.pdf";
  Files.write(directory.resolve(filename), pdf.data());

  try (BinaryStreamResponse stream = client.certificates().downloadPdfStream("cert_01JD8F")) {
    Files.copy(stream.inputStream(), directory.resolve("certificate-copy.pdf"));
  }
}
```

## Webhooks

`Webhook.constructEvent` verifies a Delivery and returns a `WebhookDelivery`:
an `EventDelivery` holding the Event, or a `TestDeliveryReceived`. It needs
three things exactly as they arrived:

1. **The raw body.** Parsed and re-serialized JSON never verifies, so read
   the body as bytes before anything parses it. In Spring, take it as
   `@RequestBody byte[]`; in a servlet, read `request.getInputStream()`.
2. **The headers.** A `Map`, or a lookup such as `request::getHeader` or
   Spring's `HttpHeaders::getFirst`.
3. **The exact request path, with its query string, or the full URL.** The
   signature covers the path the Delivery was sent to, so a verifier that
   drops the path, a context path or the query string rejects every
   Delivery. Use `request.getRequestURI()` plus `"?" +
   request.getQueryString()` when there is one.

Deliveries signed more than 5 minutes from your clock, in either direction,
are rejected. Verification throws `WebhookSignatureException` with a
`getCode()` such as `signature_mismatch`; answer it with 400. Event types this
version doesn't know arrive as an `UnknownEvent`, so new types never break
your handler. `Webhook` needs only the Signing secret, never API credentials,
so a handler doesn't need a client.

With **Spring Boot**:

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

With a plain **servlet**:

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

To test your handler, sign a payload the way TeachMeHIPAA does with
`Webhook.generateTestHeaders`. Match an `EventDelivery`'s Event against the
`Event…` types to read its data:

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

Pass `Webhook.VerifyOptions` to allow a different tolerance, or to check a
Delivery your queue stored against when it arrived rather than now:

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

## Partner apps

A Partner app acts for many Organizations, each connected through a Grant
that an admin approves on the TeachMeHIPAA consent screen. `OAuth` builds the
consent URL with a PKCE challenge and exchanges the returned code. Keep the
`state` and code verifier in the admin's server-side session between the two
steps. In these samples, `database` stands in for your own storage.

<!-- sample: partner-consent -->
```java
import com.teachmehipaa.OAuth;
import java.util.List;

static final String REDIRECT_URI = "https://partner.example.com/tmh/callback";

/** Returns the URL to send the Organization's admin to. */
String startConsent() {
  OAuth.PkcePair pkce = OAuth.createPkcePair();
  String state = OAuth.createState();
  database.saveConsent(state, pkce.codeVerifier());
  return OAuth.authorizeUrl(
      OAuth.AuthorizeUrlParams.builder()
          .clientId(System.getenv("TMH_APP_ID"))
          .redirectUri(REDIRECT_URI)
          .scopes(List.of("learners:read", "learners:write"))
          .state(state)
          .codeChallenge(pkce.codeChallenge())
          .build());
}

/** Handles the redirect back to REDIRECT_URI. */
void finishConsent(String code, String state) {
  if (!state.equals(database.consentState())) {
    throw new SecurityException("state mismatch: possible CSRF");
  }
  OAuth.GrantTokens grant =
      OAuth.exchangeCode(
          OAuth.ExchangeCodeParams.builder()
              .clientId(System.getenv("TMH_APP_ID"))
              .clientSecret(System.getenv("TMH_APP_SECRET"))
              .code(code)
              .redirectUri(REDIRECT_URI)
              .codeVerifier(database.consentCodeVerifier())
              .build());
  database.saveRefreshToken("org_acme", grant.refreshToken());
}
```

A code works once, within 10 minutes, so the exchange is never retried. The
consent URL defaults to Live; for the Sandbox, pass
`.environment(Environment.SANDBOX)` and `.authorizationUrl(...)`.

Then build a client per connected Organization with a
`PartnerGrantTokenProvider` and a `TokenStore`, which loads and saves the
Grant's Refresh token. Every token exchange rotates it, and the provider
saves each new one to the store before it uses the Access token that came
with it.

<!-- sample: partner-grant -->
```java
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.PartnerGrantTokenProvider;
import com.teachmehipaa.core.TokenStore;

TokenStore grantStore =
    new TokenStore() {
      @Override
      public String load() {
        return database.loadRefreshToken("org_acme");
      }

      @Override
      public void save(String refreshToken) {
        database.saveRefreshToken("org_acme", refreshToken);
      }
    };

PartnerGrantTokenProvider tokens =
    PartnerGrantTokenProvider.builder()
        .clientId(System.getenv("TMH_APP_ID"))
        .clientSecret(System.getenv("TMH_APP_SECRET"))
        .tokenStore(grantStore)
        .build();

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder().tokenProvider(tokens).organizationId("org_acme").build();
System.out.println(client.learners().list().items().size());
```

> [!WARNING]
> Presenting a superseded Refresh token revokes the whole Grant (ADR-0004).
> So `save` must persist durably before it returns, you should build one
> provider per Grant and share it between clients, and a Refresh token
> exchange is **never retried**: a network error during one throws
> `ApiConnectionException`, and an `OAuthException` with code `invalid_grant`
> means the Organization must go through consent again. Never retry these
> yourself with an old Refresh token. If `save` throws, the client throws
> `TokenStoreException` and doesn't use the new token.

Give the provider the same `environment` (and `baseUrl`) as the client:
`build()` refuses, with `IllegalStateException`, a client whose provider
would exchange Refresh tokens against another Environment.

For the Partner's own calls, such as listing and creating customer
Organizations, authenticate with the Partner app's own id (`app_…`) and
secret (`tmh_as_…`) as `clientId` and `clientSecret`, and pass your
`partnerId`:

<!-- sample: partner-organizations -->
```java
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.PartnerOrganization;

TeachMeHipaaClient partner =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_APP_ID"))
        .clientSecret(System.getenv("TMH_APP_SECRET"))
        .build();

for (PartnerOrganization organization :
    partner.partners().organizations().list("ptn_01JD8E2C3D4E5F6G7H8J9K0M1N").autoPagingIterable()) {
  System.out.println(organization.getOrgId() + " " + organization.isGrantActive());
}
```

If you get Access tokens some other way, pass
`.tokenProvider(new StaticTokenProvider(accessToken))`. A static token is
never renewed; once it expires, calls throw `AuthenticationException`.

## Tailing the Event log

`events().tail(...)` reads the Event log oldest first and keeps its own
position in `lastEventId()`. Save it, and resume from it after a restart to
catch up on anything you missed while down, without webhooks:

<!-- sample: events-tail -->
```java
import com.teachmehipaa.core.EventTail;
import com.teachmehipaa.model.Event;
import com.teachmehipaa.param.EventTailParams;
import java.time.Duration;

EventTail tail =
    client
        .events()
        .tail(
            EventTailParams.builder()
                .after(database.loadEventId())
                .follow(true)
                .interval(Duration.ofSeconds(5))
                .build());
for (Event event : tail) {
  System.out.println(event.getEventType() + " " + event.getEventId());
  database.saveEventId(tail.lastEventId());
}
```

Without `follow`, the loop ends once it has caught up. With it, the tail
polls every `interval` until you `break` or the thread is interrupted, for
example by `ExecutorService.shutdownNow()`; the loop then ends quietly and
leaves the interrupt flag set.

## Configuration

<!-- sample: configuration -->
```java
import com.teachmehipaa.AppInfo;
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.JdkHttpTransport;
import java.net.http.HttpClient;
import java.time.Duration;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .scopes("learners:read", "learners:write")
        .organizationId("org_acme")
        .maxRetries(3)
        .timeout(Duration.ofSeconds(30))
        .appInfo(AppInfo.of("acme-hr-sync", "4.2.0", "https://acme.example"))
        .httpTransport(
            new JdkHttpTransport(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()))
        .build();
System.out.println(client.getEnvironment() + " " + client.getBaseUrl() + " " + client.getMaxRetries());
```

| Builder option       | Default                                  | Description                                                                         |
| -------------------- | ---------------------------------------- | ----------------------------------------------------------------------------------- |
| `clientId`           | None: public methods only                | Your API client's id (`cli_…`), or a Partner app's (`app_…`)                        |
| `clientSecret`       | Required with `clientId`                 | The matching secret (`tmh_cs_…` or `tmh_as_…`)                                      |
| `scopes`             | Every Scope the API client holds         | Narrows the Access token's Scopes                                                   |
| `tokenProvider`      | Client credentials                       | Supplies Access tokens instead: `PartnerGrantTokenProvider`, `StaticTokenProvider`  |
| `environment`        | `Environment.LIVE`                       | `LIVE` or `SANDBOX`; credentials work only in their own Environment                 |
| `baseUrl`            | The Environment's URL                    | Overrides it, for example to point at a mock server                                 |
| `organizationId`     | Looked up once with `auth().introspect()`| The Organization to act for                                                         |
| `maxRetries`         | `2`                                      | Retries of a failed call that is safe to replay                                     |
| `timeout`            | 80 seconds                               | How long to wait for each response                                                  |
| `appInfo`            | None                                     | Your software's name, version and URL, sent in the `User-Agent`                  |
| `httpTransport`      | A `JdkHttpTransport`                     | Sends requests: wrap a configured `HttpClient` (proxy, mTLS) or plug in a test fake |
| `logger`             | None: nothing is logged                  | A `RequestLogger` that receives one entry per request (see [Logging](#logging))     |
| `clock`, `sleeper`   | The system clock, `Thread.sleep`         | Supply the time and the waits between retries and polls, for tests                  |

Pass either `clientId` and `clientSecret`, or `tokenProvider`, not both;
`scopes` then belong to the token provider. With neither, the client makes
only public calls. Base URLs: Live is `https://api.tme.so/v1`; Sandbox is
`https://api-teachmehipaa-dev.web.app/v1`. Every setting can be read back
from the client (`getEnvironment()`, `getBaseUrl()`, `getMaxRetries()` and so
on).

## Logging

The library prints nothing, sends no telemetry and writes nothing to disk.
Logging is off until you give the client a `RequestLogger`. It then receives
one `RequestLogEntry` per HTTP request, token requests included, with the
method, the path (without its query string, which can hold Learner emails),
the status, the latency and the request id. A request that got no response is
logged before its exception is raised, with a `null` status, the request id it
sent (if any) and an `errorKind` of `TIMEOUT` or `CONNECTION`. Request and
response bodies, headers and credentials are never logged (ADR-0007), because
they hold Learner details.

<!-- sample: logging -->
```java
import com.teachmehipaa.TeachMeHipaaClient;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .organizationId("org_acme")
        .logger(
            entry ->
                System.out.printf(
                    "%s %s %d in %d ms (request id %s)%n",
                    entry.method(),
                    entry.path(),
                    entry.statusCode(),
                    entry.latency().toMillis(),
                    entry.requestId()))
        .build();

client.learners().list();
// POST /v1/auth/token 200 in 41 ms (request id null)
// GET /v1/organizations/org_acme/learners 200 in 84 ms (request id req_7f3c2a)
```

To send entries to SLF4J instead, pass
`.logger(entry -> log.debug("{}", entry))`. The logger is called from
whichever thread made the request, so it must be thread-safe.

## Requirements

Java 17 or newer, on any JVM. The library is compiled with `--release 17`
and tested on Java 17 and 21. It is server-side only: never ship a client
secret or a Partner app secret to a browser or a mobile app.

## Versioning

The library follows semantic versioning. Its services, models and params are
generated from the Developer API's OpenAPI document; `OPENAPI_VERSION` in
this repository names the document release this version was generated from.
Models are tolerant: unknown response fields are kept (`getExtraFields()`),
and enum fields accept values this version doesn't know, so API additions
don't break your code. See the [changelog](CHANGELOG.md).

## Support and security

<!-- TODO(Nick): confirm the support address before launch. -->

For help, contact TeachMeHIPAA support and quote the request id from the
exception (`getRequestId()`) or from `getLastResponse().requestId()`. To
report a vulnerability, see [SECURITY.md](SECURITY.md); please don't open a
public issue.

See [CONTRIBUTING.md](CONTRIBUTING.md) to work on the library.
