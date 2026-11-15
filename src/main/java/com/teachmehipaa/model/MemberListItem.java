// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class MemberListItem extends ApiModel {
  private final String subjectId;
  private final String email;
  private final String name;
  private final MemberListItemRole role;
  private final boolean isDisabled;
  private final String enrollmentSubjectId;
  private final Boolean holdsInvitation;

  @JsonCreator
  MemberListItem(
      @JsonProperty("subjectId") String subjectId,
      @JsonProperty("email") String email,
      @JsonProperty("name") String name,
      @JsonProperty("role") MemberListItemRole role,
      @JsonProperty("isDisabled") boolean isDisabled,
      @JsonProperty("enrollmentSubjectId") String enrollmentSubjectId,
      @JsonProperty("holdsInvitation") Boolean holdsInvitation) {
    this.subjectId = subjectId;
    this.email = email;
    this.name = name;
    this.role = role;
    this.isDisabled = isDisabled;
    this.enrollmentSubjectId = enrollmentSubjectId;
    this.holdsInvitation = holdsInvitation;
  }

  /** May be {@code null}. */
  public String getSubjectId() {
    return subjectId;
  }

  public String getEmail() {
    return email;
  }

  /** May be {@code null}. */
  public String getName() {
    return name;
  }

  public MemberListItemRole getRole() {
    return role;
  }

  public boolean isDisabled() {
    return isDisabled;
  }

  /** May be {@code null}. */
  public String getEnrollmentSubjectId() {
    return enrollmentSubjectId;
  }

  /** May be {@code null}. */
  public Boolean getHoldsInvitation() {
    return holdsInvitation;
  }
}
