// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable. */
public final class SeatOrderCreateRequestPaymentHostedCheckout
    implements SeatOrderCreateRequestPayment {
  private final String returnUrl;

  private SeatOrderCreateRequestPaymentHostedCheckout(Builder builder) {
    this.returnUrl = builder.returnUrl;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("method", "hosted_checkout");
    if (returnUrl != null) {
      json.put("returnUrl", returnUrl);
    }
    return json;
  }

  public static final class Builder {
    private String returnUrl;

    private Builder() {}

    /** Required. */
    public Builder returnUrl(String returnUrl) {
      this.returnUrl = Objects.requireNonNull(returnUrl, "returnUrl");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code returnUrl} is not set
     */
    public SeatOrderCreateRequestPaymentHostedCheckout build() {
      if (returnUrl == null) {
        throw new IllegalStateException("returnUrl is required");
      }
      return new SeatOrderCreateRequestPaymentHostedCheckout(this);
    }
  }
}
