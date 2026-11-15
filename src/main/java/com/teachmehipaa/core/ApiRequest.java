package com.teachmehipaa.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One API call as a service method describes it. Internal: the generated service layer builds
 * these.
 *
 * <p>{@code pathTemplate} is the spec's template, such as {@code
 * /organizations/{orgId}/learners/{learnerId}}. {@code {orgId}} comes from the client's
 * Organization binding, never from the caller (ADR-0003).
 *
 * @param method the HTTP method
 * @param pathTemplate the path relative to the base URL, with {@code {name}} placeholders
 * @param pathParams values for the placeholders other than {@code orgId}
 * @param queryParams query parameters, in order
 * @param retrySafe the IR's {@code retrySafe}: whether a transient failure may be retried. GETs,
 *     calls that carry a generated Idempotency-Key and calls idempotent by meaning are; PATCH and
 *     DELETE never are, Idempotency-Key or not
 * @param idempotencyKeyRequired the IR's {@code idempotencyKey: required}: each call gets one
 *     generated key, reused by its retries, unless the caller passes their own
 * @param body the request body, serialized as JSON, or {@code null}
 * @param contentType the body's media type, or {@code null} when there is no body
 */
public record ApiRequest(
    String method,
    String pathTemplate,
    Map<String, String> pathParams,
    Map<String, String> queryParams,
    boolean retrySafe,
    boolean idempotencyKeyRequired,
    Object body,
    String contentType) {
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
  private static final String ORGANIZATION_PARAM = "orgId";

  public ApiRequest {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(pathTemplate, "pathTemplate");
    pathParams = Map.copyOf(pathParams);
    queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(queryParams));
  }

  public static Builder builder(String method, String pathTemplate) {
    return new Builder(method, pathTemplate);
  }

  /** Whether the path needs the client's Organization. */
  public boolean organizationScoped() {
    return pathTemplate.contains("{" + ORGANIZATION_PARAM + "}");
  }

  String path(String organizationId) {
    Matcher matcher = PLACEHOLDER.matcher(pathTemplate);
    StringBuilder path = new StringBuilder();
    while (matcher.find()) {
      String name = matcher.group(1);
      String value = name.equals(ORGANIZATION_PARAM) ? organizationId : pathParams.get(name);
      if (value == null) {
        throw new IllegalArgumentException("missing path parameter " + name);
      }
      matcher.appendReplacement(path, Matcher.quoteReplacement(UrlEncoding.percentEncode(value)));
    }
    matcher.appendTail(path);
    return path.toString();
  }

  public static final class Builder {
    private final String method;
    private final String pathTemplate;
    private final Map<String, String> pathParams = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();
    private boolean retrySafe;
    private boolean idempotencyKeyRequired;
    private Object body;
    private String contentType;

    private Builder(String method, String pathTemplate) {
      this.method = method;
      this.pathTemplate = pathTemplate;
    }

    public Builder pathParam(String name, String value) {
      pathParams.put(name, Objects.requireNonNull(value, name));
      return this;
    }

    /** Adds a query parameter; a {@code null} value leaves it out. */
    public Builder queryParam(String name, String value) {
      if (value != null) {
        queryParams.put(name, value);
      }
      return this;
    }

    /** Marks a transient failure as safe to retry. Off by default. */
    public Builder retrySafe() {
      this.retrySafe = true;
      return this;
    }

    /** Sends a generated {@code Idempotency-Key} with the call. */
    public Builder idempotencyKeyRequired() {
      this.idempotencyKeyRequired = true;
      return this;
    }

    /** Sends {@code body} as {@code application/json}. */
    public Builder jsonBody(Object body) {
      return body(body, "application/json");
    }

    /** Sends {@code body} as an RFC 7396 merge patch, where {@code null} clears a field. */
    public Builder mergePatchBody(Object body) {
      return body(body, "application/merge-patch+json");
    }

    private Builder body(Object body, String contentType) {
      this.body = Objects.requireNonNull(body, "body");
      this.contentType = contentType;
      return this;
    }

    public ApiRequest build() {
      return new ApiRequest(
          method,
          pathTemplate,
          pathParams,
          queryParams,
          retrySafe,
          idempotencyKeyRequired,
          body,
          contentType);
    }
  }
}
