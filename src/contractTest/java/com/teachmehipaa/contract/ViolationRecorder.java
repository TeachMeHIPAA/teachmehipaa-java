package com.teachmehipaa.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.HttpTransport;
import com.teachmehipaa.core.JdkHttpTransport;
import com.teachmehipaa.core.StreamingTransportResponse;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.core.TransportResponse;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The real transport, plus the request violations Prism names in each response's {@code
 * sl-violations} header, as {@code METHOD url: location message}. Violations in Prism's own
 * generated responses are left out: they come from its example generator, not from anything the SDK
 * sent.
 */
final class ViolationRecorder implements HttpTransport {
  private static final ObjectMapper JSON = new ObjectMapper();

  private final HttpTransport delegate = new JdkHttpTransport();
  private final List<String> found = new CopyOnWriteArrayList<>();

  @Override
  public TransportResponse send(TransportRequest request) throws IOException {
    TransportResponse response = delegate.send(request);
    record(request, response.headers());
    return response;
  }

  @Override
  public StreamingTransportResponse sendStreaming(TransportRequest request) throws IOException {
    StreamingTransportResponse response = delegate.sendStreaming(request);
    record(request, response.headers());
    return response;
  }

  @Override
  public CompletableFuture<TransportResponse> sendAsync(TransportRequest request) {
    return delegate
        .sendAsync(request)
        .thenApply(
            response -> {
              recordUnchecked(request, response.headers());
              return response;
            });
  }

  @Override
  public CompletableFuture<StreamingTransportResponse> sendStreamingAsync(
      TransportRequest request) {
    return delegate
        .sendStreamingAsync(request)
        .thenApply(
            response -> {
              recordUnchecked(request, response.headers());
              return response;
            });
  }

  List<String> found() {
    return List.copyOf(found);
  }

  void clear() {
    found.clear();
  }

  private void recordUnchecked(TransportRequest request, HttpHeaders headers) {
    try {
      record(request, headers);
    } catch (IOException unreadable) {
      throw new java.io.UncheckedIOException(unreadable);
    }
  }

  private void record(TransportRequest request, HttpHeaders headers) throws IOException {
    String header = headers.firstValue("sl-violations").orElse(null);
    if (header == null) {
      return;
    }
    for (JsonNode violation : JSON.readTree(header)) {
      JsonNode location = violation.path("location");
      if (location.path(0).asText().equals("request")) {
        found.add(
            request.method()
                + " "
                + request.uri()
                + ": "
                + StreamSupport.stream(location.spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.joining("."))
                + " "
                + violation.path("message").asText());
      }
    }
  }
}
