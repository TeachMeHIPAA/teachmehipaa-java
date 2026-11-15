// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.teachmehipaa.model.ListEnrollmentsArchived;
import com.teachmehipaa.model.ListEnrollmentsInclude;
import com.teachmehipaa.model.ListEnrollmentsStatus;
import java.util.Objects;

/** Parameters for {@code client.enrollments().list(…)}. Immutable. */
public final class EnrollmentListParams {
  private final Long limit;
  private final String cursor;
  private final ListEnrollmentsStatus status;
  private final String courseId;
  private final ListEnrollmentsArchived archived;
  private final ListEnrollmentsInclude include;

  private EnrollmentListParams(Builder builder) {
    this.limit = builder.limit;
    this.cursor = builder.cursor;
    this.status = builder.status;
    this.courseId = builder.courseId;
    this.archived = builder.archived;
    this.include = builder.include;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** How many objects to return, between 1 and 1000. Defaults to 20. */
  public Long getLimit() {
    return limit;
  }

  /** The <code>nextCursor</code> from the previous page. */
  public String getCursor() {
    return cursor;
  }

  /** Filter on the derived effectiveStatus. */
  public ListEnrollmentsStatus getStatus() {
    return status;
  }

  public String getCourseId() {
    return courseId;
  }

  public ListEnrollmentsArchived getArchived() {
    return archived;
  }

  /**
   * Opt-in enrichment. With <code>priorCertificate</code>, an item carries <code>priorCertificate
   * </code> (object, or null when the server confirmed there is none). The key is omitted on an
   * item whose answer is unknown, and on every item of the page if the certificate lookup failed;
   * clients must treat a missing key as unknown, never as null.
   */
  public ListEnrollmentsInclude getInclude() {
    return include;
  }

  public static final class Builder {
    private Long limit;
    private String cursor;
    private ListEnrollmentsStatus status;
    private String courseId;
    private ListEnrollmentsArchived archived;
    private ListEnrollmentsInclude include;

    private Builder() {}

    /** How many objects to return, between 1 and 1000. Defaults to 20. */
    public Builder limit(long limit) {
      this.limit = limit;
      return this;
    }

    /** The <code>nextCursor</code> from the previous page. */
    public Builder cursor(String cursor) {
      this.cursor = Objects.requireNonNull(cursor, "cursor");
      return this;
    }

    /** Filter on the derived effectiveStatus. */
    public Builder status(ListEnrollmentsStatus status) {
      this.status = Objects.requireNonNull(status, "status");
      return this;
    }

    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    public Builder archived(ListEnrollmentsArchived archived) {
      this.archived = Objects.requireNonNull(archived, "archived");
      return this;
    }

    /**
     * Opt-in enrichment. With <code>priorCertificate</code>, an item carries <code>priorCertificate
     * </code> (object, or null when the server confirmed there is none). The key is omitted on an
     * item whose answer is unknown, and on every item of the page if the certificate lookup failed;
     * clients must treat a missing key as unknown, never as null.
     */
    public Builder include(ListEnrollmentsInclude include) {
      this.include = Objects.requireNonNull(include, "include");
      return this;
    }

    public EnrollmentListParams build() {
      return new EnrollmentListParams(this);
    }
  }
}
