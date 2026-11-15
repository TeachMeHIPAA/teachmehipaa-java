package com.teachmehipaa.exception;

/** No response arrived: the connection failed or the body was cut off. */
public class ApiConnectionException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  /**
   * @param cause the I/O failure
   */
  public ApiConnectionException(String message, Throwable cause) {
    super(message, ErrorDetails.noResponse(), cause);
  }
}
