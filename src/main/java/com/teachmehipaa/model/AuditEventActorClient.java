// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventActorClient extends ApiModel implements AuditEventActor {
  private final String type;
  private final String clientId;

  @JsonCreator
  AuditEventActorClient(
      @JsonProperty("type") String type, @JsonProperty("clientId") String clientId) {
    this.type = type;
    this.clientId = clientId;
  }

  public String getType() {
    return type;
  }

  public String getClientId() {
    return clientId;
  }
}
