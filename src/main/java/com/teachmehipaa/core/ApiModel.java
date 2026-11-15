package com.teachmehipaa.core;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base of every object the API returns. Fields the library doesn't know yet are kept rather than
 * rejected (ADR-0006): {@code /v1} grows by adding fields.
 */
public abstract class ApiModel {
  private final Map<String, Object> extraFields = new LinkedHashMap<>();

  protected ApiModel() {}

  /**
   * Response fields this version of the library has no getter for, keyed by their JSON name. Values
   * are strings, numbers, booleans, {@code null}, lists or maps.
   */
  public Map<String, Object> getExtraFields() {
    return Collections.unmodifiableMap(extraFields);
  }

  @JsonAnySetter
  void putExtraField(String name, Object value) {
    extraFields.put(name, value);
  }

  /** An unmodifiable view of a list Jackson created for this model, or {@code null}. */
  protected static <T> List<T> immutable(List<T> list) {
    return list == null ? null : Collections.unmodifiableList(list);
  }

  /** An unmodifiable view of a map Jackson created for this model, or {@code null}. */
  protected static <T> Map<String, T> immutable(Map<String, T> map) {
    return map == null ? null : Collections.unmodifiableMap(map);
  }
}
