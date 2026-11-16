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
public final class AuditEventEnrollmentTransitionTransition {
  public static final AuditEventEnrollmentTransitionTransition BOUNCE =
      new AuditEventEnrollmentTransitionTransition("bounce");
  public static final AuditEventEnrollmentTransitionTransition START =
      new AuditEventEnrollmentTransitionTransition("start");
  public static final AuditEventEnrollmentTransitionTransition BEGIN_ASSESSMENT =
      new AuditEventEnrollmentTransitionTransition("beginAssessment");
  public static final AuditEventEnrollmentTransitionTransition SUBMIT_ASSESSMENT =
      new AuditEventEnrollmentTransitionTransition("submitAssessment");
  public static final AuditEventEnrollmentTransitionTransition CERTIFY =
      new AuditEventEnrollmentTransitionTransition("certify");
  public static final AuditEventEnrollmentTransitionTransition REVOKE_CERT =
      new AuditEventEnrollmentTransitionTransition("revokeCert");
  public static final AuditEventEnrollmentTransitionTransition ARCHIVE =
      new AuditEventEnrollmentTransitionTransition("archive");
  public static final AuditEventEnrollmentTransitionTransition UNARCHIVE =
      new AuditEventEnrollmentTransitionTransition("unarchive");
  public static final AuditEventEnrollmentTransitionTransition REINVITE =
      new AuditEventEnrollmentTransitionTransition("reinvite");
  public static final AuditEventEnrollmentTransitionTransition CANCEL_INVITE =
      new AuditEventEnrollmentTransitionTransition("cancelInvite");

  private static final Map<String, AuditEventEnrollmentTransitionTransition> KNOWN =
      Stream.of(
              BOUNCE,
              START,
              BEGIN_ASSESSMENT,
              SUBMIT_ASSESSMENT,
              CERTIFY,
              REVOKE_CERT,
              ARCHIVE,
              UNARCHIVE,
              REINVITE,
              CANCEL_INVITE)
          .collect(
              Collectors.toUnmodifiableMap(
                  AuditEventEnrollmentTransitionTransition::value, Function.identity()));

  private final String value;

  private AuditEventEnrollmentTransitionTransition(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static AuditEventEnrollmentTransitionTransition of(String value) {
    Objects.requireNonNull(value, "value");
    AuditEventEnrollmentTransitionTransition known = KNOWN.get(value);
    return known != null ? known : new AuditEventEnrollmentTransitionTransition(value);
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
    return other instanceof AuditEventEnrollmentTransitionTransition that
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
