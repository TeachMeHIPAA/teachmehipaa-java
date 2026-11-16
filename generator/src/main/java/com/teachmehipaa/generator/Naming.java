package com.teachmehipaa.generator;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** How IR names become Java names. Every rule fails loudly rather than guess. */
final class Naming {
  private static final Set<String> KEYWORDS =
      Set.of(
          "abstract",
          "assert",
          "boolean",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "class",
          "const",
          "continue",
          "default",
          "do",
          "double",
          "else",
          "enum",
          "extends",
          "final",
          "finally",
          "float",
          "for",
          "goto",
          "if",
          "implements",
          "import",
          "instanceof",
          "int",
          "interface",
          "long",
          "native",
          "new",
          "package",
          "private",
          "protected",
          "public",
          "return",
          "short",
          "static",
          "strictfp",
          "super",
          "switch",
          "synchronized",
          "this",
          "throw",
          "throws",
          "transient",
          "try",
          "void",
          "volatile",
          "while",
          "true",
          "false",
          "null",
          "_");
  private static final Pattern IDENTIFIER = Pattern.compile("[a-z][A-Za-z0-9]*");
  private static final Pattern SCREAMING = Pattern.compile("[A-Z0-9_]+");
  private static final Pattern CONSTANT = Pattern.compile("[A-Z][A-Z0-9_]*");

  static boolean isKeyword(String name) {
    return KEYWORDS.contains(name);
  }

  static String pascal(String name) {
    return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * The Java identifier for a wire name: {@code refresh_token} becomes {@code refreshToken} and
   * {@code UNUSED} becomes {@code unused}. The wire name itself is kept in {@code @JsonProperty}.
   */
  static String identifier(String wireName, String where) {
    String lower =
        SCREAMING.matcher(wireName).matches() ? wireName.toLowerCase(Locale.ROOT) : wireName;
    String[] words = lower.split("_");
    StringBuilder name = new StringBuilder(words[0]);
    for (int i = 1; i < words.length; i++) {
      name.append(pascal(words[i]));
    }
    String identifier = name.toString();
    if (!IDENTIFIER.matcher(identifier).matches() || isKeyword(identifier)) {
      throw new GeneratorException(where + ": \"" + wireName + "\" has no Java identifier");
    }
    return identifier;
  }

  /** A public method name; a Java keyword uses the operation id instead ({@code import}). */
  static String methodName(String name, String operationId, String where) {
    String method = isKeyword(name) ? operationId : name;
    if (!IDENTIFIER.matcher(method).matches() || isKeyword(method)) {
      throw new GeneratorException(where + ": \"" + name + "\" has no Java method name");
    }
    return method;
  }

  /** An enum constant: {@code enrollments:read} becomes {@code ENROLLMENTS_READ}. */
  static String constant(String value, String where) {
    String constant =
        value
            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
            .replaceAll("[^A-Za-z0-9]+", "_")
            .replaceAll("^_+|_+$", "")
            .toUpperCase(Locale.ROOT);
    if (!CONSTANT.matcher(constant).matches()) {
      throw new GeneratorException(where + ": \"" + value + "\" has no constant name");
    }
    return constant;
  }

  /** The design record's singular: drop a trailing "s" ({@code learners}, {@code seats}). */
  static String singular(String word) {
    return word.endsWith("s") ? word.substring(0, word.length() - 1) : word;
  }

  /** {@code [webhookSubscriptions]} becomes {@code WebhookSubscription}; nested parts join. */
  static String singularPascal(List<String> resourcePath) {
    return resourcePath.stream().map(part -> pascal(singular(part))).collect(Collectors.joining());
  }

  static String serviceClass(List<String> resourcePath) {
    return singularPascal(resourcePath) + "Service";
  }

  /** Singular resource path + method + {@code Params}, such as {@code LearnerCreateParams}. */
  static String paramsClass(List<String> resourcePath, String methodName) {
    return singularPascal(resourcePath) + pascal(methodName) + "Params";
  }

  /** {@code auditEvents} becomes {@code Audit events}, for docs. */
  static String words(String camel) {
    return pascal(camel.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT));
  }

  static String getter(String identifier, boolean primitiveBoolean) {
    if (!primitiveBoolean) {
      return "get" + pascal(identifier);
    }
    // "isDisabled" reads as isDisabled(), not isIsDisabled().
    return identifier.matches("is[A-Z].*") ? identifier : "is" + pascal(identifier);
  }

  private Naming() {}
}
