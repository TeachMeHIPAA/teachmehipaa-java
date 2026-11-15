package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerRetrieveParams;
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
            .build();
    return requester.request(request, Learner.class, options);
  }
}
