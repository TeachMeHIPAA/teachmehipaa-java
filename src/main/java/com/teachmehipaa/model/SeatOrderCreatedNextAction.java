// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

/**
 * Where to send the buyer next: a checkout <code>url</code> to redirect to, or a hosted invoice.
 *
 * <p>Told apart by {@code type}; a value this version of the library doesn't know reads as {@link
 * UnknownSeatOrderCreatedNextAction}, never as an error.
 */
@JsonDeserialize(using = SeatOrderCreatedNextActionDeserializer.class)
public sealed interface SeatOrderCreatedNextAction
    permits SeatOrderCreatedNextActionInvoice,
        SeatOrderCreatedNextActionRedirect,
        UnknownSeatOrderCreatedNextAction {
  /** The {@code type} that selects the variant. */
  String getType();

  /** Fields this version of the library has no getter for, keyed by JSON name. */
  Map<String, Object> getExtraFields();
}
