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
 * <code>admin</code> or <code>learner</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class MemberRole {
  public static final MemberRole ADMIN = new MemberRole("admin");
  public static final MemberRole LEARNER = new MemberRole("learner");

  private static final Map<String, MemberRole> KNOWN =
      Stream.of(ADMIN, LEARNER)
          .collect(Collectors.toUnmodifiableMap(MemberRole::value, Function.identity()));

  private final String value;

  private MemberRole(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static MemberRole of(String value) {
    Objects.requireNonNull(value, "value");
    MemberRole known = KNOWN.get(value);
    return known != null ? known : new MemberRole(value);
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
    return other instanceof MemberRole that && that.value.equals(value);
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
