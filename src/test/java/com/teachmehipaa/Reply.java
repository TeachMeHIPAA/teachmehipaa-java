package com.teachmehipaa;

import com.teachmehipaa.core.TransportResponse;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One scripted HTTP response. JSON bodies are written as literal text, never by the SDK. */
record Reply(int status, Map<String, String> headers, String body) {
  static Reply json(String body) {
    return json(200, body);
  }

  static Reply json(int status, String body) {
    return new Reply(status, Map.of("content-type", "application/json"), body);
  }

  static Reply problem(int status, String body) {
    return new Reply(status, Map.of("content-type", "application/problem+json"), body);
  }

  /** A success with no body, such as a 204. */
  static Reply empty(int status) {
    return new Reply(status, Map.of(), "");
  }

  Reply withHeader(String name, String value) {
    Map<String, String> merged = new LinkedHashMap<>(headers);
    merged.put(name, value);
    return new Reply(status, merged, body);
  }

  TransportResponse toResponse() {
    Map<String, List<String>> multi = new LinkedHashMap<>();
    headers.forEach((name, value) -> multi.put(name, List.of(value)));
    return new TransportResponse(
        status,
        HttpHeaders.of(multi, (name, value) -> true),
        body.getBytes(StandardCharsets.UTF_8));
  }
}
