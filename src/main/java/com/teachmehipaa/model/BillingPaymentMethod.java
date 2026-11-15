// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class BillingPaymentMethod extends ApiResource {
  private final BillingPaymentMethodDefaultPaymentMethod defaultPaymentMethod;
  private final boolean chargeable;

  @JsonCreator
  BillingPaymentMethod(
      @JsonProperty("defaultPaymentMethod")
          BillingPaymentMethodDefaultPaymentMethod defaultPaymentMethod,
      @JsonProperty("chargeable") boolean chargeable) {
    this.defaultPaymentMethod = defaultPaymentMethod;
    this.chargeable = chargeable;
  }

  /**
   * The saved card's brand, last four digits, expiry and id, or <code>null</code> when none is
   * saved. May be {@code null}.
   */
  public BillingPaymentMethodDefaultPaymentMethod getDefaultPaymentMethod() {
    return defaultPaymentMethod;
  }

  /** Whether a <code>saved_payment_method</code> order would charge it now. */
  public boolean isChargeable() {
    return chargeable;
  }
}
