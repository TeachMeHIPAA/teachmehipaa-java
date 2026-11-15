// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Etags;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.CancelEnrollmentResponse;
import com.teachmehipaa.model.Enrollment;
import com.teachmehipaa.model.EnrollmentActionAccepted;
import com.teachmehipaa.model.EnrollmentsBatchResults;
import com.teachmehipaa.model.ImportPreview;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.model.OrgEnrollmentListItem;
import com.teachmehipaa.param.EnrollmentBatchByFilterParams;
import com.teachmehipaa.param.EnrollmentBatchParams;
import com.teachmehipaa.param.EnrollmentCancelParams;
import com.teachmehipaa.param.EnrollmentCreateParams;
import com.teachmehipaa.param.EnrollmentExportParams;
import com.teachmehipaa.param.EnrollmentImportParams;
import com.teachmehipaa.param.EnrollmentListParams;
import com.teachmehipaa.param.EnrollmentPreviewImportParams;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Enrollments in the client's Organization. Reach it through {@code client.enrollments()}. */
public final class EnrollmentService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public EnrollmentService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Update many enrollments. */
  public EnrollmentsBatchResults batch(EnrollmentBatchParams params) {
    return batch(params, RequestOptions.none());
  }

  /**
   * Update many enrollments.
   *
   * <p>Reminds, resends, cancels, renews, archives or unarchives up to 500 enrollments in one
   * request, chosen by id or by filter. Each enrollment succeeds or fails on its own. Requires the
   * <code>enrollments:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link EnrollmentsBatchResults}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public EnrollmentsBatchResults batch(EnrollmentBatchParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments:batch")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, EnrollmentsBatchResults.class, options);
  }

  /** Update many enrollments. */
  public Operation batchByFilter(EnrollmentBatchByFilterParams params) {
    return batchByFilter(params, RequestOptions.none());
  }

  /**
   * Update many enrollments.
   *
   * <p>Reminds, resends, cancels, renews, archives or unarchives up to 500 enrollments in one
   * request, chosen by id or by filter. Each enrollment succeeds or fails on its own. Requires the
   * <code>enrollments:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which finishes in the background
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation batchByFilter(EnrollmentBatchByFilterParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments:batch")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Cancel an enrollment. */
  public CancelEnrollmentResponse cancel(String enrollmentId) {
    return cancel(enrollmentId, EnrollmentCancelParams.builder().build(), RequestOptions.none());
  }

  /** Cancel an enrollment. */
  public CancelEnrollmentResponse cancel(String enrollmentId, RequestOptions options) {
    return cancel(enrollmentId, EnrollmentCancelParams.builder().build(), options);
  }

  /** Cancel an enrollment. */
  public CancelEnrollmentResponse cancel(String enrollmentId, EnrollmentCancelParams params) {
    return cancel(enrollmentId, params, RequestOptions.none());
  }

  /**
   * Cancel an enrollment.
   *
   * <p>Cancels an invite that hasn't been accepted yet and returns its seat to the pool. With
   * <code>refund: true</code>, refunds the seat to the card that paid for it instead. The
   * enrollment remains, with status <code>CANCELLED</code>. Requires the <code>enrollments:write
   * </code> scope.
   *
   * @param enrollmentId
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link CancelEnrollmentResponse}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public CancelEnrollmentResponse cancel(
      String enrollmentId, EnrollmentCancelParams params, RequestOptions options) {
    Objects.requireNonNull(enrollmentId, "enrollmentId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments/{enrollmentId}:cancel")
            .pathParam("enrollmentId", enrollmentId)
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, CancelEnrollmentResponse.class, options);
  }

  /** Create an enrollment. */
  public Enrollment create(EnrollmentCreateParams params) {
    return create(params, RequestOptions.none());
  }

  /**
   * Create an enrollment.
   *
   * <p>Invites a person to a course, creating their profile if needed, and uses one seat. The
   * course defaults to <code>hipaa</code>. Requires the <code>enrollments:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Enrollment}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Enrollment create(EnrollmentCreateParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Enrollment.class, options);
  }

  /** Export enrollments. */
  public Operation export(EnrollmentExportParams params) {
    return export(params, RequestOptions.none());
  }

  /**
   * Export enrollments.
   *
   * <p>Exports the organization's enrollments as CSV. Requires the <code>enrollments:read</code>
   * scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which finishes in the background
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation export(EnrollmentExportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments:export")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Import learners from CSV. */
  public Operation importEnrollments(EnrollmentImportParams params) {
    return importEnrollments(params, RequestOptions.none());
  }

  /**
   * Import learners from CSV.
   *
   * <p>Invites everyone in a CSV file with an <code>email</code> column (and optional <code>name
   * </code> and <code>courseId</code>). Send <code>validateOnly: true</code> first to preview the
   * file without inviting anyone. Up to 500 rows. Requires the <code>enrollments:write</code>
   * scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which finishes in the background
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation importEnrollments(EnrollmentImportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments:import")
            .retrySafe()
            .idempotencyKeyRequired()
            .multipartBody(params)
            .filePart("file", params.getFile(), "text/csv")
            .fixedBodyValue(List.of("options", "validateOnly"), false)
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** List all enrollments. */
  public Page<OrgEnrollmentListItem> list() {
    return list(EnrollmentListParams.builder().build(), RequestOptions.none());
  }

  /** List all enrollments. */
  public Page<OrgEnrollmentListItem> list(RequestOptions options) {
    return list(EnrollmentListParams.builder().build(), options);
  }

  /** List all enrollments. */
  public Page<OrgEnrollmentListItem> list(EnrollmentListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all enrollments.
   *
   * <p>Returns a list of the organization's enrollments. Filter by <code>status</code>, <code>
   * courseId</code> or <code>archived</code>. Requires the <code>enrollments:read</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link OrgEnrollmentListItem}; {@code nextCursor()} is {@code null} on the
   *     last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<OrgEnrollmentListItem> list(EnrollmentListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/enrollments")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("status", params.getStatus())
            .queryParam("courseId", params.getCourseId())
            .queryParam("archived", params.getArchived())
            .queryParam("include", params.getInclude())
            .retrySafe()
            .build();
    return requester.requestPage(
        request, Pagination.cursor("cursor"), OrgEnrollmentListItem.class, options);
  }

  /** Import learners from CSV. */
  public ImportPreview previewImport(EnrollmentPreviewImportParams params) {
    return previewImport(params, RequestOptions.none());
  }

  /**
   * Import learners from CSV.
   *
   * <p>Invites everyone in a CSV file with an <code>email</code> column (and optional <code>name
   * </code> and <code>courseId</code>). Send <code>validateOnly: true</code> first to preview the
   * file without inviting anyone. Up to 500 rows. Requires the <code>enrollments:write</code>
   * scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link ImportPreview}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public ImportPreview previewImport(EnrollmentPreviewImportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments:import")
            .retrySafe()
            .idempotencyKeyRequired()
            .multipartBody(params)
            .filePart("file", params.getFile(), "text/csv")
            .fixedBodyValue(List.of("options", "validateOnly"), true)
            .build();
    return requester.request(request, ImportPreview.class, options);
  }

  /** Remind an enrollment. */
  public EnrollmentActionAccepted remind(String enrollmentId) {
    return remind(enrollmentId, RequestOptions.none());
  }

  /**
   * Remind an enrollment.
   *
   * <p>Emails the learner again: the invitation if they haven't joined, or a reminder linking to
   * the training if they have. Requires the <code>enrollments:write</code> scope.
   *
   * @param enrollmentId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link EnrollmentActionAccepted}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public EnrollmentActionAccepted remind(String enrollmentId, RequestOptions options) {
    Objects.requireNonNull(enrollmentId, "enrollmentId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments/{enrollmentId}:remind")
            .pathParam("enrollmentId", enrollmentId)
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, EnrollmentActionAccepted.class, options);
  }

  /** Renew an enrollment. */
  public Enrollment renew(String enrollmentId) {
    return renew(enrollmentId, RequestOptions.none());
  }

  /**
   * Renew an enrollment.
   *
   * <p>Starts the next certification cycle for a certified learner and invites them again, using
   * one seat. Retrying never uses a second seat. Requires the <code>enrollments:write</code> scope.
   *
   * @param enrollmentId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Enrollment}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Enrollment renew(String enrollmentId, RequestOptions options) {
    Objects.requireNonNull(enrollmentId, "enrollmentId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments/{enrollmentId}:renew")
            .pathParam("enrollmentId", enrollmentId)
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, Enrollment.class, options);
  }

  /** Resend an invite. */
  public EnrollmentActionAccepted resendInvite(String enrollmentId) {
    return resendInvite(enrollmentId, RequestOptions.none());
  }

  /**
   * Resend an invite.
   *
   * <p>Sends a pending invite again, within the same cycle. To invite a certified learner again,
   * renew the enrollment. Requires the <code>enrollments:write</code> scope.
   *
   * @param enrollmentId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link EnrollmentActionAccepted}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public EnrollmentActionAccepted resendInvite(String enrollmentId, RequestOptions options) {
    Objects.requireNonNull(enrollmentId, "enrollmentId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/enrollments/{enrollmentId}:resendInvite")
            .pathParam("enrollmentId", enrollmentId)
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, EnrollmentActionAccepted.class, options);
  }

  /** Retrieve an enrollment. */
  public Enrollment retrieve(String enrollmentId) {
    return retrieve(enrollmentId, RequestOptions.none());
  }

  /**
   * Retrieve an enrollment.
   *
   * <p>Retrieves an enrollment. Requires the <code>enrollments:read</code> scope.
   *
   * @param enrollmentId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Enrollment}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Enrollment retrieve(String enrollmentId, RequestOptions options) {
    Objects.requireNonNull(enrollmentId, "enrollmentId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/enrollments/{enrollmentId}")
            .pathParam("enrollmentId", enrollmentId)
            .retrySafe()
            .build();
    return requester.request(request, Enrollment.class, options);
  }

  /** Retrieves the enrollment unless the copy you hold is current. */
  public Optional<Enrollment> retrieveIfModified(String enrollmentId, String etag) {
    return retrieveIfModified(enrollmentId, etag, RequestOptions.none());
  }

  /**
   * Retrieves the enrollment unless the copy you hold is current.
   *
   * @throws IllegalArgumentException when {@code cached} carries no ETag; nothing is sent
   */
  public Optional<Enrollment> retrieveIfModified(String enrollmentId, Enrollment cached) {
    return retrieveIfModified(enrollmentId, cached, RequestOptions.none());
  }

  /**
   * Retrieves the enrollment unless the copy you hold is current.
   *
   * @throws IllegalArgumentException when {@code cached} carries no ETag; nothing is sent
   */
  public Optional<Enrollment> retrieveIfModified(
      String enrollmentId, Enrollment cached, RequestOptions options) {
    return retrieveIfModified(enrollmentId, Etags.of(cached), options);
  }

  /**
   * Retrieves the enrollment only if it changed since you read the copy you hold: sends {@code
   * If-None-Match}, so an unchanged one costs no body.
   *
   * @param enrollmentId
   * @param etag the ETag of the copy you hold, from {@code getLastResponse().etag()}
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Enrollment} when it changed, or empty when the API answers 304 Not Modified
   *     and your copy is current
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Optional<Enrollment> retrieveIfModified(
      String enrollmentId, String etag, RequestOptions options) {
    Objects.requireNonNull(enrollmentId, "enrollmentId");
    Objects.requireNonNull(etag, "etag");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/enrollments/{enrollmentId}")
            .pathParam("enrollmentId", enrollmentId)
            .header("if-none-match", etag)
            .retrySafe()
            .build();
    return requester.requestIfModified(request, Enrollment.class, options);
  }
}
