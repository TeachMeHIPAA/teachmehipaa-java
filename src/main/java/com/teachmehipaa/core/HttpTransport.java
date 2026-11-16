package com.teachmehipaa.core;

import java.io.ByteArrayInputStream;
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

  /**
   * Sends a fully built request and returns the response as soon as its headers arrive, whatever
   * its status, with the body still to be read. The default reads the whole body through {@link
   * #send}; override it to stream.
   *
   * @throws IOException only when no response arrived
   */
  default StreamingTransportResponse sendStreaming(TransportRequest request) throws IOException {
    TransportResponse response = send(request);
    return new StreamingTransportResponse(
        response.statusCode(), response.headers(), new ByteArrayInputStream(response.body()));
  }
}
