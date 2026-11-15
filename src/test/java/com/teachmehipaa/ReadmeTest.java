package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.APP_ID;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.REFRESH_TOKEN_1;
import static com.teachmehipaa.Fixtures.REFRESH_TOKEN_2;
import static com.teachmehipaa.Fixtures.REQUEST_ID;
import static com.teachmehipaa.Fixtures.SANDBOX_BASE;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.grantTokenJson;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.tokenJson;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.TransportRequest;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * Keeps the README honest. Every {@code java} block must follow a {@code <!-- sample: <id> -->}
 * line. Each sample is compiled with {@code javax.tools} against the library and the frameworks it
 * names, and nothing else, then compiled again and run against {@link FakeTransport}, with what it
 * printed and sent checked.
 *
 * <p>A sample is statements (wrapped in a method), members (methods and fields, wrapped in a class)
 * or whole classes, such as the webhook controller. One that uses {@code client} without building
 * it gets the Quickstart's client, bound to an Organization so no lookup is scripted. Statement and
 * member samples may name {@code database}, the caller's own storage. To run, the only change is
 * that the library's builders become {@link ReadmeHarness}'s, which wire them to the fake.
 */
class ReadmeTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String README = read(Path.of("README.md"));
  private static final Pattern BLOCK =
      Pattern.compile("^([^\\n]*)\\n```java\\n(.*?)^```$", Pattern.MULTILINE | Pattern.DOTALL);
  private static final Pattern TAG = Pattern.compile("<!-- sample: ([a-z0-9-]+) -->");
  private static final Pattern TOP_LEVEL_CLASS =
      Pattern.compile("^(?:public )?(?:final )?class (\\w+)", Pattern.MULTILINE);
  // A method declared at the top level of a sample: it ends in "{" and assigns nothing.
  private static final Pattern MEMBER_METHOD =
      Pattern.compile(
          "^(?!(?:if|for|while|switch|try|catch|else|do|synchronized)\\b)[A-Za-z][^=\\n]*\\)"
              + "(?:\\s*throws [\\w., ]+)?\\s*\\{$",
          Pattern.MULTILINE);
  private static final Map<Pattern, String> RUN_BUILDERS =
      Map.of(
          Pattern.compile("(?<![\\w.])TeachMeHipaaClient\\.builder\\(\\)"),
          "com.teachmehipaa.ReadmeHarness.clientBuilder()",
          Pattern.compile("(?<![\\w.])PartnerGrantTokenProvider\\.builder\\(\\)"),
          "com.teachmehipaa.ReadmeHarness.grantProviderBuilder()",
          Pattern.compile("(?<![\\w.])OAuth\\.ExchangeCodeParams\\.builder\\(\\)"),
          "com.teachmehipaa.ReadmeHarness.exchangeCodeParamsBuilder()");

  private static final String PRELUDE_IMPORTS =
      "import com.teachmehipaa.Environment;\nimport com.teachmehipaa.TeachMeHipaaClient;\n";
  private static final String PRELUDE =
      """
      TeachMeHipaaClient client =
          TeachMeHipaaClient.builder()
              .clientId(System.getenv("TMH_CLIENT_ID"))
              .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
              .environment(Environment.SANDBOX)
              .organizationId("org_acme")
              .build();
      """;

  // The caller's storage as the samples see it: a type to compile against, then the harness's.
  private static final String CALLER_TO_COMPILE =
      """
      public abstract class ReadmeCaller {
        protected final Database database = null;

        public interface Database {
          void saveConsent(String state, String codeVerifier);

          String consentState();

          String consentCodeVerifier();

          String loadRefreshToken(String orgId);

          void saveRefreshToken(String orgId, String refreshToken);

          String loadEventId();

          void saveEventId(String eventId);
        }
      }
      """;
  private static final String CALLER_TO_RUN =
      """
      public abstract class ReadmeCaller {
        protected final com.teachmehipaa.ReadmeHarness.Storage database =
            com.teachmehipaa.ReadmeHarness.storage();
      }
      """;

  private static final List<String> JAVAC_OPTIONS =
      List.of(
          "--release",
          "17",
          "-proc:none",
          // Servlets are Serializable, and Spring's jars name annotations that aren't on the path.
          "-Xlint:all,-serial,-classfile,-processing",
          "-Werror");

  private static final String ORG = "/v1/organizations/org_acme";
  private static final String WEBHOOK_SECRET = "readme-test-signing-secret";

  @TempDir static Path runClasses;
  private static URLClassLoader samples;

  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private PrintStream realStdout;
  private FakeTransport api;
  private FakeSleeper sleeper;
  private ReadmeHarness.Storage storage;
  @TempDir Path work;

  enum Kind {
    STATEMENTS,
    MEMBERS,
    CLASSES
  }

  record Sample(String id, String imports, String body) {
    Kind kind() {
      if (TOP_LEVEL_CLASS.matcher(body).find()) {
        return Kind.CLASSES;
      }
      return MEMBER_METHOD.matcher(body).find() ? Kind.MEMBERS : Kind.STATEMENTS;
    }

    String className() {
      if (kind() == Kind.CLASSES) {
        Matcher declared = TOP_LEVEL_CLASS.matcher(body);
        declared.find();
        return declared.group(1);
      }
      return "Readme_" + id.replace('-', '_');
    }

    String source() {
      boolean prelude =
          Pattern.compile("\\bclient\\s*\\.").matcher(body).find()
              && !body.contains("TeachMeHipaaClient client =");
      String imports = prelude ? PRELUDE_IMPORTS + this.imports : this.imports;
      String client = prelude ? indent(PRELUDE, 4) : "";
      return switch (kind()) {
        case CLASSES -> imports + body;
        case MEMBERS ->
            imports
                + "public final class "
                + className()
                + " extends ReadmeCaller {\n"
                + client
                + indent(body, 2)
                + "}\n";
        case STATEMENTS ->
            imports
                + "public final class "
                + className()
                + " extends ReadmeCaller {\n  public void run() throws Exception {\n"
                + client
                + indent(body, 4)
                + "  }\n}\n";
      };
    }

    String sourceToRun() {
      String source = source();
      for (Map.Entry<Pattern, String> builder : RUN_BUILDERS.entrySet()) {
        source =
            builder
                .getKey()
                .matcher(source)
                .replaceAll(Matcher.quoteReplacement(builder.getValue()));
      }
      return source;
    }
  }

  private static List<Sample> samples() {
    List<Sample> found = new ArrayList<>();
    Matcher block = BLOCK.matcher(README);
    while (block.find()) {
      Matcher tag = TAG.matcher(block.group(1));
      if (!tag.matches()) {
        throw new AssertionError("README: a java block has no sample tag:\n" + block.group(2));
      }
      String code = block.group(2);
      String imports =
          code.lines()
              .filter(line -> line.startsWith("import "))
              .collect(Collectors.joining("\n", "", "\n"));
      String body =
          code.lines()
              .filter(line -> !line.startsWith("import "))
              .dropWhile(String::isBlank)
              .collect(Collectors.joining("\n", "", "\n"));
      found.add(new Sample(tag.group(1), imports, body));
    }
    return found;
  }

  static Stream<String> sampleIds() {
    return samples().stream().map(Sample::id);
  }

  @BeforeAll
  static void compileSamplesToRun() throws IOException {
    List<JavaFileObject> sources = new ArrayList<>();
    sources.add(new Source("ReadmeCaller", CALLER_TO_RUN));
    for (Sample sample : samples()) {
      sources.add(new Source(sample.className(), sample.sourceToRun()));
    }
    String failures = compile(sources, System.getProperty("java.class.path"), runClasses);
    assertEquals("", failures, "the README samples, as run, don't compile");
    samples =
        new URLClassLoader(
            new URL[] {runClasses.toUri().toURL()}, ReadmeTest.class.getClassLoader());
  }

  @AfterAll
  static void closeSamples() throws IOException {
    if (samples != null) {
      samples.close();
    }
  }

  @BeforeEach
  void pointTheSamplesAtTheFake() {
    FakeClock clock = new FakeClock();
    api = new FakeTransport();
    sleeper = new FakeSleeper(clock);
    storage = new ReadmeHarness.Storage();
    ReadmeHarness.api = api;
    ReadmeHarness.clock = clock;
    ReadmeHarness.sleeper = sleeper;
    ReadmeHarness.storage = storage;
    realStdout = System.out;
    System.setOut(new PrintStream(stdout, true, UTF_8));
  }

  @AfterEach
  void restore() {
    System.setOut(realStdout);
    ReadmeHarness.api = null;
    ReadmeHarness.clock = null;
    ReadmeHarness.sleeper = null;
    ReadmeHarness.storage = null;
  }

  @Test
  void everyJavaBlockIsTaggedOnceAndChecked() {
    List<String> ids = samples().stream().map(Sample::id).toList();

    assertEquals(ids.size(), ids.stream().distinct().count(), "duplicate sample ids: " + ids);
    assertEquals(CHECKS.keySet(), Set.copyOf(ids));
  }

  @Test
  void everySampleCompilesAgainstTheLibraryAlone(@TempDir Path classes) throws IOException {
    String classpath = System.getProperty("readme.classpath");
    assertNotNull(classpath, "run through Gradle, which passes -Dreadme.classpath");
    List<JavaFileObject> sources = new ArrayList<>();
    sources.add(new Source("ReadmeCaller", CALLER_TO_COMPILE));
    for (Sample sample : samples()) {
      sources.add(new Source(sample.className(), sample.source()));
    }

    assertEquals("", compile(sources, classpath, classes));
  }

  @ParameterizedTest
  @MethodSource("sampleIds")
  void sampleRuns(String id) throws Exception {
    CHECKS.get(id).check(this);
  }

  // The checks, one per sample.

  @FunctionalInterface
  interface Check {
    void check(ReadmeTest test) throws Exception;
  }

  private static final Map<String, Check> CHECKS =
      Map.ofEntries(
          Map.entry("quickstart", ReadmeTest::quickstart),
          Map.entry("organization", ReadmeTest::organization),
          Map.entry("public-lookups", ReadmeTest::publicLookups),
          Map.entry("learner-writes", ReadmeTest::learnerWrites),
          Map.entry("pagination", ReadmeTest::pagination),
          Map.entry("errors", ReadmeTest::errors),
          Map.entry("request-options", ReadmeTest::requestOptions),
          Map.entry("conditional-write", ReadmeTest::conditionalWrite),
          Map.entry("not-modified", ReadmeTest::notModified),
          Map.entry("operations", ReadmeTest::operations),
          Map.entry("webhook-delivery-operation", ReadmeTest::webhookDeliveryOperation),
          Map.entry("files", ReadmeTest::files),
          Map.entry("webhook-spring", ReadmeTest::webhookSpring),
          Map.entry("webhook-servlet", ReadmeTest::webhookServlet),
          Map.entry("webhook-test-headers", ReadmeTest::webhookTestHeaders),
          Map.entry("partner-consent", ReadmeTest::partnerConsent),
          Map.entry("partner-grant", ReadmeTest::partnerGrant),
          Map.entry("partner-organizations", ReadmeTest::partnerOrganizations),
          Map.entry("events-tail", ReadmeTest::eventsTail),
          Map.entry("configuration", ReadmeTest::configuration),
          Map.entry("logging", ReadmeTest::logging));

  private void quickstart() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")))
        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON))
        .on("GET " + ORG + "/learners", page(LEARNER_JSON));

    run("quickstart");

    assertEquals(List.of("Ana Lima in_progress", "ana@example.com"), printed());
    assertEquals(
        URI.create(SANDBOX_BASE + "/auth/token"), api.requestsTo(TOKEN_ROUTE).get(0).uri());
    assertEquals(
        Map.of("status", "in_progress", "limit", "20"),
        query(api.requestsTo("GET " + ORG + "/learners").get(0)));
  }

  private void organization() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            "GET " + ORG + "/compliance",
            Reply.json(COMPLIANCE).withHeader("x-request-id", REQUEST_ID));

    run("organization");

    assertEquals(List.of("0.75 " + REQUEST_ID), printed());
    assertEquals(List.of(), api.requestsTo(INTROSPECTION_ROUTE));
  }

  private void publicLookups() throws Exception {
    api.on("GET /v1/certificate-verifications/aB3dE9", Reply.json(VERIFICATION))
        .on("GET /v1/courses", page(COURSE));

    run("public-lookups");

    assertEquals(List.of("valid Ana Lima", "HIPAA Essentials 2026"), printed());
    assertEquals(List.of(), api.requestsTo(TOKEN_ROUTE));
    assertTrue(
        api.requests().stream()
            .noneMatch(request -> request.headers().containsKey("authorization")));
  }

  private void learnerWrites() throws Exception {
    String upsertRoute = "PUT " + ORG + "/learners/by-external-id/hr-123";
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on("POST " + ORG + "/learners", Reply.json(201, LEARNER_JSON))
        .on(upsertRoute, Reply.json(LEARNER_JSON));

    run("learner-writes");

    assertEquals(List.of("invited", LEARNER_ID), printed());
    assertEquals(
        JSON.readTree("{\"email\": \"ana@example.com\", \"name\": \"Ana Example\"}"),
        body(api.requestsTo(upsertRoute).get(0)));
  }

  private void pagination() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            "GET " + ORG + "/learners",
            request ->
                "cursor-2".equals(query(request).get("cursor"))
                    ? page(EXPIRED_LEARNER_JSON)
                    : pageWithNext("cursor-2", LEARNER_JSON))
        .on(
            "GET " + ORG + "/enrollments",
            request ->
                "cursor-e2".equals(query(request).get("cursor"))
                    ? page(enrollment(2), enrollment(3))
                    : pageWithNext("cursor-e2", enrollment(1)));

    run("pagination");

    assertEquals(List.of(LEARNER_ID, EXPIRED_LEARNER_ID, "[bo@example.com]", "1", "2"), printed());
    assertEquals(
        Map.of("limit", "100", "cursor", "cursor-2"),
        query(api.requestsTo("GET " + ORG + "/learners").get(1)));
  }

  private void errors() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            LEARNER_ROUTE,
            Reply.problem(
                    403,
                    """
                    {"title": "Forbidden", "status": 403, "code": "insufficient_scope",
                     "detail": "This call needs the learners:read Scope."}
                    """)
                .withHeader("x-request-id", REQUEST_ID));

    run("errors");

    assertEquals(
        List.of("Missing a Scope: This call needs the learners:read Scope. (request req_7f3c2a)"),
        printed());
  }

  private void requestOptions() throws Exception {
    String route = "POST " + ORG + "/enrollments";
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            route,
            Reply.problem(
                503, "{\"title\": \"Unavailable\", \"status\": 503, \"code\": \"unavailable\"}"))
        .on(route, Reply.json(201, enrollment(1)).withHeader("x-request-id", "req_e1"));

    run("request-options");

    List<TransportRequest> attempts = api.requestsTo(route);
    assertEquals(2, attempts.size());
    for (TransportRequest attempt : attempts) {
      assertEquals("hr-sync-2026-11-15-ana", attempt.headers().get("idempotency-key"));
      assertEquals("hr-sync-2026-11-15-ana", attempt.headers().get("x-request-id"));
      assertEquals(Duration.ofSeconds(10), attempt.timeout());
    }
    assertEquals(1, sleeper.sleeps().size());
    assertEquals(List.of(enrollmentId(1) + " req_e1"), printed());
  }

  private void conditionalWrite() throws Exception {
    String patchRoute = "PATCH " + ORG + "/learners/" + LEARNER_ID;
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON).withHeader("etag", "\"7\""))
        .on(
            patchRoute,
            Reply.problem(
                412,
                "{\"title\": \"Precondition Failed\", \"status\": 412, \"code\": \"precondition_failed\"}"));

    run("conditional-write");

    TransportRequest update = api.requestsTo(patchRoute).get(0);
    assertEquals("\"7\"", update.headers().get("if-match"));
    assertEquals("application/merge-patch+json", update.headers().get("content-type"));
    assertEquals(
        JSON.readTree("{\"name\": \"Ana Q. Example\", \"externalId\": null}"), body(update));
    assertEquals(List.of("Someone else changed this Learner; read it again"), printed());
  }

  private void notModified() throws Exception {
    String route = "GET /v1/courses/hipaa-2026";
    api.on(route, Reply.json(COURSE).withHeader("etag", "\"c1\"")).on(route, Reply.empty(304));

    run("not-modified");

    assertEquals("\"c1\"", api.requestsTo(route).get(1).headers().get("if-none-match"));
    assertEquals(List.of("still current", "HIPAA Essentials 2026"), printed());
  }

  private void operations() throws Exception {
    // base64 of EXPORTED_CSV, computed outside the SDK.
    String artifact =
        """
        {"format": "csv", "contentType": "text/csv; charset=utf-8",
         "filename": "learners-org_acme.csv", "rowCount": 1,
         "dataBase64": "bGVhcm5lcklkLGVtYWlsDQpzdWJfMDEsYW5hQGV4YW1wbGUuY29tDQo="}
        """;
    String exported = "learnerId,email\r\nsub_01,ana@example.com\r\n";
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on("POST " + ORG + "/learners:export", Reply.json(202, operation(true, artifact)));

    run("operations");

    assertEquals(List.of("learners-org_acme.csv 1 " + exported.length()), printed());
    assertEquals(List.of(), sleeper.sleeps());
  }

  private void webhookDeliveryOperation() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            "POST " + ORG + "/webhook-subscriptions/whs_01JD8D:test",
            Reply.json(202, operation(false, null)))
        .on(
            "GET " + ORG + "/operations/" + OPERATION_ID,
            Reply.json(operation(true, "{\"_tag\": \"sent\", \"responseStatus\": 200}")));

    run("webhook-delivery-operation");

    assertEquals(List.of("endpoint answered 200"), printed());
    assertEquals(List.of(Duration.ofSeconds(1)), sleeper.sleeps());
  }

  private void files() throws Exception {
    byte[] csv = "email,name\r\nana@example.com,Ana Example\r\n".getBytes(UTF_8);
    byte[] pdf = "%PDF-1.7 certificate".getBytes(UTF_8);
    Path roster = Files.write(work.resolve("roster.csv"), csv);
    String importRoute = "POST " + ORG + "/enrollments:import";
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            importRoute,
            Reply.json(
                """
                {"totalRows": 1, "validRows": 1, "seatsRequired": 1, "seatsAvailable": 9,
                 "rows": [{"rowNumber": 2, "email": "ana@example.com", "courseId": "hipaa-2026",
                           "status": "valid"}]}
                """))
        .on(importRoute, Reply.json(202, operation(true, null)))
        .on(
            "GET " + ORG + "/certificates/cert_01JD8F/content",
            new Reply(
                200,
                Map.of(
                    "content-type",
                    "application/pdf",
                    "content-disposition",
                    "attachment; filename=\"ana.pdf\""),
                pdf));

    Object sample = construct("files");
    call(sample, "importRoster", roster);
    call(sample, "saveCertificate", work);

    assertEquals(List.of("1 of 1 rows are valid"), printed());
    List<TransportRequest> imports = api.requestsTo(importRoute);
    assertEquals(2, imports.size());
    for (TransportRequest upload : imports) {
      assertTrue(contains(upload.body(), csv), "the CSV went out in the multipart body");
    }
    assertArrayEquals(pdf, Files.readAllBytes(work.resolve("ana.pdf")));
    assertArrayEquals(pdf, Files.readAllBytes(work.resolve("certificate-copy.pdf")));
  }

  private void webhookSpring() throws Exception {
    Object controller = construct("webhook-spring");
    Method receive =
        controller
            .getClass()
            .getMethod("receive", byte[].class, HttpHeaders.class, HttpServletRequest.class);

    webhookOutcomes(
        (target, payload, signed) -> {
          HttpHeaders headers = new HttpHeaders();
          signed.forEach(headers::add);
          ResponseEntity<?> response =
              (ResponseEntity<?>)
                  receive.invoke(controller, payload, headers, request(target, payload, signed));
          return response.getStatusCode().value() + " " + response.getBody();
        });
  }

  private void webhookServlet() throws Exception {
    HttpServlet servlet = (HttpServlet) construct("webhook-servlet");

    webhookOutcomes(
        (target, payload, signed) -> {
          StringWriter written = new StringWriter();
          int[] status = {200};
          HttpServletResponse response =
              proxy(
                  HttpServletResponse.class,
                  (method, args) ->
                      switch (method) {
                        case "setStatus" -> {
                          status[0] = (Integer) args[0];
                          yield null;
                        }
                        case "setContentType" -> null;
                        case "getWriter" -> new PrintWriter(written, true);
                        default -> throw new UnsupportedOperationException(method);
                      });
          servlet.service(request(target, payload, signed), response);
          return status[0] + " " + written;
        });
  }

  private void webhookTestHeaders() throws Exception {
    run("webhook-test-headers");

    assertEquals(List.of("certified " + LEARNER_ID), printed());
  }

  private void partnerConsent() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-token-grant", REFRESH_TOKEN_1)));
    Object sample = construct("partner-consent");

    URI consentUrl = URI.create((String) call(sample, "startConsent"));
    Map<String, String> consent = form(consentUrl.getRawQuery());
    assertThrows(
        SecurityException.class, () -> call(sample, "finishConsent", "code_123", "forged"));
    call(sample, "finishConsent", "code_123", consent.get("state"));

    assertEquals("console.teachmehipaa.com", consentUrl.getHost());
    assertEquals("S256", consent.get("code_challenge_method"));
    assertEquals(APP_ID, consent.get("client_id"));
    List<TransportRequest> exchanges = api.requestsTo(TOKEN_ROUTE);
    assertEquals(1, exchanges.size());
    Map<String, String> exchange = form(new String(exchanges.get(0).body(), UTF_8));
    assertEquals("code_123", exchange.get("code"));
    assertEquals(storage.consentCodeVerifier, exchange.get("code_verifier"));
    assertEquals(Map.of("org_acme", REFRESH_TOKEN_1), storage.refreshTokens);
  }

  private void partnerGrant() throws Exception {
    storage.refreshTokens.put("org_acme", REFRESH_TOKEN_1);
    api.on(TOKEN_ROUTE, Reply.json(grantTokenJson("access-token-grant", REFRESH_TOKEN_2)))
        .on("GET " + ORG + "/learners", page(LEARNER_JSON));

    run("partner-grant");

    List<TransportRequest> exchanges = api.requestsTo(TOKEN_ROUTE);
    assertEquals(1, exchanges.size());
    assertEquals(
        REFRESH_TOKEN_1, form(new String(exchanges.get(0).body(), UTF_8)).get("refresh_token"));
    assertEquals(Map.of("org_acme", REFRESH_TOKEN_2), storage.refreshTokens);
    assertEquals(
        "Bearer access-token-grant",
        api.requestsTo("GET " + ORG + "/learners").get(0).headers().get("authorization"));
    assertEquals(List.of("1"), printed());
  }

  private void partnerOrganizations() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(
            "GET /v1/partners/ptn_01JD8E2C3D4E5F6G7H8J9K0M1N/organizations",
            page(
                """
                {"orgId": "org_clinic", "name": "Clinic", "createdAt": "2026-11-01T09:00:00Z",
                 "grantActive": true}
                """));

    run("partner-organizations");

    assertEquals(List.of("org_clinic true"), printed());
    assertEquals(List.of(), api.requestsTo(INTROSPECTION_ROUTE));
  }

  private void eventsTail() throws Exception {
    storage.eventId = "evt_01";
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on("GET " + ORG + "/events", page(event("evt_02"), event("evt_03")))
        .on(
            "GET " + ORG + "/events",
            request -> {
              // How a service ends the tail: shutdownNow() interrupts the thread running it.
              Thread.currentThread().interrupt();
              return page();
            });

    try {
      run("events-tail");
    } finally {
      assertTrue(Thread.interrupted(), "the tail ended on the interrupt and kept the flag");
    }

    assertEquals(
        List.of("evt_01", "evt_03"),
        api.requestsTo("GET " + ORG + "/events").stream()
            .map(poll -> query(poll).get("after"))
            .toList());
    assertEquals(List.of("evt_02", "evt_03"), storage.savedEventIds);
    assertEquals(List.of("learner.certified evt_02", "learner.certified evt_03"), printed());
  }

  private void configuration() throws Exception {
    run("configuration");

    assertEquals(List.of("SANDBOX https://api-teachmehipaa-dev.web.app/v1 3"), printed());
    assertEquals(List.of(), api.requests());
  }

  private void logging() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on("GET " + ORG + "/learners", page().withHeader("x-request-id", "req_l"));

    run("logging");

    assertEquals(
        List.of(
            "POST /v1/auth/token 200 in N ms (request id null)",
            "GET /v1/organizations/org_acme/learners 200 in N ms (request id req_l)"),
        printed().stream().map(line -> line.replaceAll("\\d+ ms", "N ms")).toList());
  }

  // Running samples.

  /** Builds a sample and runs it if it is statements; returns the instance. */
  private Object run(String id) throws Exception {
    Object sample = construct(id);
    call(sample, "run");
    return sample;
  }

  private Object construct(String id) throws Exception {
    Sample sample = samples().stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    try {
      return samples.loadClass(sample.className()).getDeclaredConstructor().newInstance();
    } catch (InvocationTargetException failed) {
      throw rethrow(failed);
    }
  }

  private static Object call(Object sample, String name, Object... args) throws Exception {
    Method method =
        Arrays.stream(sample.getClass().getDeclaredMethods())
            .filter(m -> m.getName().equals(name) && m.getParameterCount() == args.length)
            .findFirst()
            .orElseThrow(() -> new AssertionError("the sample has no method " + name));
    method.setAccessible(true);
    try {
      return method.invoke(sample, args);
    } catch (InvocationTargetException failed) {
      throw rethrow(failed);
    }
  }

  private static Exception rethrow(InvocationTargetException failed) {
    if (failed.getCause() instanceof Error error) {
      throw error;
    }
    return (Exception) failed.getCause();
  }

  private List<String> printed() {
    return stdout.toString(UTF_8).lines().toList();
  }

  // Webhooks.

  @FunctionalInterface
  interface WebhookEndpoint {
    String post(String target, byte[] payload, Map<String, String> headers) throws Exception;
  }

  /**
   * A Delivery verifies on the exact path it was signed for, with or without a query string, and
   * fails on a changed body or another path.
   */
  private static void webhookOutcomes(WebhookEndpoint endpoint) throws Exception {
    byte[] delivery = event("evt_01JD8M").getBytes(UTF_8);
    byte[] tampered =
        new String(delivery, UTF_8).replace("learner.certified", "learner.removed").getBytes(UTF_8);
    for (String target : List.of("/webhooks/tmh", "/webhooks/tmh?tenant=acme")) {
      assertEquals(
          "200 received learner.certified",
          endpoint.post(
              target, delivery, Webhook.generateTestHeaders(delivery, WEBHOOK_SECRET, target)));
    }
    Map<String, String> signed =
        Webhook.generateTestHeaders(delivery, WEBHOOK_SECRET, "/webhooks/tmh");
    assertEquals("400 body_hash_mismatch", endpoint.post("/webhooks/tmh", tampered, signed));
    assertEquals(
        "400 signature_mismatch", endpoint.post("/webhooks/tmh?tenant=acme", delivery, signed));
  }

  /** The servlet request a container would hand over for a POST to {@code target}. */
  private static HttpServletRequest request(
      String target, byte[] payload, Map<String, String> headers) {
    URI uri = URI.create(target);
    Map<String, String> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    byName.putAll(headers);
    return proxy(
        HttpServletRequest.class,
        (method, args) ->
            switch (method) {
              case "getMethod" -> "POST";
              case "getRequestURI" -> uri.getRawPath();
              case "getQueryString" -> uri.getRawQuery();
              case "getHeader" -> byName.get((String) args[0]);
              case "getInputStream" -> new BodyStream(payload);
              default -> throw new UnsupportedOperationException(method);
            });
  }

  @FunctionalInterface
  interface Handler {
    Object handle(String method, Object[] args) throws Exception;
  }

  private static <T> T proxy(Class<T> type, Handler handler) {
    return type.cast(
        Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] {type},
            (proxy, method, args) -> handler.handle(method.getName(), args)));
  }

  private static final class BodyStream extends ServletInputStream {
    private final ByteArrayInputStream body;

    BodyStream(byte[] payload) {
      this.body = new ByteArrayInputStream(payload);
    }

    @Override
    public int read() {
      return body.read();
    }

    @Override
    public boolean isFinished() {
      return body.available() == 0;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
      throw new UnsupportedOperationException();
    }
  }

  // Compiling samples.

  private static String compile(List<JavaFileObject> sources, String classpath, Path out)
      throws IOException {
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager files = javac.getStandardFileManager(diagnostics, null, UTF_8)) {
      List<String> options = new ArrayList<>(JAVAC_OPTIONS);
      options.addAll(List.of("-classpath", classpath, "-d", out.toString()));
      javac.getTask(null, files, diagnostics, options, null, sources).call();
    }
    return diagnostics.getDiagnostics().stream()
        .filter(
            d ->
                d.getKind() == Diagnostic.Kind.ERROR
                    || d.getKind() == Diagnostic.Kind.WARNING
                    || d.getKind() == Diagnostic.Kind.MANDATORY_WARNING)
        .map(
            d ->
                d.getSource() == null
                    ? d.getMessage(null)
                    : d.getSource().getName() + ":" + d.getLineNumber() + ": " + d.getMessage(null))
        .collect(Collectors.joining("\n"));
  }

  private static final class Source extends SimpleJavaFileObject {
    private final String code;

    Source(String className, String code) {
      super(URI.create("string:///" + className + ".java"), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }

  // API values, from the developer spec's schemas and examples.

  private static final String EXPIRED_LEARNER_ID = "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EJ";
  private static final String EXPIRED_LEARNER_JSON =
      """
      {"learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EJ", "email": "bo@example.com", "name": "Bo",
       "externalId": null, "status": "expired",
       "course": {"courseId": "hipaa-essentials", "title": "HIPAA Essentials"},
       "invitedAt": "2025-11-01T09:00:00Z", "startedAt": "2025-11-02T10:30:00Z",
       "completedAt": "2025-11-03T15:45:00Z", "dueAt": null, "certificate": null}
      """;
  private static final String COMPLIANCE =
      """
      {"asOf": "2026-11-15T12:00:00.000Z", "courseId": "hipaa-2026",
       "totals": {"learners": 4, "compliant": 3, "expiringSoon": 1, "expired": 0,
                  "inProgress": 1, "notStarted": 0, "inviteBounced": 0},
       "compliantRate": 0.75}
      """;
  private static final String COURSE =
      """
      {"courseId": "hipaa-2026", "title": "HIPAA Essentials 2026", "courseVersion": "2026.1",
       "passingThreshold": 0.8}
      """;
  private static final String VERIFICATION =
      """
      {"status": "valid", "learnerName": "Ana Lima", "courseTitle": "HIPAA Essentials 2026",
       "issuedAt": "2026-11-02T10:30:00.000Z", "expiresAt": "2027-11-02T10:30:00.000Z",
       "issuer": "TeachMeHIPAA"}
      """;
  private static final String OPERATION_ID = "0b7d4a8e-5c1f-4e2a-9d3b";

  private static String operation(boolean done, String response) {
    return """
        {"name": "organizations/org_acme/operations/%s", "done": %s, "response": %s}
        """
        .formatted(OPERATION_ID, done, response == null ? "null" : response);
  }

  private static String enrollmentId(int n) {
    return LEARNER_ID + "__hipaa-2026-" + n;
  }

  /** dev#/components/schemas/Enrollment. */
  private static String enrollment(int n) {
    return """
        {"enrollmentId": "%s", "subjectId": "%s", "courseId": "hipaa-2026", "orgId": "org_acme",
         "status": "INVITED", "effectiveStatus": "INVITED", "archived": false, "cycle": 1,
         "rev": 1, "invitedAt": "2026-11-15T09:00:00.000Z", "startedAt": null,
         "completedAt": null, "certifiedAt": null, "expiresAt": null, "certificateId": null}
        """
        .formatted(enrollmentId(n), LEARNER_ID);
  }

  /** A learner.certified Event, as the log and Deliveries carry it. */
  private static String event(String eventId) {
    return """
        {"eventId": "%s", "eventType": "learner.certified", "orgId": "org_acme",
         "occurredAt": "2026-11-15T12:00:00.000Z",
         "data": {"learnerId": "%s", "email": "ana@example.com", "externalId": "hr-123",
                  "courseId": "hipaa-2026", "status": "certified",
                  "occurredAt": "2026-11-15T12:00:00.000Z"}}
        """
        .formatted(eventId, LEARNER_ID);
  }

  private static Reply page(String... items) {
    return Reply.json("{\"items\": [" + String.join(",", items) + "], \"nextCursor\": null}");
  }

  private static Reply pageWithNext(String nextCursor, String... items) {
    return Reply.json(
        "{\"items\": [" + String.join(",", items) + "], \"nextCursor\": \"" + nextCursor + "\"}");
  }

  // Reading what was sent.

  private static JsonNode body(TransportRequest request) throws IOException {
    return JSON.readTree(request.body());
  }

  private static Map<String, String> query(TransportRequest request) {
    return form(request.uri().getRawQuery());
  }

  private static Map<String, String> form(String encoded) {
    Map<String, String> fields = new LinkedHashMap<>();
    if (encoded != null && !encoded.isEmpty()) {
      for (String pair : encoded.split("&")) {
        String[] field = pair.split("=", 2);
        fields.put(
            URLDecoder.decode(field[0], UTF_8),
            field.length > 1 ? URLDecoder.decode(field[1], UTF_8) : "");
      }
    }
    return fields;
  }

  private static boolean contains(byte[] haystack, byte[] needle) {
    for (int start = 0; start + needle.length <= haystack.length; start++) {
      if (Arrays.equals(haystack, start, start + needle.length, needle, 0, needle.length)) {
        return true;
      }
    }
    return false;
  }

  private static String indent(String code, int spaces) {
    String pad = " ".repeat(spaces);
    return code.lines()
        .map(line -> line.isEmpty() ? line : pad + line)
        .collect(Collectors.joining("\n", "", "\n"));
  }

  private static String read(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }
}
