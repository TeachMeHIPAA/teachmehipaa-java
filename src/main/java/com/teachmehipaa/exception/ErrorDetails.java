package com.teachmehipaa.exception;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

/**
 * What a failed response said, as carried by every {@link TeachMeHipaaException}. The fields mirror
 * the API's RFC 9457 problem body.
 *
 * @param statusCode the HTTP status, or {@code null} when no response arrived
 * @param code the machine-readable reason, or {@code null}
 * @param title the problem's title, or {@code null}
 * @param detail the problem's detail, or {@code null}
 * @param errors the invalid fields of a {@code validation_failed} problem; empty otherwise
 * @param requestId the response's {@code x-request-id}, or {@code null}
 * @param headers the response headers; empty when no response arrived
 * @param rawBody the response body as text, or {@code null} when no response arrived
 */
public record ErrorDetails(
    Integer statusCode,
    String code,
    String title,
    String detail,
    List<FieldError> errors,
    String requestId,
    HttpHeaders headers,
    String rawBody) {
  private static final HttpHeaders NO_HEADERS = HttpHeaders.of(Map.of(), (name, value) -> true);
  private static final ErrorDetails NO_RESPONSE = builder().build();

  public ErrorDetails {
    errors = errors == null ? List.of() : List.copyOf(errors);
    headers = headers == null ? NO_HEADERS : headers;
  }

  /** For a request that got no response. */
  public static ErrorDetails noResponse() {
    return NO_RESPONSE;
  }

  /** Starts details with every field unset: no status, no headers, no body. */
  public static Builder builder() {
    return new Builder();
  }

  /** Collects {@link ErrorDetails}. Every field is optional. Not thread-safe. */
  public static final class Builder {
    private Integer statusCode;
    private String code;
    private String title;
    private String detail;
    private List<FieldError> errors;
    private String requestId;
    private HttpHeaders headers;
    private String rawBody;

    private Builder() {}

    public Builder statusCode(Integer statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder errors(List<FieldError> errors) {
      this.errors = errors;
      return this;
    }

    public Builder requestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public Builder headers(HttpHeaders headers) {
      this.headers = headers;
      return this;
    }

    public Builder rawBody(String rawBody) {
      this.rawBody = rawBody;
      return this;
    }

    public ErrorDetails build() {
      return new ErrorDetails(statusCode, code, title, detail, errors, requestId, headers, rawBody);
    }
  }
}
