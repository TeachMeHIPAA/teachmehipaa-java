package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.NotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The snippets in {@code docs/reference/code-samples.json}, which the monorepo's API reference
 * shows for Java. Each one is compiled against the library alone (see {@link DocSampleTest}), then
 * run against a fake that answers most calls with 404: the snippet must build its params without
 * error and send the request its operation id names in the IR.
 */
class CodeSamplesTest extends DocSampleTest {
  private static final Path SAMPLES = Path.of("docs/reference/code-samples.json");
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
  private static final String SUBSCRIPTION_JSON =
      """
      {"subscriptionId": "whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B",
       "endpointUrl": "https://example.com/webhooks/teachmehipaa",
       "eventTypes": ["learner.certified"], "enabled": true, "description": null,
       "seatsLowThreshold": 5, "secretKeyId": "key_1", "createdAt": "2026-11-15T12:00:00Z",
       "rev": 3}
      """;

  /** Each operation's HTTP method and path, from the IR. */
  private final Map<String, Route> routes = routes();

  private List<Sample> parsedSamples;

  record Route(String method, Pattern path, boolean authenticated) {}

  @Override
  List<Path> docs() {
    return List.of(SAMPLES);
  }

  @Override
  List<Sample> samples() {
    if (parsedSamples == null) {
      parsedSamples = new ArrayList<>();
      json(SAMPLES)
          .path("samples")
          .properties()
          .forEach(
              entry -> {
                String code = entry.getValue().asText();
                String imports =
                    code.lines()
                        .filter(line -> line.startsWith("import "))
                        .collect(Collectors.joining("\n", "", "\n"));
                String body =
                    code.lines()
                        .filter(line -> !line.startsWith("import "))
                        .dropWhile(String::isBlank)
                        .collect(Collectors.joining("\n", "", "\n"));
                parsedSamples.add(new Sample(SAMPLES, entry.getKey(), imports, body));
              });
    }
    return parsedSamples;
  }

  @Override
  Map<String, Check> checks() {
    Map<String, Check> checks = new LinkedHashMap<>();
    routes
        .keySet()
        .forEach(operationId -> checks.put(operationId, () -> sendsItsRequest(operationId)));
    return checks;
  }

  @Test
  void isForJavaAtTheVendoredRelease() throws IOException {
    JsonNode file = json(SAMPLES);

    assertEquals("java", file.path("language").asText());
    assertEquals(
        Files.readString(Path.of("OPENAPI_VERSION")).trim(), file.path("release").asText());
    assertEquals(43, file.path("samples").size());
  }

  @Test
  void buildsTheClientAsTheReadmeQuickstartDoes() {
    for (Sample sample : samples()) {
      boolean credentials = sample.body().contains("System.getenv(\"TMH_CLIENT_ID\")");
      assertEquals(routes.get(sample.id()).authenticated(), credentials, sample.id());
      assertTrue(sample.body().contains(".environment(Environment.SANDBOX)"), sample.id());
    }
  }

  private void sendsItsRequest(String operationId) throws Exception {
    Route route = routes.get(operationId);
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")))
        // The updates read the object first, to send its ETag as If-Match.
        .on(
            "GET " + ORG + "/learners/sub_01JD80AB2C3D4E5F6G7H8J9K0M",
            Reply.json(LEARNER_JSON).withHeader("etag", "\"7\""))
        .on(
            "GET " + ORG + "/webhook-subscriptions/whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B",
            Reply.json(SUBSCRIPTION_JSON).withHeader("etag", "\"3\""))
        .otherwise(
            request ->
                Reply.problem(
                    404,
                    """
                    {"title": "Not found", "status": 404, "code": "not_found"}
                    """));
    // The import sample uploads roster.csv from the working directory.
    Path roster = Path.of("roster.csv");
    boolean madeRoster = !Files.exists(roster);
    if (madeRoster) {
      Files.writeString(roster, "email,name\r\ndana.whitfield@example.com,Dana Whitfield\r\n");
    }
    try {
      // The scripted reads above succeed; every other call gets the 404.
      run(operationId);
    } catch (NotFoundException answered) {
      // The request went out; the route check below says whether it was the right one.
    } finally {
      if (madeRoster) {
        Files.delete(roster);
      }
    }

    List<TransportRequest> sent =
        api.requests().stream().filter(request -> !routeOf(request).equals(TOKEN_ROUTE)).toList();
    // The Organization lookup, when there is one, comes before the call.
    TransportRequest last = sent.get(sent.size() - 1);
    assertEquals(route.method(), last.method(), operationId);
    assertTrue(
        route.path().matcher(last.uri().getRawPath()).matches(),
        operationId + " sent " + routeOf(last) + ", not " + route.path());
    assertEquals(route.authenticated(), !api.requestsTo(TOKEN_ROUTE).isEmpty(), operationId);
  }

  private static String routeOf(TransportRequest request) {
    return request.method() + " " + request.uri().getRawPath();
  }

  // The first method of each operation id, as the generator picks it.
  private static Map<String, Route> routes() {
    Map<String, Route> routes = new LinkedHashMap<>();
    for (JsonNode resource : json(Path.of("openapi/sdk-ir.json")).path("resources")) {
      for (JsonNode method : resource.path("methods")) {
        JsonNode path = method.path("path");
        Matcher placeholder = PLACEHOLDER.matcher(path.path("template").asText());
        StringBuilder regex = new StringBuilder("/v1");
        int end = 0;
        while (placeholder.find()) {
          regex.append(
              Pattern.quote(path.path("template").asText().substring(end, placeholder.start())));
          regex.append(placeholder.group(1).equals("orgId") ? "org_acme" : "[^/:]+");
          end = placeholder.end();
        }
        regex.append(Pattern.quote(path.path("template").asText().substring(end)));
        if (path.hasNonNull("customVerb")) {
          regex.append(Pattern.quote(":" + path.path("customVerb").asText()));
        }
        routes.putIfAbsent(
            method.path("operationId").asText(),
            new Route(
                method.path("httpMethod").asText(),
                Pattern.compile(regex.toString()),
                !"none".equals(method.path("security").path("kind").asText())));
      }
    }
    return routes;
  }

  private static JsonNode json(Path file) {
    try {
      return JSON.readTree(file.toFile());
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }
}
