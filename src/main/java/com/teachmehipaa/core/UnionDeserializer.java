package com.teachmehipaa.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads a union of models, picking the variant from the JSON object. Internal: each generated union
 * has a subclass.
 *
 * <p>A variant this version of the library doesn't know never fails the read (ADR-0006): it becomes
 * the union's fallback, which keeps every field.
 *
 * @param <T> the union type
 */
public abstract class UnionDeserializer<T> extends StdDeserializer<T> {
  private static final long serialVersionUID = 1L;

  private final String property;
  private final transient Map<String, Class<? extends T>> variants;
  private final Class<? extends T> fallback;

  /**
   * A union told apart by the value of one property, such as {@code type}.
   *
   * @param variants each known value and the model it selects
   * @param fallback the model for any other value, or a missing property
   */
  protected UnionDeserializer(
      Class<T> union,
      String property,
      Map<String, Class<? extends T>> variants,
      Class<? extends T> fallback) {
    super(union);
    this.property = Objects.requireNonNull(property, "property");
    this.variants = new LinkedHashMap<>(variants);
    this.fallback = Objects.requireNonNull(fallback, "fallback");
  }

  /**
   * A union told apart by which property is present.
   *
   * @param markers each marker property and the model it selects, checked in order
   * @param fallback the model when no marker is present
   */
  protected UnionDeserializer(
      Class<T> union,
      List<Map.Entry<String, Class<? extends T>>> markers,
      Class<? extends T> fallback) {
    super(union);
    this.property = null;
    this.variants = new LinkedHashMap<>();
    markers.forEach(marker -> variants.put(marker.getKey(), marker.getValue()));
    this.fallback = Objects.requireNonNull(fallback, "fallback");
  }

  @Override
  public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    JsonNode node = context.readTree(parser);
    return context.readTreeAsValue(node, variantOf(node));
  }

  private Class<? extends T> variantOf(JsonNode node) {
    if (property != null) {
      JsonNode tag = node.get(property);
      Class<? extends T> variant =
          tag != null && tag.isTextual() ? variants.get(tag.asText()) : null;
      return variant != null ? variant : fallback;
    }
    return variants.entrySet().stream()
        .filter(marker -> node.has(marker.getKey()))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(fallback);
  }
}
