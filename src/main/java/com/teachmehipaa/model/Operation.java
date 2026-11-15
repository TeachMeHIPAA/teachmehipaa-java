package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.LastResponse;
import com.teachmehipaa.exception.ErrorDetails;
import com.teachmehipaa.exception.FieldError;
import com.teachmehipaa.exception.OperationFailedException;
import com.teachmehipaa.exception.OperationInterruptedException;
import com.teachmehipaa.exception.OperationTimeoutException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Work that may finish after the request that started it, such as an export or a redrive (grilling
 * Q24). {@link #getResponse()} holds the result once {@link #isDone()} is true, unless {@link
 * #getError()} holds the problem.
 *
 * <p>Most Operations are done when they are returned; Test deliveries and redrives finish later, so
 * wait for them:
 *
 * <pre>{@code
 * WebhookDeliveryOperation delivery =
 *     client.webhookSubscriptions().test(subscriptionId).waitForCompletion();
 * }</pre>
 */
public sealed class Operation extends OperationBase permits WebhookDeliveryOperation {
  /** How long {@link #waitForCompletion()} waits in all: 5 minutes. */
  public static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofMinutes(5);

  /** How long {@link #waitForCompletion()} waits before its first poll: 1 second. */
  public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);

  private static final Duration MAX_POLL_INTERVAL = Duration.ofSeconds(10);
  // The same call as the generated operations().retrieve().
  private static final String POLL_PATH = "/organizations/{orgId}/operations/{operationId}";

  @JsonCreator
  Operation(
      @JsonProperty("name") String name,
      @JsonProperty("done") boolean done,
      @JsonProperty("metadata") Map<String, JsonNode> metadata,
      @JsonProperty("response") JsonNode response,
      @JsonProperty("error") Problem error) {
    super(name, done, metadata, response, error);
  }

  /**
   * Polls until the Operation is done, for up to {@link #DEFAULT_WAIT_TIMEOUT}, starting {@link
   * #DEFAULT_POLL_INTERVAL} apart.
   *
   * @see #waitForCompletion(Duration, Duration)
   */
  public Operation waitForCompletion() {
    return waitForCompletion(DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
  }

  /**
   * Polls until the Operation is done and returns its final state. An Operation that is already
   * done returns itself, without a request.
   *
   * <p>The first poll comes {@code interval} after the call; later ones back off, doubling to 10 s
   * apart with jitter, never closer than {@code interval}. The last poll lands on the {@code
   * timeout}. The client's {@code sleeper} and {@code clock} do the waiting and timekeeping.
   *
   * @param timeout how long to wait in all; zero or more
   * @param interval how long to wait before the first poll; more than zero
   * @return the done Operation, of this Operation's own class
   * @throws OperationFailedException when the Operation finished with an error
   * @throws OperationTimeoutException when it is not done within {@code timeout}
   * @throws OperationInterruptedException when the thread is interrupted while waiting; the thread
   *     keeps its interrupt flag
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when a poll fails
   * @throws IllegalArgumentException when {@code timeout} or {@code interval} is out of range
   * @throws IllegalStateException when this Operation was not returned by an API call
   */
  public Operation waitForCompletion(Duration timeout, Duration interval) {
    Objects.requireNonNull(timeout, "timeout");
    Objects.requireNonNull(interval, "interval");
    if (timeout.isNegative()) {
      throw new IllegalArgumentException("timeout must be zero or more");
    }
    if (interval.isZero() || interval.isNegative()) {
      throw new IllegalArgumentException("interval must be more than zero");
    }
    if (isDone()) {
      return settled();
    }
    ApiRequester requester = requester();
    ApiRequest poll =
        ApiRequest.builder("GET", POLL_PATH)
            .pathParam("operationId", operationIdFromName(getName()))
            .retrySafe()
            .build();
    Instant deadline = requester.clock().instant().plus(timeout);
    Operation latest = this;
    for (int pollNumber = 1; ; pollNumber++) {
      Duration remaining = Duration.between(requester.clock().instant(), deadline);
      if (remaining.isZero() || remaining.isNegative()) {
        throw timedOut(latest, timeout);
      }
      Duration delay = pollDelay(interval, pollNumber);
      try {
        requester.sleeper().sleep(delay.compareTo(remaining) < 0 ? delay : remaining);
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        throw new OperationInterruptedException(
            "Interrupted while waiting for Operation " + getName(),
            details(latest, null, null, null, null, List.of()),
            latest,
            interrupted);
      }
      // Read into this Operation's own class, so a WebhookDeliveryOperation stays one.
      latest = requester.request(poll, getClass(), RequestOptions.none());
      if (latest.isDone()) {
        return latest.settled();
      }
      // That poll was the one at the deadline.
      if (delay.compareTo(remaining) >= 0) {
        throw timedOut(latest, timeout);
      }
    }
  }

  /**
   * The CSV file of a done export Operation: {@code enrollments().export}, {@code
   * certificates().export} or {@code learners().export}.
   *
   * @throws OperationFailedException when the export failed
   * @throws IllegalStateException when the Operation is not done, or is not a CSV export
   */
  public ExportResult getExportResult() {
    if (!isDone()) {
      throw new IllegalStateException(
          "Operation " + getName() + " is not done; call waitForCompletion() first");
    }
    settled();
    // The spec says only that an export's response "holds the CSV file". The API sends it inline
    // as the monorepo's ExportArtifact: {format: "csv", contentType, filename, rowCount,
    // dataBase64}.
    JsonNode artifact = getResponse();
    if (artifact == null
        || !artifact.path("format").asText("").equals("csv")
        || !artifact.path("dataBase64").isTextual()) {
      throw notAnExport();
    }
    byte[] csv;
    try {
      csv = Base64.getDecoder().decode(artifact.get("dataBase64").asText());
    } catch (IllegalArgumentException notBase64) {
      throw notAnExport();
    }
    JsonNode filename = artifact.path("filename");
    JsonNode rowCount = artifact.path("rowCount");
    return new ExportResult(
        new String(csv, StandardCharsets.UTF_8),
        filename.isTextual() ? filename.asText() : null,
        rowCount.isIntegralNumber() ? rowCount.asLong() : null);
  }

  private IllegalStateException notAnExport() {
    return new IllegalStateException("Operation " + getName() + " did not produce a CSV export");
  }

  /**
   * The operation id in an Operation's name, which is {@code
   * organizations/{orgId}/operations/{operationId}}: what {@code client.operations().retrieve}
   * takes.
   *
   * @throws IllegalArgumentException when {@code name} is not an Operation name
   */
  public static String operationIdFromName(String name) {
    Objects.requireNonNull(name, "name");
    String[] parts = name.split("/", -1);
    if (parts.length != 4
        || !parts[0].equals("organizations")
        || !parts[2].equals("operations")
        || !isSegment(parts[1])
        || !isSegment(parts[3])) {
      throw new IllegalArgumentException(
          "\"" + name + "\" is not an Operation name (organizations/{orgId}/operations/{id})");
    }
    return parts[3];
  }

  private static boolean isSegment(String part) {
    return !part.isEmpty() && part.chars().noneMatch(Character::isWhitespace);
  }

  // The ceiling doubles from the interval up to 10 s, and jitter picks 50-100 % of it so that many
  // waiters don't poll in step. Nothing waits less than the interval, and an interval longer than
  // the cap stays as it is.
  private static Duration pollDelay(Duration interval, int pollNumber) {
    double base = interval.toNanos();
    double cap = Math.max(MAX_POLL_INTERVAL.toNanos(), base);
    double ceiling = Math.min(base * Math.pow(2, pollNumber - 1), cap);
    double jittered = ceiling * (0.5 + ThreadLocalRandom.current().nextDouble() * 0.5);
    return Duration.ofNanos(Math.round(Math.max(base, jittered)));
  }

  /** This finished Operation, or the error it finished with. */
  Operation settled() {
    Problem problem = getError();
    if (problem == null) {
      return this;
    }
    String summary = join(problem.getTitle(), problem.getDetail());
    List<FieldError> errors =
        problem.getErrors() == null
            ? List.of()
            : problem.getErrors().stream()
                .filter(item -> item.getPath() != null && item.getMessage() != null)
                .map(item -> new FieldError(item.getPath(), item.getMessage()))
                .toList();
    throw new OperationFailedException(
        summary == null ? "Operation " + getName() + " failed" : summary,
        details(
            this,
            problem.getStatus() == 0 ? null : (int) problem.getStatus(),
            problem.getCode() == null ? null : problem.getCode().value(),
            problem.getTitle(),
            problem.getDetail(),
            errors),
        this);
  }

  private OperationTimeoutException timedOut(Operation latest, Duration timeout) {
    String seconds = BigDecimal.valueOf(timeout.toMillis(), 3).stripTrailingZeros().toPlainString();
    return new OperationTimeoutException(
        "Operation " + getName() + " was not done after " + seconds + " s",
        details(latest, null, null, null, null, List.of()),
        latest);
  }

  private static ErrorDetails details(
      Operation operation,
      Integer status,
      String code,
      String title,
      String detail,
      List<FieldError> errors) {
    LastResponse response = operation.getLastResponse();
    return new ErrorDetails(
        status,
        code,
        title,
        detail,
        errors,
        response == null ? null : response.requestId(),
        response == null ? null : response.headers(),
        null);
  }

  private static String join(String first, String second) {
    if (first == null) {
      return second;
    }
    return second == null ? first : first + ": " + second;
  }
}
