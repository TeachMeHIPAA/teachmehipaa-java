// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable. */
public final class SeatOrderCreateRequestPaymentInvoice implements SeatOrderCreateRequestPayment {
  private final Long daysUntilDue;

  private SeatOrderCreateRequestPaymentInvoice(Builder builder) {
    this.daysUntilDue = builder.daysUntilDue;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getDaysUntilDue() {
    return daysUntilDue;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("method", "invoice");
    if (daysUntilDue != null) {
      json.put("daysUntilDue", daysUntilDue);
    }
    return json;
  }

  public static final class Builder {
    private Long daysUntilDue;

    private Builder() {}

    public Builder daysUntilDue(long daysUntilDue) {
      this.daysUntilDue = daysUntilDue;
      return this;
    }

    public SeatOrderCreateRequestPaymentInvoice build() {
      return new SeatOrderCreateRequestPaymentInvoice(this);
    }
  }
}
