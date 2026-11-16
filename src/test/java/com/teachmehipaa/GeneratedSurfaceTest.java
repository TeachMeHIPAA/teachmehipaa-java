package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.CLIENT_ID;
import static com.teachmehipaa.Fixtures.CLIENT_SECRET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.generator.Generator;
import com.teachmehipaa.generator.HandWritten;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class GeneratedSurfaceTest {
  private static final Path ROOT = Path.of("").toAbsolutePath();
  private static final ObjectMapper JSON = new ObjectMapper();

  /**
   * The public method for every Developer API operation, from the Developer API reference (the
   * vendored {@code openapi/openapi.developer.json}) written down independently of the generator,
   * with the renames settled since (grilling Q12: {@code retrieveLinks}; the IR: {@code
   * auth.introspect} and {@code delete}), the Q25 splits of {@code importEnrollments} and {@code
   * batchEnrollments}, and {@code import} spelled {@code importEnrollments} because it is a Java
   * keyword.
   */
  private static final Map<String, List<String>> EXPECTED_METHODS =
      Map.ofEntries(
          Map.entry("listAuditEvents", List.of("auditEvents.list")),
          Map.entry("introspectAccessToken", List.of("auth.introspect")),
          Map.entry("getCertificateVerification", List.of("certificateVerifications.retrieve")),
          Map.entry("downloadCertificatePdf", List.of("certificates.downloadPdf")),
          Map.entry("exportCertificates", List.of("certificates.export")),
          Map.entry("listCertificates", List.of("certificates.list")),
          Map.entry("getCertificate", List.of("certificates.retrieve")),
          Map.entry("getCertificateCredentials", List.of("certificates.retrieveLinks")),
          Map.entry("getComplianceSummary", List.of("compliance.retrieve")),
          Map.entry("listCourses", List.of("courses.list")),
          Map.entry("getCourse", List.of("courses.retrieve")),
          Map.entry("batchEnrollments", List.of("enrollments.batch", "enrollments.batchByFilter")),
          Map.entry("cancelEnrollment", List.of("enrollments.cancel")),
          Map.entry("createEnrollment", List.of("enrollments.create")),
          Map.entry("exportEnrollments", List.of("enrollments.export")),
          Map.entry(
              "importEnrollments",
              List.of("enrollments.importEnrollments", "enrollments.previewImport")),
          Map.entry("listEnrollments", List.of("enrollments.list")),
          Map.entry("remindEnrollment", List.of("enrollments.remind")),
          Map.entry("renewEnrollment", List.of("enrollments.renew")),
          Map.entry("resendEnrollmentInvite", List.of("enrollments.resendInvite")),
          Map.entry("getEnrollment", List.of("enrollments.retrieve")),
          Map.entry("listOrgEvents", List.of("events.list")),
          Map.entry("batchUpsertLearners", List.of("learners.batchUpsert")),
          Map.entry("createLearner", List.of("learners.create")),
          Map.entry("createLearnerLaunchLink", List.of("learners.createLaunchLink")),
          Map.entry("exportLearners", List.of("learners.export")),
          Map.entry("listLearners", List.of("learners.list")),
          Map.entry("offboardLearner", List.of("learners.offboard")),
          Map.entry("remindLearner", List.of("learners.remind")),
          Map.entry("getLearner", List.of("learners.retrieve")),
          Map.entry("updateLearner", List.of("learners.update")),
          Map.entry("upsertLearnerByExternalId", List.of("learners.upsertByExternalId")),
          Map.entry("listMembers", List.of("members.list")),
          Map.entry("getMember", List.of("members.retrieve")),
          Map.entry("getOperation", List.of("operations.retrieve")),
          Map.entry("getSeatInventory", List.of("seats.retrieve")),
          Map.entry("createWebhookSubscription", List.of("webhookSubscriptions.create")),
          Map.entry("deleteWebhookSubscription", List.of("webhookSubscriptions.delete")),
          Map.entry("listWebhookSubscriptions", List.of("webhookSubscriptions.list")),
          Map.entry("redriveWebhookSubscription", List.of("webhookSubscriptions.redrive")),
          Map.entry("getWebhookSubscription", List.of("webhookSubscriptions.retrieve")),
          Map.entry("testWebhookSubscription", List.of("webhookSubscriptions.test")),
          Map.entry("updateWebhookSubscription", List.of("webhookSubscriptions.update")));

  /** The token endpoint is implemented by the core's token providers. */
  private static final Set<String> HAND_WRITTEN_OPERATIONS = Set.of("createAccessToken");

  /**
   * Generated siblings that send the same operation as the method they name, with a return type
   * Java can't fold into one signature: a download read as it arrives instead of buffered, and the
   * IR's optional {@code If-None-Match} reads, which return {@code Optional} so a 304 is empty.
   */
  private static final Map<String, String> GENERATED_SIBLINGS =
      Map.of(
          "certificates.downloadPdfStream", "certificates.downloadPdf",
          "courses.retrieveIfModified", "courses.retrieve",
          "enrollments.retrieveIfModified", "enrollments.retrieve",
          "webhookSubscriptions.retrieveIfModified", "webhookSubscriptions.retrieve");

  /** IR method names that are Java keywords, and the public name used instead. */
  private static final Map<String, String> KEYWORD_RENAMES =
      Map.of("enrollments.import", "enrollments.importEnrollments");

  private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

  private static JsonNode readJson(String path) throws IOException {
    return JSON.readTree(ROOT.resolve(path).toFile());
  }

  private static List<String> specOperationIds() throws IOException {
    List<String> operationIds = new ArrayList<>();
    readJson("openapi/openapi.developer.json")
        .path("paths")
        .forEach(
            item ->
                item.properties()
                    .forEach(
                        operation -> {
                          if (HTTP_METHODS.contains(operation.getKey())
                              && operation.getValue().has("operationId")) {
                            operationIds.add(operation.getValue().path("operationId").asText());
                          }
                        }));
    return operationIds;
  }

  /** Every callable reachable from the client, as {@code resource.method} paths. */
  private static Set<String> publicMethods(Class<?> type, String prefix) {
    Set<String> names = new TreeSet<>();
    // A hand-written service inherits its generated methods from a base class.
    List<Method> methods = new ArrayList<>();
    for (Class<?> declaring = type;
        declaring == type || isService(declaring);
        declaring = declaring.getSuperclass()) {
      methods.addAll(List.of(declaring.getDeclaredMethods()));
    }
    for (Method method : methods) {
      if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      if (isService(method.getReturnType()) && method.getParameterCount() == 0) {
        names.addAll(publicMethods(method.getReturnType(), prefix + method.getName() + "."));
      } else if (type != TeachMeHipaaClient.class) {
        names.add(prefix + method.getName());
      }
    }
    return names;
  }

  private static boolean isService(Class<?> type) {
    return type.getPackageName().equals("com.teachmehipaa.service");
  }

  @Test
  void readsAnIrBuiltFromTheVendoredSpec() throws IOException, NoSuchAlgorithmException {
    byte[] spec = Files.readAllBytes(ROOT.resolve("openapi/openapi.developer.json"));
    JsonNode source = readJson("openapi/sdk-ir.json").path("source");

    assertEquals(
        source.path("sha256").asText(),
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(spec)));
    assertEquals(
        source.path("release").asText(),
        Files.readString(ROOT.resolve("OPENAPI_VERSION"), StandardCharsets.UTF_8).trim());
  }

  @Test
  void coversEveryDeveloperApiOperationExceptTheTokenEndpoint() throws IOException {
    List<String> covered =
        specOperationIds().stream()
            .filter(operationId -> !HAND_WRITTEN_OPERATIONS.contains(operationId))
            .toList();

    assertEquals(43, covered.size());
    assertEquals(new TreeSet<>(EXPECTED_METHODS.keySet()), new TreeSet<>(covered));
  }

  @Test
  void exposesExactlyOnePublicMethodPerIrMethodWithTheExpectedNamePlusSiblingsAndHandWrittenOnes() {
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder().clientId(CLIENT_ID).clientSecret(CLIENT_SECRET).build();
    Set<String> expected =
        EXPECTED_METHODS.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toCollection(TreeSet::new));

    assertEquals(45, expected.size());
    assertTrue(expected.containsAll(GENERATED_SIBLINGS.values()));
    expected.addAll(GENERATED_SIBLINGS.keySet());
    // Issue #2: every generated method, siblings included, has an …Async twin.
    expected.addAll(expected.stream().map(name -> name + "Async").toList());
    expected.addAll(HandWritten.methods());
    assertEquals(expected, publicMethods(client.getClass(), ""));
  }

  @Test
  void everyAsyncOverloadReturnsAFutureOfWhatItsBlockingTwinReturns() {
    List<Method> services = new ArrayList<>();
    for (Method accessor : TeachMeHipaaClient.class.getMethods()) {
      if (isService(accessor.getReturnType())) {
        services.addAll(serviceMethods(accessor.getReturnType()));
      }
    }
    int checked = 0;
    for (Method async : services) {
      if (!async.getName().endsWith("Async")) {
        continue;
      }
      String twinName = async.getName().substring(0, async.getName().length() - "Async".length());
      Method twin;
      try {
        twin = async.getDeclaringClass().getMethod(twinName, async.getParameterTypes());
      } catch (NoSuchMethodException missing) {
        throw new AssertionError(async + " has no blocking twin with the same parameters");
      }
      Type returned = async.getGenericReturnType();
      assertTrue(
          returned instanceof ParameterizedType future
              && future.getRawType() == CompletableFuture.class
              && future.getActualTypeArguments()[0].equals(twin.getGenericReturnType()),
          async + " returns " + returned + ", not a future of " + twin.getGenericReturnType());
      checked++;
    }
    // Every blocking overload has its twin, so the two counts match.
    long blocking =
        services.stream()
            .filter(method -> !method.getName().endsWith("Async"))
            .filter(
                method ->
                    !HandWritten.methods().stream()
                        .anyMatch(h -> h.endsWith("." + method.getName())))
            .count();
    assertEquals(blocking, checked);
  }

  /** The public instance methods of a service and its generated base, accessors left out. */
  private static List<Method> serviceMethods(Class<?> service) {
    List<Method> methods = new ArrayList<>();
    for (Method method : service.getMethods()) {
      if (method.getDeclaringClass() != Object.class
          && !Modifier.isStatic(method.getModifiers())
          && !isService(method.getReturnType())) {
        methods.add(method);
      }
    }
    for (Method accessor : service.getMethods()) {
      if (isService(accessor.getReturnType()) && accessor.getParameterCount() == 0) {
        methods.addAll(serviceMethods(accessor.getReturnType()));
      }
    }
    return methods;
  }

  @Test
  void namesEachIrMethodAsItsOperationsExpectedPublicMethod() throws IOException {
    Set<String> named = new TreeSet<>();
    for (JsonNode resource : readJson("openapi/sdk-ir.json").path("resources")) {
      String path =
          Stream.of(JSON.convertValue(resource.path("path"), String[].class))
              .collect(Collectors.joining("."));
      for (JsonNode method : resource.path("methods")) {
        String irName = path + "." + method.path("name").asText();
        String name = KEYWORD_RENAMES.getOrDefault(irName, irName);
        List<String> expected =
            EXPECTED_METHODS.getOrDefault(method.path("operationId").asText(), List.of());
        assertTrue(expected.contains(name), irName + " is not " + expected);
        named.add(name);
      }
    }

    assertEquals(45, named.size());
  }

  @Test
  void isExactlyWhatTheGeneratorWritesFromTheVendoredIr() throws IOException {
    Map<String, String> fresh = new TreeMap<>(Generator.render(ROOT));

    Map<String, String> committed = new TreeMap<>();
    for (String directory : Generator.OWNED_DIRECTORIES) {
      try (Stream<Path> files = Files.list(ROOT.resolve(directory))) {
        for (Path file : files.sorted().toList()) {
          String path = ROOT.relativize(file).toString();
          if (!HandWritten.files().contains(path)) {
            committed.put(path, Files.readString(file));
          }
        }
      }
    }
    committed.put(Generator.CLIENT_FILE, Files.readString(ROOT.resolve(Generator.CLIENT_FILE)));
    committed.put(
        Generator.REFERENCE_FILE, Files.readString(ROOT.resolve(Generator.REFERENCE_FILE)));
    committed.put(Generator.SAMPLES_FILE, Files.readString(ROOT.resolve(Generator.SAMPLES_FILE)));

    assertEquals(committed.keySet(), fresh.keySet());
    fresh.forEach((path, source) -> assertEquals(committed.get(path), source, path));
  }
}
