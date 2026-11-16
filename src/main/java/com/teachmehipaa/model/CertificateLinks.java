// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;

public final class CertificateLinks extends ApiResource {
  private final String certificateId;
  private final String linkedInUrl;
  private final String shortLink;
  private final String tinyId;
  private final Boolean paywalled;

  @JsonCreator
  CertificateLinks(
      @JsonProperty("certificateId") String certificateId,
      @JsonProperty("linkedInUrl") String linkedInUrl,
      @JsonProperty("shortLink") String shortLink,
      @JsonProperty("tinyId") String tinyId,
      @JsonProperty("paywalled") Boolean paywalled) {
    this.certificateId = certificateId;
    this.linkedInUrl = linkedInUrl;
    this.shortLink = shortLink;
    this.tinyId = tinyId;
    this.paywalled = paywalled;
  }

  /** The certificate. */
  public String getCertificateId() {
    return certificateId;
  }

  /** Adds the certificate to the holder's LinkedIn profile. */
  public String getLinkedInUrl() {
    return linkedInUrl;
  }

  /** The public <code>tme.so</code> verification link. */
  public String getShortLink() {
    return shortLink;
  }

  /** The verification link's id. */
  public String getTinyId() {
    return tinyId;
  }

  /** May be {@code null}. */
  public Boolean getPaywalled() {
    return paywalled;
  }
}
