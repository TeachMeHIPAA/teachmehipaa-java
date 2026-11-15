package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Mints Access tokens with the OAuth client-credentials grant, authenticating with the API client's
 * id and secret. It caches the token, mints a new one 60 seconds before the old one expires, and
 * makes concurrent callers share a single mint. A mint that fails on the network or with a 5xx is
 * retried like any request, since each one mints a fresh token.
 */
public final class ClientCredentialsTokenProvider implements TokenProvider {
  private static final Duration REMINT_MARGIN = Duration.ofSeconds(60);

  private final ReentrantLock mintLock = new ReentrantLock();
  private volatile CachedToken current;
  private final String basicCredentials;
  private final byte[] form;
  private final ClientConfig config;

  public ClientCredentialsTokenProvider(String clientId, String clientSecret, ClientConfig config) {
    Objects.requireNonNull(clientId, "clientId");
    Objects.requireNonNull(clientSecret, "clientSecret");
    this.config = Objects.requireNonNull(config, "config");
    // RFC 6749 §2.3.1: each half is form-encoded before base64.
    this.basicCredentials =
        Base64.getEncoder()
            .encodeToString(
                (formEncode(clientId) + ":" + formEncode(clientSecret))
                    .getBytes(StandardCharsets.UTF_8));
    String body = "grant_type=client_credentials";
    // Leaving scope out grants every Scope the API client holds.
    if (!config.scopes().isEmpty()) {
      body += "&scope=" + formEncode(String.join(" ", config.scopes()));
    }
    this.form = body.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String accessToken() {
    CachedToken token = current;
    if (isFresh(token)) {
      return token.value();
    }
    mintLock.lock();
    try {
      // Whoever held the lock before us may already have minted.
      token = current;
      if (!isFresh(token)) {
        token = mint();
        current = token;
      }
      return token.value();
    } finally {
      mintLock.unlock();
    }
  }

  @Override
  public void invalidate(String accessToken) {
    mintLock.lock();
    try {
      CachedToken token = current;
      if (token != null && token.value().equals(accessToken)) {
        current = null;
      }
    } finally {
      mintLock.unlock();
    }
  }

  private boolean isFresh(CachedToken token) {
    return token != null
        && config.clock().instant().isBefore(token.expiresAt().minus(REMINT_MARGIN));
  }

  private CachedToken mint() {
    for (int retryNumber = 1; ; retryNumber++) {
      try {
        return mintOnce();
      } catch (TeachMeHipaaException failure) {
        if (retryNumber > config.maxRetries() || !isNetworkOrServerFailure(failure)) {
          throw failure;
        }
        Retries.sleep(
            config.sleeper(),
            Retries.delay(failure, retryNumber, config.clock().instant()),
            failure);
      }
    }
  }

  private CachedToken mintOnce() {
    TransportRequest request =
        new TransportRequest(
            "POST",
            URI.create(config.baseUrl() + "/auth/token"),
            Map.of(
                "accept",
                "application/json",
                "authorization",
                "Basic " + basicCredentials,
                "content-type",
                "application/x-www-form-urlencoded",
                "user-agent",
                config.userAgent()),
            form,
            config.timeout());
    // Expiry counts from when the request left, so a slow response can't stretch the token past
    // its real lifetime.
    Instant requestedAt = config.clock().instant();
    TransportResponse response = Transport.send(config, request);
    JsonNode token = tokenBody(response);
    if (token == null) {
      throw Errors.fromOAuthError(response, config.clock().instant());
    }
    return new CachedToken(
        token.get("access_token").asText(),
        requestedAt.plusSeconds(token.get("expires_in").asLong()));
  }

  // A refusal such as invalid_client would only be refused again.
  private static boolean isNetworkOrServerFailure(TeachMeHipaaException failure) {
    return failure.getStatusCode() == null || failure.getStatusCode() >= 500;
  }

  private static JsonNode tokenBody(TransportResponse response) {
    if (response.statusCode() / 100 != 2 || !Errors.isJson(response)) {
      return null;
    }
    try {
      JsonNode body = Json.MAPPER.readTree(response.body());
      boolean complete =
          body.path("access_token").isTextual() && body.path("expires_in").isNumber();
      return complete ? body : null;
    } catch (IOException notJson) {
      return null;
    }
  }

  private static String formEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private record CachedToken(String value, Instant expiresAt) {}
}
