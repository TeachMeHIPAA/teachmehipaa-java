package com.teachmehipaa.core;

import com.teachmehipaa.RequestLogEntry;
import com.teachmehipaa.RequestLogEntry.ErrorKind;
import com.teachmehipaa.exception.ApiTimeoutException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

final class Transport {
  private static final String REQUEST_ID_HEADER = "x-request-id";

  // Every request, token mints included, passes here, so logging sits where nothing above it can
  // leak headers or bodies into a log.
  static TransportResponse send(ClientConfig config, TransportRequest request) {
    Instant startedAt = config.clock().instant();
    TransportResponse response;
    try {
      response = config.transport().send(request);
    } catch (IOException noResponse) {
      throw noResponse(config, request, startedAt, noResponse);
    }
    logResponse(config, request, startedAt, response.statusCode(), response.headers());
    return response;
  }

  /** As {@link #send}, with the body left unread: logged once the headers arrive. */
  static StreamingTransportResponse sendStreaming(ClientConfig config, TransportRequest request) {
    Instant startedAt = config.clock().instant();
    StreamingTransportResponse response;
    try {
      response = config.transport().sendStreaming(request);
    } catch (IOException noResponse) {
      throw noResponse(config, request, startedAt, noResponse);
    }
    logResponse(config, request, startedAt, response.statusCode(), response.headers());
    return response;
  }

  /**
   * As {@link #send}, without blocking: the transport's {@code sendAsync}. Each attempt is logged
   * once, as in the blocking path. A cancelled attempt is logged as a connection failure, like an
   * interrupted blocking send.
   */
  static CompletableFuture<TransportResponse> sendAsync(
      ClientConfig config, TransportRequest request) {
    return logged(
        config,
        request,
        config.transport()::sendAsync,
        TransportResponse::statusCode,
        TransportResponse::headers);
  }

  /** As {@link #sendAsync}, with the body left unread: logged once the headers arrive. */
  static CompletableFuture<StreamingTransportResponse> sendStreamingAsync(
      ClientConfig config, TransportRequest request) {
    return logged(
        config,
        request,
        config.transport()::sendStreamingAsync,
        StreamingTransportResponse::statusCode,
        StreamingTransportResponse::headers);
  }

  private static <R> CompletableFuture<R> logged(
      ClientConfig config,
      TransportRequest request,
      Function<TransportRequest, CompletableFuture<R>> sendAsync,
      Function<R, Integer> statusCode,
      Function<R, HttpHeaders> headers) {
    Instant startedAt = config.clock().instant();
    CompletableFuture<R> sent = Async.start(() -> sendAsync.apply(request));
    CompletableFuture<R> result = new CompletableFuture<>();
    sent.whenComplete(
        (response, failure) -> {
          if (failure == null) {
            logResponse(
                config, request, startedAt, statusCode.apply(response), headers.apply(response));
            result.complete(response);
            return;
          }
          Throwable cause = Async.unwrap(failure);
          if (cause instanceof CancellationException) {
            log(
                config,
                request,
                startedAt,
                null,
                request.headers().get(REQUEST_ID_HEADER),
                ErrorKind.CONNECTION);
            result.completeExceptionally(cause);
          } else if (cause instanceof IOException noResponse) {
            result.completeExceptionally(noResponse(config, request, startedAt, noResponse));
          } else {
            result.completeExceptionally(cause);
          }
        });
    Async.cancelWith(result, sent);
    return result;
  }

  // Logged before it propagates: a request that never got an answer is the one an operator most
  // needs to see.
  private static TeachMeHipaaException noResponse(
      ClientConfig config, TransportRequest request, Instant startedAt, IOException cause) {
    TeachMeHipaaException error = Errors.noResponse(request, cause);
    log(
        config,
        request,
        startedAt,
        null,
        request.headers().get(REQUEST_ID_HEADER),
        error instanceof ApiTimeoutException ? ErrorKind.TIMEOUT : ErrorKind.CONNECTION);
    return error;
  }

  private static void logResponse(
      ClientConfig config,
      TransportRequest request,
      Instant startedAt,
      int statusCode,
      HttpHeaders headers) {
    log(
        config,
        request,
        startedAt,
        statusCode,
        headers.firstValue(REQUEST_ID_HEADER).orElse(null),
        null);
  }

  private static void log(
      ClientConfig config,
      TransportRequest request,
      Instant startedAt,
      Integer statusCode,
      String requestId,
      ErrorKind errorKind) {
    if (config.logger() != null) {
      config
          .logger()
          .log(
              new RequestLogEntry(
                  request.method(),
                  request.uri().getRawPath(),
                  statusCode,
                  Duration.between(startedAt, config.clock().instant()),
                  requestId,
                  errorKind));
    }
  }

  private Transport() {}
}
