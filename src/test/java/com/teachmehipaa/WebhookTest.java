package com.teachmehipaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.exception.WebhookSignatureException;
import com.teachmehipaa.model.EventLearnerCertified;
import com.teachmehipaa.model.UnknownEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link Webhook#constructEvent} and {@link Webhook#generateTestHeaders}, tested with the shared
 * cross-SDK vector ({@code _shared/webhook-vector.json}).
 *
 * <p>The vector was signed by the platform's own signer, not this SDK: {@code bun platform-sign.ts}
 * run in the monorepo calls packages/auth-s2s {@code signRequest} on packages/webhooks {@code
 * canonicalizeEventBody(envelope)}, and a node:crypto-only replica of auth-s2s canonical.ts
 * produced the same hex.
 */
class WebhookTest {
  private static final String SECRET = "4l28+FYKZo2O57actVGINwR/Xyhzt3OtqCipV81/Zd0=";
  private static final String KEY_ID = "WHSEC-7KQ2-M4XD-9RTB-H3VN";
  private static final String PATH = "/hooks/tmh?source=teachmehipaa";
  private static final String EVENT_TIMESTAMP = "2026-11-15T16:31:32.123Z";
  private static final String EVENT_BODY =
      "{\"data\":{\"courseId\":\"hipaa-basics-2026\",\"email\":\"zoë.müller@example.com\","
          + "\"externalId\":\"hr-123\",\"learnerId\":\"lrn_4h7j2k\","
          + "\"occurredAt\":\"2026-11-15T16:31:30.000Z\",\"status\":\"certified\"},"
          + "\"eventId\":\"evt_01JCZ8Y2K5V3M7Q9R4T6W8X0AB\",\"eventType\":\"learner.certified\","
          + "\"occurredAt\":\"2026-11-15T16:31:30.000Z\",\"orgId\":\"org_9f3k2m\"}";
  private static final String EVENT_BODY_HASH =
      "0a849105d2fea53433e3284d0dc6dee83ebccdf6e64088bf34bf3b537b6e640b";
  private static final String EVENT_SIGNATURE =
      "c2a0fd14bca6c361c44e84a5b3bf0378b00c49a533636a229c2b6972073b618a";

  // What a signer that wrongly base64-decodes the Signing secret produces for the same Delivery
  // (vector/replica.mjs, Buffer.from(secret, "base64")).
  private static final String SIGNATURE_WITH_DECODED_KEY =
      "26c88e3993dc44d1f52949f6bc1a9061c5e3e9c0b12caf04934e2e4ea3814f2d";

  // The vector's Test delivery, signed the same way: the body the platform's :test route builds,
  // canonicalized.
  private static final String TEST_DELIVERY_TIMESTAMP = "2026-11-15T16:40:05.000Z";
  private static final String TEST_DELIVERY_BODY =
      "{\"subscriptionId\":\"whs_2b8f4c1d\",\"test\":true}";
  private static final String TEST_DELIVERY_BODY_HASH =
      "42879a05e13a5dfba51eee07c2fef4814ec6009091f1389a8db4403661ce6a66";
  private static final String TEST_DELIVERY_SIGNATURE =
      "b7baa3ae58a738d025adf4e27ce7af833ac6085f5cb1cc5fc8ff1addb191c3b9";

  private static final Instant AT_EVENT_TIME = Instant.parse("2026-11-15T16:31:40.000Z");

  private static Map<String, String> eventHeaders() {
    return Map.of(
        "x-tmh-key-id", KEY_ID,
        "x-tmh-timestamp", EVENT_TIMESTAMP,
        "x-tmh-signature", EVENT_SIGNATURE,
        "x-tmh-body-hash", EVENT_BODY_HASH,
        "x-tmh-event-type", "learner.certified",
        "x-tmh-event-id", "evt_01JCZ8Y2K5V3M7Q9R4T6W8X0AB",
        "content-type", "application/json");
  }

  private static final String TAMPERED_EVENT_BODY = EVENT_BODY.replace("hr-123", "hr-124");

  private static Map<String, String> eventHeadersWithout(String name) {
    Map<String, String> headers = new HashMap<>(eventHeaders());
    headers.remove(name);
    return headers;
  }

  private static Map<String, String> eventHeadersWith(String name, String value) {
    Map<String, String> headers = new HashMap<>(eventHeaders());
    headers.put(name, value);
    return headers;
  }

  private static Webhook.VerifyOptions at(Instant receivedAt) {
    return Webhook.VerifyOptions.builder().receivedAt(receivedAt).build();
  }

  private static WebhookSignatureException rejection(String payload, Map<String, String> headers) {
    return rejection(payload, headers, SECRET, PATH, at(AT_EVENT_TIME));
  }

  private static WebhookSignatureException rejection(
      String payload,
      Map<String, String> headers,
      String secret,
      String path,
      Webhook.VerifyOptions options) {
    WebhookSignatureException error =
        assertThrows(
            WebhookSignatureException.class,
            () -> Webhook.constructEvent(payload, headers, secret, path, options));
    assertInstanceOf(TeachMeHipaaException.class, error);
    // ADR-0007: the body may hold PHI, so a rejection never carries it.
    assertNull(error.getRawBody());
    assertTrue(error.getHeaders().map().isEmpty());
    assertNull(error.getStatusCode());
    assertFalse(error.getMessage().contains("hr-12"));
    return error;
  }

  @Test
  void verifiesADeliverySignedByThePlatformAndReturnsItsTypedEvent() {
    WebhookDelivery delivery =
        Webhook.constructEvent(EVENT_BODY, eventHeaders(), SECRET, PATH, at(AT_EVENT_TIME));

    WebhookDelivery.EventDelivery received =
        assertInstanceOf(WebhookDelivery.EventDelivery.class, delivery);
    EventLearnerCertified certified =
        assertInstanceOf(EventLearnerCertified.class, received.event());
    assertEquals("evt_01JCZ8Y2K5V3M7Q9R4T6W8X0AB", certified.getEventId());
    assertEquals("learner.certified", certified.getEventType());
    assertEquals("org_9f3k2m", certified.getOrgId());
    assertEquals(
        OffsetDateTime.of(2026, 11, 15, 16, 31, 30, 0, ZoneOffset.UTC), certified.getOccurredAt());
    assertEquals("lrn_4h7j2k", certified.getData().getLearnerId());
    assertEquals("zoë.müller@example.com", certified.getData().getEmail());
    assertEquals("hr-123", certified.getData().getExternalId());
    assertEquals("hipaa-basics-2026", certified.getData().getCourseId());
    assertEquals("certified", certified.getData().getStatus().value());
  }

  @Test
  void rejectsATamperedBody() {
    WebhookSignatureException error = rejection(TAMPERED_EVENT_BODY, eventHeaders());

    assertEquals(WebhookSignatureException.BODY_HASH_MISMATCH, error.getCode());
    assertTrue(error.getMessage().contains("raw request body"));
  }

  @Test
  void rejectsATamperedBodyWhenNoBodyHashHeaderCameWithIt() {
    WebhookSignatureException error =
        rejection(TAMPERED_EVENT_BODY, eventHeadersWithout("x-tmh-body-hash"));

    assertEquals(WebhookSignatureException.SIGNATURE_MISMATCH, error.getCode());
  }

  @Test
  void rejectsADeliveryVerifiedAgainstTheWrongPath() {
    WebhookSignatureException error =
        rejection(EVENT_BODY, eventHeaders(), SECRET, "/hooks/tmh", at(AT_EVENT_TIME));

    assertEquals("signature_mismatch", error.getCode());
    assertTrue(error.getMessage().contains("exact request path"));
  }

  @Test
  void rejectsADeliveryVerifiedWithTheWrongSigningSecret() {
    WebhookSignatureException error =
        rejection(
            EVENT_BODY,
            eventHeaders(),
            "Zm9vYmFyYmF6cXV4cXV1eGNvcmdlZ3JhdWx0Z2FycGx5",
            PATH,
            at(AT_EVENT_TIME));

    assertEquals("signature_mismatch", error.getCode());
  }

  @Test
  void rejectsASignatureMadeWithTheBase64DecodedSigningSecret() {
    WebhookSignatureException error =
        rejection(EVENT_BODY, eventHeadersWith("x-tmh-signature", SIGNATURE_WITH_DECODED_KEY));

    assertEquals("signature_mismatch", error.getCode());
  }

  @Test
  void rejectsAnUppercaseSignature() {
    WebhookSignatureException error =
        rejection(
            EVENT_BODY,
            eventHeadersWith("x-tmh-signature", EVENT_SIGNATURE.toUpperCase(Locale.ROOT)));

    assertEquals("signature_mismatch", error.getCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"x-tmh-signature", "x-tmh-timestamp"})
  void rejectsADeliveryWithoutARequiredHeader(String name) {
    WebhookSignatureException error = rejection(EVENT_BODY, eventHeadersWithout(name));

    assertEquals(WebhookSignatureException.MISSING_HEADER, error.getCode());
    assertTrue(error.getMessage().contains(name));
  }

  @Test
  void treatsAnEmptySignatureHeaderAsMissing() {
    WebhookSignatureException error =
        rejection(EVENT_BODY, eventHeadersWith("x-tmh-signature", ""));

    assertEquals("missing_header", error.getCode());
  }

  @Test
  void rejectsADeliveryWhoseTimestampIsMoreThanFiveMinutesOld() {
    WebhookSignatureException error =
        rejection(
            EVENT_BODY,
            eventHeaders(),
            SECRET,
            PATH,
            at(Instant.parse("2026-11-15T16:36:32.124Z")));

    assertEquals(WebhookSignatureException.TIMESTAMP_OUTSIDE_TOLERANCE, error.getCode());
  }

  @Test
  void rejectsADeliveryWhoseTimestampIsMoreThanFiveMinutesInTheFuture() {
    WebhookSignatureException error =
        rejection(
            EVENT_BODY,
            eventHeaders(),
            SECRET,
            PATH,
            at(Instant.parse("2026-11-15T16:26:32.122Z")));

    assertEquals("timestamp_outside_tolerance", error.getCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"2026-11-15T16:36:32.123Z", "2026-11-15T16:26:32.123Z"})
  void acceptsADeliveryExactlyFiveMinutesOff(String receivedAt) {
    WebhookDelivery delivery =
        Webhook.constructEvent(
            EVENT_BODY, eventHeaders(), SECRET, PATH, at(Instant.parse(receivedAt)));

    assertInstanceOf(WebhookDelivery.EventDelivery.class, delivery);
  }

  @Test
  void honorsACustomTolerance() {
    WebhookSignatureException error =
        rejection(
            EVENT_BODY,
            eventHeaders(),
            SECRET,
            PATH,
            Webhook.VerifyOptions.builder()
                .tolerance(Duration.ofSeconds(5))
                .receivedAt(AT_EVENT_TIME)
                .build());

    assertEquals("timestamp_outside_tolerance", error.getCode());
  }

  @Test
  void checksTheSignatureBeforeTheTimestampSoAForgeryNeverLooksMerelyLate() {
    WebhookSignatureException error =
        rejection(
            TAMPERED_EVENT_BODY,
            eventHeadersWithout("x-tmh-body-hash"),
            SECRET,
            PATH,
            at(Instant.parse("2027-01-01T00:00:00.000Z")));

    assertEquals("signature_mismatch", error.getCode());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1763224292",
        "2026-11-15T16:31:32.123",
        "2026-11-15 16:31:32Z",
        "2026-11-15T16:31Z",
        "2026-13-15T16:31:32.123Z"
      })
  void rejectsATimestampThatIsNotAnIso8601DateTimeWithAnOffset(String timestamp) {
    WebhookSignatureException error =
        rejection(EVENT_BODY, eventHeadersWith("x-tmh-timestamp", timestamp));

    assertEquals(WebhookSignatureException.INVALID_TIMESTAMP, error.getCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void refusesABlankSigningSecretAsAProgrammingError(String secret) {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                Webhook.constructEvent(
                    EVENT_BODY, eventHeaders(), secret, PATH, at(AT_EVENT_TIME)));

    assertTrue(error.getMessage().contains("Signing secret"));
  }

  @ParameterizedTest
  @ValueSource(longs = {0, -1})
  void refusesANonPositiveToleranceAsAProgrammingError(long seconds) {
    Webhook.VerifyOptions.Builder options = Webhook.VerifyOptions.builder();

    assertThrows(
        IllegalArgumentException.class, () -> options.tolerance(Duration.ofSeconds(seconds)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"hooks/tmh", "", "ftp://partner.example.com/hooks/tmh", "https:hooks"})
  void refusesAPathThatIsNeitherARequestPathNorAUrlAsAProgrammingError(String path) {
    assertThrows(
        IllegalArgumentException.class,
        () -> Webhook.constructEvent(EVENT_BODY, eventHeaders(), SECRET, path, at(AT_EVENT_TIME)));
  }

  @Test
  void returnsATestDeliveryAsItsOwnType() {
    Map<String, String> headers =
        Map.of(
            "x-tmh-key-id", KEY_ID,
            "x-tmh-timestamp", TEST_DELIVERY_TIMESTAMP,
            "x-tmh-signature", TEST_DELIVERY_SIGNATURE,
            "x-tmh-body-hash", TEST_DELIVERY_BODY_HASH,
            "x-tmh-event-type", "learner.certified",
            "x-tmh-event-id", "3f1c9a52-7d4e-4b8a-9c21-5e6f7a8b9c0d",
            "content-type", "application/json");

    WebhookDelivery delivery =
        Webhook.constructEvent(
            TEST_DELIVERY_BODY,
            headers,
            SECRET,
            PATH,
            at(Instant.parse("2026-11-15T16:40:06.000Z")));

    WebhookDelivery.TestDeliveryReceived received =
        assertInstanceOf(WebhookDelivery.TestDeliveryReceived.class, delivery);
    assertEquals("whs_2b8f4c1d", received.testDelivery().getSubscriptionId());
    assertTrue(received.testDelivery().isTest());
  }

  @Test
  void returnsAnEventOfATypeNotInTheCatalogAsAnUnknownEvent() {
    String body =
        "{\"data\":{\"badge\":\"gold\",\"learnerId\":\"lrn_4h7j2k\"},"
            + "\"eventId\":\"evt_01JCZ9000000000000000000AB\",\"eventType\":\"learner.promoted\","
            + "\"occurredAt\":\"2026-11-15T16:31:30.000Z\",\"orgId\":\"org_9f3k2m\"}";
    Map<String, String> headers =
        Webhook.generateTestHeaders(
            body,
            SECRET,
            PATH,
            Webhook.TestHeaderOptions.builder().timestamp(Instant.parse(EVENT_TIMESTAMP)).build());

    WebhookDelivery delivery =
        Webhook.constructEvent(body, headers, SECRET, PATH, at(AT_EVENT_TIME));

    WebhookDelivery.EventDelivery received =
        assertInstanceOf(WebhookDelivery.EventDelivery.class, delivery);
    UnknownEvent unknown = assertInstanceOf(UnknownEvent.class, received.event());
    assertEquals("learner.promoted", unknown.getEventType());
    assertEquals("evt_01JCZ9000000000000000000AB", unknown.getEventId());
    assertEquals("gold", unknown.getData().get("badge").asText());
  }

  @Test
  void generatesTheHeadersThePlatformSendsForTheSameDelivery() {
    Map<String, String> headers =
        Webhook.generateTestHeaders(
            EVENT_BODY,
            SECRET,
            PATH,
            Webhook.TestHeaderOptions.builder()
                .timestamp(Instant.parse(EVENT_TIMESTAMP))
                .eventType("learner.certified")
                .eventId("evt_01JCZ8Y2K5V3M7Q9R4T6W8X0AB")
                .keyId(KEY_ID)
                .build());

    assertEquals(eventHeaders(), headers);
  }

  @Test
  void generatesHeadersThatConstructEventAcceptsNow() {
    Map<String, String> headers = Webhook.generateTestHeaders(TEST_DELIVERY_BODY, SECRET, PATH);

    WebhookDelivery delivery = Webhook.constructEvent(TEST_DELIVERY_BODY, headers, SECRET, PATH);

    WebhookDelivery.TestDeliveryReceived received =
        assertInstanceOf(WebhookDelivery.TestDeliveryReceived.class, delivery);
    assertEquals("whs_2b8f4c1d", received.testDelivery().getSubscriptionId());
  }

  @Test
  void leavesOutTheIdentifyingHeadersItWasNotGiven() {
    Map<String, String> headers =
        Webhook.generateTestHeaders(
            TEST_DELIVERY_BODY,
            SECRET,
            PATH,
            Webhook.TestHeaderOptions.builder()
                .timestamp(Instant.parse(TEST_DELIVERY_TIMESTAMP))
                .build());

    assertEquals(
        Map.of(
            "x-tmh-timestamp", TEST_DELIVERY_TIMESTAMP,
            "x-tmh-signature", TEST_DELIVERY_SIGNATURE,
            "x-tmh-body-hash", TEST_DELIVERY_BODY_HASH,
            "content-type", "application/json"),
        headers);
  }

  @Test
  void signsTheTimestampInThePlatformsFormatWhateverItsPrecision() {
    Map<String, String> headers =
        Webhook.generateTestHeaders(
            TEST_DELIVERY_BODY,
            SECRET,
            PATH,
            Webhook.TestHeaderOptions.builder()
                .timestamp(Instant.parse("2026-11-15T16:40:05.000999999Z"))
                .build());

    assertEquals(TEST_DELIVERY_TIMESTAMP, headers.get("x-tmh-timestamp"));
    assertEquals(TEST_DELIVERY_SIGNATURE, headers.get("x-tmh-signature"));
  }

  @Test
  void generatesHeadersLookedUpWithoutRegardToCase() {
    Map<String, String> headers =
        Webhook.generateTestHeaders(
            TEST_DELIVERY_BODY,
            SECRET,
            PATH,
            Webhook.TestHeaderOptions.builder()
                .timestamp(Instant.parse(TEST_DELIVERY_TIMESTAMP))
                .build());

    assertEquals(TEST_DELIVERY_SIGNATURE, headers.get("X-TMH-Signature"));
  }

  @Test
  void generatesHeadersForARawBodyGivenAsBytes() {
    Map<String, String> headers =
        Webhook.generateTestHeaders(
            EVENT_BODY.getBytes(StandardCharsets.UTF_8),
            SECRET,
            PATH,
            Webhook.TestHeaderOptions.builder().timestamp(Instant.parse(EVENT_TIMESTAMP)).build());

    assertEquals(EVENT_SIGNATURE, headers.get("x-tmh-signature"));
    assertEquals(EVENT_BODY_HASH, headers.get("x-tmh-body-hash"));
  }

  @Test
  void acceptsTheRawBodyAsBytesAndHeadersInAnyCase() {
    Map<String, String> headers =
        Map.of("X-TMH-Timestamp", EVENT_TIMESTAMP, "X-Tmh-Signature", EVENT_SIGNATURE);

    WebhookDelivery delivery =
        Webhook.constructEvent(
            EVENT_BODY.getBytes(StandardCharsets.UTF_8), headers, SECRET, PATH, at(AT_EVENT_TIME));

    assertInstanceOf(
        EventLearnerCertified.class,
        assertInstanceOf(WebhookDelivery.EventDelivery.class, delivery).event());
  }

  @Test
  void acceptsAHeaderLookupSuchAsAServletRequestsGetHeader() {
    Map<String, String> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    requestHeaders.putAll(eventHeaders());

    WebhookDelivery delivery =
        Webhook.constructEvent(EVENT_BODY, requestHeaders::get, SECRET, PATH, at(AT_EVENT_TIME));

    assertInstanceOf(WebhookDelivery.EventDelivery.class, delivery);
  }

  @Test
  void acceptsTheFullRequestUrlInPlaceOfThePath() {
    WebhookDelivery delivery =
        Webhook.constructEvent(
            EVENT_BODY,
            eventHeaders(),
            SECRET,
            "https://partner.example.com/hooks/tmh?source=teachmehipaa",
            at(AT_EVENT_TIME));

    assertInstanceOf(WebhookDelivery.EventDelivery.class, delivery);
  }

  @Test
  void isAlsoReachableFromAClientWithoutMakingARequest() {
    FakeTransport api = new FakeTransport();
    TeachMeHipaaClient client = Fixtures.clientFor(api).build();

    Map<String, String> headers =
        client.webhooks().generateTestHeaders(TEST_DELIVERY_BODY, SECRET, PATH);
    WebhookDelivery delivery =
        client.webhooks().constructEvent(TEST_DELIVERY_BODY, headers, SECRET, PATH);
    WebhookDelivery fromBytes =
        client
            .webhooks()
            .constructEvent(
                EVENT_BODY.getBytes(StandardCharsets.UTF_8),
                eventHeaders()::get,
                SECRET,
                PATH,
                at(AT_EVENT_TIME));

    assertInstanceOf(WebhookDelivery.TestDeliveryReceived.class, delivery);
    assertInstanceOf(WebhookDelivery.EventDelivery.class, fromBytes);
    assertTrue(api.requests().isEmpty());
  }

  // The verifier hardcodes ADR-0005's recipe. If the vendored IR ever describes a different one,
  // this fails and the verifier must change with it.
  @Test
  void verifiesTheSigningRecipeTheVendoredIrDescribes() throws IOException {
    ObjectMapper json = new ObjectMapper();
    JsonNode expected =
        json.readTree(
            """
            {
              "algorithm": "hmacSha256Hex",
              "canonical": {
                "parts": [
                  { "kind": "literal", "value": "POST" },
                  { "kind": "pathAndQuery" },
                  { "kind": "header", "name": "x-tmh-timestamp" },
                  { "kind": "bodySha256Hex" }
                ],
                "separator": "\\n"
              },
              "headers": {
                "bodyHash": "x-tmh-body-hash",
                "eventId": "x-tmh-event-id",
                "eventType": "x-tmh-event-type",
                "keyId": "x-tmh-key-id",
                "signature": "x-tmh-signature",
                "timestamp": "x-tmh-timestamp"
              },
              "key": "secretUtf8",
              "timestampFormat": "iso8601",
              "toleranceSeconds": 300
            }
            """);

    JsonNode signing =
        json.readTree(Path.of("openapi/sdk-ir.json").toFile()).path("events").path("signing");

    assertEquals(expected, signing, "The IR's signing recipe changed: " + signing);
  }
}
