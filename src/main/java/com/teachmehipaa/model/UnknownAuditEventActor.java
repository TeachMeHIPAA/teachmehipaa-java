// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

/**
 * A {@link AuditEventActor} of a kind this version of the library doesn't know. Its other fields
 * are kept in {@link #getExtraFields()}.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public final class UnknownAuditEventActor extends ApiModel implements AuditEventActor {
  private final String type;

  @JsonCreator
  UnknownAuditEventActor(@JsonProperty("type") String type) {
    this.type = type;
  }

  /** The <code>type</code> this version of the library doesn't know. */
  public String getType() {
    return type;
  }
}
