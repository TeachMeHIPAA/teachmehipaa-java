# Async with CompletableFuture

Every service method that sends a request has an `…Async` sibling with the
same parameters and overloads. The sibling returns
`CompletableFuture<T>`, where `T` is exactly what the blocking method
returns:

| Blocking | Async |
| --- | --- |
| `Learner retrieve(String)` | `CompletableFuture<Learner> retrieveAsync(String)` |
| `Page<Learner> list(LearnerListParams)` | `CompletableFuture<Page<Learner>> listAsync(LearnerListParams)` |
| `Optional<Course> retrieveIfModified(String, String)` | `CompletableFuture<Optional<Course>> retrieveIfModifiedAsync(String, String)` |
| `BinaryStreamResponse downloadPdfStream(String)` | `CompletableFuture<BinaryStreamResponse> downloadPdfStreamAsync(String)` |
| `Page.nextPage()` | `Page.nextPageAsync()` |
| `Operation.waitForCompletion()` | `Operation.waitForCompletionAsync()` |

The [resource reference](../reference/resources.md) lists every async
overload. Samples use a `client` built as in
[Getting started](getting-started.md).

<!-- sample: async-retrieve -->
```java
client
    .learners()
    .retrieveAsync("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG")
    .thenAccept(learner -> System.out.println(learner.getName() + " " + learner.getStatus()))
    .join();
```

`join()` keeps the sample's thread alive until the line prints. In a Spring
WebFlux handler or a JAX-RS `@Suspended` resource, return or complete with
the future instead of joining it.

## Threads

The default `JdkHttpTransport` sends with `HttpClient.sendAsync`, so no thread
waits on the network. Waits between retries and Operation polls run on
`CompletableFuture.delayedExecutor`'s timer, so no thread sleeps either. The
client still owns nothing that needs closing.

| Work | Runs on |
| --- | --- |
| The `…Async` call | Your thread, until the request reaches the transport; then the call returns |
| Your dependent stages (`thenApply`, `thenAccept`, …) | The thread that completed the future: the `HttpClient`'s executor, or `ForkJoinPool.commonPool()` after a retry wait |
| A custom transport's blocking `send` | A library daemon thread named `teachmehipaa-blocking-io` |
| A custom `TokenProvider`'s `accessToken()` | The same library daemon threads |

Dependent stages that block, such as a JDBC write, belong on an executor you
own: `thenAcceptAsync(action, executor)`.

## Concurrent calls

Start the calls first, then combine the futures:

<!-- sample: async-fan-out -->
```java
import com.teachmehipaa.model.Learner;
import java.util.List;
import java.util.concurrent.CompletableFuture;

List<CompletableFuture<Learner>> lookups =
    List.of("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG", "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EJ").stream()
        .map(learnerId -> client.learners().retrieveAsync(learnerId))
        .toList();
CompletableFuture.allOf(lookups.toArray(new CompletableFuture<?>[0])).join();
for (CompletableFuture<Learner> lookup : lookups) {
  System.out.println(lookup.join().getEmail());
}
```

Concurrent calls on one client share a single Access token mint and a single
Organization lookup, however many start before either completes.

## Failures

A failed call completes its future exceptionally with the same
`TeachMeHipaaException` subclass the blocking method throws. The library
never wraps it. What your code receives depends on how it observes the
future:

| Observed through | Receives |
| --- | --- |
| `exceptionally`, `handle` or `whenComplete` on the returned future | The exception itself, such as `NotFoundException` |
| `get()` | `ExecutionException`, cause set to the exception |
| `join()` | `CompletionException`, cause set to the exception |
| A stage derived with `thenApply`, `thenCompose` and the like | `CompletionException`, cause set to the exception |

The last row is standard `CompletableFuture` behavior for dependent stages.
To match on the type, attach the handler to the future the library returned:

<!-- sample: async-errors -->
```java
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.model.Learner;
import java.util.concurrent.CompletionException;

Learner learner =
    client
        .learners()
        .retrieveAsync("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EJ")
        .exceptionally(
            failure -> {
              if (failure instanceof NotFoundException) {
                return null;
              }
              throw new CompletionException(failure);
            })
        .join();
System.out.println(learner == null ? "no such Learner" : learner.getName());
```

Arguments are validated before anything is sent: a `null` id throws
`NullPointerException` from the `…Async` call itself, and a params builder
missing a required field throws from `build()`.

## Retries, timeouts and cancellation

Async calls follow the blocking [retry rules](exceptions.md#retries): network
failures, 409 `conflict`, 429 and 5xx, twice by default, 0.5 s doubling to
5 s with jitter, or the `Retry-After` the API sends, up to 60 s.
`RequestOptions` apply unchanged.

`cancel(true)` on the returned future cancels the request in flight and
starts no further retry. Completing the future from outside has the same
effect, so `orTimeout` caps a call together with all its retries:

<!-- sample: async-timeout -->
```java
import com.teachmehipaa.model.Learner;
import java.util.concurrent.TimeUnit;

Learner learner =
    client
        .learners()
        .retrieveAsync("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG")
        .orTimeout(30, TimeUnit.SECONDS)
        .join();
System.out.println(learner.getEmail());
```

The client's `timeout` (80 s by default) still bounds each attempt. Work that
other callers share, the token mint and the Organization lookup, keeps running
when one caller cancels.

## Pages

`listAsync` completes with the first `Page`. `nextPageAsync()` fetches the
page after it. A recursive method walks every page without blocking:

<!-- sample: async-pages -->
```java
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Learner;
import java.util.concurrent.CompletableFuture;

CompletableFuture<Integer> countLearners() {
  return client.learners().listAsync().thenCompose(page -> count(page, 0));
}

CompletableFuture<Integer> count(Page<Learner> page, int before) {
  int total = before + page.items().size();
  if (!page.hasNextPage()) {
    return CompletableFuture.completedFuture(total);
  }
  return page.nextPageAsync().thenCompose(next -> count(next, total));
}
```

On the last page, `nextPageAsync()` throws `NoSuchElementException`
immediately, without sending a request. Pages are interchangeable between the
two styles: a page from `list` offers `nextPageAsync()`, and a page from
`listAsync` offers `nextPage()`.

## Operations

`waitForCompletionAsync()` polls on the blocking schedule: up to 5 minutes,
the first poll after 1 s, backing off to 10 s between polls. The future fails
with `OperationFailedException` or `OperationTimeoutException`, and
cancelling it stops the polls.

<!-- sample: async-operation -->
```java
import com.teachmehipaa.model.CertificatesExportRequestFormat;
import com.teachmehipaa.model.ExportResult;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.CertificateExportParams;

ExportResult csv =
    client
        .certificates()
        .exportAsync(
            CertificateExportParams.builder()
                .format(CertificatesExportRequestFormat.CSV)
                .build())
        .thenCompose(Operation::waitForCompletionAsync)
        .thenApply(Operation::getExportResult)
        .join();
System.out.println(csv.filename() + " " + csv.rowCount());
```

`Operation.waitForCompletionAsync()` is declared to return
`CompletableFuture<? extends Operation>`, so that
`WebhookDeliveryOperation` can narrow it to
`CompletableFuture<WebhookDeliveryOperation>` and `getDeliveryResult()` needs
no cast. Chain it with `thenCompose` as above, or hold it in a variable of
type `CompletableFuture<? extends Operation>`.

## What stays blocking

- `events().tail(…)` returns an `Iterable`; iterate it on a thread you own.
- `BinaryStreamResponse.inputStream()` is an ordinary `InputStream`.
  `downloadPdfStreamAsync` completes when the headers arrive; reading the
  body blocks like any stream read.

## Testing async code

Pass `asyncSleeper` to the builder to replace the retry and poll timer. An
`AsyncSleeper` that returns `CompletableFuture.completedFuture(null)` makes
backoff instant; one that records each `Duration` lets a test assert the
schedule. See [Testing](testing.md).
