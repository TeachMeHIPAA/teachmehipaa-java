package com.teachmehipaa.exception;

import com.teachmehipaa.model.Operation;

/**
 * An Operation was not done within {@code waitForCompletion}'s timeout. {@link #getOperation()} is
 * the last state seen; the Operation may still finish, so you can wait on it again.
 */
public class OperationTimeoutException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  private final transient Operation operation;

  /**
   * @param details the request id and headers of the last poll's response
   * @param operation the last state seen
   */
  public OperationTimeoutException(String message, ErrorDetails details, Operation operation) {
    super(message, details);
    this.operation = operation;
  }

  /** The last state seen, still not done. */
  public Operation getOperation() {
    return operation;
  }
}
