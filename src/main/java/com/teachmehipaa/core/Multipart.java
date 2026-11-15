package com.teachmehipaa.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Encodes RFC 7578 {@code multipart/form-data} bodies. */
final class Multipart {
  /**
   * The file parts first, then one {@code application/json} part per other field, as the spec's
   * {@code encoding} for the import asks.
   */
  static byte[] encode(String boundary, ObjectNode fields, List<ApiRequest.FilePart> files) {
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    for (ApiRequest.FilePart file : files) {
      part(
          body,
          boundary,
          "form-data; name=\""
              + quote(file.name())
              + "\"; filename=\""
              + quote(file.file().filename())
              + "\"",
          file.contentType(),
          file.file().data());
    }
    fields
        .properties()
        .forEach(
            field ->
                part(
                    body,
                    boundary,
                    "form-data; name=\"" + quote(field.getKey()) + "\"",
                    "application/json",
                    json(field.getValue())));
    write(body, "--" + boundary + "--\r\n");
    return body.toByteArray();
  }

  private static void part(
      ByteArrayOutputStream body,
      String boundary,
      String disposition,
      String contentType,
      byte[] content) {
    write(body, "--" + boundary + "\r\n");
    write(body, "Content-Disposition: " + disposition + "\r\n");
    write(body, "Content-Type: " + contentType + "\r\n\r\n");
    body.writeBytes(content);
    write(body, "\r\n");
  }

  // RFC 7578 §4.2: quotes and line breaks in names are percent-encoded.
  private static String quote(String name) {
    return name.replace("\"", "%22").replace("\r", "%0D").replace("\n", "%0A");
  }

  private static byte[] json(Object value) {
    try {
      return Json.MAPPER.writeValueAsBytes(value);
    } catch (JsonProcessingException unserializable) {
      throw new IllegalStateException(
          "multipart field is not serializable as JSON", unserializable);
    }
  }

  private static void write(ByteArrayOutputStream body, String text) {
    body.writeBytes(text.getBytes(StandardCharsets.UTF_8));
  }

  private Multipart() {}
}
