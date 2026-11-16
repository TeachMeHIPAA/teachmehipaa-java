# Getting started

The steps below take a JVM service from no dependency to an invited Learner
in the Sandbox. They assume the library is on your classpath; if it is not,
see [Installation](installation.md).

## Credentials

An Organization admin creates an **API client** in the TeachMeHIPAA console.
An API client has a client id (`cli_…`) and a client secret (`tmh_cs_…`) and
belongs to exactly one Organization and one Environment. Create the first one
in the **Sandbox**: it accepts the same calls as Live and never emails a
Learner.

Supply both values from your secret store or the process environment. The
samples read environment variables:

```sh
export TMH_CLIENT_ID=cli_...
export TMH_CLIENT_SECRET=tmh_cs_...
```

## Build the client and make a call

`TeachMeHipaaClient` is built once and shared: it is immutable and
thread-safe, and it caches the Access token it mints. A Spring application
registers it as a singleton `@Bean`; a plain service keeps it in a `static
final` field or passes it to the objects that need it.

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

The client fetched an Access token, asked the API which Organization the
token belongs to and then read the summary: three requests, of which only the
last repeats on later calls. [Authentication](authentication.md) explains
the token cache and the Organization binding.

## Invite a Learner

The remaining samples use the `client` built above.

`learners().create` takes a `LearnerCreateParams`, built with its builder.
`build()` throws `IllegalStateException` when a required field such as
`email` is missing, before anything reaches the network. The returned
`LearnerWriteResult` reports through `isCreated()` whether this call created
the Learner (201) or found an existing one (200).

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

A job that mirrors an HR system calls `learners().upsertByExternalId`
instead. The External ID is your own identifier for the person. The first
call creates and invites the Learner; every later call with the same External
ID returns the same Learner, so a nightly job can run it again without
creating duplicates.

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

[`SyncRoster.java`](../../examples/SyncRoster.java) runs a complete roster
sync this way.

## Catch a failure

Every exception the library throws for a failed call extends
`TeachMeHipaaException`, an unchecked exception. Catch the most specific
subclass you can act on, branch on `getCode()` within it, and log
`getRequestId()`: support needs it to find the request. With the
`REQUIRE_AVAILABLE` Seat policy, `create` fails with 409 `seat_unavailable`
when the Organization has no free Seat:

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

## Where to go next

| Task | Guide |
| --- | --- |
| Tune timeouts, retries, logging or the HTTP client | [The client](client.md) |
| Map every exception to an HTTP status | [Exceptions and retries](exceptions.md) |
| Call the API from reactive or async code | [Async with CompletableFuture](async.md) |
| Read every Learner, or follow the Event log | [Pagination and the Event log](pagination.md) |
| Receive `learner.certified` as it happens | [Webhooks](webhooks.md) |
| Look up a method's route, Scopes and overloads | [Resource reference](../reference/resources.md) |
