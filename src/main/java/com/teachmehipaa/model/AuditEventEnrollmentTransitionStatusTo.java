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
public final class AuditEventEnrollmentTransitionStatusTo {
  public static final AuditEventEnrollmentTransitionStatusTo INVITED =
      new AuditEventEnrollmentTransitionStatusTo("INVITED");
  public static final AuditEventEnrollmentTransitionStatusTo EMAIL_BOUNCED =
      new AuditEventEnrollmentTransitionStatusTo("EMAIL_BOUNCED");
  public static final AuditEventEnrollmentTransitionStatusTo STARTED =
      new AuditEventEnrollmentTransitionStatusTo("STARTED");
  public static final AuditEventEnrollmentTransitionStatusTo ASSESSMENT_IN_PROGRESS =
      new AuditEventEnrollmentTransitionStatusTo("ASSESSMENT_IN_PROGRESS");
  public static final AuditEventEnrollmentTransitionStatusTo CERTIFIED =
      new AuditEventEnrollmentTransitionStatusTo("CERTIFIED");
  public static final AuditEventEnrollmentTransitionStatusTo CANCELLED =
      new AuditEventEnrollmentTransitionStatusTo("CANCELLED");

  private static final Map<String, AuditEventEnrollmentTransitionStatusTo> KNOWN =
      Stream.of(INVITED, EMAIL_BOUNCED, STARTED, ASSESSMENT_IN_PROGRESS, CERTIFIED, CANCELLED)
          .collect(
              Collectors.toUnmodifiableMap(
                  AuditEventEnrollmentTransitionStatusTo::value, Function.identity()));

  private final String value;

  private AuditEventEnrollmentTransitionStatusTo(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static AuditEventEnrollmentTransitionStatusTo of(String value) {
    Objects.requireNonNull(value, "value");
    AuditEventEnrollmentTransitionStatusTo known = KNOWN.get(value);
    return known != null ? known : new AuditEventEnrollmentTransitionStatusTo(value);
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
    return other instanceof AuditEventEnrollmentTransitionStatusTo that && that.value.equals(value);
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
