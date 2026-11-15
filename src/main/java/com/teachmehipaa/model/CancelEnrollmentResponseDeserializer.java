// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.teachmehipaa.core.UnionDeserializer;
import java.util.List;
import java.util.Map;

final class CancelEnrollmentResponseDeserializer
    extends UnionDeserializer<CancelEnrollmentResponse> {
  private static final long serialVersionUID = 1L;

  CancelEnrollmentResponseDeserializer() {
    super(
        CancelEnrollmentResponse.class,
        List.of(Map.entry("refund", EnrollmentCancelRefunded.class)),
        Enrollment.class);
  }
}
