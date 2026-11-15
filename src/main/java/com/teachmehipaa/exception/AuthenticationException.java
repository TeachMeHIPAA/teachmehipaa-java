package com.teachmehipaa.exception;

/** 401: the Access token was missing, expired or revoked. */
public class AuthenticationException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public AuthenticationException(String message, ErrorDetails details) {
    super(message, details);
  }
}
