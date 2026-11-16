# Changelog

All notable changes to this library are documented here. It follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and
[semantic versioning](https://semver.org/).

## [Unreleased]

The first release, to ship as 1.0.0 when the Developer API launches.
Generated from the Developer API document release `trimmed-2026-11-15`.

### Added

- `TeachMeHipaaClient`, covering all 44 Developer API operations as services,
  models and params generated from the shared SDK IR: Learners, Enrollments,
  Certificates, Certificate verifications, Compliance, Courses, Members,
  Seats, Audit events, Webhook subscriptions, the Event log, Operations and
  `auth().introspect()`. One immutable, thread-safe client per API client,
  built with `TeachMeHipaaClient.builder()`.
- An `…Async` sibling of every method that sends a request, returning a
  `CompletableFuture` of the same type, sent with `HttpClient.sendAsync`.
  Retries and Operation polls wait on a timer instead of a thread
  (`AsyncSleeper`), concurrent calls share one Access token mint, failures
  complete the future with the same exception subclass as the blocking call,
  and cancelling stops retries and polls. `Page.nextPageAsync()` and
  `Operation.waitForCompletionAsync()` round it out
  ([#2](https://github.com/TeachMeHIPAA/teachmehipaa-java/issues/2)).
- Client credentials authentication with cached, single-flight Access tokens
  renewed 60 s before expiry, and one renewal and replay on a 401.
- Live and Sandbox Environments, a `baseUrl` override, narrower `scopes`, and
  `StaticTokenProvider` for Access tokens obtained elsewhere. Public methods
  (`courses()`, `certificateVerifications().retrieve`) send no token, and a
  client built without credentials can call them.
- The Organization binding: discovered once through `auth().introspect()`, or
  given as `organizationId`.
- Unchecked exceptions by status (`InvalidRequestException`,
  `AuthenticationException`, `PermissionDeniedException`,
  `NotFoundException`, `ConflictException`, `PreconditionFailedException`,
  `IdempotencyException`, `RateLimitException`, `ApiException`,
  `ApiConnectionException`, `ApiTimeoutException`, `OAuthException`), all
  `TeachMeHipaaException`s carrying the problem `getCode()`, `getDetail()`,
  field `getErrors()`, `getHeaders()` and `getRequestId()`, with a guard
  that throws `ApiException` for non-JSON responses instead of a parse error.
- Retries with exponential backoff and jitter for network errors, 409
  `conflict`, 429 and 5xx, honoring `Retry-After`, only for calls that are
  safe to replay; a generated `Idempotency-Key` per create and custom method,
  reused across retries; and `RequestOptions` per call for the
  `idempotencyKey`, `maxRetries`, `timeout` and `requestId`
  (`x-request-id`).
- `getLastResponse()` on every returned object, with status, headers,
  request id and ETag.
- `Page<T>` with `items()`, `nextCursor()`, `hasNextPage()`, `nextPage()`,
  `pages()`, and lazy `autoPagingIterable()` and `autoPagingStream()` across
  every page.
- Conditional writes and reads with ETags: `ifMatch` (an ETag or the object
  read), required where the API requires it, and `retrieveIfModified`
  returning an empty `Optional` on 304. Merge-patch updates with explicit
  `clearX()` methods to send `null`.
- `Operation.waitForCompletion()` with backoff and `getExportResult()` for
  export CSVs; `WebhookDeliveryOperation.getDeliveryResult()` for Test
  deliveries and redrives, typed by `_tag` with an `Unknown` fallback.
- Enrollment CSV imports from a `FileUpload` (a path, bytes or a stream), and
  Certificate PDF downloads whole (`downloadPdf`) or streamed
  (`downloadPdfStream`).
- `Webhook.constructEvent`, which verifies a Delivery's signature, body hash,
  path and timestamp and returns a typed Event, `UnknownEvent` or Test
  delivery, taking the headers as a `Map` or a lookup such as
  `request::getHeader` and an optional `Webhook.VerifyOptions` (tolerance,
  arrival time); and `Webhook.generateTestHeaders` for testing handlers.
- `events().tail(...)` to read the Event log from a saved position, once or
  following new Events until the thread is interrupted.
- Opt-in logging through a `RequestLogger`, one entry per request with
  method, path, status, latency and request id, or for a request that got no
  response a `null` status and an `errorKind` (`TIMEOUT`, `CONNECTION`); never
  bodies, headers or credentials. `AppInfo` in the `User-Agent`, and a
  pluggable `HttpTransport`.
- Java 17 or newer, with `jackson-databind` and `jackson-annotations` as API
  (compile-scope) dependencies, since public models expose `JsonNode`. No
  telemetry and no disk writes.

### Documentation

- Guides in `docs/guides/`: getting started, authentication, errors and
  retries, pagination and the Event log, webhooks (Spring Boot, servlet and
  JDK HTTP server handlers), Operations and files, conditional requests,
  testing, configuration and versioning. Every Java sample in them and in the
  README is compiled against the library and run against a fake transport by
  the test suite, which also checks every relative link and anchor.
- `docs/reference/resources.md`, generated from the SDK IR by
  `./gradlew generate`: every service and method with its overloads, HTTP
  route, Scopes, retry, idempotency and conditional behavior, and return
  type. `generateCheck` keeps it current, and a test checks it against the
  compiled services.
- The Javadoc as the API reference, with an overview page, built with doclint
  and `-Werror` on every build and published to GitHub Pages from `main` by
  a new Docs workflow.
- Runnable examples in `examples/`: a quickstart, a roster sync by External
  ID, a webhook endpoint on the JDK's HTTP server and a Certificate export,
  compiled by `check` and run with `./gradlew runExample -Pexample=<Name>`.
- A shorter README with a pre-release status note that links to the guides,
  and `docs/README.md`, an index of all documentation.
