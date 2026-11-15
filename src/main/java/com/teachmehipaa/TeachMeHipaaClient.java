package com.teachmehipaa;

import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.ClientConfig;
import com.teachmehipaa.core.ClientCredentialsTokenProvider;
import com.teachmehipaa.core.HttpTransport;
import com.teachmehipaa.core.JdkHttpTransport;
import com.teachmehipaa.core.Sleeper;
import com.teachmehipaa.service.AuditEventService;
import com.teachmehipaa.service.AuthService;
import com.teachmehipaa.service.CertificateService;
import com.teachmehipaa.service.CertificateVerificationService;
import com.teachmehipaa.service.ComplianceService;
import com.teachmehipaa.service.CourseService;
import com.teachmehipaa.service.EnrollmentService;
import com.teachmehipaa.service.EventService;
import com.teachmehipaa.service.LearnerService;
import com.teachmehipaa.service.MemberService;
import com.teachmehipaa.service.OperationService;
import com.teachmehipaa.service.PartnerService;
import com.teachmehipaa.service.PaymentMethodService;
import com.teachmehipaa.service.SeatOrderService;
import com.teachmehipaa.service.SeatService;
import com.teachmehipaa.service.WebhookSubscriptionService;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A client for one Organization's view of the TeachMeHIPAA Developer API.
 *
 * <p>Build one per API client and share it: it mints, caches and renews Access tokens itself, and
 * discovers the Organization the credential is bound to unless {@code organizationId} is given. Its
 * configuration is fixed when it is built (ADR-0002), and it is safe to use from many threads.
 *
 * <pre>{@code
 * TeachMeHipaaClient client = TeachMeHipaaClient.builder()
 *     .clientId("cli_...")
 *     .clientSecret("tmh_cs_...")
 *     .environment(Environment.SANDBOX)
 *     .build();
 * Learner learner = client.learners().retrieve("sub_...");
 * }</pre>
 */
public final class TeachMeHipaaClient {
  private static final Webhooks WEBHOOKS = new Webhooks();

  private final Environment environment;
  private final ClientConfig config;
  private final ApiRequester requester;

  private TeachMeHipaaClient(Builder builder, ClientConfig config) {
    this.environment = builder.environment;
    this.config = config;
    this.requester =
        new ApiRequester(
            config,
            new ClientCredentialsTokenProvider(builder.clientId, builder.clientSecret, config));
  }

  public static Builder builder() {
    return new Builder();
  }

  // Services hold nothing but the requester, so each accessor call makes a fresh one, as in
  // stripe-java.
  // The beginning of the section generated from openapi/sdk-ir.json.

  /** Audit events in the client's Organization. */
  public AuditEventService auditEvents() {
    return new AuditEventService(requester);
  }

  /** Auth. */
  public AuthService auth() {
    return new AuthService(requester);
  }

  /** Certificate verifications. */
  public CertificateVerificationService certificateVerifications() {
    return new CertificateVerificationService(requester);
  }

  /** Certificates in the client's Organization. */
  public CertificateService certificates() {
    return new CertificateService(requester);
  }

  /** Compliance in the client's Organization. */
  public ComplianceService compliance() {
    return new ComplianceService(requester);
  }

  /** Courses. */
  public CourseService courses() {
    return new CourseService(requester);
  }

  /** Enrollments in the client's Organization. */
  public EnrollmentService enrollments() {
    return new EnrollmentService(requester);
  }

  /** Events in the client's Organization. */
  public EventService events() {
    return new EventService(requester);
  }

  /** Learners in the client's Organization. */
  public LearnerService learners() {
    return new LearnerService(requester);
  }

  /** Members in the client's Organization. */
  public MemberService members() {
    return new MemberService(requester);
  }

  /** Operations in the client's Organization. */
  public OperationService operations() {
    return new OperationService(requester);
  }

  /** Partners. */
  public PartnerService partners() {
    return new PartnerService(requester);
  }

  /** Payment method in the client's Organization. */
  public PaymentMethodService paymentMethod() {
    return new PaymentMethodService(requester);
  }

  /** Seat orders in the client's Organization. */
  public SeatOrderService seatOrders() {
    return new SeatOrderService(requester);
  }

  /** Seats in the client's Organization. */
  public SeatService seats() {
    return new SeatService(requester);
  }

  /** Webhook subscriptions in the client's Organization. */
  public WebhookSubscriptionService webhookSubscriptions() {
    return new WebhookSubscriptionService(requester);
  }

  // The end of the section generated from openapi/sdk-ir.json.

  /** Verifies Deliveries and signs test payloads; the same as {@link Webhook}. */
  public Webhooks webhooks() {
    return WEBHOOKS;
  }

  public Environment getEnvironment() {
    return environment;
  }

  /** The API base URL requests go to, without a trailing slash. */
  public String getBaseUrl() {
    return config.baseUrl();
  }

  /** The Organization given when the client was built, or {@code null} if it is discovered. */
  public String getOrganizationId() {
    return config.organizationId();
  }

  /** The Scopes requested; empty means every Scope the API client holds. */
  public List<String> getScopes() {
    return config.scopes();
  }

  public int getMaxRetries() {
    return config.maxRetries();
  }

  public Duration getTimeout() {
    return config.timeout();
  }

  /** The integration named in the {@code User-Agent}, or {@code null}. */
  public AppInfo getAppInfo() {
    return config.appInfo();
  }

  /**
   * Collects settings for a {@link TeachMeHipaaClient}. Not thread-safe; the client it builds is.
   */
  public static final class Builder {
    private static final int DEFAULT_MAX_RETRIES = 2;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(80);

    private String clientId;
    private String clientSecret;
    private Environment environment = Environment.LIVE;
    private String baseUrl;
    private String organizationId;
    private List<String> scopes = List.of();
    private int maxRetries = DEFAULT_MAX_RETRIES;
    private Duration timeout = DEFAULT_TIMEOUT;
    private HttpTransport httpTransport;
    private Clock clock = Clock.systemUTC();
    private Sleeper sleeper = Sleeper.SYSTEM;
    private AppInfo appInfo;
    private RequestLogger logger;

    private Builder() {}

    /** Your API client's id ({@code cli_…}). Required. */
    public Builder clientId(String clientId) {
      this.clientId = requireNonBlank(clientId, "clientId");
      return this;
    }

    /** Your API client's secret ({@code tmh_cs_…}). Required. */
    public Builder clientSecret(String clientSecret) {
      this.clientSecret = requireNonBlank(clientSecret, "clientSecret");
      return this;
    }

    /**
     * Which deployment your credentials belong to. Defaults to {@link Environment#LIVE}; Sandbox
     * credentials never work against Live.
     */
    public Builder environment(Environment environment) {
      this.environment = Objects.requireNonNull(environment, "environment");
      return this;
    }

    /** Overrides the Environment's base URL, for example to point at a mock server. */
    public Builder baseUrl(String baseUrl) {
      Objects.requireNonNull(baseUrl, "baseUrl");
      URI uri = URI.create(baseUrl);
      if (!uri.isAbsolute() || uri.getHost() == null) {
        throw new IllegalArgumentException("baseUrl must be an absolute http(s) URL");
      }
      this.baseUrl = baseUrl.replaceAll("/+$", "");
      return this;
    }

    /**
     * The Organization to act for. When omitted, the client looks it up once from the Access token.
     */
    public Builder organizationId(String organizationId) {
      this.organizationId = requireNonBlank(organizationId, "organizationId");
      return this;
    }

    /** The Scopes to request, such as {@code learners:write}. Defaults to all the client holds. */
    public Builder scopes(String... scopes) {
      return scopes(Arrays.asList(scopes));
    }

    /** The Scopes to request, such as {@code learners:write}. Defaults to all the client holds. */
    public Builder scopes(Collection<String> scopes) {
      this.scopes = List.copyOf(scopes);
      return this;
    }

    /** Retries for a failed request that is safe to replay. Defaults to 2. */
    public Builder maxRetries(int maxRetries) {
      if (maxRetries < 0) {
        throw new IllegalArgumentException("maxRetries must be zero or more");
      }
      this.maxRetries = maxRetries;
      return this;
    }

    /** How long to wait for each response. Defaults to 80 seconds. */
    public Builder timeout(Duration timeout) {
      Objects.requireNonNull(timeout, "timeout");
      if (timeout.isNegative() || timeout.isZero()) {
        throw new IllegalArgumentException("timeout must be positive");
      }
      this.timeout = timeout;
      return this;
    }

    /**
     * Replaces the HTTP layer, for example to add a proxy or mTLS, or with a fake in tests.
     * Defaults to a {@link JdkHttpTransport}.
     */
    public Builder httpTransport(HttpTransport httpTransport) {
      this.httpTransport = Objects.requireNonNull(httpTransport, "httpTransport");
      return this;
    }

    /** Tells the time for token expiry. For tests; defaults to the system clock. */
    public Builder clock(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock");
      return this;
    }

    /** Waits between retries. For tests; defaults to {@link Thread#sleep}. */
    public Builder sleeper(Sleeper sleeper) {
      this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
      return this;
    }

    /** Your integration's name, version and URL, sent in the {@code User-Agent}. */
    public Builder appInfo(AppInfo appInfo) {
      this.appInfo = Objects.requireNonNull(appInfo, "appInfo");
      return this;
    }

    /**
     * Turns on logging: one entry per response with its method, path, status, latency and request
     * id, never bodies or credentials. Off by default.
     */
    public Builder logger(RequestLogger logger) {
      this.logger = Objects.requireNonNull(logger, "logger");
      return this;
    }

    /**
     * @throws IllegalStateException when the client id or secret is missing
     */
    public TeachMeHipaaClient build() {
      if (clientId == null || clientSecret == null) {
        throw new IllegalStateException("clientId and clientSecret are required");
      }
      ClientConfig config =
          new ClientConfig(
              baseUrl != null ? baseUrl : environment.baseUrl(),
              organizationId,
              scopes,
              maxRetries,
              timeout,
              appInfo,
              httpTransport != null ? httpTransport : new JdkHttpTransport(),
              clock,
              sleeper,
              logger);
      return new TeachMeHipaaClient(this, config);
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
