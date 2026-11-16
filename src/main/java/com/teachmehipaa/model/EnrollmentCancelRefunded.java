// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class EnrollmentCancelRefunded extends ApiResource
    implements CancelEnrollmentResponse {
  private final String enrollmentId;
  private final String subjectId;
  private final String courseId;
  private final String orgId;
  private final EnrollmentCancelRefundedStatus status;
  private final EnrollmentCancelRefundedEffectiveStatus effectiveStatus;
  private final boolean archived;
  private final long cycle;
  private final long rev;
  private final String invitedAt;
  private final String startedAt;
  private final String completedAt;
  private final String certifiedAt;
  private final String expiresAt;
  private final String certificateId;
  private final EnrollmentCancelRefundedRefund refund;

  @JsonCreator
  EnrollmentCancelRefunded(
      @JsonProperty("enrollmentId") String enrollmentId,
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("orgId") String orgId,
      @JsonProperty("status") EnrollmentCancelRefundedStatus status,
      @JsonProperty("effectiveStatus") EnrollmentCancelRefundedEffectiveStatus effectiveStatus,
      @JsonProperty("archived") boolean archived,
      @JsonProperty("cycle") long cycle,
      @JsonProperty("rev") long rev,
      @JsonProperty("invitedAt") String invitedAt,
      @JsonProperty("startedAt") String startedAt,
      @JsonProperty("completedAt") String completedAt,
      @JsonProperty("certifiedAt") String certifiedAt,
      @JsonProperty("expiresAt") String expiresAt,
      @JsonProperty("certificateId") String certificateId,
      @JsonProperty("refund") EnrollmentCancelRefundedRefund refund) {
    this.enrollmentId = enrollmentId;
    this.subjectId = subjectId;
    this.courseId = courseId;
    this.orgId = orgId;
    this.status = status;
    this.effectiveStatus = effectiveStatus;
    this.archived = archived;
    this.cycle = cycle;
    this.rev = rev;
    this.invitedAt = invitedAt;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
    this.certifiedAt = certifiedAt;
    this.expiresAt = expiresAt;
    this.certificateId = certificateId;
    this.refund = refund;
  }

  public String getEnrollmentId() {
    return enrollmentId;
  }

  public String getSubjectId() {
    return subjectId;
  }

  public String getCourseId() {
    return courseId;
  }

  public String getOrgId() {
    return orgId;
  }

  public EnrollmentCancelRefundedStatus getStatus() {
    return status;
  }

  public EnrollmentCancelRefundedEffectiveStatus getEffectiveStatus() {
    return effectiveStatus;
  }

  public boolean isArchived() {
    return archived;
  }

  public long getCycle() {
    return cycle;
  }

  public long getRev() {
    return rev;
  }

  /** May be {@code null}. */
  public String getInvitedAt() {
    return invitedAt;
  }

  /** May be {@code null}. */
  public String getStartedAt() {
    return startedAt;
  }

  /** May be {@code null}. */
  public String getCompletedAt() {
    return completedAt;
  }

  /** May be {@code null}. */
  public String getCertifiedAt() {
    return certifiedAt;
  }

  /** May be {@code null}. */
  public String getExpiresAt() {
    return expiresAt;
  }

  /** May be {@code null}. */
  public String getCertificateId() {
    return certificateId;
  }

  public EnrollmentCancelRefundedRefund getRefund() {
    return refund;
  }
}
