package com.teachmehipaa.exception;

import java.time.Duration;

/** 429: too many requests. */
public class RateLimitException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  private final Duration retryAfter;

  /**
   * @param retryAfter how long the API asked the caller to wait, or {@code null}
   */
  public RateLimitException(String message, ErrorDetails details, Duration retryAfter) {
    super(message, details);
    this.retryAfter = retryAfter;
  }

  /** How long the API asked you to wait, from {@code Retry-After}, or {@code null}. */
  public Duration getRetryAfter() {
    return retryAfter;
  }
}
