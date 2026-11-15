// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class EnrollmentActionAccepted extends ApiResource {
  private final String enrollmentId;
  private final EnrollmentActionAcceptedAction action;

  @JsonCreator
  EnrollmentActionAccepted(
      @JsonProperty("enrollmentId") String enrollmentId,
      @JsonProperty("action") EnrollmentActionAcceptedAction action) {
    this.enrollmentId = enrollmentId;
    this.action = action;
  }

  /** The enrollment the request is for. */
  public String getEnrollmentId() {
    return enrollmentId;
  }

  /** The action that was accepted: <code>remind</code> or <code>resendInvite</code>. */
  public EnrollmentActionAcceptedAction getAction() {
    return action;
  }
}
