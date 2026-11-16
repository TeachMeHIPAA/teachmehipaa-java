package com.teachmehipaa.core;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * One HTTP request, fully built: a transport only has to send it.
 *
 * @param method the HTTP method, such as {@code GET} or {@code PATCH}
 * @param uri the absolute URI, query included
 * @param headers header values keyed by lower-case name
 * @param body the request body, or {@code null} when there is none
 * @param timeout how long to wait for the response
 */
public record TransportRequest(
    String method, URI uri, Map<String, String> headers, byte[] body, Duration timeout) {
  public TransportRequest {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(timeout, "timeout");
    headers = Map.copyOf(headers);
    body = body == null ? null : body.clone();
  }

  /** Returns a copy of the body, or {@code null} when there is none. */
  @Override
  public byte[] body() {
    return body == null ? null : body.clone();
  }
}
