# Getting started

This guide takes you from nothing to inviting your first Learner in the
Sandbox Environment. It takes about ten minutes.

## 1. Add the library

The Developer API is in pre-release and the library isn't on Maven Central
yet, so build it from source and install it into your local Maven repository
(`~/.m2`):

```sh
git clone https://github.com/TeachMeHIPAA/teachmehipaa-java.git
cd teachmehipaa-java
./gradlew publishToMavenLocal   # installs com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT
```

Then add `mavenLocal()` and the dependency to your project. With Gradle
(Kotlin DSL):

```kotlin
repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("com.teachmehipaa:teachmehipaa-java:0.1.0-SNAPSHOT")
}
```

With Maven, which reads `~/.m2` already:

```xml
<dependency>
  <groupId>com.teachmehipaa</groupId>
  <artifactId>teachmehipaa-java</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

You need Java 17 or newer. The library's only dependency is Jackson, and it
sends requests with the JDK's own `java.net.http.HttpClient`.

## 2. Get Sandbox credentials

An Organization admin creates an **API client** in the TeachMeHIPAA console.
It has a client id (`cli_…`) and a client secret (`tmh_cs_…`), and belongs to
exactly one Organization. Create it in the Sandbox Environment first: Sandbox
Learners are never emailed, so you can experiment freely.

Keep the secret on your server. Put both values in the environment:

```sh
export TMH_CLIENT_ID=cli_...
export TMH_CLIENT_SECRET=tmh_cs_...
```

## 3. Make your first call

Build one client and share it for the life of your process: it is
thread-safe. It fetches an Access token on the first call and renews it
before it expires.

<!-- sample: getting-started-first-call -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.ComplianceSummary;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .build();

ComplianceSummary summary = client.compliance().retrieve();
System.out.println(
    summary.getTotals().getCompliant() + " of " + summary.getTotals().getLearners() + " compliant");
```

You never pass an Organization id: the client asks the API which Organization
your API client belongs to, once, and remembers it. See
[Authentication](authentication.md) for the details.

## 4. Invite a Learner

Samples from here on use a `client` built as above.

`learners().create` invites someone to a Course by email. The result's
`isCreated()` says whether this call created the Learner or found an existing
one.

<!-- sample: getting-started-invite -->
```java
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.param.LearnerCreateParams;

LearnerWriteResult learner =
    client
        .learners()
        .create(
            LearnerCreateParams.builder()
                .email("ana@example.com")
                .name("Ana Example")
                .courseId("hipaa-2026")
                .build());
System.out.println(learner.getLearnerId() + " " + (learner.isCreated() ? "invited" : "already there"));
```

If you sync people from another system, prefer
`learners().upsertByExternalId`, keyed by your own id for the person, their
External ID. It creates and invites the Learner the first time and returns
them every time after, so a sync job can replay it safely:

<!-- sample: getting-started-upsert -->
```java
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.param.LearnerUpsertByExternalIdParams;

LearnerWriteResult synced =
    client
        .learners()
        .upsertByExternalId(
            "hr-123",
            LearnerUpsertByExternalIdParams.builder()
                .email("ana@example.com")
                .name("Ana Example")
                .build());
System.out.println(synced.getLearnerId() + " " + synced.isCreated());
```

The [roster sync example](../../examples/SyncRoster.java) shows it end to
end.

## 5. Handle failures

A failed call throws a subclass of `TeachMeHipaaException`, which is
unchecked. Catch the class you can handle, or branch on its `getCode()`, and
log the `getRequestId()` so support can find the request. Here the
`REQUIRE_AVAILABLE` Seat policy makes the create fail with a 409 when no Seat
is free:

<!-- sample: getting-started-errors -->
```java
import com.teachmehipaa.exception.ConflictException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.LearnerCreateRequestSeatPolicy;
import com.teachmehipaa.param.LearnerCreateParams;

try {
  client
      .learners()
      .create(
          LearnerCreateParams.builder()
              .email("ana@example.com")
              .seatPolicy(LearnerCreateRequestSeatPolicy.REQUIRE_AVAILABLE)
              .build());
} catch (ConflictException e) {
  if ("seat_unavailable".equals(e.getCode())) {
    System.out.println("No Seat is free for this Learner");
  } else {
    System.out.println(e.getCode() + " " + e.getRequestId());
  }
} catch (TeachMeHipaaException e) {
  System.out.println(e.getCode() + " " + e.getRequestId());
}
```

## Next steps

- [Pagination and the Event log](pagination-and-events.md) to read lists and
  follow what changes.
- [Webhooks](webhooks.md) to be told when a Learner is certified.
- [Errors and retries](errors-and-retries.md) for every exception and how
  the client retries.
- The [resource reference](../reference/resources.md) for every method.
