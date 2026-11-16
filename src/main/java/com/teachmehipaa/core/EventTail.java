package com.teachmehipaa.core;

import com.teachmehipaa.model.Event;
import java.time.Duration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * A position in the Event log that you iterate. Returned by {@code client.events().tail(…)}.
 *
 * <p>{@link #lastEventId()} is the id of the last Event handed out, or the {@code after} the tail
 * started from. Save it to resume from the same place after a restart. Iterating the tail again
 * carries on from it. A tail is for one thread at a time; another thread may read {@link
 * #lastEventId()}.
 *
 * <pre>{@code
 * EventTail tail = client.events().tail(
 *     EventTailParams.builder().after(savedId).follow(true).build());
 * for (Event event : tail) {
 *   handle(event);
 * }
 * save(tail.lastEventId());
 * }</pre>
 */
public final class EventTail implements Iterable<Event> {
  private final Function<String, Page<Event>> eventsAfter;
  private final boolean follow;
  private final Duration interval;
  private final Sleeper sleeper;
  private volatile String lastEventId;

  /**
   * Internal: {@code client.events().tail(…)} creates tails.
   *
   * @param eventsAfter lists the Events after an id, or from the oldest retained for {@code null}
   */
  public EventTail(
      Function<String, Page<Event>> eventsAfter,
      String after,
      boolean follow,
      Duration interval,
      Sleeper sleeper) {
    this.eventsAfter = Objects.requireNonNull(eventsAfter, "eventsAfter");
    this.lastEventId = after;
    this.follow = follow;
    this.interval = Objects.requireNonNull(interval, "interval");
    this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
  }

  /**
   * The id of the last Event handed out, or the {@code after} the tail started from ({@code null}
   * when it started at the oldest retained Event and has handed out none).
   */
  public String lastEventId() {
    return lastEventId;
  }

  /**
   * Each Event after {@link #lastEventId()}, oldest first, fetched as the loop reaches it. Ends
   * once caught up; a following tail instead waits the interval and polls again, until the loop
   * breaks or the thread is interrupted. An interrupt ends the iteration quietly with the interrupt
   * flag still set, so {@link #lastEventId()} can be saved.
   *
   * @throws com.teachmehipaa.exception.TeachMeHipaaException from {@code hasNext()} when a poll
   *     fails
   */
  @Override
  public Iterator<Event> iterator() {
    return new Iterator<>() {
      private Iterator<Event> events;
      private boolean ready;
      private boolean ended;

      @Override
      public boolean hasNext() {
        while (!ready && !ended) {
          // Checked before each Event and each poll, so an interrupt never starts a request.
          if (Thread.currentThread().isInterrupted()) {
            ended = true;
          } else if (events == null) {
            // The log's nextCursor is null at the current end, so each poll starts from the
            // last Event handed out, not from a cursor.
            events = eventsAfter.apply(lastEventId).autoPagingIterable().iterator();
          } else if (events.hasNext()) {
            ready = true;
          } else if (follow) {
            events = null;
            pause();
          } else {
            ended = true;
          }
        }
        return ready;
      }

      @Override
      public Event next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        ready = false;
        Event event = events.next();
        lastEventId = event.getEventId();
        return event;
      }

      private void pause() {
        try {
          sleeper.sleep(interval);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          ended = true;
        }
      }
    };
  }
}
