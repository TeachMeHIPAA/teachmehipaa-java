package com.teachmehipaa.exception;

/** 403: the Access token lacks a Scope, or the Organization may not do this. */
public class PermissionDeniedException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public PermissionDeniedException(String message, ErrorDetails details) {
    super(message, details);
  }
}
