// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.seatOrders().quote(…)}. Immutable. */
public final class SeatOrderQuoteParams {
  private final Long quantity;
  private final String currency;

  private SeatOrderQuoteParams(Builder builder) {
    this.quantity = builder.quantity;
    this.currency = builder.currency;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Seats to quote. */
  public Long getQuantity() {
    return quantity;
  }

  /** The currency you expect to be charged in. It must match the organization's. */
  public String getCurrency() {
    return currency;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (quantity != null) {
      json.put("quantity", quantity);
    }
    if (currency != null) {
      json.put("currency", currency);
    }
    return json;
  }

  public static final class Builder {
    private Long quantity;
    private String currency;

    private Builder() {}

    /** Seats to quote. Required. */
    public Builder quantity(long quantity) {
      this.quantity = quantity;
      return this;
    }

    /** The currency you expect to be charged in. It must match the organization's. */
    public Builder currency(String currency) {
      this.currency = Objects.requireNonNull(currency, "currency");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code quantity} is not set
     */
    public SeatOrderQuoteParams build() {
      if (quantity == null) {
        throw new IllegalStateException("quantity is required");
      }
      return new SeatOrderQuoteParams(this);
    }
  }
}
