// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.FileUpload;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.enrollments().previewImport(…)}. Immutable. */
public final class EnrollmentPreviewImportParams {
  private final FileUpload file;
  private final ImportOptions options;

  private EnrollmentPreviewImportParams(Builder builder) {
    this.file = builder.file;
    this.options = builder.options;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** UTF-8 CSV (RFC 4180: quoted fields, <code>""</code> escapes, CRLF/LF). Header row required. */
  public FileUpload getFile() {
    return file;
  }

  public ImportOptions getOptions() {
    return options;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (options != null) {
      json.put("options", options);
    }
    return json;
  }

  public static final class Builder {
    private FileUpload file;
    private ImportOptions options;

    private Builder() {}

    /**
     * UTF-8 CSV (RFC 4180: quoted fields, <code>""</code> escapes, CRLF/LF). Header row required.
     * Required.
     */
    public Builder file(FileUpload file) {
      this.file = Objects.requireNonNull(file, "file");
      return this;
    }

    public Builder options(ImportOptions options) {
      this.options = Objects.requireNonNull(options, "options");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code file} is not set
     */
    public EnrollmentPreviewImportParams build() {
      if (file == null) {
        throw new IllegalStateException("file is required");
      }
      return new EnrollmentPreviewImportParams(this);
    }
  }
}
