// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Certificate;
import com.teachmehipaa.model.CertificateLinks;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.CertificateExportParams;
import com.teachmehipaa.param.CertificateListParams;
import java.util.Objects;

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
            .header("accept", "application/pdf")
            .retrySafe()
            .build();
    return requester.requestBinary(request, options);
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
   * @return the {@link Operation}, which finishes in the background
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
    return requester.requestPage(request, Certificate.class, options);
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
}
