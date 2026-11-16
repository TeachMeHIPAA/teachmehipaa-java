// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventEnrollmentTransition extends ApiModel implements AuditEvent {
  private final String eventId;
  private final String at;
  private final String type;
  private final AuditEventEnrollmentTransitionTransition transition;
  private final String subjectId;
  private final String courseId;
  private final AuditEventEnrollmentTransitionStatusFrom statusFrom;
  private final AuditEventEnrollmentTransitionStatusTo statusTo;
  private final boolean archivedFrom;
  private final boolean archivedTo;
  private final long rev;

  @JsonCreator
  AuditEventEnrollmentTransition(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("at") String at,
      @JsonProperty("type") String type,
      @JsonProperty("transition") AuditEventEnrollmentTransitionTransition transition,
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("statusFrom") AuditEventEnrollmentTransitionStatusFrom statusFrom,
      @JsonProperty("statusTo") AuditEventEnrollmentTransitionStatusTo statusTo,
      @JsonProperty("archivedFrom") boolean archivedFrom,
      @JsonProperty("archivedTo") boolean archivedTo,
      @JsonProperty("rev") long rev) {
    this.eventId = eventId;
    this.at = at;
    this.type = type;
    this.transition = transition;
    this.subjectId = subjectId;
    this.courseId = courseId;
    this.statusFrom = statusFrom;
    this.statusTo = statusTo;
    this.archivedFrom = archivedFrom;
    this.archivedTo = archivedTo;
    this.rev = rev;
  }

  public String getEventId() {
    return eventId;
  }

  public String getAt() {
    return at;
  }

  public String getType() {
    return type;
  }

  public AuditEventEnrollmentTransitionTransition getTransition() {
    return transition;
  }

  public String getSubjectId() {
    return subjectId;
  }

  public String getCourseId() {
    return courseId;
  }

  public AuditEventEnrollmentTransitionStatusFrom getStatusFrom() {
    return statusFrom;
  }

  public AuditEventEnrollmentTransitionStatusTo getStatusTo() {
    return statusTo;
  }

  public boolean isArchivedFrom() {
    return archivedFrom;
  }

  public boolean isArchivedTo() {
    return archivedTo;
  }

  public long getRev() {
    return rev;
  }
}
