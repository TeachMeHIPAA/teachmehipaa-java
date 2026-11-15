// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class SeatOrder extends ApiResource {
  private final String seatOrderId;
  private final String orgId;
  private final long quantity;
  private final long unitAmount;
  private final String currency;
  private final long total;
  private final SeatOrderPaymentMethod paymentMethod;
  private final SeatOrderStatus status;
  private final long seatsGranted;
  private final long seatsRefunded;
  private final SeatOrderFailure failure;
  private final String clientId;
  private final String clientReference;
  private final String checkoutId;
  private final OffsetDateTime createdAt;
  private final OffsetDateTime updatedAt;
  private final long rev;

  @JsonCreator
  SeatOrder(
      @JsonProperty("seatOrderId") String seatOrderId,
      @JsonProperty("orgId") String orgId,
      @JsonProperty("quantity") long quantity,
      @JsonProperty("unitAmount") long unitAmount,
      @JsonProperty("currency") String currency,
      @JsonProperty("total") long total,
      @JsonProperty("paymentMethod") SeatOrderPaymentMethod paymentMethod,
      @JsonProperty("status") SeatOrderStatus status,
      @JsonProperty("seatsGranted") long seatsGranted,
      @JsonProperty("seatsRefunded") long seatsRefunded,
      @JsonProperty("failure") SeatOrderFailure failure,
      @JsonProperty("clientId") String clientId,
      @JsonProperty("clientReference") String clientReference,
      @JsonProperty("checkoutId") String checkoutId,
      @JsonProperty("createdAt") OffsetDateTime createdAt,
      @JsonProperty("updatedAt") OffsetDateTime updatedAt,
      @JsonProperty("rev") long rev) {
    this.seatOrderId = seatOrderId;
    this.orgId = orgId;
    this.quantity = quantity;
    this.unitAmount = unitAmount;
    this.currency = currency;
    this.total = total;
    this.paymentMethod = paymentMethod;
    this.status = status;
    this.seatsGranted = seatsGranted;
    this.seatsRefunded = seatsRefunded;
    this.failure = failure;
    this.clientId = clientId;
    this.clientReference = clientReference;
    this.checkoutId = checkoutId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.rev = rev;
  }

  /** Unique identifier for the seat order. */
  public String getSeatOrderId() {
    return seatOrderId;
  }

  /** The organization's id. */
  public String getOrgId() {
    return orgId;
  }

  /** Seats ordered. */
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

  /** The order total, in the currency's smallest unit. */
  public long getTotal() {
    return total;
  }

  /** <code>hosted_checkout</code>, <code>saved_payment_method</code> or <code>invoice</code>. */
  public SeatOrderPaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  /**
   * <code>requires_action</code>, <code>processing</code>, <code>invoiced</code>, <code>paid</code>
   * , <code>failed</code>, <code>expired</code> or <code>refunded</code>.
   */
  public SeatOrderStatus getStatus() {
    return status;
  }

  /** Seats granted so far. */
  public long getSeatsGranted() {
    return seatsGranted;
  }

  /** Seats refunded through support. */
  public long getSeatsRefunded() {
    return seatsRefunded;
  }

  /** Why the order failed, with Stripe's decline code when there is one. May be {@code null}. */
  public SeatOrderFailure getFailure() {
    return failure;
  }

  /** The API client that placed the order, if one did. May be {@code null}. */
  public String getClientId() {
    return clientId;
  }

  /** Your own reference for the order. May be {@code null}. */
  public String getClientReference() {
    return clientReference;
  }

  /** The Stripe checkout session, for a hosted checkout. May be {@code null}. */
  public String getCheckoutId() {
    return checkoutId;
  }

  /** When it was created (ISO 8601). */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /** When it last changed. */
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  /** The resource's revision. Its strong <code>ETag</code> is derived from it. */
  public long getRev() {
    return rev;
  }
}
