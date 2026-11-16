// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class CertificateRevokedEventData extends ApiModel {
  private final String certificateId;
  private final String learnerId;
  private final String courseId;

  @JsonCreator
  CertificateRevokedEventData(
      @JsonProperty("certificateId") String certificateId,
      @JsonProperty("learnerId") String learnerId,
      @JsonProperty("courseId") String courseId) {
    this.certificateId = certificateId;
    this.learnerId = learnerId;
    this.courseId = courseId;
  }

  public String getCertificateId() {
    return certificateId;
  }

  public String getLearnerId() {
    return learnerId;
  }

  public String getCourseId() {
    return courseId;
  }
}
