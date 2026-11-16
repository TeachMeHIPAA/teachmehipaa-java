# Exceptions and retries

Samples use a `client` built as in [Getting started](getting-started.md).

## Checked and unchecked

The library's API declares no checked exception for a failed call
(ADR-0008). A method such as `learners().retrieve` has no `throws` clause;
you catch what you can handle and let the rest propagate. Checked exceptions
appear only where the JDK puts them:

| Exception | Kind | Raised by |
| --- | --- | --- |
| `TeachMeHipaaException` and every subclass | Unchecked (`RuntimeException`) | Any call that sent, or tried to send, a request; webhook verification |
| `IllegalStateException`, `IllegalArgumentException`, `NullPointerException` | Unchecked | Incorrect use of the library, before anything is sent (see [below](#incorrect-use)) |
| `java.io.UncheckedIOException` | Unchecked | A `FileUpload` whose file or stream can't be read; a `BinaryStreamResponse` that fails to close |
| `java.io.IOException` | Checked | Reading `BinaryStreamResponse.inputStream()`; your own `HttpTransport.send` when no response arrived |
| `InterruptedException` | Checked | Your own `Sleeper.sleep` |

## The hierarchy

All API failures live in `com.teachmehipaa.exception`:

```text
RuntimeException
└── TeachMeHipaaException
    ├── InvalidRequestException        400, 405, 406, 415, 422
    │   └── IdempotencyException       422 idempotency_key_reuse
    ├── AuthenticationException        401
    ├── PermissionDeniedException      403
    ├── NotFoundException              404
    ├── ConflictException              409
    ├── PreconditionFailedException    412, 428
    ├── RateLimitException             429; getRetryAfter()
    ├── ApiException                   5xx, other statuses, or a non-JSON success
    ├── ApiConnectionException         no response arrived
    │   └── ApiTimeoutException        no response within the timeout
    ├── OAuthException                 the token endpoint refused to issue a token
    ├── OperationFailedException       a waited-on Operation finished with an error
    ├── OperationTimeoutException      waitForCompletion's deadline passed
    ├── OperationInterruptedException  the waiting thread was interrupted
    └── WebhookSignatureException      a Delivery failed verification
```

The three `Operation…` exceptions carry the Operation's last known state in
`getOperation()`. `ApiTimeoutException` extends `ApiConnectionException`, so
one `catch (ApiConnectionException e)` covers both.

## Problem details

API errors arrive as RFC 9457 problem documents. Every `TeachMeHipaaException`
exposes them:

| Accessor | Returns |
| --- | --- |
| `getStatusCode()` | `Integer` HTTP status; `null` when no response arrived |
| `getCode()` | Machine-readable reason, such as `seat_unavailable`; may be `null` |
| `getTitle()` | Short summary |
| `getDetail()` | Explanation of this occurrence |
| `getErrors()` | `List<FieldError>`, each with `path()` and `message()`; empty when none |
| `getRequestId()` | The `x-request-id` to quote to support; may be `null` |
| `getHeaders()` | Response headers |
| `getRawBody()` | Response body as text; `null` when no response arrived |

Order `catch` clauses from specific to general:

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

## Branching on `getCode()`

The class identifies the kind of failure and `getCode()` the reason. New codes
appear without a library release, so every branch on a code needs an `else`.
Compare with `"code".equals(e.getCode())`: a response that was not a problem
document, such as a proxy's HTML error page, has no code.

<!-- sample: errors-code -->
```java
import com.teachmehipaa.exception.ConflictException;
import com.teachmehipaa.param.LearnerUpdateParams;

try {
  client
      .learners()
      .update(
          "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG",
          LearnerUpdateParams.builder().ifMatch("*").externalId("hr-123").build());
} catch (ConflictException e) {
  if ("external_id_conflict".equals(e.getCode())) {
    System.out.println("Another Learner already has that External ID");
  } else if ("learner_removed".equals(e.getCode())) {
    System.out.println("That Learner was offboarded");
  } else {
    System.out.println("Conflict: " + e.getCode() + " " + e.getDetail());
  }
}
```

A 422 `validation_failed` lists each rejected field in `getErrors()`.
`path()` is the dot-joined location, such as `items.3.email`.

## Request ids

Every response carries an `x-request-id`. Read it from
`getLastResponse().requestId()` on a returned object, or from
`getRequestId()` on an exception, and include it in your logs. To correlate
with your own tracing, send your id through `RequestOptions.requestId`; the
API echoes it.

## Incorrect use

A mistake in how the library is called is a defect in the calling code, so
it never surfaces as a `TeachMeHipaaException`:

| Mistake | Exception | When |
| --- | --- | --- |
| A required params field is missing, such as `ifMatch` on `LearnerUpdateParams` | `IllegalStateException` | `build()` |
| An Organization-scoped call on a client built without credentials | `IllegalStateException` | The call, before sending |
| An object without an ETag passed as `ifMatch`, or as the cached copy to `retrieveIfModified` | `IllegalArgumentException` | The builder method or the call, before sending |
| A `null` argument | `NullPointerException` | The call |
| `nextPage()` on the last page | `NoSuchElementException` | The call |

## Retries

The client retries a failed call up to `maxRetries` times (default 2) when
two conditions hold. The failure must be transient:

| Failure | Retried |
| --- | --- |
| No response (`ApiConnectionException`, `ApiTimeoutException`) | Yes |
| 409 with `getCode()` equal to `conflict` | Yes |
| 409 with any other code | No |
| 429 | Yes |
| 5xx | Yes |
| Any other 4xx | No |

And the call must be safe to replay: a read, a write that carries an
`Idempotency-Key`, or a write that is idempotent by meaning, such as
`learners().createLaunchLink`. PATCH and DELETE are never retried. The
[resource reference](../reference/resources.md) states the rule for each
method.

Between attempts the client waits 0.5 s, then doubles the ceiling up to 5 s,
picking 50 to 100 % of each ceiling at random and never less than 0.5 s. A
`Retry-After` header replaces that wait, capped at 60 s. When retries run out,
the last failure propagates; `RateLimitException.getRetryAfter()` holds the
wait the API asked for, as a `Duration`.

A 401 `unauthorized` is handled apart from retries: the client tells the
token provider to drop the refused Access token, gets a new one and replays
the call once. The replay does not
count against `maxRetries`.

Interrupting a thread while it waits to retry ends the retries. The call
throws the failure it would have retried, with the `InterruptedException`
attached through `getSuppressed()`, and the thread's interrupt flag stays set.

## Idempotency

Creates and custom methods send an `Idempotency-Key` header. The client
generates a UUID once per call and sends it on every retry of that call, so a
retried `learners().create` never invites a Learner twice.

A job that may run twice, such as a nightly sync, passes its own key derived
from the work it does, so the second run's call is a replay of the first:

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

The API stores each key with the request it arrived with. The same key with a
different body throws `IdempotencyException`. PATCH and DELETE take no key;
the API ignores one sent with them.
