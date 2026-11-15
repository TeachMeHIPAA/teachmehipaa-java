// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.EmptyResponse;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.model.WebhookSubscription;
import com.teachmehipaa.model.WebhookSubscriptionCreated;
import com.teachmehipaa.param.WebhookSubscriptionCreateParams;
import com.teachmehipaa.param.WebhookSubscriptionListParams;
import com.teachmehipaa.param.WebhookSubscriptionUpdateParams;
import java.util.Objects;

/**
 * Webhook subscriptions in the client's Organization. Reach it through {@code
 * client.webhookSubscriptions()}.
 */
public final class WebhookSubscriptionService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public WebhookSubscriptionService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Create a webhook subscription. */
  public WebhookSubscriptionCreated create(WebhookSubscriptionCreateParams params) {
    return create(params, RequestOptions.none());
  }

  /**
   * Create a webhook subscription.
   *
   * <p>Subscribes an HTTPS endpoint to event types and creates its signing secret. Requires the
   * <code>webhooks:manage</code> or <code>org:admin</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link WebhookSubscriptionCreated}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public WebhookSubscriptionCreated create(
      WebhookSubscriptionCreateParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/webhook-subscriptions")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, WebhookSubscriptionCreated.class, options);
  }

  /** Delete a webhook subscription. */
  public EmptyResponse delete(String subscriptionId) {
    return delete(subscriptionId, RequestOptions.none());
  }

  /**
   * Delete a webhook subscription.
   *
   * <p>Deletes a webhook subscription and its signing secret. Requires the <code>webhooks:manage
   * </code> or <code>org:admin</code> scope.
   *
   * <p>Never retried: a lost response may hide a change that already happened.
   *
   * @param subscriptionId The webhook subscription id.
   * @param options settings for this call only, such as a timeout or request id
   * @return the response's metadata: the API answers 204 No Content
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public EmptyResponse delete(String subscriptionId, RequestOptions options) {
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder(
                "DELETE", "/organizations/{orgId}/webhook-subscriptions/{subscriptionId}")
            .pathParam("subscriptionId", subscriptionId)
            .build();
    return requester.requestEmpty(request, options);
  }

  /** List all webhook subscriptions. */
  public Page<WebhookSubscription> list() {
    return list(WebhookSubscriptionListParams.builder().build(), RequestOptions.none());
  }

  /** List all webhook subscriptions. */
  public Page<WebhookSubscription> list(RequestOptions options) {
    return list(WebhookSubscriptionListParams.builder().build(), options);
  }

  /** List all webhook subscriptions. */
  public Page<WebhookSubscription> list(WebhookSubscriptionListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all webhook subscriptions.
   *
   * <p>Returns a list of the organization's webhook subscriptions. Signing secrets are never
   * returned. Requires the <code>webhooks:manage</code> or <code>org:admin</code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link WebhookSubscription}; {@code nextCursor()} is {@code null} on the
   *     last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<WebhookSubscription> list(
      WebhookSubscriptionListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/webhook-subscriptions")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .retrySafe()
            .build();
    return requester.requestPage(
        request, Pagination.cursor("cursor"), WebhookSubscription.class, options);
  }

  /** Retry failed deliveries. */
  public Operation redrive(String subscriptionId) {
    return redrive(subscriptionId, RequestOptions.none());
  }

  /**
   * Retry failed deliveries.
   *
   * <p>Delivers the subscription's failed events again. Requires the <code>webhooks:manage</code>
   * or <code>org:admin</code> scope.
   *
   * @param subscriptionId The webhook subscription id.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which finishes in the background
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation redrive(String subscriptionId, RequestOptions options) {
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder(
                "POST", "/organizations/{orgId}/webhook-subscriptions/{subscriptionId}:redrive")
            .pathParam("subscriptionId", subscriptionId)
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Retrieve a webhook subscription. */
  public WebhookSubscription retrieve(String subscriptionId) {
    return retrieve(subscriptionId, RequestOptions.none());
  }

  /**
   * Retrieve a webhook subscription.
   *
   * <p>Retrieves a webhook subscription. Requires the <code>webhooks:manage</code> or <code>
   * org:admin</code> scope.
   *
   * @param subscriptionId The webhook subscription id.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link WebhookSubscription}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public WebhookSubscription retrieve(String subscriptionId, RequestOptions options) {
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/webhook-subscriptions/{subscriptionId}")
            .pathParam("subscriptionId", subscriptionId)
            .retrySafe()
            .build();
    return requester.request(request, WebhookSubscription.class, options);
  }

  /** Send a test event. */
  public Operation test(String subscriptionId) {
    return test(subscriptionId, RequestOptions.none());
  }

  /**
   * Send a test event.
   *
   * <p>Sends a signed test delivery to the subscription's endpoint, so you can check your verifier.
   * Requires the <code>webhooks:manage</code> or <code>org:admin</code> scope.
   *
   * @param subscriptionId The webhook subscription id.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which finishes in the background
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation test(String subscriptionId, RequestOptions options) {
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder(
                "POST", "/organizations/{orgId}/webhook-subscriptions/{subscriptionId}:test")
            .pathParam("subscriptionId", subscriptionId)
            .retrySafe()
            .idempotencyKeyRequired()
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Update a webhook subscription. */
  public WebhookSubscription update(String subscriptionId, WebhookSubscriptionUpdateParams params) {
    return update(subscriptionId, params, RequestOptions.none());
  }

  /**
   * Update a webhook subscription.
   *
   * <p>Updates a webhook subscription; parameters you don't send are left unchanged. Send its
   * current <code>ETag</code> in <code>If-Match</code>. The signing secret doesn't change. Requires
   * the <code>webhooks:manage</code> or <code>org:admin</code> scope.
   *
   * <p>Never retried: a lost response may hide a change that already happened.
   *
   * @param subscriptionId The webhook subscription id.
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link WebhookSubscription}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public WebhookSubscription update(
      String subscriptionId, WebhookSubscriptionUpdateParams params, RequestOptions options) {
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("PATCH", "/organizations/{orgId}/webhook-subscriptions/{subscriptionId}")
            .pathParam("subscriptionId", subscriptionId)
            .header("if-match", params.getIfMatch())
            .mergePatchBody(params)
            .build();
    return requester.request(request, WebhookSubscription.class, options);
  }
}
