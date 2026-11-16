// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.BinaryStreamResponse;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.Pagination;
import com.teachmehipaa.model.Certificate;
import com.teachmehipaa.model.CertificateLinks;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.CertificateExportParams;
import com.teachmehipaa.param.CertificateListParams;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Certificates in the client's Organization. Reach it through {@code client.certificates()}. */
public final class CertificateService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public CertificateService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** Download a certificate. */
  public BinaryResponse downloadPdf(String certId) {
    return downloadPdf(certId, RequestOptions.none());
  }

  /**
   * Download a certificate.
   *
   * <p>Downloads the certificate as a PDF. Revoked and expired certificates are watermarked.
   * Requires the <code>certificates:read</code> scope.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return the file's bytes, content type and filename
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public BinaryResponse downloadPdf(String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}/content")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.requestBinary(request, "application/pdf", options);
  }

  /** Download a certificate. */
  public CompletableFuture<BinaryResponse> downloadPdfAsync(String certId) {
    return downloadPdfAsync(certId, RequestOptions.none());
  }

  /**
   * Download a certificate.
   *
   * <p>The asynchronous form of {@link #downloadPdf(String, RequestOptions)}: it sends the same
   * request, with the same retries, without blocking the calling thread. The future fails with the
   * {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the file's bytes, content type and filename
   */
  public CompletableFuture<BinaryResponse> downloadPdfAsync(String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}/content")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.requestBinaryAsync(request, "application/pdf", options);
  }

  /** Download a certificate. */
  public BinaryStreamResponse downloadPdfStream(String certId) {
    return downloadPdfStream(certId, RequestOptions.none());
  }

  /**
   * Download a certificate.
   *
   * <p>Downloads the certificate as a PDF. Revoked and expired certificates are watermarked.
   * Requires the <code>certificates:read</code> scope.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return the file as a stream, with its content type and filename; close it to release the
   *     connection
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public BinaryStreamResponse downloadPdfStream(String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}/content")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.requestBinaryStream(request, "application/pdf", options);
  }

  /** Download a certificate. */
  public CompletableFuture<BinaryStreamResponse> downloadPdfStreamAsync(String certId) {
    return downloadPdfStreamAsync(certId, RequestOptions.none());
  }

  /**
   * Download a certificate.
   *
   * <p>The asynchronous form of {@link #downloadPdfStream(String, RequestOptions)}: it sends the
   * same request, with the same retries, without blocking the calling thread. The future fails with
   * the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the file as a stream, with its content type and filename; close it to
   *     release the connection
   */
  public CompletableFuture<BinaryStreamResponse> downloadPdfStreamAsync(
      String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}/content")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.requestBinaryStreamAsync(request, "application/pdf", options);
  }

  /** Export certificates. */
  public Operation export(CertificateExportParams params) {
    return export(params, RequestOptions.none());
  }

  /**
   * Export certificates.
   *
   * <p>Exports the organization's certificate records as CSV. Requires the <code>certificates:read
   * </code> scope.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Operation}, which may finish in the background: {@code waitForCompletion()}
   *     polls until it is done
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Operation export(CertificateExportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/certificates:export")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.request(request, Operation.class, options);
  }

  /** Export certificates. */
  public CompletableFuture<Operation> exportAsync(CertificateExportParams params) {
    return exportAsync(params, RequestOptions.none());
  }

  /**
   * Export certificates.
   *
   * <p>The asynchronous form of {@link #export(CertificateExportParams, RequestOptions)}: it sends
   * the same request, with the same retries, without blocking the calling thread. The future fails
   * with the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Operation}, which may finish in the background: {@code
   *     waitForCompletion()} polls until it is done
   */
  public CompletableFuture<Operation> exportAsync(
      CertificateExportParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("POST", "/organizations/{orgId}/certificates:export")
            .retrySafe()
            .idempotencyKeyRequired()
            .jsonBody(params)
            .build();
    return requester.requestAsync(request, Operation.class, options);
  }

  /** List all certificates. */
  public Page<Certificate> list() {
    return list(CertificateListParams.builder().build(), RequestOptions.none());
  }

  /** List all certificates. */
  public Page<Certificate> list(RequestOptions options) {
    return list(CertificateListParams.builder().build(), options);
  }

  /** List all certificates. */
  public Page<Certificate> list(CertificateListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all certificates.
   *
   * <p>Returns a list of the organization's certificates. Pass <code>subjectId</code> to list one
   * learner's.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link Certificate}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<Certificate> list(CertificateListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("subjectId", params.getSubjectId())
            .retrySafe()
            .build();
    return requester.requestPage(request, Pagination.cursor("cursor"), Certificate.class, options);
  }

  /** List all certificates. */
  public CompletableFuture<Page<Certificate>> listAsync() {
    return listAsync(CertificateListParams.builder().build(), RequestOptions.none());
  }

  /** List all certificates. */
  public CompletableFuture<Page<Certificate>> listAsync(RequestOptions options) {
    return listAsync(CertificateListParams.builder().build(), options);
  }

  /** List all certificates. */
  public CompletableFuture<Page<Certificate>> listAsync(CertificateListParams params) {
    return listAsync(params, RequestOptions.none());
  }

  /**
   * List all certificates.
   *
   * <p>The asynchronous form of {@link #list(CertificateListParams, RequestOptions)}: it sends the
   * same request, with the same retries, without blocking the calling thread. The future fails with
   * the {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of one page of {@link Certificate}; {@code nextCursor()} is {@code null} on
   *     the last
   */
  public CompletableFuture<Page<Certificate>> listAsync(
      CertificateListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("subjectId", params.getSubjectId())
            .retrySafe()
            .build();
    return requester.requestPageAsync(
        request, Pagination.cursor("cursor"), Certificate.class, options);
  }

  /** Retrieve a certificate. */
  public Certificate retrieve(String certId) {
    return retrieve(certId, RequestOptions.none());
  }

  /**
   * Retrieve a certificate.
   *
   * <p>Retrieves a certificate.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link Certificate}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Certificate retrieve(String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.request(request, Certificate.class, options);
  }

  /** Retrieve a certificate. */
  public CompletableFuture<Certificate> retrieveAsync(String certId) {
    return retrieveAsync(certId, RequestOptions.none());
  }

  /**
   * Retrieve a certificate.
   *
   * <p>The asynchronous form of {@link #retrieve(String, RequestOptions)}: it sends the same
   * request, with the same retries, without blocking the calling thread. The future fails with the
   * {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link Certificate}
   */
  public CompletableFuture<Certificate> retrieveAsync(String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.requestAsync(request, Certificate.class, options);
  }

  /** Retrieve certificate links. */
  public CertificateLinks retrieveLinks(String certId) {
    return retrieveLinks(certId, RequestOptions.none());
  }

  /**
   * Retrieve certificate links.
   *
   * <p>Retrieves the certificate's LinkedIn add-to-profile link and its public <code>tme.so</code>
   * verification link. Requires the <code>certificates:read</code> scope.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return the {@link CertificateLinks}
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public CertificateLinks retrieveLinks(String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}/credentials")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.request(request, CertificateLinks.class, options);
  }

  /** Retrieve certificate links. */
  public CompletableFuture<CertificateLinks> retrieveLinksAsync(String certId) {
    return retrieveLinksAsync(certId, RequestOptions.none());
  }

  /**
   * Retrieve certificate links.
   *
   * <p>The asynchronous form of {@link #retrieveLinks(String, RequestOptions)}: it sends the same
   * request, with the same retries, without blocking the calling thread. The future fails with the
   * {@code TeachMeHipaaException} subclass that method throws, never wrapped in a {@code
   * CompletionException}. Cancelling it stops the call and its retries.
   *
   * @param certId
   * @param options settings for this call only, such as a timeout or request id
   * @return a future of the {@link CertificateLinks}
   */
  public CompletableFuture<CertificateLinks> retrieveLinksAsync(
      String certId, RequestOptions options) {
    Objects.requireNonNull(certId, "certId");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/certificates/{certId}/credentials")
            .pathParam("certId", certId)
            .retrySafe()
            .build();
    return requester.requestAsync(request, CertificateLinks.class, options);
  }
}
