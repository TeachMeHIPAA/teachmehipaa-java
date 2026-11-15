// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class EventSeatConsumed extends ApiModel implements Event {
  private final String eventId;
  private final String eventType;
  private final String orgId;
  private final OffsetDateTime occurredAt;
  private final Map<String, JsonNode> data;

  @JsonCreator
  EventSeatConsumed(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("eventType") String eventType,
      @JsonProperty("orgId") String orgId,
      @JsonProperty("occurredAt") OffsetDateTime occurredAt,
      @JsonProperty("data") Map<String, JsonNode> data) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.orgId = orgId;
    this.occurredAt = occurredAt;
    this.data = immutable(data);
  }

  public String getEventId() {
    return eventId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getOrgId() {
    return orgId;
  }

  public OffsetDateTime getOccurredAt() {
    return occurredAt;
  }

  public Map<String, JsonNode> getData() {
    return data;
  }
}
