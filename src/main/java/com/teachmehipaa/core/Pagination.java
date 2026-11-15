package com.teachmehipaa.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * How a list asks for the page after the one it has, from the IR. Internal: the generated service
 * layer passes one with each list request.
 *
 * @param style how the next position is worked out
 * @param cursorParam the query parameter that carries the position, such as {@code cursor}
 */
public record Pagination(Style style, String cursorParam) {
  /** How the next position is worked out. */
  public enum Style {
    /** Send the page's opaque {@code nextCursor}. */
    CURSOR,
    /** Send the last Event's {@code eventId}: the Event log keys on Event ids. */
    AFTER
  }

  public Pagination {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(cursorParam, "cursorParam");
  }

  public static Pagination cursor(String cursorParam) {
    return new Pagination(Style.CURSOR, cursorParam);
  }

  public static Pagination after(String cursorParam) {
    return new Pagination(Style.AFTER, cursorParam);
  }

  /** The position of the page after the one whose raw {@code items} are given. */
  String positionAfter(JsonNode items, String nextCursor) {
    if (style == Style.CURSOR || items.isEmpty()) {
      return nextCursor;
    }
    // The Event log's nextCursor is the last Event id while more rows exist, so the last Event
    // is the authoritative position.
    JsonNode eventId = items.get(items.size() - 1).path("eventId");
    return eventId.isTextual() ? eventId.textValue() : nextCursor;
  }
}
