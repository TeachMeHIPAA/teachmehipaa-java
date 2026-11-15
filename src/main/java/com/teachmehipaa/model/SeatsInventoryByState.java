// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

/**
 * Seats by internal state. Prefer <code>purchased</code>, <code>assigned</code> and <code>available
 * </code>.
 */
public final class SeatsInventoryByState extends ApiModel {
  private final long unused;
  private final long assigned;
  private final long accepted;
  private final long revoked;
  private final long free;
  private final long refunded;

  @JsonCreator
  SeatsInventoryByState(
      @JsonProperty("UNUSED") long unused,
      @JsonProperty("ASSIGNED") long assigned,
      @JsonProperty("ACCEPTED") long accepted,
      @JsonProperty("REVOKED") long revoked,
      @JsonProperty("FREE") long free,
      @JsonProperty("REFUNDED") long refunded) {
    this.unused = unused;
    this.assigned = assigned;
    this.accepted = accepted;
    this.revoked = revoked;
    this.free = free;
    this.refunded = refunded;
  }

  public long getUnused() {
    return unused;
  }

  public long getAssigned() {
    return assigned;
  }

  public long getAccepted() {
    return accepted;
  }

  public long getRevoked() {
    return revoked;
  }

  public long getFree() {
    return free;
  }

  public long getRefunded() {
    return refunded;
  }
}
