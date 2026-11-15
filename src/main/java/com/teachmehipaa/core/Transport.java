package com.teachmehipaa.core;

import com.teachmehipaa.RequestLogEntry;
import java.io.IOException;
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
    if (config.logger() != null) {
      config
          .logger()
          .log(
              new RequestLogEntry(
                  request.method(),
                  request.uri().getRawPath(),
                  response.statusCode(),
                  Duration.between(startedAt, config.clock().instant()),
                  response.headers().firstValue("x-request-id").orElse(null)));
    }
    return response;
  }

  private Transport() {}
}
