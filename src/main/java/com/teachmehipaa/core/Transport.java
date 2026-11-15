package com.teachmehipaa.core;

import com.teachmehipaa.RequestLogEntry;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.time.Duration;
import java.time.Instant;

final class Transport {
  // Every request, token mints included, passes here, so logging sits where nothing above it can
  // leak headers or bodies into a log.
  static TransportResponse send(ClientConfig config, TransportRequest request) {
    Instant startedAt = config.clock().instant();
    TransportResponse response;
    try {
      response = config.transport().send(request);
    } catch (IOException noResponse) {
      throw Errors.noResponse(request, noResponse);
    }
    log(config, request, startedAt, response.statusCode(), response.headers());
    return response;
  }

  /** As {@link #send}, with the body left unread: logged once the headers arrive. */
  static StreamingTransportResponse sendStreaming(ClientConfig config, TransportRequest request) {
    Instant startedAt = config.clock().instant();
    StreamingTransportResponse response;
    try {
      response = config.transport().sendStreaming(request);
    } catch (IOException noResponse) {
      throw Errors.noResponse(request, noResponse);
    }
    log(config, request, startedAt, response.statusCode(), response.headers());
    return response;
  }

  private static void log(
      ClientConfig config,
      TransportRequest request,
      Instant startedAt,
      int statusCode,
      HttpHeaders headers) {
    if (config.logger() != null) {
      config
          .logger()
          .log(
              new RequestLogEntry(
                  request.method(),
                  request.uri().getRawPath(),
                  statusCode,
                  Duration.between(startedAt, config.clock().instant()),
                  headers.firstValue("x-request-id").orElse(null)));
    }
  }

  private Transport() {}
}
