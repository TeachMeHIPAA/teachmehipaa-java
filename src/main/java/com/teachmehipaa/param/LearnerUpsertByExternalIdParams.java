// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.LearnerUpsertRequestDelivery;
import com.teachmehipaa.model.LearnerUpsertRequestSeatPolicy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.learners().upsertByExternalId(…)}. Immutable. */
public final class LearnerUpsertByExternalIdParams {
  private final String email;
  private final String name;
  private final String courseId;
  private final LearnerUpsertRequestDelivery delivery;
  private final LearnerUpsertRequestSeatPolicy seatPolicy;

  private LearnerUpsertByExternalIdParams(Builder builder) {
    this.email = builder.email;
    this.name = builder.name;
    this.courseId = builder.courseId;
    this.delivery = builder.delivery;
    this.seatPolicy = builder.seatPolicy;
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

  /** The course to invite them to. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  /**
   * <code>email</code> (default) sends the invite email. <code>none</code> sends nothing and
   * returns <code>joinUrl</code> for you to deliver.
   */
  public LearnerUpsertRequestDelivery getDelivery() {
    return delivery;
  }

  /** <code>require_available</code> fails with <code>409</code> when no seat is free. */
  public LearnerUpsertRequestSeatPolicy getSeatPolicy() {
    return seatPolicy;
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
    if (delivery != null) {
      json.put("delivery", delivery);
    }
    if (seatPolicy != null) {
      json.put("seatPolicy", seatPolicy);
    }
    return json;
  }

  public static final class Builder {
    private String email;
    private String name;
    private String courseId;
    private LearnerUpsertRequestDelivery delivery;
    private LearnerUpsertRequestSeatPolicy seatPolicy;

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

    /** The course to invite them to. Defaults to the organization's default course. */
    public Builder courseId(String courseId) {
      this.courseId = Objects.requireNonNull(courseId, "courseId");
      return this;
    }

    /**
     * <code>email</code> (default) sends the invite email. <code>none</code> sends nothing and
     * returns <code>joinUrl</code> for you to deliver.
     */
    public Builder delivery(LearnerUpsertRequestDelivery delivery) {
      this.delivery = Objects.requireNonNull(delivery, "delivery");
      return this;
    }

    /** <code>require_available</code> fails with <code>409</code> when no seat is free. */
    public Builder seatPolicy(LearnerUpsertRequestSeatPolicy seatPolicy) {
      this.seatPolicy = Objects.requireNonNull(seatPolicy, "seatPolicy");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code email} is not set
     */
    public LearnerUpsertByExternalIdParams build() {
      if (email == null) {
        throw new IllegalStateException("email is required");
      }
      return new LearnerUpsertByExternalIdParams(this);
    }
  }
}
