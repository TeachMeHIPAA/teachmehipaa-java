package com.teachmehipaa;

import com.teachmehipaa.core.HttpTransport;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.core.TransportResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A transport that only sends asynchronously, leaving each response for the test to deliver. Its
 * blocking {@code send} fails the test, so a call that reaches it was not asynchronous.
 */
final class PendingTransport implements HttpTransport {
  /** One request and the future the client waits on for its response. */
  record Exchange(TransportRequest request, CompletableFuture<TransportResponse> response) {
    String route() {
      return request.method() + " " + request.uri().getRawPath();
    }
  }

  private final List<Exchange> exchanges = new ArrayList<>();

  @Override
  public TransportResponse send(TransportRequest request) {
    throw new AssertionError("a blocking send: " + request.method() + " " + request.uri());
  }

  @Override
  public synchronized CompletableFuture<TransportResponse> sendAsync(TransportRequest request) {
    CompletableFuture<TransportResponse> response = new CompletableFuture<>();
    exchanges.add(new Exchange(request, response));
    return response;
  }

  synchronized List<Exchange> exchanges() {
    return List.copyOf(exchanges);
  }

  synchronized List<Exchange> to(String route) {
    return exchanges.stream().filter(exchange -> exchange.route().equals(route)).toList();
  }

  /** Answers the only unanswered request to {@code route} from a thread of its own. */
  void answer(String route, Reply reply) throws InterruptedException {
    List<Exchange> waiting = to(route).stream().filter(e -> !e.response().isDone()).toList();
    if (waiting.size() != 1) {
      throw new AssertionError(waiting.size() + " unanswered requests to " + route);
    }
    Thread responder =
        new Thread(() -> waiting.get(0).response().complete(reply.toResponse()), "responder");
    responder.start();
    responder.join();
  }
}
