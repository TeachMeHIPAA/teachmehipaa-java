// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.SeatsInventory;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Seats in the client's Organization. Reach it through {@code client.seats()}. */
public final class SeatService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public SeatService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Retrieve the seat inventory. */
  public SeatsInventory retrieve() {
    return retrieve(RequestOptions.none());
  }

  /**
   * Retrieve the seat inventory.
   *
   * <p>Retrieves how many seats the organization has, how many are assigned and how many are
   * available for new invites. Requires the <code>enrollments:read</code> scope.
   *
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link SeatsInventory}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public SeatsInventory retrieve(RequestOptions options) {
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/seats").retrySafe().build();
    return requester.request(request, SeatsInventory.class, options);
  }

  /** Retrieve the seat inventory. */
  public CompletableFuture<SeatsInventory> retrieveAsync() {
    return retrieveAsync(RequestOptions.none());
  }

  /**
   * Retrieve the seat inventory.
   *
   * <p>The asynchronous form of {@link #retrieve(RequestOptions)}: it sends the same request, with
   * the same retries, without blocking the calling thread. The future fails with the {@code
   * TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link SeatsInventory}
   */
  public CompletableFuture<SeatsInventory> retrieveAsync(RequestOptions options) {
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/seats").retrySafe().build();
    return requester.requestAsync(request, SeatsInventory.class, options);
  }
}
