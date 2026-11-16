import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.Webhook;
import com.teachmehipaa.WebhookDelivery;
import com.teachmehipaa.exception.WebhookSignatureException;
import com.teachmehipaa.model.Event;
import com.teachmehipaa.model.EventLearnerCertified;
import com.teachmehipaa.model.Learner;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Receives and verifies webhook Deliveries with the JDK's built-in HTTP server, and looks up each
 * certified Learner in the Sandbox.
 *
 * <p>Run: {@code ./gradlew runExample -Pexample=WebhookServer}, then point a Webhook subscription
 * at {@code https://<your host>/webhooks/tmh}, for example through a tunnel. It listens on {@code
 * PORT} (3000 by default).
 */
public final class WebhookServer {
  public static void main(String[] args) throws IOException {
    String signingSecret = requireEnv("TMH_WEBHOOK_SECRET");
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(requireEnv("TMH_CLIENT_ID"))
            .clientSecret(requireEnv("TMH_CLIENT_SECRET"))
            .environment(Environment.SANDBOX)
            .build();
    // Slow work happens here, after the Delivery is answered, so TeachMeHIPAA never retries a
    // Delivery because a lookup was slow.
    ExecutorService work = Executors.newSingleThreadExecutor();

    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext(
        "/webhooks/tmh",
        exchange -> {
          try {
            if (!exchange.getRequestMethod().equals("POST")) {
              respond(exchange, 405, "");
              return;
            }
            // The signature covers the exact bytes and path sent, so read the body raw and pass
            // the path with its query string as it arrived.
            byte[] body = exchange.getRequestBody().readAllBytes();
            URI target = exchange.getRequestURI();
            String path =
                target.getRawQuery() == null
                    ? target.getRawPath()
                    : target.getRawPath() + "?" + target.getRawQuery();
            WebhookDelivery delivery;
            try {
              delivery =
                  Webhook.constructEvent(
                      body, exchange.getRequestHeaders()::getFirst, signingSecret, path);
            } catch (WebhookSignatureException rejected) {
              respond(exchange, 400, rejected.getCode());
              return;
            }
            respond(exchange, 200, "");
            work.submit(() -> handle(client, delivery));
          } finally {
            exchange.close();
          }
        });
    server.start();
    System.out.println("Listening on http://localhost:" + port + "/webhooks/tmh");
  }

  private static void handle(TeachMeHipaaClient client, WebhookDelivery delivery) {
    if (delivery instanceof WebhookDelivery.TestDeliveryReceived test) {
      System.out.println("Test delivery for " + test.testDelivery().getSubscriptionId());
      return;
    }
    Event event = ((WebhookDelivery.EventDelivery) delivery).event();
    System.out.println(event.getEventType() + " " + event.getEventId());
    if (event instanceof EventLearnerCertified certified) {
      Learner learner = client.learners().retrieve(certified.getData().getLearnerId());
      System.out.println(
          learner.getEmail()
              + " is certified until "
              + (learner.getCertificate() == null
                  ? "unknown"
                  : learner.getCertificate().getExpiresAt()));
    }
  }

  private static void respond(HttpExchange exchange, int status, String text) throws IOException {
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("content-type", "text/plain; charset=utf-8");
    exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }

  private static String requireEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      System.err.println("Set " + name + ".");
      System.exit(1);
    }
    return value;
  }

  private WebhookServer() {}
}
