// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;
import java.time.OffsetDateTime;

/** The learner's current certificate, with its verification and PDF links. */
public final class LearnerCertificate extends ApiModel {
  private final String certificateId;
  private final OffsetDateTime issuedAt;
  private final OffsetDateTime expiresAt;
  private final String verifyUrl;
  private final String pdfUrl;

  @JsonCreator
  LearnerCertificate(
      @JsonProperty("certificateId") String certificateId,
      @JsonProperty("issuedAt") OffsetDateTime issuedAt,
      @JsonProperty("expiresAt") OffsetDateTime expiresAt,
      @JsonProperty("verifyUrl") String verifyUrl,
      @JsonProperty("pdfUrl") String pdfUrl) {
    this.certificateId = certificateId;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.verifyUrl = verifyUrl;
    this.pdfUrl = pdfUrl;
  }

  public String getCertificateId() {
    return certificateId;
  }

  public OffsetDateTime getIssuedAt() {
    return issuedAt;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  /** May be {@code null}. */
  public String getVerifyUrl() {
    return verifyUrl;
  }

  public String getPdfUrl() {
    return pdfUrl;
  }
}
