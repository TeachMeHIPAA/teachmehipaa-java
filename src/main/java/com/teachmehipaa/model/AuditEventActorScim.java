// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventActorScim extends ApiModel implements AuditEventActor {
  private final String type;
  private final String keyId;

  @JsonCreator
  AuditEventActorScim(@JsonProperty("type") String type, @JsonProperty("keyId") String keyId) {
    this.type = type;
    this.keyId = keyId;
  }

  public String getType() {
    return type;
  }

  public String getKeyId() {
    return keyId;
  }
}
