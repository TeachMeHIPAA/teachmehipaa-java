// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.teachmehipaa.core.LastResponse;
import java.util.Map;

/** Told apart by which field is present; with none of them it reads as {@link Enrollment}. */
@JsonDeserialize(using = CancelEnrollmentResponseDeserializer.class)
public sealed interface CancelEnrollmentResponse permits Enrollment, EnrollmentCancelRefunded {
  /** Fields this version of the library has no getter for, keyed by JSON name. */
  Map<String, Object> getExtraFields();

  /** The response this object was read from. */
  LastResponse getLastResponse();
}
