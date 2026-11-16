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
 * <code>email</code> (default) sends the invite email. <code>none</code> sends nothing and returns
 * <code>joinUrl</code> for you to deliver.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class LearnerUpsertRequestDelivery {
  public static final LearnerUpsertRequestDelivery EMAIL =
      new LearnerUpsertRequestDelivery("email");
  public static final LearnerUpsertRequestDelivery NONE = new LearnerUpsertRequestDelivery("none");

  private static final Map<String, LearnerUpsertRequestDelivery> KNOWN =
      Stream.of(EMAIL, NONE)
          .collect(
              Collectors.toUnmodifiableMap(
                  LearnerUpsertRequestDelivery::value, Function.identity()));

  private final String value;

  private LearnerUpsertRequestDelivery(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static LearnerUpsertRequestDelivery of(String value) {
    Objects.requireNonNull(value, "value");
    LearnerUpsertRequestDelivery known = KNOWN.get(value);
    return known != null ? known : new LearnerUpsertRequestDelivery(value);
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
    return other instanceof LearnerUpsertRequestDelivery that && that.value.equals(value);
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
