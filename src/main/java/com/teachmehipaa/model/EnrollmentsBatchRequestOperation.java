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
 * What to do to each enrollment: <code>remind</code>, <code>resendInvite</code>, <code>cancel
 * </code>, <code>recertify</code>, <code>archive</code> or <code>unarchive</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class EnrollmentsBatchRequestOperation {
  public static final EnrollmentsBatchRequestOperation REMIND =
      new EnrollmentsBatchRequestOperation("remind");
  public static final EnrollmentsBatchRequestOperation RESEND_INVITE =
      new EnrollmentsBatchRequestOperation("resendInvite");
  public static final EnrollmentsBatchRequestOperation CANCEL =
      new EnrollmentsBatchRequestOperation("cancel");
  public static final EnrollmentsBatchRequestOperation RECERTIFY =
      new EnrollmentsBatchRequestOperation("recertify");
  public static final EnrollmentsBatchRequestOperation ARCHIVE =
      new EnrollmentsBatchRequestOperation("archive");
  public static final EnrollmentsBatchRequestOperation UNARCHIVE =
      new EnrollmentsBatchRequestOperation("unarchive");

  private static final Map<String, EnrollmentsBatchRequestOperation> KNOWN =
      Stream.of(REMIND, RESEND_INVITE, CANCEL, RECERTIFY, ARCHIVE, UNARCHIVE)
          .collect(
              Collectors.toUnmodifiableMap(
                  EnrollmentsBatchRequestOperation::value, Function.identity()));

  private final String value;

  private EnrollmentsBatchRequestOperation(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentsBatchRequestOperation of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentsBatchRequestOperation known = KNOWN.get(value);
    return known != null ? known : new EnrollmentsBatchRequestOperation(value);
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
    return other instanceof EnrollmentsBatchRequestOperation that && that.value.equals(value);
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
