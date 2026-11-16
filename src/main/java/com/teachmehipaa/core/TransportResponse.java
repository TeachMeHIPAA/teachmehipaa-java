package com.teachmehipaa.core;

import java.net.http.HttpHeaders;
import java.util.Objects;

/**
 * One HTTP response as a transport received it.
 *
 * @param statusCode the HTTP status
 * @param headers the response headers, looked up case-insensitively
 * @param body the response body, empty when there was none
 */
public record TransportResponse(int statusCode, HttpHeaders headers, byte[] body) {
  public TransportResponse {
    Objects.requireNonNull(headers, "headers");
    body = body == null ? new byte[0] : body.clone();
  }

  /** Returns a copy of the body. */
  @Override
  public byte[] body() {
    return body.clone();
  }
}
