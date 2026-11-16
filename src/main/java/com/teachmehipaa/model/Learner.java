// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class Learner extends ApiResource {
  private final String learnerId;
  private final String email;
  private final String name;
  private final String externalId;
  private final LearnerStatus status;
  private final LearnerCourse course;
  private final OffsetDateTime invitedAt;
  private final OffsetDateTime startedAt;
  private final OffsetDateTime completedAt;
  private final OffsetDateTime dueAt;
  private final LearnerCertificate certificate;

  @JsonCreator
  Learner(
      @JsonProperty("learnerId") String learnerId,
      @JsonProperty("email") String email,
      @JsonProperty("name") String name,
      @JsonProperty("externalId") String externalId,
      @JsonProperty("status") LearnerStatus status,
      @JsonProperty("course") LearnerCourse course,
      @JsonProperty("invitedAt") OffsetDateTime invitedAt,
      @JsonProperty("startedAt") OffsetDateTime startedAt,
      @JsonProperty("completedAt") OffsetDateTime completedAt,
      @JsonProperty("dueAt") OffsetDateTime dueAt,
      @JsonProperty("certificate") LearnerCertificate certificate) {
    this.learnerId = learnerId;
    this.email = email;
    this.name = name;
    this.externalId = externalId;
    this.status = status;
    this.course = course;
    this.invitedAt = invitedAt;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
    this.dueAt = dueAt;
    this.certificate = certificate;
  }

  /** Unique identifier for the learner. The same as the member's <code>subjectId</code>. */
  public String getLearnerId() {
    return learnerId;
  }

  /** The learner's email address. */
  public String getEmail() {
    return email;
  }

  /** The learner's full name. May be {@code null}. */
  public String getName() {
    return name;
  }

  /** Your own id for the learner, unique within the organization. May be {@code null}. */
  public String getExternalId() {
    return externalId;
  }

  /**
   * The learner's training status in this course. One of <code>not_started</code> (invited), <code>
   * invite_bounced</code>, <code>in_progress</code>, <code>compliant</code>, <code>expiring_soon
   * </code> (within 60 days of expiry), <code>expired</code> or <code>removed</code>.
   */
  public LearnerStatus getStatus() {
    return status;
  }

  /** The course this learner record is for. */
  public LearnerCourse getCourse() {
    return course;
  }

  /** When the learner was invited (ISO 8601). May be {@code null}. */
  public OffsetDateTime getInvitedAt() {
    return invitedAt;
  }

  /** When the learner started training. May be {@code null}. */
  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  /** When the learner completed the course. May be {@code null}. */
  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  /** When the learner's training is due. May be {@code null}. */
  public OffsetDateTime getDueAt() {
    return dueAt;
  }

  /**
   * The learner's current certificate, with its verification and PDF links. May be {@code null}.
   */
  public LearnerCertificate getCertificate() {
    return certificate;
  }
}
