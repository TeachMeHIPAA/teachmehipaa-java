package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.exception.ApiConnectionException;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.ApiTimeoutException;
import com.teachmehipaa.exception.AuthenticationException;
import com.teachmehipaa.exception.ConflictException;
import com.teachmehipaa.exception.ErrorDetails;
import com.teachmehipaa.exception.FieldError;
import com.teachmehipaa.exception.IdempotencyException;
import com.teachmehipaa.exception.InvalidRequestException;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.OAuthException;
import com.teachmehipaa.exception.PermissionDeniedException;
import com.teachmehipaa.exception.PreconditionFailedException;
import com.teachmehipaa.exception.RateLimitException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

  /**
   * A response the caller can't use: an error status, or a success whose body isn't what the call
   * returns. RFC 9457 problem fields are read when the body has them.
   */
  static TeachMeHipaaException fromResponse(TransportResponse response, Instant now) {
    JsonNode problem = jsonBody(response);
    String title = text(problem, "title");
    String detail = text(problem, "detail");
    String code = text(problem, "code");
    String summary = join(title, detail);
    String message = summary == null ? fallbackMessage(response) : summary;
    ErrorDetails details = details(response, code, title, detail, fieldErrors(problem));
    int status = response.statusCode();
    if (status / 100 == 2) {
      return new ApiException(message, details);
    }
    return switch (status) {
      case 401 -> new AuthenticationException(message, details);
      case 403 -> new PermissionDeniedException(message, details);
      case 404 -> new NotFoundException(message, details);
      case 409 -> new ConflictException(message, details);
      case 412, 428 -> new PreconditionFailedException(message, details);
      case 422 ->
          "idempotency_key_reuse".equals(code)
              ? new IdempotencyException(message, details)
              : new InvalidRequestException(message, details);
      case 400, 405, 406, 415 -> new InvalidRequestException(message, details);
      case 429 ->
          new RateLimitException(message, details, RetryAfter.from(response.headers(), now));
      default -> new ApiException(message, details);
    };
  }

  /**
   * The token endpoint answers in RFC 6749 {@code {error, ...}} form; anything else from it, such
   * as an HTML page or a bare 502, maps like any other response.
   */
  static TeachMeHipaaException fromOAuthError(TransportResponse response, Instant now) {
    JsonNode oauth = jsonBody(response);
    String code = text(oauth, "error");
    if (code == null) {
      return fromResponse(response, now);
    }
    String detail = text(oauth, "error_description");
    return new OAuthException(join(code, detail), details(response, code, null, detail, null));
  }

  /** A success that claimed JSON but could not be read as what the call returns. */
  static TeachMeHipaaException unparseable(TransportResponse response, IOException cause) {
    return new ApiException(
        "Could not read the TeachMeHIPAA API response as JSON",
        details(response, null, null, null, null),
        cause);
  }

  static TeachMeHipaaException noResponse(TransportRequest request, IOException cause) {
    // The host only: the query can carry a Learner's email.
    String host = request.uri().getHost();
    if (cause instanceof HttpTimeoutException) {
      return new ApiTimeoutException(
          "No response from " + host + " within " + request.timeout().toMillis() + " ms", cause);
    }
    return new ApiConnectionException("No response from " + host + ": " + cause, cause);
  }

  /** A response that is valid JSON but breaks a promise the API makes. */
  static TeachMeHipaaException unexpected(TransportResponse response, String message) {
    return new TeachMeHipaaException(message, details(response, null, null, null, null));
  }

  private static ErrorDetails details(
      TransportResponse response,
      String code,
      String title,
      String detail,
      List<FieldError> errors) {
    return new ErrorDetails(
        response.statusCode(),
        code,
        title,
        detail,
        errors,
        response.headers().firstValue("x-request-id").orElse(null),
        response.headers(),
        new String(response.body(), StandardCharsets.UTF_8));
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

  private static List<FieldError> fieldErrors(JsonNode problem) {
    List<FieldError> errors = new ArrayList<>();
    if (problem != null && problem.path("errors").isArray()) {
      for (JsonNode item : problem.get("errors")) {
        String path = text(item, "path");
        String message = text(item, "message");
        if (path != null && message != null) {
          errors.add(new FieldError(path, message));
        }
      }
    }
    return errors;
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

  private Errors() {}
}
