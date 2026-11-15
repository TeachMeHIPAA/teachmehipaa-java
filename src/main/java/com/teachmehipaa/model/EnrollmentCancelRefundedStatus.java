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
public final class EnrollmentCancelRefundedStatus {
  public static final EnrollmentCancelRefundedStatus INVITED =
      new EnrollmentCancelRefundedStatus("INVITED");
  public static final EnrollmentCancelRefundedStatus EMAIL_BOUNCED =
      new EnrollmentCancelRefundedStatus("EMAIL_BOUNCED");
  public static final EnrollmentCancelRefundedStatus STARTED =
      new EnrollmentCancelRefundedStatus("STARTED");
  public static final EnrollmentCancelRefundedStatus ASSESSMENT_IN_PROGRESS =
      new EnrollmentCancelRefundedStatus("ASSESSMENT_IN_PROGRESS");
  public static final EnrollmentCancelRefundedStatus CERTIFIED =
      new EnrollmentCancelRefundedStatus("CERTIFIED");
  public static final EnrollmentCancelRefundedStatus CANCELLED =
      new EnrollmentCancelRefundedStatus("CANCELLED");

  private static final Map<String, EnrollmentCancelRefundedStatus> KNOWN =
      Stream.of(INVITED, EMAIL_BOUNCED, STARTED, ASSESSMENT_IN_PROGRESS, CERTIFIED, CANCELLED)
          .collect(
              Collectors.toUnmodifiableMap(
                  EnrollmentCancelRefundedStatus::value, Function.identity()));

  private final String value;

  private EnrollmentCancelRefundedStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentCancelRefundedStatus of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentCancelRefundedStatus known = KNOWN.get(value);
    return known != null ? known : new EnrollmentCancelRefundedStatus(value);
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
    return other instanceof EnrollmentCancelRefundedStatus that && that.value.equals(value);
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
