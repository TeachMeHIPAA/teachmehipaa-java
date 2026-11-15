// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.CertificatesExportRequestFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.certificates().export(…)}. Immutable. */
public final class CertificateExportParams {
  private final CertificatesExportRequestFormat format;

  private CertificateExportParams(Builder builder) {
    this.format = builder.format;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The file format. Only <code>csv</code> today. */
  public CertificatesExportRequestFormat getFormat() {
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
    private CertificatesExportRequestFormat format;

    private Builder() {}

    /** The file format. Only <code>csv</code> today. Required. */
    public Builder format(CertificatesExportRequestFormat format) {
      this.format = Objects.requireNonNull(format, "format");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code format} is not set
     */
    public CertificateExportParams build() {
      if (format == null) {
        throw new IllegalStateException("format is required");
      }
      return new CertificateExportParams(this);
    }
  }
}
