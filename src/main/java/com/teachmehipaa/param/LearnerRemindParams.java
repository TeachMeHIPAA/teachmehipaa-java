// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import java.util.Objects;

/** Parameters for {@code client.learners().remind(…)}. Immutable. */
public final class LearnerRemindParams {
  private final String courseId;

  private LearnerRemindParams(Builder builder) {
    this.courseId = builder.courseId;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The course. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  public static final class Builder {
    private String courseId;

    private Builder() {}

    /** The course. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    public LearnerRemindParams build() {
      return new LearnerRemindParams(this);
    }
  }
}
