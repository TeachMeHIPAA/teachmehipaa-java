package com.teachmehipaa.core;

import java.io.IOException;

final class Transport {
  static TransportResponse send(HttpTransport transport, TransportRequest request) {
    try {
      return transport.send(request);
    } catch (IOException noResponse) {
      throw Errors.noResponse(request, noResponse);
    }
  }

  private Transport() {}
}
