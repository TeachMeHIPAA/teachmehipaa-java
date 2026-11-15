// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.Partner;
import java.util.Objects;

/** Partners. Reach it through {@code client.partners()}. */
public final class PartnerService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public PartnerService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Organizations. */
  public PartnerOrganizationService organizations() {
    return new PartnerOrganizationService(requester);
  }

  /** Retrieve your partner account. */
  public Partner retrieve(String partnerId) {
    return retrieve(partnerId, RequestOptions.none());
  }

  /**
   * Retrieve your partner account.
   *
   * <p>Retrieves your partner account, including how many organizations you can create. Requires
   * the <code>partner:orgs:read</code> scope.
   *
   * @param partnerId The partner account, <code>ptn_…</code>. Must be the partner the token was
   *     issued for.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Partner}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Partner retrieve(String partnerId, RequestOptions options) {
    Objects.requireNonNull(partnerId, "partnerId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/partners/{partnerId}")
            .pathParam("partnerId", partnerId)
            .retrySafe()
            .build();
    return requester.request(request, Partner.class, options);
  }
}
