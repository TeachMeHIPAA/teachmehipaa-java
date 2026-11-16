// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class Course extends ApiResource {
  private final String courseId;
  private final String title;
  private final String courseVersion;
  private final double passingThreshold;

  @JsonCreator
  Course(
      @JsonProperty("courseId") String courseId,
      @JsonProperty("title") String title,
      @JsonProperty("courseVersion") String courseVersion,
      @JsonProperty("passingThreshold") double passingThreshold) {
    this.courseId = courseId;
    this.title = title;
    this.courseVersion = courseVersion;
    this.passingThreshold = passingThreshold;
  }

  /** Unique identifier for the course. */
  public String getCourseId() {
    return courseId;
  }

  /** The course's title. */
  public String getTitle() {
    return title;
  }

  /** The current version of the course content. */
  public String getCourseVersion() {
    return courseVersion;
  }

  /** The score needed to pass the course's assessment. */
  public double getPassingThreshold() {
    return passingThreshold;
  }
}
