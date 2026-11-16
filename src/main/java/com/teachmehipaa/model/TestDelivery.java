// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

/** The body of a :test delivery. It is not an event envelope; its x-tmh-event-id is a UUID. */
public final class TestDelivery extends ApiModel {
  private final String subscriptionId;
  private final boolean test;

  @JsonCreator
  TestDelivery(
      @JsonProperty("subscriptionId") String subscriptionId, @JsonProperty("test") boolean test) {
    this.subscriptionId = subscriptionId;
    this.test = test;
  }

  /** The subscription that was tested. */
  public String getSubscriptionId() {
    return subscriptionId;
  }

  /** Always true. */
  public boolean isTest() {
    return test;
  }
}
