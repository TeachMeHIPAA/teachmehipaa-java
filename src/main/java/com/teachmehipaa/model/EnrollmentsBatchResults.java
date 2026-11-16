// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.util.List;

public final class EnrollmentsBatchResults extends ApiResource {
  private final EnrollmentsBatchResultsOperation operation;
  private final List<EnrollmentsBatchResultsResultsItem> results;

  @JsonCreator
  EnrollmentsBatchResults(
      @JsonProperty("operation") EnrollmentsBatchResultsOperation operation,
      @JsonProperty("results") List<EnrollmentsBatchResultsResultsItem> results) {
    this.operation = operation;
    this.results = immutable(results);
  }

  /** The operation that was run. */
  public EnrollmentsBatchResultsOperation getOperation() {
    return operation;
  }

  /**
   * One result per enrollment: <code>succeeded</code>, <code>skipped</code> or <code>failed</code>
   * with a problem.
   */
  public List<EnrollmentsBatchResultsResultsItem> getResults() {
    return results;
  }
}
