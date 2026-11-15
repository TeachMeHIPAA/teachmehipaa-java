// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.ApiModel;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AuditEventSeatOrderPaid extends ApiModel implements AuditEvent {
  private final String eventId;
  private final String at;
  private final String actorSubjectId;
  private final AuditEventActor actor;
  private final String seatOrderId;
  private final long quantity;
  private final AuditEventPaymentMethod paymentMethod;
  private final String type;

  @JsonCreator
  AuditEventSeatOrderPaid(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("at") String at,
      @JsonProperty("actorSubjectId") String actorSubjectId,
      @JsonProperty("actor") AuditEventActor actor,
      @JsonProperty("seatOrderId") String seatOrderId,
      @JsonProperty("quantity") long quantity,
      @JsonProperty("paymentMethod") AuditEventPaymentMethod paymentMethod,
      @JsonProperty("type") String type) {
    this.eventId = eventId;
    this.at = at;
    this.actorSubjectId = actorSubjectId;
    this.actor = actor;
    this.seatOrderId = seatOrderId;
    this.quantity = quantity;
    this.paymentMethod = paymentMethod;
    this.type = type;
  }

  public String getEventId() {
    return eventId;
  }

  public String getAt() {
    return at;
  }

  public String getActorSubjectId() {
    return actorSubjectId;
  }

  public AuditEventActor getActor() {
    return actor;
  }

  public String getSeatOrderId() {
    return seatOrderId;
  }

  public long getQuantity() {
    return quantity;
  }

  public AuditEventPaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public String getType() {
    return type;
  }
}
