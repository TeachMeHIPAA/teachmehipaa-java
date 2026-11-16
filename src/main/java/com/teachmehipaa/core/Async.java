package com.teachmehipaa.core;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The plumbing behind the {@code …Async} methods. Internal: shared with the model package so
 * Operation polling cancels and fails the same way requests do.
 *
 * <p>Two rules hold for every future it hands out. A failure completes it with the exception
 * itself, never a {@link CompletionException} around it, so {@code exceptionally} and {@code
 * handle} see a {@code TeachMeHipaaException} subclass. Cancelling it, or completing it from
 * outside as {@code orTimeout} does, cancels the work it waits on, unless that work is shared with
 * other callers.
 */
public final class Async {
  // Custom transports and token providers may implement only the blocking methods; their calls run
  // here so the caller of an …Async method never waits on them. Daemon threads: the library must
  // never keep a JVM alive.
  private static final ExecutorService BLOCKING =
      Executors.newCachedThreadPool(
          work -> {
            Thread thread = new Thread(work, "teachmehipaa-blocking-io");
            thread.setDaemon(true);
            return thread;
          });

  /** The exception inside any {@link CompletionException} or {@link ExecutionException}. */
  public static Throwable unwrap(Throwable failure) {
    Throwable cause = failure;
    while ((cause instanceof CompletionException || cause instanceof ExecutionException)
        && cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }

  /**
   * Runs blocking {@code work} on a library-owned daemon thread. Completing the future from
   * outside, by cancelling it or otherwise, interrupts the thread.
   */
  static <T> CompletableFuture<T> blocking(Callable<T> work) {
    CompletableFuture<T> result = new CompletableFuture<>();
    // Set before the work completes the future, so the callbacks that run then don't interrupt
    // the thread they run on.
    AtomicBoolean finished = new AtomicBoolean();
    Future<?> task =
        BLOCKING.submit(
            () -> {
              T value;
              try {
                value = work.call();
              } catch (Throwable failure) {
                finished.set(true);
                result.completeExceptionally(failure);
                return;
              }
              finished.set(true);
              result.complete(value);
            });
    result.whenComplete(
        (value, failure) -> {
          if (!finished.get()) {
            task.cancel(true);
          }
        });
    return result;
  }

  /** Calls {@code start}, turning anything it throws into a failed future. */
  static <T> CompletableFuture<T> start(Callable<CompletableFuture<T>> start) {
    try {
      CompletableFuture<T> started = start.call();
      return started == null
          ? CompletableFuture.failedFuture(new NullPointerException("returned no future"))
          : started;
    } catch (Throwable failure) {
      return CompletableFuture.failedFuture(failure);
    }
  }

  /**
   * {@code fn} applied to {@code source}'s value; a throw from {@code fn} fails the result.
   * Completing the result from outside, such as by cancelling it, cancels {@code source}.
   */
  public static <T, U> CompletableFuture<U> map(
      CompletableFuture<T> source, Function<? super T, ? extends U> fn) {
    CompletableFuture<U> target = new CompletableFuture<>();
    source.whenComplete(
        (value, failure) -> {
          if (failure != null) {
            target.completeExceptionally(unwrap(failure));
            return;
          }
          try {
            target.complete(fn.apply(value));
          } catch (Throwable thrown) {
            target.completeExceptionally(thrown);
          }
        });
    cancelWith(target, source);
    return target;
  }

  /**
   * The future {@code fn} starts from {@code source}'s value. Completing the result from outside
   * cancels whichever of the two is running.
   */
  static <T, U> CompletableFuture<U> compose(
      CompletableFuture<T> source, Function<? super T, CompletableFuture<U>> fn) {
    Loop<U> loop = new Loop<>();
    loop.await(source, value -> loop.await(start(() -> fn.apply(value)), loop::complete));
    return loop.result();
  }

  /**
   * A view of a future other callers share: it completes as {@code shared} does, and cancelling it
   * leaves {@code shared} running for them.
   */
  static <T> CompletableFuture<T> detached(CompletableFuture<T> shared) {
    CompletableFuture<T> view = new CompletableFuture<>();
    shared.whenComplete(
        (value, failure) -> {
          if (failure != null) {
            view.completeExceptionally(unwrap(failure));
          } else {
            view.complete(value);
          }
        });
    return view;
  }

  // Once target is complete nothing needs upstream; it is already done unless target was
  // completed from outside.
  static void cancelWith(CompletableFuture<?> target, Future<?> upstream) {
    target.whenComplete((value, failure) -> upstream.cancel(true));
  }

  /**
   * A chain of asynchronous steps, such as a request and its retries, behind one future. Each step
   * waits on one stage at a time. Once {@link #result()} is complete no step runs, and if it was
   * completed from outside, by a cancel or {@code orTimeout}, the stage in flight is cancelled.
   *
   * @param <R> what the chain produces
   */
  public static final class Loop<R> {
    private final CompletableFuture<R> result = new CompletableFuture<>();
    private volatile Future<?> inFlight;

    public Loop() {
      result.whenComplete(
          (value, failure) -> {
            Future<?> current = inFlight;
            if (current != null) {
              current.cancel(true);
            }
          });
    }

    /** The future the caller holds. */
    public CompletableFuture<R> result() {
      return result;
    }

    public void complete(R value) {
      result.complete(value);
    }

    /** Fails the chain with {@code failure}, unwrapped. */
    public void fail(Throwable failure) {
      result.completeExceptionally(unwrap(failure));
    }

    /** {@link #await(CompletableFuture, Consumer, Consumer)}, failing the chain on a failure. */
    public <T> void await(CompletableFuture<T> stage, Consumer<? super T> onSuccess) {
      await(stage, onSuccess, this::fail);
    }

    /**
     * Waits for {@code stage}, then runs {@code onSuccess} with its value or {@code onFailure} with
     * its unwrapped exception. Neither runs once the chain is complete, and a throw from either
     * fails the chain. Pass only a stage this chain owns: cancelling the chain cancels it.
     */
    public <T> void await(
        CompletableFuture<T> stage,
        Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onFailure) {
      inFlight = stage;
      // A completion that landed before inFlight was set never saw this stage.
      if (result.isDone()) {
        stage.cancel(true);
        return;
      }
      stage.whenComplete(
          (value, failure) -> {
            if (result.isDone()) {
              return;
            }
            try {
              if (failure == null) {
                onSuccess.accept(value);
              } else {
                onFailure.accept(unwrap(failure));
              }
            } catch (Throwable thrown) {
              fail(thrown);
            }
          });
    }
  }

  private Async() {}
}
