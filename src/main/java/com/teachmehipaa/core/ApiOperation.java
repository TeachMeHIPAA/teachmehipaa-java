package com.teachmehipaa.core;

/**
 * An Operation returned by an API call, which keeps the client's requester so it can poll for its
 * final state. Internal: {@code com.teachmehipaa.model.Operation} is the public type.
 */
public abstract class ApiOperation extends ApiResource {
  // Not a JSON property and not part of the Operation's state: two reads of one Operation are the
  // same Operation whichever client read them.
  private volatile ApiRequester requester;

  protected ApiOperation() {}

  /**
   * The requester of the client that returned this Operation.
   *
   * @throws IllegalStateException when the Operation did not come from an API call
   */
  protected final ApiRequester requester() {
    ApiRequester bound = requester;
    if (bound == null) {
      throw new IllegalStateException(
          "This Operation was not returned by an API call, so it has no client to poll with");
    }
    return bound;
  }

  // Not named like a setter, so Jackson never treats it as a JSON property.
  void attachRequester(ApiRequester requester) {
    this.requester = requester;
  }
}
