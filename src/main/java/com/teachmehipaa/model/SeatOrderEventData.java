// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class SeatOrderEventData extends ApiModel {
  private final String seatOrderId;
  private final long quantity;
  private final SeatOrderEventDataStatus status;

  @JsonCreator
  SeatOrderEventData(
      @JsonProperty("seatOrderId") String seatOrderId,
      @JsonProperty("quantity") long quantity,
      @JsonProperty("status") SeatOrderEventDataStatus status) {
    this.seatOrderId = seatOrderId;
    this.quantity = quantity;
    this.status = status;
  }

  public String getSeatOrderId() {
    return seatOrderId;
  }

  public long getQuantity() {
    return quantity;
  }

  public SeatOrderEventDataStatus getStatus() {
    return status;
  }
}
