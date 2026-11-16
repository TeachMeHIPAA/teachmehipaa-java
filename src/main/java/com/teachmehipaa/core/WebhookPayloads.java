package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.model.Event;
import com.teachmehipaa.model.TestDelivery;
import java.io.IOException;

/** Reads a verified Delivery body. Internal: call {@code Webhook.constructEvent}. */
public final class WebhookPayloads {
  private WebhookPayloads() {}

  /**
   * The Delivery the body carries.
   *
   * @throws IllegalArgumentException when the body is not an Event or a Test delivery
   */
  public static WebhookDelivery parse(byte[] body) {
    try {
      JsonNode json = Json.MAPPER.readTree(body);
      if (!json.isObject()) {
        throw new IllegalArgumentException("The Delivery body is not a JSON object");
      }
      // A Test delivery is not an Event envelope (ADR-0005), so it never reaches the Event
      // deserializer, whose fallback would misread it as an UnknownEvent.
      if (json.path("test").isBoolean()
          && json.path("test").booleanValue()
          && json.has("subscriptionId")) {
        return new WebhookDelivery.TestDeliveryReceived(
            Json.MAPPER.treeToValue(json, TestDelivery.class));
      }
      return new WebhookDelivery.EventDelivery(Json.MAPPER.treeToValue(json, Event.class));
    } catch (JsonMappingException unreadable) {
      // The path names the field without quoting its value, which can be a Learner's email.
      throw new IllegalArgumentException(
          "The Delivery body could not be read at " + unreadable.getPathReference());
    } catch (IOException unreadable) {
      throw new IllegalArgumentException("The Delivery body is not JSON");
    }
  }
}
