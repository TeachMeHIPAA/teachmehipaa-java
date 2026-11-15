// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

/**
 * How to pay: <code>hosted_checkout</code> with a <code>returnUrl</code>, <code>
 * saved_payment_method</code> with the card's <code>paymentMethodId</code>, or <code>invoice</code>
 * . A <code>returnUrl</code> must be https and on your API client's allowlist.
 *
 * <p>One of {@link SeatOrderCreateRequestPaymentHostedCheckout}, {@link
 * SeatOrderCreateRequestPaymentInvoice}, {@link SeatOrderCreateRequestPaymentSavedPaymentMethod}.
 */
public sealed interface SeatOrderCreateRequestPayment
    permits SeatOrderCreateRequestPaymentHostedCheckout,
        SeatOrderCreateRequestPaymentInvoice,
        SeatOrderCreateRequestPaymentSavedPaymentMethod {}
