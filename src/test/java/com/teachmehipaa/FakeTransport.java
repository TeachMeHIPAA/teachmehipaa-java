package com.teachmehipaa;

import com.teachmehipaa.core.HttpTransport;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.core.TransportResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A scripted fake of the TeachMeHIPAA API behind the transport seam. Replies are queued per route
 * ({@code "GET /v1/auth/introspection"}) and consumed in order; the last one repeats. Every request
 * is recorded for wire assertions.
 */
final class FakeTransport implements HttpTransport {
  private final List<TransportRequest> requests = new ArrayList<>();
  private final Map<String, Deque<Responder>> routes = new HashMap<>();
  private Responder otherwise;

  /** Computes a reply, or throws to simulate a request that got no response. */
  @FunctionalInterface
  interface Responder {
    Reply respond(TransportRequest request) throws IOException;
  }

  FakeTransport on(String route, Reply reply) {
    return on(route, request -> reply);
  }

  synchronized FakeTransport on(String route, Responder responder) {
    routes.computeIfAbsent(route, key -> new ArrayDeque<>()).add(responder);
    return this;
  }

  /** The next request to the route gets no response. */
  FakeTransport fail(String route, IOException error) {
    return on(
        route,
        request -> {
          throw error;
        });
  }

  /** Answers every request no route is scripted for. */
  synchronized FakeTransport otherwise(Responder responder) {
    this.otherwise = responder;
    return this;
  }

  synchronized FakeTransport replace(String route, Reply reply) {
    routes.remove(route);
    return on(route, reply);
  }

  @Override
  public TransportResponse send(TransportRequest request) throws IOException {
    Responder responder;
    synchronized (this) {
      requests.add(request);
      Deque<Responder> queue = routes.get(routeOf(request));
      if (queue == null || queue.isEmpty()) {
        if (otherwise == null) {
          throw new AssertionError("unscripted request " + request.method() + " " + request.uri());
        }
        responder = otherwise;
      } else {
        responder = queue.size() > 1 ? queue.poll() : queue.peek();
      }
    }
    // Outside the lock, so a slow responder doesn't serialize concurrent callers.
    return responder.respond(request).toResponse();
  }

  synchronized List<TransportRequest> requests() {
    return List.copyOf(requests);
  }

  synchronized List<TransportRequest> requestsTo(String route) {
    return requests.stream().filter(request -> routeOf(request).equals(route)).toList();
  }

  private static String routeOf(TransportRequest request) {
    return request.method() + " " + request.uri().getRawPath();
  }
}
