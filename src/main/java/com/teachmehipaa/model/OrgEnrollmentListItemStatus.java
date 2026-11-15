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
public final class OrgEnrollmentListItemStatus {
  public static final OrgEnrollmentListItemStatus INVITED =
      new OrgEnrollmentListItemStatus("INVITED");
  public static final OrgEnrollmentListItemStatus EMAIL_BOUNCED =
      new OrgEnrollmentListItemStatus("EMAIL_BOUNCED");
  public static final OrgEnrollmentListItemStatus STARTED =
      new OrgEnrollmentListItemStatus("STARTED");
  public static final OrgEnrollmentListItemStatus ASSESSMENT_IN_PROGRESS =
      new OrgEnrollmentListItemStatus("ASSESSMENT_IN_PROGRESS");
  public static final OrgEnrollmentListItemStatus CERTIFIED =
      new OrgEnrollmentListItemStatus("CERTIFIED");
  public static final OrgEnrollmentListItemStatus CANCELLED =
      new OrgEnrollmentListItemStatus("CANCELLED");

  private static final Map<String, OrgEnrollmentListItemStatus> KNOWN =
      Stream.of(INVITED, EMAIL_BOUNCED, STARTED, ASSESSMENT_IN_PROGRESS, CERTIFIED, CANCELLED)
          .collect(
              Collectors.toUnmodifiableMap(
                  OrgEnrollmentListItemStatus::value, Function.identity()));

  private final String value;

  private OrgEnrollmentListItemStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static OrgEnrollmentListItemStatus of(String value) {
    Objects.requireNonNull(value, "value");
    OrgEnrollmentListItemStatus known = KNOWN.get(value);
    return known != null ? known : new OrgEnrollmentListItemStatus(value);
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
    return other instanceof OrgEnrollmentListItemStatus that && that.value.equals(value);
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
