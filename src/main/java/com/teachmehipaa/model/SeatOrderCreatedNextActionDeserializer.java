// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.teachmehipaa.core.UnionDeserializer;
import java.util.Map;

final class SeatOrderCreatedNextActionDeserializer
    extends UnionDeserializer<SeatOrderCreatedNextAction> {
  private static final long serialVersionUID = 1L;

  SeatOrderCreatedNextActionDeserializer() {
    super(
        SeatOrderCreatedNextAction.class,
        "type",
        Map.ofEntries(
            Map.entry("invoice", SeatOrderCreatedNextActionInvoice.class),
            Map.entry("redirect", SeatOrderCreatedNextActionRedirect.class)),
        UnknownSeatOrderCreatedNextAction.class);
  }
}
