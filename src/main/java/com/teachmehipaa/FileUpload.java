package com.teachmehipaa;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A file sent in a multipart request, such as the CSV of an Enrollment import. The content is read
 * when the upload is created, so retries resend exactly the same bytes. Immutable.
 */
public final class FileUpload {
  private final byte[] data;
  private final String filename;

  private FileUpload(byte[] data, String filename) {
    this.data = data;
    this.filename = Objects.requireNonNull(filename, "filename");
  }

  /** The file's bytes, sent under {@code filename}. */
  public static FileUpload of(byte[] data, String filename) {
    return new FileUpload(Objects.requireNonNull(data, "data").clone(), filename);
  }

  /**
   * Reads {@code stream} to its end now; the caller still closes it.
   *
   * @throws UncheckedIOException when the stream can't be read
   */
  public static FileUpload of(InputStream stream, String filename) {
    Objects.requireNonNull(stream, "stream");
    try {
      return new FileUpload(stream.readAllBytes(), filename);
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  /**
   * Reads the file at {@code path} now, sent under its own name.
   *
   * @throws UncheckedIOException when the file can't be read
   */
  public static FileUpload of(Path path) {
    Objects.requireNonNull(path, "path");
    try {
      return new FileUpload(Files.readAllBytes(path), String.valueOf(path.getFileName()));
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  /** Returns a copy of the file's bytes. */
  public byte[] data() {
    return data.clone();
  }

  /** The name the file is sent under. */
  public String filename() {
    return filename;
  }
}
