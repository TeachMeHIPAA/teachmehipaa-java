package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.OAuth.GrantTokens;
import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.ApiTimeoutException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * One {@code authorization_code} or {@code refresh_token} exchange, sent exactly once. Internal:
 * {@code OAuth} and {@link PartnerGrantTokenProvider} call it.
 *
 * <p>Replaying a spent code or a superseded Refresh token revokes the Grant (ADR-0004), so a
 * failure is raised with guidance instead of retried, whatever the client's retry settings.
 */
public final class GrantExchange {
  private static final String CODE_NETWORK_FAILURE =
      "The code exchange was not retried: the code may have been consumed, and a code works only"
          + " once. Re-run consent to get a new code.";
  private static final String REFRESH_NETWORK_FAILURE =
      "The Refresh token exchange was not retried, because replaying a Refresh token the endpoint"
          + " may already have rotated revokes the Grant. If the next call fails with"
          + " invalid_grant, re-run consent for this Organization.";
  private static final String REFRESH_INVALID_GRANT =
      "The Grant was revoked, or its Refresh token was reused (presenting a superseded Refresh"
          + " token revokes the Grant). Re-run consent for the Organization.";

  /**
   * Exchanges the consent redirect's code for the Grant's first tokens.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the exchange fails
   */
  public static GrantTokens exchangeCode(
      ClientConfig config,
      String clientId,
      String clientSecret,
      String code,
      String redirectUri,
      String codeVerifier) {
    String form =
        "grant_type=authorization_code&code="
            + TokenEndpoint.formEncode(code)
            + "&redirect_uri="
            + TokenEndpoint.formEncode(redirectUri)
            + "&code_verifier="
            + TokenEndpoint.formEncode(codeVerifier);
    return exchange(
        config,
        TokenEndpoint.basicAuthorization(clientId, clientSecret),
        form,
        CODE_NETWORK_FAILURE,
        null);
  }

  /** Exchanges a Refresh token for an Access token and the Refresh token that replaces it. */
  static GrantTokens refresh(
      ClientConfig config, String authorization, String refreshToken, String scope) {
    String form =
        "grant_type=refresh_token&refresh_token=" + TokenEndpoint.formEncode(refreshToken);
    if (scope != null) {
      form += "&scope=" + TokenEndpoint.formEncode(scope);
    }
    return exchange(config, authorization, form, REFRESH_NETWORK_FAILURE, REFRESH_INVALID_GRANT);
  }

  private static GrantTokens exchange(
      ClientConfig config,
      String authorization,
      String form,
      String networkFailure,
      String invalidGrant) {
    TransportResponse response;
    try {
      response = Transport.send(config, TokenEndpoint.request(config, authorization, form));
    } catch (ApiTimeoutException noResponse) {
      throw new ApiTimeoutException(
          Errors.withGuidance(noResponse.getMessage(), networkFailure), noResponse.getCause());
    } catch (ApiConnectionException noResponse) {
      throw new ApiConnectionException(
          Errors.withGuidance(noResponse.getMessage(), networkFailure), noResponse.getCause());
    }
    if (!Errors.isSuccess(response.statusCode())) {
      throw Errors.fromOAuthError(response, config.clock().instant(), invalidGrant);
    }
    if (!Errors.isJson(response)) {
      throw Errors.fromResponse(response, config.clock().instant());
    }
    return read(response);
  }

  private static GrantTokens read(TransportResponse response) {
    JsonNode body;
    try {
      body = Json.MAPPER.readTree(response.body());
    } catch (IOException notJson) {
      throw notAGrant(response);
    }
    if (body == null
        || !body.path("access_token").isTextual()
        || !body.path("refresh_token").isTextual()
        || body.path("refresh_token").asText().isEmpty()
        || !body.path("expires_in").isNumber()) {
      throw notAGrant(response);
    }
    JsonNode scope = body.path("scope");
    List<String> scopes =
        scope.isTextual() && !scope.asText().isBlank()
            ? Arrays.asList(scope.asText().trim().split("\\s+"))
            : List.of();
    return new GrantTokens(
        body.get("access_token").asText(),
        body.get("refresh_token").asText(),
        Duration.ofSeconds(body.get("expires_in").asLong()),
        scopes);
  }

  // No body: it may hold a live Access token, which must not reach error reporters.
  private static ApiException notAGrant(TransportResponse response) {
    return new ApiException(
        "The token endpoint answered without an access_token, refresh_token and expires_in",
        Errors.withoutBody(response).build());
  }

  private GrantExchange() {}
}
