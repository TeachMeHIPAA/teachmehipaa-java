# Compatibility

## Semantic versioning

Releases follow [Semantic Versioning 2.0.0](https://semver.org/). Within a
major version, code that compiled and ran against an earlier release
compiles and runs against a later one:

| Release | May contain |
| --- | --- |
| Patch (1.0.x) | Bug fixes |
| Minor (1.x.0) | New methods, overloads, builder options, model fields, Event types and enum constants |
| Major (x.0.0) | Removals and renames, each with a migration note in the [changelog](../../CHANGELOG.md) |

Until 1.0.0 the library is pre-release, and any snapshot may change its
interface.

That promise covers the interfaces you implement: `HttpTransport`,
`TokenProvider`, `Sleeper`, `AsyncSleeper` and `RequestLogger`. A minor
release adds methods to them only as `default` methods; `sendAsync` and
`accessTokenAsync` arrived that way.

## Supported JDKs

| JDK | Status |
| --- | --- |
| 17 | Supported; the bytecode target (`--release 17`); tested in CI |
| 21 | Supported; tested in CI |
| Any other JDK 17 or later | Not tested in CI |

## API release

Services, models and params are generated from the Developer API's OpenAPI
document through a shared SDK IR. `OPENAPI_VERSION` at the repository root
names the document release a library version was generated from, and the
changelog repeats it for each release. A method the API gains after that
release arrives in a later library version.

## Forward-compatible models

The Developer API changes by addition: new endpoints, optional fields, enum
values, error codes and Event types. The library reads all of them without an
upgrade:

| API addition | What this library version does |
| --- | --- |
| A new response field | Keeps it in the object's `getExtraFields()`, keyed by JSON name |
| A new enum value | Reads it: enums are open classes, and `isKnown()` returns `false` for values this version lacks a constant for |
| A new Event type | Reads it as `UnknownEvent` with the raw `getEventType()` and `getData()` |
| A new variant of another union, such as an Audit event | Reads it as the union's `Unknown…` class |
| A new error code | Passes it through `getCode()`; the exception class still follows the HTTP status |

Branches on an enum, an Event type or an error code therefore need a final
`else`:

<!-- sample: versioning-open-enums -->
```java
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerStatus;

Learner learner = client.learners().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
LearnerStatus status = learner.getStatus();
if (status.equals(LearnerStatus.COMPLIANT) || status.equals(LearnerStatus.EXPIRING_SOON)) {
  System.out.println("trained");
} else if (status.equals(LearnerStatus.NOT_STARTED) || status.equals(LearnerStatus.IN_PROGRESS)) {
  System.out.println("in training");
} else {
  System.out.println("needs attention: " + status.value() + (status.isKnown() ? "" : " (new)"));
}
```

`client` is built as in [Getting started](getting-started.md). Compare open
enums with `equals`, never `==`: two reads of an unknown value are equal but
are different instances. `value()` returns the wire value.
