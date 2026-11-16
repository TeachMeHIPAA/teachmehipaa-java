package com.teachmehipaa.generator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Renders {@code com.teachmehipaa.service}: one service per resource, reached from the client, with
 * overloads per method: path parameters, then the params (when the method has any), then the
 * request options. Each method has an {@code …Async} sibling with the same overloads, returning a
 * {@code CompletableFuture} of what the method returns.
 */
final class ServiceWriter {
  private static final String CORE = "com.teachmehipaa.core.";
  private static final String THROWS =
      "@throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails";
  private static final String FUTURE = "java.util.concurrent.CompletableFuture";

  private final Surface surface;

  ServiceWriter(Surface surface) {
    this.surface = surface;
  }

  List<JavaFile> files() {
    List<JavaFile> files = new ArrayList<>();
    Surface.walk(surface.roots.values(), node -> files.add(service(node)));
    return files;
  }

  static String resourceDoc(Surface.ResourceNode node) {
    String words = Naming.words(node.accessor());
    return node.organizationScoped() ? words + " in the client's Organization." : words + ".";
  }

  private JavaFile service(Surface.ResourceNode node) {
    boolean extended = HandWritten.extendedResources().contains(String.join(".", node.path()));
    String exposed = node.serviceClass();
    String name = extended ? exposed + "Base" : exposed;
    JavaFile file = new JavaFile(Surface.SERVICE_PACKAGE, name);
    String requester = file.use(CORE + "ApiRequester");
    String accessor =
        "client." + node.path().stream().map(part -> part + "()").collect(Collectors.joining("."));
    if (extended) {
      file.append(
          Javadoc.block(
              "",
              "The generated methods of {@code "
                  + accessor
                  + "}. The client returns the hand-written {@link "
                  + exposed
                  + "}, which adds to them."));
      file.append("public abstract sealed class " + name + " permits " + exposed + " {\n");
    } else {
      file.append(
          Javadoc.block("", resourceDoc(node) + " Reach it through {@code " + accessor + "}."));
      file.append("public final class " + name + " {\n");
    }
    file.append("  private final " + requester + " requester;\n\n");
    file.append("  /** Internal: the client creates services. */\n");
    file.append("  " + (extended ? "" : "public ") + name + "(" + requester + " requester) {\n");
    file.append("    this.requester = " + file.use("java.util.Objects"));
    file.append(".requireNonNull(requester, \"requester\");\n  }\n");
    for (Surface.ResourceNode child : node.children().values()) {
      file.append("\n").append(Javadoc.block("  ", resourceDoc(child)));
      file.append("  public " + child.serviceClass() + " " + child.accessor() + "() {\n");
      file.append("    return new " + child.serviceClass() + "(requester);\n  }\n");
    }
    for (Surface.MethodPlan plan : node.methods()) {
      Returns returns = returns(plan, file);
      method(file, plan, plan.javaName(), returns);
      method(file, plan, plan.javaName(), async(returns, file));
      if (plan.streamName() != null) {
        Returns stream = streamReturns(plan, file);
        method(file, plan, plan.streamName(), stream);
        method(file, plan, plan.streamName(), async(stream, file));
      }
      if (plan.ifModifiedName() != null) {
        ifModified(file, plan, false);
        ifModified(file, plan, true);
      }
    }
    return file.append("}\n");
  }

  /**
   * What a method returns and how the requester produces it.
   *
   * @param syncType for an {@code …Async} method, what its blocking twin returns; otherwise {@code
   *     null}
   */
  record Returns(String type, String call, String doc, String syncType) {
    Returns(String type, String call, String doc) {
      this(type, call, doc, null);
    }

    boolean isAsync() {
      return syncType != null;
    }
  }

  /** The suffix of every asynchronous sibling's name. */
  static final String ASYNC = "Async";

  /** The {@code …Async} form of {@code returns}: a future of the same type, from the async call. */
  static Returns async(Returns returns, JavaFile file) {
    return new Returns(
        file.use(FUTURE) + "<" + returns.type() + ">",
        returns.call().replaceFirst("\\(", ASYNC + "("),
        "a future of " + returns.doc(),
        returns.type());
  }

  static Returns returns(Surface.MethodPlan plan, JavaFile file) {
    Ir.Response response = plan.method().response();
    if (response instanceof Ir.ModelResponse model) {
      String type = model(model.type(), file);
      return new Returns(
          type, "request(request, " + type + ".class, options)", "the {@link " + type + "}");
    }
    if (response instanceof Ir.OperationResponse operation) {
      String special = HandWritten.operationModels().get(plan.method().operationId());
      String type =
          special == null
              ? model(operation.type(), file)
              : file.use(Surface.MODEL_PACKAGE + "." + special);
      return new Returns(
          type,
          "request(request, " + type + ".class, options)",
          "the {@link "
              + type
              + "}, which may finish in the background: {@code waitForCompletion()} polls until"
              + " it is done");
    }
    if (response instanceof Ir.WriteResultResponse writeResult) {
      String type = model(writeResult.type(), file);
      return new Returns(
          type,
          "requestWriteResult(request, " + type + ".class, options)",
          "the Learner; {@code isCreated()} is true when this call created it (201) and false when"
              + " it already existed (200)");
    }
    if (response instanceof Ir.PageResponse) {
      Ir.Pagination pagination = plan.method().pagination();
      String item = model(pagination.itemType(), file);
      return new Returns(
          file.use(CORE + "Page") + "<" + item + ">",
          "requestPage(request, "
              + file.use(CORE + "Pagination")
              + "."
              + pagination.style().coreFactory()
              + "("
              + ModelWriter.literal(pagination.cursorParam())
              + "), "
              + item
              + ".class, options)",
          "one page of {@link " + item + "}; {@code nextCursor()} is {@code null} on the last");
    }
    if (response instanceof Ir.EmptyResponse) {
      return new Returns(
          file.use(CORE + "EmptyResponse"),
          "requestEmpty(request, options)",
          "the response's metadata: the API answers 204 No Content");
    }
    return new Returns(
        file.use(CORE + "BinaryResponse"),
        "requestBinary(request, " + mediaType(plan) + ", options)",
        "the file's bytes, content type and filename");
  }

  // A sibling of each download: the same request, its body handed over unread.
  static Returns streamReturns(Surface.MethodPlan plan, JavaFile file) {
    return new Returns(
        file.use(CORE + "BinaryStreamResponse"),
        "requestBinaryStream(request, " + mediaType(plan) + ", options)",
        "the file as a stream, with its content type and filename; close it to release the"
            + " connection");
  }

  private static String mediaType(Surface.MethodPlan plan) {
    return ModelWriter.literal(((Ir.BinaryResponse) plan.method().response()).contentType());
  }

  private static String model(Ir.TypeRef ref, JavaFile file) {
    return file.use(Surface.MODEL_PACKAGE + "." + ((Ir.Ref) ref).name());
  }

  private void method(JavaFile file, Surface.MethodPlan plan, String syncName, Returns returns) {
    String javaName = returns.isAsync() ? syncName + ASYNC : syncName;
    Ir.Method method = plan.method();
    String options = file.use("com.teachmehipaa.RequestOptions");
    String params =
        plan.hasParams() ? file.use(Surface.PARAM_PACKAGE + "." + plan.paramsClass()) : null;
    List<String> pathArguments = plan.pathArguments();
    String summary =
        Javadoc.present(method.summary()) ? Javadoc.text(method.summary()) + "." : null;
    summary = summary == null ? null : summary.replaceAll("\\.\\.$", ".");

    String newParams = params == null ? null : params + ".builder().build()";

    // The short overloads: each forwards to the full one, which comes last.
    List<List<String>> overloads = overloads(plan, params, options);
    for (List<String> parameters : overloads.subList(0, overloads.size() - 1)) {
      List<String> forwarded = new ArrayList<>(pathArguments);
      if (params != null) {
        forwarded.add(parameters.contains(params + " params") ? "params" : newParams);
      }
      forwarded.add(parameters.contains(options + " options") ? "options" : options + ".none()");
      file.append("\n").append(Javadoc.block("  ", summary));
      file.append("  public " + returns.type() + " " + javaName + "(");
      file.append(String.join(", ", parameters) + ") {\n");
      file.append("    return " + javaName + "(" + String.join(", ", forwarded) + ");\n  }\n");
    }

    List<String> tags = new ArrayList<>();
    for (Ir.Param param : method.pathParams()) {
      String doc = Javadoc.present(param.description()) ? Javadoc.text(param.description()) : "";
      tags.add(("@param " + param.name() + " " + doc).trim());
    }
    if (params != null) {
      tags.add("@param params the call's parameters");
    }
    tags.add("@param options settings for this call only, such as a timeout or request id");
    tags.add("@return " + returns.doc());
    String description =
        Javadoc.present(method.description()) ? Javadoc.text(method.description()) : null;
    String retries =
        method.retrySafe()
            ? null
            : "Never retried: a lost response may hide a change that already happened.";
    List<String> paragraphs;
    if (returns.isAsync()) {
      paragraphs =
          nonNull(summary, asyncParagraph(syncName, overloads.get(overloads.size() - 1)), retries);
    } else {
      tags.add(THROWS);
      paragraphs = nonNull(summary, description, retries);
    }
    file.append("\n").append(Javadoc.block("  ", paragraphs, tags));
    file.append("  public " + returns.type() + " " + javaName + "(");
    file.append(String.join(", ", overloads.get(overloads.size() - 1)) + ") {\n");
    String objects = file.use("java.util.Objects");
    for (String argument : pathArguments) {
      file.append("    " + objects + ".requireNonNull(" + argument + ", \"" + argument + "\");\n");
    }
    if (params != null) {
      file.append("    " + objects + ".requireNonNull(params, \"params\");\n");
    }
    file.append("    " + objects + ".requireNonNull(options, \"options\");\n");
    appendRequest(file, plan, null);
    file.append("    return requester." + returns.call() + ";\n  }\n");
  }

  /**
   * The paragraph that ties an {@code …Async} method to its blocking twin.
   *
   * @param parameters the full overload's parameter declarations, such as {@code String certId}
   */
  static String asyncParagraph(String syncName, List<String> parameters) {
    String types =
        parameters.stream()
            .map(parameter -> parameter.substring(0, parameter.lastIndexOf(' ')))
            .collect(Collectors.joining(", "));
    return "The asynchronous form of {@link #"
        + syncName
        + "("
        + types
        + ")}: it sends the same request, with the same retries, without blocking the calling"
        + " thread. The future fails with the {@code TeachMeHipaaException} subclass that method"
        + " throws, never wrapped in a {@code CompletionException}. Cancelling it stops the call"
        + " and its retries.";
  }

  /**
   * Every overload of a method, as its parameter declarations, the full one last: path arguments
   * first, then the params (left out where none are required), then the request options.
   *
   * @param params the params class's name, or {@code null} when the method takes none
   * @param options the request options class's name
   */
  static List<List<String>> overloads(Surface.MethodPlan plan, String params, String options) {
    List<String> head = plan.pathArguments().stream().map(arg -> "String " + arg).toList();
    List<List<String>> tails = new ArrayList<>();
    if (params == null) {
      tails.add(List.of());
      tails.add(List.of(options + " options"));
    } else {
      if (!plan.paramsRequired()) {
        tails.add(List.of());
        tails.add(List.of(options + " options"));
      }
      tails.add(List.of(params + " params"));
      tails.add(List.of(params + " params", options + " options"));
    }
    return tails.stream()
        .map(
            tail -> {
              List<String> parameters = new ArrayList<>(head);
              parameters.addAll(tail);
              return List.copyOf(parameters);
            })
        .toList();
  }

  /** Writes {@code ApiRequest request = ...;} for the plan, optionally conditional. */
  private static void appendRequest(JavaFile file, Surface.MethodPlan plan, String ifNoneMatch) {
    Ir.Method method = plan.method();
    List<String> pathArguments = plan.pathArguments();
    String apiRequest = file.use(CORE + "ApiRequest");
    file.append("    " + apiRequest + " request =\n        " + apiRequest + ".builder(");
    file.append(ModelWriter.literal(method.httpMethod()) + ", ");
    file.append(ModelWriter.literal(plan.pathTemplate()) + ")\n");
    for (String argument : pathArguments) {
      file.append("            .pathParam(\"" + argument + "\", " + argument + ")\n");
    }
    for (Surface.ParamField field : plan.paramFields()) {
      String getter = "params." + Naming.getter(field.identifier(), false) + "()";
      if (field.location() == Surface.Location.QUERY) {
        file.append(
            "            .queryParam("
                + ModelWriter.literal(field.wireName())
                + ", "
                + getter
                + ")\n");
      } else if (field.location() == Surface.Location.IF_MATCH) {
        file.append("            .header(\"if-match\", " + getter + ")\n");
      }
    }
    if (ifNoneMatch != null) {
      file.append("            .header(\"if-none-match\", " + ifNoneMatch + ")\n");
    }
    if ("none".equals(method.security().kind())) {
      file.append("            .unauthenticated()\n");
    }
    if (method.retrySafe()) {
      file.append("            .retrySafe()\n");
    }
    if ("required".equals(method.idempotencyKey())) {
      file.append("            .idempotencyKeyRequired()\n");
    }
    Ir.RequestBody body = method.requestBody();
    if (body != null) {
      String encoder =
          switch (body.contentType()) {
            case "application/merge-patch+json" -> "mergePatchBody";
            case "multipart/form-data" -> "multipartBody";
            default -> "jsonBody";
          };
      file.append("            ." + encoder + "(params)\n");
      for (Ir.BinaryPart part : body.binaryParts()) {
        String getter =
            "params." + Naming.getter(Naming.identifier(part.name(), plan.where()), false) + "()";
        file.append("            .filePart(" + ModelWriter.literal(part.name()) + ", " + getter);
        file.append(", " + ModelWriter.literal(part.contentType()) + ")\n");
      }
      for (Surface.FixedValue fixed : plan.fixedValues()) {
        file.append("            .fixedBodyValue(" + file.use("java.util.List") + ".of(");
        file.append(
            fixed.path().stream().map(ModelWriter::literal).collect(Collectors.joining(", ")));
        file.append("), " + literal(fixed.value(), plan.where()) + ")\n");
      }
    }
    file.append("            .build();\n");
  }

  private void ifModified(JavaFile file, Surface.MethodPlan plan, boolean async) {
    Ir.Method method = plan.method();
    String syncName = plan.ifModifiedName();
    String name = async ? syncName + ASYNC : syncName;
    String type = model(((Ir.ModelResponse) method.response()).type(), file);
    String optional = file.use("java.util.Optional");
    String options = file.use("com.teachmehipaa.RequestOptions");
    String objects = file.use("java.util.Objects");
    List<String> pathArguments = plan.pathArguments();
    String head =
        pathArguments.stream().map(arg -> "String " + arg + ", ").collect(Collectors.joining());
    String forwarded = pathArguments.stream().map(arg -> arg + ", ").collect(Collectors.joining());
    String syncReturnType = optional + "<" + type + ">";
    String returnType = async ? file.use(FUTURE) + "<" + syncReturnType + ">" : syncReturnType;
    String what = Naming.words(type).toLowerCase(Locale.ROOT);
    String summary = "Retrieves the " + what + " unless the copy you hold is current.";
    String noEtag =
        "@throws IllegalArgumentException when {@code cached} carries no ETag; nothing is sent";

    file.append("\n").append(Javadoc.block("  ", summary));
    file.append("  public " + returnType + " " + name + "(" + head + "String etag) {\n");
    file.append("    return " + name + "(" + forwarded + "etag, " + options + ".none());\n  }\n");

    file.append("\n").append(Javadoc.block("  ", List.of(summary), List.of(noEtag)));
    file.append("  public " + returnType + " " + name + "(" + head + type + " cached) {\n");
    file.append("    return " + name + "(" + forwarded + "cached, " + options + ".none());\n  }\n");

    file.append("\n").append(Javadoc.block("  ", List.of(summary), List.of(noEtag)));
    file.append(
        "  public "
            + returnType
            + " "
            + name
            + "("
            + head
            + type
            + " cached, "
            + options
            + " options) {\n");
    file.append(
        "    return "
            + name
            + "("
            + forwarded
            + file.use(CORE + "Etags")
            + ".of(cached), options);\n  }\n");

    List<String> tags = new ArrayList<>();
    List<String> fullParameters = new ArrayList<>();
    for (Ir.Param param : method.pathParams()) {
      String doc = Javadoc.present(param.description()) ? Javadoc.text(param.description()) : "";
      tags.add(("@param " + param.name() + " " + doc).trim());
    }
    pathArguments.forEach(arg -> fullParameters.add("String " + arg));
    fullParameters.add("String etag");
    fullParameters.add(options + " options");
    tags.add("@param etag the ETag of the copy you hold, from {@code getLastResponse().etag()}");
    tags.add("@param options settings for this call only, such as a timeout or request id");
    String result =
        "the {@link "
            + type
            + "} when it changed, or empty when the API answers 304 Not"
            + " Modified and your copy is current";
    tags.add("@return " + (async ? "a future of " + result : result));
    if (!async) {
      tags.add(THROWS);
    }
    List<String> paragraphs = new ArrayList<>();
    paragraphs.add(
        "Retrieves the "
            + what
            + " only if it changed since you read the copy you hold: sends"
            + " {@code If-None-Match}, so an unchanged one costs no body.");
    if (async) {
      paragraphs.add(asyncParagraph(syncName, fullParameters));
    }
    file.append("\n").append(Javadoc.block("  ", paragraphs, tags));
    file.append(
        "  public "
            + returnType
            + " "
            + name
            + "("
            + head
            + "String etag, "
            + options
            + " options) {\n");
    for (String argument : pathArguments) {
      file.append("    " + objects + ".requireNonNull(" + argument + ", \"" + argument + "\");\n");
    }
    file.append("    " + objects + ".requireNonNull(etag, \"etag\");\n");
    file.append("    " + objects + ".requireNonNull(options, \"options\");\n");
    appendRequest(file, plan, "etag");
    file.append(
        "    return requester.requestIfModified"
            + (async ? ASYNC : "")
            + "(request, "
            + type
            + ".class, options);\n  }\n");
  }

  private static List<String> nonNull(String... paragraphs) {
    List<String> present = new ArrayList<>();
    for (String paragraph : paragraphs) {
      if (paragraph != null) {
        present.add(paragraph);
      }
    }
    return present;
  }

  private static String literal(JsonNode value, String where) {
    if (value.isBoolean()) {
      return String.valueOf(value.booleanValue());
    }
    if (value.isTextual()) {
      return ModelWriter.literal(value.textValue());
    }
    if (value.isIntegralNumber() && value.canConvertToLong()) {
      return value.longValue() + "L";
    }
    throw new GeneratorException(where + ": no literal for fixed value " + value);
  }
}
