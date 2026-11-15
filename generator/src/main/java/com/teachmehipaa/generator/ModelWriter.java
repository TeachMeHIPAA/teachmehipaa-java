package com.teachmehipaa.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Renders {@code com.teachmehipaa.model}: tolerant immutable models, open enums and unions that
 * never fail on a variant they don't know (ADR-0006).
 */
final class ModelWriter {
  private static final String CORE = "com.teachmehipaa.core.";
  private static final Set<String> INHERITED_GETTERS =
      Set.of("getClass", "getExtraFields", "getLastResponse");

  private final Surface surface;

  ModelWriter(Surface surface) {
    this.surface = surface;
  }

  List<JavaFile> files() {
    List<JavaFile> files = new ArrayList<>();
    for (String name : surface.models) {
      Ir.IrType type = surface.types.get(name);
      if (type instanceof Ir.ObjectType object) {
        String doc =
            Javadoc.present(object.description()) ? Javadoc.text(object.description()) : null;
        files.add(object(object.name(), doc, object.fields()));
      } else if (type instanceof Ir.EnumType enumType) {
        files.add(openEnum(enumType));
      } else if (type instanceof Ir.UnionType union) {
        files.add(union(union));
        files.add(deserializer(union));
      }
    }
    for (Map.Entry<String, String> fallback : surface.fallbacks.entrySet()) {
      if (surface.unknownVariants.contains(fallback.getValue())) {
        files.add(unknownVariant((Ir.UnionType) surface.types.get(fallback.getKey())));
      }
    }
    return files;
  }

  // --- objects ---------------------------------------------------------------------------------

  /** {@code doc} is the class comment, already in Javadoc form, or {@code null}. */
  private JavaFile object(String name, String doc, List<Ir.Field> fields) {
    JavaFile file = new JavaFile(Surface.MODEL_PACKAGE, name);
    boolean resource = surface.resources.contains(name);
    String base = file.use(CORE + (resource ? "ApiResource" : "ApiModel"));
    Set<String> unions = surface.modelUnionsOf.getOrDefault(name, Set.of());

    file.append(Javadoc.block("", doc));
    if (!unions.isEmpty()) {
      file.append(variantAnnotation(file));
    }
    file.append("public final class ").append(name).append(" extends ").append(base);
    if (!unions.isEmpty()) {
      file.append(" implements ").append(String.join(", ", unions));
    }
    file.append(" {\n");

    List<Member> members = members(name, fields, file);
    members.forEach(
        member -> file.append("  private final " + member.type + " " + member.identifier + ";\n"));
    file.append("\n  @").append(file.use("com.fasterxml.jackson.annotation.JsonCreator"));
    file.append("\n  ").append(name).append("(");
    String property = file.use("com.fasterxml.jackson.annotation.JsonProperty");
    List<String> parameters = new ArrayList<>();
    members.forEach(
        member ->
            parameters.add(
                "@"
                    + property
                    + "(\""
                    + member.wireName
                    + "\") "
                    + member.type
                    + " "
                    + member.identifier));
    file.append(String.join(", ", parameters)).append(") {\n");
    members.forEach(
        member -> {
          String value =
              member.collection ? "immutable(" + member.identifier + ")" : member.identifier;
          file.append("    this." + member.identifier + " = " + value + ";\n");
        });
    file.append("  }\n");
    members.forEach(member -> file.append("\n").append(getter(member)));
    return file.append("}\n");
  }

  /** One property of a model: its field, creator parameter and getter. */
  private record Member(
      String wireName,
      String identifier,
      String type,
      String getter,
      boolean collection,
      String description) {}

  private List<Member> members(String owner, List<Ir.Field> fields, JavaFile file) {
    List<Member> members = new ArrayList<>();
    Set<String> identifiers = new HashSet<>();
    Set<String> getters = new HashSet<>(INHERITED_GETTERS);
    for (Ir.Field field : fields) {
      String where = owner + "." + field.name();
      String identifier = Naming.identifier(field.name(), where);
      boolean primitive = field.required() && !field.nullable();
      String type = type(field.type(), primitive, file, where);
      String getter = Naming.getter(identifier, type.equals("boolean"));
      if (!identifiers.add(identifier) || !getters.add(getter)) {
        throw new GeneratorException(where + ": " + identifier + " clashes with another member");
      }
      String description =
          Javadoc.present(field.description()) ? Javadoc.text(field.description()) : "";
      if (!primitive) {
        description = (description + " May be {@code null}.").trim();
      }
      members.add(
          new Member(
              field.name(),
              identifier,
              type,
              getter,
              field.type() instanceof Ir.ArrayOf || field.type() instanceof Ir.MapOf,
              description));
    }
    return members;
  }

  private static String getter(Member member) {
    return Javadoc.block("  ", member.description)
        + "  public "
        + member.type
        + " "
        + member.getter
        + "() {\n    return "
        + member.identifier
        + ";\n  }\n";
  }

  private String type(Ir.TypeRef ref, boolean primitive, JavaFile file, String where) {
    if (ref instanceof Ir.Primitive scalar) {
      return switch (scalar.type()) {
        case "string" -> "String";
        case "integer" -> primitive ? "long" : "Long";
        case "number" -> primitive ? "double" : "Double";
        case "boolean" -> primitive ? "boolean" : "Boolean";
        case "dateTime" -> file.use("java.time.OffsetDateTime");
        case "json" -> file.use("com.fasterxml.jackson.databind.JsonNode");
        default -> throw new GeneratorException(where + ": no model type for " + scalar.type());
      };
    }
    if (ref instanceof Ir.Ref named) {
      return file.use(Surface.MODEL_PACKAGE + "." + named.name());
    }
    if (ref instanceof Ir.ArrayOf array) {
      return file.use("java.util.List") + "<" + type(array.items(), false, file, where) + ">";
    }
    Ir.MapOf map = (Ir.MapOf) ref;
    return file.use("java.util.Map") + "<String, " + type(map.values(), false, file, where) + ">";
  }

  // A variant inherits its union's deserializer otherwise, and would read itself forever.
  private static String variantAnnotation(JavaFile file) {
    return "@"
        + file.use("com.fasterxml.jackson.databind.annotation.JsonDeserialize")
        + "(using = "
        + file.use("com.fasterxml.jackson.databind.JsonDeserializer")
        + ".None.class)\n";
  }

  // --- enums -----------------------------------------------------------------------------------

  private JavaFile openEnum(Ir.EnumType type) {
    String name = type.name();
    JavaFile file = new JavaFile(Surface.MODEL_PACKAGE, name);
    String summary = Javadoc.present(type.description()) ? Javadoc.text(type.description()) : null;
    file.append(
        Javadoc.block(
            "",
            summary,
            "The known values are constants; the API may add values at any time, and {@link #of}"
                + " accepts them too (ADR-0006)."));
    file.append("public final class ").append(name).append(" {\n");
    List<String> constants = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (String value : type.values()) {
      String constant = Naming.constant(value, name);
      if (!seen.add(constant)) {
        throw new GeneratorException(name + ": two values are named " + constant);
      }
      constants.add(constant);
      file.append("  public static final " + name + " " + constant + " = new " + name + "(");
      file.append(literal(value)).append(");\n");
    }
    String map = file.use("java.util.Map");
    file.append("\n  private static final " + map + "<String, " + name + "> KNOWN =\n");
    file.append(
        "      " + file.use("java.util.stream.Stream") + ".of(" + String.join(", ", constants));
    file.append(")\n          .collect(" + file.use("java.util.stream.Collectors"));
    file.append(".toUnmodifiableMap(" + name + "::value, ");
    file.append(file.use("java.util.function.Function") + ".identity()));\n\n");
    file.append("  private final String value;\n\n");
    file.append("  private " + name + "(String value) {\n    this.value = value;\n  }\n\n");
    file.append("  /** The value for a wire value, known or not. */\n");
    file.append("  @" + file.use("com.fasterxml.jackson.annotation.JsonCreator"));
    file.append("(mode = JsonCreator.Mode.DELEGATING)\n");
    file.append("  public static " + name + " of(String value) {\n");
    file.append("    " + file.use("java.util.Objects") + ".requireNonNull(value, \"value\");\n");
    file.append("    " + name + " known = KNOWN.get(value);\n");
    file.append("    return known != null ? known : new " + name + "(value);\n  }\n\n");
    file.append("  /** The wire value. */\n");
    file.append("  @" + file.use("com.fasterxml.jackson.annotation.JsonValue") + "\n");
    file.append("  public String value() {\n    return value;\n  }\n\n");
    file.append("  /** Whether this version of the library knows this value. */\n");
    file.append("  public boolean isKnown() {\n    return KNOWN.containsKey(value);\n  }\n\n");
    file.append("  @Override\n  public boolean equals(Object other) {\n");
    file.append(
        "    return other instanceof " + name + " that && that.value.equals(value);\n  }\n\n");
    file.append("  @Override\n  public int hashCode() {\n    return value.hashCode();\n  }\n\n");
    file.append("  @Override\n  public String toString() {\n    return value;\n  }\n}\n");
    return file;
  }

  static String literal(String value) {
    StringBuilder literal = new StringBuilder("\"");
    for (char c : value.toCharArray()) {
      switch (c) {
        case '"' -> literal.append("\\\"");
        case '\\' -> literal.append("\\\\");
        default -> {
          if (c < 0x20 || c > 0x7e) {
            literal.append(String.format("\\u%04x", (int) c));
          } else {
            literal.append(c);
          }
        }
      }
    }
    return literal.append('"').toString();
  }

  // --- unions ----------------------------------------------------------------------------------

  private JavaFile union(Ir.UnionType union) {
    String name = union.name();
    JavaFile file = new JavaFile(Surface.MODEL_PACKAGE, name);
    String fallback = surface.fallbacks.get(name);
    List<String> members = new ArrayList<>(Surface.variants(union));
    members.add(fallback);
    String summary =
        Javadoc.present(union.description()) ? Javadoc.text(union.description()) : null;
    String how =
        union.discriminator() instanceof Ir.PropertyDiscriminator property
            ? "Told apart by {@code "
                + property.property()
                + "}; a value this version of the"
                + " library doesn't know reads as {@link "
                + fallback
                + "}, never as an error."
            : "Told apart by which field is present; with none of them it reads as {@link "
                + fallback
                + "}.";
    file.append(Javadoc.block("", summary, how));
    file.append("@" + file.use("com.fasterxml.jackson.databind.annotation.JsonDeserialize"));
    file.append("(using = " + name + "Deserializer.class)\n");
    file.append("public sealed interface " + name + " permits ");
    file.append(String.join(", ", new TreeSet<>(members))).append(" {\n");
    if (union.discriminator() instanceof Ir.PropertyDiscriminator property) {
      String identifier = Naming.identifier(property.property(), name);
      file.append("  /** The {@code " + property.property() + "} that selects the variant. */\n");
      file.append("  String " + Naming.getter(identifier, false) + "();\n\n");
    }
    file.append(
        "  /** Fields this version of the library has no getter for, keyed by JSON name. */\n");
    file.append("  " + file.use("java.util.Map") + "<String, Object> getExtraFields();\n");
    if (surface.resources.contains(name)) {
      file.append("\n  /** The response this object was read from. */\n");
      file.append("  " + file.use(CORE + "LastResponse") + " getLastResponse();\n");
    }
    return file.append("}\n");
  }

  private JavaFile deserializer(Ir.UnionType union) {
    String name = union.name();
    JavaFile file = new JavaFile(Surface.MODEL_PACKAGE, name + "Deserializer");
    String base = file.use(CORE + "UnionDeserializer");
    String map = file.use("java.util.Map");
    List<String> entries = new ArrayList<>();
    union
        .discriminator()
        .mapping()
        .forEach(
            (key, member) ->
                entries.add(map + ".entry(" + literal(key) + ", " + member + ".class)"));
    file.append("final class " + name + "Deserializer extends " + base + "<" + name + "> {\n");
    file.append("  private static final long serialVersionUID = 1L;\n\n");
    file.append("  " + name + "Deserializer() {\n    super(\n        " + name + ".class,\n");
    if (union.discriminator() instanceof Ir.PropertyDiscriminator property) {
      file.append("        " + literal(property.property()) + ",\n");
      file.append("        " + map + ".ofEntries(" + String.join(", ", entries) + "),\n");
    } else {
      file.append("        " + file.use("java.util.List") + ".of(" + String.join(", ", entries));
      file.append("),\n");
    }
    file.append("        " + surface.fallbacks.get(name) + ".class);\n  }\n}\n");
    return file;
  }

  private JavaFile unknownVariant(Ir.UnionType union) {
    Ir.PropertyDiscriminator property = (Ir.PropertyDiscriminator) union.discriminator();
    Ir.Field tag =
        new Ir.Field(
            property.property(),
            "The `" + property.property() + "` this version of the library doesn't know.",
            true,
            false,
            new Ir.Primitive("string"));
    return object(
        surface.fallbacks.get(union.name()),
        "A {@link "
            + union.name()
            + "} of a kind this version of the library doesn't know. Its other fields are kept in"
            + " {@link #getExtraFields()}.",
        List.of(tag));
  }
}
