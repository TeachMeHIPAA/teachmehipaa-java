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
 * The order's status.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class SeatOrderCreatedStatus {
  public static final SeatOrderCreatedStatus REQUIRES_ACTION =
      new SeatOrderCreatedStatus("requires_action");
  public static final SeatOrderCreatedStatus PROCESSING = new SeatOrderCreatedStatus("processing");
  public static final SeatOrderCreatedStatus INVOICED = new SeatOrderCreatedStatus("invoiced");
  public static final SeatOrderCreatedStatus PAID = new SeatOrderCreatedStatus("paid");
  public static final SeatOrderCreatedStatus FAILED = new SeatOrderCreatedStatus("failed");
  public static final SeatOrderCreatedStatus EXPIRED = new SeatOrderCreatedStatus("expired");
  public static final SeatOrderCreatedStatus REFUNDED = new SeatOrderCreatedStatus("refunded");

  private static final Map<String, SeatOrderCreatedStatus> KNOWN =
      Stream.of(REQUIRES_ACTION, PROCESSING, INVOICED, PAID, FAILED, EXPIRED, REFUNDED)
          .collect(
              Collectors.toUnmodifiableMap(SeatOrderCreatedStatus::value, Function.identity()));

  private final String value;

  private SeatOrderCreatedStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static SeatOrderCreatedStatus of(String value) {
    Objects.requireNonNull(value, "value");
    SeatOrderCreatedStatus known = KNOWN.get(value);
    return known != null ? known : new SeatOrderCreatedStatus(value);
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
    return other instanceof SeatOrderCreatedStatus that && that.value.equals(value);
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
