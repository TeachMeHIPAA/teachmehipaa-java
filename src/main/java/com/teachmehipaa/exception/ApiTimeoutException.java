package com.teachmehipaa.exception;

/** No response arrived within the timeout. */
public class ApiTimeoutException extends ApiConnectionException {
  private static final long serialVersionUID = 1L;

  /**
   * @param cause the timeout
   */
  public ApiTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
