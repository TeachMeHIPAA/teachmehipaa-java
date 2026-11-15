package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.APP_ID;
import static com.teachmehipaa.Fixtures.APP_SECRET;
import static com.teachmehipaa.Fixtures.CLIENT_ID;
import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.SANDBOX_BASE;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.PartnerGrantTokenProvider;
import com.teachmehipaa.core.StaticTokenProvider;
import com.teachmehipaa.core.TokenStore;
import com.teachmehipaa.exception.AuthenticationException;
import com.teachmehipaa.model.CertificateVerification;
import com.teachmehipaa.model.Course;
import java.util.List;
import org.junit.jupiter.api.Test;

class TokenProviderOptionTest {
  private static final Reply UNAUTHORIZED =
      Reply.problem(
          401,
          """
          {"title": "Unauthorized", "status": 401, "code": "unauthorized"}
          """);

  private static final TokenStore UNUSED_STORE =
      new TokenStore() {
        @Override
        public String load() {
          throw new AssertionError("no exchange expected");
        }

        @Override
        public void save(String refreshToken) {
          throw new AssertionError("no exchange expected");
        }
      };

  private static TeachMeHipaaClient.Builder staticClient(FakeTransport api, String accessToken) {
    return TeachMeHipaaClient.builder()
        .tokenProvider(new StaticTokenProvider(accessToken))
        .organizationId(ORG_ID)
        .httpTransport(api)
        .sleeper(new FakeSleeper());
  }

  private static PartnerGrantTokenProvider.Builder grant() {
    return PartnerGrantTokenProvider.builder()
        .clientId(APP_ID)
        .clientSecret(APP_SECRET)
        .tokenStore(UNUSED_STORE);
  }

  @Test
  void aStaticTokenIsSentAsIsAndTheTokenEndpointIsNeverCalled() {
    FakeTransport api = new FakeTransport().on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));
    TeachMeHipaaClient client = staticClient(api, "access-from-elsewhere").build();

    client.learners().retrieve(LEARNER_ID);
    client.learners().retrieve(LEARNER_ID);

    assertEquals(List.of(), api.requestsTo(TOKEN_ROUTE));
    assertEquals(
        List.of("Bearer access-from-elsewhere", "Bearer access-from-elsewhere"),
        api.requestsTo(LEARNER_ROUTE).stream()
            .map(request -> request.headers().get("authorization"))
            .toList());
  }

  @Test
  void aStaticTokenRefusedWith401IsSentOnce() {
    FakeTransport api = new FakeTransport().on(LEARNER_ROUTE, UNAUTHORIZED);
    TeachMeHipaaClient client = staticClient(api, "access-expired").build();

    assertThrows(AuthenticationException.class, () -> client.learners().retrieve(LEARNER_ID));

    assertEquals(1, api.requests().size());
  }

  @Test
  void aStaticTokenMustNotBeBlank() {
    assertThrows(IllegalArgumentException.class, () -> new StaticTokenProvider(" "));
  }

  @Test
  void aTokenProviderCannotBeCombinedWithCredentialsOrScopes() {
    StaticTokenProvider tokens = new StaticTokenProvider("access-token-static");

    for (TeachMeHipaaClient.Builder conflicting :
        List.of(
            TeachMeHipaaClient.builder().clientId(CLIENT_ID).clientSecret(CLIENT_SECRET),
            TeachMeHipaaClient.builder().clientId(CLIENT_ID),
            TeachMeHipaaClient.builder().clientSecret(CLIENT_SECRET),
            TeachMeHipaaClient.builder().scopes("learners:read"))) {
      IllegalStateException error =
          assertThrows(IllegalStateException.class, conflicting.tokenProvider(tokens)::build);
      assertTrue(
          error.getMessage().contains("either tokenProvider or clientId"), error.getMessage());
    }
  }

  @Test
  void aPartnerGrantForAnotherEnvironmentIsRefused() {
    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () ->
                TeachMeHipaaClient.builder()
                    .tokenProvider(grant().environment(Environment.SANDBOX).build())
                    .build());

    assertTrue(error.getMessage().contains(SANDBOX_BASE), error.getMessage());
    assertTrue(error.getMessage().contains("https://api.tme.so/v1"), error.getMessage());
  }

  @Test
  void aPartnerGrantForTheSameBaseUrlIsAccepted() {
    assertDoesNotThrow(
        () ->
            TeachMeHipaaClient.builder()
                .tokenProvider(grant().environment(Environment.SANDBOX).build())
                .environment(Environment.SANDBOX)
                .build());
    assertDoesNotThrow(
        () ->
            TeachMeHipaaClient.builder()
                .tokenProvider(grant().baseUrl("http://localhost:4010/v1/").build())
                .baseUrl("http://localhost:4010/v1")
                .build());
  }

  @Test
  void aClientWithoutCredentialsCallsPublicEndpoints() {
    FakeTransport api =
        new FakeTransport()
            .on(
                "GET /v1/certificate-verifications/Xy7Kq2",
                Reply.json(
                    """
                    {"status": "valid", "learnerName": "Ana Lima", "courseTitle": "HIPAA Essentials",
                     "issuedAt": "2026-11-03T15:45:00Z", "expiresAt": "2027-11-03T15:45:00Z",
                     "issuer": "TeachMeHIPAA"}
                    """))
            .on(
                "GET /v1/courses",
                Reply.json(
                    """
                    {"items": [{"courseId": "hipaa-essentials", "title": "HIPAA Essentials",
                                "courseVersion": "2026.1", "passingThreshold": 0.8}],
                     "nextCursor": null}
                    """));
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder().environment(Environment.SANDBOX).httpTransport(api).build();

    CertificateVerification verification = client.certificateVerifications().retrieve("Xy7Kq2");
    Page<Course> courses = client.courses().list();

    assertEquals("Ana Lima", verification.getLearnerName());
    assertEquals("hipaa-essentials", courses.items().get(0).getCourseId());
    assertTrue(api.requests().stream().noneMatch(r -> r.headers().containsKey("authorization")));
  }

  @Test
  void aClientWithoutCredentialsRefusesAuthenticatedCallsBeforeSendingAnything() {
    FakeTransport api = new FakeTransport();
    TeachMeHipaaClient discovering = TeachMeHipaaClient.builder().httpTransport(api).build();
    TeachMeHipaaClient bound =
        TeachMeHipaaClient.builder().organizationId(ORG_ID).httpTransport(api).build();

    for (TeachMeHipaaClient client : List.of(discovering, bound)) {
      IllegalStateException error =
          assertThrows(IllegalStateException.class, () -> client.learners().retrieve(LEARNER_ID));
      assertTrue(error.getMessage().contains("no credentials"), error.getMessage());
    }
    assertEquals(List.of(), api.requests());
  }
}
