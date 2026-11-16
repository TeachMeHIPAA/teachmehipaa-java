package com.teachmehipaa.core;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * One Access token, fetched again 60 seconds before it expires, with concurrent callers sharing a
 * single fetch (grilling Q34). The token providers that mint or exchange their own tokens keep one
 * each.
 */
final class AccessTokenCache {
  private static final Duration REMINT_MARGIN = Duration.ofSeconds(60);

  private final ReentrantLock lock = new ReentrantLock();
  private final Clock clock;
  private final Supplier<CachedToken> fetch;
  private volatile CachedToken current;

  /**
   * @param fetch gets a new token; it only ever runs while this cache's lock is held, so state it
   *     touches needs no lock of its own
   */
  AccessTokenCache(Clock clock, Supplier<CachedToken> fetch) {
    this.clock = Objects.requireNonNull(clock, "clock");
    this.fetch = Objects.requireNonNull(fetch, "fetch");
  }

  String accessToken() {
    CachedToken token = current;
    if (isFresh(token)) {
      return token.value();
    }
    lock.lock();
    try {
      // Whoever held the lock before us may already have fetched.
      token = current;
      if (!isFresh(token)) {
        token = fetch.get();
        current = token;
      }
      return token.value();
    } finally {
      lock.unlock();
    }
  }

  /** Drops {@code accessToken} if it is still the cached one, so the next call fetches anew. */
  void invalidate(String accessToken) {
    lock.lock();
    try {
      CachedToken token = current;
      if (token != null && token.value().equals(accessToken)) {
        current = null;
      }
    } finally {
      lock.unlock();
    }
  }

  private boolean isFresh(CachedToken token) {
    return token != null && clock.instant().isBefore(token.expiresAt().minus(REMINT_MARGIN));
  }

  /**
   * @param value the Access token
   * @param expiresAt when it expires, counted from when its request left
   */
  record CachedToken(String value, Instant expiresAt) {}
}
