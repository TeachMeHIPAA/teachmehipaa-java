package com.teachmehipaa.generator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders {@code com.teachmehipaa.service}: one service per resource, reached from the client, with
 * overloads per method: path parameters, then the params (when the method has any), then the
 * request options.
 */
final class ServiceWriter {
  private static final String CORE = "com.teachmehipaa.core.";
  private static final String THROWS =
      "@throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails";

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
    String name = node.serviceClass();
    JavaFile file = new JavaFile(Surface.SERVICE_PACKAGE, name);
    String requester = file.use(CORE + "ApiRequester");
    String accessor =
        "client." + node.path().stream().map(part -> part + "()").collect(Collectors.joining("."));
    file.append(
        Javadoc.block("", resourceDoc(node) + " Reach it through {@code " + accessor + "}."));
    file.append("public final class " + name + " {\n");
    file.append("  private final " + requester + " requester;\n\n");
    file.append("  /** Internal: the client creates services. */\n");
    file.append("  public " + name + "(" + requester + " requester) {\n");
    file.append("    this.requester = " + file.use("java.util.Objects"));
    file.append(".requireNonNull(requester, \"requester\");\n  }\n");
    for (Surface.ResourceNode child : node.children().values()) {
      file.append("\n").append(Javadoc.block("  ", resourceDoc(child)));
      file.append("  public " + child.serviceClass() + " " + child.accessor() + "() {\n");
      file.append("    return new " + child.serviceClass() + "(requester);\n  }\n");
    }
    for (Surface.MethodPlan plan : node.methods()) {
      method(file, plan);
    }
    return file.append("}\n");
  }

  /** What a method returns and how the requester produces it. */
  private record Returns(String type, String call, String doc) {}

  private Returns returns(Surface.MethodPlan plan, JavaFile file) {
    Ir.Response response = plan.method().response();
    if (response instanceof Ir.ModelResponse model) {
      String type = model(model.type(), file);
      return new Returns(
          type, "request(request, " + type + ".class, options)", "the {@link " + type + "}");
    }
    if (response instanceof Ir.OperationResponse operation) {
      String type = model(operation.type(), file);
      return new Returns(
          type,
          "request(request, " + type + ".class, options)",
          "the {@link " + type + "}, which finishes in the background");
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
      String item = model(plan.method().pagination().itemType(), file);
      return new Returns(
          file.use(CORE + "Page") + "<" + item + ">",
          "requestPage(request, " + item + ".class, options)",
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
        "requestBinary(request, options)",
        "the file's bytes, content type and filename");
  }

  private static String model(Ir.TypeRef ref, JavaFile file) {
    return file.use(Surface.MODEL_PACKAGE + "." + ((Ir.Ref) ref).name());
  }

  private void method(JavaFile file, Surface.MethodPlan plan) {
    Ir.Method method = plan.method();
    Returns returns = returns(plan, file);
    String options = file.use("com.teachmehipaa.RequestOptions");
    String params =
        plan.hasParams() ? file.use(Surface.PARAM_PACKAGE + "." + plan.paramsClass()) : null;
    List<String> pathArguments = plan.pathArguments();
    String summary =
        Javadoc.present(method.summary()) ? Javadoc.text(method.summary()) + "." : null;
    summary = summary == null ? null : summary.replaceAll("\\.\\.$", ".");

    List<String> signatureHead = pathArguments.stream().map(arg -> "String " + arg).toList();
    String head = String.join(", ", pathArguments);
    String newParams = params == null ? null : params + ".builder().build()";

    // The short overloads: each forwards to the full one.
    List<List<String>> overloads = new ArrayList<>();
    if (params == null) {
      overloads.add(List.of());
    } else {
      if (!plan.paramsRequired()) {
        overloads.add(List.of());
        overloads.add(List.of(options + " options"));
      }
      overloads.add(List.of(params + " params"));
    }
    for (List<String> extra : overloads) {
      List<String> parameters = new ArrayList<>(signatureHead);
      parameters.addAll(extra);
      List<String> forwarded = new ArrayList<>(pathArguments);
      if (params != null) {
        forwarded.add(extra.stream().anyMatch(p -> p.endsWith(" params")) ? "params" : newParams);
      }
      forwarded.add(
          extra.stream().anyMatch(p -> p.endsWith(" options")) ? "options" : options + ".none()");
      file.append("\n").append(Javadoc.block("  ", summary));
      file.append("  public " + returns.type() + " " + plan.javaName() + "(");
      file.append(String.join(", ", parameters) + ") {\n");
      file.append(
          "    return " + plan.javaName() + "(" + String.join(", ", forwarded) + ");\n  }\n");
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
    tags.add(THROWS);
    String description =
        Javadoc.present(method.description()) ? Javadoc.text(method.description()) : null;
    String retries =
        method.retrySafe()
            ? null
            : "Never retried: a lost response may hide a change that already happened.";
    file.append("\n").append(Javadoc.block("  ", nonNull(summary, description, retries), tags));
    List<String> parameters = new ArrayList<>(signatureHead);
    if (params != null) {
      parameters.add(params + " params");
    }
    parameters.add(options + " options");
    file.append("  public " + returns.type() + " " + plan.javaName() + "(");
    file.append(String.join(", ", parameters) + ") {\n");
    String objects = file.use("java.util.Objects");
    for (String argument : pathArguments) {
      file.append("    " + objects + ".requireNonNull(" + argument + ", \"" + argument + "\");\n");
    }
    if (params != null) {
      file.append("    " + objects + ".requireNonNull(params, \"params\");\n");
    }
    file.append("    " + objects + ".requireNonNull(options, \"options\");\n");
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
    if (method.response() instanceof Ir.BinaryResponse binary) {
      file.append(
          "            .header(\"accept\", " + ModelWriter.literal(binary.contentType()) + ")\n");
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
    file.append("    return requester." + returns.call() + ";\n  }\n");
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
