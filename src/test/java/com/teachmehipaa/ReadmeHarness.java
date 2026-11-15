package com.teachmehipaa;

import com.teachmehipaa.core.PartnerGrantTokenProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What the README samples reach for, once {@link ReadmeTest} compiles them to run: builders wired
 * to the fake API, and the caller's own storage. Public because the samples compile outside this
 * package. {@link ReadmeTest} sets the fields before each sample and clears them after.
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

  /** Stands in for {@code PartnerGrantTokenProvider.builder()}. */
  public static PartnerGrantTokenProvider.Builder grantProviderBuilder() {
    return PartnerGrantTokenProvider.builder().httpTransport(api).clock(clock);
  }

  /** Stands in for {@code OAuth.ExchangeCodeParams.builder()}. */
  public static OAuth.ExchangeCodeParams.Builder exchangeCodeParamsBuilder() {
    return OAuth.ExchangeCodeParams.builder().httpTransport(api);
  }

  /** The {@code database} the samples name. */
  public static Storage storage() {
    return storage;
  }

  /** The caller's storage, in memory, recording what the samples save. */
  public static final class Storage {
    String consentState;
    String consentCodeVerifier;
    final Map<String, String> refreshTokens = new HashMap<>();
    String eventId;
    final List<String> savedEventIds = new ArrayList<>();

    public void saveConsent(String state, String codeVerifier) {
      consentState = state;
      consentCodeVerifier = codeVerifier;
    }

    public String consentState() {
      return consentState;
    }

    public String consentCodeVerifier() {
      return consentCodeVerifier;
    }

    public String loadRefreshToken(String orgId) {
      return refreshTokens.get(orgId);
    }

    public void saveRefreshToken(String orgId, String refreshToken) {
      refreshTokens.put(orgId, refreshToken);
    }

    public String loadEventId() {
      return eventId;
    }

    public void saveEventId(String eventId) {
      savedEventIds.add(eventId);
    }
  }

  private ReadmeHarness() {}
}
