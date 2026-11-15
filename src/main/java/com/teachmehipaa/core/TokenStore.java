package com.teachmehipaa.core;

/**
 * Where a Grant's Refresh token lives between processes, such as a database row or a secrets
 * manager. {@link PartnerGrantTokenProvider} reads and writes it.
 *
 * <p>Every exchange replaces the Refresh token, and presenting a replaced one revokes the Grant, so
 * {@link #save} must persist durably before it returns.
 */
public interface TokenStore {
  /** The Grant's current Refresh token, read once, at the first exchange. */
  String load();

  /**
   * Persists the Refresh token that replaces the current one. Throw if it could not be saved; the
   * provider then raises instead of using the new Access token.
   */
  void save(String refreshToken);
}
