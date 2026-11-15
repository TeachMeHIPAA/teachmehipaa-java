package com.teachmehipaa.exception;

import com.teachmehipaa.model.Operation;

/**
 * The thread was interrupted while {@code waitForCompletion} waited between polls. The thread keeps
 * its interrupt flag. {@link #getOperation()} is the last state seen; the Operation carries on, so
 * you can wait on it again.
 *
 * <p>Unchecked, like every exception the library raises (ADR-0008). It is its own type rather than
 * an {@link ApiConnectionException} because nothing failed on the network, and nothing should retry
 * it.
 */
public class OperationInterruptedException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  private final transient Operation operation;

  /**
   * @param details the request id and headers of the last response seen
   * @param operation the last state seen
   * @param cause the interrupt
   */
  public OperationInterruptedException(
      String message, ErrorDetails details, Operation operation, InterruptedException cause) {
    super(message, details, cause);
    this.operation = operation;
  }

  /** The last state seen, still not done. */
  public Operation getOperation() {
    return operation;
  }
}
