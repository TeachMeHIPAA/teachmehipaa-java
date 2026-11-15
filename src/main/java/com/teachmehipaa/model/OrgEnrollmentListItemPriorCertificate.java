// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class OrgEnrollmentListItemPriorCertificate extends ApiModel {
  private final String certificateId;
  private final String issuedAt;
  private final String expiresAt;
  private final OrgEnrollmentListItemPriorCertificateStatus status;

  @JsonCreator
  OrgEnrollmentListItemPriorCertificate(
      @JsonProperty("certificateId") String certificateId,
      @JsonProperty("issuedAt") String issuedAt,
      @JsonProperty("expiresAt") String expiresAt,
      @JsonProperty("status") OrgEnrollmentListItemPriorCertificateStatus status) {
    this.certificateId = certificateId;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.status = status;
  }

  public String getCertificateId() {
    return certificateId;
  }

  /** May be {@code null}. */
  public String getIssuedAt() {
    return issuedAt;
  }

  /** May be {@code null}. */
  public String getExpiresAt() {
    return expiresAt;
  }

  public OrgEnrollmentListItemPriorCertificateStatus getStatus() {
    return status;
  }
}
