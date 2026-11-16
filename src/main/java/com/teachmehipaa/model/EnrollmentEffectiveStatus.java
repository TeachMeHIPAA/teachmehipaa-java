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
 * The status to display. Adds <code>EXPIRING</code>, <code>EXPIRED</code> and <code>ARCHIVED</code>
 * , derived when you read it.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class EnrollmentEffectiveStatus {
  public static final EnrollmentEffectiveStatus INVITED = new EnrollmentEffectiveStatus("INVITED");
  public static final EnrollmentEffectiveStatus EMAIL_BOUNCED =
      new EnrollmentEffectiveStatus("EMAIL_BOUNCED");
  public static final EnrollmentEffectiveStatus STARTED = new EnrollmentEffectiveStatus("STARTED");
  public static final EnrollmentEffectiveStatus ASSESSMENT_IN_PROGRESS =
      new EnrollmentEffectiveStatus("ASSESSMENT_IN_PROGRESS");
  public static final EnrollmentEffectiveStatus CERTIFIED =
      new EnrollmentEffectiveStatus("CERTIFIED");
  public static final EnrollmentEffectiveStatus CANCELLED =
      new EnrollmentEffectiveStatus("CANCELLED");
  public static final EnrollmentEffectiveStatus EXPIRING =
      new EnrollmentEffectiveStatus("EXPIRING");
  public static final EnrollmentEffectiveStatus EXPIRED = new EnrollmentEffectiveStatus("EXPIRED");
  public static final EnrollmentEffectiveStatus ARCHIVED =
      new EnrollmentEffectiveStatus("ARCHIVED");

  private static final Map<String, EnrollmentEffectiveStatus> KNOWN =
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
              Collectors.toUnmodifiableMap(EnrollmentEffectiveStatus::value, Function.identity()));

  private final String value;

  private EnrollmentEffectiveStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentEffectiveStatus of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentEffectiveStatus known = KNOWN.get(value);
    return known != null ? known : new EnrollmentEffectiveStatus(value);
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
    return other instanceof EnrollmentEffectiveStatus that && that.value.equals(value);
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
