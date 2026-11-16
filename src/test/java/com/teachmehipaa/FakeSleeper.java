package com.teachmehipaa;

import com.teachmehipaa.core.Sleeper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Records each wait instead of waiting, so retry tests run instantly. Given a clock, it moves the
 * clock on by each wait, as a real sleep would.
 */
final class FakeSleeper implements Sleeper {
  private final List<Duration> sleeps = new ArrayList<>();
  private final FakeClock clock;

  FakeSleeper() {
    this(null);
  }

  FakeSleeper(FakeClock clock) {
    this.clock = clock;
  }

  @Override
  public synchronized void sleep(Duration duration) {
    sleeps.add(duration);
    if (clock != null) {
      clock.advance(duration);
    }
  }

  synchronized List<Duration> sleeps() {
    return List.copyOf(sleeps);
  }
}
