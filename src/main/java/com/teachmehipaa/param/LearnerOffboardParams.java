// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.teachmehipaa.core.ApiResource;
import com.teachmehipaa.core.Etags;
import java.util.Objects;

/** Parameters for {@code client.learners().offboard(…)}. Immutable. */
public final class LearnerOffboardParams {
  private final String courseId;
  private final String ifMatch;

  private LearnerOffboardParams(Builder builder) {
    this.courseId = builder.courseId;
    this.ifMatch = builder.ifMatch;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The course. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  /**
   * The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a
   * stale one with 412 and a missing one with 428, both as {@link
   * com.teachmehipaa.exception.PreconditionFailedException}.
   */
  public String getIfMatch() {
    return ifMatch;
  }

  public static final class Builder {
    private String courseId;
    private String ifMatch;

    private Builder() {}

    /** The course. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /**
     * The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a
     * stale one with 412 and a missing one with 428, both as {@link
     * com.teachmehipaa.exception.PreconditionFailedException}.
     */
    public Builder ifMatch(String ifMatch) {
      this.ifMatch = Objects.requireNonNull(ifMatch, "ifMatch");
      return this;
    }

    /**
     * Sends the ETag {@code object} was read with, from {@code getLastResponse().etag()}.
     *
     * @throws IllegalArgumentException when {@code object} carries no ETag
     */
    public Builder ifMatch(ApiResource object) {
      this.ifMatch = Etags.of(object);
      return this;
    }

    public LearnerOffboardParams build() {
      return new LearnerOffboardParams(this);
    }
  }
}
