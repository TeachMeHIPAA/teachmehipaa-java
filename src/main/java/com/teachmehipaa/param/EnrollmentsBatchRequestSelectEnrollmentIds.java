// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable. */
public final class EnrollmentsBatchRequestSelectEnrollmentIds {
  private final List<String> enrollmentIds;

  private EnrollmentsBatchRequestSelectEnrollmentIds(Builder builder) {
    this.enrollmentIds = builder.enrollmentIds;
  }

  public static Builder builder() {
    return new Builder();
  }

  public List<String> getEnrollmentIds() {
    return enrollmentIds;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (enrollmentIds != null) {
      json.put("enrollmentIds", enrollmentIds);
    }
    return json;
  }

  public static final class Builder {
    private List<String> enrollmentIds;

    private Builder() {}

    /** Required. */
    public Builder enrollmentIds(List<String> enrollmentIds) {
      this.enrollmentIds = List.copyOf(enrollmentIds);
      return this;
    }

    /**
     * @throws IllegalStateException when {@code enrollmentIds} is not set
     */
    public EnrollmentsBatchRequestSelectEnrollmentIds build() {
      if (enrollmentIds == null) {
        throw new IllegalStateException("enrollmentIds is required");
      }
      return new EnrollmentsBatchRequestSelectEnrollmentIds(this);
    }
  }
}
