// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class PaymentMethodSetup extends ApiResource {
  private final String url;

  @JsonCreator
  PaymentMethodSetup(@JsonProperty("url") String url) {
    this.url = url;
  }

  /** The Stripe-hosted page to send the buyer to. */
  public String getUrl() {
    return url;
  }
}
