package com.teachmehipaa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What the doc samples reach for, once {@link DocSampleTest} compiles them to run: a client builder
 * wired to the fake API, and the caller's own storage. Public because the samples compile outside
 * this package. {@link DocSampleTest} sets the fields before each sample and clears them after.
 */
public final class DocHarness {
  static volatile FakeTransport api;
  static volatile FakeClock clock;
  static volatile FakeSleeper sleeper;
  static volatile FakeAsyncSleeper asyncSleeper;
  static volatile Storage storage;

  /** Stands in for {@code TeachMeHipaaClient.builder()}. */
  public static TeachMeHipaaClient.Builder clientBuilder() {
    return TeachMeHipaaClient.builder()
        .httpTransport(api)
        .clock(clock)
        .sleeper(sleeper)
        .asyncSleeper(asyncSleeper);
  }

  /** The {@code database} the samples name. */
  public static Storage storage() {
    return storage;
  }

  /** The caller's storage, in memory, recording what the samples save. */
  public static final class Storage {
    String eventId;
    final List<String> savedEventIds = new ArrayList<>();
    String operationName;
    String signingSecret;
    final Set<String> processed = new HashSet<>();
    final List<String> handled = new ArrayList<>();

    public String loadEventId() {
      return eventId;
    }

    public void saveEventId(String eventId) {
      savedEventIds.add(eventId);
    }

    public String loadOperationName() {
      return operationName;
    }

    public void saveOperationName(String name) {
      operationName = name;
    }

    public void saveSigningSecret(String secret) {
      signingSecret = secret;
    }

    public boolean isProcessed(String eventId) {
      return processed.contains(eventId);
    }

    public void markProcessed(String eventId) {
      processed.add(eventId);
      handled.add(eventId);
    }
  }

  private DocHarness() {}
}
