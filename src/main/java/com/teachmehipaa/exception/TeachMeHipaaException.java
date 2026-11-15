package com.teachmehipaa.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/**
 * The base of every exception the library raises about an API call. Catch a subclass to handle one
 * kind of failure; its fields mirror the API's RFC 9457 problem body.
 */
public class TeachMeHipaaException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final Integer statusCode;
  private final String code;
  private final String title;
  private final String detail;
  private final FieldError[] errors;
  private final String requestId;
  private final transient HttpHeaders headers;
  private final String rawBody;

  /**
   * @param message what went wrong; the request id is appended when there is one
   * @param details what the response said
   * @param cause the underlying failure, or {@code null}
   */
  public TeachMeHipaaException(String message, ErrorDetails details, Throwable cause) {
    super(
        details.requestId() == null
            ? message
            : message + " (request id: " + details.requestId() + ")",
        cause);
    this.statusCode = details.statusCode();
    this.code = details.code();
    this.title = details.title();
    this.detail = details.detail();
    this.errors = details.errors().toArray(FieldError[]::new);
    this.requestId = details.requestId();
    this.headers = details.headers();
    this.rawBody = details.rawBody();
  }

  public TeachMeHipaaException(String message, ErrorDetails details) {
    this(message, details, null);
  }

  /** The HTTP status, or {@code null} when no response arrived. */
  public Integer getStatusCode() {
    return statusCode;
  }

  /**
   * The machine-readable reason, such as {@code seat_unavailable}, or an OAuth error such as {@code
   * invalid_client} from the token endpoint. Branch on this; new codes may appear at any time.
   */
  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }

  /** The invalid fields of a {@code validation_failed} problem; empty otherwise. */
  public List<FieldError> getErrors() {
    return List.of(errors);
  }

  /** The request id to quote to TeachMeHIPAA support, or {@code null}. */
  public String getRequestId() {
    return requestId;
  }

  /** The response headers; empty when no response arrived. */
  public HttpHeaders getHeaders() {
    return headers;
  }

  /** The response body as text, or {@code null} when no response arrived. */
  public String getRawBody() {
    return rawBody;
  }
}
