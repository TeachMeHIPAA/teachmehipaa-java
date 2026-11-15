package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.APP_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class OAuthConsentTest {

  static final String REDIRECT_URI = "https://partner.example.com/oauth/callback?tenant=7";
  // RFC 7636 appendix B.
  static final String RFC_VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
  static final String RFC_CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

  private static final Pattern UNRESERVED_43_TO_128 = Pattern.compile("[A-Za-z0-9._~-]{43,128}");
  private static final Pattern BASE64URL_43 = Pattern.compile("[A-Za-z0-9_-]{43}");

  /** RFC 7636 §4.2, computed here rather than by the SDK. */
  private static String s256(String codeVerifier) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
  }

  private static Map<String, String> query(URI uri) {
    Map<String, String> params = new LinkedHashMap<>();
    for (String pair : uri.getRawQuery().split("&")) {
      String[] parts = pair.split("=", 2);
      params.put(
          URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
          URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
    }
    return params;
  }

  private static OAuth.AuthorizeUrlParams.Builder consent() {
    return OAuth.AuthorizeUrlParams.builder()
        .clientId(APP_ID)
        .redirectUri(REDIRECT_URI)
        .scopes(List.of("learners:read", "learners:write"))
        .state("af0ifjsldkj")
        .codeChallenge(RFC_CHALLENGE);
  }

  @Test
  void theCodeChallengeIsTheS256HashOfTheCodeVerifier() throws Exception {
    // The independent hash agrees with the RFC's worked example, so it can judge the SDK's.
    assertEquals(RFC_CHALLENGE, s256(RFC_VERIFIER));

    OAuth.PkcePair pair = OAuth.createPkcePair();

    assertEquals("S256", pair.method());
    assertTrue(UNRESERVED_43_TO_128.matcher(pair.codeVerifier()).matches(), pair.codeVerifier());
    assertTrue(BASE64URL_43.matcher(pair.codeChallenge()).matches(), pair.codeChallenge());
    assertEquals(s256(pair.codeVerifier()), pair.codeChallenge());
  }

  @Test
  void everyPkcePairHasAFreshCodeVerifier() {
    assertNotEquals(OAuth.createPkcePair().codeVerifier(), OAuth.createPkcePair().codeVerifier());
  }

  @Test
  void statesAreHardToGuess() {
    String state = OAuth.createState();

    assertTrue(BASE64URL_43.matcher(state).matches(), state);
    assertNotEquals(state, OAuth.createState());
  }

  @Test
  void theConsentUrlCarriesClientRedirectScopesStateAndS256() {
    URI url = URI.create(OAuth.authorizeUrl(consent().build()));

    assertEquals(
        "https://console.teachmehipaa.com/oauth/authorize",
        url.getScheme() + "://" + url.getHost() + url.getPath());
    assertEquals(
        Map.of(
            "response_type", "code",
            "client_id", APP_ID,
            "redirect_uri", REDIRECT_URI,
            "scope", "learners:read learners:write",
            "state", "af0ifjsldkj",
            "code_challenge", RFC_CHALLENGE,
            "code_challenge_method", "S256"),
        query(url));
  }

  @Test
  void theConsentUrlEncodesSpacesInTheScopeAsPercent20() {
    assertTrue(
        OAuth.authorizeUrl(consent().build()).contains("scope=learners%3Aread%20learners%3Awrite"));
  }

  @Test
  void aConsentUrlNeedsAState() {
    assertThrows(IllegalArgumentException.class, () -> consent().state(" "));
    OAuth.AuthorizeUrlParams.Builder withoutState =
        OAuth.AuthorizeUrlParams.builder()
            .clientId(APP_ID)
            .redirectUri(REDIRECT_URI)
            .scopes(List.of("learners:read"))
            .codeChallenge(RFC_CHALLENGE);

    IllegalStateException missing = assertThrows(IllegalStateException.class, withoutState::build);
    assertTrue(missing.getMessage().contains("state"), missing.getMessage());
  }

  @Test
  void sandboxConsentNeedsAnExplicitAuthorizationUrl() {
    IllegalArgumentException refused =
        assertThrows(
            IllegalArgumentException.class,
            () -> OAuth.authorizeUrl(consent().environment(Environment.SANDBOX).build()));
    assertTrue(refused.getMessage().contains("authorizationUrl"), refused.getMessage());

    URI url =
        URI.create(
            OAuth.authorizeUrl(
                consent()
                    .environment(Environment.SANDBOX)
                    .authorizationUrl("https://console.example.test/oauth/authorize")
                    .build()));

    assertEquals("console.example.test", url.getHost());
    assertEquals("/oauth/authorize", url.getPath());
    assertEquals("S256", query(url).get("code_challenge_method"));
  }
}
