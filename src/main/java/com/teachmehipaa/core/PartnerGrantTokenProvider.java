package com.teachmehipaa.core;

import com.teachmehipaa.AppInfo;
import com.teachmehipaa.Environment;
import com.teachmehipaa.OAuth.GrantTokens;
import com.teachmehipaa.exception.TokenStoreException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Access tokens for one Grant, from its rotating Refresh token (ADR-0004).
 *
 * <p>Build one provider per Grant and share it between clients: its lock is what keeps two
 * exchanges of the same Refresh token from racing. Each rotated Refresh token goes to the {@link
 * TokenStore} before its Access token is used, and an exchange is never retried, because replaying
 * a Refresh token the endpoint may already have rotated revokes the Grant.
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
  private static final Duration REMINT_MARGIN = Duration.ofSeconds(60);

  private final ReentrantLock exchangeLock = new ReentrantLock();
  private final ClientConfig config;
  private final String authorization;
  private final TokenStore tokenStore;
  private final String scope;
  // Guarded by exchangeLock.
  private String latestRefreshToken;
  private volatile CachedToken current;

  private PartnerGrantTokenProvider(Builder builder) {
    this.config =
        new ClientConfig(
            builder.baseUrl != null ? builder.baseUrl : builder.environment.baseUrl(),
            null,
            builder.scopes,
            0,
            builder.timeout,
            builder.appInfo,
            builder.httpTransport != null ? builder.httpTransport : new JdkHttpTransport(),
            builder.clock,
            Sleeper.SYSTEM,
            null);
    this.authorization = TokenEndpoint.basicAuthorization(builder.clientId, builder.clientSecret);
    this.tokenStore = builder.tokenStore;
    // Leaving scope out keeps every Scope the Grant holds.
    this.scope = builder.scopes.isEmpty() ? null : String.join(" ", builder.scopes);
    this.latestRefreshToken = builder.refreshToken;
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
    CachedToken token = current;
    if (isFresh(token)) {
      return token.value();
    }
    exchangeLock.lock();
    try {
      // Whoever held the lock before us may already have exchanged.
      token = current;
      if (!isFresh(token)) {
        token = exchange();
        current = token;
      }
      return token.value();
    } finally {
      exchangeLock.unlock();
    }
  }

  @Override
  public void invalidate(String accessToken) {
    exchangeLock.lock();
    try {
      CachedToken token = current;
      if (token != null && token.value().equals(accessToken)) {
        current = null;
      }
    } finally {
      exchangeLock.unlock();
    }
  }

  private boolean isFresh(CachedToken token) {
    return token != null
        && config.clock().instant().isBefore(token.expiresAt().minus(REMINT_MARGIN));
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

  private record CachedToken(String value, Instant expiresAt) {}

  /** Collects settings for a {@link PartnerGrantTokenProvider}. Not thread-safe. */
  public static final class Builder {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(80);

    private String clientId;
    private String clientSecret;
    private TokenStore tokenStore;
    private String refreshToken;
    private List<String> scopes = List.of();
    private Environment environment = Environment.LIVE;
    private String baseUrl;
    private Duration timeout = DEFAULT_TIMEOUT;
    private AppInfo appInfo;
    private HttpTransport httpTransport;
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
      this.environment = Objects.requireNonNull(environment, "environment");
      return this;
    }

    /** Overrides the Environment's base URL, for example to point at a mock server. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = BaseUrls.normalize(baseUrl);
      return this;
    }

    /** How long to wait for the token endpoint. Defaults to 80 seconds. */
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

    private static String requireNonBlank(String value, String name) {
      Objects.requireNonNull(value, name);
      if (value.isBlank()) {
        throw new IllegalArgumentException(name + " must not be blank");
      }
      return value;
    }
  }
}
