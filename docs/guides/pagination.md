# Pagination and the Event log

Samples use a `client` built as in [Getting started](getting-started.md).

## `Page<T>`

A list method, such as `learners().list` or `certificates().list`, sends one
request and returns the first `Page<T>`. The page fetches the pages after it
on demand, with the same filters, `limit` and `RequestOptions`:

| Method | Returns |
| --- | --- |
| `List<T> items()` | This page's items, unmodifiable |
| `String nextCursor()` | The next page's cursor; `null` on the last page |
| `boolean hasNextPage()` | Whether another page follows. A page can be short, or empty, and still have one. |
| `Page<T> nextPage()` | The next page; `NoSuchElementException` on the last |
| `CompletableFuture<Page<T>> nextPageAsync()` | The same, without blocking; see [Async](async.md#pages) |
| `Iterable<Page<T>> pages()` | This page, then each later page as iteration reaches it |
| `Iterable<T> autoPagingIterable()` | Every item from this page on, fetching pages as iteration reaches them |
| `Stream<T> autoPagingStream()` | The same, as a sequential lazy `Stream` |
| `LastResponse getLastResponse()` | Status, headers and request id of this page's response |

Nothing beyond the first page is fetched until iteration asks for it.
Leaving a `for` loop with `break`, or a short-circuiting stream operation such
as `findFirst`, `limit` or `anyMatch`, ends the fetching:

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

`limit` sets the page size, within each list's maximum (shown in the
[resource reference](../reference/resources.md)). An iterator is not
thread-safe; each thread that walks a list calls `autoPagingIterable()` or
`pages()` itself.

To commit one transaction per page, iterate `pages()`:

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

Each Organization keeps 30 days of Events, such as `learner.certified` and
`invite.bounced`. [Webhooks](webhooks.md) push them as they occur; the Event
log lets you pull them, including any a webhook endpoint missed.

`events().list` returns Events oldest first. `type` filters by Event type
and `after` starts after an Event id you already processed. `Event` is a
sealed interface with one final class per type, such as
`EventLearnerCertified`, each with a typed `getData()`. Pattern matching with
`instanceof` reads them:

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

Event types are added over time and never removed. A type this library
version does not know arrives as `UnknownEvent`, with `getEventType()` and the
raw `getData()`, so keep the final `else`. Reading the log requires the
`webhooks:manage` or `audit:read` Scope.

## Tailing the Event log

`events().tail(EventTailParams)` returns an `EventTail`, an `Iterable<Event>`
that walks the log oldest first and tracks its own position in
`lastEventId()`. Persist that id and pass it as `after` on restart to resume
where the process stopped. `database` stands for your own storage:

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

| `EventTailParams` field | Default | Effect |
| --- | --- | --- |
| `after` | The oldest retained Event | Start after this Event id |
| `follow` | `false` | Keep polling for new Events once caught up, instead of ending |
| `interval` | 2 s | Wait between polls once caught up, with `follow` |
| `type` | All types | Only Events of this type |
| `limit` | The API's default, 20 | Events per request |

Without `follow`, iteration ends once the tail has caught up. With `follow`,
it polls every `interval` until the loop breaks or the thread is interrupted,
for example by `ExecutorService.shutdownNow()`. An interrupt ends the loop
without an exception and leaves the interrupt flag set, so the code after the
loop can save `lastEventId()`.

One thread iterates a tail at a time; other threads may read `lastEventId()`.
A consumer offline for more than 30 days has lost Events from the log and
should resynchronize from the lists, such as `learners().list`, before
tailing again.
