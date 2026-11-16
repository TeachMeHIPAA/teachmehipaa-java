// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class ComplianceSummary extends ApiResource {
  private final OffsetDateTime asOf;
  private final String courseId;
  private final ComplianceSummaryTotals totals;
  private final double compliantRate;

  @JsonCreator
  ComplianceSummary(
      @JsonProperty("asOf") OffsetDateTime asOf,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("totals") ComplianceSummaryTotals totals,
      @JsonProperty("compliantRate") double compliantRate) {
    this.asOf = asOf;
    this.courseId = courseId;
    this.totals = totals;
    this.compliantRate = compliantRate;
  }

  /** When the counts were taken (ISO 8601). */
  public OffsetDateTime getAsOf() {
    return asOf;
  }

  /** The course. */
  public String getCourseId() {
    return courseId;
  }

  /**
   * Learners by status: <code>learners</code>, <code>compliant</code>, <code>expiringSoon</code>,
   * <code>expired</code>, <code>inProgress</code>, <code>notStarted</code> and <code>inviteBounced
   * </code>.
   */
  public ComplianceSummaryTotals getTotals() {
    return totals;
  }

  /** <code>compliant</code> divided by <code>learners</code>, or 0 with no learners. */
  public double getCompliantRate() {
    return compliantRate;
  }
}
