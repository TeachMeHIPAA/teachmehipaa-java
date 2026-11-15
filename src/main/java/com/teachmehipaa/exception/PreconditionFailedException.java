package com.teachmehipaa.exception;

/** 412 for a stale {@code If-Match}, 428 for a missing one. */
public class PreconditionFailedException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public PreconditionFailedException(String message, ErrorDetails details) {
    super(message, details);
  }
}
