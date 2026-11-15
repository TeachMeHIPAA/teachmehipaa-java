package com.teachmehipaa.core;

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
   * Called after the API refused {@code accessToken} with 401. Forget it if it is still the cached
   * one, so the next {@link #accessToken()} mints afresh; leave a token that already replaced it
   * alone, so concurrent refusals mint only once.
   */
  void invalidate(String accessToken);
}
