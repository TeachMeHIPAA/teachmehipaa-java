// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.enrollments().create(…)}. Immutable. */
public final class EnrollmentCreateParams {
  private final String email;
  private final String name;
  private final String courseId;

  private EnrollmentCreateParams(Builder builder) {
    this.email = builder.email;
    this.name = builder.name;
    this.courseId = builder.courseId;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The learner's email address. */
  public String getEmail() {
    return email;
  }

  /** The learner's full name. */
  public String getName() {
    return name;
  }

  /** The course to invite them to. Defaults to <code>hipaa</code>. */
  public String getCourseId() {
    return courseId;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (email != null) {
      json.put("email", email);
    }
    if (name != null) {
      json.put("name", name);
    }
    if (courseId != null) {
      json.put("courseId", courseId);
    }
    return json;
  }

  public static final class Builder {
    private String email;
    private String name;
    private String courseId;

    private Builder() {}

    /** The learner's email address. Required. */
    public Builder email(String email) {
      this.email = Objects.requireNonNull(email, "email");
      return this;
    }

    /** The learner's full name. */
    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    /** The course to invite them to. Defaults to <code>hipaa</code>. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code email} is not set
     */
    public EnrollmentCreateParams build() {
      if (email == null) {
        throw new IllegalStateException("email is required");
      }
      return new EnrollmentCreateParams(this);
    }
  }
}
