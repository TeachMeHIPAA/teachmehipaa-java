// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.SeatsInventory;
import java.util.Objects;

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
}
