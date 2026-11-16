// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The known values are constants; the API may add values at any time, and {@link #of} accepts them
 * too (ADR-0006).
 */
public final class EventType {
  public static final EventType ENROLLMENT_CERTIFIED = new EventType("enrollment.certified");
  public static final EventType SEAT_CONSUMED = new EventType("seat.consumed");
  public static final EventType SEAT_FREED = new EventType("seat.freed");
  public static final EventType LEARNER_INVITED = new EventType("learner.invited");
  public static final EventType LEARNER_STARTED = new EventType("learner.started");
  public static final EventType LEARNER_CERTIFIED = new EventType("learner.certified");
  public static final EventType LEARNER_EXPIRING = new EventType("learner.expiring");
  public static final EventType LEARNER_EXPIRED = new EventType("learner.expired");
  public static final EventType LEARNER_REMOVED = new EventType("learner.removed");
  public static final EventType INVITE_BOUNCED = new EventType("invite.bounced");
  public static final EventType CERTIFICATE_REVOKED = new EventType("certificate.revoked");
  public static final EventType SEATS_LOW = new EventType("seats.low");

  private static final Map<String, EventType> KNOWN =
      Stream.of(
              ENROLLMENT_CERTIFIED,
              SEAT_CONSUMED,
              SEAT_FREED,
              LEARNER_INVITED,
              LEARNER_STARTED,
              LEARNER_CERTIFIED,
              LEARNER_EXPIRING,
              LEARNER_EXPIRED,
              LEARNER_REMOVED,
              INVITE_BOUNCED,
              CERTIFICATE_REVOKED,
              SEATS_LOW)
          .collect(Collectors.toUnmodifiableMap(EventType::value, Function.identity()));

  private final String value;

  private EventType(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EventType of(String value) {
    Objects.requireNonNull(value, "value");
    EventType known = KNOWN.get(value);
    return known != null ? known : new EventType(value);
  }

  /** The wire value. */
  @JsonValue
  public String value() {
    return value;
  }

  /** Whether this version of the library knows this value. */
  public boolean isKnown() {
    return KNOWN.containsKey(value);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EventType that && that.value.equals(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}
