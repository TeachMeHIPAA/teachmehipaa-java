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
 * <code>VALID</code>, <code>EXPIRED</code> or <code>REVOKED</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class CertificateStatus {
  public static final CertificateStatus VALID = new CertificateStatus("VALID");
  public static final CertificateStatus EXPIRED = new CertificateStatus("EXPIRED");
  public static final CertificateStatus REVOKED = new CertificateStatus("REVOKED");

  private static final Map<String, CertificateStatus> KNOWN =
      Stream.of(VALID, EXPIRED, REVOKED)
          .collect(Collectors.toUnmodifiableMap(CertificateStatus::value, Function.identity()));

  private final String value;

  private CertificateStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static CertificateStatus of(String value) {
    Objects.requireNonNull(value, "value");
    CertificateStatus known = KNOWN.get(value);
    return known != null ? known : new CertificateStatus(value);
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
    return other instanceof CertificateStatus that && that.value.equals(value);
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
