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
 * Whether the holder could be resolved: <code>resolved</code>, <code>none</code> or <code>
 * lookup_failed</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class CertificateHolderStatus {
  public static final CertificateHolderStatus RESOLVED = new CertificateHolderStatus("resolved");
  public static final CertificateHolderStatus NONE = new CertificateHolderStatus("none");
  public static final CertificateHolderStatus LOOKUP_FAILED =
      new CertificateHolderStatus("lookup_failed");

  private static final Map<String, CertificateHolderStatus> KNOWN =
      Stream.of(RESOLVED, NONE, LOOKUP_FAILED)
          .collect(
              Collectors.toUnmodifiableMap(CertificateHolderStatus::value, Function.identity()));

  private final String value;

  private CertificateHolderStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static CertificateHolderStatus of(String value) {
    Objects.requireNonNull(value, "value");
    CertificateHolderStatus known = KNOWN.get(value);
    return known != null ? known : new CertificateHolderStatus(value);
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
    return other instanceof CertificateHolderStatus that && that.value.equals(value);
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
