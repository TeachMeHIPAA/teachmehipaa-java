// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The operation that was run.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class EnrollmentsBatchResultsOperation {
  public static final EnrollmentsBatchResultsOperation REMIND =
      new EnrollmentsBatchResultsOperation("remind");
  public static final EnrollmentsBatchResultsOperation RESEND_INVITE =
      new EnrollmentsBatchResultsOperation("resendInvite");
  public static final EnrollmentsBatchResultsOperation CANCEL =
      new EnrollmentsBatchResultsOperation("cancel");
  public static final EnrollmentsBatchResultsOperation RECERTIFY =
      new EnrollmentsBatchResultsOperation("recertify");
  public static final EnrollmentsBatchResultsOperation ARCHIVE =
      new EnrollmentsBatchResultsOperation("archive");
  public static final EnrollmentsBatchResultsOperation UNARCHIVE =
      new EnrollmentsBatchResultsOperation("unarchive");

  private static final Map<String, EnrollmentsBatchResultsOperation> KNOWN =
      Stream.of(REMIND, RESEND_INVITE, CANCEL, RECERTIFY, ARCHIVE, UNARCHIVE)
          .collect(
              Collectors.toUnmodifiableMap(
                  EnrollmentsBatchResultsOperation::value, Function.identity()));

  private final String value;

  private EnrollmentsBatchResultsOperation(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentsBatchResultsOperation of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentsBatchResultsOperation known = KNOWN.get(value);
    return known != null ? known : new EnrollmentsBatchResultsOperation(value);
  }

  /** The wire value. */
  @JsonValue
  public String value() {
    return value;
  }

  /** Whether this version of the library knows this value. */
  public boolean isKnown() {
    return KNOWN.containsKey(value);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EnrollmentsBatchResultsOperation that && that.value.equals(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}
