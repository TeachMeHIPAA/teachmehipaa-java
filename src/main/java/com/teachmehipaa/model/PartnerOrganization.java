// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;
import java.time.OffsetDateTime;

public final class PartnerOrganization extends ApiModel {
  private final String orgId;
  private final String name;
  private final OffsetDateTime createdAt;
  private final boolean grantActive;

  @JsonCreator
  PartnerOrganization(
      @JsonProperty("orgId") String orgId,
      @JsonProperty("name") String name,
      @JsonProperty("createdAt") OffsetDateTime createdAt,
      @JsonProperty("grantActive") boolean grantActive) {
    this.orgId = orgId;
    this.name = name;
    this.createdAt = createdAt;
    this.grantActive = grantActive;
  }

  public String getOrgId() {
    return orgId;
  }

  public String getName() {
    return name;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public boolean isGrantActive() {
    return grantActive;
  }
}
