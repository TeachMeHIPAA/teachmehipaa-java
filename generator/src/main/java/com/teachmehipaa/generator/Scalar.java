package com.teachmehipaa.generator;

import java.util.Arrays;

/** The one mapping from IR primitive types to Java types, for models and params alike. */
enum Scalar {
  STRING("string", "java.lang.String", null),
  INTEGER("integer", "java.lang.Long", "long"),
  NUMBER("number", "java.lang.Double", "double"),
  BOOLEAN("boolean", "java.lang.Boolean", "boolean"),
  DATE_TIME("dateTime", "java.time.OffsetDateTime", null),
  JSON("json", "com.fasterxml.jackson.databind.JsonNode", null),
  BINARY("binary", "com.teachmehipaa.FileUpload", null);

  private final String irType;
  private final String boxed;
  private final String primitive;

  Scalar(String irType, String boxed, String primitive) {
    this.irType = irType;
    this.boxed = boxed;
    this.primitive = primitive;
  }

  /**
   * @throws GeneratorException when the IR names a primitive type with no Java type here
   */
  static Scalar of(Ir.Primitive primitive, String where) {
    return Arrays.stream(values())
        .filter(scalar -> scalar.irType.equals(primitive.type()))
        .findFirst()
        .orElseThrow(
            () -> new GeneratorException(where + ": no Java type for " + primitive.type()));
  }

  /** The type that can be {@code null}, imported into {@code file}. */
  String boxed(JavaFile file) {
    return file.use(boxed);
  }

  /** The type of a value that is always present: the primitive, where Java has one. */
  String primitive(JavaFile file) {
    return primitive != null ? primitive : boxed(file);
  }
}
