package com.teachmehipaa.generator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/**
 * The parts of {@code openapi/sdk-ir.json} the generator reads. The vendored {@code
 * sdk-ir.schema.json} is the authority: the input is validated against it before it is read into
 * these records.
 */
record Ir(
    int irVersion, Source source, List<Resource> resources, List<IrType> types, Events events) {

  record Source(String release, String sha256) {}

  record Resource(List<String> path, List<Method> methods) {}

  record Method(
      String name,
      String operationId,
      String summary,
      String description,
      String httpMethod,
      PathTemplate path,
      boolean orgScoped,
      List<Param> pathParams,
      List<Param> queryParams,
      List<Param> headerParams,
      RequestBody requestBody,
      Response response,
      Pagination pagination,
      String idempotencyKey,
      String ifMatch,
      String ifNoneMatch,
      boolean retrySafe,
      Security security) {}

  record PathTemplate(String template, String customVerb) {}

  record Param(String name, String description, boolean required, TypeRef type) {}

  record Security(String kind) {}

  record RequestBody(
      String contentType,
      boolean required,
      TypeRef type,
      List<BinaryPart> binaryParts,
      List<Constraint> constraints) {}

  record BinaryPart(String name, String contentType) {}

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = Const.class, name = "const"),
    @JsonSubTypes.Type(value = Variant.class, name = "variant")
  })
  sealed interface Constraint {
    List<String> path();
  }

  /** The method sends {@code value} at {@code path}; callers never set it. */
  record Const(List<String> path, JsonNode value) implements Constraint {}

  /** The presence-union field at {@code path} is narrowed to the member that requires a field. */
  record Variant(List<String> path, String requiredProperty) implements Constraint {}

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = ModelResponse.class, name = "model"),
    @JsonSubTypes.Type(value = PageResponse.class, name = "page"),
    @JsonSubTypes.Type(value = BinaryResponse.class, name = "binary"),
    @JsonSubTypes.Type(value = OperationResponse.class, name = "operation"),
    @JsonSubTypes.Type(value = EmptyResponse.class, name = "empty"),
    @JsonSubTypes.Type(value = WriteResultResponse.class, name = "writeResult")
  })
  sealed interface Response {}

  record ModelResponse(TypeRef type) implements Response {}

  record PageResponse(TypeRef type) implements Response {}

  record BinaryResponse(String contentType) implements Response {}

  record OperationResponse(TypeRef type) implements Response {}

  record EmptyResponse() implements Response {}

  record WriteResultResponse(TypeRef type, TypeRef existing, TypeRef created) implements Response {}

  record Pagination(
      String style, String cursorParam, String limitParam, int maxLimit, TypeRef itemType) {}

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = Primitive.class, name = "primitive"),
    @JsonSubTypes.Type(value = Ref.class, name = "ref"),
    @JsonSubTypes.Type(value = ArrayOf.class, name = "array"),
    @JsonSubTypes.Type(value = MapOf.class, name = "map")
  })
  sealed interface TypeRef {}

  record Primitive(String type) implements TypeRef {}

  record Ref(String name) implements TypeRef {}

  record ArrayOf(TypeRef items) implements TypeRef {}

  record MapOf(TypeRef values) implements TypeRef {}

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = ObjectType.class, name = "object"),
    @JsonSubTypes.Type(value = EnumType.class, name = "enum"),
    @JsonSubTypes.Type(value = UnionType.class, name = "union")
  })
  sealed interface IrType {
    String name();

    String description();
  }

  record ObjectType(String name, String description, List<Field> fields, boolean mergePatch)
      implements IrType {}

  record EnumType(String name, String description, List<String> values) implements IrType {}

  record UnionType(String name, String description, Discriminator discriminator)
      implements IrType {}

  record Field(String name, String description, boolean required, boolean nullable, TypeRef type) {}

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = PropertyDiscriminator.class, name = "property"),
    @JsonSubTypes.Type(value = PresenceDiscriminator.class, name = "presence")
  })
  sealed interface Discriminator {
    /** Tag or marker property to member type, in IR order. */
    Map<String, String> mapping();
  }

  record PropertyDiscriminator(String property, Map<String, String> mapping)
      implements Discriminator {}

  record PresenceDiscriminator(Map<String, String> mapping, String fallback)
      implements Discriminator {}

  record Events(String envelope, String unknownEvent, TestDelivery testDelivery) {}

  record TestDelivery(String type) {}
}
