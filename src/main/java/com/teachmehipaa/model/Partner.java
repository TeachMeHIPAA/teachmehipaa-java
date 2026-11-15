// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class Partner extends ApiResource {
  private final String partnerId;
  private final String name;
  private final String appId;
  private final PartnerStatus status;
  private final long maxChildOrgs;
  private final long childOrgCount;
  private final OffsetDateTime createdAt;

  @JsonCreator
  Partner(
      @JsonProperty("partnerId") String partnerId,
      @JsonProperty("name") String name,
      @JsonProperty("appId") String appId,
      @JsonProperty("status") PartnerStatus status,
      @JsonProperty("maxChildOrgs") long maxChildOrgs,
      @JsonProperty("childOrgCount") long childOrgCount,
      @JsonProperty("createdAt") OffsetDateTime createdAt) {
    this.partnerId = partnerId;
    this.name = name;
    this.appId = appId;
    this.status = status;
    this.maxChildOrgs = maxChildOrgs;
    this.childOrgCount = childOrgCount;
    this.createdAt = createdAt;
  }

  /** Unique identifier for the partner account. */
  public String getPartnerId() {
    return partnerId;
  }

  /** The partner's name. */
  public String getName() {
    return name;
  }

  /** The partner app. */
  public String getAppId() {
    return appId;
  }

  /** <code>active</code> or <code>suspended</code>. */
  public PartnerStatus getStatus() {
    return status;
  }

  /** How many organizations the partner can create. */
  public long getMaxChildOrgs() {
    return maxChildOrgs;
  }

  /** How many it has created. */
  public long getChildOrgCount() {
    return childOrgCount;
  }

  /** When it was created (ISO 8601). */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
