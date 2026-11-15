package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

/** Parameters for inviting a Learner to a Course. Immutable. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LearnerCreateParams {
  private final String email;
  private final String name;
  private final String externalId;
  private final String courseId;

  private LearnerCreateParams(Builder builder) {
    this.email = builder.email;
    this.name = builder.name;
    this.externalId = builder.externalId;
    this.courseId = builder.courseId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getCourseId() {
    return courseId;
  }

  public static final class Builder {
    private String email;
    private String name;
    private String externalId;
    private String courseId;

    private Builder() {}

    /** The Learner's email address. Required. */
    public Builder email(String email) {
      this.email = Objects.requireNonNull(email, "email");
      return this;
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    /** Your own id for the Learner, unique within the Organization. */
    public Builder externalId(String externalId) {
      this.externalId = Objects.requireNonNull(externalId, "externalId");
      return this;
    }

    /** The Course to invite them to. Defaults to the Organization's default Course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /**
     * @throws IllegalStateException when the email is missing
     */
    public LearnerCreateParams build() {
      if (email == null) {
        throw new IllegalStateException("email is required");
      }
      return new LearnerCreateParams(this);
    }
  }
}
