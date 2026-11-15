package com.teachmehipaa;

import com.teachmehipaa.core.Sleeper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Records each wait instead of waiting, so retry tests run instantly. */
final class FakeSleeper implements Sleeper {
  private final List<Duration> sleeps = new ArrayList<>();

  @Override
  public synchronized void sleep(Duration duration) {
    sleeps.add(duration);
  }

  synchronized List<Duration> sleeps() {
    return List.copyOf(sleeps);
  }
}
