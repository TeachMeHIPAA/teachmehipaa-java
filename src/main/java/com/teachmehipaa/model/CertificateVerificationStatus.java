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
 * <code>valid</code>, <code>expired</code> or <code>revoked</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class CertificateVerificationStatus {
  public static final CertificateVerificationStatus VALID =
      new CertificateVerificationStatus("valid");
  public static final CertificateVerificationStatus EXPIRED =
      new CertificateVerificationStatus("expired");
  public static final CertificateVerificationStatus REVOKED =
      new CertificateVerificationStatus("revoked");

  private static final Map<String, CertificateVerificationStatus> KNOWN =
      Stream.of(VALID, EXPIRED, REVOKED)
          .collect(
              Collectors.toUnmodifiableMap(
                  CertificateVerificationStatus::value, Function.identity()));

  private final String value;

  private CertificateVerificationStatus(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static CertificateVerificationStatus of(String value) {
    Objects.requireNonNull(value, "value");
    CertificateVerificationStatus known = KNOWN.get(value);
    return known != null ? known : new CertificateVerificationStatus(value);
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
    return other instanceof CertificateVerificationStatus that && that.value.equals(value);
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
