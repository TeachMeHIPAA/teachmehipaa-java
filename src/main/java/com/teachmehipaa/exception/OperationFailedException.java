package com.teachmehipaa.exception;

import com.teachmehipaa.model.Operation;

/**
 * An Operation finished with an error. The problem's fields are on the exception, and {@link
 * #getOperation()} is the failed Operation. {@link #getRawBody()} is {@code null}: the problem
 * arrived inside the Operation, not as a response of its own.
 */
public class OperationFailedException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  private final transient Operation operation;

  /**
   * @param details the Operation's problem, with the request id and headers of the response that
   *     carried it
   * @param operation the failed Operation
   */
  public OperationFailedException(String message, ErrorDetails details, Operation operation) {
    super(message, details);
    this.operation = operation;
  }

  /** The failed Operation. */
  public Operation getOperation() {
    return operation;
  }
}
