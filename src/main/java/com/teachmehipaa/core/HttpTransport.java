package com.teachmehipaa.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * The HTTP seam. The client sends every request, token mints included, through one of these. Supply
 * your own to add a proxy, mTLS or a test fake; the default is {@link JdkHttpTransport}.
 *
 * <p>Implementations must be thread-safe. The {@code …Async} methods of the client call {@link
 * #sendAsync} and {@link #sendStreamingAsync}; their defaults run the blocking methods on a
 * library-owned thread, so a transport that implements only {@link #send} serves both.
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

  /**
   * Sends a fully built request without blocking the calling thread. The future completes with the
   * response, whatever its status, or exceptionally with an {@link IOException} only when no
   * response arrived. The client cancels it when the call is cancelled.
   *
   * <p>The default runs {@link #send} on a library-owned daemon thread and interrupts that thread
   * on cancel. Override it with a non-blocking client, as {@link JdkHttpTransport} does.
   */
  default CompletableFuture<TransportResponse> sendAsync(TransportRequest request) {
    return Async.blocking(() -> send(request));
  }

  /**
   * As {@link #sendAsync}, completing as soon as the headers arrive with the body still to be read.
   * The default runs {@link #sendStreaming} on a library-owned daemon thread.
   */
  default CompletableFuture<StreamingTransportResponse> sendStreamingAsync(
      TransportRequest request) {
    return Async.blocking(() -> sendStreaming(request));
  }
}
