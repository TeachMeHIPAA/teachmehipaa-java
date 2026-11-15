package com.teachmehipaa.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
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

  /** Waits between polls, as between retries: the client's injected {@link Sleeper}. */
  public Sleeper sleeper() {
    return config.sleeper();
  }

  /**
   * Sends the request and reads a successful response into {@code type}.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public <T> T request(ApiRequest request, Class<T> type, RequestOptions options) {
    TransportResponse response = send(request, options, Expect.JSON);
    return read(response, Json.MAPPER.constructType(type));
  }

  /**
   * Sends a Learner create or upsert and reads the Learner into {@code type}, with {@code created}
   * set from the status: 201 when this call created the Learner, 200 when it already existed.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public <T> T requestWriteResult(ApiRequest request, Class<T> type, RequestOptions options) {
    TransportResponse response = send(request, options, Expect.JSON);
    JsonNode tree = readTree(response);
    if (!(tree instanceof ObjectNode learner)) {
      throw Errors.fromResponse(response, config.clock().instant());
    }
    learner.put("created", response.statusCode() == 201);
    T result;
    try {
      result = Json.MAPPER.treeToValue(learner, type);
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
    return attach(result, response);
  }

  /**
   * Sends a list request and reads one page of {@code itemType}. The page fetches the pages after
   * it by resending {@code request} with the next position in {@code pagination}'s parameter, with
   * the same options.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public <T> Page<T> requestPage(
      ApiRequest request, Pagination pagination, Class<T> itemType, RequestOptions options) {
    TransportResponse response = send(request, options, Expect.JSON);
    JsonNode tree = readTree(response);
    PageBody<T> body;
    try {
      body =
          Json.MAPPER
              .readerFor(
                  Json.MAPPER.getTypeFactory().constructParametricType(PageBody.class, itemType))
              .readValue(tree);
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
    // A bare JSON null parses without error but is no page.
    if (body == null) {
      throw Errors.fromResponse(response, config.clock().instant());
    }
    String position =
        body.nextCursor() == null
            ? null
            : pagination.positionAfter(tree.path("items"), body.nextCursor());
    return new Page<>(
        body.items(),
        body.nextCursor(),
        LastResponse.of(response),
        () ->
            requestPage(
                request.withQueryParam(pagination.cursorParam(), position),
                pagination,
                itemType,
                options));
  }

  /**
   * Sends a request whose success has no body, such as a DELETE answered with 204.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public EmptyResponse requestEmpty(ApiRequest request, RequestOptions options) {
    return new EmptyResponse(LastResponse.of(send(request, options, Expect.NOTHING)));
  }

  /**
   * Sends a download request and returns the file's bytes as received.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public BinaryResponse requestBinary(ApiRequest request, RequestOptions options) {
    TransportResponse response = send(request, options, Expect.ANYTHING);
    return new BinaryResponse(response.body(), LastResponse.of(response));
  }

  private <T> T read(TransportResponse response, JavaType type) {
    T result;
    try {
      result = Json.MAPPER.readValue(response.body(), type);
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
    // A bare JSON null parses without error but is no object.
    if (result == null) {
      throw Errors.fromResponse(response, config.clock().instant());
    }
    return attach(result, response);
  }

  private static <T> T attach(T result, TransportResponse response) {
    if (result instanceof ApiResource resource) {
      resource.attachLastResponse(LastResponse.of(response));
    }
    return result;
  }

  private static JsonNode readTree(TransportResponse response) {
    try {
      return Json.MAPPER.readTree(response.body());
    } catch (IOException unreadable) {
      throw Errors.unparseable(response, unreadable);
    }
  }

  private TransportResponse send(ApiRequest request, RequestOptions options, Expect expect) {
    String organizationId = request.organizationScoped() ? organizationId() : null;
    return send(request, organizationId, options, expect);
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
    TransportResponse response = send(INTROSPECTION, null, RequestOptions.none(), Expect.JSON);
    JsonNode orgId = readTree(response).path("orgId");
    if (!orgId.isTextual() || orgId.asText().isEmpty()) {
      throw Errors.unexpected(
          response,
          "The Access token is not bound to an Organization. Pass organizationId when you build"
              + " the client.");
    }
    return orgId.asText();
  }

  private TransportResponse send(
      ApiRequest request, String organizationId, RequestOptions options, Expect expect) {
    TransportRequest prepared = prepare(request, organizationId, options);
    int maxRetries =
        options.getMaxRetries() == null ? config.maxRetries() : options.getMaxRetries();
    boolean replayed = false;
    int retryNumber = 1;
    while (true) {
      // Outside the catch below: a failed mint was already retried by the token provider. Public
      // endpoints never mint one.
      String accessToken = request.authenticated() ? tokens.accessToken() : null;
      try {
        return exchange(prepared, accessToken, expect);
      } catch (TeachMeHipaaException failure) {
        // A token revoked mid-flight gets one re-mint and replay per call. The API refuses a 401
        // before acting, so replaying even a PATCH is safe, and the replay isn't a retry.
        if (accessToken != null && !replayed && isUnauthorized(failure)) {
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
    headers.putAll(request.headers());
    byte[] body = null;
    if (request.body() != null) {
      ObjectNode fields = bodyFields(request);
      if (request.encoding() == ApiRequest.BodyEncoding.MULTIPART) {
        String boundary = "tmh-" + UUID.randomUUID();
        headers.put("content-type", request.encoding().mediaType() + "; boundary=" + boundary);
        body = Multipart.encode(boundary, fields, request.fileParts());
      } else {
        headers.put("content-type", request.encoding().mediaType());
        body = serialize(fields);
      }
    }
    return new TransportRequest(
        request.method(),
        URI.create(config.baseUrl() + request.path(organizationId) + query(request)),
        headers,
        body,
        options.getTimeout() == null ? config.timeout() : options.getTimeout());
  }

  private TransportResponse exchange(TransportRequest prepared, String accessToken, Expect expect) {
    Map<String, String> headers = new LinkedHashMap<>(prepared.headers());
    if (accessToken != null) {
      headers.put("authorization", "Bearer " + accessToken);
    }
    TransportResponse response =
        Transport.send(
            config,
            new TransportRequest(
                prepared.method(), prepared.uri(), headers, prepared.body(), prepared.timeout()));
    if (response.statusCode() / 100 != 2 || !isUsable(response, expect)) {
      throw Errors.fromResponse(response, config.clock().instant());
    }
    return response;
  }

  // A body nobody reads may be empty or JSON, but never the HTML page an unknown path returns.
  private static boolean isUsable(TransportResponse response, Expect expect) {
    return switch (expect) {
      case JSON -> Errors.isJson(response);
      case NOTHING -> Errors.isJson(response) || response.body().length == 0;
      case ANYTHING -> true;
    };
  }

  private static ObjectNode bodyFields(ApiRequest request) {
    JsonNode tree = Json.MAPPER.valueToTree(request.body());
    if (!(tree instanceof ObjectNode fields)) {
      throw new IllegalStateException("request body is not a JSON object");
    }
    request
        .fixedBodyValues()
        .forEach(
            (path, value) -> {
              ObjectNode parent = fields;
              for (String name : path.subList(0, path.size() - 1)) {
                parent =
                    parent.get(name) instanceof ObjectNode child ? child : parent.putObject(name);
              }
              parent.set(path.get(path.size() - 1), Json.MAPPER.valueToTree(value));
            });
    return fields;
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

  /** What a successful response must carry to be usable. */
  private enum Expect {
    JSON,
    /** An empty body, or JSON nobody reads. */
    NOTHING,
    /** Any bytes, such as a PDF. */
    ANYTHING
  }

  private record PageBody<T>(List<T> items, String nextCursor) {}
}
