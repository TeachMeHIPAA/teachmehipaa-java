package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.APP_BASIC_CREDENTIALS;
import static com.teachmehipaa.Fixtures.APP_ID;
import static com.teachmehipaa.Fixtures.APP_SECRET;
import static com.teachmehipaa.Fixtures.REFRESH_TOKEN_1;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.grantTokenJson;
import static com.teachmehipaa.Fixtures.oauthError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.OAuthException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class OAuthCodeExchangeTest {
  private static final String REDIRECT_URI = "https://partner.example.com/oauth/callback?tenant=7";
  private static final String VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
  private static final String CODE = "SplxlOBeZQQYbYS6WxSbIA";

  private static OAuth.ExchangeCodeParams.Builder exchange(FakeTransport api) {
    return OAuth.ExchangeCodeParams.builder()
        .clientId(APP_ID)
        .clientSecret(APP_SECRET)
        .code(CODE)
        .redirectUri(REDIRECT_URI)
        .codeVerifier(VERIFIER)
        .httpTransport(api);
  }

  @Test
  void theCodeExchangeReturnsTheGrantTokens() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_1)));

    OAuth.GrantTokens grant = OAuth.exchangeCode(exchange(api).build());

    assertEquals(
        new OAuth.GrantTokens(
            "access-org",
            REFRESH_TOKEN_1,
            Duration.ofSeconds(900),
            List.of("learners:read", "learners:write")),
        grant);
    TransportRequest request = api.requests().get(0);
    assertEquals("https://api.tme.so/v1/auth/token", request.uri().toString());
    assertEquals("Basic " + APP_BASIC_CREDENTIALS, request.headers().get("authorization"));
    assertEquals("application/x-www-form-urlencoded", request.headers().get("content-type"));
    assertEquals(
        "grant_type=authorization_code&code=SplxlOBeZQQYbYS6WxSbIA"
            + "&redirect_uri=https%3A%2F%2Fpartner.example.com%2Foauth%2Fcallback%3Ftenant%3D7"
            + "&code_verifier=dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",
        new String(request.body(), StandardCharsets.UTF_8));
  }

  @Test
  void grantTokensKeepTheirSecretsOutOfToString() {
    OAuth.GrantTokens grant =
        new OAuth.GrantTokens("access-org", REFRESH_TOKEN_1, Duration.ofSeconds(900), List.of());

    assertFalse(grant.toString().contains("access-org"), grant.toString());
    assertFalse(grant.toString().contains(REFRESH_TOKEN_1), grant.toString());
  }

  @Test
  void theCodeExchangeGoesToTheSandboxTokenEndpointWhenAsked() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-org", REFRESH_TOKEN_1)));

    OAuth.exchangeCode(exchange(api).environment(Environment.SANDBOX).build());

    assertEquals(
        "https://api-teachmehipaa-dev.web.app/v1/auth/token",
        api.requests().get(0).uri().toString());
  }

  @Test
  void aRefusedCodeRaisesAnOAuthException() {
    FakeTransport api =
        new FakeTransport()
            .on(TOKEN_ROUTE, oauthError(400, "invalid_grant", "The code has expired or was used"));

    OAuthException error =
        assertThrows(OAuthException.class, () -> OAuth.exchangeCode(exchange(api).build()));

    assertEquals("invalid_grant", error.getCode());
    assertEquals(400, error.getStatusCode());
  }

  @Test
  void aNetworkFailureIsNeverRetriedAndSaysTheCodeMayBeConsumed() {
    ConnectException refused = new ConnectException("Connection refused");
    FakeTransport api = new FakeTransport().fail(TOKEN_ROUTE, refused);

    ApiConnectionException error =
        assertThrows(ApiConnectionException.class, () -> OAuth.exchangeCode(exchange(api).build()));

    assertTrue(error.getMessage().contains("consumed"), error.getMessage());
    assertTrue(error.getMessage().contains("consent"), error.getMessage());
    assertEquals(refused, error.getCause());
    assertEquals(1, api.requests().size());
  }

  @Test
  void aServerErrorIsNeverRetriedEither() {
    FakeTransport api =
        new FakeTransport().on(TOKEN_ROUTE, oauthError(503, "temporarily_unavailable", "Later"));

    assertThrows(OAuthException.class, () -> OAuth.exchangeCode(exchange(api).build()));

    assertEquals(1, api.requests().size());
  }

  @Test
  void aTokenResponseWithoutARefreshTokenRaisesWithoutExposingTheBody() {
    FakeTransport api =
        new FakeTransport()
            .on(
                TOKEN_ROUTE,
                Reply.json(
                    """
                    {"access_token": "access-org", "token_type": "Bearer", "expires_in": 900}
                    """));

    ApiException error =
        assertThrows(ApiException.class, () -> OAuth.exchangeCode(exchange(api).build()));

    assertEquals(200, error.getStatusCode());
    assertNull(error.getRawBody());
  }

  @Test
  void theExchangeNeedsEveryRequiredSetting() {
    assertThrows(
        IllegalStateException.class,
        () ->
            OAuth.ExchangeCodeParams.builder()
                .clientId(APP_ID)
                .clientSecret(APP_SECRET)
                .code(CODE)
                .redirectUri(REDIRECT_URI)
                .build());
  }
}
