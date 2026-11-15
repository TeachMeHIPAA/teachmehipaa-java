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
 * How the invite was delivered: <code>email</code>, or <code>none</code> when you deliver it.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class LearnerCreatedDelivery {
  public static final LearnerCreatedDelivery EMAIL = new LearnerCreatedDelivery("email");
  public static final LearnerCreatedDelivery NONE = new LearnerCreatedDelivery("none");

  private static final Map<String, LearnerCreatedDelivery> KNOWN =
      Stream.of(EMAIL, NONE)
          .collect(
              Collectors.toUnmodifiableMap(LearnerCreatedDelivery::value, Function.identity()));

  private final String value;

  private LearnerCreatedDelivery(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static LearnerCreatedDelivery of(String value) {
    Objects.requireNonNull(value, "value");
    LearnerCreatedDelivery known = KNOWN.get(value);
    return known != null ? known : new LearnerCreatedDelivery(value);
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
    return other instanceof LearnerCreatedDelivery that && that.value.equals(value);
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
