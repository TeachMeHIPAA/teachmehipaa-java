// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Told apart by {@code eventType}; a value this version of the library doesn't know reads as {@link
 * UnknownEvent}, never as an error.
 */
@JsonDeserialize(using = EventDeserializer.class)
public sealed interface Event
    permits EventCertificateRevoked,
        EventEnrollmentCertified,
        EventInviteBounced,
        EventLearnerCertified,
        EventLearnerExpired,
        EventLearnerExpiring,
        EventLearnerInvited,
        EventLearnerRemoved,
        EventLearnerStarted,
        EventSeatConsumed,
        EventSeatFreed,
        EventSeatOrderExpired,
        EventSeatOrderFailed,
        EventSeatOrderPaid,
        EventSeatsLow,
        UnknownEvent {
  /** The {@code eventType} that selects the variant. */
  String getEventType();

  String getEventId();

  String getOrgId();

  OffsetDateTime getOccurredAt();

  /** Fields this version of the library has no getter for, keyed by JSON name. */
  Map<String, Object> getExtraFields();
}
