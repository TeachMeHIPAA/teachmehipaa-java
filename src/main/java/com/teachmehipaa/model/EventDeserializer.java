// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.teachmehipaa.core.UnionDeserializer;
import java.util.Map;

final class EventDeserializer extends UnionDeserializer<Event> {
  private static final long serialVersionUID = 1L;

  EventDeserializer() {
    super(
        Event.class,
        "eventType",
        Map.ofEntries(
            Map.entry("certificate.revoked", EventCertificateRevoked.class),
            Map.entry("enrollment.certified", EventEnrollmentCertified.class),
            Map.entry("invite.bounced", EventInviteBounced.class),
            Map.entry("learner.certified", EventLearnerCertified.class),
            Map.entry("learner.expired", EventLearnerExpired.class),
            Map.entry("learner.expiring", EventLearnerExpiring.class),
            Map.entry("learner.invited", EventLearnerInvited.class),
            Map.entry("learner.removed", EventLearnerRemoved.class),
            Map.entry("learner.started", EventLearnerStarted.class),
            Map.entry("seat.consumed", EventSeatConsumed.class),
            Map.entry("seat.freed", EventSeatFreed.class),
            Map.entry("seat_order.expired", EventSeatOrderExpired.class),
            Map.entry("seat_order.failed", EventSeatOrderFailed.class),
            Map.entry("seat_order.paid", EventSeatOrderPaid.class),
            Map.entry("seats.low", EventSeatsLow.class)),
        UnknownEvent.class);
  }
}
