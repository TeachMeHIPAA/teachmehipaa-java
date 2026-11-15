// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class Member extends ApiResource {
  private final String subjectId;
  private final String email;
  private final String name;
  private final MemberRole role;
  private final boolean isDisabled;

  @JsonCreator
  Member(
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("email") String email,
      @JsonProperty("name") String name,
      @JsonProperty("role") MemberRole role,
      @JsonProperty("isDisabled") boolean isDisabled) {
    this.subjectId = subjectId;
    this.email = email;
    this.name = name;
    this.role = role;
    this.isDisabled = isDisabled;
  }

  /**
   * Unique identifier for the member. <code>null</code> for an older member who can't be addressed
   * yet. May be {@code null}.
   */
  public String getSubjectId() {
    return subjectId;
  }

  /** The member's email address. */
  public String getEmail() {
    return email;
  }

  /** The member's full name. May be {@code null}. */
  public String getName() {
    return name;
  }

  /** <code>admin</code> or <code>learner</code>. */
  public MemberRole getRole() {
    return role;
  }

  /** Whether the member's account is disabled. */
  public boolean isDisabled() {
    return isDisabled;
  }
}
