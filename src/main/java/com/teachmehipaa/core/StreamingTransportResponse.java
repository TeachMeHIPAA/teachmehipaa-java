package com.teachmehipaa.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.util.Objects;

/**
 * One HTTP response whose body is read as it arrives. Closing it releases the connection.
 *
 * @param statusCode the HTTP status
 * @param headers the response headers, looked up case-insensitively
 * @param body the response body, unread
 */
public record StreamingTransportResponse(int statusCode, HttpHeaders headers, InputStream body)
    implements Closeable {
  public StreamingTransportResponse {
    Objects.requireNonNull(headers, "headers");
    Objects.requireNonNull(body, "body");
  }

  @Override
  public void close() throws IOException {
    body.close();
  }
}
