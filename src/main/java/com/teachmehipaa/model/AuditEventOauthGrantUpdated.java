// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventOauthGrantUpdated extends ApiModel implements AuditEvent {
  private final String eventId;
  private final String at;
  private final String type;
  private final String actorSubjectId;
  private final AuditEventActor actor;
  private final String appId;
  private final String grantId;
  private final List<AuditEventOauthGrantUpdatedFieldsItem> fields;

  @JsonCreator
  AuditEventOauthGrantUpdated(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("at") String at,
      @JsonProperty("type") String type,
      @JsonProperty("actorSubjectId") String actorSubjectId,
      @JsonProperty("actor") AuditEventActor actor,
      @JsonProperty("appId") String appId,
      @JsonProperty("grantId") String grantId,
      @JsonProperty("fields") List<AuditEventOauthGrantUpdatedFieldsItem> fields) {
    this.eventId = eventId;
    this.at = at;
    this.type = type;
    this.actorSubjectId = actorSubjectId;
    this.actor = actor;
    this.appId = appId;
    this.grantId = grantId;
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

  public String getAppId() {
    return appId;
  }

  public String getGrantId() {
    return grantId;
  }

  public List<AuditEventOauthGrantUpdatedFieldsItem> getFields() {
    return fields;
  }
}
