package com.teachmehipaa;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Keeps the docs honest: the harness behind {@link ReadmeTest} and {@link CodeSamplesTest}. Every
 * {@code java} block in a subclass's Markdown files must follow a {@code <!-- sample: <id> -->}
 * line. Each sample is compiled with {@code javax.tools} against the library and the frameworks it
 * names, and nothing else, then compiled again and run against {@link FakeTransport}, with what it
 * printed and sent checked by the subclass.
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
          // Library jars may name annotations that aren't on the samples' path.
          "-Xlint:all,-serial,-classfile,-processing",
          "-Werror");

  static final String ORG = "/v1/organizations/org_acme";

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
