package com.teachmehipaa.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * A file being downloaded, such as a Certificate PDF, read from {@link #inputStream()} as it
 * arrives. Close it, ideally with try-with-resources, to release the connection:
 *
 * <pre>{@code
 * try (BinaryStreamResponse pdf = client.certificates().downloadPdfStream(certId)) {
 *   Files.copy(pdf.inputStream(), Path.of(pdf.filename()));
 * }
 * }</pre>
 */
public final class BinaryStreamResponse implements AutoCloseable {
  private final InputStream inputStream;
  private final String contentType;
  private final String filename;
  private final LastResponse lastResponse;

  BinaryStreamResponse(InputStream inputStream, LastResponse lastResponse) {
    this.inputStream = Objects.requireNonNull(inputStream, "inputStream");
    this.lastResponse = Objects.requireNonNull(lastResponse, "lastResponse");
    this.contentType = lastResponse.headers().firstValue("content-type").orElse(null);
    this.filename =
        lastResponse
            .headers()
            .firstValue("content-disposition")
            .map(ContentDisposition::filename)
            .orElse(null);
  }

  /** The file's bytes, exactly as received. It can be read once. */
  public InputStream inputStream() {
    return inputStream;
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

  /**
   * Releases the connection, leaving any unread bytes unread.
   *
   * @throws UncheckedIOException when the connection can't be closed cleanly
   */
  @Override
  public void close() {
    try {
      inputStream.close();
    } catch (IOException failed) {
      throw new UncheckedIOException(failed);
    }
  }
}
