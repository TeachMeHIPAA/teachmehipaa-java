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
 * The known values are constants; the API may add values at any time, and {@link #of} accepts them
 * too (ADR-0006).
 */
public final class TokenIntrospectionScopesItem {
  public static final TokenIntrospectionScopesItem ME = new TokenIntrospectionScopesItem("me");
  public static final TokenIntrospectionScopesItem ENROLLMENTS_READ =
      new TokenIntrospectionScopesItem("enrollments:read");
  public static final TokenIntrospectionScopesItem ENROLLMENTS_WRITE =
      new TokenIntrospectionScopesItem("enrollments:write");
  public static final TokenIntrospectionScopesItem MEMBERS_READ =
      new TokenIntrospectionScopesItem("members:read");
  public static final TokenIntrospectionScopesItem MEMBERS_ADMIN =
      new TokenIntrospectionScopesItem("members:admin");
  public static final TokenIntrospectionScopesItem CERTIFICATES_READ =
      new TokenIntrospectionScopesItem("certificates:read");
  public static final TokenIntrospectionScopesItem CERTIFICATES_WRITE =
      new TokenIntrospectionScopesItem("certificates:write");
  public static final TokenIntrospectionScopesItem BILLING_WRITE =
      new TokenIntrospectionScopesItem("billing:write");
  public static final TokenIntrospectionScopesItem ORG_ADMIN =
      new TokenIntrospectionScopesItem("org:admin");
  public static final TokenIntrospectionScopesItem AUDIT_READ =
      new TokenIntrospectionScopesItem("audit:read");
  public static final TokenIntrospectionScopesItem SCIM = new TokenIntrospectionScopesItem("scim");
  public static final TokenIntrospectionScopesItem SUPPORT_WRITE =
      new TokenIntrospectionScopesItem("support:write");
  public static final TokenIntrospectionScopesItem LEARNERS_READ =
      new TokenIntrospectionScopesItem("learners:read");
  public static final TokenIntrospectionScopesItem LEARNERS_WRITE =
      new TokenIntrospectionScopesItem("learners:write");
  public static final TokenIntrospectionScopesItem COMPLIANCE_READ =
      new TokenIntrospectionScopesItem("compliance:read");
  public static final TokenIntrospectionScopesItem WEBHOOKS_MANAGE =
      new TokenIntrospectionScopesItem("webhooks:manage");

  private static final Map<String, TokenIntrospectionScopesItem> KNOWN =
      Stream.of(
              ME,
              ENROLLMENTS_READ,
              ENROLLMENTS_WRITE,
              MEMBERS_READ,
              MEMBERS_ADMIN,
              CERTIFICATES_READ,
              CERTIFICATES_WRITE,
              BILLING_WRITE,
              ORG_ADMIN,
              AUDIT_READ,
              SCIM,
              SUPPORT_WRITE,
              LEARNERS_READ,
              LEARNERS_WRITE,
              COMPLIANCE_READ,
              WEBHOOKS_MANAGE)
          .collect(
              Collectors.toUnmodifiableMap(
                  TokenIntrospectionScopesItem::value, Function.identity()));

  private final String value;

  private TokenIntrospectionScopesItem(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static TokenIntrospectionScopesItem of(String value) {
    Objects.requireNonNull(value, "value");
    TokenIntrospectionScopesItem known = KNOWN.get(value);
    return known != null ? known : new TokenIntrospectionScopesItem(value);
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
    return other instanceof TokenIntrospectionScopesItem that && that.value.equals(value);
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
