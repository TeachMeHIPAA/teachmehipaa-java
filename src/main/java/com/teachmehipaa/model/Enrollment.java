// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class Enrollment extends ApiResource implements CancelEnrollmentResponse {
  private final String enrollmentId;
  private final String subjectId;
  private final String courseId;
  private final String orgId;
  private final EnrollmentStatus status;
  private final EnrollmentEffectiveStatus effectiveStatus;
  private final boolean archived;
  private final long cycle;
  private final long rev;
  private final String invitedAt;
  private final String startedAt;
  private final String completedAt;
  private final String certifiedAt;
  private final String expiresAt;
  private final String certificateId;

  @JsonCreator
  Enrollment(
      @JsonProperty("enrollmentId") String enrollmentId,
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("orgId") String orgId,
      @JsonProperty("status") EnrollmentStatus status,
      @JsonProperty("effectiveStatus") EnrollmentEffectiveStatus effectiveStatus,
      @JsonProperty("archived") boolean archived,
      @JsonProperty("cycle") long cycle,
      @JsonProperty("rev") long rev,
      @JsonProperty("invitedAt") String invitedAt,
      @JsonProperty("startedAt") String startedAt,
      @JsonProperty("completedAt") String completedAt,
      @JsonProperty("certifiedAt") String certifiedAt,
      @JsonProperty("expiresAt") String expiresAt,
      @JsonProperty("certificateId") String certificateId) {
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
  }

  /** Unique identifier for the enrollment. */
  public String getEnrollmentId() {
    return enrollmentId;
  }

  /** The learner's member id. */
  public String getSubjectId() {
    return subjectId;
  }

  /** The course. */
  public String getCourseId() {
    return courseId;
  }

  /** The organization's id. */
  public String getOrgId() {
    return orgId;
  }

  /**
   * The stored lifecycle state: <code>INVITED</code>, <code>EMAIL_BOUNCED</code>, <code>STARTED
   * </code>, <code>ASSESSMENT_IN_PROGRESS</code>, <code>CERTIFIED</code> or <code>CANCELLED</code>.
   */
  public EnrollmentStatus getStatus() {
    return status;
  }

  /**
   * The status to display. Adds <code>EXPIRING</code>, <code>EXPIRED</code> and <code>ARCHIVED
   * </code>, derived when you read it.
   */
  public EnrollmentEffectiveStatus getEffectiveStatus() {
    return effectiveStatus;
  }

  /** Whether the enrollment is archived. */
  public boolean isArchived() {
    return archived;
  }

  /** The certification cycle, starting at 0. Renewing adds one. */
  public long getCycle() {
    return cycle;
  }

  /** The resource's revision. Its strong <code>ETag</code> is derived from it. */
  public long getRev() {
    return rev;
  }

  /** When the learner was invited (ISO 8601). May be {@code null}. */
  public String getInvitedAt() {
    return invitedAt;
  }

  /** When the learner started. May be {@code null}. */
  public String getStartedAt() {
    return startedAt;
  }

  /** When the learner completed the course. May be {@code null}. */
  public String getCompletedAt() {
    return completedAt;
  }

  /** When the learner was certified. May be {@code null}. */
  public String getCertifiedAt() {
    return certifiedAt;
  }

  /** When the certification expires. May be {@code null}. */
  public String getExpiresAt() {
    return expiresAt;
  }

  /** The certificate issued for this cycle, if any. May be {@code null}. */
  public String getCertificateId() {
    return certificateId;
  }
}
