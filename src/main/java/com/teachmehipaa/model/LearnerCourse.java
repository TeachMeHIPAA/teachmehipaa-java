package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiModel;

/** The Course a Learner record is for. */
public final class LearnerCourse extends ApiModel {
  private final String courseId;
  private final String title;

  @JsonCreator
  LearnerCourse(@JsonProperty("courseId") String courseId, @JsonProperty("title") String title) {
    this.courseId = courseId;
    this.title = title;
  }

  public String getCourseId() {
    return courseId;
  }

  public String getTitle() {
    return title;
  }
}
