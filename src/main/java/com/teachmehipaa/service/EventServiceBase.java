// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.Event;
import com.teachmehipaa.param.EventListParams;
import java.util.Objects;

/**
 * The generated methods of {@code client.events()}. The client returns the hand-written {@link
 * EventService}, which adds to them.
 */
public abstract sealed class EventServiceBase permits EventService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  EventServiceBase(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** List all events. */
  public Page<Event> list() {
    return list(EventListParams.builder().build(), RequestOptions.none());
  }

  /** List all events. */
  public Page<Event> list(RequestOptions options) {
    return list(EventListParams.builder().build(), options);
  }

  /** List all events. */
  public Page<Event> list(EventListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all events.
   *
   * <p>Returns a list of the organization's events from the last 30 days, oldest first. Pass the
   * last <code>eventId</code> you processed as <code>after</code> to resume where you stopped, and
   * <code>type</code> to list one event type.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link Event}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<Event> list(EventListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/events")
            .queryParam("limit", params.getLimit())
            .queryParam("after", params.getAfter())
            .queryParam("type", params.getType())
            .retrySafe()
            .build();
    return requester.requestPage(request, Pagination.after("after"), Event.class, options);
  }
}
