# Documentation

Documentation for `teachmehipaa-java`, the Java library for the TeachMeHIPAA
Developer API. New here? Start with
[Getting started](guides/getting-started.md).

## Guides

Every Java sample in these guides is compiled against the library and run
against a fake of the API in CI, so the samples work as written.

1. [Getting started](guides/getting-started.md): install, credentials, first
   calls, inviting a Learner.
2. [Authentication](guides/authentication.md): client credentials, Scopes,
   Environments (Live and Sandbox), the Organization binding, static Access
   tokens and public lookups without credentials.
3. [Errors and retries](guides/errors-and-retries.md): exceptions,
   `getCode()`, request ids, automatic retries, idempotency keys and request
   options.
4. [Pagination and the Event log](guides/pagination-and-events.md):
   auto-paging lists, reading the Event log and tailing it.
5. [Webhooks](guides/webhooks.md): Webhook subscriptions, verifying
   Deliveries with the raw body and exact path, Spring Boot, servlet and JDK
   HTTP server handlers, Test deliveries and signing your own test
   Deliveries.
6. [Operations and files](guides/operations-and-files.md): waiting on
   Operations, CSV exports and imports, Certificate PDFs and bulk changes.
7. [Conditional requests](guides/conditional-requests.md): ETags, `ifMatch`,
   `retrieveIfModified` and merge-patch updates.
8. [Testing](guides/testing.md): the Sandbox, injecting a fake
   `HttpTransport`, mock servers and testing webhook handlers.
9. [Configuration](guides/configuration.md): every builder option and its
   default, timeouts, a custom HTTP client, logging and requirements.
10. [Versioning](guides/versioning.md): semantic versioning,
    `OPENAPI_VERSION` and how additive API changes reach you.

## API reference

- [Resource reference](reference/resources.md): every service and method,
  with its overloads, HTTP route, required Scopes, retry, idempotency and
  conditional behavior, and return type. Generated from the Developer API's
  SDK IR by `./gradlew generate`.
- [Javadoc](https://teachmehipaa.github.io/teachmehipaa-java/): every public
  class, method and builder. Build it locally with `./gradlew javadoc`; it
  lands in `build/docs/javadoc/`.

## Examples

Runnable programs in [`examples/`](../examples/README.md), each reading
`TMH_CLIENT_ID` and `TMH_CLIENT_SECRET` and targeting the Sandbox. Run one
with `./gradlew runExample -Pexample=<Name>`:

- [`Quickstart.java`](../examples/Quickstart.java): the Compliance summary and
  a few Learners.
- [`SyncRoster.java`](../examples/SyncRoster.java): sync a roster with
  `upsertByExternalId`.
- [`WebhookServer.java`](../examples/WebhookServer.java): a webhook endpoint
  on the JDK's HTTP server that verifies Deliveries.
- [`ExportCertificates.java`](../examples/ExportCertificates.java): export
  Certificates to CSV and wait for the Operation.

## Contributing and design records

These explain how the library is built and why. You don't need them to use
it.

- [CONTRIBUTING.md](../CONTRIBUTING.md): setup, generated code, tests and
  how the documentation is built.
- [`adr/`](adr/): architecture decision records, such as
  [generated surface from a shared IR](adr/0001-generated-surface-from-shared-ir.md),
  [the Organization bound to the client](adr/0003-organization-bound-to-client.md),
  [webhook verification needs the path](adr/0005-webhook-verification-needs-the-path.md),
  [tolerant models and open enums](adr/0006-tolerant-models-open-enums.md),
  [no telemetry, no body logging](adr/0007-no-telemetry-no-body-logging.md)
  and [Java 17 with unchecked exceptions and Jackson](adr/0008-java17-unchecked-jackson.md).
  Superseded records stay, marked as such.
- [`design/`](design/grilling-2026-11-15.md): the design review that settled
  the library's interface.
- [`research/`](research/stripe-java.md): research into Stripe's Java
  library, whose conventions this library follows where they fit.
- [`agents/`](agents/domain.md): conventions for coding agents working on
  this repository: domain docs, the issue tracker and triage labels.
- [GLOSSARY.md](../GLOSSARY.md): the terms these docs use, such as Learner,
  Enrollment and Delivery.
