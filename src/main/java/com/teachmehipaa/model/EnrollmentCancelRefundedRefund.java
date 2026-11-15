// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class EnrollmentCancelRefundedRefund extends ApiModel {
  private final String refundId;
  private final long amountCents;
  private final String currency;

  @JsonCreator
  EnrollmentCancelRefundedRefund(
      @JsonProperty("refundId") String refundId,
      @JsonProperty("amountCents") long amountCents,
      @JsonProperty("currency") String currency) {
    this.refundId = refundId;
    this.amountCents = amountCents;
    this.currency = currency;
  }

  public String getRefundId() {
    return refundId;
  }

  public long getAmountCents() {
    return amountCents;
  }

  public String getCurrency() {
    return currency;
  }
}
