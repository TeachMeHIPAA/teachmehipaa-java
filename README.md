# teachmehipaa-java

Java client library for the TeachMeHIPAA Developer API: enroll your workforce
in HIPAA training, track Certificates and compliance, and receive signed
webhook Events from your own JVM services.

[![CI](https://github.com/TeachMeHIPAA/teachmehipaa-java/actions/workflows/ci.yml/badge.svg)](https://github.com/TeachMeHIPAA/teachmehipaa-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

| Requirement | Value |
| --- | --- |
| Java | 17 or later (compiled with `--release 17`; CI runs 17 and 21) |
| Runtime dependencies | Jackson 2.22 (`jackson-databind`, `jackson-annotations`, `jackson-datatype-jsr310`) |
| HTTP | The JDK's `java.net.http.HttpClient`; no other HTTP library |
| Coordinates | `com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT` |

> [!IMPORTANT]
> **Pre-release.** The Developer API is not generally available, and no
> artifact is published to Maven Central. Until 1.0.0, install the library
> from this repository into your local Maven repository (below). Class and
> method names can change between snapshots.

## Installation

Build from source and install into `~/.m2`:

```sh
git clone https://github.com/TeachMeHIPAA/teachmehipaa-java.git
cd teachmehipaa-java
./gradlew publishToMavenLocal
```

Then declare the dependency:

<table>
<tr><th>Maven (<code>pom.xml</code>)</th><th>Gradle (<code>build.gradle.kts</code>)</th></tr>
<tr>
<td>

```xml
<dependency>
  <groupId>com.teachmehipaa</groupId>
  <artifactId>teachmehipaa-java</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

</td>
<td>

```kotlin
repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT")
}
```

</td>
</tr>
</table>

Maven reads `~/.m2` without extra configuration. Gradle needs `mavenLocal()`.
[Installation](docs/guides/installation.md) covers Gradle's Groovy DSL, the
transitive Jackson dependency and version alignment.

## Quickstart

An Organization admin creates an API client in the TeachMeHIPAA console. It
has a client id (`cli_…`) and a client secret (`tmh_cs_…`). Use Sandbox
credentials while you build: the Sandbox never emails a Learner.

<!-- sample: quickstart -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.param.LearnerListParams;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .build();

Learner learner = client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
System.out.println(learner.getName() + " " + learner.getStatus());

LearnerListParams inProgress =
    LearnerListParams.builder().status(LearnerStatus.IN_PROGRESS).limit(20).build();
for (Learner item : client.learners().list(inProgress).autoPagingIterable()) {
  System.out.println(item.getEmail());
}
```

Every method that sends a request also has an `…Async` form that returns a
`CompletableFuture` without blocking the calling thread:

<!-- sample: quickstart-async -->
```java
client
    .learners()
    .retrieveAsync("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG")
    .thenAccept(found -> System.out.println(found.getEmail()))
    .join();
```

## Design at a glance

| Concern | Behavior |
| --- | --- |
| Construction | `TeachMeHipaaClient.builder()`; every setting is fixed at `build()` |
| Thread safety | One client serves any number of threads. Builders are not thread-safe. |
| Lifecycle | The client is not `AutoCloseable` and holds nothing to close. `BinaryStreamResponse` is, so read a streamed PDF in try-with-resources. |
| Method shape | Path ids, then a `…Params` object from its builder, then optional `RequestOptions` |
| Exceptions | All unchecked: `TeachMeHipaaException` and its subclasses, by HTTP status |
| Retries | Network failures, 409 `conflict`, 429 and 5xx; twice by default, 0.5 s doubling to 5 s, `Retry-After` honored up to 60 s |
| Timeouts | 80 s per attempt by default |
| Async | `…Async` siblings return `CompletableFuture<T>` of the blocking method's `T` |
| Logging | Off unless you pass a `RequestLogger`; never logs bodies, headers or credentials |

## Documentation

The guides are written for engineers wiring the API into a JVM service. Every
Java block in them compiles against the library and runs against a fake of
the API in CI.

| Guide | Covers |
| --- | --- |
| [Installation](docs/guides/installation.md) | Maven and Gradle, the Jackson dependency, building from source |
| [Getting started](docs/guides/getting-started.md) | Credentials, a first call, inviting a Learner, a first `catch` |
| [The client](docs/guides/client.md) | Builder options, lifecycle, thread safety, per-call options, logging, a custom `HttpClient` |
| [Authentication](docs/guides/authentication.md) | Client credentials, Scopes, Environments, the Organization binding, token providers |
| [Exceptions and retries](docs/guides/exceptions.md) | The exception hierarchy, problem details, retry rules, idempotency keys |
| [Async with CompletableFuture](docs/guides/async.md) | The `…Async` methods, their threads, failures, cancellation, pages and Operations |
| [Pagination and the Event log](docs/guides/pagination.md) | `Page`, auto-paging iterables and streams, reading and tailing Events |
| [Webhooks](docs/guides/webhooks.md) | Subscriptions, signature verification in Spring, servlets and the JDK server |
| [Operations and files](docs/guides/operations-and-files.md) | Long-running Operations, CSV export and import, PDFs, bulk changes |
| [Optimistic concurrency](docs/guides/optimistic-concurrency.md) | ETags, `If-Match`, merge patches, `retrieveIfModified` |
| [Testing](docs/guides/testing.md) | The Sandbox, a fake `HttpTransport`, mock servers, webhook handler tests |
| [Compatibility](docs/guides/compatibility.md) | Semantic versioning, supported JDKs, open enums and forward-compatible models |

Reference material:

- [Resource reference](docs/reference/resources.md): every service method and
  overload, its HTTP route, Scopes and retry behavior. Generated.
- [Javadoc](https://teachmehipaa.github.io/teachmehipaa-java/): every public
  type.
- [Examples](examples/README.md): runnable programs for the Sandbox.
- [Documentation index](docs/README.md), with the design records.

## Support and security

<!-- TODO(Nick): confirm the support address before launch. -->

Quote the request id when you contact TeachMeHIPAA support. An exception
carries it in `getRequestId()`; a returned object in
`getLastResponse().requestId()`. Report vulnerabilities privately, as
[SECURITY.md](SECURITY.md) describes.

The library sends no telemetry and writes nothing to disk. Changes are
recorded in the [changelog](CHANGELOG.md);
[CONTRIBUTING.md](CONTRIBUTING.md) explains how to build and test it.
Licensed under the [MIT License](LICENSE).
