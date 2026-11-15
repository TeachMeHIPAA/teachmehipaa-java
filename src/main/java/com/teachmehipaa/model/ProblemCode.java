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
 * A machine-readable error code, such as <code>seat_unavailable</code>.
 *
 * <p>The known values are constants; the API may add values at any time, and {@link #of} accepts
 * them too (ADR-0006).
 */
public final class ProblemCode {
  public static final ProblemCode VALIDATION_FAILED = new ProblemCode("validation_failed");
  public static final ProblemCode UNAUTHORIZED = new ProblemCode("unauthorized");
  public static final ProblemCode FORBIDDEN = new ProblemCode("forbidden");
  public static final ProblemCode NOT_FOUND = new ProblemCode("not_found");
  public static final ProblemCode METHOD_NOT_ALLOWED = new ProblemCode("method_not_allowed");
  public static final ProblemCode NOT_ACCEPTABLE = new ProblemCode("not_acceptable");
  public static final ProblemCode CONFLICT = new ProblemCode("conflict");
  public static final ProblemCode RATE_LIMITED = new ProblemCode("rate_limited");
  public static final ProblemCode PRECONDITION_REQUIRED = new ProblemCode("precondition_required");
  public static final ProblemCode PRECONDITION_FAILED = new ProblemCode("precondition_failed");
  public static final ProblemCode IDEMPOTENCY_KEY_REUSE = new ProblemCode("idempotency_key_reuse");
  public static final ProblemCode UNSUPPORTED_MEDIA_TYPE =
      new ProblemCode("unsupported_media_type");
  public static final ProblemCode ILLEGAL_TRANSITION = new ProblemCode("illegal_transition");
  public static final ProblemCode SEAT_UNAVAILABLE = new ProblemCode("seat_unavailable");
  public static final ProblemCode SEAT_ALREADY_CLAIMED = new ProblemCode("seat_already_claimed");
  public static final ProblemCode ALREADY_INVITED = new ProblemCode("already_invited");
  public static final ProblemCode EMAIL_ALREADY_REGISTERED =
      new ProblemCode("email_already_registered");
  public static final ProblemCode LAST_ADMIN_REMOVAL_BLOCKED =
      new ProblemCode("last_admin_removal_blocked");
  public static final ProblemCode ORG_NOT_ACTIVE = new ProblemCode("org_not_active");
  public static final ProblemCode ENROLLMENT_ARCHIVED = new ProblemCode("enrollment_archived");
  public static final ProblemCode LESSONS_INCOMPLETE = new ProblemCode("lessons_incomplete");
  public static final ProblemCode BATCH_TOO_LARGE = new ProblemCode("batch_too_large");
  public static final ProblemCode IMPORT_TOO_LARGE = new ProblemCode("import_too_large");
  public static final ProblemCode EXPORT_TOO_LARGE = new ProblemCode("export_too_large");
  public static final ProblemCode SUMMARY_TOO_LARGE = new ProblemCode("summary_too_large");
  public static final ProblemCode SEAT_NOT_REFUNDABLE = new ProblemCode("seat_not_refundable");
  public static final ProblemCode REFUND_FAILED = new ProblemCode("refund_failed");
  public static final ProblemCode NOT_CONFIGURED = new ProblemCode("not_configured");
  public static final ProblemCode RESET_UNAVAILABLE = new ProblemCode("reset_unavailable");
  public static final ProblemCode AUTO_RECERTIFY_UNAVAILABLE =
      new ProblemCode("auto_recertify_unavailable");
  public static final ProblemCode API_CLIENT_LIMIT_REACHED =
      new ProblemCode("api_client_limit_reached");
  public static final ProblemCode EXTERNAL_ID_CONFLICT = new ProblemCode("external_id_conflict");
  public static final ProblemCode CANNOT_OFFBOARD_ADMIN = new ProblemCode("cannot_offboard_admin");
  public static final ProblemCode SPEND_LIMIT_EXCEEDED = new ProblemCode("spend_limit_exceeded");
  public static final ProblemCode PAYMENT_METHOD_NOT_AVAILABLE =
      new ProblemCode("payment_method_not_available");
  public static final ProblemCode RETURN_URL_NOT_ALLOWED =
      new ProblemCode("return_url_not_allowed");
  public static final ProblemCode CURRENCY_NOT_AVAILABLE =
      new ProblemCode("currency_not_available");
  public static final ProblemCode PAYMENT_METHOD_REQUIRED =
      new ProblemCode("payment_method_required");
  public static final ProblemCode PAYMENT_METHOD_CHANGED =
      new ProblemCode("payment_method_changed");
  public static final ProblemCode PRICE_CHANGED = new ProblemCode("price_changed");
  public static final ProblemCode SUPERSEDED = new ProblemCode("superseded");
  public static final ProblemCode SETUP_NOT_SUCCEEDED = new ProblemCode("setup_not_succeeded");
  public static final ProblemCode INVOICE_BILLING_NOT_ENABLED =
      new ProblemCode("invoice_billing_not_enabled");
  public static final ProblemCode CANNOT_LAUNCH_ADMIN = new ProblemCode("cannot_launch_admin");
  public static final ProblemCode LEARNER_REMOVED = new ProblemCode("learner_removed");
  public static final ProblemCode LEARNER_NOT_ACCEPTED = new ProblemCode("learner_not_accepted");
  public static final ProblemCode PARTNER_ORG_LIMIT_REACHED =
      new ProblemCode("partner_org_limit_reached");
  public static final ProblemCode BILLING_CONTACT_REQUIRED =
      new ProblemCode("billing_contact_required");
  public static final ProblemCode CANNOT_RENAME_ADMIN = new ProblemCode("cannot_rename_admin");
  public static final ProblemCode LEARNER_IN_MULTIPLE_ORGS =
      new ProblemCode("learner_in_multiple_orgs");
  public static final ProblemCode ACCOUNT_EXISTS_USE_SIGN_IN =
      new ProblemCode("account_exists_use_sign_in");
  public static final ProblemCode NAME_CHANGE_NOT_ALLOWED =
      new ProblemCode("name_change_not_allowed");
  public static final ProblemCode TEMPORARILY_UNAVAILABLE =
      new ProblemCode("temporarily_unavailable");

  private static final Map<String, ProblemCode> KNOWN =
      Stream.of(
              VALIDATION_FAILED,
              UNAUTHORIZED,
              FORBIDDEN,
              NOT_FOUND,
              METHOD_NOT_ALLOWED,
              NOT_ACCEPTABLE,
              CONFLICT,
              RATE_LIMITED,
              PRECONDITION_REQUIRED,
              PRECONDITION_FAILED,
              IDEMPOTENCY_KEY_REUSE,
              UNSUPPORTED_MEDIA_TYPE,
              ILLEGAL_TRANSITION,
              SEAT_UNAVAILABLE,
              SEAT_ALREADY_CLAIMED,
              ALREADY_INVITED,
              EMAIL_ALREADY_REGISTERED,
              LAST_ADMIN_REMOVAL_BLOCKED,
              ORG_NOT_ACTIVE,
              ENROLLMENT_ARCHIVED,
              LESSONS_INCOMPLETE,
              BATCH_TOO_LARGE,
              IMPORT_TOO_LARGE,
              EXPORT_TOO_LARGE,
              SUMMARY_TOO_LARGE,
              SEAT_NOT_REFUNDABLE,
              REFUND_FAILED,
              NOT_CONFIGURED,
              RESET_UNAVAILABLE,
              AUTO_RECERTIFY_UNAVAILABLE,
              API_CLIENT_LIMIT_REACHED,
              EXTERNAL_ID_CONFLICT,
              CANNOT_OFFBOARD_ADMIN,
              SPEND_LIMIT_EXCEEDED,
              PAYMENT_METHOD_NOT_AVAILABLE,
              RETURN_URL_NOT_ALLOWED,
              CURRENCY_NOT_AVAILABLE,
              PAYMENT_METHOD_REQUIRED,
              PAYMENT_METHOD_CHANGED,
              PRICE_CHANGED,
              SUPERSEDED,
              SETUP_NOT_SUCCEEDED,
              INVOICE_BILLING_NOT_ENABLED,
              CANNOT_LAUNCH_ADMIN,
              LEARNER_REMOVED,
              LEARNER_NOT_ACCEPTED,
              PARTNER_ORG_LIMIT_REACHED,
              BILLING_CONTACT_REQUIRED,
              CANNOT_RENAME_ADMIN,
              LEARNER_IN_MULTIPLE_ORGS,
              ACCOUNT_EXISTS_USE_SIGN_IN,
              NAME_CHANGE_NOT_ALLOWED,
              TEMPORARILY_UNAVAILABLE)
          .collect(Collectors.toUnmodifiableMap(ProblemCode::value, Function.identity()));

  private final String value;

  private ProblemCode(String value) {
    this.value = value;
  }

  /** The value for a wire value, known or not. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ProblemCode of(String value) {
    Objects.requireNonNull(value, "value");
    ProblemCode known = KNOWN.get(value);
    return known != null ? known : new ProblemCode(value);
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
    return other instanceof ProblemCode that && that.value.equals(value);
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
