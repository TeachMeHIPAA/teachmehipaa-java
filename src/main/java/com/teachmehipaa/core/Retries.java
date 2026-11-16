package com.teachmehipaa.core;

import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * When to retry a failed attempt, and how long to wait first (grilling Q18). Internal: only {@link
 * #backoff} is shared outside this package, so Operation polling backs off the same way.
 */
public final class Retries {
  private static final Duration INITIAL_DELAY = Duration.ofMillis(500);
  private static final Duration MAX_DELAY = Duration.ofSeconds(5);
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
    // Stripe's schedule: 0.5 s doubling to 5 s.
    return backoff(INITIAL_DELAY, MAX_DELAY, retryNumber);
  }

  /**
   * How long to wait before attempt {@code attemptNumber} (1 for the first wait): the ceiling
   * doubles from {@code initial} up to {@code max}, and jitter picks 50-100 % of it so that many
   * waiters don't retry in step. Nothing waits less than {@code initial}, and an {@code initial}
   * longer than {@code max} stays as it is.
   */
  public static Duration backoff(Duration initial, Duration max, int attemptNumber) {
    double base = initial.toNanos();
    double cap = Math.max(max.toNanos(), base);
    double ceiling = Math.min(base * Math.pow(2, attemptNumber - 1), cap);
    double jittered = ceiling * (0.5 + ThreadLocalRandom.current().nextDouble() * 0.5);
    return Duration.ofNanos(Math.round(Math.max(base, jittered)));
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
