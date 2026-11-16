# Optimistic concurrency

The API versions Learners, Enrollments, Courses and Webhook subscriptions
with HTTP **ETags**. A write that carries the ETag you read succeeds only if
no one changed the object since: optimistic locking without a lock. A read
that carries it skips the body when your copy is current.

Samples use a `client` built as in [Getting started](getting-started.md).

## Obtaining an ETag

Every object returned by the client has `getLastResponse().etag()`: the
`ETag` header of the response it was read from, or `null` if there was none.
Where a method accepts an ETag, it also accepts the object itself and reads
`getLastResponse().etag()` for you.

## Conditional writes

Updates guarded by an ETag take `ifMatch` on their params, sent as
`If-Match`:

| Method | `ifMatch` |
| --- | --- |
| `learners().update` | Required; `build()` throws `IllegalStateException` without it |
| `webhookSubscriptions().update` | Required; `build()` throws `IllegalStateException` without it |
| `learners().offboard` | Optional |

A stale ETag fails the write with `PreconditionFailedException` (412). Read
the object again, reapply the change and write again. `"*"` matches any
version, for an intentional overwrite.

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

An object whose response had no ETag, passed to `ifMatch`, throws
`IllegalArgumentException`, so a write never turns unconditional by accident.

## Merge patches

`update` methods send `application/merge-patch+json` (RFC 7396):

| In the params | On the wire | Effect |
| --- | --- | --- |
| A field you set | The value | Field changes |
| A field you leave unset | Absent | Field unchanged |
| A field you clear with `clearX()` | `null` | Field cleared |

The sample above changes `name`, clears `externalId` and leaves every other
field alone. A merge patch is never retried, because replaying it is not
safe; after a `PreconditionFailedException`, the read-modify-write cycle is
yours to repeat.

<!-- sample: conditional-update-subscription -->
```java
import com.teachmehipaa.model.WebhookSubscription;
import com.teachmehipaa.param.WebhookSubscriptionUpdateParams;

WebhookSubscription subscription = client.webhookSubscriptions().retrieve("whs_01JD8Q");
WebhookSubscription paused =
    client
        .webhookSubscriptions()
        .update(
            subscription.getSubscriptionId(),
            WebhookSubscriptionUpdateParams.builder()
                .ifMatch(subscription)
                .enabled(false)
                .clearDescription()
                .build());
System.out.println(paused.isEnabled() + " " + paused.getLastResponse().etag());
```

## Conditional reads

`courses()`, `enrollments()` and `webhookSubscriptions()` have
`retrieveIfModified` beside `retrieve`. It sends `If-None-Match` with the
ETag of your copy, or with the copy itself, and returns `Optional.empty()`
when the API answers 304 Not Modified. An unchanged object costs a round trip
and no body:

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

A copy without an ETag throws `IllegalArgumentException`, because the read
could never be conditional. `retrieveIfModifiedAsync` returns
`CompletableFuture<Optional<T>>`.
