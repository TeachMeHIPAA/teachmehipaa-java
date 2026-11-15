package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.RequestOptions;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Sends API requests with the Organization binding, auth and error mapping. Internal: the service
 * layer calls it; callers use the services.
 */
public final class ApiRequester {
  private static final ApiRequest INTROSPECTION =
      ApiRequest.builder("GET", "/auth/introspection").build();

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
    TransportResponse response = send(request, organizationId, options);
    try {
      T resource = Json.MAPPER.readValue(response.body(), type);
      resource.attachLastResponse(LastResponse.of(response));
      return resource;
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
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
    TransportResponse response = send(INTROSPECTION, null, RequestOptions.none());
    JsonNode orgId;
    try {
      orgId = Json.MAPPER.readTree(response.body()).path("orgId");
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
    if (!orgId.isTextual() || orgId.asText().isEmpty()) {
      throw Errors.exception(
          response,
          "The Access token is not bound to an Organization. Pass organizationId when you build"
              + " the client.",
          null,
          null,
          null);
    }
    return orgId.asText();
  }

  private TransportResponse send(
      ApiRequest request, String organizationId, RequestOptions options) {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("accept", "application/json");
    headers.put("authorization", "Bearer " + tokens.accessToken());
    headers.put("user-agent", config.userAgent());
    if (options.getRequestId() != null) {
      headers.put("x-request-id", options.getRequestId());
    }
    TransportRequest transportRequest =
        new TransportRequest(
            request.method(),
            URI.create(config.baseUrl() + request.path(organizationId) + query(request)),
            headers,
            null,
            options.getTimeout() == null ? config.timeout() : options.getTimeout());
    TransportResponse response = Transport.send(config.transport(), transportRequest);
    if (response.statusCode() / 100 != 2 || !Errors.isJson(response)) {
      throw Errors.fromProblem(response);
    }
    return response;
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
