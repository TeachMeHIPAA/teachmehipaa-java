// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable. */
public final class LearnerBatchUpsertRequestItemsItem {
  private final String email;
  private final String name;
  private final String externalId;
  private final String courseId;

  private LearnerBatchUpsertRequestItemsItem(Builder builder) {
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
    if (externalId != null) {
      json.put("externalId", externalId);
    }
    if (courseId != null) {
      json.put("courseId", courseId);
    }
    return json;
  }

  public static final class Builder {
    private String email;
    private String name;
    private String externalId;
    private String courseId;

    private Builder() {}

    /** Required. */
    public Builder email(String email) {
      this.email = Objects.requireNonNull(email, "email");
      return this;
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    public Builder externalId(String externalId) {
      this.externalId = Objects.requireNonNull(externalId, "externalId");
      return this;
    }

    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code email} is not set
     */
    public LearnerBatchUpsertRequestItemsItem build() {
      if (email == null) {
        throw new IllegalStateException("email is required");
      }
      return new LearnerBatchUpsertRequestItemsItem(this);
    }
  }
}
