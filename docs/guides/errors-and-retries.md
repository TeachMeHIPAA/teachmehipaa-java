# Errors and retries

## Exceptions

Every exception the library throws about a call is unchecked: a subclass of
`TeachMeHipaaException`, which extends `RuntimeException`. API errors are
chosen by HTTP status:

| Exception                     | When                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| `InvalidRequestException`     | 400, 405, 406, 415 and 422                                   |
| `IdempotencyException`        | 422 `idempotency_key_reuse` (an `InvalidRequestException`)   |
| `AuthenticationException`     | 401                                                          |
| `PermissionDeniedException`   | 403, such as a missing Scope                                 |
| `NotFoundException`           | 404                                                          |
| `ConflictException`           | 409                                                          |
| `PreconditionFailedException` | 412 (stale ETag) and 428 (missing `ifMatch`)                 |
| `RateLimitException`          | 429, with `getRetryAfter()`                                  |
| `ApiException`                | 5xx, or a response that isn't JSON (such as an HTML page)    |
| `ApiConnectionException`      | No response arrived                                          |
| `ApiTimeoutException`         | No response within the timeout (an `ApiConnectionException`) |
| `OAuthException`              | The token endpoint refused to issue a token                  |
| `OperationFailedException`    | An Operation you waited on finished with an error            |
| `OperationTimeoutException`   | An Operation wasn't done when `waitForCompletion` gave up    |
| `WebhookSignatureException`   | A Delivery failed verification                               |

They live in `com.teachmehipaa.exception`. Each carries the API's problem
details:

| Method            | What it returns                                                 |
| ----------------- | --------------------------------------------------------------- |
| `getStatusCode()` | The HTTP status, or `null` when no response arrived             |
| `getCode()`       | The machine-readable reason, such as `seat_unavailable`         |
| `getTitle()`      | A short, human-readable summary                                 |
| `getDetail()`     | An explanation of this occurrence                               |
| `getErrors()`     | Field-level problems, each a `FieldError` with `path()` and `message()` |
| `getRequestId()`  | The request id to quote to support, or `null`                   |
| `getHeaders()`    | The response headers                                            |
| `getRawBody()`    | The response body as text, or `null` when no response arrived   |

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

Samples use a `client` built as in [Getting started](getting-started.md).

## Branching on `getCode()`

The class tells you the kind of failure; `getCode()` tells you the reason.
Branch on it when you handle a case specially. New codes can appear at any
time, so always keep a fallback, and compare with `"code".equals(...)`: a
response that wasn't a problem document, such as a proxy's HTML error page,
has no code.

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

A `validation_failed` problem (422) lists each invalid field in
`getErrors()`: `path()` is the dot-joined field path, such as
`items.3.email`.

## Request ids

Every response carries a request id. Returned objects expose it as
`getLastResponse().requestId()`, and exceptions as `getRequestId()`. Log it:
it is what TeachMeHIPAA support needs to find a request. To correlate with
your own logs, send your own id with the `requestId` request option; the API
echoes it back.

## Mistakes in calling the library

A mistake in how you call the library is a bug to fix, not a failure to
handle, so it is never a `TeachMeHipaaException`. Building params without a
required field, such as `ifMatch` on `LearnerUpdateParams`, throws
`IllegalStateException` from `build()`; so does an Organization-scoped call
on a client built without credentials. Passing an object whose response had
no ETag as `ifMatch` throws `IllegalArgumentException` before anything is
sent, and a `null` id throws `NullPointerException`.

## Retries

The client retries a failed call up to `maxRetries` times (2 by default) when
the failure is transient:

- no response arrived (`ApiConnectionException`, `ApiTimeoutException`);
- 409 with the code `conflict` (a concurrent write);
- 429 (`RateLimitException`);
- any 5xx.

It backs off exponentially from 0.5 s up to 5 s, with jitter, and honors a
`Retry-After` header up to 60 s. It retries only calls that are safe to
replay: reads, writes that carry an `Idempotency-Key`, and the few writes that
are idempotent by nature, such as `learners().createLaunchLink`. PATCH and
DELETE calls are never retried. The
[resource reference](../reference/resources.md) says which methods retry.

A 429 or 5xx that outlasts the retries is thrown as usual;
`RateLimitException.getRetryAfter()` carries the wait the API asked for, as a
`Duration`. Interrupting a thread while the client waits to retry stops the
retries: the call throws the failure it would have retried, and the thread
keeps its interrupt flag.

## Idempotency

Every create and custom method sends an `Idempotency-Key` header: a UUID the
client generates once per call and reuses on each of its retries, so a retry
never enrolls a Learner twice.

To make a call safe to repeat across processes, such as a sync job that may
run twice, pass your own key, derived from what the call does:

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

The API remembers a key with the request it came with. Sending the same key
with a different body throws `IdempotencyException`, so never reuse a key for
a different request. PATCH and DELETE take no key: the API ignores one on
them.

## Request options

Every method has an overload that takes `RequestOptions` last, overriding the
client's settings for that call:

| Option           | Effect                                                     |
| ---------------- | ---------------------------------------------------------- |
| `idempotencyKey` | Replaces the generated `Idempotency-Key`                   |
| `maxRetries`     | Overrides the client's `maxRetries` for this call          |
| `timeout`        | How long to wait for each response; overrides the client's |
| `requestId`      | Sent as `x-request-id` on every attempt                    |

`requestId` must match `^[A-Za-z0-9._-]{1,128}$`, or the API replaces it with
its own. Conditional requests take their ETag on the params instead; see
[Conditional requests](conditional-requests.md).
