package com.teachmehipaa.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;

/** The default {@link HttpTransport}, built on {@code java.net.http.HttpClient}. */
public final class JdkHttpTransport implements HttpTransport {
  private final HttpClient httpClient;

  /** Uses a new {@link HttpClient} with default settings. */
  public JdkHttpTransport() {
    this(HttpClient.newHttpClient());
  }

  /** Uses the given {@link HttpClient}, for example one configured with a proxy. */
  public JdkHttpTransport(HttpClient httpClient) {
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
  }

  @Override
  public TransportResponse send(TransportRequest request) throws IOException {
    HttpResponse<byte[]> response = exchange(request, BodyHandlers.ofByteArray());
    return new TransportResponse(response.statusCode(), response.headers(), response.body());
  }

  @Override
  public StreamingTransportResponse sendStreaming(TransportRequest request) throws IOException {
    HttpResponse<InputStream> response = exchange(request, BodyHandlers.ofInputStream());
    return new StreamingTransportResponse(
        response.statusCode(), response.headers(), response.body());
  }

  private <T> HttpResponse<T> exchange(TransportRequest request, BodyHandler<T> handler)
      throws IOException {
    byte[] body = request.body();
    HttpRequest.Builder builder =
        HttpRequest.newBuilder(request.uri())
            .timeout(request.timeout())
            .method(
                request.method(),
                body == null ? BodyPublishers.noBody() : BodyPublishers.ofByteArray(body));
    request.headers().forEach(builder::header);
    try {
      return httpClient.send(builder.build(), handler);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      InterruptedIOException error = new InterruptedIOException("request interrupted");
      error.initCause(interrupted);
      throw error;
    }
  }
}
