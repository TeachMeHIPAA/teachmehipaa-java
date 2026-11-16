# Conditional requests

The API tags versions of an object with an **ETag**. Send it back with a
write, and the write happens only if nobody changed the object since you read
it. Send it with a read, and the API skips the transfer when your copy is
still current.

Samples use a `client` built as in [Getting started](getting-started.md).

## Where ETags come from

Every object the library returns has `getLastResponse()`, whose `etag()` is
the ETag the API sent with it, or `null` when it sent none. Wherever a method
takes an ETag, you can pass either the ETag string or the object itself; the
library reads `getLastResponse().etag()` for you.

## Conditional writes with `ifMatch`

Updates that the API guards with ETags take `ifMatch` on their params, sent
as `If-Match`:

| Method                          | `ifMatch`                                  |
| ------------------------------- | ------------------------------------------ |
| `learners().update`             | Required: `build()` throws without it      |
| `webhookSubscriptions().update` | Required: `build()` throws without it      |
| `learners().offboard`           | Optional                                   |

The API refuses the required ones without an ETag, so the params' `build()`
throws `IllegalStateException` before anything is sent. A stale ETag throws
`PreconditionFailedException` (412): read the object again, reapply your
change and retry. `"*"` matches any version, for when you mean to overwrite.

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

Passing an object whose response had no ETag throws
`IllegalArgumentException` before anything is sent, rather than silently
making the write unconditional.

## Merge-patch updates

`update` methods send a JSON merge patch (`application/merge-patch+json`):

- a field you set is changed to the value you set;
- a field you don't set is not sent, and stays unchanged;
- a field you clear with its `clearX()` method is sent as `null`, which
  clears it.

In the sample above, `name` changes, `externalId` is cleared, and nothing
else is touched. Merge-patch updates are never retried, since replaying one
isn't safe; on a conflict, read and try again yourself.

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

## Conditional reads with `retrieveIfModified`

`courses()`, `enrollments()` and `webhookSubscriptions()` have a
`retrieveIfModified` next to `retrieve`. It sends `If-None-Match` with the
ETag of the copy you hold, or the copy itself, and returns an empty
`Optional` when the API answers 304 Not Modified, so an unchanged object
costs no body:

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

Like `ifMatch`, passing a copy whose response had no ETag throws
`IllegalArgumentException`, since the read could never be conditional.
