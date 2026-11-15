package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.CLIENT_ID;
import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.teachmehipaa.core.JdkHttpTransport;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.core.TransportResponse;
import com.teachmehipaa.model.Learner;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The default adapter against a real local HTTP server, to catch quirks a fake can't. */
class JdkHttpTransportTest {
  private record Received(
      String method, String path, Map<String, List<String>> headers, String body) {}

  private final List<Received> received = new CopyOnWriteArrayList<>();
  private HttpServer server;
  private String origin;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/", this::handle);
    server.start();
    origin = "http://127.0.0.1:" + server.getAddress().getPort();
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  private void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getRawPath();
    received.add(
        new Received(
            exchange.getRequestMethod(),
            path,
            Map.copyOf(exchange.getRequestHeaders()),
            new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
    String body;
    int status = 200;
    if (path.equals("/v1/auth/token")) {
      body = tokenJson("local.token");
    } else if (path.startsWith("/v1/organizations/")) {
      body = LEARNER_JSON;
      exchange.getResponseHeaders().add("ETag", "\"local\"");
    } else {
      status = 412;
      body = "{\"title\": \"Precondition Failed\"}";
    }
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.getResponseHeaders().add("X-Request-Id", "req_local");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }

  @Test
  void sendsMethodHeadersAndBodyAndReturnsErrorStatusesAsResponses() throws IOException {
    TransportResponse response =
        new JdkHttpTransport()
            .send(
                new TransportRequest(
                    "PATCH",
                    URI.create(origin + "/v1/anything"),
                    Map.of("content-type", "application/merge-patch+json", "if-match", "\"7\""),
                    "{\"name\":\"Ana\"}".getBytes(StandardCharsets.UTF_8),
                    Duration.ofSeconds(5)));

    assertEquals(412, response.statusCode());
    assertEquals(Optional.of("req_local"), response.headers().firstValue("x-request-id"));
    assertArrayEquals(
        "{\"title\": \"Precondition Failed\"}".getBytes(StandardCharsets.UTF_8), response.body());
    Received request = received.get(0);
    assertEquals("PATCH", request.method());
    assertEquals(List.of("application/merge-patch+json"), request.headers().get("Content-type"));
    assertEquals(List.of("\"7\""), request.headers().get("If-match"));
    assertEquals("{\"name\":\"Ana\"}", request.body());
  }

  @Test
  void aClientWithNoTransportGivenUsesTheJdkAdapter() {
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .baseUrl(origin + "/v1")
            .organizationId("org_acme")
            .build();

    Learner learner = client.learners().retrieve(LEARNER_ID);

    assertEquals(LEARNER_ID, learner.getLearnerId());
    assertEquals("\"local\"", learner.getLastResponse().etag());
    assertEquals(
        List.of("POST /v1/auth/token", "GET /v1/organizations/org_acme/learners/" + LEARNER_ID),
        received.stream().map(request -> request.method() + " " + request.path()).toList());
    assertEquals(List.of("Bearer local.token"), received.get(1).headers().get("Authorization"));
  }

  @Test
  void throwsWhenNoResponseArrives() {
    server.stop(0);

    assertThrows(
        IOException.class,
        () ->
            new JdkHttpTransport()
                .send(
                    new TransportRequest(
                        "GET",
                        URI.create(origin + "/v1/x"),
                        Map.of(),
                        null,
                        Duration.ofSeconds(5))));
  }
}
