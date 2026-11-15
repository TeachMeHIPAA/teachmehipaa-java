// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class EnrollmentsBatchResultsResultsItem extends ApiModel {
  private final String enrollmentId;
  private final EnrollmentsBatchResultsResultsItemOutcome outcome;
  private final Problem problem;

  @JsonCreator
  EnrollmentsBatchResultsResultsItem(
      @JsonProperty("enrollmentId") String enrollmentId,
      @JsonProperty("outcome") EnrollmentsBatchResultsResultsItemOutcome outcome,
      @JsonProperty("problem") Problem problem) {
    this.enrollmentId = enrollmentId;
    this.outcome = outcome;
    this.problem = problem;
  }

  public String getEnrollmentId() {
    return enrollmentId;
  }

  public EnrollmentsBatchResultsResultsItemOutcome getOutcome() {
    return outcome;
  }

  /** May be {@code null}. */
  public Problem getProblem() {
    return problem;
  }
}
