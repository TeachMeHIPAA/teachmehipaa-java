// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.EventType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.webhookSubscriptions().create(…)}. Immutable. */
public final class WebhookSubscriptionCreateParams {
  private final String endpointUrl;
  private final List<EventType> eventTypes;
  private final String description;
  private final Long seatsLowThreshold;

  private WebhookSubscriptionCreateParams(Builder builder) {
    this.endpointUrl = builder.endpointUrl;
    this.eventTypes = builder.eventTypes;
    this.description = builder.description;
    this.seatsLowThreshold = builder.seatsLowThreshold;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The HTTPS URL to deliver events to. */
  public String getEndpointUrl() {
    return endpointUrl;
  }

  /** The event types to deliver. */
  public List<EventType> getEventTypes() {
    return eventTypes;
  }

  /** A description for your own reference. */
  public String getDescription() {
    return description;
  }

  /** Send <code>seats.low</code> when available seats drop to this number. Defaults to 5. */
  public Long getSeatsLowThreshold() {
    return seatsLowThreshold;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (endpointUrl != null) {
      json.put("endpointUrl", endpointUrl);
    }
    if (eventTypes != null) {
      json.put("eventTypes", eventTypes);
    }
    if (description != null) {
      json.put("description", description);
    }
    if (seatsLowThreshold != null) {
      json.put("seatsLowThreshold", seatsLowThreshold);
    }
    return json;
  }

  public static final class Builder {
    private String endpointUrl;
    private List<EventType> eventTypes;
    private String description;
    private Long seatsLowThreshold;

    private Builder() {}

    /** The HTTPS URL to deliver events to. Required. */
    public Builder endpointUrl(String endpointUrl) {
      this.endpointUrl = Objects.requireNonNull(endpointUrl, "endpointUrl");
      return this;
    }

    /** The event types to deliver. Required. */
    public Builder eventTypes(List<EventType> eventTypes) {
      this.eventTypes = List.copyOf(eventTypes);
      return this;
    }

    /** A description for your own reference. */
    public Builder description(String description) {
      this.description = Objects.requireNonNull(description, "description");
      return this;
    }

    /** Send <code>seats.low</code> when available seats drop to this number. Defaults to 5. */
    public Builder seatsLowThreshold(long seatsLowThreshold) {
      this.seatsLowThreshold = seatsLowThreshold;
      return this;
    }

    /**
     * @throws IllegalStateException when {@code endpointUrl}, {@code eventTypes} is not set
     */
    public WebhookSubscriptionCreateParams build() {
      if (endpointUrl == null) {
        throw new IllegalStateException("endpointUrl is required");
      }
      if (eventTypes == null) {
        throw new IllegalStateException("eventTypes is required");
      }
      return new WebhookSubscriptionCreateParams(this);
    }
  }
}
