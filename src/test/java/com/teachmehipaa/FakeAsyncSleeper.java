package com.teachmehipaa;

import com.teachmehipaa.core.AsyncSleeper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Records each async wait instead of waiting, so async retry and poll tests take no wall-clock
 * time. Given a clock, it moves the clock on by each wait. A holding sleeper leaves each wait
 * pending until the test releases it, to cancel a call mid-wait.
 */
final class FakeAsyncSleeper implements AsyncSleeper {
  private final List<Duration> sleeps = new ArrayList<>();
  private final List<CompletableFuture<Void>> pending = new ArrayList<>();
  private final FakeClock clock;
  private final boolean holding;

  FakeAsyncSleeper() {
    this(null, false);
  }

  FakeAsyncSleeper(FakeClock clock) {
    this(clock, false);
  }

  private FakeAsyncSleeper(FakeClock clock, boolean holding) {
    this.clock = clock;
    this.holding = holding;
  }

  /** A sleeper whose waits stay pending until {@link #release()}. */
  static FakeAsyncSleeper holding() {
    return new FakeAsyncSleeper(null, true);
  }

  @Override
  public synchronized CompletableFuture<Void> sleep(Duration duration) {
    sleeps.add(duration);
    if (clock != null) {
      clock.advance(duration);
    }
    if (!holding) {
      return CompletableFuture.completedFuture(null);
    }
    CompletableFuture<Void> wait = new CompletableFuture<>();
    pending.add(wait);
    return wait;
  }

  synchronized List<Duration> sleeps() {
    return List.copyOf(sleeps);
  }

  /** Every wait handed out by a holding sleeper, in order. */
  synchronized List<CompletableFuture<Void>> waits() {
    return List.copyOf(pending);
  }

  /** Ends every pending wait, as if its time had passed. */
  void release() {
    waits().forEach(wait -> wait.complete(null));
  }
}
