# teachmehipaa-java

Official Java library for the TeachMeHIPAA Developer API.

[![CI](https://github.com/TeachMeHIPAA/teachmehipaa-java/actions/workflows/ci.yml/badge.svg)](https://github.com/TeachMeHIPAA/teachmehipaa-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Documentation

**[docs.teachmehipaa.com/api](https://docs.teachmehipaa.com/api?lang=java)**
has the API reference with Java samples for every operation, plus guides for
authentication, pagination, errors and webhooks.

## Install

> [!IMPORTANT]
> **Pre-release.** The Developer API is not generally available, and no
> artifact is published to Maven Central. Until 1.0.0, install the library
> from this repository into your local Maven repository. Class and method
> names can change between snapshots.

```sh
git clone https://github.com/TeachMeHIPAA/teachmehipaa-java.git
cd teachmehipaa-java
./gradlew publishToMavenLocal
```

Then depend on `com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT`. Maven
reads `~/.m2` without extra configuration; Gradle needs `mavenLocal()` in
`repositories`.

## Requirements

Java 17 or later. The only runtime dependency is Jackson 2.22
(`jackson-databind`, `jackson-annotations`, `jackson-datatype-jsr310`); HTTP
goes through the JDK's `java.net.http.HttpClient`.

## Usage

<!-- sample: quickstart -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .build();

var learner = client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
System.out.println(learner.getName() + " " + learner.getStatus());
```

The client id (`cli_…`) and secret (`tmh_cs_…`) come from an API client you
create in the TeachMeHIPAA console. Use Sandbox credentials
(`https://sandbox.api.tme.so/v1`) while you build. [`examples/`](examples/README.md)
holds runnable programs.

## Support

<!-- TODO(Nick): confirm the support address before launch. -->

Quote the request id when you contact TeachMeHIPAA support. An exception
carries it in `getRequestId()`; a returned object in
`getLastResponse().requestId()`. Changes are recorded in the
[changelog](CHANGELOG.md).

## Security

Report vulnerabilities privately, as [SECURITY.md](SECURITY.md) describes.

## Contributing

[CONTRIBUTING.md](CONTRIBUTING.md) explains how to build and test the
library.

## License

Licensed under the [MIT License](LICENSE).
