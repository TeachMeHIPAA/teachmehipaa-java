package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.BinaryStreamResponse;
import com.teachmehipaa.exception.ApiException;
import com.teachmehipaa.exception.NotFoundException;
import com.teachmehipaa.exception.TeachMeHipaaException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Certificate PDF downloads, whole and streamed. */
class CertificatePdfTest {
  private static final String CERTIFICATE_ID = "cert_01JD8Z4N3QX5V7W9Y2B4C6D8EH";
  private static final String ROUTE =
      "GET /v1/organizations/org_acme/certificates/" + CERTIFICATE_ID + "/content";
  private static final String ACCEPT = "application/pdf, application/problem+json";

  /** Every byte value, so any text decoding along the way would show. */
  private static final byte[] EVERY_BYTE = everyByte();

  private static final Reply PDF =
      new Reply(
          200,
          Map.of(
              "content-type",
              "application/pdf",
              "content-disposition",
              "attachment; filename=\"certificate-ana.pdf\"",
              "x-request-id",
              "req_pdf"),
          EVERY_BYTE);
  private static final Reply NOT_FOUND =
      Reply.problem(
              404,
              """
              {"type": "https://docs.teachmehipaa.com/errors/not_found", "title": "Not found",
               "status": 404, "code": "not_found", "detail": "No such certificate."}
              """)
          .withHeader("x-request-id", "req_404");
  private static final Reply HTML_PAGE =
      new Reply(
          200, Map.of("content-type", "text/html"), "<!doctype html><title>Not found</title>");

  private static byte[] everyByte() {
    byte[] bytes = new byte[256];
    for (int value = 0; value < bytes.length; value++) {
      bytes[value] = (byte) value;
    }
    return bytes;
  }

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  // --- Whole file ------------------------------------------------------------------------------

  @Test
  void everyByteComesBackUnchangedWithItsTypeAndFilename() {
    FakeTransport api = api().on(ROUTE, PDF);

    BinaryResponse pdf = clientOn(api).certificates().downloadPdf(CERTIFICATE_ID);

    assertArrayEquals(EVERY_BYTE, pdf.data());
    assertEquals("application/pdf", pdf.contentType());
    assertEquals("certificate-ana.pdf", pdf.filename());
    assertEquals(200, pdf.getLastResponse().statusCode());
    assertEquals("req_pdf", pdf.getLastResponse().requestId());
    assertEquals(ACCEPT, api.requestsTo(ROUTE).get(0).headers().get("accept"));
  }

  @Test
  void aUtf8FilenameIsPreferredOverThePlainOne() {
    FakeTransport api =
        api()
            .on(
                ROUTE,
                PDF.withHeader(
                    "content-disposition",
                    "attachment; filename=\"certificate-jose.pdf\";"
                        + " filename*=UTF-8''certificate-jos%C3%A9.pdf"));

    BinaryResponse pdf = clientOn(api).certificates().downloadPdf(CERTIFICATE_ID);

    assertEquals("certificate-josé.pdf", pdf.filename());
  }

  @Test
  void aMalformedUtf8FilenameFallsBackToThePlainOne() {
    FakeTransport api =
        api()
            .on(
                ROUTE,
                PDF.withHeader(
                    "content-disposition",
                    "attachment; filename*=UTF-8''broken%E; filename=\"certificate.pdf\""));

    BinaryResponse pdf = clientOn(api).certificates().downloadPdf(CERTIFICATE_ID);

    assertEquals("certificate.pdf", pdf.filename());
  }

  @Test
  void aProblemResponseRaisesTheTypedError() {
    FakeTransport api = api().on(ROUTE, NOT_FOUND);

    NotFoundException failed =
        assertThrows(
            NotFoundException.class,
            () -> clientOn(api).certificates().downloadPdf(CERTIFICATE_ID));

    assertEquals("not_found", failed.getCode());
    assertEquals("req_404", failed.getRequestId());
  }

  @Test
  void anHtmlPageIsNotHandedBackAsAPdf() {
    FakeTransport api = api().on(ROUTE, HTML_PAGE);

    TeachMeHipaaException failed =
        assertThrows(
            TeachMeHipaaException.class,
            () -> clientOn(api).certificates().downloadPdf(CERTIFICATE_ID));

    assertInstanceOf(ApiException.class, failed);
    assertEquals(200, failed.getStatusCode());
  }

  // --- Streamed --------------------------------------------------------------------------------

  @Test
  void aStreamYieldsEveryByteUnchanged() throws IOException {
    FakeTransport api = api().on(ROUTE, PDF);

    byte[] received;
    try (BinaryStreamResponse pdf =
        clientOn(api).certificates().downloadPdfStream(CERTIFICATE_ID)) {
      assertEquals("application/pdf", pdf.contentType());
      assertEquals("certificate-ana.pdf", pdf.filename());
      assertEquals(200, pdf.getLastResponse().statusCode());
      received = pdf.inputStream().readAllBytes();
    }

    assertArrayEquals(EVERY_BYTE, received);
    assertEquals(ACCEPT, api.requestsTo(ROUTE).get(0).headers().get("accept"));
  }

  @Test
  void aStreamedDownloadIsRetriedUntilTheFileStarts() throws IOException {
    FakeTransport api =
        api()
            .on(
                ROUTE,
                Reply.problem(
                    503,
                    "{\"title\": \"Unavailable\", \"status\": 503,"
                        + " \"code\": \"temporarily_unavailable\"}"))
            .on(ROUTE, PDF);

    try (BinaryStreamResponse pdf =
        clientOn(api).certificates().downloadPdfStream(CERTIFICATE_ID)) {
      assertArrayEquals(EVERY_BYTE, pdf.inputStream().readAllBytes());
    }

    assertEquals(2, api.requestsTo(ROUTE).size());
  }

  @Test
  void aStreamedProblemResponseRaisesTheTypedError() {
    FakeTransport api = api().on(ROUTE, NOT_FOUND);

    NotFoundException failed =
        assertThrows(
            NotFoundException.class,
            () -> clientOn(api).certificates().downloadPdfStream(CERTIFICATE_ID));

    assertEquals("No such certificate.", failed.getDetail());
  }

  @Test
  void anHtmlPageRaisesBeforeAnyStream() {
    FakeTransport api = api().on(ROUTE, HTML_PAGE);

    TeachMeHipaaException failed =
        assertThrows(
            TeachMeHipaaException.class,
            () -> clientOn(api).certificates().downloadPdfStream(CERTIFICATE_ID));

    assertInstanceOf(ApiException.class, failed);
  }
}
