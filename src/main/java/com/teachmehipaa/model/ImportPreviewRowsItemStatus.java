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
public final class ImportPreviewRowsItemStatus {
  public static final ImportPreviewRowsItemStatus VALID = new ImportPreviewRowsItemStatus("valid");
  public static final ImportPreviewRowsItemStatus INVALID_EMAIL =
      new ImportPreviewRowsItemStatus("invalid_email");
  public static final ImportPreviewRowsItemStatus DUPLICATE_IN_FILE =
      new ImportPreviewRowsItemStatus("duplicate_in_file");
  public static final ImportPreviewRowsItemStatus ALREADY_ENROLLED =
      new ImportPreviewRowsItemStatus("already_enrolled");
  public static final ImportPreviewRowsItemStatus ALREADY_INVITED =
      new ImportPreviewRowsItemStatus("already_invited");

  private static final Map<String, ImportPreviewRowsItemStatus> KNOWN =
      Stream.of(VALID, INVALID_EMAIL, DUPLICATE_IN_FILE, ALREADY_ENROLLED, ALREADY_INVITED)
          .collect(
              Collectors.toUnmodifiableMap(
                  ImportPreviewRowsItemStatus::value, Function.identity()));

  private final String value;

  private ImportPreviewRowsItemStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ImportPreviewRowsItemStatus of(String value) {
    Objects.requireNonNull(value, "value");
    ImportPreviewRowsItemStatus known = KNOWN.get(value);
    return known != null ? known : new ImportPreviewRowsItemStatus(value);
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
    return other instanceof ImportPreviewRowsItemStatus that && that.value.equals(value);
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
