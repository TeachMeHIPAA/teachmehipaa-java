package com.teachmehipaa;

/**
 * Receives one entry per HTTP response, token mints included. Logging is off unless you give the
 * client one, for example {@code entry -> log.debug("teachmehipaa {}", entry)}.
 *
 * <p>Entries never carry bodies, headers, query strings or credentials (ADR-0007): bodies hold
 * Learner names and emails. Implementations must be thread-safe.
 */
@FunctionalInterface
public interface RequestLogger {
  void log(RequestLogEntry entry);
}
