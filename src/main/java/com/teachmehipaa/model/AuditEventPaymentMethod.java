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
public final class AuditEventPaymentMethod {
  public static final AuditEventPaymentMethod HOSTED_CHECKOUT =
      new AuditEventPaymentMethod("hosted_checkout");
  public static final AuditEventPaymentMethod SAVED_PAYMENT_METHOD =
      new AuditEventPaymentMethod("saved_payment_method");
  public static final AuditEventPaymentMethod INVOICE = new AuditEventPaymentMethod("invoice");

  private static final Map<String, AuditEventPaymentMethod> KNOWN =
      Stream.of(HOSTED_CHECKOUT, SAVED_PAYMENT_METHOD, INVOICE)
          .collect(
              Collectors.toUnmodifiableMap(AuditEventPaymentMethod::value, Function.identity()));

  private final String value;

  private AuditEventPaymentMethod(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static AuditEventPaymentMethod of(String value) {
    Objects.requireNonNull(value, "value");
    AuditEventPaymentMethod known = KNOWN.get(value);
    return known != null ? known : new AuditEventPaymentMethod(value);
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
    return other instanceof AuditEventPaymentMethod that && that.value.equals(value);
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
