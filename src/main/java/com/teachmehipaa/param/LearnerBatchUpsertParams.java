// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.LearnerBatchUpsertRequestDelivery;
import com.teachmehipaa.model.LearnerBatchUpsertRequestSeatPolicy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.learners().batchUpsert(…)}. Immutable. */
public final class LearnerBatchUpsertParams {
  private final List<LearnerBatchUpsertRequestItemsItem> items;
  private final LearnerBatchUpsertRequestDelivery delivery;
  private final LearnerBatchUpsertRequestSeatPolicy seatPolicy;

  private LearnerBatchUpsertParams(Builder builder) {
    this.items = builder.items;
    this.delivery = builder.delivery;
    this.seatPolicy = builder.seatPolicy;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Up to 500 learners, each with the same fields as creating one learner. */
  public List<LearnerBatchUpsertRequestItemsItem> getItems() {
    return items;
  }

  /**
   * Applies to every item. <code>email</code> (default) sends invites; <code>none</code> sends
   * nothing.
   */
  public LearnerBatchUpsertRequestDelivery getDelivery() {
    return delivery;
  }

  /** Applies to every item. <code>require_available</code> fails an item when no seat is free. */
  public LearnerBatchUpsertRequestSeatPolicy getSeatPolicy() {
    return seatPolicy;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (items != null) {
      json.put("items", items);
    }
    if (delivery != null) {
      json.put("delivery", delivery);
    }
    if (seatPolicy != null) {
      json.put("seatPolicy", seatPolicy);
    }
    return json;
  }

  public static final class Builder {
    private List<LearnerBatchUpsertRequestItemsItem> items;
    private LearnerBatchUpsertRequestDelivery delivery;
    private LearnerBatchUpsertRequestSeatPolicy seatPolicy;

    private Builder() {}

    /** Up to 500 learners, each with the same fields as creating one learner. Required. */
    public Builder items(List<LearnerBatchUpsertRequestItemsItem> items) {
      this.items = List.copyOf(items);
      return this;
    }

    /**
     * Applies to every item. <code>email</code> (default) sends invites; <code>none</code> sends
     * nothing.
     */
    public Builder delivery(LearnerBatchUpsertRequestDelivery delivery) {
      this.delivery = Objects.requireNonNull(delivery, "delivery");
      return this;
    }

    /** Applies to every item. <code>require_available</code> fails an item when no seat is free. */
    public Builder seatPolicy(LearnerBatchUpsertRequestSeatPolicy seatPolicy) {
      this.seatPolicy = Objects.requireNonNull(seatPolicy, "seatPolicy");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code items} is not set
     */
    public LearnerBatchUpsertParams build() {
      if (items == null) {
        throw new IllegalStateException("items is required");
      }
      return new LearnerBatchUpsertParams(this);
    }
  }
}
