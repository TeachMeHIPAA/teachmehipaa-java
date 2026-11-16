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
public final class OrgEnrollmentListItemEffectiveStatus {
  public static final OrgEnrollmentListItemEffectiveStatus INVITED =
      new OrgEnrollmentListItemEffectiveStatus("INVITED");
  public static final OrgEnrollmentListItemEffectiveStatus EMAIL_BOUNCED =
      new OrgEnrollmentListItemEffectiveStatus("EMAIL_BOUNCED");
  public static final OrgEnrollmentListItemEffectiveStatus STARTED =
      new OrgEnrollmentListItemEffectiveStatus("STARTED");
  public static final OrgEnrollmentListItemEffectiveStatus ASSESSMENT_IN_PROGRESS =
      new OrgEnrollmentListItemEffectiveStatus("ASSESSMENT_IN_PROGRESS");
  public static final OrgEnrollmentListItemEffectiveStatus CERTIFIED =
      new OrgEnrollmentListItemEffectiveStatus("CERTIFIED");
  public static final OrgEnrollmentListItemEffectiveStatus CANCELLED =
      new OrgEnrollmentListItemEffectiveStatus("CANCELLED");
  public static final OrgEnrollmentListItemEffectiveStatus EXPIRING =
      new OrgEnrollmentListItemEffectiveStatus("EXPIRING");
  public static final OrgEnrollmentListItemEffectiveStatus EXPIRED =
      new OrgEnrollmentListItemEffectiveStatus("EXPIRED");
  public static final OrgEnrollmentListItemEffectiveStatus ARCHIVED =
      new OrgEnrollmentListItemEffectiveStatus("ARCHIVED");

  private static final Map<String, OrgEnrollmentListItemEffectiveStatus> KNOWN =
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
                  OrgEnrollmentListItemEffectiveStatus::value, Function.identity()));

  private final String value;

  private OrgEnrollmentListItemEffectiveStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static OrgEnrollmentListItemEffectiveStatus of(String value) {
    Objects.requireNonNull(value, "value");
    OrgEnrollmentListItemEffectiveStatus known = KNOWN.get(value);
    return known != null ? known : new OrgEnrollmentListItemEffectiveStatus(value);
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
    return other instanceof OrgEnrollmentListItemEffectiveStatus that && that.value.equals(value);
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
