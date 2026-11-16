package com.teachmehipaa.core;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Waits between retry attempts and Operation polls of the {@code …Async} methods, without holding a
 * thread: the {@link Sleeper}'s counterpart. Inject one so async retry tests run without real
 * delays.
 */
@FunctionalInterface
public interface AsyncSleeper {
  /**
   * The real thing: completes after {@code duration} on {@link CompletableFuture#delayedExecutor}'s
   * timer. No thread waits, and the client owns nothing that needs closing.
   */
  AsyncSleeper SYSTEM =
      duration -> {
        CompletableFuture<Void> elapsed = new CompletableFuture<>();
        CompletableFuture.delayedExecutor(nanos(duration), TimeUnit.NANOSECONDS)
            .execute(() -> elapsed.complete(null));
        return elapsed;
      };

  /**
   * Returns a future that completes once {@code duration} has passed. The client cancels it when
   * the call it delays is cancelled; an implementation may stop its timer then.
   */
  CompletableFuture<Void> sleep(Duration duration);

  // Duration.toNanos overflows past 292 years; a wait that long never ends anyway.
  private static long nanos(Duration duration) {
    try {
      return Math.max(0, duration.toNanos());
    } catch (ArithmeticException tooLong) {
      return Long.MAX_VALUE;
    }
  }
}
