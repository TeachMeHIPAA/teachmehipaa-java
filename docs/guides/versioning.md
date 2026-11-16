# Versioning

## Semantic versioning

The library follows [semantic versioning](https://semver.org/). Within one
major version, upgrading never breaks code that compiled and ran against an
earlier minor or patch release:

- **Patch** releases fix bugs.
- **Minor** releases add methods, options, fields, Event types and enum
  values.
- **Major** releases may remove or rename things, and say how to migrate in
  the [changelog](../../CHANGELOG.md).

Until 1.0.0, the library is pre-release and any release may change its
interface.

## Which API version a release covers

Services, models and params are generated from the Developer API's OpenAPI
document. The `OPENAPI_VERSION` file at the root of the repository names the
document release a library version was generated from, and the changelog
repeats it for each release. A method or field the API added after that
release appears in a later library version.

## Additive API changes

The Developer API grows by addition: new endpoints, new optional fields, new
enum values, new error codes and new Event types. The library is built so
that none of these break you, even before you upgrade:

- **Unknown response fields are kept.** A field the API added is in the
  object's `getExtraFields()`, keyed by its JSON name, until a version of the
  library adds a getter for it.
- **Enums are open.** An enum such as `LearnerStatus` is a class with a
  constant for each value this version knows, and reads any other value the
  API sends too: `isKnown()` tells them apart, and `value()` is the wire
  value.
- **Unknown Event types parse.** An Event of a type this version doesn't know
  arrives as an `UnknownEvent`, with its `getEventType()` and `getData()` as
  sent. Unknown variants of other unions, such as Audit events and Test
  delivery results, have an `Unknown…` fallback too.
- **New error codes reach you.** An exception's `getCode()` is whatever the
  API sent, and the exception class still follows the HTTP status.

So always keep a fallback when you branch on an enum, an Event type or an
error code:

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

Here `client` is built as in [Getting started](getting-started.md). Compare
open enums with `equals`, not `==`: two reads of a value this version doesn't
know are equal, but not the same object.

## Supported Java versions

The library supports Java 17 and newer, and is compiled with `--release 17`.
CI tests it on Java 17 and 21.
