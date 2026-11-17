# Documentation

The API reference and guides for the TeachMeHIPAA Java library live at
[docs.teachmehipaa.com/api](https://docs.teachmehipaa.com/api?lang=java).
The [README](../README.md) covers install and a first call.

## Code samples

[`code-samples.json`](reference/code-samples.json) holds one Java snippet
per Developer API operation, generated from the SDK IR by
`./gradlew generate`. The hosted API reference shows them when you pick
Java.

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

- [CONTRIBUTING.md](../CONTRIBUTING.md): building, the generator and the
  test suites.
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
