package com.teachmehipaa.exception;

/** 404: no such object in this Organization. */
public class NotFoundException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public NotFoundException(String message, ErrorDetails details) {
    super(message, details);
  }
}
