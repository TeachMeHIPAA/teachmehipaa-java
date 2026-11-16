// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.model.CertificateVerification;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Certificate verifications. Reach it through {@code client.certificateVerifications()}. */
public final class CertificateVerificationService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public CertificateVerificationService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Verify a certificate. */
  public CertificateVerification retrieve(String tinyId) {
    return retrieve(tinyId, RequestOptions.none());
  }

  /**
   * Verify a certificate.
   *
   * <p>Verifies the certificate behind a <code>https://tme.so/{tinyId}</code> link. Needs no access
   * token. Limited to 60 requests a minute per IP address.
   *
   * @param tinyId The id at the end of the certificate's tme.so link.
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link CertificateVerification}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public CertificateVerification retrieve(String tinyId, RequestOptions options) {
    Objects.requireNonNull(tinyId, "tinyId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/certificate-verifications/{tinyId}")
            .pathParam("tinyId", tinyId)
            .unauthenticated()
            .retrySafe()
            .build();
    return requester.request(request, CertificateVerification.class, options);
  }

  /** Verify a certificate. */
  public CompletableFuture<CertificateVerification> retrieveAsync(String tinyId) {
    return retrieveAsync(tinyId, RequestOptions.none());
  }

  /**
   * Verify a certificate.
   *
   * <p>The asynchronous form of {@link #retrieve(String, RequestOptions)}: it sends the same
   * request, with the same retries, without blocking the calling thread. The future fails with the
   * {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param tinyId The id at the end of the certificate's tme.so link.
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link CertificateVerification}
   */
  public CompletableFuture<CertificateVerification> retrieveAsync(
      String tinyId, RequestOptions options) {
    Objects.requireNonNull(tinyId, "tinyId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/certificate-verifications/{tinyId}")
            .pathParam("tinyId", tinyId)
            .unauthenticated()
            .retrySafe()
            .build();
    return requester.requestAsync(request, CertificateVerification.class, options);
  }
}
