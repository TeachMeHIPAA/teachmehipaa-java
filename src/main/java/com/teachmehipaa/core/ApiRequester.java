package com.teachmehipaa.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Sends API requests with the Organization binding, auth and error mapping. Internal: the service
 * layer calls it; callers use the services.
 */
public final class ApiRequester {
  private static final ApiRequest INTROSPECTION =
      ApiRequest.builder("GET", "/auth/introspection").retrySafe().build();

  private final ClientConfig config;
  private final TokenProvider tokens;
  private final ReentrantLock organizationLock = new ReentrantLock();
  private volatile String organizationId;

  public ApiRequester(ClientConfig config, TokenProvider tokens) {
    this.config = Objects.requireNonNull(config, "config");
    this.tokens = Objects.requireNonNull(tokens, "tokens");
    this.organizationId = config.organizationId();
  }

  /**
   * Sends the request and reads a successful response into {@code type}.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public <T extends ApiResource> T request(
      ApiRequest request, Class<T> type, RequestOptions options) {
    String organizationId = request.organizationScoped() ? organizationId() : null;
    TransportResponse response = send(request, organizationId, options, true);
    try {
      T resource = Json.MAPPER.readValue(response.body(), type);
      // A bare JSON null parses without error but is no object.
      if (resource == null) {
        throw Errors.fromResponse(response, config.clock().instant());
      }
      resource.attachLastResponse(LastResponse.of(response));
      return resource;
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
  }

  /**
   * Sends a request whose success has no body, such as a DELETE answered with 204.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public void requestEmpty(ApiRequest request, RequestOptions options) {
    String organizationId = request.organizationScoped() ? organizationId() : null;
    send(request, organizationId, options, false);
  }

  // ADR-0003: resolved at most once per client; concurrent first calls share one lookup, and a
  // failed lookup is not cached.
  private String organizationId() {
    String known = organizationId;
    if (known != null) {
      return known;
    }
    organizationLock.lock();
    try {
      if (organizationId == null) {
        organizationId = discoverOrganizationId();
      }
      return organizationId;
    } finally {
      organizationLock.unlock();
    }
  }

  private String discoverOrganizationId() {
    TransportResponse response = send(INTROSPECTION, null, RequestOptions.none(), true);
    JsonNode orgId;
    try {
      orgId = Json.MAPPER.readTree(response.body()).path("orgId");
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
    if (!orgId.isTextual() || orgId.asText().isEmpty()) {
      throw Errors.unexpected(
          response,
          "The Access token is not bound to an Organization. Pass organizationId when you build"
              + " the client.");
    }
    return orgId.asText();
  }

  private TransportResponse send(
      ApiRequest request, String organizationId, RequestOptions options, boolean expectsBody) {
    TransportRequest prepared = prepare(request, organizationId, options);
    int maxRetries =
        options.getMaxRetries() == null ? config.maxRetries() : options.getMaxRetries();
    boolean replayed = false;
    int retryNumber = 1;
    while (true) {
      // Outside the catch below: a failed mint was already retried by the token provider.
      String accessToken = tokens.accessToken();
      try {
        return exchange(prepared, accessToken, expectsBody);
      } catch (TeachMeHipaaException failure) {
        // A token revoked mid-flight gets one re-mint and replay per call. The API refuses a 401
        // before acting, so replaying even a PATCH is safe, and the replay isn't a retry.
        if (!replayed && isUnauthorized(failure)) {
          replayed = true;
          tokens.invalidate(accessToken);
          continue;
        }
        if (!request.retrySafe() || retryNumber > maxRetries || !Retries.isTransient(failure)) {
          throw failure;
        }
        Retries.sleep(
            config.sleeper(),
            Retries.delay(failure, retryNumber, config.clock().instant()),
            failure);
        retryNumber++;
      }
    }
  }

  private static boolean isUnauthorized(TeachMeHipaaException failure) {
    Integer status = failure.getStatusCode();
    return status != null && status == 401 && "unauthorized".equals(failure.getCode());
  }

  // Runs once per call, so every retry sends exactly the same request.
  private TransportRequest prepare(
      ApiRequest request, String organizationId, RequestOptions options) {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("accept", "application/json");
    headers.put("user-agent", config.userAgent());
    if (options.getRequestId() != null) {
      headers.put("x-request-id", options.getRequestId());
    }
    String idempotencyKey =
        options.getIdempotencyKey() != null
            ? options.getIdempotencyKey()
            : request.idempotencyKeyRequired() ? UUID.randomUUID().toString() : null;
    if (idempotencyKey != null) {
      headers.put("idempotency-key", idempotencyKey);
    }
    byte[] body = null;
    if (request.body() != null) {
      headers.put("content-type", request.contentType());
      body = serialize(request.body());
    }
    return new TransportRequest(
        request.method(),
        URI.create(config.baseUrl() + request.path(organizationId) + query(request)),
        headers,
        body,
        options.getTimeout() == null ? config.timeout() : options.getTimeout());
  }

  private TransportResponse exchange(
      TransportRequest prepared, String accessToken, boolean expectsBody) {
    Map<String, String> headers = new LinkedHashMap<>(prepared.headers());
    headers.put("authorization", "Bearer " + accessToken);
    TransportResponse response =
        Transport.send(
            config,
            new TransportRequest(
                prepared.method(), prepared.uri(), headers, prepared.body(), prepared.timeout()));
    if (response.statusCode() / 100 != 2 || !isUsable(response, expectsBody)) {
      throw Errors.fromResponse(response, config.clock().instant());
    }
    return response;
  }

  // A body nobody reads may be empty or JSON, but never the HTML page an unknown path returns.
  private static boolean isUsable(TransportResponse response, boolean expectsBody) {
    return Errors.isJson(response) || (!expectsBody && response.body().length == 0);
  }

  private static byte[] serialize(Object body) {
    try {
      return Json.MAPPER.writeValueAsBytes(body);
    } catch (JsonProcessingException unserializable) {
      throw new IllegalStateException("request body is not serializable as JSON", unserializable);
    }
  }

  private static String query(ApiRequest request) {
    if (request.queryParams().isEmpty()) {
      return "";
    }
    return request.queryParams().entrySet().stream()
        .map(
            param ->
                UrlEncoding.percentEncode(param.getKey())
                    + "="
                    + UrlEncoding.percentEncode(param.getValue()))
        .collect(Collectors.joining("&", "?", ""));
  }
}
