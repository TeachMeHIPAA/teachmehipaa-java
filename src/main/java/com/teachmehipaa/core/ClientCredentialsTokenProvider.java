package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.core.AccessTokenCache.CachedToken;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Mints Access tokens with the OAuth client-credentials grant, authenticating with the API client's
 * id and secret. It caches the token, mints a new one 60 seconds before the old one expires, and
 * makes concurrent callers share a single mint, async callers included. A mint that fails on the
 * network or with a 5xx is retried like any request, since each one mints a fresh token.
 */
public final class ClientCredentialsTokenProvider implements TokenProvider {
  private final AccessTokenCache cache;
  private final String authorization;
  private final String form;
  private final ClientConfig config;

  public ClientCredentialsTokenProvider(String clientId, String clientSecret, ClientConfig config) {
    Objects.requireNonNull(clientId, "clientId");
    Objects.requireNonNull(clientSecret, "clientSecret");
    this.config = Objects.requireNonNull(config, "config");
    this.authorization = TokenEndpoint.basicAuthorization(clientId, clientSecret);
    String body = "grant_type=client_credentials";
    // Leaving scope out grants every Scope the API client holds.
    if (!config.scopes().isEmpty()) {
      body += "&scope=" + TokenEndpoint.formEncode(String.join(" ", config.scopes()));
    }
    this.form = body;
    this.cache = new AccessTokenCache(config.clock(), this::mint, this::mintAsync);
  }

  @Override
  public String accessToken() {
    return cache.accessToken();
  }

  /** Mints without blocking; concurrent callers with a cold cache share one mint. */
  @Override
  public CompletableFuture<String> accessTokenAsync() {
    return cache.accessTokenAsync();
  }

  @Override
  public void invalidate(String accessToken) {
    cache.invalidate(accessToken);
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

  // The same schedule as mint(), waited on the async sleeper.
  private CompletableFuture<CachedToken> mintAsync() {
    Async.Loop<CachedToken> loop = new Async.Loop<>();
    mintAttempt(loop, 1);
    return loop.result();
  }

  private void mintAttempt(Async.Loop<CachedToken> loop, int retryNumber) {
    TransportRequest request = TokenEndpoint.request(config, authorization, form);
    Instant requestedAt = config.clock().instant();
    loop.await(
        Async.map(Transport.sendAsync(config, request), response -> token(response, requestedAt)),
        loop::complete,
        failure -> {
          if (!(failure instanceof TeachMeHipaaException retryable)
              || retryNumber > config.maxRetries()
              || !isNetworkOrServerFailure(retryable)) {
            loop.fail(failure);
            return;
          }
          loop.await(
              config
                  .asyncSleeper()
                  .sleep(Retries.delay(retryable, retryNumber, config.clock().instant())),
              elapsed -> mintAttempt(loop, retryNumber + 1),
              stopped -> loop.fail(retryable));
        });
  }

  private CachedToken mintOnce() {
    TransportRequest request = TokenEndpoint.request(config, authorization, form);
    // Expiry counts from when the request left, so a slow response can't stretch the token past
    // its real lifetime.
    Instant requestedAt = config.clock().instant();
    return token(Transport.send(config, request), requestedAt);
  }

  private CachedToken token(TransportResponse response, Instant requestedAt) {
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
    if (!Errors.isSuccess(response.statusCode()) || !Errors.isJson(response)) {
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
}
