package com.teachmehipaa.core;

/** An object returned directly by an API call, carrying the response it was read from. */
public abstract class ApiResource extends ApiModel {
  private volatile LastResponse lastResponse;

  protected ApiResource() {}

  /**
   * The status, headers, request id and ETag of the response this object was read from, or {@code
   * null} when it did not come from an API call.
   */
  public LastResponse getLastResponse() {
    return lastResponse;
  }

  // Not named like a setter, so Jackson never treats it as a JSON property.
  void attachLastResponse(LastResponse response) {
    this.lastResponse = response;
  }
}
