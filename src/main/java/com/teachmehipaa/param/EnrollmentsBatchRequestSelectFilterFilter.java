// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.EnrollmentsBatchRequestSelectFilterFilterStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable. */
public final class EnrollmentsBatchRequestSelectFilterFilter {
  private final EnrollmentsBatchRequestSelectFilterFilterStatus status;
  private final String courseId;
  private final Boolean archived;

  private EnrollmentsBatchRequestSelectFilterFilter(Builder builder) {
    this.status = builder.status;
    this.courseId = builder.courseId;
    this.archived = builder.archived;
  }

  public static Builder builder() {
    return new Builder();
  }

  public EnrollmentsBatchRequestSelectFilterFilterStatus getStatus() {
    return status;
  }

  public String getCourseId() {
    return courseId;
  }

  public Boolean getArchived() {
    return archived;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (status != null) {
      json.put("status", status);
    }
    if (courseId != null) {
      json.put("courseId", courseId);
    }
    if (archived != null) {
      json.put("archived", archived);
    }
    return json;
  }

  public static final class Builder {
    private EnrollmentsBatchRequestSelectFilterFilterStatus status;
    private String courseId;
    private Boolean archived;

    private Builder() {}

    public Builder status(EnrollmentsBatchRequestSelectFilterFilterStatus status) {
      this.status = Objects.requireNonNull(status, "status");
      return this;
    }

    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    public Builder archived(boolean archived) {
      this.archived = archived;
      return this;
    }

    public EnrollmentsBatchRequestSelectFilterFilter build() {
      return new EnrollmentsBatchRequestSelectFilterFilter(this);
    }
  }
}
