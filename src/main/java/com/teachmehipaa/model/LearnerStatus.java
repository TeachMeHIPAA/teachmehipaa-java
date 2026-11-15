package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Learner's training status in one Course. The known values are constants; the API may add values
 * at any time, and {@link #of} accepts them too (ADR-0006).
 */
public final class LearnerStatus {
  /** Invited, not started. */
  public static final LearnerStatus NOT_STARTED = new LearnerStatus("not_started");

  public static final LearnerStatus INVITE_BOUNCED = new LearnerStatus("invite_bounced");
  public static final LearnerStatus IN_PROGRESS = new LearnerStatus("in_progress");
  public static final LearnerStatus COMPLIANT = new LearnerStatus("compliant");

  /** Within 60 days of the Certificate's expiry. */
  public static final LearnerStatus EXPIRING_SOON = new LearnerStatus("expiring_soon");

  public static final LearnerStatus EXPIRED = new LearnerStatus("expired");
  public static final LearnerStatus REMOVED = new LearnerStatus("removed");

  private static final Map<String, LearnerStatus> KNOWN =
      Stream.of(
              NOT_STARTED, INVITE_BOUNCED, IN_PROGRESS, COMPLIANT, EXPIRING_SOON, EXPIRED, REMOVED)
          .collect(Collectors.toUnmodifiableMap(LearnerStatus::value, Function.identity()));

  private final String value;

  private LearnerStatus(String value) {
    this.value = value;
  }

  /** The status for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static LearnerStatus of(String value) {
    Objects.requireNonNull(value, "value");
    LearnerStatus known = KNOWN.get(value);
    return known != null ? known : new LearnerStatus(value);
  }

  /** The wire value, such as {@code in_progress}. */
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
    return other instanceof LearnerStatus status && status.value.equals(value);
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
