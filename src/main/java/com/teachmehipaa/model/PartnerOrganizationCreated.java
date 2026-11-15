// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class PartnerOrganizationCreated extends ApiResource {
  private final String orgId;
  private final String name;
  private final OffsetDateTime createdAt;
  private final PartnerOrganizationCreatedGrant grant;
  private final String refreshToken;

  @JsonCreator
  PartnerOrganizationCreated(
      @JsonProperty("orgId") String orgId,
      @JsonProperty("name") String name,
      @JsonProperty("createdAt") OffsetDateTime createdAt,
      @JsonProperty("grant") PartnerOrganizationCreatedGrant grant,
      @JsonProperty("refresh_token") String refreshToken) {
    this.orgId = orgId;
    this.name = name;
    this.createdAt = createdAt;
    this.grant = grant;
    this.refreshToken = refreshToken;
  }

  /** The new organization's id. */
  public String getOrgId() {
    return orgId;
  }

  /** The organization's name. */
  public String getName() {
    return name;
  }

  /** When it was created (ISO 8601). */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /** Your app's connection to the organization. */
  public PartnerOrganizationCreatedGrant getGrant() {
    return grant;
  }

  /**
   * Returned only once. Exchange it at <code>POST /v1/auth/token</code> for the organization's
   * tokens.
   */
  public String getRefreshToken() {
    return refreshToken;
  }
}
