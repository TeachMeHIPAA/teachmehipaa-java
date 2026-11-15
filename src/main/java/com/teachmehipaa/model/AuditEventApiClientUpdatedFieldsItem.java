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
public final class AuditEventApiClientUpdatedFieldsItem {
  public static final AuditEventApiClientUpdatedFieldsItem NAME =
      new AuditEventApiClientUpdatedFieldsItem("name");
  public static final AuditEventApiClientUpdatedFieldsItem MAX_SEATS_PER_ORDER =
      new AuditEventApiClientUpdatedFieldsItem("maxSeatsPerOrder");
  public static final AuditEventApiClientUpdatedFieldsItem MAX_SPEND_PER30_DAYS =
      new AuditEventApiClientUpdatedFieldsItem("maxSpendPer30Days");
  public static final AuditEventApiClientUpdatedFieldsItem RETURN_URL_ALLOWLIST =
      new AuditEventApiClientUpdatedFieldsItem("returnUrlAllowlist");

  private static final Map<String, AuditEventApiClientUpdatedFieldsItem> KNOWN =
      Stream.of(NAME, MAX_SEATS_PER_ORDER, MAX_SPEND_PER30_DAYS, RETURN_URL_ALLOWLIST)
          .collect(
              Collectors.toUnmodifiableMap(
                  AuditEventApiClientUpdatedFieldsItem::value, Function.identity()));

  private final String value;

  private AuditEventApiClientUpdatedFieldsItem(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static AuditEventApiClientUpdatedFieldsItem of(String value) {
    Objects.requireNonNull(value, "value");
    AuditEventApiClientUpdatedFieldsItem known = KNOWN.get(value);
    return known != null ? known : new AuditEventApiClientUpdatedFieldsItem(value);
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
    return other instanceof AuditEventApiClientUpdatedFieldsItem that && that.value.equals(value);
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
