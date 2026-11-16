// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

/**
 * Told apart by {@code type}; a value this version of the library doesn't know reads as {@link
 * UnknownAuditEventActor}, never as an error.
 */
@JsonDeserialize(using = AuditEventActorDeserializer.class)
public sealed interface AuditEventActor
    permits AuditEventActorAgent,
        AuditEventActorClient,
        AuditEventActorScim,
        AuditEventActorUser,
        UnknownAuditEventActor {
  /** The {@code type} that selects the variant. */
  String getType();

  /** Fields this version of the library has no getter for, keyed by JSON name. */
  Map<String, Object> getExtraFields();
}
