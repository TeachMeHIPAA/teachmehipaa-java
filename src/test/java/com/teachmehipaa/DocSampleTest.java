package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.LEARNER_ID;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.TransportRequest;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Keeps the docs honest: the harness behind {@link ReadmeTest} and {@link GuidesTest}. Every {@code
 * java} block in a subclass's Markdown files must follow a {@code <!-- sample: <id> -->} line. Each
 * sample is compiled with {@code javax.tools} against the library and the frameworks it names, and
 * nothing else, then compiled again and run against {@link FakeTransport}, with what it printed and
 * sent checked by the subclass.
 *
 * <p>A sample is statements (wrapped in a method), members (methods and fields, wrapped in a class)
 * or whole classes, such as a webhook controller or a JUnit test. One that uses {@code client}
 * without building it gets the Quickstart's client, bound to an Organization so no lookup is
 * scripted. Statement and member samples may name {@code database}, the caller's own storage. To
 * run, the only change is that the client builder becomes {@link DocHarness}'s, which wires it to
 * the fake.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DocSampleTest {
  static final ObjectMapper JSON = new ObjectMapper();

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
  private static final Pattern BUILDER =
      Pattern.compile("(?<![\\w.])TeachMeHipaaClient\\.builder\\(\\)");

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
      public abstract class DocCaller {
        protected final Database database = null;

        public interface Database {
          String loadEventId();

          void saveEventId(String eventId);

          String loadOperationName();

          void saveOperationName(String name);

          void saveSigningSecret(String secret);

          boolean isProcessed(String eventId);

          void markProcessed(String eventId);
        }
      }
      """;
  private static final String CALLER_TO_RUN =
      """
      public abstract class DocCaller {
        protected final com.teachmehipaa.DocHarness.Storage database =
            com.teachmehipaa.DocHarness.storage();
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

  static final String ORG = "/v1/organizations/org_acme";
  static final String WEBHOOK_SECRET = "readme-test-signing-secret";

  private Path runClasses;
  private URLClassLoader samples;
  private List<Sample> parsed;

  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private PrintStream realStdout;
  FakeTransport api;
  FakeSleeper sleeper;
  FakeAsyncSleeper asyncSleeper;
  DocHarness.Storage storage;

  /** A fresh directory for each check to write files into. */
  Path work;

  /** The Markdown files whose samples this class checks, relative to the repository root. */
  abstract List<Path> docs();

  /** One check per sample id. */
  abstract Map<String, Check> checks();

  @FunctionalInterface
  interface Check {
    void check() throws Exception;
  }

  enum Kind {
    STATEMENTS,
    MEMBERS,
    CLASSES
  }

  record Sample(Path doc, String id, String imports, String body) {
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
      return "Sample_" + id.replace('-', '_');
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
                + " extends DocCaller {\n"
                + client
                + indent(body, 2)
                + "}\n";
        case STATEMENTS ->
            imports
                + "public final class "
                + className()
                + " extends DocCaller {\n  public void run() throws Exception {\n"
                + client
                + indent(body, 4)
                + "  }\n}\n";
      };
    }

    String sourceToRun() {
      return BUILDER
          .matcher(source())
          .replaceAll(Matcher.quoteReplacement("com.teachmehipaa.DocHarness.clientBuilder()"));
    }
  }

  /** Every tagged sample in {@code docs}, in order. */
  static List<Sample> samples(List<Path> docs) {
    List<Sample> found = new ArrayList<>();
    for (Path doc : docs) {
      Matcher block = BLOCK.matcher(read(doc));
      while (block.find()) {
        Matcher tag = TAG.matcher(block.group(1));
        if (!tag.matches()) {
          throw new AssertionError(doc + ": a java block has no sample tag:\n" + block.group(2));
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
        found.add(new Sample(doc, tag.group(1), imports, body));
      }
    }
    return found;
  }

  /** Markdown files in {@code directory}, sorted, relative to the repository root. */
  static List<Path> markdownIn(String directory) {
    try (Stream<Path> files = Files.list(Path.of(directory))) {
      return files
          .filter(file -> file.toString().endsWith(".md"))
          .sorted(Comparator.comparing(Path::toString))
          .toList();
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  List<Sample> samples() {
    if (parsed == null) {
      parsed = samples(docs());
    }
    return parsed;
  }

  Stream<String> sampleIds() {
    return samples().stream().map(Sample::id);
  }

  @BeforeAll
  void compileSamplesToRun() throws IOException {
    runClasses = Files.createTempDirectory("doc-samples");
    List<JavaFileObject> sources = new ArrayList<>();
    sources.add(new Source("DocCaller", CALLER_TO_RUN));
    for (Sample sample : samples()) {
      sources.add(new Source(sample.className(), sample.sourceToRun()));
    }
    String failures = compile(sources, System.getProperty("java.class.path"), runClasses);
    assertEquals("", failures, "the samples, as run, don't compile");
    samples =
        new URLClassLoader(new URL[] {runClasses.toUri().toURL()}, getClass().getClassLoader());
  }

  @AfterAll
  void closeSamples() throws IOException {
    if (samples != null) {
      samples.close();
    }
    deleteRecursively(runClasses);
  }

  @BeforeEach
  void pointTheSamplesAtTheFake() throws IOException {
    FakeClock clock = new FakeClock();
    api = new FakeTransport();
    sleeper = new FakeSleeper(clock);
    asyncSleeper = new FakeAsyncSleeper(clock);
    storage = new DocHarness.Storage();
    work = Files.createTempDirectory(runClasses, "work");
    DocHarness.api = api;
    DocHarness.clock = clock;
    DocHarness.sleeper = sleeper;
    DocHarness.asyncSleeper = asyncSleeper;
    DocHarness.storage = storage;
    stdout.reset();
    realStdout = System.out;
    System.setOut(new PrintStream(stdout, true, UTF_8));
  }

  @AfterEach
  void restore() {
    System.setOut(realStdout);
    DocHarness.api = null;
    DocHarness.clock = null;
    DocHarness.sleeper = null;
    DocHarness.asyncSleeper = null;
    DocHarness.storage = null;
  }

  @Test
  void everyJavaBlockIsTaggedOnceAndChecked() {
    List<String> ids = samples().stream().map(Sample::id).toList();

    assertEquals(ids.size(), ids.stream().distinct().count(), "duplicate sample ids: " + ids);
    assertEquals(checks().keySet(), Set.copyOf(ids));
  }

  @Test
  void everySampleCompilesAgainstTheLibraryAlone() throws IOException {
    String classpath = System.getProperty("readme.classpath");
    assertNotNull(classpath, "run through Gradle, which passes -Dreadme.classpath");
    Path classes = Files.createTempDirectory(runClasses, "compile-only");
    List<JavaFileObject> sources = new ArrayList<>();
    sources.add(new Source("DocCaller", CALLER_TO_COMPILE));
    for (Sample sample : samples()) {
      sources.add(new Source(sample.className(), sample.source()));
    }

    assertEquals("", compile(sources, classpath, classes));
  }

  @ParameterizedTest
  @MethodSource("sampleIds")
  void sampleRuns(String id) throws Exception {
    checks().get(id).check();
  }

  // Running samples.

  /** Builds a sample and runs it if it is statements; returns the instance. */
  Object run(String id) throws Exception {
    Object sample = construct(id);
    call(sample, "run");
    return sample;
  }

  Object construct(String id) throws Exception {
    Sample sample = samples().stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    try {
      Constructor<?> constructor = samples.loadClass(sample.className()).getDeclaredConstructor();
      // Class samples, such as a JUnit test, may be package-private.
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (InvocationTargetException failed) {
      throw rethrow(failed);
    }
  }

  static Object call(Object sample, String name, Object... args) throws Exception {
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

  List<String> printed() {
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
  static void webhookOutcomes(WebhookEndpoint endpoint) throws Exception {
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
  static HttpServletRequest servletRequest(
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

  static <T> T proxy(Class<T> type, Handler handler) {
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

  static final String EXPIRED_LEARNER_ID = "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EJ";
  static final String EXPIRED_LEARNER_JSON =
      """
      {"learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EJ", "email": "bo@example.com", "name": "Bo",
       "externalId": null, "status": "expired",
       "course": {"courseId": "hipaa-essentials", "title": "HIPAA Essentials"},
       "invitedAt": "2025-11-01T09:00:00Z", "startedAt": "2025-11-02T10:30:00Z",
       "completedAt": "2025-11-03T15:45:00Z", "dueAt": null, "certificate": null}
      """;
  static final String COMPLIANCE =
      """
      {"asOf": "2026-11-15T12:00:00.000Z", "courseId": "hipaa-2026",
       "totals": {"learners": 4, "compliant": 3, "expiringSoon": 1, "expired": 0,
                  "inProgress": 1, "notStarted": 0, "inviteBounced": 0},
       "compliantRate": 0.75}
      """;
  static final String COURSE =
      """
      {"courseId": "hipaa-2026", "title": "HIPAA Essentials 2026", "courseVersion": "2026.1",
       "passingThreshold": 0.8}
      """;
  static final String VERIFICATION =
      """
      {"status": "valid", "learnerName": "Ana Lima", "courseTitle": "HIPAA Essentials 2026",
       "issuedAt": "2026-11-02T10:30:00.000Z", "expiresAt": "2027-11-02T10:30:00.000Z",
       "issuer": "TeachMeHIPAA"}
      """;
  static final String OPERATION_ID = "0b7d4a8e-5c1f-4e2a-9d3b";

  static String operation(boolean done, String response) {
    return """
        {"name": "organizations/org_acme/operations/%s", "done": %s, "response": %s}
        """
        .formatted(OPERATION_ID, done, response == null ? "null" : response);
  }

  static String enrollmentId(int n) {
    return LEARNER_ID + "__hipaa-2026-" + n;
  }

  /** dev#/components/schemas/Enrollment. */
  static String enrollment(int n) {
    return """
        {"enrollmentId": "%s", "subjectId": "%s", "courseId": "hipaa-2026", "orgId": "org_acme",
         "status": "INVITED", "effectiveStatus": "INVITED", "archived": false, "cycle": 1,
         "rev": 1, "invitedAt": "2026-11-15T09:00:00.000Z", "startedAt": null,
         "completedAt": null, "certifiedAt": null, "expiresAt": null, "certificateId": null}
        """
        .formatted(enrollmentId(n), LEARNER_ID);
  }

  /** A learner.certified Event, as the log and Deliveries carry it. */
  static String event(String eventId) {
    return """
        {"eventId": "%s", "eventType": "learner.certified", "orgId": "org_acme",
         "occurredAt": "2026-11-15T12:00:00.000Z",
         "data": {"learnerId": "%s", "email": "ana@example.com", "externalId": "hr-123",
                  "courseId": "hipaa-2026", "status": "certified",
                  "occurredAt": "2026-11-15T12:00:00.000Z"}}
        """
        .formatted(eventId, LEARNER_ID);
  }

  static Reply page(String... items) {
    return Reply.json("{\"items\": [" + String.join(",", items) + "], \"nextCursor\": null}");
  }

  static Reply pageWithNext(String nextCursor, String... items) {
    return Reply.json(
        "{\"items\": [" + String.join(",", items) + "], \"nextCursor\": \"" + nextCursor + "\"}");
  }

  // Reading what was sent.

  static JsonNode body(TransportRequest request) throws IOException {
    return JSON.readTree(request.body());
  }

  static Map<String, String> query(TransportRequest request) {
    String encoded = request.uri().getRawQuery();
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

  static boolean contains(byte[] haystack, byte[] needle) {
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

  static String read(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  private static void deleteRecursively(Path root) throws IOException {
    if (root == null || !Files.exists(root)) {
      return;
    }
    try (Stream<Path> paths = Files.walk(root)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.delete(path);
      }
    }
  }
}
