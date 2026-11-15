package com.teachmehipaa.core;

import static com.teachmehipaa.core.Arguments.requireNonBlank;

import com.teachmehipaa.AppInfo;
import com.teachmehipaa.Environment;
import com.teachmehipaa.OAuth.GrantTokens;
import com.teachmehipaa.core.AccessTokenCache.CachedToken;
import com.teachmehipaa.exception.TokenStoreException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Access tokens for one Grant, from its rotating Refresh token (ADR-0004).
 *
 * <p>Build one provider per Grant and share it between clients: its single-flight cache is what
 * keeps two exchanges of the same Refresh token from racing. Each rotated Refresh token goes to the
 * {@link TokenStore} before its Access token is used, and an exchange is never retried, because
 * replaying a Refresh token the endpoint may already have rotated revokes the Grant.
 *
 * <pre>{@code
 * TokenProvider tokens = PartnerGrantTokenProvider.builder()
 *     .clientId("app_...")
 *     .clientSecret("tmh_as_...")
 *     .tokenStore(store)
 *     .build();
 * TeachMeHipaaClient client = TeachMeHipaaClient.builder().tokenProvider(tokens).build();
 * }</pre>
 */
public final class PartnerGrantTokenProvider implements TokenProvider {
  private final AccessTokenCache cache;
  private final ClientConfig config;
  private final String authorization;
  private final TokenStore tokenStore;
  private final String scope;
  // Touched only by exchange(), which the cache runs under its lock.
  private String latestRefreshToken;

  private PartnerGrantTokenProvider(Builder builder) {
    this.config = builder.connection.tokenEndpointConfig(builder.scopes, builder.clock);
    this.authorization = TokenEndpoint.basicAuthorization(builder.clientId, builder.clientSecret);
    this.tokenStore = builder.tokenStore;
    // Leaving scope out keeps every Scope the Grant holds.
    this.scope = builder.scopes.isEmpty() ? null : String.join(" ", builder.scopes);
    this.latestRefreshToken = builder.refreshToken;
    this.cache = new AccessTokenCache(config.clock(), this::exchange);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Where this provider exchanges Refresh tokens, without a trailing slash. A client given this
   * provider must call the same base URL.
   */
  public String baseUrl() {
    return config.baseUrl();
  }

  /**
   * @throws com.teachmehipaa.exception.OAuthException when the token endpoint refuses the Refresh
   *     token; {@code invalid_grant} means consent must be re-run
   * @throws com.teachmehipaa.exception.ApiConnectionException when no response arrived; the
   *     exchange is not retried
   * @throws TokenStoreException when the token store fails; the new Access token is not used
   */
  @Override
  public String accessToken() {
    return cache.accessToken();
  }

  @Override
  public void invalidate(String accessToken) {
    cache.invalidate(accessToken);
  }

  private CachedToken exchange() {
    String refreshToken = latestRefreshToken != null ? latestRefreshToken : tokenStore.load();
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new TokenStoreException("tokenStore.load() returned no Refresh token", null);
    }
    // Expiry counts from when the request left, so a slow response can't stretch the token past
    // its real lifetime.
    Instant requestedAt = config.clock().instant();
    GrantTokens grant = GrantExchange.refresh(config, authorization, refreshToken, scope);
    // The token just presented is superseded whatever happens next, so this process must never
    // present it again, even if the save fails.
    latestRefreshToken = grant.refreshToken();
    try {
      tokenStore.save(grant.refreshToken());
    } catch (RuntimeException failure) {
      // The store didn't record the rotation, so the next process would load and replay the
      // superseded token and revoke the Grant. Using the new Access token would hide that until
      // it's too late.
      throw new TokenStoreException(
          "tokenStore.save() failed, so the new Access token was not used. Until a save"
              + " succeeds, your store holds a superseded Refresh token, and exchanging it would"
              + " revoke the Grant.",
          failure);
    }
    return new CachedToken(grant.accessToken(), requestedAt.plus(grant.expiresIn()));
  }

  /** Collects settings for a {@link PartnerGrantTokenProvider}. Not thread-safe. */
  public static final class Builder {
    private String clientId;
    private String clientSecret;
    private TokenStore tokenStore;
    private String refreshToken;
    private List<String> scopes = List.of();
    private ConnectionSettings connection = ConnectionSettings.DEFAULTS;
    private Clock clock = Clock.systemUTC();

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

    /** Receives every rotated Refresh token. Required. */
    public Builder tokenStore(TokenStore tokenStore) {
      this.tokenStore = Objects.requireNonNull(tokenStore, "tokenStore");
      return this;
    }

    /**
     * The Refresh token for the first exchange, for example straight from {@code
     * OAuth.exchangeCode}. When omitted, the first exchange loads it from the token store.
     */
    public Builder refreshToken(String refreshToken) {
      this.refreshToken = requireNonBlank(refreshToken, "refreshToken");
      return this;
    }

    /** Narrows the Access token to these Scopes; it can never widen them. */
    public Builder scopes(String... scopes) {
      return scopes(Arrays.asList(scopes));
    }

    /** Narrows the Access token to these Scopes; it can never widen them. */
    public Builder scopes(Collection<String> scopes) {
      this.scopes = List.copyOf(scopes);
      return this;
    }

    /**
     * Which deployment the Partner app belongs to. Defaults to {@link Environment#LIVE}. A client
     * given this provider must use the same Environment and base URL.
     */
    public Builder environment(Environment environment) {
      connection = connection.withEnvironment(environment);
      return this;
    }

    /** Overrides the Environment's base URL, for example to point at a mock server. */
    public Builder baseUrl(String baseUrl) {
      connection = connection.withBaseUrl(baseUrl);
      return this;
    }

    /** How long to wait for the token endpoint. Defaults to 80 seconds. */
    public Builder timeout(Duration timeout) {
      connection = connection.withTimeout(timeout);
      return this;
    }

    /** Your software's name, version and URL, sent in the {@code User-Agent}. */
    public Builder appInfo(AppInfo appInfo) {
      connection = connection.withAppInfo(appInfo);
      return this;
    }

    /** Replaces the HTTP layer. Defaults to a {@link JdkHttpTransport}. */
    public Builder httpTransport(HttpTransport httpTransport) {
      connection = connection.withHttpTransport(httpTransport);
      return this;
    }

    /** Tells the time for token expiry. For tests; defaults to the system clock. */
    public Builder clock(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock");
      return this;
    }

    /**
     * @throws IllegalStateException when the client id, secret or token store is missing
     */
    public PartnerGrantTokenProvider build() {
      if (clientId == null || clientSecret == null || tokenStore == null) {
        throw new IllegalStateException("clientId, clientSecret and tokenStore are required");
      }
      return new PartnerGrantTokenProvider(this);
    }
  }
}
