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
public final class PartnerOrganizationCreatedGrantScopesItem {
  public static final PartnerOrganizationCreatedGrantScopesItem ENROLLMENTS_READ =
      new PartnerOrganizationCreatedGrantScopesItem("enrollments:read");
  public static final PartnerOrganizationCreatedGrantScopesItem ENROLLMENTS_WRITE =
      new PartnerOrganizationCreatedGrantScopesItem("enrollments:write");
  public static final PartnerOrganizationCreatedGrantScopesItem CERTIFICATES_READ =
      new PartnerOrganizationCreatedGrantScopesItem("certificates:read");
  public static final PartnerOrganizationCreatedGrantScopesItem MEMBERS_READ =
      new PartnerOrganizationCreatedGrantScopesItem("members:read");
  public static final PartnerOrganizationCreatedGrantScopesItem AUDIT_READ =
      new PartnerOrganizationCreatedGrantScopesItem("audit:read");
  public static final PartnerOrganizationCreatedGrantScopesItem LEARNERS_READ =
      new PartnerOrganizationCreatedGrantScopesItem("learners:read");
  public static final PartnerOrganizationCreatedGrantScopesItem LEARNERS_WRITE =
      new PartnerOrganizationCreatedGrantScopesItem("learners:write");
  public static final PartnerOrganizationCreatedGrantScopesItem COMPLIANCE_READ =
      new PartnerOrganizationCreatedGrantScopesItem("compliance:read");
  public static final PartnerOrganizationCreatedGrantScopesItem SEATS_PURCHASE =
      new PartnerOrganizationCreatedGrantScopesItem("seats:purchase");
  public static final PartnerOrganizationCreatedGrantScopesItem WEBHOOKS_MANAGE =
      new PartnerOrganizationCreatedGrantScopesItem("webhooks:manage");

  private static final Map<String, PartnerOrganizationCreatedGrantScopesItem> KNOWN =
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
                  PartnerOrganizationCreatedGrantScopesItem::value, Function.identity()));

  private final String value;

  private PartnerOrganizationCreatedGrantScopesItem(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static PartnerOrganizationCreatedGrantScopesItem of(String value) {
    Objects.requireNonNull(value, "value");
    PartnerOrganizationCreatedGrantScopesItem known = KNOWN.get(value);
    return known != null ? known : new PartnerOrganizationCreatedGrantScopesItem(value);
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
    return other instanceof PartnerOrganizationCreatedGrantScopesItem that
        && that.value.equals(value);
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
