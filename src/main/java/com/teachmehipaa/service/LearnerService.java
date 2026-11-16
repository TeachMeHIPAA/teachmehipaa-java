// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerLaunchLink;
import com.teachmehipaa.model.LearnerRemindAccepted;
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.LearnerBatchUpsertParams;
import com.teachmehipaa.param.LearnerCreateLaunchLinkParams;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerExportParams;
import com.teachmehipaa.param.LearnerListParams;
import com.teachmehipaa.param.LearnerOffboardParams;
import com.teachmehipaa.param.LearnerRemindParams;
import com.teachmehipaa.param.LearnerRetrieveParams;
import com.teachmehipaa.param.LearnerUpdateParams;
import com.teachmehipaa.param.LearnerUpsertByExternalIdParams;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Learners in the client's Organization. Reach it through {@code client.learners()}. */
public final class LearnerService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public LearnerService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Create many learners. */
  public Operation batchUpsert(LearnerBatchUpsertParams params) {
    return batchUpsert(params, RequestOptions.none());
  }

  /**
   * Create many learners.
   *
   * <p>Creates or retrieves up to 500 learners in one request. Each item works like creating one
   * learner and succeeds or fails on its own.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which may finish in the background: {@code waitForCompletion()}
   *     polls until it is done
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation batchUpsert(LearnerBatchUpsertParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners:batchUpsert")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Create many learners. */
  public CompletableFuture<Operation> batchUpsertAsync(LearnerBatchUpsertParams params) {
    return batchUpsertAsync(params, RequestOptions.none());
  }

  /**
   * Create many learners.
   *
   * <p>The asynchronous form of {@link #batchUpsert(LearnerBatchUpsertParams, RequestOptions)}: it
   * sends the same request, with the same retries, without blocking the calling thread. The future
   * fails with the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a
   * {@code CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Operation}, which may finish in the background: {@code
   *     waitForCompletion()} polls until it is done
   */
  public CompletableFuture<Operation> batchUpsertAsync(
      LearnerBatchUpsertParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners:batchUpsert")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestAsync(request, Operation.class, options);
  }

  /** Create a learner. */
  public LearnerWriteResult create(LearnerCreateParams params) {
    return create(params, RequestOptions.none());
  }

  /**
   * Create a learner.
   *
   * <p>Adds a person to the organization and invites them to a course in one call, using one seat.
   * If the email or <code>externalId</code> already belongs to an active learner in the course,
   * nothing is created. With <code>delivery: "none"</code>, no email is sent and the response
   * includes a <code>joinUrl</code> for you to deliver.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the Learner; {@code isCreated()} is true when this call created it (201) and false when
   *     it already existed (200)
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public LearnerWriteResult create(LearnerCreateParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestWriteResult(request, LearnerWriteResult.class, options);
  }

  /** Create a learner. */
  public CompletableFuture<LearnerWriteResult> createAsync(LearnerCreateParams params) {
    return createAsync(params, RequestOptions.none());
  }

  /**
   * Create a learner.
   *
   * <p>The asynchronous form of {@link #create(LearnerCreateParams, RequestOptions)}: it sends the
   * same request, with the same retries, without blocking the calling thread. The future fails with
   * the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the Learner; {@code isCreated()} is true when this call created it (201)
   *     and false when it already existed (200)
   */
  public CompletableFuture<LearnerWriteResult> createAsync(
      LearnerCreateParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestWriteResultAsync(request, LearnerWriteResult.class, options);
  }

  /** Create a launch link. */
  public LearnerLaunchLink createLaunchLink(String learnerId) {
    return createLaunchLink(
        learnerId, LearnerCreateLaunchLinkParams.builder().build(), RequestOptions.none());
  }

  /** Create a launch link. */
  public LearnerLaunchLink createLaunchLink(String learnerId, RequestOptions options) {
    return createLaunchLink(learnerId, LearnerCreateLaunchLinkParams.builder().build(), options);
  }

  /** Create a launch link. */
  public LearnerLaunchLink createLaunchLink(
      String learnerId, LearnerCreateLaunchLinkParams params) {
    return createLaunchLink(learnerId, params, RequestOptions.none());
  }

  /**
   * Create a launch link.
   *
   * <p>Creates a link that opens the course with the learner already signed in, for a "Start
   * training" button in your product. The link works once and expires after 5 minutes: create it
   * when the learner clicks, then redirect them to it. Never email, store or log it. Only for a
   * learner who has accepted their invite.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link LearnerLaunchLink}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public LearnerLaunchLink createLaunchLink(
      String learnerId, LearnerCreateLaunchLinkParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners/{learnerId}:createLaunchLink")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.request(request, LearnerLaunchLink.class, options);
  }

  /** Create a launch link. */
  public CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(String learnerId) {
    return createLaunchLinkAsync(
        learnerId, LearnerCreateLaunchLinkParams.builder().build(), RequestOptions.none());
  }

  /** Create a launch link. */
  public CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(
      String learnerId, RequestOptions options) {
    return createLaunchLinkAsync(
        learnerId, LearnerCreateLaunchLinkParams.builder().build(), options);
  }

  /** Create a launch link. */
  public CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(
      String learnerId, LearnerCreateLaunchLinkParams params) {
    return createLaunchLinkAsync(learnerId, params, RequestOptions.none());
  }

  /**
   * Create a launch link.
   *
   * <p>The asynchronous form of {@link #createLaunchLink(String, LearnerCreateLaunchLinkParams,
   * RequestOptions)}: it sends the same request, with the same retries, without blocking the
   * calling thread. The future fails with the {@code TeachMeHipaaException} subclass that method
   * throws, never wrapped in a {@code CompletionException}. Cancelling it stops the call and its
   * retries.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link LearnerLaunchLink}
   */
  public CompletableFuture<LearnerLaunchLink> createLaunchLinkAsync(
      String learnerId, LearnerCreateLaunchLinkParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners/{learnerId}:createLaunchLink")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.requestAsync(request, LearnerLaunchLink.class, options);
  }

  /** Export learners. */
  public Operation export(LearnerExportParams params) {
    return export(params, RequestOptions.none());
  }

  /**
   * Export learners.
   *
   * <p>Exports the organization's learners in one course as CSV, optionally only those with one
   * <code>status</code>. Requires the <code>learners:read</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which may finish in the background: {@code waitForCompletion()}
   *     polls until it is done
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation export(LearnerExportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners:export")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Export learners. */
  public CompletableFuture<Operation> exportAsync(LearnerExportParams params) {
    return exportAsync(params, RequestOptions.none());
  }

  /**
   * Export learners.
   *
   * <p>The asynchronous form of {@link #export(LearnerExportParams, RequestOptions)}: it sends the
   * same request, with the same retries, without blocking the calling thread. The future fails with
   * the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Operation}, which may finish in the background: {@code
   *     waitForCompletion()} polls until it is done
   */
  public CompletableFuture<Operation> exportAsync(
      LearnerExportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners:export")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestAsync(request, Operation.class, options);
  }

  /** List all learners. */
  public Page<Learner> list() {
    return list(LearnerListParams.builder().build(), RequestOptions.none());
  }

  /** List all learners. */
  public Page<Learner> list(RequestOptions options) {
    return list(LearnerListParams.builder().build(), options);
  }

  /** List all learners. */
  public Page<Learner> list(LearnerListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all learners.
   *
   * <p>Returns a list of the organization's learners in one course, with each learner's status,
   * dates and current certificate. Filter by <code>status</code>, or find one learner by <code>
   * email</code> or <code>externalId</code>.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link Learner}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<Learner> list(LearnerListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/learners")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("email", params.getEmail())
            .queryParam("externalId", params.getExternalId())
            .queryParam("status", params.getStatus())
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.requestPage(request, Pagination.cursor("cursor"), Learner.class, options);
  }

  /** List all learners. */
  public CompletableFuture<Page<Learner>> listAsync() {
    return listAsync(LearnerListParams.builder().build(), RequestOptions.none());
  }

  /** List all learners. */
  public CompletableFuture<Page<Learner>> listAsync(RequestOptions options) {
    return listAsync(LearnerListParams.builder().build(), options);
  }

  /** List all learners. */
  public CompletableFuture<Page<Learner>> listAsync(LearnerListParams params) {
    return listAsync(params, RequestOptions.none());
  }

  /**
   * List all learners.
   *
   * <p>The asynchronous form of {@link #list(LearnerListParams, RequestOptions)}: it sends the same
   * request, with the same retries, without blocking the calling thread. The future fails with the
   * {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of one page of {@link Learner}; {@code nextCursor()} is {@code null} on the
   *     last
   */
  public CompletableFuture<Page<Learner>> listAsync(
      LearnerListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/learners")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("email", params.getEmail())
            .queryParam("externalId", params.getExternalId())
            .queryParam("status", params.getStatus())
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.requestPageAsync(request, Pagination.cursor("cursor"), Learner.class, options);
  }

  /** Offboard a learner. */
  public Learner offboard(String learnerId) {
    return offboard(learnerId, LearnerOffboardParams.builder().build(), RequestOptions.none());
  }

  /** Offboard a learner. */
  public Learner offboard(String learnerId, RequestOptions options) {
    return offboard(learnerId, LearnerOffboardParams.builder().build(), options);
  }

  /** Offboard a learner. */
  public Learner offboard(String learnerId, LearnerOffboardParams params) {
    return offboard(learnerId, params, RequestOptions.none());
  }

  /**
   * Offboard a learner.
   *
   * <p>Removes a learner from the organization. A learner who hasn't started has their invite
   * cancelled and the seat returned; a learner who has started keeps their training history and
   * certificates. Releases the learner's <code>externalId</code>.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Learner}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Learner offboard(String learnerId, LearnerOffboardParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners/{learnerId}:offboard")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .header("if-match", params.getIfMatch())
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, Learner.class, options);
  }

  /** Offboard a learner. */
  public CompletableFuture<Learner> offboardAsync(String learnerId) {
    return offboardAsync(learnerId, LearnerOffboardParams.builder().build(), RequestOptions.none());
  }

  /** Offboard a learner. */
  public CompletableFuture<Learner> offboardAsync(String learnerId, RequestOptions options) {
    return offboardAsync(learnerId, LearnerOffboardParams.builder().build(), options);
  }

  /** Offboard a learner. */
  public CompletableFuture<Learner> offboardAsync(String learnerId, LearnerOffboardParams params) {
    return offboardAsync(learnerId, params, RequestOptions.none());
  }

  /**
   * Offboard a learner.
   *
   * <p>The asynchronous form of {@link #offboard(String, LearnerOffboardParams, RequestOptions)}:
   * it sends the same request, with the same retries, without blocking the calling thread. The
   * future fails with the {@code TeachMeHipaaException} subclass that method throws, never wrapped
   * in a {@code CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Learner}
   */
  public CompletableFuture<Learner> offboardAsync(
      String learnerId, LearnerOffboardParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners/{learnerId}:offboard")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .header("if-match", params.getIfMatch())
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.requestAsync(request, Learner.class, options);
  }

  /** Remind a learner. */
  public LearnerRemindAccepted remind(String learnerId) {
    return remind(learnerId, LearnerRemindParams.builder().build(), RequestOptions.none());
  }

  /** Remind a learner. */
  public LearnerRemindAccepted remind(String learnerId, RequestOptions options) {
    return remind(learnerId, LearnerRemindParams.builder().build(), options);
  }

  /** Remind a learner. */
  public LearnerRemindAccepted remind(String learnerId, LearnerRemindParams params) {
    return remind(learnerId, params, RequestOptions.none());
  }

  /**
   * Remind a learner.
   *
   * <p>Emails the learner a reminder: the invite again if they haven't started, or a nudge to
   * finish if they have.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link LearnerRemindAccepted}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public LearnerRemindAccepted remind(
      String learnerId, LearnerRemindParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners/{learnerId}:remind")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, LearnerRemindAccepted.class, options);
  }

  /** Remind a learner. */
  public CompletableFuture<LearnerRemindAccepted> remindAsync(String learnerId) {
    return remindAsync(learnerId, LearnerRemindParams.builder().build(), RequestOptions.none());
  }

  /** Remind a learner. */
  public CompletableFuture<LearnerRemindAccepted> remindAsync(
      String learnerId, RequestOptions options) {
    return remindAsync(learnerId, LearnerRemindParams.builder().build(), options);
  }

  /** Remind a learner. */
  public CompletableFuture<LearnerRemindAccepted> remindAsync(
      String learnerId, LearnerRemindParams params) {
    return remindAsync(learnerId, params, RequestOptions.none());
  }

  /**
   * Remind a learner.
   *
   * <p>The asynchronous form of {@link #remind(String, LearnerRemindParams, RequestOptions)}: it
   * sends the same request, with the same retries, without blocking the calling thread. The future
   * fails with the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a
   * {@code CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link LearnerRemindAccepted}
   */
  public CompletableFuture<LearnerRemindAccepted> remindAsync(
      String learnerId, LearnerRemindParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners/{learnerId}:remind")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.requestAsync(request, LearnerRemindAccepted.class, options);
  }

  /** Retrieve a learner. */
  public Learner retrieve(String learnerId) {
    return retrieve(learnerId, LearnerRetrieveParams.builder().build(), RequestOptions.none());
  }

  /** Retrieve a learner. */
  public Learner retrieve(String learnerId, RequestOptions options) {
    return retrieve(learnerId, LearnerRetrieveParams.builder().build(), options);
  }

  /** Retrieve a learner. */
  public Learner retrieve(String learnerId, LearnerRetrieveParams params) {
    return retrieve(learnerId, params, RequestOptions.none());
  }

  /**
   * Retrieve a learner.
   *
   * <p>Retrieves a learner's status in one course.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Learner}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Learner retrieve(String learnerId, LearnerRetrieveParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/learners/{learnerId}")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.request(request, Learner.class, options);
  }

  /** Retrieve a learner. */
  public CompletableFuture<Learner> retrieveAsync(String learnerId) {
    return retrieveAsync(learnerId, LearnerRetrieveParams.builder().build(), RequestOptions.none());
  }

  /** Retrieve a learner. */
  public CompletableFuture<Learner> retrieveAsync(String learnerId, RequestOptions options) {
    return retrieveAsync(learnerId, LearnerRetrieveParams.builder().build(), options);
  }

  /** Retrieve a learner. */
  public CompletableFuture<Learner> retrieveAsync(String learnerId, LearnerRetrieveParams params) {
    return retrieveAsync(learnerId, params, RequestOptions.none());
  }

  /**
   * Retrieve a learner.
   *
   * <p>The asynchronous form of {@link #retrieve(String, LearnerRetrieveParams, RequestOptions)}:
   * it sends the same request, with the same retries, without blocking the calling thread. The
   * future fails with the {@code TeachMeHipaaException} subclass that method throws, never wrapped
   * in a {@code CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Learner}
   */
  public CompletableFuture<Learner> retrieveAsync(
      String learnerId, LearnerRetrieveParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/learners/{learnerId}")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.requestAsync(request, Learner.class, options);
  }

  /** Update a learner. */
  public Learner update(String learnerId, LearnerUpdateParams params) {
    return update(learnerId, params, RequestOptions.none());
  }

  /**
   * Update a learner.
   *
   * <p>Updates a learner's <code>name</code> or <code>externalId</code>; parameters you don't send
   * are left unchanged. Send the body as <code>application/merge-patch+json</code> with the
   * learner's current <code>ETag</code> in <code>If-Match</code>.
   *
   * <p>Never retried: a lost response may hide a change that already happened.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Learner}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Learner update(String learnerId, LearnerUpdateParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("PATCH", "/organizations/{orgId}/learners/{learnerId}")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .header("if-match", params.getIfMatch())
            .mergePatchBody(params)
            .build();
    return requester.request(request, Learner.class, options);
  }

  /** Update a learner. */
  public CompletableFuture<Learner> updateAsync(String learnerId, LearnerUpdateParams params) {
    return updateAsync(learnerId, params, RequestOptions.none());
  }

  /**
   * Update a learner.
   *
   * <p>The asynchronous form of {@link #update(String, LearnerUpdateParams, RequestOptions)}: it
   * sends the same request, with the same retries, without blocking the calling thread. The future
   * fails with the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a
   * {@code CompletionException}. Cancelling it stops the call and its retries.
   *
   * <p>Never retried: a lost response may hide a change that already happened.
   *
   * @param learnerId The learner's canonical subject id (sub_… ULID).
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Learner}
   */
  public CompletableFuture<Learner> updateAsync(
      String learnerId, LearnerUpdateParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("PATCH", "/organizations/{orgId}/learners/{learnerId}")
            .pathParam("learnerId", learnerId)
            .queryParam("courseId", params.getCourseId())
            .header("if-match", params.getIfMatch())
            .mergePatchBody(params)
            .build();
    return requester.requestAsync(request, Learner.class, options);
  }

  /** Create or retrieve a learner by your id. */
  public LearnerWriteResult upsertByExternalId(
      String externalId, LearnerUpsertByExternalIdParams params) {
    return upsertByExternalId(externalId, params, RequestOptions.none());
  }

  /**
   * Create or retrieve a learner by your id.
   *
   * <p>Makes sure a learner exists for your own id. Creates and invites the learner the first time,
   * and returns them on every later call, so a sync job can replay it safely. Never uses a second
   * seat.
   *
   * @param externalId Your id for the learner, URL-encoded.
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the Learner; {@code isCreated()} is true when this call created it (201) and false when
   *     it already existed (200)
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public LearnerWriteResult upsertByExternalId(
      String externalId, LearnerUpsertByExternalIdParams params, RequestOptions options) {
    Objects.requireNonNull(externalId, "externalId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("PUT", "/organizations/{orgId}/learners/by-external-id/{externalId}")
            .pathParam("externalId", externalId)
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestWriteResult(request, LearnerWriteResult.class, options);
  }

  /** Create or retrieve a learner by your id. */
  public CompletableFuture<LearnerWriteResult> upsertByExternalIdAsync(
      String externalId, LearnerUpsertByExternalIdParams params) {
    return upsertByExternalIdAsync(externalId, params, RequestOptions.none());
  }

  /**
   * Create or retrieve a learner by your id.
   *
   * <p>The asynchronous form of {@link #upsertByExternalId(String, LearnerUpsertByExternalIdParams,
   * RequestOptions)}: it sends the same request, with the same retries, without blocking the
   * calling thread. The future fails with the {@code TeachMeHipaaException} subclass that method
   * throws, never wrapped in a {@code CompletionException}. Cancelling it stops the call and its
   * retries.
   *
   * @param externalId Your id for the learner, URL-encoded.
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the Learner; {@code isCreated()} is true when this call created it (201)
   *     and false when it already existed (200)
   */
  public CompletableFuture<LearnerWriteResult> upsertByExternalIdAsync(
      String externalId, LearnerUpsertByExternalIdParams params, RequestOptions options) {
    Objects.requireNonNull(externalId, "externalId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("PUT", "/organizations/{orgId}/learners/by-external-id/{externalId}")
            .pathParam("externalId", externalId)
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestWriteResultAsync(request, LearnerWriteResult.class, options);
  }
}
