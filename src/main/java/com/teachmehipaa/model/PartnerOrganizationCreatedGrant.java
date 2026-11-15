// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;
import java.util.List;

/** Your app's connection to the organization. */
public final class PartnerOrganizationCreatedGrant extends ApiModel {
  private final String grantId;
  private final List<PartnerOrganizationCreatedGrantScopesItem> scopes;

  @JsonCreator
  PartnerOrganizationCreatedGrant(
      @JsonProperty("grantId") String grantId,
      @JsonProperty("scopes") List<PartnerOrganizationCreatedGrantScopesItem> scopes) {
    this.grantId = grantId;
    this.scopes = immutable(scopes);
  }

  public String getGrantId() {
    return grantId;
  }

  public List<PartnerOrganizationCreatedGrantScopesItem> getScopes() {
    return scopes;
  }
}
