package com.teachmehipaa;

import com.teachmehipaa.core.WebhookPayloads;
import com.teachmehipaa.exception.WebhookSignatureException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies Deliveries and signs payloads for your own tests. Needs only the Signing secret, never
 * API credentials. Also reachable as {@link TeachMeHipaaClient#webhooks()}.
 *
 * <p>Pass the raw body exactly as received and the path the request arrived on, with its query
 * string: the signature covers both. In a servlet:
 *
 * <pre>{@code
 * String path = request.getQueryString() == null
 *     ? request.getRequestURI()
 *     : request.getRequestURI() + "?" + request.getQueryString();
 * try {
 *   WebhookDelivery delivery =
 *       Webhook.constructEvent(rawBody, request::getHeader, signingSecret, path);
 *   if (delivery instanceof WebhookDelivery.EventDelivery received) {
 *     handle(received.event());
 *   }
 *   response.setStatus(200);
 * } catch (WebhookSignatureException rejected) {
 *   response.setStatus(400);
 * }
 * }</pre>
 */
public final class Webhook {
  /**
   * How far a Delivery's timestamp may be from when it arrived, in either direction, unless you
   * pass another tolerance: 5 minutes.
   */
  public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

  private static final String SIGNATURE_HEADER = "x-tmh-signature";
  private static final String TIMESTAMP_HEADER = "x-tmh-timestamp";
  private static final String BODY_HASH_HEADER = "x-tmh-body-hash";

  private static final String RAW_BODY_HINT =
      "Are you passing the raw request body exactly as received? Parsed and re-serialized JSON"
          + " does not verify.";

  // OffsetDateTime.parse alone also takes a time without seconds; the platform always sends them.
  private static final Pattern ISO_8601_DATE_TIME =
      Pattern.compile(
          "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:\\d{2})");

  private static final Pattern HEX_SHA256 = Pattern.compile("[0-9a-f]{64}");

  // The platform's format: JavaScript's Date.toISOString(), always UTC with milliseconds.
  private static final DateTimeFormatter PLATFORM_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private static final HexFormat HEX = HexFormat.of();

  private Webhook() {}

  /**
   * Verifies a Delivery and returns its Event, or the Test delivery. Allows {@link
   * #DEFAULT_TOLERANCE} between the Delivery's timestamp and now.
   *
   * @param payload the raw request body exactly as received; parsed and re-serialized JSON does not
   *     verify
   * @param headers the request headers, matched without regard to case
   * @param secret the Webhook subscription's Signing secret
   * @param path the path, with its query string, the Delivery was sent to (such as {@code
   *     /webhooks/tmh?x=1}), or the full request URL; the signature covers it
   * @return the verified Delivery: an Event, or a Test delivery
   * @throws WebhookSignatureException when the Delivery fails verification
   * @throws IllegalArgumentException when the secret is blank or the path is not a path or URL
   */
  public static WebhookDelivery constructEvent(
      String payload, Map<String, String> headers, String secret, String path) {
    return constructEvent(payload, headers, secret, path, VerifyOptions.DEFAULTS);
  }

  /**
   * Verifies a Delivery and returns its Event, or the Test delivery.
   *
   * @param options the tolerance and arrival time to check the Delivery's timestamp against
   * @see #constructEvent(String, Map, String, String)
   */
  public static WebhookDelivery constructEvent(
      String payload,
      Map<String, String> headers,
      String secret,
      String path,
      VerifyOptions options) {
    return verify(utf8(payload), lookup(headers), secret, path, options);
  }

  /**
   * Verifies a Delivery whose headers come from a lookup, such as a servlet's {@code
   * request::getHeader} or Spring's {@code httpHeaders::getFirst}.
   *
   * @param headers returns a request header's value, or {@code null} when it is absent; it is
   *     called with lowercase names, so look them up without regard to case
   * @see #constructEvent(String, Map, String, String)
   */
  public static WebhookDelivery constructEvent(
      String payload, Function<String, String> headers, String secret, String path) {
    return constructEvent(payload, headers, secret, path, VerifyOptions.DEFAULTS);
  }

  /**
   * Verifies a Delivery whose headers come from a lookup.
   *
   * @see #constructEvent(String, Function, String, String)
   * @see #constructEvent(String, Map, String, String, VerifyOptions)
   */
  public static WebhookDelivery constructEvent(
      String payload,
      Function<String, String> headers,
      String secret,
      String path,
      VerifyOptions options) {
    return verify(utf8(payload), Objects.requireNonNull(headers, "headers"), secret, path, options);
  }

  /**
   * Verifies a Delivery whose raw body is bytes.
   *
   * @see #constructEvent(String, Map, String, String)
   */
  public static WebhookDelivery constructEvent(
      byte[] payload, Map<String, String> headers, String secret, String path) {
    return constructEvent(payload, headers, secret, path, VerifyOptions.DEFAULTS);
  }

  /**
   * Verifies a Delivery whose raw body is bytes.
   *
   * @see #constructEvent(String, Map, String, String, VerifyOptions)
   */
  public static WebhookDelivery constructEvent(
      byte[] payload,
      Map<String, String> headers,
      String secret,
      String path,
      VerifyOptions options) {
    return verify(
        Objects.requireNonNull(payload, "payload"), lookup(headers), secret, path, options);
  }

  /**
   * Verifies a Delivery whose raw body is bytes and whose headers come from a lookup.
   *
   * @see #constructEvent(String, Function, String, String)
   */
  public static WebhookDelivery constructEvent(
      byte[] payload, Function<String, String> headers, String secret, String path) {
    return constructEvent(payload, headers, secret, path, VerifyOptions.DEFAULTS);
  }

  /**
   * Verifies a Delivery whose raw body is bytes and whose headers come from a lookup.
   *
   * @see #constructEvent(String, Function, String, String, VerifyOptions)
   */
  public static WebhookDelivery constructEvent(
      byte[] payload,
      Function<String, String> headers,
      String secret,
      String path,
      VerifyOptions options) {
    return verify(
        Objects.requireNonNull(payload, "payload"),
        Objects.requireNonNull(headers, "headers"),
        secret,
        path,
        options);
  }

  /**
   * Returns the headers TeachMeHIPAA would send with {@code payload} to {@code path} now, so you
   * can test your handler without a real Delivery.
   *
   * @param payload the exact body you will send
   * @param secret the Signing secret your handler verifies with
   * @param path the path, with its query string, your handler is mounted on, or the full URL
   * @return the headers, looked up without regard to case
   * @throws IllegalArgumentException when the secret is blank or the path is not a path or URL
   */
  public static Map<String, String> generateTestHeaders(
      String payload, String secret, String path) {
    return generateTestHeaders(payload, secret, path, TestHeaderOptions.DEFAULTS);
  }

  /**
   * Returns the headers TeachMeHIPAA would send with {@code payload} to {@code path}, so you can
   * test your handler without a real Delivery.
   *
   * @param payload the exact body you will send
   * @param secret the Signing secret your handler verifies with
   * @param path the path, with its query string, your handler is mounted on, or the full URL
   * @param options the signing time and the identifying headers to send
   * @return the headers, looked up without regard to case
   * @throws IllegalArgumentException when the secret is blank or the path is not a path or URL
   */
  public static Map<String, String> generateTestHeaders(
      String payload, String secret, String path, TestHeaderOptions options) {
    return headers(utf8(payload), secret, path, options);
  }

  /**
   * Returns the headers TeachMeHIPAA would send with a body given as bytes.
   *
   * @see #generateTestHeaders(String, String, String)
   */
  public static Map<String, String> generateTestHeaders(
      byte[] payload, String secret, String path) {
    return generateTestHeaders(payload, secret, path, TestHeaderOptions.DEFAULTS);
  }

  /**
   * Returns the headers TeachMeHIPAA would send with a body given as bytes.
   *
   * @see #generateTestHeaders(String, String, String, TestHeaderOptions)
   */
  public static Map<String, String> generateTestHeaders(
      byte[] payload, String secret, String path, TestHeaderOptions options) {
    return headers(Objects.requireNonNull(payload, "payload"), secret, path, options);
  }

  private static Map<String, String> headers(
      byte[] body, String secret, String path, TestHeaderOptions options) {
    checkSecret(secret);
    String signedPath = pathAndQuery(path);
    Objects.requireNonNull(options, "options");
    String signedAt =
        PLATFORM_TIMESTAMP.format(options.timestamp != null ? options.timestamp : Instant.now());
    String bodyHash = sha256Hex(body);

    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (options.keyId != null) {
      headers.put("x-tmh-key-id", options.keyId);
    }
    headers.put(TIMESTAMP_HEADER, signedAt);
    headers.put(SIGNATURE_HEADER, sign(secret, signedPath, signedAt, bodyHash));
    headers.put(BODY_HASH_HEADER, bodyHash);
    if (options.eventType != null) {
      headers.put("x-tmh-event-type", options.eventType);
    }
    if (options.eventId != null) {
      headers.put("x-tmh-event-id", options.eventId);
    }
    headers.put("content-type", "application/json");
    return Collections.unmodifiableMap(headers);
  }

  private static WebhookDelivery verify(
      byte[] body,
      Function<String, String> headers,
      String secret,
      String path,
      VerifyOptions options) {
    checkSecret(secret);
    String signedPath = pathAndQuery(path);
    Objects.requireNonNull(options, "options");
    Duration tolerance = options.tolerance;
    Instant receivedAt = options.receivedAt != null ? options.receivedAt : Instant.now();

    String signature = requiredHeader(headers, SIGNATURE_HEADER);
    String timestamp = requiredHeader(headers, TIMESTAMP_HEADER);
    Instant signedAt = parseTimestamp(timestamp);
    String bodyHash = sha256Hex(body);

    String sentBodyHash = headers.apply(BODY_HASH_HEADER);
    if (sentBodyHash != null && !sentBodyHash.equals(bodyHash)) {
      throw new WebhookSignatureException(
          WebhookSignatureException.BODY_HASH_MISMATCH,
          "The " + BODY_HASH_HEADER + " header does not match the body. " + RAW_BODY_HINT);
    }

    // Past the format check both sides are 64 ASCII characters, and isEqual takes the same time
    // however much of them matches.
    String expected = sign(secret, signedPath, timestamp, bodyHash);
    if (!HEX_SHA256.matcher(signature).matches()
        || !MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.US_ASCII),
            signature.getBytes(StandardCharsets.US_ASCII))) {
      throw new WebhookSignatureException(
          WebhookSignatureException.SIGNATURE_MISMATCH,
          "No signature matched the expected signature for this Delivery. "
              + RAW_BODY_HINT
              + " Are you passing the exact request path, with its query string, that the"
              + " Delivery was sent to, and the Webhook subscription's Signing secret?");
    }

    // Only after the signature, so a forgery is never reported as merely late.
    if (Duration.between(signedAt, receivedAt).abs().compareTo(tolerance) > 0) {
      throw new WebhookSignatureException(
          WebhookSignatureException.TIMESTAMP_OUTSIDE_TOLERANCE,
          "The Delivery's timestamp "
              + timestamp
              + " is more than "
              + tolerance.toSeconds()
              + " seconds from when it arrived. Check your server's clock; otherwise the Delivery"
              + " is being replayed.");
    }

    return WebhookPayloads.parse(body);
  }

  private static Function<String, String> lookup(Map<String, String> headers) {
    Objects.requireNonNull(headers, "headers");
    return name ->
        headers.entrySet().stream()
            .filter(header -> name.equalsIgnoreCase(header.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
  }

  private static String requiredHeader(Function<String, String> headers, String name) {
    String value = headers.apply(name);
    if (value == null || value.isEmpty()) {
      throw new WebhookSignatureException(
          WebhookSignatureException.MISSING_HEADER,
          "No "
              + name
              + " header found on the Delivery. Are you passing the request headers as"
              + " TeachMeHIPAA sent them?");
    }
    return value;
  }

  private static Instant parseTimestamp(String timestamp) {
    if (ISO_8601_DATE_TIME.matcher(timestamp).matches()) {
      try {
        return OffsetDateTime.parse(timestamp).toInstant();
      } catch (DateTimeParseException invalid) {
        // Shaped right but not a real date-time, such as month 13.
      }
    }
    throw new WebhookSignatureException(
        WebhookSignatureException.INVALID_TIMESTAMP,
        "The "
            + TIMESTAMP_HEADER
            + " header \""
            + timestamp
            + "\" is not an ISO 8601 date-time with an offset.");
  }

  // Mirrors the platform signer, which signs the endpoint URL's pathname and search and takes a
  // "/"-prefixed path as given.
  private static String pathAndQuery(String path) {
    Objects.requireNonNull(path, "path");
    if (path.startsWith("/")) {
      return path;
    }
    try {
      URI url = new URI(path);
      String scheme = url.getScheme();
      if (url.getRawAuthority() != null
          && ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
        String pathname = url.getRawPath().isEmpty() ? "/" : url.getRawPath();
        String query = url.getRawQuery();
        return query == null || query.isEmpty() ? pathname : pathname + "?" + query;
      }
    } catch (URISyntaxException notAUrl) {
      // Reported below with the rest.
    }
    throw new IllegalArgumentException(
        "path must be the request path (such as \"/webhooks/tmh?x=1\") or the full request URL,"
            + " got \""
            + path
            + "\"");
  }

  private static void checkSecret(String secret) {
    Objects.requireNonNull(secret, "secret");
    if (secret.isBlank()) {
      throw new IllegalArgumentException(
          "secret must be the Webhook subscription's Signing secret; got an empty value");
    }
  }

  // ADR-0005; the vendored IR's events.signing.canonical describes the same four parts, and a test
  // pins the two together.
  private static String sign(String secret, String signedPath, String timestamp, String bodyHash) {
    try {
      Mac hmac = Mac.getInstance("HmacSHA256");
      // The key is the Signing secret's UTF-8 bytes. The secret looks like base64, but the
      // platform never decodes it.
      hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      String canonical = "POST\n" + signedPath + "\n" + timestamp + "\n" + bodyHash;
      return HEX.formatHex(hmac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException unavailable) {
      throw new IllegalStateException("Every Java runtime provides HmacSHA256", unavailable);
    }
  }

  private static String sha256Hex(byte[] body) {
    try {
      return HEX.formatHex(MessageDigest.getInstance("SHA-256").digest(body));
    } catch (GeneralSecurityException unavailable) {
      throw new IllegalStateException("Every Java runtime provides SHA-256", unavailable);
    }
  }

  private static byte[] utf8(String payload) {
    return Objects.requireNonNull(payload, "payload").getBytes(StandardCharsets.UTF_8);
  }

  /**
   * How {@link #constructEvent(String, Map, String, String, VerifyOptions)} checks a Delivery's
   * timestamp. Every setting is optional.
   */
  public static final class VerifyOptions {
    static final VerifyOptions DEFAULTS = builder().build();

    private final Duration tolerance;
    private final Instant receivedAt;

    private VerifyOptions(Builder builder) {
      this.tolerance = builder.tolerance;
      this.receivedAt = builder.receivedAt;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Collects {@link VerifyOptions}. Not thread-safe; the options it builds are. */
    public static final class Builder {
      private Duration tolerance = DEFAULT_TOLERANCE;
      private Instant receivedAt;

      private Builder() {}

      /**
       * How far the Delivery's timestamp may be from when it arrived, in either direction. Defaults
       * to {@link Webhook#DEFAULT_TOLERANCE}.
       *
       * @throws IllegalArgumentException when {@code tolerance} is not positive
       */
      public Builder tolerance(Duration tolerance) {
        Objects.requireNonNull(tolerance, "tolerance");
        if (tolerance.isNegative() || tolerance.isZero()) {
          throw new IllegalArgumentException("tolerance must be positive");
        }
        this.tolerance = tolerance;
        return this;
      }

      /** When the Delivery arrived. Defaults to the moment it is verified. */
      public Builder receivedAt(Instant receivedAt) {
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        return this;
      }

      public VerifyOptions build() {
        return new VerifyOptions(this);
      }
    }
  }

  /**
   * What {@link #generateTestHeaders(String, String, String, TestHeaderOptions)} signs and sends
   * besides the body. Every setting is optional.
   */
  public static final class TestHeaderOptions {
    static final TestHeaderOptions DEFAULTS = builder().build();

    private final Instant timestamp;
    private final String eventType;
    private final String eventId;
    private final String keyId;

    private TestHeaderOptions(Builder builder) {
      this.timestamp = builder.timestamp;
      this.eventType = builder.eventType;
      this.eventId = builder.eventId;
      this.keyId = builder.keyId;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Collects {@link TestHeaderOptions}. Not thread-safe; the options it builds are. */
    public static final class Builder {
      private Instant timestamp;
      private String eventType;
      private String eventId;
      private String keyId;

      private Builder() {}

      /** When the Delivery was signed, sent to the millisecond. Defaults to now. */
      public Builder timestamp(Instant timestamp) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        return this;
      }

      /** The {@code x-tmh-event-type} header, such as {@code learner.certified}. */
      public Builder eventType(String eventType) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        return this;
      }

      /** The {@code x-tmh-event-id} header. */
      public Builder eventId(String eventId) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        return this;
      }

      /** The {@code x-tmh-key-id} header naming the Signing secret. */
      public Builder keyId(String keyId) {
        this.keyId = Objects.requireNonNull(keyId, "keyId");
        return this;
      }

      public TestHeaderOptions build() {
        return new TestHeaderOptions(this);
      }
    }
  }
}
