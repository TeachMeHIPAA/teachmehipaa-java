package com.teachmehipaa;

import com.teachmehipaa.core.BaseUrls;
import com.teachmehipaa.core.ClientConfig;
import com.teachmehipaa.core.GrantExchange;
import com.teachmehipaa.core.HttpTransport;
import com.teachmehipaa.core.JdkHttpTransport;
import com.teachmehipaa.core.Sleeper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helpers for connecting Organizations to a Partner app: PKCE pairs, the consent URL and the code
 * exchange. They need no client.
 *
 * <pre>{@code
 * OAuth.PkcePair pkce = OAuth.createPkcePair();
 * String state = OAuth.createState();
 * String consentUrl = OAuth.authorizeUrl(OAuth.AuthorizeUrlParams.builder()
 *     .clientId("app_...")
 *     .redirectUri("https://partner.example.com/oauth/callback")
 *     .scopes(List.of("learners:read", "learners:write"))
 *     .state(state)
 *     .codeChallenge(pkce.codeChallenge())
 *     .build());
 * }</pre>
 */
public final class OAuth {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

  // The Sandbox console's consent URL is not published yet, so Sandbox callers pass
  // authorizationUrl rather than the library guessing a host.
  private static final String LIVE_AUTHORIZATION_URL =
      "https://console.teachmehipaa.com/oauth/authorize";

  /**
   * A PKCE code verifier and the challenge to put on the consent URL.
   *
   * @param codeVerifier keep this server-side until the code exchange; never send it to the browser
   * @param codeChallenge the S256 hash of the verifier, for {@link AuthorizeUrlParams}
   * @param method always {@code S256}, the only method TeachMeHIPAA accepts
   */
  public record PkcePair(String codeVerifier, String codeChallenge, String method) {
    public PkcePair {
      Objects.requireNonNull(codeVerifier, "codeVerifier");
      Objects.requireNonNull(codeChallenge, "codeChallenge");
      Objects.requireNonNull(method, "method");
    }

    @Override
    public String toString() {
      return "PkcePair[codeChallenge=" + codeChallenge + ", method=" + method + "]";
    }
  }

  /** A fresh code verifier and its S256 code challenge. */
  public static PkcePair createPkcePair() {
    String codeVerifier = randomToken();
    byte[] digest = sha256().digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
    return new PkcePair(codeVerifier, BASE64URL.encodeToString(digest), "S256");
  }

  /** A random {@code state} that binds the consent redirect to the admin's session. */
  public static String createState() {
    return randomToken();
  }

  /**
   * The URL to send an Organization admin to, to grant your Partner app access.
   *
   * @throws IllegalArgumentException when the Environment has no built-in consent URL and no {@code
   *     authorizationUrl} was given
   */
  public static String authorizeUrl(AuthorizeUrlParams params) {
    Objects.requireNonNull(params, "params");
    String base = params.authorizationUrl;
    if (base == null) {
      if (params.environment != Environment.LIVE) {
        throw new IllegalArgumentException(
            "pass authorizationUrl: the " + params.environment + " consent URL is not built in");
      }
      base = LIVE_AUTHORIZATION_URL;
    }
    Map<String, String> query = new LinkedHashMap<>();
    query.put("response_type", "code");
    query.put("client_id", params.clientId);
    query.put("redirect_uri", params.redirectUri);
    query.put("scope", String.join(" ", params.scopes));
    query.put("state", params.state);
    query.put("code_challenge", params.codeChallenge);
    query.put("code_challenge_method", "S256");
    return base + (base.contains("?") ? "&" : "?") + encode(query);
  }

  /**
   * Exchanges the consent redirect's code for the Grant's first tokens, exactly once. Save the
   * Refresh token in your {@link com.teachmehipaa.core.TokenStore} before you use the Access token.
   *
   * @throws com.teachmehipaa.exception.OAuthException when the token endpoint refuses the code
   * @throws com.teachmehipaa.exception.ApiConnectionException when no response arrived; the code
   *     may have been consumed, so re-run consent rather than retrying
   */
  public static GrantTokens exchangeCode(ExchangeCodeParams params) {
    Objects.requireNonNull(params, "params");
    ClientConfig config =
        new ClientConfig(
            params.baseUrl != null ? params.baseUrl : params.environment.baseUrl(),
            null,
            List.of(),
            0,
            params.timeout,
            params.appInfo,
            params.httpTransport != null ? params.httpTransport : new JdkHttpTransport(),
            Clock.systemUTC(),
            Sleeper.SYSTEM,
            null);
    return GrantExchange.exchangeCode(
        config,
        params.clientId,
        params.clientSecret,
        params.code,
        params.redirectUri,
        params.codeVerifier);
  }

  /**
   * What a Grant's token exchange returns. {@link #toString()} leaves the tokens out.
   *
   * @param accessToken the Access token for the Grant's Organization
   * @param refreshToken replaces the one exchanged; save it before anything else
   * @param expiresIn how long the Access token lasts from when it was requested
   * @param scopes the Scopes the Access token carries
   */
  public record GrantTokens(
      String accessToken, String refreshToken, Duration expiresIn, List<String> scopes) {
    public GrantTokens {
      Objects.requireNonNull(accessToken, "accessToken");
      Objects.requireNonNull(refreshToken, "refreshToken");
      Objects.requireNonNull(expiresIn, "expiresIn");
      scopes = List.copyOf(scopes);
    }

    @Override
    public String toString() {
      return "GrantTokens[expiresIn=" + expiresIn + ", scopes=" + scopes + "]";
    }
  }

  /** What {@link #exchangeCode} sends. Immutable. */
  public static final class ExchangeCodeParams {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(80);

    private final String clientId;
    private final String clientSecret;
    private final String code;
    private final String redirectUri;
    private final String codeVerifier;
    private final Environment environment;
    private final String baseUrl;
    private final Duration timeout;
    private final AppInfo appInfo;
    private final HttpTransport httpTransport;

    private ExchangeCodeParams(Builder builder) {
      this.clientId = builder.clientId;
      this.clientSecret = builder.clientSecret;
      this.code = builder.code;
      this.redirectUri = builder.redirectUri;
      this.codeVerifier = builder.codeVerifier;
      this.environment = builder.environment;
      this.baseUrl = builder.baseUrl;
      this.timeout = builder.timeout;
      this.appInfo = builder.appInfo;
      this.httpTransport = builder.httpTransport;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private String clientId;
      private String clientSecret;
      private String code;
      private String redirectUri;
      private String codeVerifier;
      private Environment environment = Environment.LIVE;
      private String baseUrl;
      private Duration timeout = DEFAULT_TIMEOUT;
      private AppInfo appInfo;
      private HttpTransport httpTransport;

      private Builder() {}

      /** Your Partner app's id ({@code app_…}). Required. */
      public Builder clientId(String clientId) {
        this.clientId = requireNonBlank(clientId, "clientId");
        return this;
      }

      /** Your Partner app's secret ({@code tmh_as_…}). Required. */
      public Builder clientSecret(String clientSecret) {
        this.clientSecret = requireNonBlank(clientSecret, "clientSecret");
        return this;
      }

      /** The {@code code} from the redirect. It works once, within 10 minutes. Required. */
      public Builder code(String code) {
        this.code = requireNonBlank(code, "code");
        return this;
      }

      /** The same redirect URI the consent URL carried. Required. */
      public Builder redirectUri(String redirectUri) {
        this.redirectUri = requireNonBlank(redirectUri, "redirectUri");
        return this;
      }

      /** The verifier of the {@link PkcePair} behind the consent URL. Required. */
      public Builder codeVerifier(String codeVerifier) {
        this.codeVerifier = requireNonBlank(codeVerifier, "codeVerifier");
        return this;
      }

      /** Which deployment the Partner app belongs to. Defaults to {@link Environment#LIVE}. */
      public Builder environment(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
        return this;
      }

      /** Overrides the Environment's base URL, for example to point at a mock server. */
      public Builder baseUrl(String baseUrl) {
        this.baseUrl = BaseUrls.normalize(baseUrl);
        return this;
      }

      /** How long to wait for the response. Defaults to 80 seconds. */
      public Builder timeout(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) {
          throw new IllegalArgumentException("timeout must be positive");
        }
        this.timeout = timeout;
        return this;
      }

      /** Your integration's name, version and URL, sent in the {@code User-Agent}. */
      public Builder appInfo(AppInfo appInfo) {
        this.appInfo = Objects.requireNonNull(appInfo, "appInfo");
        return this;
      }

      /** Replaces the HTTP layer. Defaults to a {@link JdkHttpTransport}. */
      public Builder httpTransport(HttpTransport httpTransport) {
        this.httpTransport = Objects.requireNonNull(httpTransport, "httpTransport");
        return this;
      }

      /**
       * @throws IllegalStateException when a required setting is missing
       */
      public ExchangeCodeParams build() {
        requireSet(clientId, "clientId");
        requireSet(clientSecret, "clientSecret");
        requireSet(code, "code");
        requireSet(redirectUri, "redirectUri");
        requireSet(codeVerifier, "codeVerifier");
        return new ExchangeCodeParams(this);
      }
    }
  }

  /** What {@link #authorizeUrl} puts on the consent URL. Immutable. */
  public static final class AuthorizeUrlParams {
    private final String clientId;
    private final String redirectUri;
    private final List<String> scopes;
    private final String state;
    private final String codeChallenge;
    private final Environment environment;
    private final String authorizationUrl;

    private AuthorizeUrlParams(Builder builder) {
      this.clientId = builder.clientId;
      this.redirectUri = builder.redirectUri;
      this.scopes = builder.scopes;
      this.state = builder.state;
      this.codeChallenge = builder.codeChallenge;
      this.environment = builder.environment;
      this.authorizationUrl = builder.authorizationUrl;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private String clientId;
      private String redirectUri;
      private List<String> scopes;
      private String state;
      private String codeChallenge;
      private Environment environment = Environment.LIVE;
      private String authorizationUrl;

      private Builder() {}

      /** Your Partner app's id ({@code app_…}). Required. */
      public Builder clientId(String clientId) {
        this.clientId = requireNonBlank(clientId, "clientId");
        return this;
      }

      /** One of the app's registered redirect URIs, matched exactly. Required. */
      public Builder redirectUri(String redirectUri) {
        this.redirectUri = requireNonBlank(redirectUri, "redirectUri");
        return this;
      }

      /** The Scopes to ask the admin for. Required. */
      public Builder scopes(Collection<String> scopes) {
        this.scopes = List.copyOf(scopes);
        return this;
      }

      /**
       * Echoed back on the redirect; check it matches before exchanging the code. {@link
       * #createState()} makes one. Required.
       */
      public Builder state(String state) {
        this.state = requireNonBlank(state, "state");
        return this;
      }

      /** From {@link #createPkcePair()}. Required. */
      public Builder codeChallenge(String codeChallenge) {
        this.codeChallenge = requireNonBlank(codeChallenge, "codeChallenge");
        return this;
      }

      /**
       * Which deployment's console to use. Defaults to {@link Environment#LIVE}; Sandbox has no
       * built-in consent URL, so it needs {@link #authorizationUrl}.
       */
      public Builder environment(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
        return this;
      }

      /** Overrides the Environment's consent URL. */
      public Builder authorizationUrl(String authorizationUrl) {
        this.authorizationUrl = requireAbsoluteUrl(authorizationUrl, "authorizationUrl");
        return this;
      }

      /**
       * @throws IllegalStateException when a required setting is missing
       */
      public AuthorizeUrlParams build() {
        requireSet(clientId, "clientId");
        requireSet(redirectUri, "redirectUri");
        requireSet(scopes, "scopes");
        // Without state, a forged redirect could attach an attacker's Grant to the partner's
        // session (RFC 6749 §10.12).
        requireSet(state, "state");
        requireSet(codeChallenge, "codeChallenge");
        return new AuthorizeUrlParams(this);
      }
    }
  }

  // 32 bytes encode to 43 base64url characters: the shortest verifier RFC 7636 allows, carrying
  // the full 256 bits it recommends.
  private static String randomToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return BASE64URL.encodeToString(bytes);
  }

  private static MessageDigest sha256() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException("every Java runtime provides SHA-256", impossible);
    }
  }

  // Spaces become %20: form encoding's + is only read back as a space by form parsers.
  private static String encode(Map<String, String> query) {
    return query.entrySet().stream()
        .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
        .collect(Collectors.joining("&"));
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  private static String requireAbsoluteUrl(String value, String name) {
    Objects.requireNonNull(value, name);
    URI uri = URI.create(value);
    if (!uri.isAbsolute() || uri.getHost() == null) {
      throw new IllegalArgumentException(name + " must be an absolute http(s) URL");
    }
    return value;
  }

  private static void requireSet(Object value, String name) {
    if (value == null) {
      throw new IllegalStateException(name + " is required");
    }
  }

  private OAuth() {}
}
