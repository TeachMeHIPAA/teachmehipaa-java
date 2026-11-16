# Pagination and the Event log

Samples use a `client` built as in [Getting started](getting-started.md).

## Lists

Every list method, such as `learners().list` or `certificates().list`,
returns one `Page`, with `items()` and `nextCursor()`. `autoPagingIterable()`
yields every item across all pages, fetching the next page only when the loop
reaches it, so a `break` stops fetching. `autoPagingStream()` is the same as a
lazy `Stream`: `findFirst`, `limit` and `anyMatch` stop fetching too.

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

What you can do with a page:

| Call                    | Result                                                     |
| ----------------------- | ---------------------------------------------------------- |
| `items()`               | This page's items                                          |
| `nextCursor()`          | The cursor of the next page, or `null` on the last         |
| `hasNextPage()`         | Whether more items follow                                  |
| `nextPage()`            | The next page, with the same filters and `limit`           |
| `pages()`               | This page and each one after it, fetched as you iterate    |
| `autoPagingIterable()`  | Every item on this page and the ones after it, in order    |
| `autoPagingStream()`    | The same, as a lazy `Stream`                               |
| `getLastResponse()`     | This page's status, headers and request id                 |

`limit` sets the page size; each list's maximum is in the
[resource reference](../reference/resources.md). Calling a list method sends
one request for the first page; every later page is fetched only when you ask
for it.

To work a page at a time, such as to write each page in one database
transaction, iterate the pages:

<!-- sample: pagination-pages -->
```java
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Certificate;
import com.teachmehipaa.param.CertificateListParams;

Page<Certificate> first =
    client.certificates().list(CertificateListParams.builder().limit(100).build());
for (Page<Certificate> page : first.pages()) {
  System.out.println(page.items().size() + " " + page.getLastResponse().requestId());
}
```

## The Event log

Each Organization keeps an Event log: a record of what happened, such as
`learner.certified` or `invite.bounced`, kept for 30 days. Webhooks push these
Events to you as they happen (see [Webhooks](webhooks.md)); the Event log lets
you pull them, and catch up on anything you missed.

`events().list` reads the log oldest first. Filter by Event type with `type`,
and start after an Event you already have with `after`. Each Event type has
its own class, such as `EventLearnerCertified`, with typed `getData()`:

<!-- sample: events-list -->
```java
import com.teachmehipaa.model.Event;
import com.teachmehipaa.model.EventLearnerCertified;
import com.teachmehipaa.model.EventSeatsLow;
import com.teachmehipaa.param.EventListParams;

EventListParams afterLastSeen = EventListParams.builder().after("evt_01").build();
for (Event event : client.events().list(afterLastSeen).autoPagingIterable()) {
  if (event instanceof EventLearnerCertified certified) {
    System.out.println("certified " + certified.getData().getEmail());
  } else if (event instanceof EventSeatsLow seatsLow) {
    System.out.println("Seats left: " + seatsLow.getData().getAvailable());
  } else {
    System.out.println("other " + event.getEventType());
  }
}
```

New Event types are added over time and none are removed. A type this version
of the library doesn't know arrives as an `UnknownEvent`, with its
`getEventType()` and `getData()` as sent, so always keep the fallback branch.

Reading the log needs the `webhooks:manage` or `audit:read` Scope.

## Tailing the Event log

`events().tail(...)` reads the log oldest first and keeps its own position in
`lastEventId()`. Save it, and resume from it after a restart to catch up on
anything you missed while down, without webhooks. Here `database` stands in
for your own storage:

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

| Parameter  | Default          | Effect                                                        |
| ---------- | ---------------- | ------------------------------------------------------------- |
| `after`    | The oldest Event | Start after this Event id, such as a saved `lastEventId()`    |
| `follow`   | `false`          | Once caught up, keep polling for new Events instead of ending |
| `interval` | 2 seconds        | How long to wait between polls once caught up, with `follow`  |
| `type`     | Every type       | Only Events of this type                                      |
| `limit`    | The API's (20)   | Events per request                                            |

Without `follow`, the loop ends once it has caught up. With it, the tail
polls every `interval` until you `break` or the thread is interrupted, for
example by `ExecutorService.shutdownNow()`; the loop then ends quietly and
leaves the interrupt flag set. A tail is for one thread at a time, though
another thread may read `lastEventId()`.

The log keeps 30 days of Events. A consumer that is down longer than that
should resynchronize from the lists, such as `learners().list`, before
tailing again.
