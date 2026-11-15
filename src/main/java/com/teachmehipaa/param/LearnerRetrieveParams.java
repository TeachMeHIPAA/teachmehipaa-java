package com.teachmehipaa.param;

import java.util.Objects;

/** Parameters for retrieving a Learner. Immutable. */
public final class LearnerRetrieveParams {
  private static final LearnerRetrieveParams NONE = builder().build();

  private final String courseId;

  private LearnerRetrieveParams(Builder builder) {
    this.courseId = builder.courseId;
  }

  /** No parameters: the Organization's default Course. */
  public static LearnerRetrieveParams none() {
    return NONE;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The Course to read the Learner's status in, or {@code null} for the default Course. */
  public String getCourseId() {
    return courseId;
  }

  public static final class Builder {
    private String courseId;

    private Builder() {}

    /** The Course to read the Learner's status in. Defaults to the Organization's default. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    public LearnerRetrieveParams build() {
      return new LearnerRetrieveParams(this);
    }
  }
}
