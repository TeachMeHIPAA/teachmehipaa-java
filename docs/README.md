# teachmehipaa-java documentation

Reference documentation for `com.teachmehipaa:teachmehipaa-java`, the Java 17+
client for the TeachMeHIPAA Developer API. A first integration reads
[Installation](guides/installation.md), then
[Getting started](guides/getting-started.md).

## Guides

Each Java block in the guides is compiled against the library alone and run
against a fake of the API by `./gradlew test`, so the samples match the
current code.

| # | Guide | Contents |
| --- | --- | --- |
| 1 | [Installation](guides/installation.md) | Requirements, Maven and Gradle declarations, transitive Jackson, building the pre-release from source |
| 2 | [Getting started](guides/getting-started.md) | Sandbox credentials, the first call, inviting and upserting a Learner, the first `catch` |
| 3 | [The client](guides/client.md) | Every builder option and default, lifecycle, thread-safety guarantees, `RequestOptions`, `AppInfo`, a custom `HttpClient`, logging |
| 4 | [Authentication](guides/authentication.md) | Client credentials and the token cache, Scopes, Environments, the Organization binding, `TokenProvider`, public lookups |
| 5 | [Exceptions and retries](guides/exceptions.md) | Checked versus unchecked, the exception hierarchy, problem details, retry rules, idempotency keys |
| 6 | [Async with CompletableFuture](guides/async.md) | `…Async` methods, which threads run what, failures, cancellation and `orTimeout`, async pages and Operations |
| 7 | [Pagination and the Event log](guides/pagination.md) | `Page<T>`, auto-paging iterables and streams, the Event log, `EventTail` |
| 8 | [Webhooks](guides/webhooks.md) | Subscriptions, signature verification in Spring MVC, Jakarta Servlet and `com.sun.net.httpserver`, Test deliveries |
| 9 | [Operations and files](guides/operations-and-files.md) | Waiting on and resuming Operations, CSV export and import, streamed PDFs, bulk changes |
| 10 | [Optimistic concurrency](guides/optimistic-concurrency.md) | ETags, `ifMatch`, merge patches, `retrieveIfModified` |
| 11 | [Testing](guides/testing.md) | The Sandbox, a fake `HttpTransport`, Prism, webhook handler tests |
| 12 | [Compatibility](guides/compatibility.md) | Semantic versioning, supported JDKs, `OPENAPI_VERSION`, forward-compatible models |

## API reference

- [Resource reference](reference/resources.md): each service method with its
  overloads (blocking and async), HTTP route, Scopes, retry, idempotency and
  conditional behavior. `./gradlew generate` writes it from the SDK IR.
- [`code-samples.json`](reference/code-samples.json): one Java snippet per
  Developer API operation, generated alongside it for the TeachMeHIPAA API
  reference site.
- [Javadoc](https://teachmehipaa.github.io/teachmehipaa-java/): every public
  type. `./gradlew javadoc` builds it into `build/docs/javadoc/`.

## Examples

[`examples/`](../examples/README.md) holds runnable programs that read
`TMH_CLIENT_ID` and `TMH_CLIENT_SECRET` and call the Sandbox. Run one with
`./gradlew runExample -Pexample=<Name>`.

| Program | Demonstrates |
| --- | --- |
| [`Quickstart.java`](../examples/Quickstart.java) | The Compliance summary and the first Learners |
| [`SyncRoster.java`](../examples/SyncRoster.java) | A roster sync with `upsertByExternalId` |
| [`WebhookServer.java`](../examples/WebhookServer.java) | A verifying webhook endpoint on the JDK's HTTP server |
| [`ExportCertificates.java`](../examples/ExportCertificates.java) | A Certificate export, waiting on its Operation |

## Design records and contributing

Background on how the library is built. Using it requires none of this.

- [CONTRIBUTING.md](../CONTRIBUTING.md): building, the generator, the test
  suites and how the documentation is checked.
- [`adr/`](adr/): architecture decision records, among them
  [the generated surface](adr/0001-generated-surface-from-shared-ir.md),
  [the Organization bound to the client](adr/0003-organization-bound-to-client.md),
  [webhook verification needs the path](adr/0005-webhook-verification-needs-the-path.md),
  [tolerant models and open enums](adr/0006-tolerant-models-open-enums.md),
  [no telemetry, no body logging](adr/0007-no-telemetry-no-body-logging.md)
  and [Java 17, unchecked exceptions and Jackson](adr/0008-java17-unchecked-jackson.md).
  Superseded records remain, marked as superseded.
- [`design/`](design/grilling-2026-11-15.md): the design review that fixed the
  public interface.
- [`research/`](research/stripe-java.md): the study of `stripe-java` whose
  conventions this library follows where they fit.
- [`agents/`](agents/domain.md): conventions for coding agents: domain docs,
  the issue tracker, triage labels.
- [GLOSSARY.md](../GLOSSARY.md): Learner, Enrollment, Delivery and the other
  domain terms.
