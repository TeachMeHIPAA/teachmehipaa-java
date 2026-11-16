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
public final class EnrollmentCancelRefundedEffectiveStatus {
  public static final EnrollmentCancelRefundedEffectiveStatus INVITED =
      new EnrollmentCancelRefundedEffectiveStatus("INVITED");
  public static final EnrollmentCancelRefundedEffectiveStatus EMAIL_BOUNCED =
      new EnrollmentCancelRefundedEffectiveStatus("EMAIL_BOUNCED");
  public static final EnrollmentCancelRefundedEffectiveStatus STARTED =
      new EnrollmentCancelRefundedEffectiveStatus("STARTED");
  public static final EnrollmentCancelRefundedEffectiveStatus ASSESSMENT_IN_PROGRESS =
      new EnrollmentCancelRefundedEffectiveStatus("ASSESSMENT_IN_PROGRESS");
  public static final EnrollmentCancelRefundedEffectiveStatus CERTIFIED =
      new EnrollmentCancelRefundedEffectiveStatus("CERTIFIED");
  public static final EnrollmentCancelRefundedEffectiveStatus CANCELLED =
      new EnrollmentCancelRefundedEffectiveStatus("CANCELLED");
  public static final EnrollmentCancelRefundedEffectiveStatus EXPIRING =
      new EnrollmentCancelRefundedEffectiveStatus("EXPIRING");
  public static final EnrollmentCancelRefundedEffectiveStatus EXPIRED =
      new EnrollmentCancelRefundedEffectiveStatus("EXPIRED");
  public static final EnrollmentCancelRefundedEffectiveStatus ARCHIVED =
      new EnrollmentCancelRefundedEffectiveStatus("ARCHIVED");

  private static final Map<String, EnrollmentCancelRefundedEffectiveStatus> KNOWN =
      Stream.of(
              INVITED,
              EMAIL_BOUNCED,
              STARTED,
              ASSESSMENT_IN_PROGRESS,
              CERTIFIED,
              CANCELLED,
              EXPIRING,
              EXPIRED,
              ARCHIVED)
          .collect(
              Collectors.toUnmodifiableMap(
                  EnrollmentCancelRefundedEffectiveStatus::value, Function.identity()));

  private final String value;

  private EnrollmentCancelRefundedEffectiveStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentCancelRefundedEffectiveStatus of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentCancelRefundedEffectiveStatus known = KNOWN.get(value);
    return known != null ? known : new EnrollmentCancelRefundedEffectiveStatus(value);
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
    return other instanceof EnrollmentCancelRefundedEffectiveStatus that
        && that.value.equals(value);
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
