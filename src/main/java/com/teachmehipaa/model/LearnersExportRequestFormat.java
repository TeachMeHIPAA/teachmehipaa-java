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
 * The file format. Only <code>csv</code> today.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class LearnersExportRequestFormat {
  public static final LearnersExportRequestFormat CSV = new LearnersExportRequestFormat("csv");

  private static final Map<String, LearnersExportRequestFormat> KNOWN =
      Stream.of(CSV)
          .collect(
              Collectors.toUnmodifiableMap(
                  LearnersExportRequestFormat::value, Function.identity()));

  private final String value;

  private LearnersExportRequestFormat(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static LearnersExportRequestFormat of(String value) {
    Objects.requireNonNull(value, "value");
    LearnersExportRequestFormat known = KNOWN.get(value);
    return known != null ? known : new LearnersExportRequestFormat(value);
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
    return other instanceof LearnersExportRequestFormat that && that.value.equals(value);
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
