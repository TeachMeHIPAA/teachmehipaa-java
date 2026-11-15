package com.teachmehipaa.core;

import java.net.http.HttpHeaders;
import java.util.Objects;

/**
 * The HTTP response a returned object was read from.
 *
 * @param statusCode the HTTP status
 * @param headers the response headers, looked up case-insensitively
 * @param requestId the {@code x-request-id} to quote to TeachMeHIPAA support, or {@code null}
 * @param etag the {@code ETag} to send as {@code If-Match} on a conditional update, or {@code null}
 */
public record LastResponse(int statusCode, HttpHeaders headers, String requestId, String etag) {
  public LastResponse {
    Objects.requireNonNull(headers, "headers");
  }

  static LastResponse of(TransportResponse response) {
    return of(response.statusCode(), response.headers());
  }

  static LastResponse of(int statusCode, HttpHeaders headers) {
    return new LastResponse(
        statusCode,
        headers,
        headers.firstValue("x-request-id").orElse(null),
        headers.firstValue("etag").orElse(null));
  }
}
