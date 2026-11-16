package com.teachmehipaa.core;

import java.util.Objects;

/** Reads the ETag a conditional request sends. Internal: generated params and services call it. */
public final class Etags {
  /**
   * The ETag {@code object} was read with.
   *
   * @throws IllegalArgumentException when its response carried none, so nothing is sent
   */
  public static String of(ApiResource object) {
    Objects.requireNonNull(object, "object");
    LastResponse response = object.getLastResponse();
    if (response == null || response.etag() == null) {
      throw new IllegalArgumentException(
          "The "
              + object.getClass().getSimpleName()
              + " has no ETag: it was not read from an API response that carried one");
    }
    return response.etag();
  }

  private Etags() {}
}
