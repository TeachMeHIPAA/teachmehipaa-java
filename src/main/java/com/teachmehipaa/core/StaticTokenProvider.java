package com.teachmehipaa.core;

import java.util.concurrent.CompletableFuture;

/**
 * Sends an Access token you obtained yourself, as is. It is never renewed and never touches the
 * token endpoint: once the token expires or is revoked, calls raise {@code
 * AuthenticationException}.
 */
public final class StaticTokenProvider implements TokenProvider {
  private final String accessToken;

  public StaticTokenProvider(String accessToken) {
    this.accessToken = Arguments.requireNonBlank(accessToken, "accessToken");
  }

  @Override
  public String accessToken() {
    return accessToken;
  }

  @Override
  public CompletableFuture<String> accessTokenAsync() {
    return CompletableFuture.completedFuture(accessToken);
  }

  /** Nothing to drop: this provider can't get another token. */
  @Override
  public void invalidate(String accessToken) {}

  @Override
  public String toString() {
    return "StaticTokenProvider[...]";
  }
}
