package com.teachmehipaa;

import java.time.Duration;

/**
 * The only fields the library ever logs about a request (ADR-0007).
 *
 * @param method the HTTP method
 * @param path the URL path without its query, which can carry Learner emails
 * @param statusCode the HTTP status
 * @param latency how long the response took
 * @param requestId the response's {@code x-request-id}, or {@code null}
 */
public record RequestLogEntry(
    String method, String path, int statusCode, Duration latency, String requestId) {}
