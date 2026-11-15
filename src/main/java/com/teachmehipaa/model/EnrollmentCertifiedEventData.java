// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class EnrollmentCertifiedEventData extends ApiModel {
  private final String subjectId;
  private final String courseId;
  private final String enrollmentId;

  @JsonCreator
  EnrollmentCertifiedEventData(
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("enrollmentId") String enrollmentId) {
    this.subjectId = subjectId;
    this.courseId = courseId;
    this.enrollmentId = enrollmentId;
  }

  public String getSubjectId() {
    return subjectId;
  }

  public String getCourseId() {
    return courseId;
  }

  /** May be {@code null}. */
  public String getEnrollmentId() {
    return enrollmentId;
  }
}
