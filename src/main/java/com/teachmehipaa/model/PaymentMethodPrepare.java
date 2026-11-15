// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class PaymentMethodPrepare extends ApiResource {
  private final String setupIntentId;
  private final String clientSecret;
  private final String publishableKey;

  @JsonCreator
  PaymentMethodPrepare(
      @JsonProperty("setupIntentId") String setupIntentId,
      @JsonProperty("clientSecret") String clientSecret,
      @JsonProperty("publishableKey") String publishableKey) {
    this.setupIntentId = setupIntentId;
    this.clientSecret = clientSecret;
    this.publishableKey = publishableKey;
  }

  /** The Stripe SetupIntent. */
  public String getSetupIntentId() {
    return setupIntentId;
  }

  /** Confirm the SetupIntent with Stripe.js using this secret. */
  public String getClientSecret() {
    return clientSecret;
  }

  /** The Stripe publishable key to load Stripe.js with. */
  public String getPublishableKey() {
    return publishableKey;
  }
}
