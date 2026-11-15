// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class ProblemErrorsItem extends ApiModel {
  private final String path;
  private final String message;

  @JsonCreator
  ProblemErrorsItem(@JsonProperty("path") String path, @JsonProperty("message") String message) {
    this.path = path;
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public String getMessage() {
    return message;
  }
}
