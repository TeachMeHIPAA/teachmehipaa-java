package com.teachmehipaa.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Renders {@code com.teachmehipaa.param}: an immutable {@code *Params} class with a builder per
 * method that takes arguments, and the nested types those bodies carry.
 */
final class ParamWriter {
  static final String IF_MATCH_DOC =
      "The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a"
          + " stale one with 412 and a missing one with 428.";

  private final Surface surface;

  ParamWriter(Surface surface) {
    this.surface = surface;
  }

  List<JavaFile> files() {
    List<JavaFile> files = new ArrayList<>();
    for (Surface.MethodPlan plan : surface.methods) {
      if (plan.hasParams()) {
        files.add(methodParams(plan));
      }
    }
    for (String name : surface.params) {
      Ir.IrType type = surface.types.get(name);
      if (type instanceof Ir.ObjectType object) {
        files.add(nested(object));
      } else {
        files.add(union((Ir.UnionType) type));
      }
    }
    return files;
  }

  private JavaFile methodParams(Surface.MethodPlan plan) {
    JavaFile file = new JavaFile(Surface.PARAM_PACKAGE, plan.paramsClass());
    String call =
        "client."
            + plan.resourcePath().stream().map(part -> part + "()").collect(Collectors.joining("."))
            + "."
            + plan.javaName()
            + "(…)";
    String doc = "Parameters for {@code " + call + "}. Immutable.";
    String patch =
        plan.mergePatch()
            ? "Sent as a merge patch: a field you don't set is left unchanged, and a {@code clear}"
                + " method sends {@code null} to clear one."
            : null;
    return builderClass(
        file, plan.paramsClass(), Arrays.asList(doc, patch), plan.paramFields(), null);
  }

  private JavaFile nested(Ir.ObjectType object) {
    String name = object.name();
    JavaFile file = new JavaFile(Surface.PARAM_PACKAGE, name);
    Map.Entry<String, String> tag = surface.tagOf(name);
    Set<String> omitted = new TreeSet<>(surface.omittedParamFields.getOrDefault(name, Set.of()));
    if (tag != null) {
      omitted.add(tag.getKey());
    }
    List<Surface.ParamField> fields = new ArrayList<>();
    for (Ir.Field field : object.fields()) {
      if (!omitted.contains(field.name())) {
        fields.add(
            new Surface.ParamField(
                field.name(),
                Naming.identifier(field.name(), name),
                field.type(),
                field.required(),
                field.nullable(),
                field.description(),
                Surface.Location.BODY));
      }
    }
    if (fields.stream().anyMatch(field -> field.identifier().equals("build"))) {
      throw new GeneratorException(name + ": a field named build clashes with the builder");
    }
    String doc =
        (Javadoc.present(object.description()) ? Javadoc.text(object.description()) + " " : "")
            + "Immutable.";
    return builderClass(file, name, List.of(doc), fields, tag);
  }

  private JavaFile union(Ir.UnionType union) {
    String name = union.name();
    JavaFile file = new JavaFile(Surface.PARAM_PACKAGE, name);
    List<String> variants = Surface.variants(union);
    String summary =
        Javadoc.present(union.description()) ? Javadoc.text(union.description()) : null;
    String choices =
        "One of "
            + variants.stream().map(v -> "{@link " + v + "}").collect(Collectors.joining(", "))
            + ".";
    file.append(Javadoc.block("", summary, choices));
    file.append("public sealed interface " + name + " permits ");
    return file.append(String.join(", ", new TreeSet<>(variants)) + " {}\n");
  }

  /** One setter's Java types: the stored (boxed) type and the setter's parameter type. */
  private record Types(String stored, String setter) {}

  private JavaFile builderClass(
      JavaFile file,
      String name,
      List<String> docs,
      List<Surface.ParamField> fields,
      Map.Entry<String, String> tag) {
    Set<String> unions = surface.paramUnionsOf.getOrDefault(name, Set.of());
    List<Types> types = new ArrayList<>();
    fields.forEach(field -> types.add(types(field.type(), file, name + "." + field.wireName())));
    List<Surface.ParamField> body =
        fields.stream()
            .filter(field -> field.location() == Surface.Location.BODY && !isBinary(field))
            .toList();
    String objects = file.use("java.util.Objects");

    file.append(Javadoc.block("", docs, List.of()));
    file.append("public final class " + name);
    if (!unions.isEmpty()) {
      file.append(" implements " + String.join(", ", unions));
    }
    file.append(" {\n");
    for (int i = 0; i < fields.size(); i++) {
      Surface.ParamField field = fields.get(i);
      file.append("  private final " + types.get(i).stored() + " " + field.identifier() + ";\n");
      if (field.nullable()) {
        file.append("  private final boolean " + field.identifier() + "Set;\n");
      }
    }
    file.append("\n  private " + name + "(Builder builder) {\n");
    for (Surface.ParamField field : fields) {
      file.append("    this." + field.identifier() + " = builder." + field.identifier() + ";\n");
      if (field.nullable()) {
        file.append(
            "    this." + field.identifier() + "Set = builder." + field.identifier() + "Set;\n");
      }
    }
    file.append("  }\n\n  public static Builder builder() {\n    return new Builder();\n  }\n");

    for (int i = 0; i < fields.size(); i++) {
      Surface.ParamField field = fields.get(i);
      file.append("\n").append(Javadoc.block("  ", doc(field)));
      file.append("  public " + types.get(i).stored() + " ");
      file.append(Naming.getter(field.identifier(), false) + "() {\n");
      file.append("    return " + field.identifier() + ";\n  }\n");
    }

    if (!body.isEmpty() || tag != null) {
      String map = file.use("java.util.Map");
      file.append(
          "\n  // The wire form: only fields that were set, so unset and cleared differ.\n");
      file.append("  @" + file.use("com.fasterxml.jackson.annotation.JsonValue") + "\n");
      file.append("  " + map + "<String, Object> toJson() {\n");
      file.append("    " + map + "<String, Object> json = new ");
      file.append(file.use("java.util.LinkedHashMap") + "<>();\n");
      if (tag != null) {
        file.append("    json.put(" + ModelWriter.literal(tag.getKey()) + ", ");
        file.append(ModelWriter.literal(tag.getValue()) + ");\n");
      }
      for (Surface.ParamField field : body) {
        String condition =
            field.nullable() ? field.identifier() + "Set" : field.identifier() + " != null";
        file.append("    if (" + condition + ") {\n      json.put(");
        file.append(
            ModelWriter.literal(field.wireName()) + ", " + field.identifier() + ");\n    }\n");
      }
      file.append("    return json;\n  }\n");
    }

    file.append("\n  public static final class Builder {\n");
    for (int i = 0; i < fields.size(); i++) {
      Surface.ParamField field = fields.get(i);
      file.append("    private " + types.get(i).stored() + " " + field.identifier() + ";\n");
      if (field.nullable()) {
        file.append("    private boolean " + field.identifier() + "Set;\n");
      }
    }
    file.append("\n    private Builder() {}\n");
    for (int i = 0; i < fields.size(); i++) {
      Surface.ParamField field = fields.get(i);
      Types type = types.get(i);
      String id = field.identifier();
      String value =
          !type.setter().equals(type.stored())
              ? id
              : type.stored().startsWith("List<")
                  ? file.use("java.util.List") + ".copyOf(" + id + ")"
                  : objects + ".requireNonNull(" + id + ", \"" + id + "\")";
      String doc = doc(field) + (field.required() ? " Required." : "");
      file.append("\n").append(Javadoc.block("    ", doc.trim()));
      file.append("    public Builder " + id + "(" + type.setter() + " " + id + ") {\n");
      file.append("      this." + id + " = " + value + ";\n");
      if (field.nullable()) {
        file.append("      this." + id + "Set = true;\n");
      }
      file.append("      return this;\n    }\n");
      if (field.nullable()) {
        file.append(
            "\n    /** Sends {@code null} for {@code " + field.wireName() + "}, clearing it. */\n");
        file.append("    public Builder clear" + Naming.pascal(id) + "() {\n");
        file.append("      this." + id + " = null;\n      this." + id + "Set = true;\n");
        file.append("      return this;\n    }\n");
      }
    }
    List<Surface.ParamField> required =
        fields.stream().filter(Surface.ParamField::required).toList();
    file.append("\n");
    if (!required.isEmpty()) {
      String names =
          required.stream()
              .map(field -> "{@code " + field.identifier() + "}")
              .collect(Collectors.joining(", "));
      file.append(
          Javadoc.block(
              "    ",
              List.of(),
              List.of("@throws IllegalStateException when " + names + " is not set")));
    }
    file.append("    public " + name + " build() {\n");
    for (Surface.ParamField field : required) {
      file.append("      if (" + field.identifier() + " == null) {\n");
      file.append("        throw new IllegalStateException(\"" + field.identifier());
      file.append(" is required\");\n      }\n");
    }
    file.append("      return new " + name + "(this);\n    }\n  }\n}\n");
    return file;
  }

  private static String doc(Surface.ParamField field) {
    if (field.location() == Surface.Location.IF_MATCH) {
      return IF_MATCH_DOC;
    }
    return Javadoc.present(field.description()) ? Javadoc.text(field.description()) : "";
  }

  private static boolean isBinary(Surface.ParamField field) {
    return field.type() instanceof Ir.Primitive primitive && primitive.type().equals("binary");
  }

  private Types types(Ir.TypeRef ref, JavaFile file, String where) {
    if (ref instanceof Ir.Primitive scalar) {
      return switch (scalar.type()) {
        case "string" -> new Types("String", "String");
        case "integer" -> new Types("Long", "long");
        case "number" -> new Types("Double", "double");
        case "boolean" -> new Types("Boolean", "boolean");
        case "dateTime" -> same(file.use("java.time.OffsetDateTime"));
        case "binary" -> same(file.use("com.teachmehipaa.FileUpload"));
        default -> throw new GeneratorException(where + ": no param type for " + scalar.type());
      };
    }
    if (ref instanceof Ir.Ref named) {
      String owner = surface.isEnum(named.name()) ? Surface.MODEL_PACKAGE : Surface.PARAM_PACKAGE;
      return same(file.use(owner + "." + named.name()));
    }
    if (ref instanceof Ir.ArrayOf array) {
      return same(
          file.use("java.util.List") + "<" + types(array.items(), file, where).stored() + ">");
    }
    throw new GeneratorException(where + ": maps in requests have no rule");
  }

  private static Types same(String type) {
    return new Types(type, type);
  }
}
