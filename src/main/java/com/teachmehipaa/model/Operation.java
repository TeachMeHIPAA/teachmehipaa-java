// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.core.ApiResource;
import java.util.Map;

public final class Operation extends ApiResource {
  private final String name;
  private final boolean done;
  private final Map<String, JsonNode> metadata;
  private final JsonNode response;
  private final Problem error;

  @JsonCreator
  Operation(
      @JsonProperty("name") String name,
      @JsonProperty("done") boolean done,
      @JsonProperty("metadata") Map<String, JsonNode> metadata,
      @JsonProperty("response") JsonNode response,
      @JsonProperty("error") Problem error) {
    this.name = name;
    this.done = done;
    this.metadata = immutable(metadata);
    this.response = response;
    this.error = error;
  }

  /** Unique identifier for the operation. */
  public String getName() {
    return name;
  }

  /** Whether the operation has finished. */
  public boolean isDone() {
    return done;
  }

  /** Progress information, if any. May be {@code null}. */
  public Map<String, JsonNode> getMetadata() {
    return metadata;
  }

  /** The result, once <code>done</code> is <code>true</code>. May be {@code null}. */
  public JsonNode getResponse() {
    return response;
  }

  /** The problem, if the operation failed. May be {@code null}. */
  public Problem getError() {
    return error;
  }
}
