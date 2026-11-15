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
public final class AuditEventOrganizationUpdatedFieldsItem {
  public static final AuditEventOrganizationUpdatedFieldsItem NAME =
      new AuditEventOrganizationUpdatedFieldsItem("name");
  public static final AuditEventOrganizationUpdatedFieldsItem TRAINING_NEEDS =
      new AuditEventOrganizationUpdatedFieldsItem("trainingNeeds");
  public static final AuditEventOrganizationUpdatedFieldsItem SNOOZE_DURATION =
      new AuditEventOrganizationUpdatedFieldsItem("snoozeDuration");
  public static final AuditEventOrganizationUpdatedFieldsItem AUTO_RECERTIFY =
      new AuditEventOrganizationUpdatedFieldsItem("autoRecertify");

  private static final Map<String, AuditEventOrganizationUpdatedFieldsItem> KNOWN =
      Stream.of(NAME, TRAINING_NEEDS, SNOOZE_DURATION, AUTO_RECERTIFY)
          .collect(
              Collectors.toUnmodifiableMap(
                  AuditEventOrganizationUpdatedFieldsItem::value, Function.identity()));

  private final String value;

  private AuditEventOrganizationUpdatedFieldsItem(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static AuditEventOrganizationUpdatedFieldsItem of(String value) {
    Objects.requireNonNull(value, "value");
    AuditEventOrganizationUpdatedFieldsItem known = KNOWN.get(value);
    return known != null ? known : new AuditEventOrganizationUpdatedFieldsItem(value);
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
    return other instanceof AuditEventOrganizationUpdatedFieldsItem that
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
