// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.EnrollmentsExportRequestFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.enrollments().export(…)}. Immutable. */
public final class EnrollmentExportParams {
  private final EnrollmentsExportRequestFormat format;

  private EnrollmentExportParams(Builder builder) {
    this.format = builder.format;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The file format. Only <code>csv</code> today. */
  public EnrollmentsExportRequestFormat getFormat() {
    return format;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (format != null) {
      json.put("format", format);
    }
    return json;
  }

  public static final class Builder {
    private EnrollmentsExportRequestFormat format;

    private Builder() {}

    /** The file format. Only <code>csv</code> today. Required. */
    public Builder format(EnrollmentsExportRequestFormat format) {
      this.format = Objects.requireNonNull(format, "format");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code format} is not set
     */
    public EnrollmentExportParams build() {
      if (format == null) {
        throw new IllegalStateException("format is required");
      }
      return new EnrollmentExportParams(this);
    }
  }
}
