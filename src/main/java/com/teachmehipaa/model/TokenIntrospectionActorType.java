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
 * <code>client</code> for an API client, <code>user</code> for a person.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class TokenIntrospectionActorType {
  public static final TokenIntrospectionActorType USER = new TokenIntrospectionActorType("user");
  public static final TokenIntrospectionActorType CLIENT =
      new TokenIntrospectionActorType("client");

  private static final Map<String, TokenIntrospectionActorType> KNOWN =
      Stream.of(USER, CLIENT)
          .collect(
              Collectors.toUnmodifiableMap(
                  TokenIntrospectionActorType::value, Function.identity()));

  private final String value;

  private TokenIntrospectionActorType(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static TokenIntrospectionActorType of(String value) {
    Objects.requireNonNull(value, "value");
    TokenIntrospectionActorType known = KNOWN.get(value);
    return known != null ? known : new TokenIntrospectionActorType(value);
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
    return other instanceof TokenIntrospectionActorType that && that.value.equals(value);
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
