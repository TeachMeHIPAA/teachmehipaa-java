# TeachMeHIPAA Java Library

The official Java library for the TeachMeHIPAA Developer API.

[![CI](https://github.com/TeachMeHIPAA/teachmehipaa-java/actions/workflows/ci.yml/badge.svg)](https://github.com/TeachMeHIPAA/teachmehipaa-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> [!IMPORTANT]
> **Status: pre-release.** The TeachMeHIPAA Developer API isn't generally
> available yet, and this library isn't published to Maven Central. Until it
> is, build it from source and install it into your local Maven repository,
> as shown below. Names and behavior may still change before 1.0.

Enroll your people in HIPAA training and track their compliance from your own
systems. The library handles Access tokens, the Organization binding,
retries, idempotency keys, typed exceptions, pagination, ETags, Operations and
webhook verification, so your code only calls methods.

## Installation

Build the library from a checkout and install it into `~/.m2`:

```sh
git clone https://github.com/TeachMeHIPAA/teachmehipaa-java.git
cd teachmehipaa-java
./gradlew publishToMavenLocal
```

Then depend on it from your project, with `mavenLocal()` among your
repositories. Gradle (Kotlin DSL):

```kotlin
repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT")
}
```

Maven:

```xml
<dependency>
  <groupId>com.teachmehipaa</groupId>
  <artifactId>teachmehipaa-java</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The library needs Java 17 or newer and depends only on Jackson. It sends
requests with the JDK's own `java.net.http.HttpClient`.

## Quickstart

Create an API client in the TeachMeHIPAA console to get a client id (`cli_…`)
and secret (`tmh_cs_…`). Start in the Sandbox Environment, where no real
Learners are emailed.

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

Build one client and share it: it is thread-safe, gets and renews Access
tokens itself, and retries failures that are safe to retry. Services hang off
it (`client.learners()`, `client.enrollments()`); methods take ids first, then
a `…Params` object built with a builder, then optional `RequestOptions`.
Exceptions are unchecked. [Getting started](docs/guides/getting-started.md)
walks through your first integration.

## Documentation

| Topic                                                                | What it covers                                                                   |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| [Getting started](docs/guides/getting-started.md)                    | Install, credentials, first calls, inviting a Learner                            |
| [Authentication](docs/guides/authentication.md)                      | Client credentials, Scopes, Environments, the Organization binding, public calls |
| [Errors and retries](docs/guides/errors-and-retries.md)              | Exceptions, `getCode()`, request ids, retries, idempotency, request options      |
| [Pagination and the Event log](docs/guides/pagination-and-events.md) | Auto-paging, the Event log, tailing it                                           |
| [Webhooks](docs/guides/webhooks.md)                                  | Webhook subscriptions, verifying Deliveries, Test deliveries                     |
| [Operations and files](docs/guides/operations-and-files.md)          | Waiting on Operations, CSV exports and imports, Certificate PDFs, bulk changes   |
| [Conditional requests](docs/guides/conditional-requests.md)          | ETags, `ifMatch`, `retrieveIfModified`, merge-patch updates                      |
| [Testing](docs/guides/testing.md)                                    | The Sandbox, faking the transport, signing test Deliveries                       |
| [Configuration](docs/guides/configuration.md)                        | Every builder option, logging, timeouts, a custom HTTP client                    |
| [Versioning](docs/guides/versioning.md)                              | Semantic versioning, `OPENAPI_VERSION`, additive API changes                     |

- [Resource reference](docs/reference/resources.md): every service and
  method, with its overloads, HTTP route, Scopes and return type.
- [Javadoc](https://teachmehipaa.github.io/teachmehipaa-java/): every public
  class and method.
- [Examples](examples/README.md): small programs to run against the Sandbox.
- [All documentation](docs/README.md), including the design records.

## Support and security

<!-- TODO(Nick): confirm the support address before launch. -->

For help, contact TeachMeHIPAA support and quote the request id from the
exception (`getRequestId()`) or from `getLastResponse().requestId()`. To
report a vulnerability, see [SECURITY.md](SECURITY.md); please don't open a
public issue.

The library sends no telemetry and never logs request or response bodies. See
the [changelog](CHANGELOG.md) for what changed, and
[CONTRIBUTING.md](CONTRIBUTING.md) to work on the library. It is released
under the [MIT License](LICENSE).
