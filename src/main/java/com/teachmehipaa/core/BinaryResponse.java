package com.teachmehipaa.core;

import java.util.Objects;

/** A downloaded file, such as a Certificate PDF, with its content type and filename. */
public final class BinaryResponse {
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
            .map(ContentDisposition::filename)
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

  /**
   * The filename from {@code Content-Disposition}, preferring the UTF-8 {@code filename*} form, or
   * {@code null} when it names none.
   */
  public String filename() {
    return filename;
  }

  /** The status, headers and request id of the response. */
  public LastResponse getLastResponse() {
    return lastResponse;
  }
}
