// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable. */
public final class SeatOrderCreateRequestPaymentSavedPaymentMethod
    implements SeatOrderCreateRequestPayment {
  private final String returnUrl;
  private final String paymentMethodId;
  private final Long expectedTotal;
  private final String currency;

  private SeatOrderCreateRequestPaymentSavedPaymentMethod(Builder builder) {
    this.returnUrl = builder.returnUrl;
    this.paymentMethodId = builder.paymentMethodId;
    this.expectedTotal = builder.expectedTotal;
    this.currency = builder.currency;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public String getPaymentMethodId() {
    return paymentMethodId;
  }

  public Long getExpectedTotal() {
    return expectedTotal;
  }

  public String getCurrency() {
    return currency;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("method", "saved_payment_method");
    if (returnUrl != null) {
      json.put("returnUrl", returnUrl);
    }
    if (paymentMethodId != null) {
      json.put("paymentMethodId", paymentMethodId);
    }
    if (expectedTotal != null) {
      json.put("expectedTotal", expectedTotal);
    }
    if (currency != null) {
      json.put("currency", currency);
    }
    return json;
  }

  public static final class Builder {
    private String returnUrl;
    private String paymentMethodId;
    private Long expectedTotal;
    private String currency;

    private Builder() {}

    public Builder returnUrl(String returnUrl) {
      this.returnUrl = Objects.requireNonNull(returnUrl, "returnUrl");
      return this;
    }

    public Builder paymentMethodId(String paymentMethodId) {
      this.paymentMethodId = Objects.requireNonNull(paymentMethodId, "paymentMethodId");
      return this;
    }

    public Builder expectedTotal(long expectedTotal) {
      this.expectedTotal = expectedTotal;
      return this;
    }

    public Builder currency(String currency) {
      this.currency = Objects.requireNonNull(currency, "currency");
      return this;
    }

    public SeatOrderCreateRequestPaymentSavedPaymentMethod build() {
      return new SeatOrderCreateRequestPaymentSavedPaymentMethod(this);
    }
  }
}
