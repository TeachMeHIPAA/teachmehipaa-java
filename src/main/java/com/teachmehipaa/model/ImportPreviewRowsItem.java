// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

public final class ImportPreviewRowsItem extends ApiModel {
  private final long rowNumber;
  private final String email;
  private final String name;
  private final String courseId;
  private final ImportPreviewRowsItemStatus status;
  private final String detail;

  @JsonCreator
  ImportPreviewRowsItem(
      @JsonProperty("rowNumber") long rowNumber,
      @JsonProperty("email") String email,
      @JsonProperty("name") String name,
      @JsonProperty("courseId") String courseId,
      @JsonProperty("status") ImportPreviewRowsItemStatus status,
      @JsonProperty("detail") String detail) {
    this.rowNumber = rowNumber;
    this.email = email;
    this.name = name;
    this.courseId = courseId;
    this.status = status;
    this.detail = detail;
  }

  public long getRowNumber() {
    return rowNumber;
  }

  public String getEmail() {
    return email;
  }

  /** May be {@code null}. */
  public String getName() {
    return name;
  }

  public String getCourseId() {
    return courseId;
  }

  public ImportPreviewRowsItemStatus getStatus() {
    return status;
  }

  /** May be {@code null}. */
  public String getDetail() {
    return detail;
  }
}
