// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

/**
 * Told apart by {@code type}; a value this version of the library doesn't know reads as {@link
 * UnknownAuditEvent}, never as an error.
 */
@JsonDeserialize(using = AuditEventDeserializer.class)
public sealed interface AuditEvent
    permits AuditEventApiClientCreated,
        AuditEventApiClientRevoked,
        AuditEventApiClientSecretRotated,
        AuditEventApiClientUpdated,
        AuditEventCertificateIssued,
        AuditEventCertificateRevoked,
        AuditEventEnrollmentTransition,
        AuditEventLearnerLaunchLinkCreated,
        AuditEventLearnerUpdated,
        AuditEventMemberRemoved,
        AuditEventMemberRoleChanged,
        AuditEventMemberTransferred,
        AuditEventOauthGrantCreated,
        AuditEventOauthGrantRevoked,
        AuditEventOauthGrantUpdated,
        AuditEventOrganizationClosed,
        AuditEventOrganizationCreatedByPartner,
        AuditEventOrganizationReopened,
        AuditEventOrganizationUpdated,
        AuditEventSeatGranted,
        AuditEventSeatOrderCreated,
        AuditEventSeatOrderFailed,
        AuditEventSeatOrderPaid,
        UnknownAuditEvent {
  /** The {@code type} that selects the variant. */
  String getType();

  /** Fields this version of the library has no getter for, keyed by JSON name. */
  Map<String, Object> getExtraFields();
}
