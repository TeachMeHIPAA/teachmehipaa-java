package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/** Turns failed responses into exceptions. Bodies are kept on the exception, never logged. */
final class Errors {
  private static final Pattern JSON_CONTENT_TYPE =
      Pattern.compile("^application/([\\w.+-]*\\+)?json\\b", Pattern.CASE_INSENSITIVE);

  /**
   * Unknown paths answer with a 200 HTML page, so a body is only parsed after its content type says
   * it is JSON.
   */
  static boolean isJson(TransportResponse response) {
    return response
        .headers()
        .firstValue("content-type")
        .map(type -> JSON_CONTENT_TYPE.matcher(type).find())
        .orElse(false);
  }

  /** A response from an API endpoint that failed or wasn't JSON: RFC 9457 problem fields. */
  static TeachMeHipaaException fromProblem(TransportResponse response) {
    JsonNode problem = jsonBody(response);
    String title = text(problem, "title");
    String detail = text(problem, "detail");
    String code = text(problem, "code");
    String summary = join(title, detail);
    return exception(
        response, summary == null ? fallbackMessage(response) : summary, code, title, detail);
  }

  /** A response from the token endpoint, which answers in RFC 6749 {@code {error, ...}} form. */
  static TeachMeHipaaException fromOAuthError(TransportResponse response) {
    JsonNode oauth = jsonBody(response);
    String code = text(oauth, "error");
    String detail = text(oauth, "error_description");
    String message = code == null ? fallbackMessage(response) : join(code, detail);
    return exception(response, message, code, null, detail);
  }

  static TeachMeHipaaException unparseable(TransportResponse response, IOException cause) {
    return new TeachMeHipaaException(
        "Could not read the TeachMeHIPAA API response as JSON",
        response.statusCode(),
        null,
        null,
        null,
        requestId(response),
        response.headers(),
        rawBody(response),
        cause);
  }

  static TeachMeHipaaException noResponse(TransportRequest request, IOException cause) {
    // The host only: the query can carry a Learner's email.
    return new TeachMeHipaaException(
        "No response from " + request.uri().getHost() + ": " + cause,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        cause);
  }

  static TeachMeHipaaException exception(
      TransportResponse response, String message, String code, String title, String detail) {
    return new TeachMeHipaaException(
        message,
        response.statusCode(),
        code,
        title,
        detail,
        requestId(response),
        response.headers(),
        rawBody(response),
        null);
  }

  private static String fallbackMessage(TransportResponse response) {
    if (isJson(response)) {
      return "TeachMeHIPAA API responded with HTTP " + response.statusCode();
    }
    return "Expected a JSON response but got "
        + response.headers().firstValue("content-type").orElse("no content type")
        + " with HTTP "
        + response.statusCode()
        + "; check the base URL";
  }

  private static JsonNode jsonBody(TransportResponse response) {
    if (!isJson(response)) {
      return null;
    }
    try {
      return Json.MAPPER.readTree(response.body());
    } catch (IOException notJson) {
      return null;
    }
  }

  private static String text(JsonNode node, String field) {
    if (node == null) {
      return null;
    }
    JsonNode value = node.get(field);
    return value != null && value.isTextual() ? value.asText() : null;
  }

  private static String join(String first, String second) {
    if (first == null) {
      return second;
    }
    return second == null ? first : first + ": " + second;
  }

  private static String requestId(TransportResponse response) {
    return response.headers().firstValue("x-request-id").orElse(null);
  }

  private static String rawBody(TransportResponse response) {
    return new String(response.body(), StandardCharsets.UTF_8);
  }

  private Errors() {}
}
