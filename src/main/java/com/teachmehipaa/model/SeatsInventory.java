// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class SeatsInventory extends ApiResource {
  private final long total;
  private final long used;
  private final long available;
  private final Long purchased;
  private final Long assigned;
  private final SeatsInventoryByState byState;

  @JsonCreator
  SeatsInventory(
      @JsonProperty("total") long total,
      @JsonProperty("used") long used,
      @JsonProperty("available") long available,
      @JsonProperty("purchased") Long purchased,
      @JsonProperty("assigned") Long assigned,
      @JsonProperty("byState") SeatsInventoryByState byState) {
    this.total = total;
    this.used = used;
    this.available = available;
    this.purchased = purchased;
    this.assigned = assigned;
    this.byState = byState;
  }

  /** All seats on record. */
  public long getTotal() {
    return total;
  }

  /** Seats in use. */
  public long getUsed() {
    return used;
  }

  /** Seats a new invite can use now. */
  public long getAvailable() {
    return available;
  }

  /**
   * Seats the organization holds and hasn't been refunded for, including a trial seat. May be
   * {@code null}.
   */
  public Long getPurchased() {
    return purchased;
  }

  /** Seats held by an invited or enrolled learner. May be {@code null}. */
  public Long getAssigned() {
    return assigned;
  }

  /**
   * Seats by internal state. Prefer <code>purchased</code>, <code>assigned</code> and <code>
   * available</code>.
   */
  public SeatsInventoryByState getByState() {
    return byState;
  }
}
