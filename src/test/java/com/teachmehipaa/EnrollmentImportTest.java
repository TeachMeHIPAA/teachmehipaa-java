package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.model.ImportPreview;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.EnrollmentImportParams;
import com.teachmehipaa.param.EnrollmentPreviewImportParams;
import com.teachmehipaa.param.ImportOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Enrollment CSV imports as multipart bodies. */
class EnrollmentImportTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String ROUTE = "POST /v1/organizations/org_acme/enrollments:import";
  private static final Pattern UUID_V4 =
      Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

  // RFC 4180 with CRLF, a quoted comma and non-ASCII UTF-8.
  private static final byte[] CSV =
      "email,name\r\nana@example.com,\"Example, Ana\"\r\njose@example.com,José Núñez\r\n"
          .getBytes(StandardCharsets.UTF_8);

  // dev#/components/schemas/ImportPreview.
  private static final String PREVIEW_JSON =
      """
      {"totalRows": 2, "validRows": 2, "seatsRequired": 2, "seatsAvailable": 10,
       "rows": [
         {"rowNumber": 2, "email": "ana@example.com", "courseId": "hipaa-2026", "status": "valid"},
         {"rowNumber": 3, "email": "jose@example.com", "courseId": "hipaa-2026", "status": "valid"}
       ]}
      """;
  // dev#/components/schemas/Operation, as a commit returns it (202).
  private static final String OPERATION_JSON =
      """
      {"name": "op_01JD8H", "kind": "enrollments.import", "done": false}
      """;

  @TempDir Path directory;

  /** One part of a multipart body, parsed the way a server would. */
  private record Part(Map<String, String> headers, byte[] content) {
    String header(String name) {
      return headers.get(name.toLowerCase(Locale.ROOT));
    }
  }

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  /** The parts by name, split on the boundary the Content-Type names. */
  private static Map<String, Part> partsOf(TransportRequest request) {
    String contentType = request.headers().get("content-type");
    Matcher boundary = Pattern.compile("^multipart/form-data; boundary=(.+)$").matcher(contentType);
    assertTrue(boundary.matches(), contentType);
    // ISO-8859-1 maps each byte to one char, so the body survives the round trip unchanged.
    String body = new String(request.body(), StandardCharsets.ISO_8859_1);
    String delimiter = "--" + boundary.group(1);
    assertTrue(body.startsWith(delimiter + "\r\n"), body);
    assertTrue(body.endsWith("\r\n" + delimiter + "--\r\n"), body);
    String inner = body.substring(delimiter.length() + 2, body.length() - delimiter.length() - 6);
    Map<String, Part> parts = new LinkedHashMap<>();
    for (String raw : inner.split(Pattern.quote("\r\n" + delimiter + "\r\n"))) {
      int split = raw.indexOf("\r\n\r\n");
      Map<String, String> headers = new LinkedHashMap<>();
      for (String line : raw.substring(0, split).split("\r\n")) {
        int colon = line.indexOf(':');
        headers.put(
            line.substring(0, colon).trim().toLowerCase(Locale.ROOT),
            line.substring(colon + 1).trim());
      }
      Matcher name =
          Pattern.compile("name=\"([^\"]*)\"").matcher(headers.get("content-disposition"));
      assertTrue(name.find());
      parts.put(
          name.group(1),
          new Part(headers, raw.substring(split + 4).getBytes(StandardCharsets.ISO_8859_1)));
    }
    return parts;
  }

  private static JsonNode json(byte[] content) throws IOException {
    return JSON.readTree(content);
  }

  @Test
  void aPreviewSendsACsvPartAndAJsonOptionsPart() throws IOException {
    FakeTransport api = api().on(ROUTE, Reply.json(PREVIEW_JSON));

    ImportPreview preview =
        clientOn(api)
            .enrollments()
            .previewImport(
                EnrollmentPreviewImportParams.builder()
                    .file(FileUpload.of(CSV, "roster.csv"))
                    .options(ImportOptions.builder().courseId("hipaa-2026").build())
                    .build());

    assertEquals(2, preview.getTotalRows());
    assertEquals("jose@example.com", preview.getRows().get(1).getEmail());
    TransportRequest request = api.requestsTo(ROUTE).get(0);
    Map<String, Part> parts = partsOf(request);
    assertEquals(List.of("file", "options"), List.copyOf(parts.keySet()));
    assertEquals(
        "form-data; name=\"file\"; filename=\"roster.csv\"",
        parts.get("file").header("Content-Disposition"));
    assertEquals("text/csv", parts.get("file").header("Content-Type"));
    assertArrayEquals(CSV, parts.get("file").content());
    assertEquals("application/json", parts.get("options").header("Content-Type"));
    assertEquals(
        JSON.readTree("{\"courseId\": \"hipaa-2026\", \"validateOnly\": true}"),
        json(parts.get("options").content()));
    assertTrue(UUID_V4.matcher(request.headers().get("idempotency-key")).matches());
  }

  static Stream<Arguments> sources() {
    return Stream.of(
        Arguments.of("a path", (Function<Path, FileUpload>) FileUpload::of, "roster.csv"),
        Arguments.of(
            "bytes",
            (Function<Path, FileUpload>) path -> FileUpload.of(CSV, "hr-export.csv"),
            "hr-export.csv"),
        Arguments.of(
            "a stream",
            (Function<Path, FileUpload>)
                path -> FileUpload.of(new ByteArrayInputStream(CSV), "stream.csv"),
            "stream.csv"));
  }

  @ParameterizedTest(name = "from {0}")
  @MethodSource("sources")
  void anImportSendsTheBytesUnchangedWithAnIdempotencyKey(
      String source, Function<Path, FileUpload> upload, String filename) throws IOException {
    Path roster = Files.write(directory.resolve("roster.csv"), CSV);
    FakeTransport api = api().on(ROUTE, Reply.json(202, OPERATION_JSON));

    Operation operation =
        clientOn(api)
            .enrollments()
            .importEnrollments(EnrollmentImportParams.builder().file(upload.apply(roster)).build());

    assertEquals("op_01JD8H", operation.getName());
    assertFalse(operation.isDone());
    TransportRequest request = api.requestsTo(ROUTE).get(0);
    assertTrue(UUID_V4.matcher(request.headers().get("idempotency-key")).matches());
    Map<String, Part> parts = partsOf(request);
    assertEquals(
        "form-data; name=\"file\"; filename=\"" + filename + "\"",
        parts.get("file").header("Content-Disposition"));
    assertEquals("text/csv", parts.get("file").header("Content-Type"));
    assertArrayEquals(CSV, parts.get("file").content());
    assertEquals(JSON.readTree("{\"validateOnly\": false}"), json(parts.get("options").content()));
  }

  @Test
  void aRetriedImportResendsIdenticalBytesUnderTheSameKey() {
    FakeTransport api =
        api()
            .on(
                ROUTE,
                Reply.problem(
                    503,
                    "{\"title\": \"Unavailable\", \"status\": 503,"
                        + " \"code\": \"temporarily_unavailable\"}"))
            .on(ROUTE, Reply.json(202, OPERATION_JSON));

    // A stream can be read only once, so identical retries show the bytes were read once.
    clientOn(api)
        .enrollments()
        .importEnrollments(
            EnrollmentImportParams.builder()
                .file(FileUpload.of(new ByteArrayInputStream(CSV), "roster.csv"))
                .build());

    List<TransportRequest> attempts = api.requestsTo(ROUTE);
    assertEquals(2, attempts.size());
    assertEquals(
        attempts.get(0).headers().get("idempotency-key"),
        attempts.get(1).headers().get("idempotency-key"));
    assertArrayEquals(CSV, partsOf(attempts.get(0)).get("file").content());
    assertArrayEquals(CSV, partsOf(attempts.get(1)).get("file").content());
  }

  @Test
  void aPathIsReadAfreshByEachCall() throws IOException {
    Path roster = Files.write(directory.resolve("roster.csv"), CSV);
    FakeTransport api = api().on(ROUTE, Reply.json(PREVIEW_JSON));
    EnrollmentPreviewImportParams params =
        EnrollmentPreviewImportParams.builder().file(FileUpload.of(roster)).build();
    TeachMeHipaaClient client = clientOn(api);
    byte[] corrected = "email\r\nana@example.org\r\n".getBytes(StandardCharsets.UTF_8);

    client.enrollments().previewImport(params);
    Files.write(roster, corrected);
    client.enrollments().previewImport(params);

    List<TransportRequest> calls = api.requestsTo(ROUTE);
    assertArrayEquals(CSV, partsOf(calls.get(0)).get("file").content());
    assertArrayEquals(corrected, partsOf(calls.get(1)).get("file").content());
  }

  @Test
  void aMissingFileFailsTheCallBeforeAnythingIsSent() {
    FakeTransport api = api().on(ROUTE, Reply.json(PREVIEW_JSON));
    EnrollmentPreviewImportParams params =
        EnrollmentPreviewImportParams.builder()
            .file(FileUpload.of(directory.resolve("missing.csv")))
            .build();

    assertThrows(
        UncheckedIOException.class, () -> clientOn(api).enrollments().previewImport(params));

    assertTrue(api.requestsTo(ROUTE).isEmpty());
  }
}
