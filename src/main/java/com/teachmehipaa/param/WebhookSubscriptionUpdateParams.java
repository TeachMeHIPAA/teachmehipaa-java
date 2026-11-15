// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.EventType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for {@code client.webhookSubscriptions().update(…)}. Immutable.
 *
 * <p>Sent as a merge patch: a field you don't set is left unchanged, and a {@code clear} method
 * sends {@code null} to clear one.
 */
public final class WebhookSubscriptionUpdateParams {
  private final String endpointUrl;
  private final List<EventType> eventTypes;
  private final Boolean enabled;
  private final String description;
  private final boolean descriptionSet;
  private final Long seatsLowThreshold;
  private final String ifMatch;

  private WebhookSubscriptionUpdateParams(Builder builder) {
    this.endpointUrl = builder.endpointUrl;
    this.eventTypes = builder.eventTypes;
    this.enabled = builder.enabled;
    this.description = builder.description;
    this.descriptionSet = builder.descriptionSet;
    this.seatsLowThreshold = builder.seatsLowThreshold;
    this.ifMatch = builder.ifMatch;
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

  /** Turn deliveries on or off. */
  public Boolean getEnabled() {
    return enabled;
  }

  /** A description for your own reference. */
  public String getDescription() {
    return description;
  }

  /** Send <code>seats.low</code> when available seats drop to this number. */
  public Long getSeatsLowThreshold() {
    return seatsLowThreshold;
  }

  /**
   * The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a
   * stale one with 412 and a missing one with 428.
   */
  public String getIfMatch() {
    return ifMatch;
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
    if (enabled != null) {
      json.put("enabled", enabled);
    }
    if (descriptionSet) {
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
    private Boolean enabled;
    private String description;
    private boolean descriptionSet;
    private Long seatsLowThreshold;
    private String ifMatch;

    private Builder() {}

    /** The HTTPS URL to deliver events to. */
    public Builder endpointUrl(String endpointUrl) {
      this.endpointUrl = Objects.requireNonNull(endpointUrl, "endpointUrl");
      return this;
    }

    /** The event types to deliver. */
    public Builder eventTypes(List<EventType> eventTypes) {
      this.eventTypes = List.copyOf(eventTypes);
      return this;
    }

    /** Turn deliveries on or off. */
    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /** A description for your own reference. */
    public Builder description(String description) {
      this.description = Objects.requireNonNull(description, "description");
      this.descriptionSet = true;
      return this;
    }

    /** Sends {@code null} for {@code description}, clearing it. */
    public Builder clearDescription() {
      this.description = null;
      this.descriptionSet = true;
      return this;
    }

    /** Send <code>seats.low</code> when available seats drop to this number. */
    public Builder seatsLowThreshold(long seatsLowThreshold) {
      this.seatsLowThreshold = seatsLowThreshold;
      return this;
    }

    /**
     * The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a
     * stale one with 412 and a missing one with 428. Required.
     */
    public Builder ifMatch(String ifMatch) {
      this.ifMatch = Objects.requireNonNull(ifMatch, "ifMatch");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code ifMatch} is not set
     */
    public WebhookSubscriptionUpdateParams build() {
      if (ifMatch == null) {
        throw new IllegalStateException("ifMatch is required");
      }
      return new WebhookSubscriptionUpdateParams(this);
    }
  }
}
