package com.teachmehipaa.exception;

/** 409: the object's state forbids this, such as {@code seat_unavailable}. */
public class ConflictException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public ConflictException(String message, ErrorDetails details) {
    super(message, details);
  }
}
