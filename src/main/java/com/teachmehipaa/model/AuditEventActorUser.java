// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventActorUser extends ApiModel implements AuditEventActor {
  private final String type;
  private final String subjectId;

  @JsonCreator
  AuditEventActorUser(
      @JsonProperty("type") String type, @JsonProperty("subjectId") String subjectId) {
    this.type = type;
    this.subjectId = subjectId;
  }

  public String getType() {
    return type;
  }

  public String getSubjectId() {
    return subjectId;
  }
}
