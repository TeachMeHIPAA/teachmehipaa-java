package com.teachmehipaa;

import java.util.ArrayList;
import java.util.List;

/**
 * What the README samples reach for, once {@link ReadmeTest} compiles them to run: a client builder
 * wired to the fake API, and the caller's own storage. Public because the samples compile outside
 * this package. {@link ReadmeTest} sets the fields before each sample and clears them after.
 */
public final class ReadmeHarness {
  static volatile FakeTransport api;
  static volatile FakeClock clock;
  static volatile FakeSleeper sleeper;
  static volatile Storage storage;

  /** Stands in for {@code TeachMeHipaaClient.builder()}. */
  public static TeachMeHipaaClient.Builder clientBuilder() {
    return TeachMeHipaaClient.builder().httpTransport(api).clock(clock).sleeper(sleeper);
  }

  /** The {@code database} the samples name. */
  public static Storage storage() {
    return storage;
  }

  /** The caller's storage, in memory, recording what the samples save. */
  public static final class Storage {
    String eventId;
    final List<String> savedEventIds = new ArrayList<>();

    public String loadEventId() {
      return eventId;
    }

    public void saveEventId(String eventId) {
      savedEventIds.add(eventId);
    }
  }

  private ReadmeHarness() {}
}
