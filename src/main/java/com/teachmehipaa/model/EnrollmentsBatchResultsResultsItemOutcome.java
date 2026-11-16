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
public final class EnrollmentsBatchResultsResultsItemOutcome {
  public static final EnrollmentsBatchResultsResultsItemOutcome SUCCEEDED =
      new EnrollmentsBatchResultsResultsItemOutcome("succeeded");
  public static final EnrollmentsBatchResultsResultsItemOutcome SKIPPED =
      new EnrollmentsBatchResultsResultsItemOutcome("skipped");
  public static final EnrollmentsBatchResultsResultsItemOutcome FAILED =
      new EnrollmentsBatchResultsResultsItemOutcome("failed");

  private static final Map<String, EnrollmentsBatchResultsResultsItemOutcome> KNOWN =
      Stream.of(SUCCEEDED, SKIPPED, FAILED)
          .collect(
              Collectors.toUnmodifiableMap(
                  EnrollmentsBatchResultsResultsItemOutcome::value, Function.identity()));

  private final String value;

  private EnrollmentsBatchResultsResultsItemOutcome(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentsBatchResultsResultsItemOutcome of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentsBatchResultsResultsItemOutcome known = KNOWN.get(value);
    return known != null ? known : new EnrollmentsBatchResultsResultsItemOutcome(value);
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
    return other instanceof EnrollmentsBatchResultsResultsItemOutcome that
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
