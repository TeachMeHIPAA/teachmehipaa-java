# teachmehipaa-java

The official Java library for the TeachMeHIPAA Developer API. Java 17+.

> Not yet published. The Developer API ships dark; nothing is released to Maven
> Central until it goes live.

## Quickstart

```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Learner;

TeachMeHipaaClient client = TeachMeHipaaClient.builder()
    .clientId(System.getenv("TMH_CLIENT_ID"))
    .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
    .environment(Environment.SANDBOX)
    .build();

try {
  Learner learner = client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
  System.out.println(learner.getStatus());
  System.out.println(learner.getLastResponse().etag());
} catch (TeachMeHipaaException e) {
  System.err.println(e.getStatusCode() + " " + e.getCode() + " " + e.getRequestId());
}
```

The client mints and renews Access tokens itself and discovers the
Organization your API client is bound to. Pass `.organizationId(...)` to skip
that lookup. Build one client and share it; it is thread-safe and its settings
are fixed once built.

## Development

```sh
./gradlew spotlessApply   # format
./gradlew spotlessCheck build   # what CI runs: format check, compile, tests, javadoc
```

Layout under `src/main/java/com/teachmehipaa/`:

| Package | Contents |
|---|---|
| `com.teachmehipaa` | `TeachMeHipaaClient`, `Environment`, `AppInfo`, `RequestOptions` |
| `core` | Hand-written core: transport seam, token providers, request pipeline, model bases |
| `exception` | `TeachMeHipaaException` and its subclasses |
| `service` | One service per resource (`LearnerService`) |
| `model` | Response models (`Learner`) and open enums |
| `param` | Request parameter builders (`LearnerRetrieveParams`) |

`service`, `model` and `param` will be generated from `openapi/sdk-ir.json`
(ADR-0001); the `Learner` retrieve there is hand-written until the generator
lands.
