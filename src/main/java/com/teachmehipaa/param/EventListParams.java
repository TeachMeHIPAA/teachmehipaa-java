// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.teachmehipaa.model.EventType;
import java.util.Objects;

/** Parameters for {@code client.events().list(…)}. Immutable. */
public final class EventListParams {
  private final Long limit;
  private final String after;
  private final EventType type;

  private EventListParams(Builder builder) {
    this.limit = builder.limit;
    this.after = builder.after;
    this.type = builder.type;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** How many objects to return, between 1 and 100. Defaults to 20. */
  public Long getLimit() {
    return limit;
  }

  /** Return events after this event id (exclusive). The previous page's <code>nextCursor</code>. */
  public String getAfter() {
    return after;
  }

  /** Filter to one event type. */
  public EventType getType() {
    return type;
  }

  public static final class Builder {
    private Long limit;
    private String after;
    private EventType type;

    private Builder() {}

    /** How many objects to return, between 1 and 100. Defaults to 20. */
    public Builder limit(long limit) {
      this.limit = limit;
      return this;
    }

    /**
     * Return events after this event id (exclusive). The previous page's <code>nextCursor</code>.
     */
    public Builder after(String after) {
      this.after = Objects.requireNonNull(after, "after");
      return this;
    }

    /** Filter to one event type. */
    public Builder type(EventType type) {
      this.type = Objects.requireNonNull(type, "type");
      return this;
    }

    public EventListParams build() {
      return new EventListParams(this);
    }
  }
}
