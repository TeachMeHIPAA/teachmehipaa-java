# Async calls

Every client method that sends a request has an `…Async` sibling with the
same parameters and overloads. It returns a `CompletableFuture` of the type
the blocking method returns: `retrieveAsync` returns
`CompletableFuture<Learner>`, `listAsync` returns
`CompletableFuture<Page<Learner>>`. Samples use a `client` built as in
[Getting started](getting-started.md).

<!-- sample: async-retrieve -->
```java
client
    .learners()
    .retrieveAsync("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG")
    .thenAccept(learner -> System.out.println(learner.getName() + " " + learner.getStatus()))
    .join();
```

The `join()` keeps the sample's thread alive until the line prints. In a
server, return the future to your framework instead.

## Threads

The default transport sends with `HttpClient.sendAsync`, so no thread waits
on the network. Retry waits and Operation polls run on
`CompletableFuture.delayedExecutor`'s timer, so no thread sleeps either.

| Stage                        | Runs on                                                      |
| ---------------------------- | ------------------------------------------------------------ |
| The `…Async` call itself     | Your thread, until the request is handed to the transport    |
| Your callbacks               | The thread that completed the response: the `HttpClient`'s executor, or `ForkJoinPool.commonPool()` after a retry wait |
| A custom transport's `send`  | A library-owned daemon thread, `teachmehipaa-blocking-io`    |

Callbacks that block, such as a JDBC write, belong on your own executor:
`thenAcceptAsync(callback, executor)`.

A custom `HttpTransport` needs nothing new. `sendAsync` defaults to running
your `send` on a library-owned daemon thread; override it if your HTTP client
has a non-blocking API. A custom `TokenProvider` works the same way through
`accessTokenAsync`.

## Several calls at once

Start the calls, then wait for all of them:

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

Concurrent calls share one Access token mint and one Organization lookup,
however many start before either finishes.

## Errors

A failed call completes its future exceptionally with the same
`TeachMeHipaaException` subclass the blocking method throws, never wrapped.
How you receive it depends on where you look:

| Where you look                                   | What you get                              |
| ------------------------------------------------ | ----------------------------------------- |
| `exceptionally` or `handle` on the returned future | The exception itself, such as `NotFoundException` |
| `get()`                                          | `ExecutionException`, with it as the cause |
| `join()`                                         | `CompletionException`, with it as the cause |
| A stage after `thenApply` or `thenCompose`       | `CompletionException`, with it as the cause |

The last row is how `CompletableFuture` treats every dependent stage. Attach
the handler straight to the returned future to match on the type:

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

Arguments are checked before anything is sent: a `null` id throws
`NullPointerException` from the call itself.

## Retries, timeouts and cancelling

An async call retries on the blocking schedule: network errors, 409
`conflict`, 429 and 5xx, twice by default, waiting 0.5 s doubling to 5 s with
jitter, or the `Retry-After` the API sends, up to 60 s. `RequestOptions`
apply as they do to blocking calls.

Cancelling the future stops the call: the request in flight is cancelled and
no retry starts. Completing it yourself does the same, so `orTimeout` puts a
ceiling on a call and all its retries:

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

The client's `timeout` (80 s by default) still bounds each attempt.

## Pages

`listAsync` returns the first page; `nextPageAsync()` fetches the one after
it. A recursive method walks them all without blocking:

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

`nextPageAsync()` on the last page throws `NoSuchElementException` before
sending anything; check `hasNextPage()` first. A page from `list` can fetch
the next one with `nextPageAsync()`, and a page from `listAsync` with
`nextPage()`.

## Operations

`waitForCompletionAsync()` polls an Operation on the same schedule as
`waitForCompletion()`: up to 5 minutes, 1 s before the first poll, backing off
to 10 s. It fails with `OperationFailedException` or
`OperationTimeoutException`, and cancelling it stops the polls.

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

A `WebhookDeliveryOperation` narrows the result to
`CompletableFuture<WebhookDeliveryOperation>`, so `getDeliveryResult()` needs
no cast.

## What stays blocking

`events().tail(…)` is an `Iterable`; iterate it on a thread of your own.
Reading a `BinaryStreamResponse` blocks as any `InputStream` does:
`downloadPdfStreamAsync` completes once the headers arrive, and the body is
yours to read.

In tests, pass `asyncSleeper` to the builder to record retry and poll waits
instead of waiting them. See [Testing](testing.md).
