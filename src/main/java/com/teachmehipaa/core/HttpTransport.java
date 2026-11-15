package com.teachmehipaa.core;

import java.io.IOException;

/**
 * The HTTP seam. The client sends every request, token mints included, through one of these. Supply
 * your own to add a proxy, mTLS or a test fake; the default is {@link JdkHttpTransport}.
 *
 * <p>Implementations must be thread-safe.
 */
@FunctionalInterface
public interface HttpTransport {
  /**
   * Sends a fully built request and returns the response, whatever its status.
   *
   * @throws IOException only when no response arrived
   */
  TransportResponse send(TransportRequest request) throws IOException;
}
