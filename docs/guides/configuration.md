# Configuration

Every setting is given to the client builder, and fixed once the client is
built. To change one, build another client. Every setting can be read back
from the client (`getEnvironment()`, `getBaseUrl()`, `getMaxRetries()` and so
on).

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

## Options

| Builder option       | Default                                   | Description                                                                         |
| -------------------- | ----------------------------------------- | ----------------------------------------------------------------------------------- |
| `clientId`           | None: public methods only                 | Your API client's id (`cli_…`)                                                      |
| `clientSecret`       | Required with `clientId`                  | The matching secret (`tmh_cs_…`)                                                    |
| `scopes`             | Every Scope the API client holds          | Narrows the Access token's Scopes                                                   |
| `tokenProvider`      | Client credentials                        | Supplies Access tokens instead, such as a `StaticTokenProvider`                     |
| `environment`        | `Environment.LIVE`                        | `LIVE` or `SANDBOX`; credentials work only in their own Environment                 |
| `baseUrl`            | The Environment's URL                     | Overrides it, for example to point at a mock server                                 |
| `organizationId`     | Looked up once with `auth().introspect()` | The Organization to act for                                                         |
| `maxRetries`         | `2`                                       | Retries of a failed call that is safe to replay                                     |
| `timeout`            | 80 seconds                                | How long to wait for each response                                                  |
| `appInfo`            | None                                      | Your software's name, version and URL, sent in the `User-Agent`                     |
| `httpTransport`      | A `JdkHttpTransport`                      | Sends requests: wrap a configured `HttpClient` (proxy, mTLS) or plug in a test fake |
| `logger`             | None: nothing is logged                   | A `RequestLogger` that receives one entry per request (see [Logging](#logging))     |
| `clock`, `sleeper`   | The system clock, `Thread.sleep`          | Supply the time and the waits between retries and polls, for tests                  |
| `asyncSleeper`       | A timer that holds no thread              | Supplies the waits of the [async calls](async.md), for tests                         |

Give `clientId` and `clientSecret`, or a `tokenProvider`, or neither for a
client that can only make
[public lookups](authentication.md#public-lookups-without-credentials).
`build()` throws `IllegalStateException` for a combination that can't work,
such as only one of `clientId` and `clientSecret`; a setter throws
`IllegalArgumentException` for a value out of range, such as a negative
`maxRetries`, so a mistake fails where you made it.

## Timeouts and retries

`timeout` bounds the wait for each response; a call that times out throws
`ApiTimeoutException` after any retries. `maxRetries` sets how many times a
failed call that is safe to replay is retried. Both can be overridden for one
call with [request options](errors-and-retries.md#request-options):

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

`timeout` covers each attempt, not the whole call: with retries, a call can
take longer than `timeout` in all. `waitForCompletion` on an Operation has its
own timeout; see [Operations and files](operations-and-files.md).

## Identifying your software

`appInfo` adds your software's name, and optionally its version and URL, to
the `User-Agent` header, which helps TeachMeHIPAA support find your requests.
The `User-Agent` is the only thing the library says about its caller: it
sends no telemetry and writes nothing to disk.

## Custom HTTP client

The library sends requests with the JDK's `java.net.http.HttpClient`, through
a `JdkHttpTransport`. To change how requests leave your process, such as
through a proxy, with mTLS or with a different connect timeout, build your own
`HttpClient` and wrap it; the library keeps handling tokens, retries and
errors:

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

For anything `HttpClient` can't do, implement `HttpTransport` yourself, as
[Testing](testing.md#fake-the-transport-in-unit-tests) does with a fake.
Implementations must be thread-safe.

## Logging

The library prints nothing, sends no telemetry and writes nothing to disk.
Logging is off until you give the client a `RequestLogger`. It then receives
one `RequestLogEntry` per HTTP request, token requests included. Request and
response bodies, headers and credentials are never logged, because they hold
Learner details.

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

Each entry has:

| Field          | Example                                  |
| -------------- | ---------------------------------------- |
| `method()`     | `GET`                                    |
| `path()`       | `/v1/organizations/org_acme/learners`    |
| `statusCode()` | `200`, or `null` when no response arrived |
| `latency()`    | A `Duration`, such as 84 ms              |
| `requestId()`  | `req_…`, or `null`                       |
| `errorKind()`  | `TIMEOUT` or `CONNECTION` when no response arrived, else `null` |

The path never has its query string, which can hold Learner emails. A
request that got no response is logged before its exception is thrown, with
the request id it sent, if any.

To send entries to SLF4J instead, pass
`.logger(entry -> log.debug("{}", entry))`. The logger is called from
whichever thread made the request, so it must be thread-safe.

## Requirements

Java 17 or newer, on any JVM. The library is compiled with `--release 17`
and tested on Java 17 and 21. Its only dependency is Jackson
(`jackson-databind`, `jackson-annotations` and `jackson-datatype-jsr310`). It
is server-side only: never ship a client secret to a browser or a mobile app.
