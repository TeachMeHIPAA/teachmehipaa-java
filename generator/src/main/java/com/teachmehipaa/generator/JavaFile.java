package com.teachmehipaa.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/** One generated compilation unit: its imports, collected as names are used, and its body. */
final class JavaFile {
  static final String BANNER =
      "// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.\n";

  private final String packageName;
  private final String directory;
  private final String className;
  private final TreeSet<String> imports = new TreeSet<>();
  private final Map<String, String> simpleNames = new HashMap<>();
  private final StringBuilder body = new StringBuilder();

  JavaFile(String packageName, String className) {
    this.packageName = packageName;
    this.className = className;
    this.directory = "src/main/java/" + packageName.replace('.', '/');
  }

  /** Imports {@code qualifiedName} unless it is in this package, and returns its simple name. */
  String use(String qualifiedName) {
    int dot = qualifiedName.lastIndexOf('.');
    String simple = qualifiedName.substring(dot + 1);
    String previous = simpleNames.putIfAbsent(simple, qualifiedName);
    if (previous != null && !previous.equals(qualifiedName)) {
      throw new GeneratorException(
          className + ": " + previous + " and " + qualifiedName + " share a simple name");
    }
    String owner = qualifiedName.substring(0, dot);
    if (!owner.equals(packageName) && !owner.equals("java.lang")) {
      imports.add(qualifiedName);
    }
    return simple;
  }

  JavaFile append(String code) {
    body.append(code);
    return this;
  }

  String path() {
    return directory + "/" + className + ".java";
  }

  /** The unformatted source; {@link Formatting} makes it google-java-format clean. */
  String source() {
    StringBuilder source = new StringBuilder(BANNER).append("package ").append(packageName);
    source.append(";\n\n");
    imports.forEach(name -> source.append("import ").append(name).append(";\n"));
    return source.append('\n').append(body).toString();
  }
}
