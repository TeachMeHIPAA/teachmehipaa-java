// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.PartnerOrganization;
import com.teachmehipaa.model.PartnerOrganizationCreated;
import com.teachmehipaa.param.PartnerOrganizationCreateParams;
import com.teachmehipaa.param.PartnerOrganizationListParams;
import java.util.Objects;

/** Organizations. Reach it through {@code client.partners().organizations()}. */
public final class PartnerOrganizationService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public PartnerOrganizationService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Create a customer organization. */
  public PartnerOrganizationCreated create(
      String partnerId, PartnerOrganizationCreateParams params) {
    return create(partnerId, params, RequestOptions.none());
  }

  /**
   * Create a customer organization.
   *
   * <p>Creates an organization for a customer with your app already connected, and emails its
   * administrator a link to sign in. The organization has no seats until seats are bought for it.
   * Requires the <code>partner:orgs:write</code> scope.
   *
   * @param partnerId The partner account, <code>ptn_…</code>. Must be the partner the token was
   *     issued for.
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link PartnerOrganizationCreated}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public PartnerOrganizationCreated create(
      String partnerId, PartnerOrganizationCreateParams params, RequestOptions options) {
    Objects.requireNonNull(partnerId, "partnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/partners/{partnerId}/organizations")
            .pathParam("partnerId", partnerId)
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, PartnerOrganizationCreated.class, options);
  }

  /** List all customer organizations. */
  public Page<PartnerOrganization> list(String partnerId) {
    return list(partnerId, PartnerOrganizationListParams.builder().build(), RequestOptions.none());
  }

  /** List all customer organizations. */
  public Page<PartnerOrganization> list(String partnerId, RequestOptions options) {
    return list(partnerId, PartnerOrganizationListParams.builder().build(), options);
  }

  /** List all customer organizations. */
  public Page<PartnerOrganization> list(String partnerId, PartnerOrganizationListParams params) {
    return list(partnerId, params, RequestOptions.none());
  }

  /**
   * List all customer organizations.
   *
   * <p>Returns a list of the organizations you created. <code>grantActive</code> is <code>false
   * </code> once a customer disconnects your app. Requires the <code>partner:orgs:read</code>
   * scope.
   *
   * @param partnerId The partner account, <code>ptn_…</code>. Must be the partner the token was
   *     issued for.
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link PartnerOrganization}; {@code nextCursor()} is {@code null} on the
   *     last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<PartnerOrganization> list(
      String partnerId, PartnerOrganizationListParams params, RequestOptions options) {
    Objects.requireNonNull(partnerId, "partnerId");
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/partners/{partnerId}/organizations")
            .pathParam("partnerId", partnerId)
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .retrySafe()
            .build();
    return requester.requestPage(
        request, Pagination.cursor("cursor"), PartnerOrganization.class, options);
  }
}
