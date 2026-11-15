// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.util.List;

public final class ImportPreview extends ApiResource {
  private final long totalRows;
  private final long validRows;
  private final long seatsRequired;
  private final long seatsAvailable;
  private final List<ImportPreviewRowsItem> rows;

  @JsonCreator
  ImportPreview(
      @JsonProperty("totalRows") long totalRows,
      @JsonProperty("validRows") long validRows,
      @JsonProperty("seatsRequired") long seatsRequired,
      @JsonProperty("seatsAvailable") long seatsAvailable,
      @JsonProperty("rows") List<ImportPreviewRowsItem> rows) {
    this.totalRows = totalRows;
    this.validRows = validRows;
    this.seatsRequired = seatsRequired;
    this.seatsAvailable = seatsAvailable;
    this.rows = immutable(rows);
  }

  /** Rows in the file. */
  public long getTotalRows() {
    return totalRows;
  }

  /** Rows that would be invited. */
  public long getValidRows() {
    return validRows;
  }

  /** Seats the import would use. */
  public long getSeatsRequired() {
    return seatsRequired;
  }

  /** Seats available now. */
  public long getSeatsAvailable() {
    return seatsAvailable;
  }

  /**
   * Each row's result: <code>valid</code>, <code>invalid_email</code>, <code>duplicate_in_file
   * </code>, <code>already_enrolled</code> or <code>already_invited</code>.
   */
  public List<ImportPreviewRowsItem> getRows() {
    return rows;
  }
}
