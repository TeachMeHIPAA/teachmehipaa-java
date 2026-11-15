package com.teachmehipaa.core;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A downloaded file, such as a Certificate PDF, with its content type and filename. */
public final class BinaryResponse {
  // RFC 6266: filename="quoted" or filename=token; the RFC 5987 filename* form is not used by the
  // API.
  private static final Pattern FILENAME =
      Pattern.compile("filename\\s*=\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([^;\\s]+))");

  private final byte[] data;
  private final String contentType;
  private final String filename;
  private final LastResponse lastResponse;

  BinaryResponse(byte[] data, LastResponse lastResponse) {
    this.data = Objects.requireNonNull(data, "data");
    this.lastResponse = Objects.requireNonNull(lastResponse, "lastResponse");
    this.contentType = lastResponse.headers().firstValue("content-type").orElse(null);
    this.filename =
        lastResponse
            .headers()
            .firstValue("content-disposition")
            .map(BinaryResponse::filename)
            .orElse(null);
  }

  /** Returns a copy of the file's bytes, exactly as received. */
  public byte[] data() {
    return data.clone();
  }

  /** The response's {@code Content-Type}, such as {@code application/pdf}, or {@code null}. */
  public String contentType() {
    return contentType;
  }

  /** The filename from {@code Content-Disposition}, or {@code null} when it names none. */
  public String filename() {
    return filename;
  }

  /** The status, headers and request id of the response. */
  public LastResponse getLastResponse() {
    return lastResponse;
  }

  private static String filename(String disposition) {
    Matcher matcher = FILENAME.matcher(disposition);
    if (!matcher.find()) {
      return null;
    }
    return matcher.group(1) != null
        ? matcher.group(1).replaceAll("\\\\(.)", "$1")
        : matcher.group(2);
  }
}
