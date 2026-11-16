package com.teachmehipaa;

/** Literal API values from the developer spec's examples and shapes. */
final class Fixtures {
  static final String LIVE_BASE = "https://api.tme.so/v1";
  static final String SANDBOX_BASE = "https://sandbox.api.tme.so/v1";

  static final String CLIENT_ID = "cli_01JD8Z4N3QX5V7W9Y2B4C6D8EF";
  static final String CLIENT_SECRET = "tmh_cs_k3Jq9-Zx_8LmN2pR4tV6wY0aB1cD3eF5gH7iJ9kL0mN";
  // base64("<CLIENT_ID>:<CLIENT_SECRET>"), computed outside the SDK.
  static final String BASIC_CREDENTIALS =
      "Y2xpXzAxSkQ4WjROM1FYNVY3VzlZMkI0QzZEOEVGOnRtaF9jc19rM0pxOS1aeF84TG1OMnBS"
          + "NHRWNndZMGFCMWNEM2VGNWdIN2lKOWtMMG1O";

  static final String ORG_ID = "org_acme";
  static final String LEARNER_ID = "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG";
  static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.first.sig";
  static final String SECOND_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.second.sig";
  static final String REQUEST_ID = "req_7f3c2a";
  static final String ETAG = "\"9b2f0c4e1d\"";

  static final String TOKEN_ROUTE = "POST /v1/auth/token";
  static final String INTROSPECTION_ROUTE = "GET /v1/auth/introspection";
  static final String LEARNER_ROUTE = "GET /v1/organizations/org_acme/learners/" + LEARNER_ID;

  static final String LEARNER_JSON =
      """
      {
        "learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG",
        "email": "ana@example.com",
        "name": "Ana Lima",
        "externalId": "hr-123",
        "status": "in_progress",
        "course": {"courseId": "hipaa-essentials", "title": "HIPAA Essentials"},
        "invitedAt": "2026-11-01T09:00:00Z",
        "startedAt": "2026-11-02T10:30:00Z",
        "completedAt": null,
        "dueAt": "2026-12-01T00:00:00Z",
        "certificate": null
      }
      """;

  static final String CERTIFIED_LEARNER_JSON =
      """
      {
        "learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG",
        "email": "ana@example.com",
        "name": null,
        "externalId": null,
        "status": "compliant",
        "course": {"courseId": "hipaa-essentials", "title": "HIPAA Essentials"},
        "invitedAt": "2026-11-01T09:00:00Z",
        "startedAt": "2026-11-02T10:30:00Z",
        "completedAt": "2026-11-03T15:45:00Z",
        "dueAt": null,
        "certificate": {
          "certificateId": "cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH",
          "issuedAt": "2026-11-03T15:45:00Z",
          "expiresAt": "2027-11-03T15:45:00Z",
          "verifyUrl": "https://teachmehipaa.com/verify/cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH",
          "pdfUrl": "/v1/organizations/org_acme/certificates/cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH/pdf"
        }
      }
      """;

  static String tokenJson(String accessToken) {
    return """
        {"access_token": "%s", "token_type": "Bearer", "expires_in": 900,
         "scope": "learners:read learners:write"}
        """
        .formatted(accessToken);
  }

  static String introspectionJson(String orgIdJson) {
    return """
        {"actorType": "client", "clientId": "cli_01JD8Z4N3QX5V7W9Y2B4C6D8EF",
         "subjectId": null, "orgId": %s, "scopes": ["learners:read", "learners:write"],
         "mode": "test", "expiresAt": "2026-11-15T12:15:00Z"}
        """
        .formatted(orgIdJson);
  }

  /** Scripts the three calls a first Learner retrieve makes. */
  static FakeTransport learnerRetrieveApi(String basePath) {
    return new FakeTransport()
        .on("POST " + basePath + "/auth/token", Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            "GET " + basePath + "/auth/introspection",
            Reply.json(introspectionJson("\"org_acme\"")))
        .on(
            "GET " + basePath + "/organizations/org_acme/learners/" + LEARNER_ID,
            Reply.json(LEARNER_JSON)
                .withHeader("x-request-id", REQUEST_ID)
                .withHeader("etag", ETAG));
  }

  static FakeTransport learnerRetrieveApi() {
    return learnerRetrieveApi("/v1");
  }

  /** A client on the fake API that records retry waits instead of sleeping. */
  static TeachMeHipaaClient.Builder clientFor(FakeTransport api) {
    return TeachMeHipaaClient.builder()
        .clientId(CLIENT_ID)
        .clientSecret(CLIENT_SECRET)
        .httpTransport(api)
        .sleeper(new FakeSleeper());
  }

  private Fixtures() {}
}
