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
 * <code>requires_action</code>, <code>processing</code>, <code>invoiced</code>, <code>paid</code>,
 * <code>failed</code>, <code>expired</code> or <code>refunded</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class SeatOrderStatus {
  public static final SeatOrderStatus REQUIRES_ACTION = new SeatOrderStatus("requires_action");
  public static final SeatOrderStatus PROCESSING = new SeatOrderStatus("processing");
  public static final SeatOrderStatus INVOICED = new SeatOrderStatus("invoiced");
  public static final SeatOrderStatus PAID = new SeatOrderStatus("paid");
  public static final SeatOrderStatus FAILED = new SeatOrderStatus("failed");
  public static final SeatOrderStatus EXPIRED = new SeatOrderStatus("expired");
  public static final SeatOrderStatus REFUNDED = new SeatOrderStatus("refunded");

  private static final Map<String, SeatOrderStatus> KNOWN =
      Stream.of(REQUIRES_ACTION, PROCESSING, INVOICED, PAID, FAILED, EXPIRED, REFUNDED)
          .collect(Collectors.toUnmodifiableMap(SeatOrderStatus::value, Function.identity()));

  private final String value;

  private SeatOrderStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static SeatOrderStatus of(String value) {
    Objects.requireNonNull(value, "value");
    SeatOrderStatus known = KNOWN.get(value);
    return known != null ? known : new SeatOrderStatus(value);
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
    return other instanceof SeatOrderStatus that && that.value.equals(value);
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
