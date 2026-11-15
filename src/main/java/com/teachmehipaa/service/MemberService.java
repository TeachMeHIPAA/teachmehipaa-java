// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.Member;
import com.teachmehipaa.model.MemberListItem;
import com.teachmehipaa.param.MemberListParams;
import java.util.Objects;

/** Members in the client's Organization. Reach it through {@code client.members()}. */
public final class MemberService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public MemberService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** List all members. */
  public Page<MemberListItem> list() {
    return list(MemberListParams.builder().build(), RequestOptions.none());
  }

  /** List all members. */
  public Page<MemberListItem> list(RequestOptions options) {
    return list(MemberListParams.builder().build(), options);
  }

  /** List all members. */
  public Page<MemberListItem> list(MemberListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all members.
   *
   * <p>Returns a list of the organization's members, its admins and learners. Pass <code>role=admin
   * </code> to list only admins.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link MemberListItem}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<MemberListItem> list(MemberListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/members")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("include", params.getInclude())
            .queryParam("role", params.getRole())
            .retrySafe()
            .build();
    return requester.requestPage(
        request, Pagination.cursor("cursor"), MemberListItem.class, options);
  }

  /** Retrieve a member. */
  public Member retrieve(String subjectId) {
    return retrieve(subjectId, RequestOptions.none());
  }

  /**
   * Retrieve a member.
   *
   * <p>Retrieves a member of the organization.
   *
   * @param subjectId The member's id.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Member}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Member retrieve(String subjectId, RequestOptions options) {
    Objects.requireNonNull(subjectId, "subjectId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/members/{subjectId}")
            .pathParam("subjectId", subjectId)
            .retrySafe()
            .build();
    return requester.request(request, Member.class, options);
  }
}
