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
public final class ListAuditEventsTransition {
  public static final ListAuditEventsTransition BOUNCE = new ListAuditEventsTransition("bounce");
  public static final ListAuditEventsTransition START = new ListAuditEventsTransition("start");
  public static final ListAuditEventsTransition BEGIN_ASSESSMENT =
      new ListAuditEventsTransition("beginAssessment");
  public static final ListAuditEventsTransition SUBMIT_ASSESSMENT =
      new ListAuditEventsTransition("submitAssessment");
  public static final ListAuditEventsTransition CERTIFY = new ListAuditEventsTransition("certify");
  public static final ListAuditEventsTransition REVOKE_CERT =
      new ListAuditEventsTransition("revokeCert");
  public static final ListAuditEventsTransition ARCHIVE = new ListAuditEventsTransition("archive");
  public static final ListAuditEventsTransition UNARCHIVE =
      new ListAuditEventsTransition("unarchive");
  public static final ListAuditEventsTransition REINVITE =
      new ListAuditEventsTransition("reinvite");
  public static final ListAuditEventsTransition CANCEL_INVITE =
      new ListAuditEventsTransition("cancelInvite");

  private static final Map<String, ListAuditEventsTransition> KNOWN =
      Stream.of(
              BOUNCE,
              START,
              BEGIN_ASSESSMENT,
              SUBMIT_ASSESSMENT,
              CERTIFY,
              REVOKE_CERT,
              ARCHIVE,
              UNARCHIVE,
              REINVITE,
              CANCEL_INVITE)
          .collect(
              Collectors.toUnmodifiableMap(ListAuditEventsTransition::value, Function.identity()));

  private final String value;

  private ListAuditEventsTransition(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ListAuditEventsTransition of(String value) {
    Objects.requireNonNull(value, "value");
    ListAuditEventsTransition known = KNOWN.get(value);
    return known != null ? known : new ListAuditEventsTransition(value);
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
    return other instanceof ListAuditEventsTransition that && that.value.equals(value);
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
