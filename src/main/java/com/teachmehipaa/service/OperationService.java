// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.Operation;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Operations in the client's Organization. Reach it through {@code client.operations()}. */
public final class OperationService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public OperationService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Retrieve an operation. */
  public Operation retrieve(String operationId) {
    return retrieve(operationId, RequestOptions.none());
  }

  /**
   * Retrieve an operation.
   *
   * <p>Retrieves a bulk operation and its result. Operations are kept for 24 hours.
   *
   * @param operationId The server-minted opaque operation id.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which may finish in the background: {@code waitForCompletion()}
   *     polls until it is done
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation retrieve(String operationId, RequestOptions options) {
    Objects.requireNonNull(operationId, "operationId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/operations/{operationId}")
            .pathParam("operationId", operationId)
            .retrySafe()
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Retrieve an operation. */
  public CompletableFuture<Operation> retrieveAsync(String operationId) {
    return retrieveAsync(operationId, RequestOptions.none());
  }

  /**
   * Retrieve an operation.
   *
   * <p>The asynchronous form of {@link #retrieve(String, RequestOptions)}: it sends the same
   * request, with the same retries, without blocking the calling thread. The future fails with the
   * {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param operationId The server-minted opaque operation id.
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Operation}, which may finish in the background: {@code
   *     waitForCompletion()} polls until it is done
   */
  public CompletableFuture<Operation> retrieveAsync(String operationId, RequestOptions options) {
    Objects.requireNonNull(operationId, "operationId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/operations/{operationId}")
            .pathParam("operationId", operationId)
            .retrySafe()
            .build();
    return requester.requestAsync(request, Operation.class, options);
  }
}
