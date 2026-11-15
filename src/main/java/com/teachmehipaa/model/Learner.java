package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

/** A Member taking a Course, seen from the Organization's side. */
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

  /** The Learner's id, the same as the Member's {@code subjectId} ({@code sub_…}). */
  public String getLearnerId() {
    return learnerId;
  }

  public String getEmail() {
    return email;
  }

  /** The Learner's full name, or {@code null}. */
  public String getName() {
    return name;
  }

  /** Your own id for the Learner, unique within the Organization, or {@code null}. */
  public String getExternalId() {
    return externalId;
  }

  /** The Learner's training status in this Course. */
  public LearnerStatus getStatus() {
    return status;
  }

  /** The Course this Learner record is for. */
  public LearnerCourse getCourse() {
    return course;
  }

  /** When the Learner was invited, or {@code null}. */
  public OffsetDateTime getInvitedAt() {
    return invitedAt;
  }

  /** When the Learner started training, or {@code null}. */
  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  /** When the Learner completed the Course, or {@code null}. */
  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  /** When the Learner's training is due, or {@code null}. */
  public OffsetDateTime getDueAt() {
    return dueAt;
  }

  /** The Learner's current Certificate, or {@code null}. */
  public LearnerCertificate getCertificate() {
    return certificate;
  }
}
