package com.teachmehipaa.exception;

/** A 5xx, an unexpected status, or a response the library could not understand. */
public class ApiException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  public ApiException(String message, ErrorDetails details) {
    super(message, details);
  }

  /**
   * @param cause why the body could not be read
   */
  public ApiException(String message, ErrorDetails details, Throwable cause) {
    super(message, details, cause);
  }
}
