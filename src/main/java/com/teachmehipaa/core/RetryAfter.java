package com.teachmehipaa.core;

import java.net.http.HttpHeaders;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

final class RetryAfter {
  private static final Pattern DELAY_SECONDS = Pattern.compile("^\\s*\\d+\\s*$");

  /**
   * {@code Retry-After} as a wait from {@code now}. RFC 9110 §10.2.3 allows delay-seconds or an
   * HTTP-date; anything else counts as absent, so this returns {@code null}.
   */
  static Duration from(HttpHeaders headers, Instant now) {
    String value = headers.firstValue("retry-after").orElse(null);
    if (value == null) {
      return null;
    }
    if (DELAY_SECONDS.matcher(value).matches()) {
      try {
        return Duration.ofSeconds(Long.parseLong(value.strip()));
      } catch (NumberFormatException tooLarge) {
        return Duration.ofSeconds(Long.MAX_VALUE);
      }
    }
    try {
      Instant date =
          ZonedDateTime.parse(value.strip(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
      long millis = Math.max(0, Duration.between(now, date).toMillis());
      return Duration.ofSeconds((millis + 999) / 1000);
    } catch (DateTimeParseException notADate) {
      return null;
    }
  }

  private RetryAfter() {}
}
