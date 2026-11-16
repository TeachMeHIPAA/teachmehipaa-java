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
public final class ListAuditEventsType {
  public static final ListAuditEventsType ENROLLMENT_TRANSITION =
      new ListAuditEventsType("enrollment.transition");
  public static final ListAuditEventsType ORGANIZATION_UPDATED =
      new ListAuditEventsType("organization.updated");
  public static final ListAuditEventsType ORGANIZATION_CLOSED =
      new ListAuditEventsType("organization.closed");
  public static final ListAuditEventsType ORGANIZATION_REOPENED =
      new ListAuditEventsType("organization.reopened");
  public static final ListAuditEventsType MEMBER_ROLE_CHANGED =
      new ListAuditEventsType("member.role_changed");
  public static final ListAuditEventsType MEMBER_TRANSFERRED =
      new ListAuditEventsType("member.transferred");
  public static final ListAuditEventsType MEMBER_REMOVED =
      new ListAuditEventsType("member.removed");
  public static final ListAuditEventsType CERTIFICATE_ISSUED =
      new ListAuditEventsType("certificate.issued");
  public static final ListAuditEventsType CERTIFICATE_REVOKED =
      new ListAuditEventsType("certificate.revoked");
  public static final ListAuditEventsType SEAT_GRANTED = new ListAuditEventsType("seat.granted");
  public static final ListAuditEventsType API_CLIENT_CREATED =
      new ListAuditEventsType("api_client.created");
  public static final ListAuditEventsType API_CLIENT_SECRET_ROTATED =
      new ListAuditEventsType("api_client.secret_rotated");
  public static final ListAuditEventsType API_CLIENT_REVOKED =
      new ListAuditEventsType("api_client.revoked");
  public static final ListAuditEventsType LEARNER_UPDATED =
      new ListAuditEventsType("learner.updated");
  public static final ListAuditEventsType LEARNER_LAUNCH_LINK_CREATED =
      new ListAuditEventsType("learner.launch_link_created");
  public static final ListAuditEventsType API_CLIENT_UPDATED =
      new ListAuditEventsType("api_client.updated");

  private static final Map<String, ListAuditEventsType> KNOWN =
      Stream.of(
              ENROLLMENT_TRANSITION,
              ORGANIZATION_UPDATED,
              ORGANIZATION_CLOSED,
              ORGANIZATION_REOPENED,
              MEMBER_ROLE_CHANGED,
              MEMBER_TRANSFERRED,
              MEMBER_REMOVED,
              CERTIFICATE_ISSUED,
              CERTIFICATE_REVOKED,
              SEAT_GRANTED,
              API_CLIENT_CREATED,
              API_CLIENT_SECRET_ROTATED,
              API_CLIENT_REVOKED,
              LEARNER_UPDATED,
              LEARNER_LAUNCH_LINK_CREATED,
              API_CLIENT_UPDATED)
          .collect(Collectors.toUnmodifiableMap(ListAuditEventsType::value, Function.identity()));

  private final String value;

  private ListAuditEventsType(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ListAuditEventsType of(String value) {
    Objects.requireNonNull(value, "value");
    ListAuditEventsType known = KNOWN.get(value);
    return known != null ? known : new ListAuditEventsType(value);
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
    return other instanceof ListAuditEventsType that && that.value.equals(value);
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
