// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class CertificateVerification extends ApiResource {
  private final CertificateVerificationStatus status;
  private final String learnerName;
  private final String courseTitle;
  private final OffsetDateTime issuedAt;
  private final OffsetDateTime expiresAt;
  private final CertificateVerificationIssuer issuer;

  @JsonCreator
  CertificateVerification(
      @JsonProperty("status") CertificateVerificationStatus status,
      @JsonProperty("learnerName") String learnerName,
      @JsonProperty("courseTitle") String courseTitle,
      @JsonProperty("issuedAt") OffsetDateTime issuedAt,
      @JsonProperty("expiresAt") OffsetDateTime expiresAt,
      @JsonProperty("issuer") CertificateVerificationIssuer issuer) {
    this.status = status;
    this.learnerName = learnerName;
    this.courseTitle = courseTitle;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.issuer = issuer;
  }

  /** <code>valid</code>, <code>expired</code> or <code>revoked</code>. */
  public CertificateVerificationStatus getStatus() {
    return status;
  }

  /** The name printed on the certificate. */
  public String getLearnerName() {
    return learnerName;
  }

  /** The course. */
  public String getCourseTitle() {
    return courseTitle;
  }

  /** When it was issued (ISO 8601). */
  public OffsetDateTime getIssuedAt() {
    return issuedAt;
  }

  /** When it expires. */
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  /** Who issued it. */
  public CertificateVerificationIssuer getIssuer() {
    return issuer;
  }
}
