// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventOrganizationUpdated extends ApiModel implements AuditEvent {
  private final String eventId;
  private final String at;
  private final String type;
  private final String actorSubjectId;
  private final AuditEventActor actor;
  private final List<AuditEventOrganizationUpdatedFieldsItem> fields;

  @JsonCreator
  AuditEventOrganizationUpdated(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("at") String at,
      @JsonProperty("type") String type,
      @JsonProperty("actorSubjectId") String actorSubjectId,
      @JsonProperty("actor") AuditEventActor actor,
      @JsonProperty("fields") List<AuditEventOrganizationUpdatedFieldsItem> fields) {
    this.eventId = eventId;
    this.at = at;
    this.type = type;
    this.actorSubjectId = actorSubjectId;
    this.actor = actor;
    this.fields = immutable(fields);
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

  public List<AuditEventOrganizationUpdatedFieldsItem> getFields() {
    return fields;
  }
}
