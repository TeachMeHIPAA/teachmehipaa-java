package com.teachmehipaa.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Course;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.TokenIntrospection;
import com.teachmehipaa.model.TokenIntrospectionMode;
import com.teachmehipaa.param.CourseListParams;
import com.teachmehipaa.param.LearnerListParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * A read-only smoke test against the real Sandbox, run by hand with {@code ./gradlew sandboxTest}.
 * Without credentials every test is skipped, and CI never runs it.
 */
@EnabledIfEnvironmentVariable(named = "TMH_SANDBOX_CLIENT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TMH_SANDBOX_CLIENT_SECRET", matches = ".+")
class SandboxTest {
  private TeachMeHipaaClient client;

  @BeforeEach
  void buildClient() {
    client =
        TeachMeHipaaClient.builder()
            .clientId(System.getenv("TMH_SANDBOX_CLIENT_ID"))
            .clientSecret(System.getenv("TMH_SANDBOX_CLIENT_SECRET"))
            .environment(Environment.SANDBOX)
            .build();
  }

  @Test
  void introspectsASandboxAccessTokenForAnOrganization() {
    TokenIntrospection introspection = client.auth().introspect();

    // The spec: "`test` for a Sandbox client, `live` otherwise."
    assertEquals(TokenIntrospectionMode.TEST, introspection.getMode());
    assertNotNull(introspection.getOrgId());
  }

  @Test
  void listsCourses() {
    Page<Course> page = client.courses().list(CourseListParams.builder().limit(5).build());

    assertNotNull(page.items());
  }

  @Test
  void listsLearners() {
    Page<Learner> page = client.learners().list(LearnerListParams.builder().limit(5).build());

    assertEquals(200, page.getLastResponse().statusCode());
  }
}
