// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class LearnerRemindAccepted extends ApiResource {
  private final String learnerId;
  private final LearnerRemindAcceptedAction action;

  @JsonCreator
  LearnerRemindAccepted(
      @JsonProperty("learnerId") String learnerId,
      @JsonProperty("action") LearnerRemindAcceptedAction action) {
    this.learnerId = learnerId;
    this.action = action;
  }

  /** The learner the reminder is for. */
  public String getLearnerId() {
    return learnerId;
  }

  /** Always <code>remind</code>. */
  public LearnerRemindAcceptedAction getAction() {
    return action;
  }
}
