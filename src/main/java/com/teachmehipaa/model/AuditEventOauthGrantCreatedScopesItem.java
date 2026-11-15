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
public final class AuditEventOauthGrantCreatedScopesItem {
  public static final AuditEventOauthGrantCreatedScopesItem ENROLLMENTS_READ =
      new AuditEventOauthGrantCreatedScopesItem("enrollments:read");
  public static final AuditEventOauthGrantCreatedScopesItem ENROLLMENTS_WRITE =
      new AuditEventOauthGrantCreatedScopesItem("enrollments:write");
  public static final AuditEventOauthGrantCreatedScopesItem CERTIFICATES_READ =
      new AuditEventOauthGrantCreatedScopesItem("certificates:read");
  public static final AuditEventOauthGrantCreatedScopesItem MEMBERS_READ =
      new AuditEventOauthGrantCreatedScopesItem("members:read");
  public static final AuditEventOauthGrantCreatedScopesItem AUDIT_READ =
      new AuditEventOauthGrantCreatedScopesItem("audit:read");
  public static final AuditEventOauthGrantCreatedScopesItem LEARNERS_READ =
      new AuditEventOauthGrantCreatedScopesItem("learners:read");
  public static final AuditEventOauthGrantCreatedScopesItem LEARNERS_WRITE =
      new AuditEventOauthGrantCreatedScopesItem("learners:write");
  public static final AuditEventOauthGrantCreatedScopesItem COMPLIANCE_READ =
      new AuditEventOauthGrantCreatedScopesItem("compliance:read");
  public static final AuditEventOauthGrantCreatedScopesItem SEATS_PURCHASE =
      new AuditEventOauthGrantCreatedScopesItem("seats:purchase");
  public static final AuditEventOauthGrantCreatedScopesItem WEBHOOKS_MANAGE =
      new AuditEventOauthGrantCreatedScopesItem("webhooks:manage");

  private static final Map<String, AuditEventOauthGrantCreatedScopesItem> KNOWN =
      Stream.of(
              ENROLLMENTS_READ,
              ENROLLMENTS_WRITE,
              CERTIFICATES_READ,
              MEMBERS_READ,
              AUDIT_READ,
              LEARNERS_READ,
              LEARNERS_WRITE,
              COMPLIANCE_READ,
              SEATS_PURCHASE,
              WEBHOOKS_MANAGE)
          .collect(
              Collectors.toUnmodifiableMap(
                  AuditEventOauthGrantCreatedScopesItem::value, Function.identity()));

  private final String value;

  private AuditEventOauthGrantCreatedScopesItem(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static AuditEventOauthGrantCreatedScopesItem of(String value) {
    Objects.requireNonNull(value, "value");
    AuditEventOauthGrantCreatedScopesItem known = KNOWN.get(value);
    return known != null ? known : new AuditEventOauthGrantCreatedScopesItem(value);
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
    return other instanceof AuditEventOauthGrantCreatedScopesItem that && that.value.equals(value);
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
