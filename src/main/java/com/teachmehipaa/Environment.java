package com.teachmehipaa;

/**
 * Which TeachMeHIPAA deployment a credential belongs to. Credentials never work across
 * Environments, and secrets carry no prefix the client could infer this from.
 */
public enum Environment {
  LIVE("https://api.tme.so/v1"),
  SANDBOX("https://api-teachmehipaa-dev.web.app/v1");

  private final String baseUrl;

  Environment(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /** The API base URL for this Environment, without a trailing slash. */
  public String baseUrl() {
    return baseUrl;
  }
}
