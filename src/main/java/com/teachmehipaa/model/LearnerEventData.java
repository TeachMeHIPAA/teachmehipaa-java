// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;
import java.time.OffsetDateTime;

public final class LearnerEventData extends ApiModel {
  private final String learnerId;
  private final String email;
  private final String externalId;
  private final String courseId;
  private final LearnerStatus status;
  private final OffsetDateTime occurredAt;

  @JsonCreator
  LearnerEventData(
      @JsonProperty("learnerId") String learnerId,
      @JsonProperty("email") String email,
      @JsonProperty("externalId") String externalId,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("status") LearnerStatus status,
      @JsonProperty("occurredAt") OffsetDateTime occurredAt) {
    this.learnerId = learnerId;
    this.email = email;
    this.externalId = externalId;
    this.courseId = courseId;
    this.status = status;
    this.occurredAt = occurredAt;
  }

  public String getLearnerId() {
    return learnerId;
  }

  public String getEmail() {
    return email;
  }

  /** May be {@code null}. */
  public String getExternalId() {
    return externalId;
  }

  public String getCourseId() {
    return courseId;
  }

  public LearnerStatus getStatus() {
    return status;
  }

  public OffsetDateTime getOccurredAt() {
    return occurredAt;
  }
}
