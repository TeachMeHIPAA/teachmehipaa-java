package com.teachmehipaa.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Renders {@code docs/reference/code-samples.json}: one complete Java snippet per IR operation id,
 * which the monorepo's API reference shows when a reader picks Java. Each snippet builds a client
 * as the README's quickstart does, makes the call and uses the result once.
 *
 * <p>The calls are written here by hand, one per operation, so each reads as a person would write
 * it. The generator checks them against the IR: every operation id has exactly one, and each calls
 * the method the IR names for it. {@code CodeSamplesTest} compiles each against the library and
 * runs it against a fake of the API.
 */
final class CodeSampleWriter {
  static final String PATH = "docs/reference/code-samples.json";

  private static final String SANDBOX_CLIENT =
      """
      TeachMeHipaaClient client =
          TeachMeHipaaClient.builder()
              .clientId(System.getenv("TMH_CLIENT_ID"))
              .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
              .environment(Environment.SANDBOX)
              .build();
      """;

  // Public endpoints take no credentials, as the authentication guide's public lookups show.
  private static final String PUBLIC_CLIENT =
      """
      TeachMeHipaaClient client = TeachMeHipaaClient.builder().environment(Environment.SANDBOX).build();
      """;

  private static final String CLIENT_IMPORTS =
      "import com.teachmehipaa.Environment;\nimport com.teachmehipaa.TeachMeHipaaClient;\n";

  /**
   * One operation's sample.
   *
   * @param imports the classes it uses besides the client's, fully qualified
   * @param call the call and the line that uses its result
   */
  record Snippet(List<String> imports, String call) {}

  private static Snippet snippet(String call, String... imports) {
    return new Snippet(List.of(imports), call);
  }

  private static final String MODEL = "com.teachmehipaa.model.";
  private static final String PARAM = "com.teachmehipaa.param.";

  private static final Map<String, Snippet> SNIPPETS = new LinkedHashMap<>();

  static {
    SNIPPETS.put(
        "listAuditEvents",
        snippet(
            """
            for (AuditEvent event : client.auditEvents().list().autoPagingIterable()) {
              System.out.println(event.getType());
            }
            """,
            MODEL + "AuditEvent"));
    SNIPPETS.put(
        "introspectAccessToken",
        snippet(
            """
            TokenIntrospection token = client.auth().introspect();
            System.out.println(token.getOrgId() + " " + token.getScopes());
            """,
            MODEL + "TokenIntrospection"));
    SNIPPETS.put(
        "getCertificateVerification",
        snippet(
            """
            CertificateVerification verification =
                client.certificateVerifications().retrieve("a7Kq2Z");
            System.out.println(verification.getStatus() + " " + verification.getLearnerName());
            """,
            MODEL + "CertificateVerification"));
    SNIPPETS.put(
        "downloadCertificatePdf",
        snippet(
            """
            BinaryResponse pdf = client.certificates().downloadPdf("cert_01JD8F3G5H7J9K1M3N5P7Q9R1S");
            Files.write(Path.of("certificate.pdf"), pdf.data());
            """,
            "com.teachmehipaa.core.BinaryResponse",
            "java.nio.file.Files",
            "java.nio.file.Path"));
    SNIPPETS.put(
        "exportCertificates",
        snippet(
            """
            Operation export =
                client
                    .certificates()
                    .export(
                        CertificateExportParams.builder()
                            .format(CertificatesExportRequestFormat.CSV)
                            .build());
            System.out.println(export.waitForCompletion().getExportResult().filename());
            """,
            MODEL + "CertificatesExportRequestFormat",
            MODEL + "Operation",
            PARAM + "CertificateExportParams"));
    SNIPPETS.put(
        "listCertificates",
        snippet(
            """
            for (Certificate certificate : client.certificates().list().autoPagingIterable()) {
              System.out.println(certificate.getCertificateId() + " " + certificate.getExpiresAt());
            }
            """,
            MODEL + "Certificate"));
    SNIPPETS.put(
        "getCertificate",
        snippet(
            """
            Certificate certificate = client.certificates().retrieve("cert_01JD8F3G5H7J9K1M3N5P7Q9R1S");
            System.out.println(certificate.getStatus() + " until " + certificate.getExpiresAt());
            """,
            MODEL + "Certificate"));
    SNIPPETS.put(
        "getCertificateCredentials",
        snippet(
            """
            CertificateLinks links =
                client.certificates().retrieveLinks("cert_01JD8F3G5H7J9K1M3N5P7Q9R1S");
            System.out.println(links.getShortLink());
            """,
            MODEL + "CertificateLinks"));
    SNIPPETS.put(
        "getComplianceSummary",
        snippet(
            """
            ComplianceSummary summary = client.compliance().retrieve();
            System.out.println(summary.getTotals().getCompliant() + " of " + summary.getTotals().getLearners());
            """,
            MODEL + "ComplianceSummary"));
    SNIPPETS.put(
        "listCourses",
        snippet(
            """
            for (Course course : client.courses().list().autoPagingIterable()) {
              System.out.println(course.getCourseId() + " " + course.getTitle());
            }
            """,
            MODEL + "Course"));
    SNIPPETS.put(
        "getCourse",
        snippet(
            """
            Course course = client.courses().retrieve("hipaa-2026");
            System.out.println(course.getTitle() + " " + course.getCourseVersion());
            """,
            MODEL + "Course"));
    SNIPPETS.put(
        "batchEnrollments",
        snippet(
            """
            EnrollmentsBatchResults batch =
                client
                    .enrollments()
                    .batch(
                        EnrollmentBatchParams.builder()
                            .operation(EnrollmentsBatchRequestOperation.REMIND)
                            .select(
                                EnrollmentsBatchRequestSelectEnrollmentIds.builder()
                                    .enrollmentIds(List.of("enr_01JD8K2M4N6P8Q0R2S4T6V8W0X"))
                                    .build())
                            .build());
            batch.getResults().forEach(r -> System.out.println(r.getEnrollmentId() + " " + r.getOutcome()));
            """,
            MODEL + "EnrollmentsBatchRequestOperation",
            MODEL + "EnrollmentsBatchResults",
            PARAM + "EnrollmentBatchParams",
            PARAM + "EnrollmentsBatchRequestSelectEnrollmentIds",
            "java.util.List"));
    SNIPPETS.put(
        "cancelEnrollment",
        snippet(
            """
            CancelEnrollmentResponse cancelled =
                client.enrollments().cancel("enr_01JD8K2M4N6P8Q0R2S4T6V8W0X");
            System.out.println(cancelled instanceof EnrollmentCancelRefunded ? "refunded" : "cancelled");
            """,
            MODEL + "CancelEnrollmentResponse",
            MODEL + "EnrollmentCancelRefunded"));
    SNIPPETS.put(
        "createEnrollment",
        snippet(
            """
            Enrollment enrollment =
                client
                    .enrollments()
                    .create(
                        EnrollmentCreateParams.builder()
                            .email("dana.whitfield@example.com")
                            .courseId("hipaa-2026")
                            .build());
            System.out.println(enrollment.getEnrollmentId() + " " + enrollment.getStatus());
            """,
            MODEL + "Enrollment",
            PARAM + "EnrollmentCreateParams"));
    SNIPPETS.put(
        "exportEnrollments",
        snippet(
            """
            Operation export =
                client
                    .enrollments()
                    .export(
                        EnrollmentExportParams.builder()
                            .format(EnrollmentsExportRequestFormat.CSV)
                            .build());
            System.out.println(export.waitForCompletion().getExportResult().filename());
            """,
            MODEL + "EnrollmentsExportRequestFormat",
            MODEL + "Operation",
            PARAM + "EnrollmentExportParams"));
    SNIPPETS.put(
        "importEnrollments",
        snippet(
            """
            Operation imported =
                client
                    .enrollments()
                    .importEnrollments(
                        EnrollmentImportParams.builder()
                            .file(FileUpload.of(Path.of("roster.csv")))
                            .build());
            System.out.println(imported.waitForCompletion().getResponse());
            """,
            "com.teachmehipaa.FileUpload",
            MODEL + "Operation",
            PARAM + "EnrollmentImportParams",
            "java.nio.file.Path"));
    SNIPPETS.put(
        "listEnrollments",
        snippet(
            """
            for (OrgEnrollmentListItem enrollment : client.enrollments().list().autoPagingIterable()) {
              System.out.println(enrollment.getEnrollmentId() + " " + enrollment.getStatus());
            }
            """,
            MODEL + "OrgEnrollmentListItem"));
    SNIPPETS.put(
        "remindEnrollment",
        snippet(
            """
            EnrollmentActionAccepted accepted =
                client.enrollments().remind("enr_01JD8K2M4N6P8Q0R2S4T6V8W0X");
            System.out.println(accepted.getAction() + " " + accepted.getEnrollmentId());
            """,
            MODEL + "EnrollmentActionAccepted"));
    SNIPPETS.put(
        "renewEnrollment",
        snippet(
            """
            Enrollment renewed = client.enrollments().renew("enr_01JD8K2M4N6P8Q0R2S4T6V8W0X");
            System.out.println(renewed.getStatus() + " cycle " + renewed.getCycle());
            """,
            MODEL + "Enrollment"));
    SNIPPETS.put(
        "resendEnrollmentInvite",
        snippet(
            """
            EnrollmentActionAccepted accepted =
                client.enrollments().resendInvite("enr_01JD8K2M4N6P8Q0R2S4T6V8W0X");
            System.out.println(accepted.getAction() + " " + accepted.getEnrollmentId());
            """,
            MODEL + "EnrollmentActionAccepted"));
    SNIPPETS.put(
        "getEnrollment",
        snippet(
            """
            Enrollment enrollment = client.enrollments().retrieve("enr_01JD8K2M4N6P8Q0R2S4T6V8W0X");
            System.out.println(enrollment.getStatus() + " " + enrollment.getExpiresAt());
            """,
            MODEL + "Enrollment"));
    SNIPPETS.put(
        "listOrgEvents",
        snippet(
            """
            for (Event event : client.events().list().autoPagingIterable()) {
              System.out.println(event.getEventId() + " " + event.getEventType());
            }
            """,
            MODEL + "Event"));
    SNIPPETS.put(
        "batchUpsertLearners",
        snippet(
            """
            Operation upsert =
                client
                    .learners()
                    .batchUpsert(
                        LearnerBatchUpsertParams.builder()
                            .items(
                                List.of(
                                    LearnerBatchUpsertRequestItemsItem.builder()
                                        .email("dana.whitfield@example.com")
                                        .name("Dana Whitfield")
                                        .build()))
                            .build());
            System.out.println(upsert.waitForCompletion().getResponse());
            """,
            MODEL + "Operation",
            PARAM + "LearnerBatchUpsertParams",
            PARAM + "LearnerBatchUpsertRequestItemsItem",
            "java.util.List"));
    SNIPPETS.put(
        "createLearner",
        snippet(
            """
            LearnerWriteResult learner =
                client
                    .learners()
                    .create(
                        LearnerCreateParams.builder()
                            .email("dana.whitfield@example.com")
                            .name("Dana Whitfield")
                            .build());
            System.out.println(learner.getLearnerId() + " created: " + learner.isCreated());
            """,
            MODEL + "LearnerWriteResult",
            PARAM + "LearnerCreateParams"));
    SNIPPETS.put(
        "createLearnerLaunchLink",
        snippet(
            """
            LearnerLaunchLink link = client.learners().createLaunchLink("sub_01JD80AB2C3D4E5F6G7H8J9K0M");
            System.out.println(link.getUrl() + " expires " + link.getExpiresAt());
            """,
            MODEL + "LearnerLaunchLink"));
    SNIPPETS.put(
        "exportLearners",
        snippet(
            """
            Operation export =
                client
                    .learners()
                    .export(
                        LearnerExportParams.builder().format(LearnersExportRequestFormat.CSV).build());
            System.out.println(export.waitForCompletion().getExportResult().csv());
            """,
            MODEL + "LearnersExportRequestFormat",
            MODEL + "Operation",
            PARAM + "LearnerExportParams"));
    SNIPPETS.put(
        "listLearners",
        snippet(
            """
            for (Learner learner : client.learners().list().autoPagingIterable()) {
              System.out.println(learner.getEmail() + " " + learner.getStatus());
            }
            """,
            MODEL + "Learner"));
    SNIPPETS.put(
        "offboardLearner",
        snippet(
            """
            Learner learner = client.learners().offboard("sub_01JD80AB2C3D4E5F6G7H8J9K0M");
            System.out.println(learner.getLearnerId() + " " + learner.getStatus());
            """,
            MODEL + "Learner"));
    SNIPPETS.put(
        "remindLearner",
        snippet(
            """
            LearnerRemindAccepted accepted = client.learners().remind("sub_01JD80AB2C3D4E5F6G7H8J9K0M");
            System.out.println(accepted.getAction() + " " + accepted.getLearnerId());
            """,
            MODEL + "LearnerRemindAccepted"));
    SNIPPETS.put(
        "getLearner",
        snippet(
            """
            Learner learner = client.learners().retrieve("sub_01JD80AB2C3D4E5F6G7H8J9K0M");
            System.out.println(learner.getName() + " " + learner.getStatus());
            """,
            MODEL + "Learner"));
    SNIPPETS.put(
        "updateLearner",
        snippet(
            """
            Learner current = client.learners().retrieve("sub_01JD80AB2C3D4E5F6G7H8J9K0M");
            Learner updated =
                client
                    .learners()
                    .update(
                        current.getLearnerId(),
                        LearnerUpdateParams.builder().ifMatch(current).externalId("hr-1042").build());
            System.out.println(updated.getExternalId());
            """,
            MODEL + "Learner",
            PARAM + "LearnerUpdateParams"));
    SNIPPETS.put(
        "upsertLearnerByExternalId",
        snippet(
            """
            LearnerWriteResult learner =
                client
                    .learners()
                    .upsertByExternalId(
                        "hr-1042",
                        LearnerUpsertByExternalIdParams.builder()
                            .email("dana.whitfield@example.com")
                            .name("Dana Whitfield")
                            .build());
            System.out.println(learner.getLearnerId() + " created: " + learner.isCreated());
            """,
            MODEL + "LearnerWriteResult",
            PARAM + "LearnerUpsertByExternalIdParams"));
    SNIPPETS.put(
        "listMembers",
        snippet(
            """
            for (MemberListItem member : client.members().list().autoPagingIterable()) {
              System.out.println(member.getEmail() + " " + member.getRole());
            }
            """,
            MODEL + "MemberListItem"));
    SNIPPETS.put(
        "getMember",
        snippet(
            """
            Member member = client.members().retrieve("sub_01JD80AB2C3D4E5F6G7H8J9K0M");
            System.out.println(member.getName() + " " + member.getRole());
            """,
            MODEL + "Member"));
    SNIPPETS.put(
        "getOperation",
        snippet(
            """
            Operation operation = client.operations().retrieve("op_01JD8T4V6W8X0Y2Z4A6B8C0D2E");
            System.out.println(operation.getName() + " done: " + operation.isDone());
            """,
            MODEL + "Operation"));
    SNIPPETS.put(
        "getSeatInventory",
        snippet(
            """
            SeatsInventory seats = client.seats().retrieve();
            System.out.println(seats.getAvailable() + " of " + seats.getTotal() + " seats free");
            """,
            MODEL + "SeatsInventory"));
    SNIPPETS.put(
        "createWebhookSubscription",
        snippet(
            """
            WebhookSubscriptionCreated subscription =
                client
                    .webhookSubscriptions()
                    .create(
                        WebhookSubscriptionCreateParams.builder()
                            .endpointUrl("https://example.com/webhooks/teachmehipaa")
                            .eventTypes(List.of(EventType.LEARNER_CERTIFIED))
                            .build());
            System.out.println(subscription.getSubscriptionId() + " " + subscription.getSecret());
            """,
            MODEL + "EventType",
            MODEL + "WebhookSubscriptionCreated",
            PARAM + "WebhookSubscriptionCreateParams",
            "java.util.List"));
    SNIPPETS.put(
        "deleteWebhookSubscription",
        snippet(
            """
            EmptyResponse deleted =
                client.webhookSubscriptions().delete("whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B");
            System.out.println(deleted.getLastResponse().statusCode());
            """,
            "com.teachmehipaa.core.EmptyResponse"));
    SNIPPETS.put(
        "listWebhookSubscriptions",
        snippet(
            """
            for (WebhookSubscription subscription :
                client.webhookSubscriptions().list().autoPagingIterable()) {
              System.out.println(subscription.getSubscriptionId() + " " + subscription.getEndpointUrl());
            }
            """,
            MODEL + "WebhookSubscription"));
    SNIPPETS.put(
        "redriveWebhookSubscription",
        snippet(
            """
            WebhookDeliveryOperation redrive =
                client.webhookSubscriptions().redrive("whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B");
            System.out.println(redrive.waitForCompletion().getDeliveryResult());
            """,
            MODEL + "WebhookDeliveryOperation"));
    SNIPPETS.put(
        "getWebhookSubscription",
        snippet(
            """
            WebhookSubscription subscription =
                client.webhookSubscriptions().retrieve("whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B");
            System.out.println(subscription.getEndpointUrl() + " " + subscription.getEventTypes());
            """,
            MODEL + "WebhookSubscription"));
    SNIPPETS.put(
        "testWebhookSubscription",
        snippet(
            """
            WebhookDeliveryOperation delivery =
                client.webhookSubscriptions().test("whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B");
            System.out.println(delivery.waitForCompletion().getDeliveryResult());
            """,
            MODEL + "WebhookDeliveryOperation"));
    SNIPPETS.put(
        "updateWebhookSubscription",
        snippet(
            """
            WebhookSubscription current =
                client.webhookSubscriptions().retrieve("whs_01JD8Q5R7S9T1V3W5X7Y9Z1A3B");
            WebhookSubscription updated =
                client
                    .webhookSubscriptions()
                    .update(
                        current.getSubscriptionId(),
                        WebhookSubscriptionUpdateParams.builder().ifMatch(current).enabled(false).build());
            System.out.println(updated.isEnabled());
            """,
            MODEL + "WebhookSubscription",
            PARAM + "WebhookSubscriptionUpdateParams"));
  }

  private final Surface surface;

  CodeSampleWriter(Surface surface) {
    this.surface = surface;
  }

  /**
   * The JSON file, keys in IR order.
   *
   * @throws GeneratorException when an operation id has no snippet, a snippet has no operation, or
   *     a snippet doesn't call the method the IR names for its operation
   */
  String json() {
    Map<String, Surface.MethodPlan> firstMethods = new LinkedHashMap<>();
    List<Surface.ResourceNode> nodes = new ArrayList<>();
    Surface.walk(surface.roots.values(), nodes::add);
    for (Surface.ResourceNode node : nodes) {
      for (Surface.MethodPlan plan : node.methods()) {
        firstMethods.putIfAbsent(plan.method().operationId(), plan);
      }
    }
    Set<String> missing = new TreeSet<>(firstMethods.keySet());
    missing.removeAll(SNIPPETS.keySet());
    Set<String> extra = new TreeSet<>(SNIPPETS.keySet());
    extra.removeAll(firstMethods.keySet());
    if (!missing.isEmpty() || !extra.isEmpty()) {
      throw new GeneratorException(
          PATH + ": no snippet for " + missing + "; no operation for " + extra);
    }

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode root = mapper.createObjectNode();
    root.put("language", "java");
    root.put("release", surface.ir.source().release());
    ObjectNode samples = root.putObject("samples");
    for (Map.Entry<String, Surface.MethodPlan> entry : firstMethods.entrySet()) {
      Surface.MethodPlan plan = entry.getValue();
      Snippet snippet = SNIPPETS.get(entry.getKey());
      String call =
          "client."
              + plan.resourcePath().stream()
                  .map(part -> part + "()")
                  .collect(Collectors.joining("."))
              + "."
              + plan.javaName()
              + "(";
      if (!snippet.call().replaceAll("\\s+", "").contains(call)) {
        throw new GeneratorException(
            PATH + ": the " + entry.getKey() + " snippet does not call " + call + "…)");
      }
      samples.put(entry.getKey(), source(snippet, "none".equals(plan.method().security().kind())));
    }
    DefaultPrettyPrinter printer =
        new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter("  ", "\n"));
    try {
      return mapper.writer(printer).writeValueAsString(root) + "\n";
    } catch (JsonProcessingException unwritable) {
      throw new GeneratorException(PATH + ": " + unwritable.getMessage());
    }
  }

  private static String source(Snippet snippet, boolean publicMethod) {
    StringBuilder imports = new StringBuilder(CLIENT_IMPORTS);
    snippet.imports().stream()
        .sorted()
        .forEach(name -> imports.append("import ").append(name).append(";\n"));
    return imports + "\n" + (publicMethod ? PUBLIC_CLIENT : SANDBOX_CLIENT) + "\n" + snippet.call();
  }
}
