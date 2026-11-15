package com.teachmehipaa.model;

import java.util.Objects;

/**
 * The file an export Operation produced ({@code enrollments().export}, {@code
 * certificates().export} or {@code learners().export}), from {@link Operation#getExportResult()}.
 *
 * @param csv the whole file as text: RFC 4180, CRLF line endings
 * @param filename a suggested file name, such as {@code enrollments-org_acme.csv}, or {@code null}
 * @param rowCount the number of data rows, not counting the header, or {@code null}
 */
public record ExportResult(String csv, String filename, Long rowCount) {
  public ExportResult {
    Objects.requireNonNull(csv, "csv");
  }

  /** Leaves the CSV out: it holds Learners' names and emails (ADR-0007). */
  @Override
  public String toString() {
    return "ExportResult[filename="
        + filename
        + ", rowCount="
        + rowCount
        + ", csvLength="
        + csv.length()
        + "]";
  }
}
