package com.teachmehipaa.exception;

/** 422 {@code idempotency_key_reuse}: the Idempotency-Key was already used with another body. */
public class IdempotencyException extends InvalidRequestException {
  private static final long serialVersionUID = 1L;

  public IdempotencyException(String message, ErrorDetails details) {
    super(message, details);
  }
}
