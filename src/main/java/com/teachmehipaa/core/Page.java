package com.teachmehipaa.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * One page of a list, and the means to fetch the pages after it with the same parameters and
 * request options.
 *
 * <pre>{@code
 * for (Learner learner : client.learners().list(params).autoPagingIterable()) {
 *   // every Learner on every page; pages are fetched as the loop reaches them
 * }
 * }</pre>
 *
 * @param <T> the listed objects
 */
public final class Page<T> {
  private final List<T> items;
  private final String nextCursor;
  private final LastResponse lastResponse;
  private final Supplier<Page<T>> nextPage;

  /** {@code nextPage} is {@code null} on the last page. */
  Page(List<T> items, String nextCursor, LastResponse lastResponse, Supplier<Page<T>> nextPage) {
    this.items = items == null ? List.of() : List.copyOf(items);
    this.nextCursor = nextCursor;
    this.lastResponse = Objects.requireNonNull(lastResponse, "lastResponse");
    this.nextPage = nextCursor == null ? null : Objects.requireNonNull(nextPage, "nextPage");
  }

  /** The objects on this page, in the API's order. */
  public List<T> items() {
    return items;
  }

  /** The cursor that fetches the next page, or {@code null} when this is the last one. */
  public String nextCursor() {
    return nextCursor;
  }

  /** The status, headers and request id of the response this page was read from. */
  public LastResponse getLastResponse() {
    return lastResponse;
  }

  /**
   * Whether the API said more objects follow. A page can be short, or even empty, and still have a
   * next page.
   */
  public boolean hasNextPage() {
    return nextPage != null;
  }

  /**
   * Fetches the next page with the same parameters and request options.
   *
   * @throws NoSuchElementException on the last page; check {@link #hasNextPage()} first
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<T> nextPage() {
    if (nextPage == null) {
      throw new NoSuchElementException("This is the last page; check hasNextPage() first");
    }
    return nextPage.get();
  }

  /**
   * This page, then each page after it. Each iteration starts again from this page and fetches a
   * page only when the loop reaches it.
   */
  public Iterable<Page<T>> pages() {
    return () -> new PageIterator<>(this);
  }

  /**
   * Every object on this page and the pages after it. Pages are fetched only as the loop reaches
   * them, so breaking out of the loop stops fetching.
   */
  public Iterable<T> autoPagingIterable() {
    return () -> new ItemIterator<>(pages().iterator());
  }

  /**
   * Every object on this page and the pages after it, as a lazy sequential stream: a page is
   * fetched only when the stream needs its first object, so {@code limit} and {@code findFirst}
   * stop fetching.
   */
  public Stream<T> autoPagingStream() {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(autoPagingIterable().iterator(), Spliterator.ORDERED),
        false);
  }

  private static final class PageIterator<T> implements Iterator<Page<T>> {
    private Page<T> next;
    private Page<T> current;

    PageIterator(Page<T> first) {
      this.next = first;
    }

    @Override
    public boolean hasNext() {
      return next != null || current.hasNextPage();
    }

    @Override
    public Page<T> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      current = next != null ? next : current.nextPage();
      next = null;
      return current;
    }
  }

  private static final class ItemIterator<T> implements Iterator<T> {
    private final Iterator<Page<T>> pages;
    private Iterator<T> items = Collections.emptyIterator();

    ItemIterator(Iterator<Page<T>> pages) {
      this.pages = pages;
    }

    @Override
    public boolean hasNext() {
      // Empty pages are skipped: only the last page's null nextCursor ends the list.
      while (!items.hasNext() && pages.hasNext()) {
        items = pages.next().items().iterator();
      }
      return items.hasNext();
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return items.next();
    }
  }
}
