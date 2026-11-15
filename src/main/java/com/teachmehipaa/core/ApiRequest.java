package com.teachmehipaa.core;

import com.teachmehipaa.FileUpload;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
 * @param headers extra request headers, such as {@code if-match}, keyed in lower case
 * @param authenticated whether the call carries an Access token; public endpoints don't
 * @param retrySafe the IR's {@code retrySafe}: whether a transient failure may be retried. GETs,
 *     calls that carry a generated Idempotency-Key and calls idempotent by meaning are; PATCH and
 *     DELETE never are, Idempotency-Key or not
 * @param idempotencyKeyRequired the IR's {@code idempotencyKey: required}: each call gets one
 *     generated key, reused by its retries, unless the caller passes their own
 * @param body the request body, serialized with Jackson, or {@code null}
 * @param encoding how {@code body} goes on the wire, or {@code null} when there is no body
 * @param fixedBodyValues values the method itself writes into the body, by property path
 * @param fileParts the files of a multipart body, in order
 */
public record ApiRequest(
    String method,
    String pathTemplate,
    Map<String, String> pathParams,
    Map<String, String> queryParams,
    Map<String, String> headers,
    boolean authenticated,
    boolean retrySafe,
    boolean idempotencyKeyRequired,
    Object body,
    BodyEncoding encoding,
    Map<List<String>, Object> fixedBodyValues,
    List<FilePart> fileParts) {
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
  private static final String ORGANIZATION_PARAM = "orgId";

  /** How a request body is encoded. */
  public enum BodyEncoding {
    JSON("application/json"),
    /** RFC 7396: a {@code null} member clears the field. */
    MERGE_PATCH("application/merge-patch+json"),
    /** One part per file, plus one JSON part per other top-level body field. */
    MULTIPART("multipart/form-data");

    private final String mediaType;

    BodyEncoding(String mediaType) {
      this.mediaType = mediaType;
    }

    public String mediaType() {
      return mediaType;
    }
  }

  /**
   * One file in a multipart body.
   *
   * @param name the form field name
   * @param file the file's bytes and name
   * @param contentType the part's media type, from the spec
   */
  public record FilePart(String name, FileUpload file, String contentType) {
    public FilePart {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(file, name);
      Objects.requireNonNull(contentType, "contentType");
    }
  }

  public ApiRequest {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(pathTemplate, "pathTemplate");
    pathParams = Map.copyOf(pathParams);
    queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(queryParams));
    headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
    fixedBodyValues = Collections.unmodifiableMap(new LinkedHashMap<>(fixedBodyValues));
    fileParts = List.copyOf(fileParts);
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
    private final Map<String, String> headers = new LinkedHashMap<>();
    private boolean authenticated = true;
    private boolean retrySafe;
    private boolean idempotencyKeyRequired;
    private Object body;
    private BodyEncoding encoding;
    private final Map<List<String>, Object> fixedBodyValues = new LinkedHashMap<>();
    private final List<FilePart> fileParts = new ArrayList<>();

    private Builder(String method, String pathTemplate) {
      this.method = method;
      this.pathTemplate = pathTemplate;
    }

    public Builder pathParam(String name, String value) {
      pathParams.put(name, Objects.requireNonNull(value, name));
      return this;
    }

    /**
     * Adds a query parameter; a {@code null} value leaves it out. Date-times are sent in ISO 8601
     * with seconds; anything else as its {@code toString()}, which for open enums is the wire
     * value.
     */
    public Builder queryParam(String name, Object value) {
      if (value != null) {
        queryParams.put(name, queryValue(value));
      }
      return this;
    }

    /** Adds a request header; a {@code null} value leaves it out. */
    public Builder header(String name, String value) {
      if (value != null) {
        headers.put(name.toLowerCase(Locale.ROOT), value);
      }
      return this;
    }

    /** Sends no Access token: the endpoint is public. */
    public Builder unauthenticated() {
      this.authenticated = false;
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
      return body(body, BodyEncoding.JSON);
    }

    /** Sends {@code body} as an RFC 7396 merge patch, where {@code null} clears a field. */
    public Builder mergePatchBody(Object body) {
      return body(body, BodyEncoding.MERGE_PATCH);
    }

    /**
     * Sends {@code body} as {@code multipart/form-data}: each top-level field becomes a JSON part,
     * and each {@link #filePart} a file part.
     */
    public Builder multipartBody(Object body) {
      return body(body, BodyEncoding.MULTIPART);
    }

    /** Adds a file to a multipart body. */
    public Builder filePart(String name, FileUpload file, String contentType) {
      fileParts.add(new FilePart(name, file, contentType));
      return this;
    }

    /**
     * Writes {@code value} into the body at {@code path}, creating the objects on the way. A method
     * fixes a value this way when the spec splits one operation into two methods.
     */
    public Builder fixedBodyValue(List<String> path, Object value) {
      fixedBodyValues.put(List.copyOf(path), value);
      return this;
    }

    private Builder body(Object body, BodyEncoding encoding) {
      this.body = Objects.requireNonNull(body, "body");
      this.encoding = encoding;
      return this;
    }

    public ApiRequest build() {
      return new ApiRequest(
          method,
          pathTemplate,
          pathParams,
          queryParams,
          headers,
          authenticated,
          retrySafe,
          idempotencyKeyRequired,
          body,
          encoding,
          fixedBodyValues,
          fileParts);
    }

    private static String queryValue(Object value) {
      return value instanceof OffsetDateTime dateTime
          ? DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime)
          : value.toString();
    }
  }
}
