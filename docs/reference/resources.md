<!-- Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate. -->

# Service reference

`TeachMeHipaaClient` exposes 13 services and 45 API methods, generated from Developer API release `trimmed-2026-11-15`. This page lists each method's signatures and wire behavior. The [Javadoc](https://teachmehipaa.github.io/teachmehipaa-java/) documents every type, and the [guides](../README.md) explain usage.

A service is reached through its accessor, such as `client.learners()`. Parameters follow one order: path ids, then the method's `…Params` (from its `builder()`), then [`RequestOptions`](../guides/client.md#per-call-options). Shorter overloads omit what is optional. Each returned object carries `getLastResponse()`. Paths are relative to the Environment's base URL (Live is `https://api.tme.so/v1`; Sandbox is `https://sandbox.api.tme.so/v1`), and the client supplies `{orgId}` from its [Organization binding](../guides/authentication.md#the-organization-binding).

The blocking signatures of each method come first, followed by its `…Async` forms: the same overloads, returning `CompletableFuture<T>` of the blocking `T`. See [Async with CompletableFuture](../guides/async.md).

## Contents

- [`auditEvents()`](#auditevents): [`list`](#auditeventslist)
- [`auth()`](#auth): [`introspect`](#authintrospect)
- [`certificateVerifications()`](#certificateverifications): [`retrieve`](#certificateverificationsretrieve)
- [`certificates()`](#certificates): [`downloadPdf`](#certificatesdownloadpdf), [`export`](#certificatesexport), [`list`](#certificateslist), [`retrieve`](#certificatesretrieve), [`retrieveLinks`](#certificatesretrievelinks)
- [`compliance()`](#compliance): [`retrieve`](#complianceretrieve)
- [`courses()`](#courses): [`list`](#courseslist), [`retrieve`](#coursesretrieve)
- [`enrollments()`](#enrollments): [`batch`](#enrollmentsbatch), [`batchByFilter`](#enrollmentsbatchbyfilter), [`cancel`](#enrollmentscancel), [`create`](#enrollmentscreate), [`export`](#enrollmentsexport), [`importEnrollments`](#enrollmentsimportenrollments), [`list`](#enrollmentslist), [`previewImport`](#enrollmentspreviewimport), [`remind`](#enrollmentsremind), [`renew`](#enrollmentsrenew), [`resendInvite`](#enrollmentsresendinvite), [`retrieve`](#enrollmentsretrieve)
- [`events()`](#events): [`list`](#eventslist)
- [`learners()`](#learners): [`batchUpsert`](#learnersbatchupsert), [`create`](#learnerscreate), [`createLaunchLink`](#learnerscreatelaunchlink), [`export`](#learnersexport), [`list`](#learnerslist), [`offboard`](#learnersoffboard), [`remind`](#learnersremind), [`retrieve`](#learnersretrieve), [`update`](#learnersupdate), [`upsertByExternalId`](#learnersupsertbyexternalid)
- [`members()`](#members): [`list`](#memberslist), [`retrieve`](#membersretrieve)
- [`operations()`](#operations): [`retrieve`](#operationsretrieve)
- [`seats()`](#seats): [`retrieve`](#seatsretrieve)
- [`webhookSubscriptions()`](#webhooksubscriptions): [`create`](#webhooksubscriptionscreate), [`delete`](#webhooksubscriptionsdelete), [`list`](#webhooksubscriptionslist), [`redrive`](#webhooksubscriptionsredrive), [`retrieve`](#webhooksubscriptionsretrieve), [`test`](#webhooksubscriptionstest), [`update`](#webhooksubscriptionsupdate)

## Scopes

An Access token holds every Scope its API client holds, unless the builder's `scopes` narrows it. Where a method lists several Scopes, any one suffices. See [Authentication](../guides/authentication.md#scopes).

| Scope | Allows |
| --- | --- |
| `audit:read` | Read the organization's audit-event trail and event log. |
| `certificates:read` | Read certificates and download certificate artifacts. |
| `compliance:read` | API clients: read the compliance summary. |
| `enrollments:read` | Read enrollments, invitations, seats, and member sub-resources. |
| `enrollments:write` | Create and transition enrollments and assessment attempts. |
| `learners:read` | API clients: read learners. |
| `learners:write` | API clients: create and update learners. |
| `members:read` | Read organization members and the organization record. |
| `webhooks:manage` | API clients: manage webhook subscriptions and read the event log. |

## `auditEvents()`

### `auditEvents().list`

List all audit events

Returns a list of the organization's audit events, newest first. Filter by `type`, `transition`, `subjectId`, or a `since` and `until` range.

```java
Page<AuditEvent> list()
Page<AuditEvent> list(RequestOptions options)
Page<AuditEvent> list(AuditEventListParams params)
Page<AuditEvent> list(AuditEventListParams params, RequestOptions options)
CompletableFuture<Page<AuditEvent>> listAsync()
CompletableFuture<Page<AuditEvent>> listAsync(RequestOptions options)
CompletableFuture<Page<AuditEvent>> listAsync(AuditEventListParams params)
CompletableFuture<Page<AuditEvent>> listAsync(AuditEventListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/audit-events` |
| Scopes | `audit:read` |
| Params | `AuditEventListParams`, optional |
| Returns | `Page<AuditEvent>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 100 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

## `auth()`

### `auth().introspect`

Retrieve the current token

Describes the access token you call with: whether it belongs to an API client or a person, its organization, its scopes and when it expires. Needs no scope.

```java
TokenIntrospection introspect()
TokenIntrospection introspect(RequestOptions options)
CompletableFuture<TokenIntrospection> introspectAsync()
CompletableFuture<TokenIntrospection> introspectAsync(RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /auth/introspection` |
| Scopes | Any Access token |
| Params | None |
| Returns | `TokenIntrospection` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

## `certificateVerifications()`

### `certificateVerifications().retrieve`

Verify a certificate

Verifies the certificate behind a `https://tme.so/{tinyId}` link. Needs no access token. Limited to 60 requests a minute per IP address.

```java
CertificateVerification retrieve(String tinyId)
CertificateVerification retrieve(String tinyId, RequestOptions options)
CompletableFuture<CertificateVerification> retrieveAsync(String tinyId)
CompletableFuture<CertificateVerification> retrieveAsync(String tinyId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /certificate-verifications/{tinyId}` |
| Scopes | None; public, sent without an Access token |
| Params | None |
| Returns | `CertificateVerification` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

## `certificates()`

### `certificates().downloadPdf`

Download a certificate

Downloads the certificate as a PDF. Revoked and expired certificates are watermarked. Requires the `certificates:read` scope.

```java
BinaryResponse downloadPdf(String certId)
BinaryResponse downloadPdf(String certId, RequestOptions options)
BinaryStreamResponse downloadPdfStream(String certId)
BinaryStreamResponse downloadPdfStream(String certId, RequestOptions options)
CompletableFuture<BinaryResponse> downloadPdfAsync(String certId)
CompletableFuture<BinaryResponse> downloadPdfAsync(String certId, RequestOptions options)
CompletableFuture<BinaryStreamResponse> downloadPdfStreamAsync(String certId)
CompletableFuture<BinaryStreamResponse> downloadPdfStreamAsync(String certId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/certificates/{certId}/content` |
| Scopes | `certificates:read` |
| Params | None |
| Returns | `BinaryResponse` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | `downloadPdfStream` returns the body unread as an `AutoCloseable`; close it, ideally with try-with-resources, to release the connection. |

### `certificates().export`

Export certificates

Exports the organization's certificate records as CSV. Requires the `certificates:read` scope.

```java
Operation export(CertificateExportParams params)
Operation export(CertificateExportParams params, RequestOptions options)
CompletableFuture<Operation> exportAsync(CertificateExportParams params)
CompletableFuture<Operation> exportAsync(CertificateExportParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/certificates:export` |
| Scopes | `certificates:read` |
| Params | `CertificateExportParams`, required |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `certificates().list`

List all certificates

Returns a list of the organization's certificates. Pass `subjectId` to list one learner's.

```java
Page<Certificate> list()
Page<Certificate> list(RequestOptions options)
Page<Certificate> list(CertificateListParams params)
Page<Certificate> list(CertificateListParams params, RequestOptions options)
CompletableFuture<Page<Certificate>> listAsync()
CompletableFuture<Page<Certificate>> listAsync(RequestOptions options)
CompletableFuture<Page<Certificate>> listAsync(CertificateListParams params)
CompletableFuture<Page<Certificate>> listAsync(CertificateListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/certificates` |
| Scopes | `certificates:read` |
| Params | `CertificateListParams`, optional |
| Returns | `Page<Certificate>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 100 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

### `certificates().retrieve`

Retrieve a certificate

Retrieves a certificate.

```java
Certificate retrieve(String certId)
Certificate retrieve(String certId, RequestOptions options)
CompletableFuture<Certificate> retrieveAsync(String certId)
CompletableFuture<Certificate> retrieveAsync(String certId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/certificates/{certId}` |
| Scopes | `certificates:read` |
| Params | None |
| Returns | `Certificate` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

### `certificates().retrieveLinks`

Retrieve certificate links

Retrieves the certificate's LinkedIn add-to-profile link and its public `tme.so` verification link. Requires the `certificates:read` scope.

```java
CertificateLinks retrieveLinks(String certId)
CertificateLinks retrieveLinks(String certId, RequestOptions options)
CompletableFuture<CertificateLinks> retrieveLinksAsync(String certId)
CompletableFuture<CertificateLinks> retrieveLinksAsync(String certId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/certificates/{certId}/credentials` |
| Scopes | `certificates:read` |
| Params | None |
| Returns | `CertificateLinks` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

## `compliance()`

### `compliance().retrieve`

Retrieve the compliance summary

Counts the organization's learners in one course by status. Removed learners aren't counted.

```java
ComplianceSummary retrieve()
ComplianceSummary retrieve(RequestOptions options)
ComplianceSummary retrieve(ComplianceRetrieveParams params)
ComplianceSummary retrieve(ComplianceRetrieveParams params, RequestOptions options)
CompletableFuture<ComplianceSummary> retrieveAsync()
CompletableFuture<ComplianceSummary> retrieveAsync(RequestOptions options)
CompletableFuture<ComplianceSummary> retrieveAsync(ComplianceRetrieveParams params)
CompletableFuture<ComplianceSummary> retrieveAsync(ComplianceRetrieveParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/compliance` |
| Scopes | `compliance:read` |
| Params | `ComplianceRetrieveParams`, optional |
| Returns | `ComplianceSummary` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

## `courses()`

### `courses().list`

List all courses

Returns a list of the courses in the public catalog. Needs no access token.

```java
Page<Course> list()
Page<Course> list(RequestOptions options)
Page<Course> list(CourseListParams params)
Page<Course> list(CourseListParams params, RequestOptions options)
CompletableFuture<Page<Course>> listAsync()
CompletableFuture<Page<Course>> listAsync(RequestOptions options)
CompletableFuture<Page<Course>> listAsync(CourseListParams params)
CompletableFuture<Page<Course>> listAsync(CourseListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /courses` |
| Scopes | None; public, sent without an Access token |
| Params | `CourseListParams`, optional |
| Returns | `Page<Course>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 100 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

### `courses().retrieve`

Retrieve a course

Retrieves a course from the public catalog.

```java
Course retrieve(String courseId)
Course retrieve(String courseId, RequestOptions options)
Optional<Course> retrieveIfModified(String courseId, String etag)
Optional<Course> retrieveIfModified(String courseId, Course cached)
Optional<Course> retrieveIfModified(String courseId, Course cached, RequestOptions options)
Optional<Course> retrieveIfModified(String courseId, String etag, RequestOptions options)
CompletableFuture<Course> retrieveAsync(String courseId)
CompletableFuture<Course> retrieveAsync(String courseId, RequestOptions options)
CompletableFuture<Optional<Course>> retrieveIfModifiedAsync(String courseId, String etag)
CompletableFuture<Optional<Course>> retrieveIfModifiedAsync(String courseId, Course cached)
CompletableFuture<Optional<Course>> retrieveIfModifiedAsync(String courseId, Course cached, RequestOptions options)
CompletableFuture<Optional<Course>> retrieveIfModifiedAsync(String courseId, String etag, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /courses/{courseId}` |
| Scopes | None; public, sent without an Access token |
| Params | None |
| Returns | `Course` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | `retrieveIfModified` sends `If-None-Match` and returns an empty `Optional` when your copy is still current (304). |

## `enrollments()`

### `enrollments().batch`

Update many enrollments

Reminds, resends, cancels, renews, archives or unarchives up to 500 enrollments in one request, chosen by id or by filter. Each enrollment succeeds or fails on its own. Requires the `enrollments:write` scope.

```java
EnrollmentsBatchResults batch(EnrollmentBatchParams params)
EnrollmentsBatchResults batch(EnrollmentBatchParams params, RequestOptions options)
CompletableFuture<EnrollmentsBatchResults> batchAsync(EnrollmentBatchParams params)
CompletableFuture<EnrollmentsBatchResults> batchAsync(EnrollmentBatchParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments:batch` |
| Scopes | `enrollments:write` |
| Params | `EnrollmentBatchParams`, required |
| Returns | `EnrollmentsBatchResults` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().batchByFilter`

Update many enrollments

Reminds, resends, cancels, renews, archives or unarchives up to 500 enrollments in one request, chosen by id or by filter. Each enrollment succeeds or fails on its own. Requires the `enrollments:write` scope.

```java
Operation batchByFilter(EnrollmentBatchByFilterParams params)
Operation batchByFilter(EnrollmentBatchByFilterParams params, RequestOptions options)
CompletableFuture<Operation> batchByFilterAsync(EnrollmentBatchByFilterParams params)
CompletableFuture<Operation> batchByFilterAsync(EnrollmentBatchByFilterParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments:batch` |
| Scopes | `enrollments:write` |
| Params | `EnrollmentBatchByFilterParams`, required |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `enrollments().cancel`

Cancel an enrollment

Cancels an invite that hasn't been accepted yet and returns its seat to the pool. With `refund: true`, refunds the seat to the card that paid for it instead. The enrollment remains, with status `CANCELLED`. Requires the `enrollments:write` scope.

```java
CancelEnrollmentResponse cancel(String enrollmentId)
CancelEnrollmentResponse cancel(String enrollmentId, RequestOptions options)
CancelEnrollmentResponse cancel(String enrollmentId, EnrollmentCancelParams params)
CancelEnrollmentResponse cancel(String enrollmentId, EnrollmentCancelParams params, RequestOptions options)
CompletableFuture<CancelEnrollmentResponse> cancelAsync(String enrollmentId)
CompletableFuture<CancelEnrollmentResponse> cancelAsync(String enrollmentId, RequestOptions options)
CompletableFuture<CancelEnrollmentResponse> cancelAsync(String enrollmentId, EnrollmentCancelParams params)
CompletableFuture<CancelEnrollmentResponse> cancelAsync(String enrollmentId, EnrollmentCancelParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments/{enrollmentId}:cancel` |
| Scopes | `enrollments:write` |
| Params | `EnrollmentCancelParams`, optional |
| Returns | `CancelEnrollmentResponse` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().create`

Create an enrollment

Invites a person to a course, creating their profile if needed, and uses one seat. The course defaults to `hipaa`. Requires the `enrollments:write` scope.

```java
Enrollment create(EnrollmentCreateParams params)
Enrollment create(EnrollmentCreateParams params, RequestOptions options)
CompletableFuture<Enrollment> createAsync(EnrollmentCreateParams params)
CompletableFuture<Enrollment> createAsync(EnrollmentCreateParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments` |
| Scopes | `enrollments:write` |
| Params | `EnrollmentCreateParams`, required |
| Returns | `Enrollment` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().export`

Export enrollments

Exports the organization's enrollments as CSV. Requires the `enrollments:read` scope.

```java
Operation export(EnrollmentExportParams params)
Operation export(EnrollmentExportParams params, RequestOptions options)
CompletableFuture<Operation> exportAsync(EnrollmentExportParams params)
CompletableFuture<Operation> exportAsync(EnrollmentExportParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments:export` |
| Scopes | `enrollments:read` |
| Params | `EnrollmentExportParams`, required |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `enrollments().importEnrollments`

Import learners from CSV

Invites everyone in a CSV file with an `email` column (and optional `name` and `courseId`). Send `validateOnly: true` first to preview the file without inviting anyone. Up to 500 rows. Requires the `enrollments:write` scope.

```java
Operation importEnrollments(EnrollmentImportParams params)
Operation importEnrollments(EnrollmentImportParams params, RequestOptions options)
CompletableFuture<Operation> importEnrollmentsAsync(EnrollmentImportParams params)
CompletableFuture<Operation> importEnrollmentsAsync(EnrollmentImportParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments:import` |
| Scopes | `enrollments:write` |
| Params | `EnrollmentImportParams`, required |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `enrollments().list`

List all enrollments

Returns a list of the organization's enrollments. Filter by `status`, `courseId` or `archived`. Requires the `enrollments:read` scope.

```java
Page<OrgEnrollmentListItem> list()
Page<OrgEnrollmentListItem> list(RequestOptions options)
Page<OrgEnrollmentListItem> list(EnrollmentListParams params)
Page<OrgEnrollmentListItem> list(EnrollmentListParams params, RequestOptions options)
CompletableFuture<Page<OrgEnrollmentListItem>> listAsync()
CompletableFuture<Page<OrgEnrollmentListItem>> listAsync(RequestOptions options)
CompletableFuture<Page<OrgEnrollmentListItem>> listAsync(EnrollmentListParams params)
CompletableFuture<Page<OrgEnrollmentListItem>> listAsync(EnrollmentListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/enrollments` |
| Scopes | `enrollments:read` |
| Params | `EnrollmentListParams`, optional |
| Returns | `Page<OrgEnrollmentListItem>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 1000 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

### `enrollments().previewImport`

Import learners from CSV

Invites everyone in a CSV file with an `email` column (and optional `name` and `courseId`). Send `validateOnly: true` first to preview the file without inviting anyone. Up to 500 rows. Requires the `enrollments:write` scope.

```java
ImportPreview previewImport(EnrollmentPreviewImportParams params)
ImportPreview previewImport(EnrollmentPreviewImportParams params, RequestOptions options)
CompletableFuture<ImportPreview> previewImportAsync(EnrollmentPreviewImportParams params)
CompletableFuture<ImportPreview> previewImportAsync(EnrollmentPreviewImportParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments:import` |
| Scopes | `enrollments:write` |
| Params | `EnrollmentPreviewImportParams`, required |
| Returns | `ImportPreview` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().remind`

Remind an enrollment

Emails the learner again: the invitation if they haven't joined, or a reminder linking to the training if they have. Requires the `enrollments:write` scope.

```java
EnrollmentActionAccepted remind(String enrollmentId)
EnrollmentActionAccepted remind(String enrollmentId, RequestOptions options)
CompletableFuture<EnrollmentActionAccepted> remindAsync(String enrollmentId)
CompletableFuture<EnrollmentActionAccepted> remindAsync(String enrollmentId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments/{enrollmentId}:remind` |
| Scopes | `enrollments:write` |
| Params | None |
| Returns | `EnrollmentActionAccepted` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().renew`

Renew an enrollment

Starts the next certification cycle for a certified learner and invites them again, using one seat. Retrying never uses a second seat. Requires the `enrollments:write` scope.

```java
Enrollment renew(String enrollmentId)
Enrollment renew(String enrollmentId, RequestOptions options)
CompletableFuture<Enrollment> renewAsync(String enrollmentId)
CompletableFuture<Enrollment> renewAsync(String enrollmentId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments/{enrollmentId}:renew` |
| Scopes | `enrollments:write` |
| Params | None |
| Returns | `Enrollment` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().resendInvite`

Resend an invite

Sends a pending invite again, within the same cycle. To invite a certified learner again, renew the enrollment. Requires the `enrollments:write` scope.

```java
EnrollmentActionAccepted resendInvite(String enrollmentId)
EnrollmentActionAccepted resendInvite(String enrollmentId, RequestOptions options)
CompletableFuture<EnrollmentActionAccepted> resendInviteAsync(String enrollmentId)
CompletableFuture<EnrollmentActionAccepted> resendInviteAsync(String enrollmentId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/enrollments/{enrollmentId}:resendInvite` |
| Scopes | `enrollments:write` |
| Params | None |
| Returns | `EnrollmentActionAccepted` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `enrollments().retrieve`

Retrieve an enrollment

Retrieves an enrollment. Requires the `enrollments:read` scope.

```java
Enrollment retrieve(String enrollmentId)
Enrollment retrieve(String enrollmentId, RequestOptions options)
Optional<Enrollment> retrieveIfModified(String enrollmentId, String etag)
Optional<Enrollment> retrieveIfModified(String enrollmentId, Enrollment cached)
Optional<Enrollment> retrieveIfModified(String enrollmentId, Enrollment cached, RequestOptions options)
Optional<Enrollment> retrieveIfModified(String enrollmentId, String etag, RequestOptions options)
CompletableFuture<Enrollment> retrieveAsync(String enrollmentId)
CompletableFuture<Enrollment> retrieveAsync(String enrollmentId, RequestOptions options)
CompletableFuture<Optional<Enrollment>> retrieveIfModifiedAsync(String enrollmentId, String etag)
CompletableFuture<Optional<Enrollment>> retrieveIfModifiedAsync(String enrollmentId, Enrollment cached)
CompletableFuture<Optional<Enrollment>> retrieveIfModifiedAsync(String enrollmentId, Enrollment cached, RequestOptions options)
CompletableFuture<Optional<Enrollment>> retrieveIfModifiedAsync(String enrollmentId, String etag, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/enrollments/{enrollmentId}` |
| Scopes | `enrollments:read` |
| Params | None |
| Returns | `Enrollment` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | `retrieveIfModified` sends `If-None-Match` and returns an empty `Optional` when your copy is still current (304). |

## `events()`

`EventTail tail(EventTailParams params)` has no IR method of its own. It wraps `events().list` in an `Iterable<Event>` that walks the log oldest first and records its position in `lastEventId()`; see [Tailing the Event log](../guides/pagination.md#tailing-the-event-log).

### `events().list`

List all events

Returns a list of the organization's events from the last 30 days, oldest first. Pass the last `eventId` you processed as `after` to resume where you stopped, and `type` to list one event type.

```java
Page<Event> list()
Page<Event> list(RequestOptions options)
Page<Event> list(EventListParams params)
Page<Event> list(EventListParams params, RequestOptions options)
CompletableFuture<Page<Event>> listAsync()
CompletableFuture<Page<Event>> listAsync(RequestOptions options)
CompletableFuture<Page<Event>> listAsync(EventListParams params)
CompletableFuture<Page<Event>> listAsync(EventListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/events` |
| Scopes | `webhooks:manage` or `audit:read` |
| Params | `EventListParams`, optional |
| Returns | `Page<Event>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 100 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

## `learners()`

### `learners().batchUpsert`

Create many learners

Creates or retrieves up to 500 learners in one request. Each item works like creating one learner and succeeds or fails on its own.

```java
Operation batchUpsert(LearnerBatchUpsertParams params)
Operation batchUpsert(LearnerBatchUpsertParams params, RequestOptions options)
CompletableFuture<Operation> batchUpsertAsync(LearnerBatchUpsertParams params)
CompletableFuture<Operation> batchUpsertAsync(LearnerBatchUpsertParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/learners:batchUpsert` |
| Scopes | `learners:write` |
| Params | `LearnerBatchUpsertParams`, required |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `learners().create`

Create a learner

Adds a person to the organization and invites them to a course in one call, using one seat. If the email or `externalId` already belongs to an active learner in the course, nothing is created. With `delivery: "none"`, no email is sent and the response includes a `joinUrl` for you to deliver.

```java
LearnerWriteResult create(LearnerCreateParams params)
LearnerWriteResult create(LearnerCreateParams params, RequestOptions options)
CompletableFuture<LearnerWriteResult> createAsync(LearnerCreateParams params)
CompletableFuture<LearnerWriteResult> createAsync(LearnerCreateParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/learners` |
| Scopes | `learners:write` |
| Params | `LearnerCreateParams`, required |
| Returns | `LearnerWriteResult` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `learners().createLaunchLink`

Create a launch link

Creates a link that opens the course with the learner already signed in, for a "Start training" button in your product. The link works once and expires after 5 minutes: create it when the learner clicks, then redirect them to it. Never email, store or log it. Only for a learner who has accepted their invite.

```java
LearnerLaunchLink createLaunchLink(String learnerId)
LearnerLaunchLink createLaunchLink(String learnerId, RequestOptions options)
LearnerLaunchLink createLaunchLink(String learnerId, LearnerCreateLaunchLinkParams params)
LearnerLaunchLink createLaunchLink(String learnerId, LearnerCreateLaunchLinkParams params, RequestOptions options)
CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(String learnerId)
CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(String learnerId, RequestOptions options)
CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(String learnerId, LearnerCreateLaunchLinkParams params)
CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(String learnerId, LearnerCreateLaunchLinkParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/learners/{learnerId}:createLaunchLink` |
| Scopes | `learners:write` |
| Params | `LearnerCreateLaunchLinkParams`, optional |
| Returns | `LearnerLaunchLink` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

### `learners().export`

Export learners

Exports the organization's learners in one course as CSV, optionally only those with one `status`. Requires the `learners:read` scope.

```java
Operation export(LearnerExportParams params)
Operation export(LearnerExportParams params, RequestOptions options)
CompletableFuture<Operation> exportAsync(LearnerExportParams params)
CompletableFuture<Operation> exportAsync(LearnerExportParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/learners:export` |
| Scopes | `learners:read` |
| Params | `LearnerExportParams`, required |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `learners().list`

List all learners

Returns a list of the organization's learners in one course, with each learner's status, dates and current certificate. Filter by `status`, or find one learner by `email` or `externalId`.

```java
Page<Learner> list()
Page<Learner> list(RequestOptions options)
Page<Learner> list(LearnerListParams params)
Page<Learner> list(LearnerListParams params, RequestOptions options)
CompletableFuture<Page<Learner>> listAsync()
CompletableFuture<Page<Learner>> listAsync(RequestOptions options)
CompletableFuture<Page<Learner>> listAsync(LearnerListParams params)
CompletableFuture<Page<Learner>> listAsync(LearnerListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/learners` |
| Scopes | `learners:read` |
| Params | `LearnerListParams`, optional |
| Returns | `Page<Learner>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 100 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

### `learners().offboard`

Offboard a learner

Removes a learner from the organization. A learner who hasn't started has their invite cancelled and the seat returned; a learner who has started keeps their training history and certificates. Releases the learner's `externalId`.

```java
Learner offboard(String learnerId)
Learner offboard(String learnerId, RequestOptions options)
Learner offboard(String learnerId, LearnerOffboardParams params)
Learner offboard(String learnerId, LearnerOffboardParams params, RequestOptions options)
CompletableFuture<Learner> offboardAsync(String learnerId)
CompletableFuture<Learner> offboardAsync(String learnerId, RequestOptions options)
CompletableFuture<Learner> offboardAsync(String learnerId, LearnerOffboardParams params)
CompletableFuture<Learner> offboardAsync(String learnerId, LearnerOffboardParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/learners/{learnerId}:offboard` |
| Scopes | `learners:write` |
| Params | `LearnerOffboardParams`, optional |
| Returns | `Learner` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Accepts an optional `ifMatch`, given an ETag or the object you read. |

### `learners().remind`

Remind a learner

Emails the learner a reminder: the invite again if they haven't started, or a nudge to finish if they have.

```java
LearnerRemindAccepted remind(String learnerId)
LearnerRemindAccepted remind(String learnerId, RequestOptions options)
LearnerRemindAccepted remind(String learnerId, LearnerRemindParams params)
LearnerRemindAccepted remind(String learnerId, LearnerRemindParams params, RequestOptions options)
CompletableFuture<LearnerRemindAccepted> remindAsync(String learnerId)
CompletableFuture<LearnerRemindAccepted> remindAsync(String learnerId, RequestOptions options)
CompletableFuture<LearnerRemindAccepted> remindAsync(String learnerId, LearnerRemindParams params)
CompletableFuture<LearnerRemindAccepted> remindAsync(String learnerId, LearnerRemindParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/learners/{learnerId}:remind` |
| Scopes | `learners:write` |
| Params | `LearnerRemindParams`, optional |
| Returns | `LearnerRemindAccepted` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `learners().retrieve`

Retrieve a learner

Retrieves a learner's status in one course.

```java
Learner retrieve(String learnerId)
Learner retrieve(String learnerId, RequestOptions options)
Learner retrieve(String learnerId, LearnerRetrieveParams params)
Learner retrieve(String learnerId, LearnerRetrieveParams params, RequestOptions options)
CompletableFuture<Learner> retrieveAsync(String learnerId)
CompletableFuture<Learner> retrieveAsync(String learnerId, RequestOptions options)
CompletableFuture<Learner> retrieveAsync(String learnerId, LearnerRetrieveParams params)
CompletableFuture<Learner> retrieveAsync(String learnerId, LearnerRetrieveParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/learners/{learnerId}` |
| Scopes | `learners:read` |
| Params | `LearnerRetrieveParams`, optional |
| Returns | `Learner` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

### `learners().update`

Update a learner

Updates a learner's `name` or `externalId`; parameters you don't send are left unchanged. Send the body as `application/merge-patch+json` with the learner's current `ETag` in `If-Match`.

```java
Learner update(String learnerId, LearnerUpdateParams params)
Learner update(String learnerId, LearnerUpdateParams params, RequestOptions options)
CompletableFuture<Learner> updateAsync(String learnerId, LearnerUpdateParams params)
CompletableFuture<Learner> updateAsync(String learnerId, LearnerUpdateParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `PATCH /organizations/{orgId}/learners/{learnerId}` |
| Scopes | `learners:write` |
| Params | `LearnerUpdateParams`, required |
| Returns | `Learner` |
| Retries | No; a replay could repeat a change |
| Idempotency | None |
| Notes | Requires `ifMatch`, given an ETag or the object you read; `build()` throws `IllegalStateException` without it. A stale ETag fails with `PreconditionFailedException`. Sends an RFC 7396 merge patch: unset fields stay unchanged, and `clearX()` sends `null`. |

### `learners().upsertByExternalId`

Create or retrieve a learner by your id

Makes sure a learner exists for your own id. Creates and invites the learner the first time, and returns them on every later call, so a sync job can replay it safely. Never uses a second seat.

```java
LearnerWriteResult upsertByExternalId(String externalId, LearnerUpsertByExternalIdParams params)
LearnerWriteResult upsertByExternalId(String externalId, LearnerUpsertByExternalIdParams params, RequestOptions options)
CompletableFuture<LearnerWriteResult> upsertByExternalIdAsync(String externalId, LearnerUpsertByExternalIdParams params)
CompletableFuture<LearnerWriteResult> upsertByExternalIdAsync(String externalId, LearnerUpsertByExternalIdParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `PUT /organizations/{orgId}/learners/by-external-id/{externalId}` |
| Scopes | `learners:write` |
| Params | `LearnerUpsertByExternalIdParams`, required |
| Returns | `LearnerWriteResult` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

## `members()`

### `members().list`

List all members

Returns a list of the organization's members, its admins and learners. Pass `role=admin` to list only admins.

```java
Page<MemberListItem> list()
Page<MemberListItem> list(RequestOptions options)
Page<MemberListItem> list(MemberListParams params)
Page<MemberListItem> list(MemberListParams params, RequestOptions options)
CompletableFuture<Page<MemberListItem>> listAsync()
CompletableFuture<Page<MemberListItem>> listAsync(RequestOptions options)
CompletableFuture<Page<MemberListItem>> listAsync(MemberListParams params)
CompletableFuture<Page<MemberListItem>> listAsync(MemberListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/members` |
| Scopes | `members:read` |
| Params | `MemberListParams`, optional |
| Returns | `Page<MemberListItem>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 1000 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

### `members().retrieve`

Retrieve a member

Retrieves a member of the organization.

```java
Member retrieve(String subjectId)
Member retrieve(String subjectId, RequestOptions options)
CompletableFuture<Member> retrieveAsync(String subjectId)
CompletableFuture<Member> retrieveAsync(String subjectId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/members/{subjectId}` |
| Scopes | `members:read` |
| Params | None |
| Returns | `Member` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

## `operations()`

### `operations().retrieve`

Retrieve an operation

Retrieves a bulk operation and its result. Operations are kept for 24 hours.

```java
Operation retrieve(String operationId)
Operation retrieve(String operationId, RequestOptions options)
CompletableFuture<Operation> retrieveAsync(String operationId)
CompletableFuture<Operation> retrieveAsync(String operationId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/operations/{operationId}` |
| Scopes | Any Access token |
| Params | None |
| Returns | `Operation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

## `seats()`

### `seats().retrieve`

Retrieve the seat inventory

Retrieves how many seats the organization has, how many are assigned and how many are available for new invites. Requires the `enrollments:read` scope.

```java
SeatsInventory retrieve()
SeatsInventory retrieve(RequestOptions options)
CompletableFuture<SeatsInventory> retrieveAsync()
CompletableFuture<SeatsInventory> retrieveAsync(RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/seats` |
| Scopes | `enrollments:read` |
| Params | None |
| Returns | `SeatsInventory` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |

## `webhookSubscriptions()`

### `webhookSubscriptions().create`

Create a webhook subscription

Subscribes an HTTPS endpoint to event types and creates its signing secret. Requires the `webhooks:manage` or `org:admin` scope.

```java
WebhookSubscriptionCreated create(WebhookSubscriptionCreateParams params)
WebhookSubscriptionCreated create(WebhookSubscriptionCreateParams params, RequestOptions options)
CompletableFuture<WebhookSubscriptionCreated> createAsync(WebhookSubscriptionCreateParams params)
CompletableFuture<WebhookSubscriptionCreated> createAsync(WebhookSubscriptionCreateParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/webhook-subscriptions` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | `WebhookSubscriptionCreateParams`, required |
| Returns | `WebhookSubscriptionCreated` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |

### `webhookSubscriptions().delete`

Delete a webhook subscription

Deletes a webhook subscription and its signing secret. Requires the `webhooks:manage` or `org:admin` scope.

```java
EmptyResponse delete(String subscriptionId)
EmptyResponse delete(String subscriptionId, RequestOptions options)
CompletableFuture<EmptyResponse> deleteAsync(String subscriptionId)
CompletableFuture<EmptyResponse> deleteAsync(String subscriptionId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `DELETE /organizations/{orgId}/webhook-subscriptions/{subscriptionId}` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | None |
| Returns | `EmptyResponse` |
| Retries | No; a replay could repeat a change |
| Idempotency | None |

### `webhookSubscriptions().list`

List all webhook subscriptions

Returns a list of the organization's webhook subscriptions. Signing secrets are never returned. Requires the `webhooks:manage` or `org:admin` scope.

```java
Page<WebhookSubscription> list()
Page<WebhookSubscription> list(RequestOptions options)
Page<WebhookSubscription> list(WebhookSubscriptionListParams params)
Page<WebhookSubscription> list(WebhookSubscriptionListParams params, RequestOptions options)
CompletableFuture<Page<WebhookSubscription>> listAsync()
CompletableFuture<Page<WebhookSubscription>> listAsync(RequestOptions options)
CompletableFuture<Page<WebhookSubscription>> listAsync(WebhookSubscriptionListParams params)
CompletableFuture<Page<WebhookSubscription>> listAsync(WebhookSubscriptionListParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/webhook-subscriptions` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | `WebhookSubscriptionListParams`, optional |
| Returns | `Page<WebhookSubscription>` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | One page of up to 100 items. `autoPagingIterable()` and `autoPagingStream()` fetch later pages on demand. |

### `webhookSubscriptions().redrive`

Retry failed deliveries

Delivers the subscription's failed events again. Requires the `webhooks:manage` or `org:admin` scope.

```java
WebhookDeliveryOperation redrive(String subscriptionId)
WebhookDeliveryOperation redrive(String subscriptionId, RequestOptions options)
CompletableFuture<WebhookDeliveryOperation> redriveAsync(String subscriptionId)
CompletableFuture<WebhookDeliveryOperation> redriveAsync(String subscriptionId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/webhook-subscriptions/{subscriptionId}:redrive` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | None |
| Returns | `WebhookDeliveryOperation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `webhookSubscriptions().retrieve`

Retrieve a webhook subscription

Retrieves a webhook subscription. Requires the `webhooks:manage` or `org:admin` scope.

```java
WebhookSubscription retrieve(String subscriptionId)
WebhookSubscription retrieve(String subscriptionId, RequestOptions options)
Optional<WebhookSubscription> retrieveIfModified(String subscriptionId, String etag)
Optional<WebhookSubscription> retrieveIfModified(String subscriptionId, WebhookSubscription cached)
Optional<WebhookSubscription> retrieveIfModified(String subscriptionId, WebhookSubscription cached, RequestOptions options)
Optional<WebhookSubscription> retrieveIfModified(String subscriptionId, String etag, RequestOptions options)
CompletableFuture<WebhookSubscription> retrieveAsync(String subscriptionId)
CompletableFuture<WebhookSubscription> retrieveAsync(String subscriptionId, RequestOptions options)
CompletableFuture<Optional<WebhookSubscription>> retrieveIfModifiedAsync(String subscriptionId, String etag)
CompletableFuture<Optional<WebhookSubscription>> retrieveIfModifiedAsync(String subscriptionId, WebhookSubscription cached)
CompletableFuture<Optional<WebhookSubscription>> retrieveIfModifiedAsync(String subscriptionId, WebhookSubscription cached, RequestOptions options)
CompletableFuture<Optional<WebhookSubscription>> retrieveIfModifiedAsync(String subscriptionId, String etag, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `GET /organizations/{orgId}/webhook-subscriptions/{subscriptionId}` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | None |
| Returns | `WebhookSubscription` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | None |
| Notes | `retrieveIfModified` sends `If-None-Match` and returns an empty `Optional` when your copy is still current (304). |

### `webhookSubscriptions().test`

Send a test event

Sends a signed test delivery to the subscription's endpoint, so you can check your verifier. Requires the `webhooks:manage` or `org:admin` scope.

```java
WebhookDeliveryOperation test(String subscriptionId)
WebhookDeliveryOperation test(String subscriptionId, RequestOptions options)
CompletableFuture<WebhookDeliveryOperation> testAsync(String subscriptionId)
CompletableFuture<WebhookDeliveryOperation> testAsync(String subscriptionId, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `POST /organizations/{orgId}/webhook-subscriptions/{subscriptionId}:test` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | None |
| Returns | `WebhookDeliveryOperation` |
| Retries | Yes, after network failures, 409 `conflict`, 429 and 5xx |
| Idempotency | Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in `RequestOptions` to choose your own |
| Notes | Returns an `Operation`; `waitForCompletion()` or `waitForCompletionAsync()` polls until it is done. |

### `webhookSubscriptions().update`

Update a webhook subscription

Updates a webhook subscription; parameters you don't send are left unchanged. Send its current `ETag` in `If-Match`. The signing secret doesn't change. Requires the `webhooks:manage` or `org:admin` scope.

```java
WebhookSubscription update(String subscriptionId, WebhookSubscriptionUpdateParams params)
WebhookSubscription update(String subscriptionId, WebhookSubscriptionUpdateParams params, RequestOptions options)
CompletableFuture<WebhookSubscription> updateAsync(String subscriptionId, WebhookSubscriptionUpdateParams params)
CompletableFuture<WebhookSubscription> updateAsync(String subscriptionId, WebhookSubscriptionUpdateParams params, RequestOptions options)
```

|  |  |
| --- | --- |
| HTTP | `PATCH /organizations/{orgId}/webhook-subscriptions/{subscriptionId}` |
| Scopes | `org:admin` or `webhooks:manage` |
| Params | `WebhookSubscriptionUpdateParams`, required |
| Returns | `WebhookSubscription` |
| Retries | No; a replay could repeat a change |
| Idempotency | None |
| Notes | Requires `ifMatch`, given an ETag or the object you read; `build()` throws `IllegalStateException` without it. A stale ETag fails with `PreconditionFailedException`. Sends an RFC 7396 merge patch: unset fields stay unchanged, and `clearX()` sends `null`. |
