// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class SeatOrderQuote extends ApiResource {
  private final long quantity;
  private final long unitAmount;
  private final String currency;
  private final long subtotal;
  private final JsonNode tax;
  private final long total;
  private final OffsetDateTime quoteExpiresAt;

  @JsonCreator
  SeatOrderQuote(
      @JsonProperty("quantity") long quantity,
      @JsonProperty("unitAmount") long unitAmount,
      @JsonProperty("currency") String currency,
      @JsonProperty("subtotal") long subtotal,
      @JsonProperty("tax") JsonNode tax,
      @JsonProperty("total") long total,
      @JsonProperty("quoteExpiresAt") OffsetDateTime quoteExpiresAt) {
    this.quantity = quantity;
    this.unitAmount = unitAmount;
    this.currency = currency;
    this.subtotal = subtotal;
    this.tax = tax;
    this.total = total;
    this.quoteExpiresAt = quoteExpiresAt;
  }

  /** Seats quoted. */
  public long getQuantity() {
    return quantity;
  }

  /** Price per seat, in the currency's smallest unit. */
  public long getUnitAmount() {
    return unitAmount;
  }

  /** Three-letter ISO currency code, lowercase. */
  public String getCurrency() {
    return currency;
  }

  /** Price before tax. */
  public long getSubtotal() {
    return subtotal;
  }

  /** Always <code>null</code>: tax is calculated at payment. May be {@code null}. */
  public JsonNode getTax() {
    return tax;
  }

  /** The quoted total. */
  public long getTotal() {
    return total;
  }

  /** When the quote expires, 15 minutes after it was made. */
  public OffsetDateTime getQuoteExpiresAt() {
    return quoteExpiresAt;
  }
}
