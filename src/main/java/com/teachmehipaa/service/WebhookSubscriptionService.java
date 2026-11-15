package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import java.util.Objects;

/**
 * Webhook subscriptions in the client's Organization. Reach it through {@code
 * client.webhookSubscriptions()}.
 */
public final class WebhookSubscriptionService {
  private final ApiRequester requester;

  public WebhookSubscriptionService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Deletes a Webhook subscription; no further Deliveries are sent to it. */
  public void delete(String subscriptionId) {
    delete(subscriptionId, RequestOptions.none());
  }

  /**
   * Deletes a Webhook subscription. Never retried: a lost response may hide a delete that happened.
   *
   * @param subscriptionId the Webhook subscription's id
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails, for example a
   *     {@link com.teachmehipaa.exception.NotFoundException} when there is no such subscription
   */
  public void delete(String subscriptionId, RequestOptions options) {
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder(
                "DELETE", "/organizations/{orgId}/webhook-subscriptions/{subscriptionId}")
            .pathParam("subscriptionId", subscriptionId)
            .build();
    requester.requestEmpty(request, options);
  }
}
