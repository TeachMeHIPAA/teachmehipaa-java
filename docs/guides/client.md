# The client

`TeachMeHipaaClient` is the library's single entry point. You configure it
once through `TeachMeHipaaClient.Builder`, then reach each API resource
through an accessor: `client.learners()`, `client.enrollments()`,
`client.webhookSubscriptions()` and so on.

## Construction

<!-- sample: configuration -->
```java
import com.teachmehipaa.AppInfo;
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
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
        .build();
System.out.println(client.getEnvironment() + " " + client.getBaseUrl() + " " + client.getMaxRetries());
```

`build()` validates the combination and fixes every value; the client has no
setters. Each value can be read back: `getEnvironment()`, `getBaseUrl()`,
`getOrganizationId()`, `getScopes()`, `getMaxRetries()`, `getTimeout()` and
`getAppInfo()`. A different configuration needs a second client.

| Builder method | Default | Effect |
| --- | --- | --- |
| `clientId(String)` | None; the client serves public methods only | API client id (`cli_…`) |
| `clientSecret(String)` | None; required with `clientId` | API client secret (`tmh_cs_…`) |
| `scopes(String...)` | Every Scope the API client holds | Requests a narrower Access token |
| `tokenProvider(TokenProvider)` | Client credentials | Supplies Access tokens instead, such as a `StaticTokenProvider` |
| `environment(Environment)` | `Environment.LIVE` | `LIVE` or `SANDBOX`; credentials are valid in one only |
| `baseUrl(String)` | The Environment's URL | Points at another host, such as a mock server |
| `organizationId(String)` | Discovered once through `auth().introspect()` | The Organization the client acts for |
| `maxRetries(int)` | `2` | Retries of a failed call that is safe to replay |
| `timeout(Duration)` | 80 s | Wait for each response, per attempt |
| `appInfo(AppInfo)` | None | Your application's name, version and URL in the `User-Agent` |
| `httpTransport(HttpTransport)` | A `JdkHttpTransport` on a new `HttpClient` | Sends every request |
| `logger(RequestLogger)` | None; nothing is logged | Receives one `RequestLogEntry` per HTTP request |
| `clock(Clock)` | `Clock.systemUTC()` | Token expiry and Operation deadlines; for tests |
| `sleeper(Sleeper)` | `Thread.sleep` | Waits between blocking retries and polls; for tests |
| `asyncSleeper(AsyncSleeper)` | `AsyncSleeper.SYSTEM`, a timer that holds no thread | Waits between [async](async.md) retries and polls; for tests |

Validation fails at the point of the mistake. A setter throws
`IllegalArgumentException` for a value out of range, such as
`maxRetries(-1)` or a zero `timeout`, and `NullPointerException` for `null`.
`build()` throws `IllegalStateException` for a combination that cannot work:
`clientId` without `clientSecret`, or a `tokenProvider` together with
credentials or `scopes`. A client built with no credentials at all is valid;
it serves the [public methods](authentication.md#public-lookups-without-credentials).

## Lifecycle

`TeachMeHipaaClient` does not implement `AutoCloseable`, and nothing needs
closing when you are done with it. Create one per set of credentials at
startup and keep it for the life of the process. In Spring, declare it as a
singleton `@Bean`; in Jakarta EE or Guice, bind it as a singleton.

A new client starts with no Access token and no Organization id, so the
first call on each new client pays for a token request, and an Organization
lookup unless you gave `organizationId`. Building a client per request
repeats that cost on every request.

The threads the library starts itself are daemon threads. When you pass
your own `HttpClient` through `JdkHttpTransport`, its executor and its
shutdown remain yours.

`BinaryStreamResponse`, returned by `certificates().downloadPdfStream`, is the
one `AutoCloseable` type: it holds an open HTTP connection until closed. Read
it in a try-with-resources block, as
[Operations and files](operations-and-files.md#certificate-pdfs) shows.

## Thread safety

| Type | Guarantee |
| --- | --- |
| `TeachMeHipaaClient` | Thread-safe. Concurrent calls share one token mint and one Organization lookup. |
| Services such as `LearnerService` | Stateless; each accessor call returns a new one. |
| `TeachMeHipaaClient.Builder` and every `…Params.Builder` | Not thread-safe. Confine a builder to one thread. |
| `…Params`, `RequestOptions`, `FileUpload` | Immutable. |
| Returned models | No setters. Safe to read from any thread once the call has returned. |
| `Page` | Immutable. Each iterator from `autoPagingIterable()` or `pages()` belongs to one thread. |
| `EventTail` | One iterating thread at a time; any thread may read `lastEventId()`. |
| `BinaryStreamResponse` | One reading thread. |
| Your `HttpTransport`, `TokenProvider`, `RequestLogger` | Called from many threads at once, so they must be thread-safe. |

## Per-call options

Every service method has an overload that takes `RequestOptions` as its last
argument. The options override the client's settings for that call only:

<!-- sample: configuration-per-call -->
```java
import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerListParams;
import java.time.Duration;

Page<Learner> page =
    client
        .learners()
        .list(
            LearnerListParams.builder().limit(100).build(),
            RequestOptions.builder().timeout(Duration.ofSeconds(5)).maxRetries(0).build());
System.out.println(page.items().size());
```

| `RequestOptions.Builder` method | Effect |
| --- | --- |
| `timeout(Duration)` | Wait for each response of this call |
| `maxRetries(int)` | Retries for this call |
| `idempotencyKey(String)` | Replaces the generated `Idempotency-Key`; see [Idempotency](exceptions.md#idempotency) |
| `requestId(String)` | Sent as `x-request-id` on every attempt; must match `^[A-Za-z0-9._-]{1,128}$` or the API replaces it |

`timeout` bounds one attempt. With the default two retries, a call that times
out on every attempt runs for three timeouts plus the waits between them.
`waitForCompletion` on an Operation has a separate deadline; see
[Operations and files](operations-and-files.md).

## Identifying your application

`AppInfo.of(name, version, url)` appends your application to the
`User-Agent` header, for example
`teachmehipaa-java/0.1.0-SNAPSHOT java/21.0.5+11-LTS acme-hr-sync/4.2.0 (https://acme.example)`.
TeachMeHIPAA support uses it to find your traffic. The `User-Agent` is the
only information the library sends about its caller.

## A custom HttpClient

`JdkHttpTransport` sends requests through a `java.net.http.HttpClient`. To
route through a proxy, present a client certificate, or change the connect
timeout, configure an `HttpClient` and wrap it. Token handling, retries and
exception mapping stay in the library:

<!-- sample: configuration-http-client -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.JdkHttpTransport;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

HttpClient httpClient =
    HttpClient.newBuilder()
        .proxy(ProxySelector.of(new InetSocketAddress("proxy.internal.example", 3128)))
        .connectTimeout(Duration.ofSeconds(10))
        .build();

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .httpTransport(new JdkHttpTransport(httpClient))
        .build();
System.out.println(client.getBaseUrl());
```

For an HTTP stack other than the JDK's, implement `HttpTransport`. Its one
abstract method, `send(TransportRequest)`, returns a `TransportResponse` for
any status and throws `IOException` only when no response arrived. The
default `sendAsync` runs your `send` on a library thread; override it if your
stack has a non-blocking API. [Testing](testing.md#fake-the-transport-in-unit-tests)
implements one as a fake.

## Logging

The library writes nothing to standard output, stderr or a logging framework
until you pass a `RequestLogger`. The logger then receives one
`RequestLogEntry` per HTTP request, token requests included:

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

| Accessor | Value |
| --- | --- |
| `method()` | `GET` |
| `path()` | `/v1/organizations/org_acme/learners`, without the query string |
| `statusCode()` | `200`; `null` when no response arrived |
| `latency()` | A `Duration` |
| `requestId()` | `req_…` from the response, or the one sent; may be `null` |
| `errorKind()` | `TIMEOUT` or `CONNECTION` when no response arrived; otherwise `null` |

Bodies, headers, query strings and credentials never reach the logger: they
carry Learner names and emails (ADR-0007). A request that got no response is
logged before its exception propagates. Retries produce one entry per
attempt.

To route entries to SLF4J, pass `.logger(entry -> log.debug("{}", entry))`.
The logger runs on the thread that made the request, or for async calls on
the thread that completed it, so it must be thread-safe.
