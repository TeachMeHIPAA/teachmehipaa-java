package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.EventTail;
import com.teachmehipaa.param.EventListParams;
import com.teachmehipaa.param.EventTailParams;
import java.util.Objects;

/**
 * Events in the client's Organization. Reach it through {@code client.events()}. Hand-written: the
 * generated methods are in {@link EventServiceBase}.
 */
public final class EventService extends EventServiceBase {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public EventService(ApiRequester requester) {
    super(requester);
    this.requester = requester;
  }

  /** Tail the event log from the oldest retained Event. */
  public EventTail tail() {
    return tail(EventTailParams.builder().build(), RequestOptions.none());
  }

  /** Tail the event log. */
  public EventTail tail(EventTailParams params) {
    return tail(params, RequestOptions.none());
  }

  /**
   * Tail the event log.
   *
   * <p>Iterates the Organization's Events oldest first, after {@code after}. The log's {@code
   * nextCursor} is {@code null} at the current end, so the tail keeps its own position in {@link
   * EventTail#lastEventId()}. Once caught up the iteration ends, or with {@code follow} waits
   * {@code interval} and polls again until the loop breaks or the thread is interrupted.
   *
   * @param params where to start, which Events and how to follow
   * @param options settings for every request the tail sends, such as a timeout
   * @return the tail; nothing is fetched until it is iterated
   */
  public EventTail tail(EventTailParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    return new EventTail(
        position -> list(listParams(params, position), options),
        params.getAfter(),
        params.isFollow(),
        params.getInterval(),
        requester.sleeper());
  }

  private static EventListParams listParams(EventTailParams params, String position) {
    EventListParams.Builder builder = EventListParams.builder();
    if (params.getLimit() != null) {
      builder.limit(params.getLimit());
    }
    if (position != null) {
      builder.after(position);
    }
    if (params.getType() != null) {
      builder.type(params.getType());
    }
    return builder.build();
  }
}
