package com.teachmehipaa.core;

import java.net.URI;
import java.util.Objects;

/** Internal: checks a base URL override, the same way wherever one is accepted. */
public final class BaseUrls {
  /**
   * Returns {@code baseUrl} without trailing slashes, so URLs compare and join consistently.
   *
   * @throws IllegalArgumentException when it is not an absolute http(s) URL
   */
  public static String normalize(String baseUrl) {
    Objects.requireNonNull(baseUrl, "baseUrl");
    URI uri = URI.create(baseUrl);
    if (!uri.isAbsolute() || uri.getHost() == null) {
      throw new IllegalArgumentException("baseUrl must be an absolute http(s) URL");
    }
    return baseUrl.replaceAll("/+$", "");
  }

  private BaseUrls() {}
}
