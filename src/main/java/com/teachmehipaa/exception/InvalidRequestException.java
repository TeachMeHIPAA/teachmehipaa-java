package com.teachmehipaa.exception;

/** 400, 405, 406, 415 or 422: the request itself is wrong. */
public class InvalidRequestException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public InvalidRequestException(String message, ErrorDetails details) {
    super(message, details);
  }
}
