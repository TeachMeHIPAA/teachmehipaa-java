package com.teachmehipaa;

import java.time.Duration;

/**
 * The only fields the library ever logs about a request (ADR-0007). A request that got no response
 * is logged too, before its exception is raised, with no status and the kind of failure.
 *
 * @param method the HTTP method
 * @param path the URL path without its query, which can carry Learner emails
 * @param statusCode the HTTP status, or {@code null} when no response arrived
 * @param latency how long the response took, or how long until the request failed
 * @param requestId the response's {@code x-request-id}; when no response arrived, the one the
 *     request sent; otherwise {@code null}
 * @param errorKind why no response arrived, or {@code null} when one did
 */
public record RequestLogEntry(
    String method,
    String path,
    Integer statusCode,
    Duration latency,
    String requestId,
    ErrorKind errorKind) {

  /** An entry for a request that got a response. */
  public RequestLogEntry(
      String method, String path, int statusCode, Duration latency, String requestId) {
    this(method, path, (Integer) statusCode, latency, requestId, null);
  }

  /** Why a request got no response. */
  public enum ErrorKind {
    /** No response arrived within the timeout. */
    TIMEOUT,
    /** The connection failed or broke before a response arrived. */
    CONNECTION
  }
}
