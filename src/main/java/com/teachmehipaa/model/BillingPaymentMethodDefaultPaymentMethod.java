// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;
import java.time.OffsetDateTime;

public final class BillingPaymentMethodDefaultPaymentMethod extends ApiModel {
  private final String brand;
  private final String last4;
  private final long expMonth;
  private final long expYear;
  private final String paymentMethodId;
  private final PaymentMethodSavedBy savedBy;
  private final OffsetDateTime savedAt;
  private final boolean expired;

  @JsonCreator
  BillingPaymentMethodDefaultPaymentMethod(
      @JsonProperty("brand") String brand,
      @JsonProperty("last4") String last4,
      @JsonProperty("expMonth") long expMonth,
      @JsonProperty("expYear") long expYear,
      @JsonProperty("paymentMethodId") String paymentMethodId,
      @JsonProperty("savedBy") PaymentMethodSavedBy savedBy,
      @JsonProperty("savedAt") OffsetDateTime savedAt,
      @JsonProperty("expired") boolean expired) {
    this.brand = brand;
    this.last4 = last4;
    this.expMonth = expMonth;
    this.expYear = expYear;
    this.paymentMethodId = paymentMethodId;
    this.savedBy = savedBy;
    this.savedAt = savedAt;
    this.expired = expired;
  }

  public String getBrand() {
    return brand;
  }

  public String getLast4() {
    return last4;
  }

  public long getExpMonth() {
    return expMonth;
  }

  public long getExpYear() {
    return expYear;
  }

  /** May be {@code null}. */
  public String getPaymentMethodId() {
    return paymentMethodId;
  }

  /** May be {@code null}. */
  public PaymentMethodSavedBy getSavedBy() {
    return savedBy;
  }

  /** May be {@code null}. */
  public OffsetDateTime getSavedAt() {
    return savedAt;
  }

  public boolean isExpired() {
    return expired;
  }
}
