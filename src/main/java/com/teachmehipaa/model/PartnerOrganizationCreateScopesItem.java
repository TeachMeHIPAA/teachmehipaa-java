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
public final class PartnerOrganizationCreateScopesItem {
  public static final PartnerOrganizationCreateScopesItem ENROLLMENTS_READ =
      new PartnerOrganizationCreateScopesItem("enrollments:read");
  public static final PartnerOrganizationCreateScopesItem ENROLLMENTS_WRITE =
      new PartnerOrganizationCreateScopesItem("enrollments:write");
  public static final PartnerOrganizationCreateScopesItem CERTIFICATES_READ =
      new PartnerOrganizationCreateScopesItem("certificates:read");
  public static final PartnerOrganizationCreateScopesItem MEMBERS_READ =
      new PartnerOrganizationCreateScopesItem("members:read");
  public static final PartnerOrganizationCreateScopesItem AUDIT_READ =
      new PartnerOrganizationCreateScopesItem("audit:read");
  public static final PartnerOrganizationCreateScopesItem LEARNERS_READ =
      new PartnerOrganizationCreateScopesItem("learners:read");
  public static final PartnerOrganizationCreateScopesItem LEARNERS_WRITE =
      new PartnerOrganizationCreateScopesItem("learners:write");
  public static final PartnerOrganizationCreateScopesItem COMPLIANCE_READ =
      new PartnerOrganizationCreateScopesItem("compliance:read");
  public static final PartnerOrganizationCreateScopesItem SEATS_PURCHASE =
      new PartnerOrganizationCreateScopesItem("seats:purchase");
  public static final PartnerOrganizationCreateScopesItem WEBHOOKS_MANAGE =
      new PartnerOrganizationCreateScopesItem("webhooks:manage");

  private static final Map<String, PartnerOrganizationCreateScopesItem> KNOWN =
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
                  PartnerOrganizationCreateScopesItem::value, Function.identity()));

  private final String value;

  private PartnerOrganizationCreateScopesItem(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static PartnerOrganizationCreateScopesItem of(String value) {
    Objects.requireNonNull(value, "value");
    PartnerOrganizationCreateScopesItem known = KNOWN.get(value);
    return known != null ? known : new PartnerOrganizationCreateScopesItem(value);
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
    return other instanceof PartnerOrganizationCreateScopesItem that && that.value.equals(value);
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
