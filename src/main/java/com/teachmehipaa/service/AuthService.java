// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.TokenIntrospection;
import java.util.Objects;

/** Auth. Reach it through {@code client.auth()}. */
public final class AuthService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public AuthService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Retrieve the current token. */
  public TokenIntrospection introspect() {
    return introspect(RequestOptions.none());
  }

  /**
   * Retrieve the current token.
   *
   * <p>Describes the access token you call with: whether it belongs to an API client or a person,
   * its organization, its scopes and when it expires. Needs no scope.
   *
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link TokenIntrospection}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public TokenIntrospection introspect(RequestOptions options) {
    Objects.requireNonNull(options, "options");
    ApiRequest request = ApiRequest.builder("GET", "/auth/introspection").retrySafe().build();
    return requester.request(request, TokenIntrospection.class, options);
  }
}
