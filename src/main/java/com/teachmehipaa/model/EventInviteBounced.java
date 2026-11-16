// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;
import java.time.OffsetDateTime;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class EventInviteBounced extends ApiModel implements Event {
  private final String eventId;
  private final String eventType;
  private final String orgId;
  private final OffsetDateTime occurredAt;
  private final LearnerEventData data;

  @JsonCreator
  EventInviteBounced(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("eventType") String eventType,
      @JsonProperty("orgId") String orgId,
      @JsonProperty("occurredAt") OffsetDateTime occurredAt,
      @JsonProperty("data") LearnerEventData data) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.orgId = orgId;
    this.occurredAt = occurredAt;
    this.data = data;
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

  public LearnerEventData getData() {
    return data;
  }
}
