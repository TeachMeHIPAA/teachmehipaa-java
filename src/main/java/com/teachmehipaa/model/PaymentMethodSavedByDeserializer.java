// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.teachmehipaa.core.UnionDeserializer;
import java.util.Map;

final class PaymentMethodSavedByDeserializer extends UnionDeserializer<PaymentMethodSavedBy> {
  private static final long serialVersionUID = 1L;

  PaymentMethodSavedByDeserializer() {
    super(
        PaymentMethodSavedBy.class,
        "type",
        Map.ofEntries(
            Map.entry("api_client", PaymentMethodSavedByApiClient.class),
            Map.entry("user", PaymentMethodSavedByUser.class)),
        UnknownPaymentMethodSavedBy.class);
  }
}
