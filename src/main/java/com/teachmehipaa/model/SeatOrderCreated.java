// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class SeatOrderCreated extends ApiResource {
  private final String seatOrderId;
  private final SeatOrderCreatedStatus status;
  private final SeatOrderCreatedNextAction nextAction;
  private final SeatOrderCreatedFailure failure;

  @JsonCreator
  SeatOrderCreated(
      @JsonProperty("seatOrderId") String seatOrderId,
      @JsonProperty("status") SeatOrderCreatedStatus status,
      @JsonProperty("nextAction") SeatOrderCreatedNextAction nextAction,
      @JsonProperty("failure") SeatOrderCreatedFailure failure) {
    this.seatOrderId = seatOrderId;
    this.status = status;
    this.nextAction = nextAction;
    this.failure = failure;
  }

  /** Unique identifier for the seat order. */
  public String getSeatOrderId() {
    return seatOrderId;
  }

  /** The order's status. */
  public SeatOrderCreatedStatus getStatus() {
    return status;
  }

  /**
   * Where to send the buyer next: a checkout <code>url</code> to redirect to, or a hosted invoice.
   * May be {@code null}.
   */
  public SeatOrderCreatedNextAction getNextAction() {
    return nextAction;
  }

  /** Why the payment failed, if it did. May be {@code null}. */
  public SeatOrderCreatedFailure getFailure() {
    return failure;
  }
}
