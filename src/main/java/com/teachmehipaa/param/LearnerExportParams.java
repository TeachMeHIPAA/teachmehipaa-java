// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.model.LearnersExportRequestFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.learners().export(…)}. Immutable. */
public final class LearnerExportParams {
  private final LearnersExportRequestFormat format;
  private final String courseId;
  private final LearnerStatus status;

  private LearnerExportParams(Builder builder) {
    this.format = builder.format;
    this.courseId = builder.courseId;
    this.status = builder.status;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The file format. Only <code>csv</code> today. */
  public LearnersExportRequestFormat getFormat() {
    return format;
  }

  /** The course to export. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  /** Only export learners with this status. */
  public LearnerStatus getStatus() {
    return status;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (format != null) {
      json.put("format", format);
    }
    if (courseId != null) {
      json.put("courseId", courseId);
    }
    if (status != null) {
      json.put("status", status);
    }
    return json;
  }

  public static final class Builder {
    private LearnersExportRequestFormat format;
    private String courseId;
    private LearnerStatus status;

    private Builder() {}

    /** The file format. Only <code>csv</code> today. Required. */
    public Builder format(LearnersExportRequestFormat format) {
      this.format = Objects.requireNonNull(format, "format");
      return this;
    }

    /** The course to export. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /** Only export learners with this status. */
    public Builder status(LearnerStatus status) {
      this.status = Objects.requireNonNull(status, "status");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code format} is not set
     */
    public LearnerExportParams build() {
      if (format == null) {
        throw new IllegalStateException("format is required");
      }
      return new LearnerExportParams(this);
    }
  }
}
