// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable. */
public final class EnrollmentsBatchRequestSelectFilter {
  private final EnrollmentsBatchRequestSelectFilterFilter filter;

  private EnrollmentsBatchRequestSelectFilter(Builder builder) {
    this.filter = builder.filter;
  }

  public static Builder builder() {
    return new Builder();
  }

  public EnrollmentsBatchRequestSelectFilterFilter getFilter() {
    return filter;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (filter != null) {
      json.put("filter", filter);
    }
    return json;
  }

  public static final class Builder {
    private EnrollmentsBatchRequestSelectFilterFilter filter;

    private Builder() {}

    /** Required. */
    public Builder filter(EnrollmentsBatchRequestSelectFilterFilter filter) {
      this.filter = Objects.requireNonNull(filter, "filter");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code filter} is not set
     */
    public EnrollmentsBatchRequestSelectFilter build() {
      if (filter == null) {
        throw new IllegalStateException("filter is required");
      }
      return new EnrollmentsBatchRequestSelectFilter(this);
    }
  }
}
