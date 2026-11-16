package com.teachmehipaa.core;

import java.util.concurrent.CompletableFuture;

/**
 * Supplies the Access token sent with each request. Client credentials is the default adapter;
 * static tokens plug in here too.
 *
 * <p>Implementations must be thread-safe.
 */
public interface TokenProvider {
  /**
   * Returns a currently valid Access token, minting one if needed.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when no token could be obtained
   */
  String accessToken();

  /**
   * Returns a future of a currently valid Access token, for the {@code …Async} methods. It fails
   * with the exception {@link #accessToken()} would throw.
   *
   * <p>The default runs {@link #accessToken()} on a library-owned daemon thread, so the caller
   * never blocks on it. Override it when the provider can get a token without blocking.
   */
  default CompletableFuture<String> accessTokenAsync() {
    return Async.blocking(this::accessToken);
  }

  /**
   * Called after the API refused {@code accessToken} with 401. Forget it if it is still the cached
   * one, so the next {@link #accessToken()} mints afresh; leave a token that already replaced it
   * alone, so concurrent refusals mint only once.
   */
  void invalidate(String accessToken);
}
