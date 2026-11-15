package com.teachmehipaa;

import java.time.Duration;
import java.util.Objects;

/** Per-call settings that override the client's for one request. Immutable. */
public final class RequestOptions {
  private static final RequestOptions NONE = builder().build();

  private final String requestId;
  private final Duration timeout;
  private final Integer maxRetries;
  private final String idempotencyKey;

  private RequestOptions(Builder builder) {
    this.requestId = builder.requestId;
    this.timeout = builder.timeout;
    this.maxRetries = builder.maxRetries;
    this.idempotencyKey = builder.idempotencyKey;
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

  /** Retries for this call, or {@code null} to use the client's. */
  public Integer getMaxRetries() {
    return maxRetries;
  }

  /** The {@code Idempotency-Key} to send instead of a generated one, or {@code null}. */
  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public static final class Builder {
    private String requestId;
    private Duration timeout;
    private Integer maxRetries;
    private String idempotencyKey;

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

    /**
     * Retries for this call if it fails transiently. Only requests that are safe to replay are ever
     * retried, whatever this says.
     */
    public Builder maxRetries(int maxRetries) {
      if (maxRetries < 0) {
        throw new IllegalArgumentException("maxRetries must be zero or more");
      }
      this.maxRetries = maxRetries;
      return this;
    }

    /**
     * Replaces the generated {@code Idempotency-Key}. Reuse one key to make a call you repeat
     * yourself, such as a sync job's replay, run at most once. It never makes a call that isn't
     * safe to replay, such as a PATCH, retryable.
     */
    public Builder idempotencyKey(String idempotencyKey) {
      Objects.requireNonNull(idempotencyKey, "idempotencyKey");
      if (idempotencyKey.isBlank()) {
        throw new IllegalArgumentException("idempotencyKey must not be blank");
      }
      this.idempotencyKey = idempotencyKey;
      return this;
    }

    public RequestOptions build() {
      return new RequestOptions(this);
    }
  }
}
