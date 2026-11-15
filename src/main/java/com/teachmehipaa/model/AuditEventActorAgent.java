// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventActorAgent extends ApiModel implements AuditEventActor {
  private final String type;
  private final String agentId;

  @JsonCreator
  AuditEventActorAgent(@JsonProperty("type") String type, @JsonProperty("agentId") String agentId) {
    this.type = type;
    this.agentId = agentId;
  }

  public String getType() {
    return type;
  }

  public String getAgentId() {
    return agentId;
  }
}
