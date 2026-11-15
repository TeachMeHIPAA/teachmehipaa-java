// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for {@code client.learners().update(…)}. Immutable.
 *
 * <p>Sent as a merge patch: a field you don't set is left unchanged, and a {@code clear} method
 * sends {@code null} to clear one.
 */
public final class LearnerUpdateParams {
  private final String name;
  private final String externalId;
  private final boolean externalIdSet;
  private final String courseId;
  private final String ifMatch;

  private LearnerUpdateParams(Builder builder) {
    this.name = builder.name;
    this.externalId = builder.externalId;
    this.externalIdSet = builder.externalIdSet;
    this.courseId = builder.courseId;
    this.ifMatch = builder.ifMatch;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The learner's full name. */
  public String getName() {
    return name;
  }

  /** Your own id for the learner. Set it to <code>null</code> to clear it. */
  public String getExternalId() {
    return externalId;
  }

  /** The course. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  /**
   * The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a
   * stale one with 412 and a missing one with 428.
   */
  public String getIfMatch() {
    return ifMatch;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (name != null) {
      json.put("name", name);
    }
    if (externalIdSet) {
      json.put("externalId", externalId);
    }
    return json;
  }

  public static final class Builder {
    private String name;
    private String externalId;
    private boolean externalIdSet;
    private String courseId;
    private String ifMatch;

    private Builder() {}

    /** The learner's full name. */
    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    /** Your own id for the learner. Set it to <code>null</code> to clear it. */
    public Builder externalId(String externalId) {
      this.externalId = Objects.requireNonNull(externalId, "externalId");
      this.externalIdSet = true;
      return this;
    }

    /** Sends {@code null} for {@code externalId}, clearing it. */
    public Builder clearExternalId() {
      this.externalId = null;
      this.externalIdSet = true;
      return this;
    }

    /** The course. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /**
     * The ETag the object was read with, from {@code getLastResponse().etag()}. The API refuses a
     * stale one with 412 and a missing one with 428. Required.
     */
    public Builder ifMatch(String ifMatch) {
      this.ifMatch = Objects.requireNonNull(ifMatch, "ifMatch");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code ifMatch} is not set
     */
    public LearnerUpdateParams build() {
      if (ifMatch == null) {
        throw new IllegalStateException("ifMatch is required");
      }
      return new LearnerUpdateParams(this);
    }
  }
}
