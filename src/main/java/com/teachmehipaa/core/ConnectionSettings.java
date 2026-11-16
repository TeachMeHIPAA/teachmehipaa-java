package com.teachmehipaa.core;

import com.teachmehipaa.AppInfo;
import com.teachmehipaa.Environment;
import java.time.Duration;
import java.util.Objects;

/**
 * Internal: where and how to reach the API, as the client builder collects it. Each {@code with…}
 * method validates its value.
 *
 * @param environment which deployment to call
 * @param baseUrl overrides the Environment's base URL, or {@code null}
 * @param timeout how long to wait for each response
 * @param appInfo your software, as named in the {@code User-Agent}, or {@code null}
 * @param httpTransport replaces the HTTP layer, or {@code null} for a {@link JdkHttpTransport}
 */
public record ConnectionSettings(
    Environment environment,
    String baseUrl,
    Duration timeout,
    AppInfo appInfo,
    HttpTransport httpTransport) {
  /** How long to wait for a response unless told otherwise: 80 seconds, as in stripe-java. */
  public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(80);

  /** Live, its own base URL, the default timeout and the JDK's HTTP client. */
  public static final ConnectionSettings DEFAULTS =
      new ConnectionSettings(Environment.LIVE, null, DEFAULT_TIMEOUT, null, null);

  public ConnectionSettings {
    Objects.requireNonNull(environment, "environment");
    Objects.requireNonNull(timeout, "timeout");
  }

  public ConnectionSettings withEnvironment(Environment environment) {
    return new ConnectionSettings(
        Objects.requireNonNull(environment, "environment"),
        baseUrl,
        timeout,
        appInfo,
        httpTransport);
  }

  /**
   * @throws IllegalArgumentException when {@code baseUrl} is not an absolute http(s) URL
   */
  public ConnectionSettings withBaseUrl(String baseUrl) {
    return new ConnectionSettings(
        environment, BaseUrls.normalize(baseUrl), timeout, appInfo, httpTransport);
  }

  /**
   * @throws IllegalArgumentException when {@code timeout} is not positive
   */
  public ConnectionSettings withTimeout(Duration timeout) {
    Objects.requireNonNull(timeout, "timeout");
    if (timeout.isNegative() || timeout.isZero()) {
      throw new IllegalArgumentException("timeout must be positive");
    }
    return new ConnectionSettings(environment, baseUrl, timeout, appInfo, httpTransport);
  }

  public ConnectionSettings withAppInfo(AppInfo appInfo) {
    return new ConnectionSettings(
        environment, baseUrl, timeout, Objects.requireNonNull(appInfo, "appInfo"), httpTransport);
  }

  public ConnectionSettings withHttpTransport(HttpTransport httpTransport) {
    return new ConnectionSettings(
        environment,
        baseUrl,
        timeout,
        appInfo,
        Objects.requireNonNull(httpTransport, "httpTransport"));
  }

  /** The base URL requests go to: the override, or the Environment's own. */
  public String resolvedBaseUrl() {
    return baseUrl != null ? baseUrl : environment.baseUrl();
  }

  /** The HTTP layer requests go through: the replacement, or a new {@link JdkHttpTransport}. */
  public HttpTransport resolvedTransport() {
    return httpTransport != null ? httpTransport : new JdkHttpTransport();
  }
}
