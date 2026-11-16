// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.EnrollmentsBatchRequestOperation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.enrollments().batchByFilter(…)}. Immutable. */
public final class EnrollmentBatchByFilterParams {
  private final EnrollmentsBatchRequestOperation operation;
  private final EnrollmentsBatchRequestSelectFilter select;

  private EnrollmentBatchByFilterParams(Builder builder) {
    this.operation = builder.operation;
    this.select = builder.select;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * What to do to each enrollment: <code>remind</code>, <code>resendInvite</code>, <code>cancel
   * </code>, <code>recertify</code>, <code>archive</code> or <code>unarchive</code>.
   */
  public EnrollmentsBatchRequestOperation getOperation() {
    return operation;
  }

  /**
   * The enrollments to act on: a list of <code>enrollmentIds</code> (up to 500), or a <code>filter
   * </code>.
   */
  public EnrollmentsBatchRequestSelectFilter getSelect() {
    return select;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (operation != null) {
      json.put("operation", operation);
    }
    if (select != null) {
      json.put("select", select);
    }
    return json;
  }

  public static final class Builder {
    private EnrollmentsBatchRequestOperation operation;
    private EnrollmentsBatchRequestSelectFilter select;

    private Builder() {}

    /**
     * What to do to each enrollment: <code>remind</code>, <code>resendInvite</code>, <code>cancel
     * </code>, <code>recertify</code>, <code>archive</code> or <code>unarchive</code>. Required.
     */
    public Builder operation(EnrollmentsBatchRequestOperation operation) {
      this.operation = Objects.requireNonNull(operation, "operation");
      return this;
    }

    /**
     * The enrollments to act on: a list of <code>enrollmentIds</code> (up to 500), or a <code>
     * filter</code>. Required.
     */
    public Builder select(EnrollmentsBatchRequestSelectFilter select) {
      this.select = Objects.requireNonNull(select, "select");
      return this;
    }

    /**
     * @throws IllegalStateException when {@code operation}, {@code select} is not set
     */
    public EnrollmentBatchByFilterParams build() {
      if (operation == null) {
        throw new IllegalStateException("operation is required");
      }
      if (select == null) {
        throw new IllegalStateException("select is required");
      }
      return new EnrollmentBatchByFilterParams(this);
    }
  }
}
