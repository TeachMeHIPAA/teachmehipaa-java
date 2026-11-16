// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.ComplianceSummary;
import com.teachmehipaa.param.ComplianceRetrieveParams;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Compliance in the client's Organization. Reach it through {@code client.compliance()}. */
public final class ComplianceService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public ComplianceService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Retrieve the compliance summary. */
  public ComplianceSummary retrieve() {
    return retrieve(ComplianceRetrieveParams.builder().build(), RequestOptions.none());
  }

  /** Retrieve the compliance summary. */
  public ComplianceSummary retrieve(RequestOptions options) {
    return retrieve(ComplianceRetrieveParams.builder().build(), options);
  }

  /** Retrieve the compliance summary. */
  public ComplianceSummary retrieve(ComplianceRetrieveParams params) {
    return retrieve(params, RequestOptions.none());
  }

  /**
   * Retrieve the compliance summary.
   *
   * <p>Counts the organization's learners in one course by status. Removed learners aren't counted.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link ComplianceSummary}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public ComplianceSummary retrieve(ComplianceRetrieveParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/compliance")
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.request(request, ComplianceSummary.class, options);
  }

  /** Retrieve the compliance summary. */
  public CompletableFuture<ComplianceSummary> retrieveAsync() {
    return retrieveAsync(ComplianceRetrieveParams.builder().build(), RequestOptions.none());
  }

  /** Retrieve the compliance summary. */
  public CompletableFuture<ComplianceSummary> retrieveAsync(RequestOptions options) {
    return retrieveAsync(ComplianceRetrieveParams.builder().build(), options);
  }

  /** Retrieve the compliance summary. */
  public CompletableFuture<ComplianceSummary> retrieveAsync(ComplianceRetrieveParams params) {
    return retrieveAsync(params, RequestOptions.none());
  }

  /**
   * Retrieve the compliance summary.
   *
   * <p>The asynchronous form of {@link #retrieve(ComplianceRetrieveParams, RequestOptions)}: it
   * sends the same request, with the same retries, without blocking the calling thread. The future
   * fails with the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a
   * {@code CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link ComplianceSummary}
   */
  public CompletableFuture<ComplianceSummary> retrieveAsync(
      ComplianceRetrieveParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/compliance")
            .queryParam("courseId", params.getCourseId())
            .retrySafe()
            .build();
    return requester.requestAsync(request, ComplianceSummary.class, options);
  }
}
