// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachmehipaa.core.ApiResource;
import java.time.OffsetDateTime;

public final class LearnerLaunchLink extends ApiResource {
  private final String url;
  private final OffsetDateTime expiresAt;

  @JsonCreator
  LearnerLaunchLink(
      @JsonProperty("url") String url, @JsonProperty("expiresAt") OffsetDateTime expiresAt) {
    this.url = url;
    this.expiresAt = expiresAt;
  }

  /** The single-use sign-in URL. Redirect the learner to it right away. */
  public String getUrl() {
    return url;
  }

  /** When the URL stops working, 5 minutes after it was created. */
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }
}
