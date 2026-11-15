// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.Course;
import com.teachmehipaa.param.CourseListParams;
import java.util.Objects;

/** Courses. Reach it through {@code client.courses()}. */
public final class CourseService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public CourseService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** List all courses. */
  public Page<Course> list() {
    return list(CourseListParams.builder().build(), RequestOptions.none());
  }

  /** List all courses. */
  public Page<Course> list(RequestOptions options) {
    return list(CourseListParams.builder().build(), options);
  }

  /** List all courses. */
  public Page<Course> list(CourseListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all courses.
   *
   * <p>Returns a list of the courses in the public catalog. Needs no access token.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link Course}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<Course> list(CourseListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/courses")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .unauthenticated()
            .retrySafe()
            .build();
    return requester.requestPage(request, Pagination.cursor("cursor"), Course.class, options);
  }

  /** Retrieve a course. */
  public Course retrieve(String courseId) {
    return retrieve(courseId, RequestOptions.none());
  }

  /**
   * Retrieve a course.
   *
   * <p>Retrieves a course from the public catalog.
   *
   * @param courseId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Course}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Course retrieve(String courseId, RequestOptions options) {
    Objects.requireNonNull(courseId, "courseId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/courses/{courseId}")
            .pathParam("courseId", courseId)
            .unauthenticated()
            .retrySafe()
            .build();
    return requester.request(request, Course.class, options);
  }
}
