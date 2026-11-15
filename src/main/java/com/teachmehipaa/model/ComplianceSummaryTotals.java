// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

/**
 * Learners by status: <code>learners</code>, <code>compliant</code>, <code>expiringSoon</code>,
 * <code>expired</code>, <code>inProgress</code>, <code>notStarted</code> and <code>inviteBounced
 * </code>.
 */
public final class ComplianceSummaryTotals extends ApiModel {
  private final long learners;
  private final long compliant;
  private final long expiringSoon;
  private final long expired;
  private final long inProgress;
  private final long notStarted;
  private final long inviteBounced;

  @JsonCreator
  ComplianceSummaryTotals(
      @JsonProperty("learners") long learners,
      @JsonProperty("compliant") long compliant,
      @JsonProperty("expiringSoon") long expiringSoon,
      @JsonProperty("expired") long expired,
      @JsonProperty("inProgress") long inProgress,
      @JsonProperty("notStarted") long notStarted,
      @JsonProperty("inviteBounced") long inviteBounced) {
    this.learners = learners;
    this.compliant = compliant;
    this.expiringSoon = expiringSoon;
    this.expired = expired;
    this.inProgress = inProgress;
    this.notStarted = notStarted;
    this.inviteBounced = inviteBounced;
  }

  public long getLearners() {
    return learners;
  }

  public long getCompliant() {
    return compliant;
  }

  public long getExpiringSoon() {
    return expiringSoon;
  }

  public long getExpired() {
    return expired;
  }

  public long getInProgress() {
    return inProgress;
  }

  public long getNotStarted() {
    return notStarted;
  }

  public long getInviteBounced() {
    return inviteBounced;
  }
}
