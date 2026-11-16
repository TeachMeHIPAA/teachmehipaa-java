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
public final class EnrollmentsBatchRequestSelectFilterFilterStatus {
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus INVITED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("INVITED");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus EMAIL_BOUNCED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("EMAIL_BOUNCED");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus STARTED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("STARTED");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus ASSESSMENT_IN_PROGRESS =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("ASSESSMENT_IN_PROGRESS");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus CERTIFIED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("CERTIFIED");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus CANCELLED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("CANCELLED");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus EXPIRING =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("EXPIRING");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus EXPIRED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("EXPIRED");
  public static final EnrollmentsBatchRequestSelectFilterFilterStatus ARCHIVED =
      new EnrollmentsBatchRequestSelectFilterFilterStatus("ARCHIVED");

  private static final Map<String, EnrollmentsBatchRequestSelectFilterFilterStatus> KNOWN =
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
                  EnrollmentsBatchRequestSelectFilterFilterStatus::value, Function.identity()));

  private final String value;

  private EnrollmentsBatchRequestSelectFilterFilterStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentsBatchRequestSelectFilterFilterStatus of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentsBatchRequestSelectFilterFilterStatus known = KNOWN.get(value);
    return known != null ? known : new EnrollmentsBatchRequestSelectFilterFilterStatus(value);
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
    return other instanceof EnrollmentsBatchRequestSelectFilterFilterStatus that
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
