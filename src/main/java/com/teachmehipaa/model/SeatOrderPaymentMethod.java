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
 * <code>hosted_checkout</code>, <code>saved_payment_method</code> or <code>invoice</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class SeatOrderPaymentMethod {
  public static final SeatOrderPaymentMethod HOSTED_CHECKOUT =
      new SeatOrderPaymentMethod("hosted_checkout");
  public static final SeatOrderPaymentMethod SAVED_PAYMENT_METHOD =
      new SeatOrderPaymentMethod("saved_payment_method");
  public static final SeatOrderPaymentMethod INVOICE = new SeatOrderPaymentMethod("invoice");

  private static final Map<String, SeatOrderPaymentMethod> KNOWN =
      Stream.of(HOSTED_CHECKOUT, SAVED_PAYMENT_METHOD, INVOICE)
          .collect(
              Collectors.toUnmodifiableMap(SeatOrderPaymentMethod::value, Function.identity()));

  private final String value;

  private SeatOrderPaymentMethod(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static SeatOrderPaymentMethod of(String value) {
    Objects.requireNonNull(value, "value");
    SeatOrderPaymentMethod known = KNOWN.get(value);
    return known != null ? known : new SeatOrderPaymentMethod(value);
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
    return other instanceof SeatOrderPaymentMethod that && that.value.equals(value);
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
