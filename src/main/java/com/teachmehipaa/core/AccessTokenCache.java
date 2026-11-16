package com.teachmehipaa.core;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * One Access token, fetched again 60 seconds before it expires, with concurrent callers sharing a
 * single fetch (grilling Q34). Blocking callers share one through a lock; async callers share one
 * in-flight future, which blocking callers join too. The token providers that mint or exchange
 * their own tokens keep one each.
 */
final class AccessTokenCache {
  private static final Duration REMINT_MARGIN = Duration.ofSeconds(60);

  private final ReentrantLock lock = new ReentrantLock();
  private final Clock clock;
  private final Supplier<CachedToken> fetch;
  private final Supplier<CompletableFuture<CachedToken>> fetchAsync;
  private final AtomicReference<CachedToken> current = new AtomicReference<>();
  private final AtomicReference<CompletableFuture<CachedToken>> asyncFetch =
      new AtomicReference<>();

  /**
   * @param fetch gets a new token, blocking; it only ever runs while this cache's lock is held
   * @param fetchAsync starts getting a new token; at most one runs at a time
   */
  AccessTokenCache(
      Clock clock,
      Supplier<CachedToken> fetch,
      Supplier<CompletableFuture<CachedToken>> fetchAsync) {
    this.clock = Objects.requireNonNull(clock, "clock");
    this.fetch = Objects.requireNonNull(fetch, "fetch");
    this.fetchAsync = Objects.requireNonNull(fetchAsync, "fetchAsync");
  }

  String accessToken() {
    CachedToken token = current.get();
    if (isFresh(token)) {
      return token.value();
    }
    CompletableFuture<CachedToken> inFlight = asyncFetch.get();
    if (inFlight != null) {
      return join(inFlight).value();
    }
    lock.lock();
    try {
      // Whoever held the lock before us may already have fetched.
      token = current.get();
      if (!isFresh(token)) {
        token = fetch.get();
        current.set(token);
      }
      return token.value();
    } finally {
      lock.unlock();
    }
  }

  /**
   * The cached token, or a share of the one fetch in flight. Cancelling the returned future leaves
   * the fetch running for the others; a failed fetch is not cached, so the next call starts anew.
   */
  CompletableFuture<String> accessTokenAsync() {
    CachedToken token = current.get();
    if (isFresh(token)) {
      return CompletableFuture.completedFuture(token.value());
    }
    CompletableFuture<CachedToken> claim = new CompletableFuture<>();
    CompletableFuture<CachedToken> shared = asyncFetch.compareAndExchange(null, claim);
    if (shared == null) {
      shared = claim;
      // A fetch may have finished between the freshness check and the claim.
      CachedToken fetched = current.get();
      if (isFresh(fetched)) {
        asyncFetch.set(null);
        claim.complete(fetched);
      } else {
        Async.start(fetchAsync::get)
            .whenComplete(
                (minted, failure) -> {
                  if (failure == null) {
                    current.set(minted);
                  }
                  // Cleared before the waiters run, so one that retries after a failure starts a
                  // fresh fetch instead of rejoining this one.
                  asyncFetch.set(null);
                  if (failure == null) {
                    claim.complete(minted);
                  } else {
                    claim.completeExceptionally(Async.unwrap(failure));
                  }
                });
      }
    }
    return Async.map(Async.detached(shared), CachedToken::value);
  }

  /** Drops {@code accessToken} if it is still the cached one, so the next call fetches anew. */
  void invalidate(String accessToken) {
    CachedToken token = current.get();
    if (token != null && token.value().equals(accessToken)) {
      current.compareAndSet(token, null);
    }
  }

  private boolean isFresh(CachedToken token) {
    return token != null && clock.instant().isBefore(token.expiresAt().minus(REMINT_MARGIN));
  }

  private static CachedToken join(CompletableFuture<CachedToken> inFlight) {
    try {
      return inFlight.join();
    } catch (CompletionException failed) {
      Throwable cause = Async.unwrap(failed);
      if (cause instanceof RuntimeException runtime) {
        throw runtime;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw failed;
    }
  }

  /**
   * @param value the Access token
   * @param expiresAt when it expires, counted from when its request left
   */
  record CachedToken(String value, Instant expiresAt) {}
}
