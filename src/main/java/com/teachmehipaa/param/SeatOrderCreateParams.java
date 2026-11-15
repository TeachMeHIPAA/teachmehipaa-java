// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.seatOrders().create(…)}. Immutable. */
public final class SeatOrderCreateParams {
  private final Long quantity;
  private final SeatOrderCreateRequestPayment payment;
  private final String clientReference;

  private SeatOrderCreateParams(Builder builder) {
    this.quantity = builder.quantity;
    this.payment = builder.payment;
    this.clientReference = builder.clientReference;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Seats to buy. */
  public Long getQuantity() {
    return quantity;
  }

  /**
   * How to pay: <code>hosted_checkout</code> with a <code>returnUrl</code>, <code>
   * saved_payment_method</code> with the card's <code>paymentMethodId</code>, or <code>invoice
   * </code>. A <code>returnUrl</code> must be https and on your API client's allowlist.
   */
  public SeatOrderCreateRequestPayment getPayment() {
    return payment;
  }

  /** Your own reference for the order. */
  public String getClientReference() {
    return clientReference;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (quantity != null) {
      json.put("quantity", quantity);
    }
    if (payment != null) {
      json.put("payment", payment);
    }
    if (clientReference != null) {
      json.put("clientReference", clientReference);
    }
    return json;
  }

  public static final class Builder {
    private Long quantity;
    private SeatOrderCreateRequestPayment payment;
    private String clientReference;

    private Builder() {}

    /** Seats to buy. Required. */
    public Builder quantity(long quantity) {
      this.quantity = quantity;
      return this;
    }

    /**
     * How to pay: <code>hosted_checkout</code> with a <code>returnUrl</code>, <code>
     * saved_payment_method</code> with the card's <code>paymentMethodId</code>, or <code>invoice
     * </code>. A <code>returnUrl</code> must be https and on your API client's allowlist. Required.
     */
    public Builder payment(SeatOrderCreateRequestPayment payment) {
      this.payment = Objects.requireNonNull(payment, "payment");
      return this;
    }

    /** Your own reference for the order. */
    public Builder clientReference(String clientReference) {
      this.clientReference = Objects.requireNonNull(clientReference, "clientReference");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code quantity}, {@code payment} is not set
     */
    public SeatOrderCreateParams build() {
      if (quantity == null) {
        throw new IllegalStateException("quantity is required");
      }
      if (payment == null) {
        throw new IllegalStateException("payment is required");
      }
      return new SeatOrderCreateParams(this);
    }
  }
}
