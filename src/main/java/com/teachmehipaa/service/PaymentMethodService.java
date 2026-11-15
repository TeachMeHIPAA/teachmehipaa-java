// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.EmptyResponse;
import com.teachmehipaa.model.BillingPaymentMethod;
import com.teachmehipaa.model.PaymentMethodPrepare;
import com.teachmehipaa.model.PaymentMethodSetup;
import com.teachmehipaa.param.PaymentMethodConfirmParams;
import com.teachmehipaa.param.PaymentMethodSetupParams;
import java.util.Objects;

/** Payment method in the client's Organization. Reach it through {@code client.paymentMethod()}. */
public final class PaymentMethodService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public PaymentMethodService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Finish saving a card. */
  public BillingPaymentMethod confirm(PaymentMethodConfirmParams params) {
    return confirm(params, RequestOptions.none());
  }

  /**
   * Finish saving a card.
   *
   * <p>Makes the card from a confirmed SetupIntent the organization's default for seat orders. Safe
   * to repeat. Requires the <code>seats:purchase</code> or <code>billing:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link BillingPaymentMethod}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public BillingPaymentMethod confirm(PaymentMethodConfirmParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/billing/payment-method:confirm")
            .retrySafe()
            .jsonBody(params)
            .build();
    return requester.request(request, BillingPaymentMethod.class, options);
  }

  /** Delete the saved card. */
  public EmptyResponse delete() {
    return delete(RequestOptions.none());
  }

  /**
   * Delete the saved card.
   *
   * <p>Removes the saved card. Seats already bought aren't affected. Requires the <code>
   * seats:purchase</code> or <code>billing:write</code> scope.
   *
   * <p>Never retried: a lost response may hide a change that already happened.
   *
   * @param options settings for this call only, such as a timeout or request id
   * @return the response's metadata: the API answers 204 No Content
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public EmptyResponse delete(RequestOptions options) {
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("DELETE", "/organizations/{orgId}/billing/payment-method").build();
    return requester.requestEmpty(request, options);
  }

  /** Start saving a card. */
  public PaymentMethodPrepare prepare() {
    return prepare(RequestOptions.none());
  }

  /**
   * Start saving a card.
   *
   * <p>Creates a Stripe SetupIntent for a card form in your own page. Confirm it with Stripe.js,
   * then finish saving the card. Saving a card never charges it. Requires the <code>seats:purchase
   * </code> or <code>billing:write</code> scope.
   *
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link PaymentMethodPrepare}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public PaymentMethodPrepare prepare(RequestOptions options) {
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/billing/payment-method:prepare")
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, PaymentMethodPrepare.class, options);
  }

  /** Retrieve the saved card. */
  public BillingPaymentMethod retrieve() {
    return retrieve(RequestOptions.none());
  }

  /**
   * Retrieve the saved card.
   *
   * <p>Retrieves the card seat orders charge: its brand, last four digits and expiry. Card numbers
   * are never returned. Requires the <code>seats:purchase</code> or <code>billing:write</code>
   * scope.
   *
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link BillingPaymentMethod}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public BillingPaymentMethod retrieve(RequestOptions options) {
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/billing/payment-method")
            .retrySafe()
            .build();
    return requester.request(request, BillingPaymentMethod.class, options);
  }

  /** Save a card with Checkout. */
  public PaymentMethodSetup setup(PaymentMethodSetupParams params) {
    return setup(params, RequestOptions.none());
  }

  /**
   * Save a card with Checkout.
   *
   * <p>Creates a Stripe-hosted page where the buyer saves the card that seat orders charge. The
   * card becomes the organization's default. Requires the <code>seats:purchase</code> or <code>
   * billing:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link PaymentMethodSetup}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public PaymentMethodSetup setup(PaymentMethodSetupParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/billing/payment-method:setup")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, PaymentMethodSetup.class, options);
  }
}
