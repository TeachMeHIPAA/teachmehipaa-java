// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class SeatsLowEventData extends ApiModel {
  private final long available;
  private final long threshold;

  @JsonCreator
  SeatsLowEventData(
      @JsonProperty("available") long available, @JsonProperty("threshold") long threshold) {
    this.available = available;
    this.threshold = threshold;
  }

  public long getAvailable() {
    return available;
  }

  public long getThreshold() {
    return threshold;
  }
}
