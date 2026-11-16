package com.teachmehipaa.exception;

/**
 * A Delivery failed verification: it was not signed with the Signing secret for this request, or it
 * arrived outside the tolerance. Answer it with a 4xx and do not act on it.
 *
 * <p>{@link #getCode()} says why, as one of the constants here. The exception never carries the
 * Delivery's body or headers (ADR-0007), so it is safe to log.
 */
public class WebhookSignatureException extends TeachMeHipaaException {
  private static final long serialVersionUID = 1L;

  /** The Delivery lacks {@code x-tmh-signature} or {@code x-tmh-timestamp}. */
  public static final String MISSING_HEADER = "missing_header";

  /** {@code x-tmh-timestamp} is not an ISO 8601 date-time with an offset. */
  public static final String INVALID_TIMESTAMP = "invalid_timestamp";

  /** The Delivery was signed further from when it arrived than the tolerance allows. */
  public static final String TIMESTAMP_OUTSIDE_TOLERANCE = "timestamp_outside_tolerance";

  /** {@code x-tmh-body-hash} does not match the body, which was changed after signing. */
  public static final String BODY_HASH_MISMATCH = "body_hash_mismatch";

  /** {@code x-tmh-signature} does not match the body, path and Signing secret. */
  public static final String SIGNATURE_MISMATCH = "signature_mismatch";

  /**
   * @param code why verification failed, one of the constants here
   * @param message what went wrong and what to check
   */
  public WebhookSignatureException(String code, String message) {
    super(message, ErrorDetails.builder().code(code).build());
  }
}
