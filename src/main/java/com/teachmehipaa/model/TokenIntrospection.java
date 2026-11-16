// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;
import java.util.List;

public final class TokenIntrospection extends ApiResource {
  private final TokenIntrospectionActorType actorType;
  private final String clientId;
  private final String subjectId;
  private final String orgId;
  private final List<TokenIntrospectionScopesItem> scopes;
  private final TokenIntrospectionMode mode;
  private final OffsetDateTime expiresAt;

  @JsonCreator
  TokenIntrospection(
      @JsonProperty("actorType") TokenIntrospectionActorType actorType,
      @JsonProperty("clientId") String clientId,
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("orgId") String orgId,
      @JsonProperty("scopes") List<TokenIntrospectionScopesItem> scopes,
      @JsonProperty("mode") TokenIntrospectionMode mode,
      @JsonProperty("expiresAt") OffsetDateTime expiresAt) {
    this.actorType = actorType;
    this.clientId = clientId;
    this.subjectId = subjectId;
    this.orgId = orgId;
    this.scopes = immutable(scopes);
    this.mode = mode;
    this.expiresAt = expiresAt;
  }

  /** <code>client</code> for an API client, <code>user</code> for a person. */
  public TokenIntrospectionActorType getActorType() {
    return actorType;
  }

  /** The API client the token belongs to. May be {@code null}. */
  public String getClientId() {
    return clientId;
  }

  /** The person the token belongs to, for a user token. May be {@code null}. */
  public String getSubjectId() {
    return subjectId;
  }

  /** The organization the token is bound to, if any. May be {@code null}. */
  public String getOrgId() {
    return orgId;
  }

  /** The scopes the token holds. */
  public List<TokenIntrospectionScopesItem> getScopes() {
    return scopes;
  }

  /** <code>test</code> for a Sandbox client, <code>live</code> otherwise. May be {@code null}. */
  public TokenIntrospectionMode getMode() {
    return mode;
  }

  /** When the token expires (ISO 8601). May be {@code null}. */
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }
}
