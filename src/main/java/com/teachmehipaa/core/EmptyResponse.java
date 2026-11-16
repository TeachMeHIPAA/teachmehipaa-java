package com.teachmehipaa.core;

import java.util.Objects;

/** The result of a call whose success has no body, such as a DELETE answered with 204. */
public final class EmptyResponse {
  private final LastResponse lastResponse;

  EmptyResponse(LastResponse lastResponse) {
    this.lastResponse = Objects.requireNonNull(lastResponse, "lastResponse");
  }

  /** The status, headers and request id of the response. */
  public LastResponse getLastResponse() {
    return lastResponse;
  }
}
