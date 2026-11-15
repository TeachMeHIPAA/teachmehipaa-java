package com.teachmehipaa.exception;

/**
 * The {@link com.teachmehipaa.core.TokenStore} had no Refresh token to load, or failed to save a
 * rotated one. In the second case the new Access token was not used, and the provider keeps the
 * rotated Refresh token in memory for its next exchange.
 */
public class TokenStoreException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  /**
   * @param cause what the store threw, or {@code null}
   */
  public TokenStoreException(String message, Throwable cause) {
    super(message, ErrorDetails.noResponse(), cause);
  }
}
