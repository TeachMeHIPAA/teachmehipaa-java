// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.teachmehipaa.core.UnionDeserializer;
import java.util.Map;

final class AuditEventActorDeserializer extends UnionDeserializer<AuditEventActor> {
  private static final long serialVersionUID = 1L;

  AuditEventActorDeserializer() {
    super(
        AuditEventActor.class,
        "type",
        Map.ofEntries(
            Map.entry("agent", AuditEventActorAgent.class),
            Map.entry("client", AuditEventActorClient.class),
            Map.entry("scim", AuditEventActorScim.class),
            Map.entry("user", AuditEventActorUser.class)),
        UnknownAuditEventActor.class);
  }
}
