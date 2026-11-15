package com.teachmehipaa.param;

import com.teachmehipaa.model.EventType;
import java.time.Duration;
import java.util.Objects;

/** Parameters for {@code client.events().tail(…)}. Immutable. Hand-written. */
public final class EventTailParams {
  private static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(2);

  private final String after;
  private final EventType type;
  private final Long limit;
  private final boolean follow;
  private final Duration interval;

  private EventTailParams(Builder builder) {
    this.after = builder.after;
    this.type = builder.type;
    this.limit = builder.limit;
    this.follow = builder.follow;
    this.interval = builder.interval;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The Event id to start after (exclusive), or {@code null} to start at the oldest retained. */
  public String getAfter() {
    return after;
  }

  /** The one Event type to tail, or {@code null} for all. */
  public EventType getType() {
    return type;
  }

  /** How many Events each request returns, or {@code null} for the API's default. */
  public Long getLimit() {
    return limit;
  }

  /** Whether the tail keeps polling once caught up instead of ending. */
  public boolean isFollow() {
    return follow;
  }

  /** How long a following tail waits between polls once caught up. */
  public Duration getInterval() {
    return interval;
  }

  public static final class Builder {
    private String after;
    private EventType type;
    private Long limit;
    private boolean follow;
    private Duration interval = DEFAULT_INTERVAL;

    private Builder() {}

    /**
     * Yield only Events after this id (exclusive), such as a saved {@code lastEventId()}. {@code
     * null}, meaning nothing saved yet, starts at the oldest retained Event.
     */
    public Builder after(String after) {
      this.after = after;
      return this;
    }

    /** Tail one Event type. */
    public Builder type(EventType type) {
      this.type = Objects.requireNonNull(type, "type");
      return this;
    }

    /** How many Events each request returns, between 1 and 100. Defaults to 20. */
    public Builder limit(long limit) {
      this.limit = limit;
      return this;
    }

    /** Once caught up, keep polling for new Events instead of ending. Off by default. */
    public Builder follow(boolean follow) {
      this.follow = follow;
      return this;
    }

    /** How long a following tail waits between polls once caught up. Defaults to 2 seconds. */
    public Builder interval(Duration interval) {
      Objects.requireNonNull(interval, "interval");
      if (interval.isNegative()) {
        throw new IllegalArgumentException("interval must not be negative");
      }
      this.interval = interval;
      return this;
    }

    public EventTailParams build() {
      return new EventTailParams(this);
    }
  }
}
