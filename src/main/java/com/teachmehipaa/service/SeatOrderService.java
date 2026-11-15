// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.SeatOrder;
import com.teachmehipaa.model.SeatOrderCreated;
import com.teachmehipaa.model.SeatOrderQuote;
import com.teachmehipaa.param.SeatOrderCreateParams;
import com.teachmehipaa.param.SeatOrderListParams;
import com.teachmehipaa.param.SeatOrderQuoteParams;
import java.util.Objects;

/** Seat orders in the client's Organization. Reach it through {@code client.seatOrders()}. */
public final class SeatOrderService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public SeatOrderService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Create a seat order. */
  public SeatOrderCreated create(SeatOrderCreateParams params) {
    return create(params, RequestOptions.none());
  }

  /**
   * Create a seat order.
   *
   * <p>Buys seats at list price. Pay with a Stripe-hosted checkout, the organization's saved card,
   * or an invoice when it's enabled. Send the <code>expectedTotal</code> and <code>currency</code>
   * from the quote you showed the buyer: if the price changed, nothing is charged. Seats are
   * granted once payment succeeds. Requires the <code>seats:purchase</code> or <code>billing:write
   * </code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link SeatOrderCreated}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public SeatOrderCreated create(SeatOrderCreateParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/seat-orders")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, SeatOrderCreated.class, options);
  }

  /** List all seat orders. */
  public Page<SeatOrder> list() {
    return list(SeatOrderListParams.builder().build(), RequestOptions.none());
  }

  /** List all seat orders. */
  public Page<SeatOrder> list(RequestOptions options) {
    return list(SeatOrderListParams.builder().build(), options);
  }

  /** List all seat orders. */
  public Page<SeatOrder> list(SeatOrderListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all seat orders.
   *
   * <p>Returns a list of the organization's seat orders, oldest first. Requires the <code>
   * seats:purchase</code> or <code>billing:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link SeatOrder}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<SeatOrder> list(SeatOrderListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/seat-orders")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .retrySafe()
            .build();
    return requester.requestPage(request, Pagination.cursor("cursor"), SeatOrder.class, options);
  }

  /** Quote a seat order. */
  public SeatOrderQuote quote(SeatOrderQuoteParams params) {
    return quote(params, RequestOptions.none());
  }

  /**
   * Quote a seat order.
   *
   * <p>Gets the list price for a number of seats in the organization's currency, before tax. The
   * quote is for display and expires after 15 minutes. Requires the <code>seats:purchase</code> or
   * <code>billing:write</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link SeatOrderQuote}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public SeatOrderQuote quote(SeatOrderQuoteParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/seat-orders:quote")
            .retrySafe()
            .jsonBody(params)
            .build();
    return requester.request(request, SeatOrderQuote.class, options);
  }

  /** Retrieve a seat order. */
  public SeatOrder retrieve(String seatOrderId) {
    return retrieve(seatOrderId, RequestOptions.none());
  }

  /**
   * Retrieve a seat order.
   *
   * <p>Retrieves a seat order, its status and the seats it granted. Requires the <code>
   * seats:purchase</code> or <code>billing:write</code> scope.
   *
   * @param seatOrderId The seat order id (<code>so_</code> + ULID).
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link SeatOrder}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public SeatOrder retrieve(String seatOrderId, RequestOptions options) {
    Objects.requireNonNull(seatOrderId, "seatOrderId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/seat-orders/{seatOrderId}")
            .pathParam("seatOrderId", seatOrderId)
            .retrySafe()
            .build();
    return requester.request(request, SeatOrder.class, options);
  }
}
