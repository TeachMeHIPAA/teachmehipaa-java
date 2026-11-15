// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class SeatOrderCreatedNextActionInvoice extends ApiModel
    implements SeatOrderCreatedNextAction {
  private final String type;
  private final String hostedInvoiceUrl;

  @JsonCreator
  SeatOrderCreatedNextActionInvoice(
      @JsonProperty("type") String type,
      @JsonProperty("hostedInvoiceUrl") String hostedInvoiceUrl) {
    this.type = type;
    this.hostedInvoiceUrl = hostedInvoiceUrl;
  }

  public String getType() {
    return type;
  }

  public String getHostedInvoiceUrl() {
    return hostedInvoiceUrl;
  }
}
