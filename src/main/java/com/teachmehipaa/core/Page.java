package com.teachmehipaa.core;

import java.util.List;
import java.util.Objects;

/**
 * One page of a list. {@link #nextCursor()} is {@code null} on the last page.
 *
 * @param <T> the listed objects
 */
public final class Page<T> {
  private final List<T> items;
  private final String nextCursor;
  private final LastResponse lastResponse;

  Page(List<T> items, String nextCursor, LastResponse lastResponse) {
    this.items = items == null ? List.of() : List.copyOf(items);
    this.nextCursor = nextCursor;
    this.lastResponse = Objects.requireNonNull(lastResponse, "lastResponse");
  }

  /** The objects on this page, in the API's order. */
  public List<T> items() {
    return items;
  }

  /** The cursor that fetches the next page, or {@code null} when this is the last one. */
  public String nextCursor() {
    return nextCursor;
  }

  /** The status, headers and request id of the response this page was read from. */
  public LastResponse getLastResponse() {
    return lastResponse;
  }
}
