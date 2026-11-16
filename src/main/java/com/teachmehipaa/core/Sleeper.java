package com.teachmehipaa.core;

import java.time.Duration;

/** Waits between retry attempts. Inject one so retry tests run without real delays. */
@FunctionalInterface
public interface Sleeper {
  /** The real thing: {@link Thread#sleep}. */
  Sleeper SYSTEM = duration -> Thread.sleep(duration.toMillis());

  void sleep(Duration duration) throws InterruptedException;
}
