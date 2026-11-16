package com.teachmehipaa.exception;

/**
 * The token endpoint refused to issue an Access token, with an OAuth error such as {@code
 * invalid_client}.
 */
public class OAuthException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public OAuthException(String message, ErrorDetails details) {
    super(message, details);
  }
}
