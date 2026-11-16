# Installation

## Requirements

| Item | Requirement |
| --- | --- |
| JDK | 17 or later. The jar targets Java 17 bytecode (`--release 17`). CI builds and tests on Temurin 17 and 21. |
| Build tool | Any tool that resolves Maven artifacts |
| Network | Outbound HTTPS to `api.tme.so` (Live) or `api-teachmehipaa-dev.web.app` (Sandbox) |

The library runs server-side. A client secret authenticates as your whole
Organization, so it never belongs in a browser, an Android app or any other
code you ship to users.

## Pre-release distribution

The Developer API is in pre-release, and Maven Central has no
`teachmehipaa-java` artifact yet. Until 1.0.0 ships, you build the jar from
this repository and install it into your local Maven repository:

```sh
git clone https://github.com/TeachMeHIPAA/teachmehipaa-java.git
cd teachmehipaa-java
./gradlew publishToMavenLocal
```

The Gradle wrapper (Gradle 9.9) downloads itself. The command installs
`com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT` into `~/.m2/repository`,
with its sources and Javadoc jars. A CI pipeline that consumes the snapshot
runs the same three commands before its own build.

## Declaring the dependency

<table>
<tr><th>Maven</th><th>Gradle, Kotlin DSL</th><th>Gradle, Groovy DSL</th></tr>
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
  implementation(
    "com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT")
}
```

</td>
<td>

```groovy
repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation 'com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT'
}
```

</td>
</tr>
</table>

Maven consults `~/.m2` for every build. Gradle consults it only when
`mavenLocal()` is listed; put it first so the snapshot resolves there and
everything else from Maven Central.

## Transitive dependencies

| Artifact | Scope in the published POM | Why |
| --- | --- | --- |
| `com.fasterxml.jackson.core:jackson-databind` | `compile` | Models expose `JsonNode`, for example `Operation.getResponse()` |
| `com.fasterxml.jackson.core:jackson-annotations` | `compile` | Models carry Jackson annotations |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | `runtime` | Reads `OffsetDateTime` fields |

The versions come from `jackson-bom` 2.22.4, which the POM imports in
`<dependencyManagement>`. Under Maven, your project's own dependency
management overrides it, so a Spring Boot BOM that pins Jackson 2.x decides
the version. Gradle resolves the highest version any module requests, so it
resolves 2.22.4 or later unless you declare a strict version. CI tests only
2.22.4. Jackson 3 moved to new package names and is not supported.

The library uses its own `ObjectMapper`. Configuring your application's mapper
changes nothing in how API responses are read.

## Verifying the install

`Quickstart` in [`examples/`](../../examples/README.md) prints the Sandbox
Compliance summary and five Learners:

```sh
export TMH_CLIENT_ID=cli_...
export TMH_CLIENT_SECRET=tmh_cs_...
./gradlew runExample -Pexample=Quickstart
```

Continue with [Getting started](getting-started.md).
