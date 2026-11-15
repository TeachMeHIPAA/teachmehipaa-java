// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;
import java.util.List;

public final class WebhookSubscription extends ApiResource {
  private final String subscriptionId;
  private final String endpointUrl;
  private final List<EventType> eventTypes;
  private final boolean enabled;
  private final String description;
  private final long seatsLowThreshold;
  private final String secretKeyId;
  private final OffsetDateTime createdAt;
  private final long rev;

  @JsonCreator
  WebhookSubscription(
      @JsonProperty("subscriptionId") String subscriptionId,
      @JsonProperty("endpointUrl") String endpointUrl,
      @JsonProperty("eventTypes") List<EventType> eventTypes,
      @JsonProperty("enabled") boolean enabled,
      @JsonProperty("description") String description,
      @JsonProperty("seatsLowThreshold") long seatsLowThreshold,
      @JsonProperty("secretKeyId") String secretKeyId,
      @JsonProperty("createdAt") OffsetDateTime createdAt,
      @JsonProperty("rev") long rev) {
    this.subscriptionId = subscriptionId;
    this.endpointUrl = endpointUrl;
    this.eventTypes = immutable(eventTypes);
    this.enabled = enabled;
    this.description = description;
    this.seatsLowThreshold = seatsLowThreshold;
    this.secretKeyId = secretKeyId;
    this.createdAt = createdAt;
    this.rev = rev;
  }

  /** Unique identifier for the subscription. */
  public String getSubscriptionId() {
    return subscriptionId;
  }

  /** The HTTPS URL events are delivered to. */
  public String getEndpointUrl() {
    return endpointUrl;
  }

  /** The event types delivered. */
  public List<EventType> getEventTypes() {
    return eventTypes;
  }

  /** Whether deliveries are on. */
  public boolean isEnabled() {
    return enabled;
  }

  /** Your description of the subscription. May be {@code null}. */
  public String getDescription() {
    return description;
  }

  /** Send <code>seats.low</code> when available seats drop to this number. Defaults to 5. */
  public long getSeatsLowThreshold() {
    return seatsLowThreshold;
  }

  /** Identifies the signing secret. Sent as <code>x-tmh-key-id</code>. */
  public String getSecretKeyId() {
    return secretKeyId;
  }

  /** When it was created (ISO 8601). */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /** The resource's revision. Its strong <code>ETag</code> is derived from it. */
  public long getRev() {
    return rev;
  }
}
