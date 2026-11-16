// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;
import java.util.List;

public final class Problem extends ApiModel {
  private final String type;
  private final String title;
  private final long status;
  private final String detail;
  private final String instance;
  private final ProblemCode code;
  private final List<ProblemErrorsItem> errors;

  @JsonCreator
  Problem(
      @JsonProperty("type") String type,
      @JsonProperty("title") String title,
      @JsonProperty("status") long status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("code") ProblemCode code,
      @JsonProperty("errors") List<ProblemErrorsItem> errors) {
    this.type = type;
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.instance = instance;
    this.code = code;
    this.errors = immutable(errors);
  }

  /** A URI identifying the problem type. May be {@code null}. */
  public String getType() {
    return type;
  }

  /** A short summary of the problem. */
  public String getTitle() {
    return title;
  }

  /** The HTTP status code. */
  public long getStatus() {
    return status;
  }

  /** An explanation of this occurrence. May be {@code null}. */
  public String getDetail() {
    return detail;
  }

  /** Identifies this occurrence. May be {@code null}. */
  public String getInstance() {
    return instance;
  }

  /** A machine-readable error code, such as <code>seat_unavailable</code>. */
  public ProblemCode getCode() {
    return code;
  }

  /**
   * Each invalid field, with its <code>path</code> and <code>message</code>. May be {@code null}.
   */
  public List<ProblemErrorsItem> getErrors() {
    return errors;
  }
}
