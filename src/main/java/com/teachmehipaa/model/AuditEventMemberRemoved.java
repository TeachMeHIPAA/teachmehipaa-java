// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventMemberRemoved extends ApiModel implements AuditEvent {
  private final String eventId;
  private final String at;
  private final String type;
  private final String actorSubjectId;
  private final AuditEventActor actor;
  private final String subjectId;
  private final boolean wasAdmin;

  @JsonCreator
  AuditEventMemberRemoved(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("at") String at,
      @JsonProperty("type") String type,
      @JsonProperty("actorSubjectId") String actorSubjectId,
      @JsonProperty("actor") AuditEventActor actor,
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("wasAdmin") boolean wasAdmin) {
    this.eventId = eventId;
    this.at = at;
    this.type = type;
    this.actorSubjectId = actorSubjectId;
    this.actor = actor;
    this.subjectId = subjectId;
    this.wasAdmin = wasAdmin;
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

  public String getActorSubjectId() {
    return actorSubjectId;
  }

  public AuditEventActor getActor() {
    return actor;
  }

  public String getSubjectId() {
    return subjectId;
  }

  public boolean isWasAdmin() {
    return wasAdmin;
  }
}
