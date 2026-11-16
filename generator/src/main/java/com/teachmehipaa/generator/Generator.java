package com.teachmehipaa.generator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.ImportOrderer;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Turns {@code openapi/sdk-ir.json} into the model, param and service packages, the client's
 * service accessors and the resource reference in {@code docs/reference/}. It reads nothing else,
 * and the same IR always gives byte-identical output.
 */
public final class Generator {
  /**
   * The packages the generator owns outright: anything in them but {@link HandWritten#files()} is
   * deleted on a run.
   */
  public static final List<String> OWNED_DIRECTORIES =
      List.of(
          "src/main/java/com/teachmehipaa/model",
          "src/main/java/com/teachmehipaa/param",
          "src/main/java/com/teachmehipaa/service");

  /** Hand-written apart from the section between the markers, which the generator owns. */
  public static final String CLIENT_FILE = "src/main/java/com/teachmehipaa/TeachMeHipaaClient.java";

  /** The resource reference for partner developers, generated whole. */
  public static final String REFERENCE_FILE = ReferenceWriter.PATH;

  static final String BEGIN =
      "  // The beginning of the section generated from openapi/sdk-ir.json.";
  static final String END = "  // The end of the section generated from openapi/sdk-ir.json.";

  private static final String IR_FILE = "openapi/sdk-ir.json";
  private static final String SCHEMA_FILE = "openapi/sdk-ir.schema.json";
  private static final ObjectMapper JSON =
      JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

  /**
   * Every generated file, by path relative to {@code root}: the Java formatted with
   * google-java-format, and the resource reference.
   *
   * @throws GeneratorException when the IR is invalid or has a shape with no rule here
   */
  public static Map<String, String> render(Path root) {
    Surface surface = new Surface(load(root));
    Set<String> operationIds =
        surface.methods.stream()
            .filter(plan -> plan.method().response() instanceof Ir.OperationResponse)
            .map(plan -> plan.method().operationId())
            .collect(Collectors.toSet());
    for (String operationId : HandWritten.operationModels().keySet()) {
      if (!operationIds.contains(operationId)) {
        throw new GeneratorException(
            "HandWritten names " + operationId + ", which returns no Operation");
      }
    }
    List<JavaFile> files = new ArrayList<>();
    files.addAll(new ModelWriter(surface).files());
    files.addAll(new ParamWriter(surface).files());
    files.addAll(new ServiceWriter(surface).files());
    Map<String, String> rendered = new TreeMap<>();
    for (JavaFile file : files) {
      if (HandWritten.files().contains(file.path())) {
        throw new GeneratorException(file.path() + " is hand-written; the IR must not replace it");
      }
      if (rendered.put(file.path(), format(file.path(), file.source())) != null) {
        throw new GeneratorException(file.path() + " is generated twice");
      }
    }
    rendered.put(
        CLIENT_FILE, format(CLIENT_FILE, client(surface, read(root.resolve(CLIENT_FILE)))));
    rendered.put(REFERENCE_FILE, new ReferenceWriter(surface).markdown());
    return rendered;
  }

  private static Ir load(Path root) {
    JsonNode ir;
    JsonNode schema;
    try {
      ir = JSON.readTree(root.resolve(IR_FILE).toFile());
      schema = JSON.readTree(root.resolve(SCHEMA_FILE).toFile());
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
    Set<ValidationMessage> errors =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
            .getSchema(schema)
            .validate(ir);
    if (!errors.isEmpty()) {
      throw new GeneratorException(
          IR_FILE
              + " does not match "
              + SCHEMA_FILE
              + ":\n"
              + errors.stream()
                  .map(ValidationMessage::getMessage)
                  .sorted()
                  .collect(Collectors.joining("\n")));
    }
    try {
      return JSON.treeToValue(ir, Ir.class);
    } catch (IOException unreadable) {
      throw new GeneratorException(IR_FILE + ": " + unreadable.getMessage());
    }
  }

  /** The client source with the generated section and its imports replaced. */
  private static String client(Surface surface, String source) {
    int begin = source.indexOf(BEGIN);
    int end = source.indexOf(END);
    if (begin < 0 || end < begin) {
      throw new GeneratorException(CLIENT_FILE + " lacks the generated-section markers");
    }
    StringBuilder section = new StringBuilder(BEGIN).append("\n");
    List<String> imports = new ArrayList<>();
    for (Surface.ResourceNode node : surface.roots.values()) {
      imports.add("import " + Surface.SERVICE_PACKAGE + "." + node.serviceClass() + ";");
      section.append("\n").append(Javadoc.block("  ", ServiceWriter.resourceDoc(node)));
      section.append("  public " + node.serviceClass() + " " + node.accessor() + "() {\n");
      section.append("    return new " + node.serviceClass() + "(requester);\n  }\n");
    }
    String withSection = source.substring(0, begin) + section + "\n" + source.substring(end);
    String withoutOldImports =
        withSection.replaceAll(
            "(?m)^import " + Surface.SERVICE_PACKAGE.replace(".", "\\.") + "\\.\\w+;\n", "");
    int firstImport = withoutOldImports.indexOf("\nimport ") + 1;
    if (firstImport == 0) {
      throw new GeneratorException(CLIENT_FILE + " has no imports to add to");
    }
    return withoutOldImports.substring(0, firstImport)
        + String.join("\n", imports)
        + "\n"
        + withoutOldImports.substring(firstImport);
  }

  // Mirrors Spotless's googleJavaFormat and removeUnusedImports steps, so spotlessCheck passes.
  private static String format(String path, String source) {
    try {
      String imports =
          RemoveUnusedImports.removeUnusedImports(
              ImportOrderer.reorderImports(source, JavaFormatterOptions.Style.GOOGLE));
      return new Formatter().formatSource(imports);
    } catch (FormatterException invalid) {
      throw new GeneratorException(path + ": generated invalid Java: " + invalid.getMessage());
    }
  }

  static String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  private Generator() {}
}
