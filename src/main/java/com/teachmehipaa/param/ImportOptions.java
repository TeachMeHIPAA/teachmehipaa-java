// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable. */
public final class ImportOptions {
  private final String courseId;

  private ImportOptions(Builder builder) {
    this.courseId = builder.courseId;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The course to invite everyone to. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (courseId != null) {
      json.put("courseId", courseId);
    }
    return json;
  }

  public static final class Builder {
    private String courseId;

    private Builder() {}

    /** The course to invite everyone to. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    public ImportOptions build() {
      return new ImportOptions(this);
    }
  }
}
