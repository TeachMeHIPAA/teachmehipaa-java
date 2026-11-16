// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;

/** Parameters for {@code client.enrollments().cancel(…)}. Immutable. */
public final class EnrollmentCancelParams {
  private final Boolean refund;

  private EnrollmentCancelParams(Builder builder) {
    this.refund = builder.refund;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Refund the seat to the card that paid for it instead of returning it to the pool. Defaults to
   * <code>false</code>.
   */
  public Boolean getRefund() {
    return refund;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (refund != null) {
      json.put("refund", refund);
    }
    return json;
  }

  public static final class Builder {
    private Boolean refund;

    private Builder() {}

    /**
     * Refund the seat to the card that paid for it instead of returning it to the pool. Defaults to
     * <code>false</code>.
     */
    public Builder refund(boolean refund) {
      this.refund = refund;
      return this;
    }

    public EnrollmentCancelParams build() {
      return new EnrollmentCancelParams(this);
    }
  }
}
