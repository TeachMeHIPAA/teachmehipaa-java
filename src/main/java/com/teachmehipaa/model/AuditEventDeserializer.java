// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.teachmehipaa.core.UnionDeserializer;
import java.util.Map;

final class AuditEventDeserializer extends UnionDeserializer<AuditEvent> {
  private static final long serialVersionUID = 1L;

  AuditEventDeserializer() {
    super(
        AuditEvent.class,
        "type",
        Map.ofEntries(
            Map.entry("api_client.created", AuditEventApiClientCreated.class),
            Map.entry("api_client.revoked", AuditEventApiClientRevoked.class),
            Map.entry("api_client.secret_rotated", AuditEventApiClientSecretRotated.class),
            Map.entry("api_client.updated", AuditEventApiClientUpdated.class),
            Map.entry("certificate.issued", AuditEventCertificateIssued.class),
            Map.entry("certificate.revoked", AuditEventCertificateRevoked.class),
            Map.entry("enrollment.transition", AuditEventEnrollmentTransition.class),
            Map.entry("learner.launch_link_created", AuditEventLearnerLaunchLinkCreated.class),
            Map.entry("learner.updated", AuditEventLearnerUpdated.class),
            Map.entry("member.removed", AuditEventMemberRemoved.class),
            Map.entry("member.role_changed", AuditEventMemberRoleChanged.class),
            Map.entry("member.transferred", AuditEventMemberTransferred.class),
            Map.entry("organization.closed", AuditEventOrganizationClosed.class),
            Map.entry("organization.reopened", AuditEventOrganizationReopened.class),
            Map.entry("organization.updated", AuditEventOrganizationUpdated.class),
            Map.entry("seat.granted", AuditEventSeatGranted.class)),
        UnknownAuditEvent.class);
  }
}
