package com.teachmehipaa.core;

import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/** When to retry a failed attempt, and how long to wait first (grilling Q18). */
final class Retries {
  private static final long INITIAL_DELAY_MILLIS = 500;
  private static final long MAX_DELAY_MILLIS = 5_000;
  private static final Duration MAX_RETRY_AFTER = Duration.ofSeconds(60);

  /** Failures that leave no trace on the server, or that it asks us to retry. */
  static boolean isTransient(TeachMeHipaaException error) {
    Integer status = error.getStatusCode();
    return error instanceof ApiConnectionException
        || (status != null && (status == 429 || status >= 500))
        || (status != null && status == 409 && "conflict".equals(error.getCode()));
  }

  /** Honors {@code Retry-After} up to 60 s, otherwise backs off exponentially. */
  static Duration delay(TeachMeHipaaException error, int retryNumber, Instant now) {
    Duration retryAfter = RetryAfter.from(error.getHeaders(), now);
    if (retryAfter != null) {
      return retryAfter.compareTo(MAX_RETRY_AFTER) > 0 ? MAX_RETRY_AFTER : retryAfter;
    }
    return backoff(retryNumber);
  }

  // Stripe's schedule: the ceiling doubles from 0.5 s to 5 s, jitter picks 50-100 % of it, and
  // nothing waits less than 0.5 s.
  private static Duration backoff(int retryNumber) {
    long ceiling = INITIAL_DELAY_MILLIS << Math.min(retryNumber - 1, 10);
    ceiling = Math.min(ceiling, MAX_DELAY_MILLIS);
    double jittered = ceiling * (0.5 + ThreadLocalRandom.current().nextDouble() * 0.5);
    return Duration.ofMillis(Math.max(INITIAL_DELAY_MILLIS, Math.round(jittered)));
  }

  /**
   * Waits before a retry. An interrupt stops the retries: the thread keeps its interrupt flag and
   * the caller gets the failure it would have retried.
   */
  static void sleep(Sleeper sleeper, Duration delay, TeachMeHipaaException failure) {
    try {
      sleeper.sleep(delay);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      failure.addSuppressed(interrupted);
      throw failure;
    }
  }

  private Retries() {}
}
