package com.teachmehipaa;

import com.teachmehipaa.model.Event;
import com.teachmehipaa.model.TestDelivery;
import java.util.Objects;

/**
 * A verified Delivery: either an {@link Event} or, when someone asked TeachMeHIPAA to test the
 * endpoint, a {@link TestDelivery}, which carries no Event (ADR-0005).
 *
 * <pre>{@code
 * WebhookDelivery delivery = Webhook.constructEvent(body, headers, secret, path);
 * if (delivery instanceof WebhookDelivery.EventDelivery received
 *     && received.event() instanceof EventLearnerCertified certified) {
 *   recordCertification(certified.getData().getLearnerId());
 * }
 * }</pre>
 */
public sealed interface WebhookDelivery
    permits WebhookDelivery.EventDelivery, WebhookDelivery.TestDeliveryReceived {

  /**
   * A Delivery of an Event. Match {@link #event()} against the {@code Event*} types; an Event type
   * this version of the library doesn't know is an {@link com.teachmehipaa.model.UnknownEvent}.
   *
   * @param event the Event
   */
  record EventDelivery(Event event) implements WebhookDelivery {
    public EventDelivery {
      Objects.requireNonNull(event, "event");
    }
  }

  /**
   * A Test delivery, sent when someone tests the Webhook subscription.
   *
   * @param testDelivery the Test delivery's body
   */
  record TestDeliveryReceived(TestDelivery testDelivery) implements WebhookDelivery {
    public TestDeliveryReceived {
      Objects.requireNonNull(testDelivery, "testDelivery");
    }
  }
}
