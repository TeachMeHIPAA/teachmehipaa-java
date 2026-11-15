package com.teachmehipaa.core;

import com.teachmehipaa.AppInfo;
import com.teachmehipaa.RequestLogger;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * A client's settings after validation. Internal: built by {@code TeachMeHipaaClient.Builder} and
 * shared, unchanged, by everything the client creates.
 *
 * @param baseUrl the API base URL, without a trailing slash
 * @param organizationId the Organization given up front, or {@code null} to discover it
 * @param scopes the Scopes to request; empty requests every Scope the API client holds
 * @param maxRetries retries for a failed request that is safe to replay
 * @param timeout how long to wait for each response
 * @param appInfo the integration named in the {@code User-Agent}, or {@code null}
 * @param transport sends every HTTP request
 * @param clock tells the time for token expiry
 * @param sleeper waits between retries
 * @param logger receives an entry per response, or {@code null} for no logging
 */
public record ClientConfig(
    String baseUrl,
    String organizationId,
    List<String> scopes,
    int maxRetries,
    Duration timeout,
    AppInfo appInfo,
    HttpTransport transport,
    Clock clock,
    Sleeper sleeper,
    RequestLogger logger) {
  public ClientConfig {
    Objects.requireNonNull(baseUrl, "baseUrl");
    scopes = List.copyOf(scopes);
    Objects.requireNonNull(timeout, "timeout");
    Objects.requireNonNull(transport, "transport");
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(sleeper, "sleeper");
  }

  // ADR-0007: this header is the only thing the library says about its caller.
  String userAgent() {
    String agent = "teachmehipaa-java/" + SdkVersion.VERSION + " java/" + Runtime.version();
    if (appInfo == null) {
      return agent;
    }
    StringBuilder app = new StringBuilder(appInfo.name());
    if (appInfo.version() != null) {
      app.append('/').append(appInfo.version());
    }
    if (appInfo.url() != null) {
      app.append(" (").append(appInfo.url()).append(')');
    }
    return agent + " " + app;
  }
}
