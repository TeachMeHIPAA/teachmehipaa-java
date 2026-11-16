// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class Certificate extends ApiResource {
  private final String certificateId;
  private final String subjectId;
  private final CertificateHolderStatus holderStatus;
  private final String courseId;
  private final String courseVersion;
  private final String issuedAt;
  private final String expiresAt;
  private final CertificateStatus status;
  private final String revokedAt;

  @JsonCreator
  Certificate(
      @JsonProperty("certificateId") String certificateId,
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("holderStatus") CertificateHolderStatus holderStatus,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("courseVersion") String courseVersion,
      @JsonProperty("issuedAt") String issuedAt,
      @JsonProperty("expiresAt") String expiresAt,
      @JsonProperty("status") CertificateStatus status,
      @JsonProperty("revokedAt") String revokedAt) {
    this.certificateId = certificateId;
    this.subjectId = subjectId;
    this.holderStatus = holderStatus;
    this.courseId = courseId;
    this.courseVersion = courseVersion;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.status = status;
    this.revokedAt = revokedAt;
  }

  /** Unique identifier for the certificate. */
  public String getCertificateId() {
    return certificateId;
  }

  /** The holder's member id. May be {@code null}. */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * Whether the holder could be resolved: <code>resolved</code>, <code>none</code> or <code>
   * lookup_failed</code>. May be {@code null}.
   */
  public CertificateHolderStatus getHolderStatus() {
    return holderStatus;
  }

  /** The course the certificate is for. */
  public String getCourseId() {
    return courseId;
  }

  /** The course version the holder passed. May be {@code null}. */
  public String getCourseVersion() {
    return courseVersion;
  }

  /** When it was issued (ISO 8601). May be {@code null}. */
  public String getIssuedAt() {
    return issuedAt;
  }

  /** When it expires. May be {@code null}. */
  public String getExpiresAt() {
    return expiresAt;
  }

  /** <code>VALID</code>, <code>EXPIRED</code> or <code>REVOKED</code>. */
  public CertificateStatus getStatus() {
    return status;
  }

  /** When it was revoked, if it was. May be {@code null}. */
  public String getRevokedAt() {
    return revokedAt;
  }
}
