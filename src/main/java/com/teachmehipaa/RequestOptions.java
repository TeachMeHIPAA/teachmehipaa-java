package com.teachmehipaa;

import java.time.Duration;
import java.util.Objects;

/** Per-call settings that override the client's for one request. Immutable. */
public final class RequestOptions {
  private static final RequestOptions NONE = builder().build();

  private final String requestId;
  private final Duration timeout;

  private RequestOptions(Builder builder) {
    this.requestId = builder.requestId;
    this.timeout = builder.timeout;
  }

  /** Options that override nothing. */
  public static RequestOptions none() {
    return NONE;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The {@code x-request-id} to send, or {@code null} to let the API assign one. */
  public String getRequestId() {
    return requestId;
  }

  /** The timeout for this call, or {@code null} to use the client's. */
  public Duration getTimeout() {
    return timeout;
  }

  public static final class Builder {
    private String requestId;
    private Duration timeout;

    private Builder() {}

    /**
     * Sends this as {@code x-request-id} so your logs can be matched with TeachMeHIPAA's. It must
     * match {@code ^[A-Za-z0-9._-]{1,128}$} or the API replaces it.
     */
    public Builder requestId(String requestId) {
      this.requestId = Objects.requireNonNull(requestId, "requestId");
      return this;
    }

    /** How long to wait for this call's response. */
    public Builder timeout(Duration timeout) {
      Objects.requireNonNull(timeout, "timeout");
      if (timeout.isNegative() || timeout.isZero()) {
        throw new IllegalArgumentException("timeout must be positive");
      }
      this.timeout = timeout;
      return this;
    }

    public RequestOptions build() {
      return new RequestOptions(this);
    }
  }
}
