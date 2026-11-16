package com.teachmehipaa.core;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/** Builds {@code POST /auth/token} requests, whichever grant they carry. */
final class TokenEndpoint {
  /** The {@code Authorization} header value for a client id and secret. */
  static String basicAuthorization(String clientId, String clientSecret) {
    // RFC 6749 §2.3.1: each half is form-encoded before base64.
    return "Basic "
        + Base64.getEncoder()
            .encodeToString(
                (formEncode(clientId) + ":" + formEncode(clientSecret))
                    .getBytes(StandardCharsets.UTF_8));
  }

  static TransportRequest request(ClientConfig config, String authorization, String form) {
    return new TransportRequest(
        "POST",
        URI.create(config.baseUrl() + "/auth/token"),
        Map.of(
            "accept",
            "application/json",
            "authorization",
            authorization,
            "content-type",
            "application/x-www-form-urlencoded",
            "user-agent",
            config.userAgent()),
        form.getBytes(StandardCharsets.UTF_8),
        config.timeout());
  }

  static String formEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private TokenEndpoint() {}
}
