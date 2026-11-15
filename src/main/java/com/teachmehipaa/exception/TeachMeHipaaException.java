package com.teachmehipaa.exception;

import java.net.http.HttpHeaders;
import java.util.Map;

/**
 * The base of every exception the library raises about an API call. Its fields mirror the API's RFC
 * 9457 problem body.
 */
public class TeachMeHipaaException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private static final HttpHeaders NO_HEADERS = HttpHeaders.of(Map.of(), (name, value) -> true);

  private final Integer statusCode;
  private final String code;
  private final String title;
  private final String detail;
  private final String requestId;
  private final transient HttpHeaders headers;
  private final String rawBody;

  /**
   * @param message what went wrong; the request id is appended when there is one
   * @param statusCode the HTTP status, or {@code null} when no response arrived
   * @param code the problem's machine-readable code, or {@code null}
   * @param title the problem's title, or {@code null}
   * @param detail the problem's detail, or {@code null}
   * @param requestId the response's {@code x-request-id}, or {@code null}
   * @param headers the response headers, or {@code null} when no response arrived
   * @param rawBody the response body as text, or {@code null} when no response arrived
   * @param cause the underlying failure, or {@code null}
   */
  public TeachMeHipaaException(
      String message,
      Integer statusCode,
      String code,
      String title,
      String detail,
      String requestId,
      HttpHeaders headers,
      String rawBody,
      Throwable cause) {
    super(requestId == null ? message : message + " (request id: " + requestId + ")", cause);
    this.statusCode = statusCode;
    this.code = code;
    this.title = title;
    this.detail = detail;
    this.requestId = requestId;
    this.headers = headers == null ? NO_HEADERS : headers;
    this.rawBody = rawBody;
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
