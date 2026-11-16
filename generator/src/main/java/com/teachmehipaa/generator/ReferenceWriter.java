package com.teachmehipaa.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renders {@code docs/reference/resources.md}: every service and method of the client, with its
 * overloads, HTTP route, Scopes, retry, idempotency and conditional behaviour, as partner
 * developers read it. Signatures come from the same decisions {@link ServiceWriter} renders, so the
 * two can't drift.
 */
final class ReferenceWriter {
  static final String PATH = "docs/reference/resources.md";

  private static final String BANNER =
      "<!-- Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate."
          + " -->";
  private static final String JAVADOC = "https://teachmehipaa.github.io/teachmehipaa-java/";

  /** Public methods no IR method backs, by resource: what to read instead. */
  private static final Map<String, String> HAND_WRITTEN =
      Map.of(
          "events",
          "`events().tail(EventTailParams params)` is hand-written on top of `events().list`: it"
              + " iterates the Event log oldest first and keeps its position in `lastEventId()`."
              + " See [Pagination and the Event"
              + " log](../guides/pagination-and-events.md#tailing-the-event-log).");

  private final Surface surface;

  ReferenceWriter(Surface surface) {
    this.surface = surface;
  }

  String markdown() {
    List<Surface.ResourceNode> nodes = new ArrayList<>();
    Surface.walk(surface.roots.values(), nodes::add);
    int methodCount = nodes.stream().mapToInt(node -> node.methods().size()).sum();
    String servers =
        surface.ir.servers().stream()
            .map(
                server ->
                    (server.environment().equals("live") ? "Live" : "Sandbox")
                        + " is "
                        + code(server.url()))
            .collect(Collectors.joining("; "));

    List<String> blocks = new ArrayList<>();
    blocks.add(BANNER);
    blocks.add("# API resources");
    blocks.add(
        "Every service and method of `TeachMeHipaaClient`: "
            + nodes.size()
            + " services and "
            + methodCount
            + " API methods, generated from the Developer API release "
            + code(surface.ir.source().release())
            + ". For every class, field and builder, see the [Javadoc]("
            + JAVADOC
            + "); for how the pieces fit together, the [guides](../README.md).");
    blocks.add(
        "Reach a service from the client, such as `client.learners()`. Each method takes the"
            + " path ids first, then its `…Params` (built with `builder()`), then"
            + " [`RequestOptions`](../guides/errors-and-retries.md#request-options); the shorter"
            + " overloads leave out what is optional. Every returned object has"
            + " `getLastResponse()`. Paths are relative to the Environment's base URL ("
            + servers
            + "). The client fills in `{orgId}` from its [Organization"
            + " binding](../guides/authentication.md#the-organization-binding).");
    blocks.add("## Contents");
    blocks.add(
        nodes.stream()
            .map(
                node ->
                    "- ["
                        + code(accessor(node))
                        + "](#"
                        + anchor(accessor(node))
                        + "): "
                        + node.methods().stream()
                            .map(
                                plan ->
                                    "["
                                        + code(plan.javaName())
                                        + "](#"
                                        + anchor(accessor(node) + "." + plan.javaName())
                                        + ")")
                            .collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n")));
    blocks.add("## Scopes");
    blocks.add(
        "An Access token carries the Scopes its API client holds, or fewer if you pass `scopes` to"
            + " the client builder. A method that lists several Scopes needs any one of them. See"
            + " [Authentication](../guides/authentication.md#scopes).");
    blocks.add(
        table(
            "Scope",
            "Allows",
            surface.ir.auth().scopes().stream()
                .map(scope -> List.of(code(scope.name()), orEmpty(scope.description())))
                .toList()));
    for (Surface.ResourceNode node : nodes) {
      blocks.add("## " + code(accessor(node)));
      String handWritten = HAND_WRITTEN.get(String.join(".", node.path()));
      if (handWritten != null) {
        blocks.add(handWritten);
      }
      for (Surface.MethodPlan plan : node.methods()) {
        blocks.addAll(method(node, plan));
      }
    }
    return blocks.stream().filter(block -> !block.isEmpty()).collect(Collectors.joining("\n\n"))
        + "\n";
  }

  private List<String> method(Surface.ResourceNode node, Surface.MethodPlan plan) {
    Ir.Method method = plan.method();
    // A scratch file in the service package gives the same simple names the service uses.
    JavaFile scratch = new JavaFile(Surface.SERVICE_PACKAGE, "Reference");
    String options = scratch.use("com.teachmehipaa.RequestOptions");
    String params =
        plan.hasParams() ? scratch.use(Surface.PARAM_PACKAGE + "." + plan.paramsClass()) : null;
    String returns = ServiceWriter.returns(plan, scratch).type();

    List<String> signatures = new ArrayList<>();
    for (List<String> parameters : ServiceWriter.overloads(plan, params, options)) {
      signatures.add(signature(returns, plan.javaName(), parameters));
    }
    if (plan.streamName() != null) {
      String stream = ServiceWriter.streamReturns(plan, scratch).type();
      for (List<String> parameters : ServiceWriter.overloads(plan, params, options)) {
        signatures.add(signature(stream, plan.streamName(), parameters));
      }
    }
    if (plan.ifModifiedName() != null) {
      String model = returns;
      String optional = "Optional<" + model + ">";
      List<String> head = plan.pathArguments().stream().map(arg -> "String " + arg).toList();
      for (List<String> tail :
          List.of(
              List.of("String etag"),
              List.of(model + " cached"),
              List.of(model + " cached", options + " options"),
              List.of("String etag", options + " options"))) {
        List<String> parameters = new ArrayList<>(head);
        parameters.addAll(tail);
        signatures.add(signature(optional, plan.ifModifiedName(), parameters));
      }
    }

    List<List<String>> rows = new ArrayList<>();
    rows.add(List.of("HTTP", code(method.httpMethod() + " " + plan.pathTemplate())));
    rows.add(List.of("Scopes", scopes(method.security())));
    rows.add(
        List.of(
            "Params",
            params == null
                ? "None"
                : code(params) + (plan.paramsRequired() ? ", required" : ", optional")));
    rows.add(List.of("Returns", code(returns)));
    rows.add(
        List.of(
            "Retries",
            method.retrySafe()
                ? "Retried on network errors, 409 `conflict`, 429 and 5xx"
                : "Never retried: replaying it isn't safe"));
    rows.add(List.of("Idempotency", idempotency(method.idempotencyKey())));
    List<String> notes = notes(plan);
    if (!notes.isEmpty()) {
      rows.add(List.of("Notes", String.join(" ", notes)));
    }

    List<String> blocks = new ArrayList<>();
    blocks.add("### " + code(accessor(node) + "." + plan.javaName()));
    blocks.add(orEmpty(method.summary()));
    blocks.add(orEmpty(method.description()));
    blocks.add("```java\n" + String.join("\n", signatures) + "\n```");
    blocks.add(table("", "", rows));
    return blocks;
  }

  private static List<String> notes(Surface.MethodPlan plan) {
    Ir.Method method = plan.method();
    List<String> notes = new ArrayList<>();
    if ("required".equals(method.ifMatch())) {
      notes.add(
          "`ifMatch` is required: the ETag, or the object you read. `build()` throws without it,"
              + " and a stale ETag throws `PreconditionFailedException`.");
    } else if ("optional".equals(method.ifMatch())) {
      notes.add("Takes an optional `ifMatch`: the ETag, or the object you read.");
    }
    if (plan.mergePatch()) {
      notes.add("A merge patch: fields you don't set are left alone, and `clearX()` sends `null`.");
    }
    if (plan.ifModifiedName() != null) {
      notes.add(
          code(plan.ifModifiedName())
              + " sends `If-None-Match` and returns an empty `Optional` when your copy is still"
              + " current (304).");
    }
    if (plan.streamName() != null) {
      notes.add(
          code(plan.streamName())
              + " hands over the body unread; close it to release the connection.");
    }
    if (method.response() instanceof Ir.OperationResponse) {
      notes.add("Returns an Operation: `waitForCompletion()` polls until it is done.");
    }
    if (method.pagination() != null) {
      notes.add(
          "One page of up to "
              + method.pagination().maxLimit()
              + " items; `autoPagingIterable()` and `autoPagingStream()` fetch the rest.");
    }
    return notes;
  }

  private static String scopes(Ir.Security security) {
    if ("none".equals(security.kind())) {
      return "None: a public method, sent without an Access token";
    }
    List<List<String>> sets = security.anyOf() == null ? List.of(List.of()) : security.anyOf();
    if (sets.stream().anyMatch(List::isEmpty)) {
      return "Any Access token";
    }
    return sets.stream()
        .map(set -> set.stream().map(ReferenceWriter::code).collect(Collectors.joining(" and ")))
        .collect(Collectors.joining(" or "));
  }

  private static String idempotency(String idempotencyKey) {
    return switch (idempotencyKey) {
      case "required" ->
          "Sends a generated `Idempotency-Key`, the same on every retry; set `idempotencyKey` in"
              + " `RequestOptions` to choose your own";
      case "optional" -> "Sends an `Idempotency-Key` only when you set one in `RequestOptions`";
      default -> "None";
    };
  }

  private static String signature(String returns, String name, List<String> parameters) {
    return returns + " " + name + "(" + String.join(", ", parameters) + ")";
  }

  /** {@code learners()}, or {@code parent().child()} for a nested resource. */
  private static String accessor(Surface.ResourceNode node) {
    return node.path().stream().map(part -> part + "()").collect(Collectors.joining("."));
  }

  /** The anchor GitHub gives a heading: lower case, punctuation but hyphens dropped. */
  private static String anchor(String heading) {
    return heading.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9 _-]", "");
  }

  private static String table(String first, String second, List<List<String>> rows) {
    StringBuilder table = new StringBuilder();
    table.append("| ").append(first).append(" | ").append(second).append(" |\n");
    table.append("| --- | --- |");
    for (List<String> row : rows) {
      table.append("\n| ").append(cell(row.get(0))).append(" | ").append(cell(row.get(1)));
      table.append(" |");
    }
    return table.toString();
  }

  /** Table cells can't hold a raw pipe or a line break. */
  private static String cell(String text) {
    return text.replace("|", "\\|").replaceAll("\\s*\\n\\s*", " ");
  }

  private static String code(String text) {
    return "`" + text + "`";
  }

  private static String orEmpty(String text) {
    return text == null ? "" : text.strip();
  }
}
