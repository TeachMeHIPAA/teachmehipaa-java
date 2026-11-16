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
 * The action that was accepted: <code>remind</code> or <code>resendInvite</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class EnrollmentActionAcceptedAction {
  public static final EnrollmentActionAcceptedAction REMIND =
      new EnrollmentActionAcceptedAction("remind");
  public static final EnrollmentActionAcceptedAction RESEND_INVITE =
      new EnrollmentActionAcceptedAction("resendInvite");

  private static final Map<String, EnrollmentActionAcceptedAction> KNOWN =
      Stream.of(REMIND, RESEND_INVITE)
          .collect(
              Collectors.toUnmodifiableMap(
                  EnrollmentActionAcceptedAction::value, Function.identity()));

  private final String value;

  private EnrollmentActionAcceptedAction(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static EnrollmentActionAcceptedAction of(String value) {
    Objects.requireNonNull(value, "value");
    EnrollmentActionAcceptedAction known = KNOWN.get(value);
    return known != null ? known : new EnrollmentActionAcceptedAction(value);
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
    return other instanceof EnrollmentActionAcceptedAction that && that.value.equals(value);
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
