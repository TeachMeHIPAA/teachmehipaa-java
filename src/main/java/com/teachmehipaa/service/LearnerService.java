package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerCreateParams;
import com.teachmehipaa.param.LearnerRetrieveParams;
import com.teachmehipaa.param.LearnerUpdateParams;
import java.util.Objects;

/** Learners in the client's Organization. Reach it through {@code client.learners()}. */
public final class LearnerService {
  private final ApiRequester requester;

  public LearnerService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Retrieves a Learner's status in the Organization's default Course. */
  public Learner retrieve(String learnerId) {
    return retrieve(learnerId, LearnerRetrieveParams.none(), RequestOptions.none());
  }

  /** Retrieves a Learner's status in one Course. */
  public Learner retrieve(String learnerId, LearnerRetrieveParams params) {
    return retrieve(learnerId, params, RequestOptions.none());
  }

  /** Retrieves a Learner's status in the Organization's default Course. */
  public Learner retrieve(String learnerId, RequestOptions options) {
    return retrieve(learnerId, LearnerRetrieveParams.none(), options);
  }

  /**
   * Retrieves a Learner's status in one Course.
   *
   * @param learnerId the Learner's id ({@code sub_…})
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails, for example with
   *     status 404 when no such Learner is in the Organization
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

  /** Invites a Learner to a Course. */
  public Learner create(LearnerCreateParams params) {
    return create(params, RequestOptions.none());
  }

  /**
   * Invites a Learner to a Course. The call carries a generated Idempotency-Key, so retrying it
   * never invites twice.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Learner create(LearnerCreateParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/learners")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Learner.class, options);
  }

  /** Changes a Learner's name or External ID. */
  public Learner update(String learnerId, LearnerUpdateParams params) {
    return update(learnerId, params, RequestOptions.none());
  }

  /**
   * Changes a Learner's name or External ID. Never retried: a lost response may hide a change that
   * already happened.
   *
   * @param learnerId the Learner's id ({@code sub_…})
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Learner update(String learnerId, LearnerUpdateParams params, RequestOptions options) {
    Objects.requireNonNull(learnerId, "learnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("PATCH", "/organizations/{orgId}/learners/{learnerId}")
            .pathParam("learnerId", learnerId)
            .mergePatchBody(params)
            .build();
    return requester.request(request, Learner.class, options);
  }
}
