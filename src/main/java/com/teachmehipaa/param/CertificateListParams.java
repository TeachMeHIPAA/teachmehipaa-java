// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import java.util.Objects;

/** Parameters for {@code client.certificates().list(…)}. Immutable. */
public final class CertificateListParams {
  private final Long limit;
  private final String cursor;
  private final String subjectId;

  private CertificateListParams(Builder builder) {
    this.limit = builder.limit;
    this.cursor = builder.cursor;
    this.subjectId = builder.subjectId;
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

  /** Filter to one holder (canonical subject id). */
  public String getSubjectId() {
    return subjectId;
  }

  public static final class Builder {
    private Long limit;
    private String cursor;
    private String subjectId;

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

    /** Filter to one holder (canonical subject id). */
    public Builder subjectId(String subjectId) {
      this.subjectId = Objects.requireNonNull(subjectId, "subjectId");
      return this;
    }

    public CertificateListParams build() {
      return new CertificateListParams(this);
    }
  }
}
