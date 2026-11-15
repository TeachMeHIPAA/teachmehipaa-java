// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.teachmehipaa.model.LearnerStatus;
import java.util.Objects;

/** Parameters for {@code client.learners().list(…)}. Immutable. */
public final class LearnerListParams {
  private final Long limit;
  private final String cursor;
  private final String email;
  private final String externalId;
  private final LearnerStatus status;
  private final String courseId;

  private LearnerListParams(Builder builder) {
    this.limit = builder.limit;
    this.cursor = builder.cursor;
    this.email = builder.email;
    this.externalId = builder.externalId;
    this.status = builder.status;
    this.courseId = builder.courseId;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** How many objects to return, between 1 and 100. Defaults to 20. */
  public Long getLimit() {
    return limit;
  }

  /** The <code>nextCursor</code> from the previous page. */
  public String getCursor() {
    return cursor;
  }

  public String getEmail() {
    return email;
  }

  /**
   * The integration's own id for the learner (unique per organization), or the SCIM <code>
   * externalId</code> when none was set.
   */
  public String getExternalId() {
    return externalId;
  }

  public LearnerStatus getStatus() {
    return status;
  }

  /** The course. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  public static final class Builder {
    private Long limit;
    private String cursor;
    private String email;
    private String externalId;
    private LearnerStatus status;
    private String courseId;

    private Builder() {}

    /** How many objects to return, between 1 and 100. Defaults to 20. */
    public Builder limit(long limit) {
      this.limit = limit;
      return this;
    }

    /** The <code>nextCursor</code> from the previous page. */
    public Builder cursor(String cursor) {
      this.cursor = Objects.requireNonNull(cursor, "cursor");
      return this;
    }

    public Builder email(String email) {
      this.email = Objects.requireNonNull(email, "email");
      return this;
    }

    /**
     * The integration's own id for the learner (unique per organization), or the SCIM <code>
     * externalId</code> when none was set.
     */
    public Builder externalId(String externalId) {
      this.externalId = Objects.requireNonNull(externalId, "externalId");
      return this;
    }

    public Builder status(LearnerStatus status) {
      this.status = Objects.requireNonNull(status, "status");
      return this;
    }

    /** The course. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    public LearnerListParams build() {
      return new LearnerListParams(this);
    }
  }
}
