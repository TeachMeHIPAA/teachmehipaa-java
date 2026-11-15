// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.paymentMethod().confirm(…)}. Immutable. */
public final class PaymentMethodConfirmParams {
  private final String setupIntentId;

  private PaymentMethodConfirmParams(Builder builder) {
    this.setupIntentId = builder.setupIntentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The SetupIntent you confirmed with Stripe.js. */
  public String getSetupIntentId() {
    return setupIntentId;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (setupIntentId != null) {
      json.put("setupIntentId", setupIntentId);
    }
    return json;
  }

  public static final class Builder {
    private String setupIntentId;

    private Builder() {}

    /** The SetupIntent you confirmed with Stripe.js. Required. */
    public Builder setupIntentId(String setupIntentId) {
      this.setupIntentId = Objects.requireNonNull(setupIntentId, "setupIntentId");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code setupIntentId} is not set
     */
    public PaymentMethodConfirmParams build() {
      if (setupIntentId == null) {
        throw new IllegalStateException("setupIntentId is required");
      }
      return new PaymentMethodConfirmParams(this);
    }
  }
}
