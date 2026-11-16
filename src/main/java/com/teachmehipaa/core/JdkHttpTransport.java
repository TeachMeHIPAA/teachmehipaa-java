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
import java.util.concurrent.CompletableFuture;

/**
 * The default {@link HttpTransport}, built on {@code java.net.http.HttpClient}. The async methods
 * use {@link HttpClient#sendAsync}, so no thread waits on the network; their futures complete on
 * the {@code HttpClient}'s executor.
 */
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

  @Override
  public CompletableFuture<TransportResponse> sendAsync(TransportRequest request) {
    return Async.map(
        exchangeAsync(request, BodyHandlers.ofByteArray()),
        response ->
            new TransportResponse(response.statusCode(), response.headers(), response.body()));
  }

  @Override
  public CompletableFuture<StreamingTransportResponse> sendStreamingAsync(
      TransportRequest request) {
    return Async.map(
        exchangeAsync(request, BodyHandlers.ofInputStream()),
        response ->
            new StreamingTransportResponse(
                response.statusCode(), response.headers(), response.body()));
  }

  // HttpClient fails its future with the IOException inside a CompletionException; Async.map
  // unwraps it. Cancelling the result cancels the exchange, which aborts it on JDK 16 and later.
  private <T> CompletableFuture<HttpResponse<T>> exchangeAsync(
      TransportRequest request, BodyHandler<T> handler) {
    return Async.map(httpClient.sendAsync(httpRequest(request), handler), response -> response);
  }

  private static HttpRequest httpRequest(TransportRequest request) {
    byte[] body = request.body();
    HttpRequest.Builder builder =
        HttpRequest.newBuilder(request.uri())
            .timeout(request.timeout())
            .method(
                request.method(),
                body == null ? BodyPublishers.noBody() : BodyPublishers.ofByteArray(body));
    request.headers().forEach(builder::header);
    return builder.build();
  }

  private <T> HttpResponse<T> exchange(TransportRequest request, BodyHandler<T> handler)
      throws IOException {
    try {
      return httpClient.send(httpRequest(request), handler);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      InterruptedIOException error = new InterruptedIOException("request interrupted");
      error.initCause(interrupted);
      throw error;
    }
  }
}
