// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.LearnerCreateRequestDelivery;
import com.teachmehipaa.model.LearnerCreateRequestSeatPolicy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.learners().create(…)}. Immutable. */
public final class LearnerCreateParams {
  private final String email;
  private final String name;
  private final String externalId;
  private final String courseId;
  private final LearnerCreateRequestDelivery delivery;
  private final LearnerCreateRequestSeatPolicy seatPolicy;

  private LearnerCreateParams(Builder builder) {
    this.email = builder.email;
    this.name = builder.name;
    this.externalId = builder.externalId;
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

  /** Your own id for the learner, unique within the organization. */
  public String getExternalId() {
    return externalId;
  }

  /** The course to invite them to. Defaults to the organization's default course. */
  public String getCourseId() {
    return courseId;
  }

  /**
   * <code>email</code> (default) sends the invite email. <code>none</code> sends nothing and
   * returns <code>joinUrl</code> for you to deliver.
   */
  public LearnerCreateRequestDelivery getDelivery() {
    return delivery;
  }

  /** <code>require_available</code> fails with <code>409</code> when no seat is free. */
  public LearnerCreateRequestSeatPolicy getSeatPolicy() {
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
    if (externalId != null) {
      json.put("externalId", externalId);
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
    private String externalId;
    private String courseId;
    private LearnerCreateRequestDelivery delivery;
    private LearnerCreateRequestSeatPolicy seatPolicy;

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

    /** Your own id for the learner, unique within the organization. */
    public Builder externalId(String externalId) {
      this.externalId = Objects.requireNonNull(externalId, "externalId");
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
    public Builder delivery(LearnerCreateRequestDelivery delivery) {
      this.delivery = Objects.requireNonNull(delivery, "delivery");
      return this;
    }

    /** <code>require_available</code> fails with <code>409</code> when no seat is free. */
    public Builder seatPolicy(LearnerCreateRequestSeatPolicy seatPolicy) {
      this.seatPolicy = Objects.requireNonNull(seatPolicy, "seatPolicy");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code email} is not set
     */
    public LearnerCreateParams build() {
      if (email == null) {
        throw new IllegalStateException("email is required");
      }
      return new LearnerCreateParams(this);
    }
  }
}
