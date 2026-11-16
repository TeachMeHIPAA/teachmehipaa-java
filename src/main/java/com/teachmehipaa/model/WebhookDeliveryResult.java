package com.teachmehipaa.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * How a Test delivery or a redrive ended: the response of the {@link WebhookDeliveryOperation} that
 * {@code client.webhookSubscriptions().test} and {@code .redrive} return. The spec leaves it
 * untyped; the webhook worker settles it with one of these, told apart by {@code _tag}.
 *
 * <p>Other tags may appear: a result this version of the library doesn't know reads as {@link
 * Unknown}, never as an error, so handle it.
 */
public sealed interface WebhookDeliveryResult {
  /** The {@code _tag} that selects the variant, or {@code null} when the response has none. */
  String tag();

  /**
   * The endpoint accepted the Delivery.
   *
   * @param responseStatus the endpoint's HTTP status
   */
  record Sent(int responseStatus) implements WebhookDeliveryResult {
    @Override
    public String tag() {
      return "sent";
    }
  }

  /**
   * Delivery stopped for good.
   *
   * @param reason why, such as {@code endpoint_gone}
   * @param responseStatus the endpoint's last HTTP status, or {@code null} when it never answered
   */
  record Failed(String reason, Integer responseStatus) implements WebhookDeliveryResult {
    public Failed {
      Objects.requireNonNull(reason, "reason");
    }

    @Override
    public String tag() {
      return "failed";
    }
  }

  /**
   * The Delivery had already ended before this Operation.
   *
   * @param status how it ended, such as {@code delivered}
   */
  record Settled(String status) implements WebhookDeliveryResult {
    public Settled {
      Objects.requireNonNull(status, "status");
    }

    @Override
    public String tag() {
      return "settled";
    }
  }

  /**
   * A result of a kind, or a shape, this version of the library doesn't know.
   *
   * @param tag its {@code _tag}, or {@code null}
   * @param response the whole response, every field kept
   */
  record Unknown(String tag, JsonNode response) implements WebhookDeliveryResult {
    public Unknown {
      Objects.requireNonNull(response, "response");
    }
  }
}
