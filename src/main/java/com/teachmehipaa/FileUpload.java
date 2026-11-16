package com.teachmehipaa;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A file sent in a multipart request, such as the CSV of an Enrollment import. Each call reads it
 * once and sends those bytes, so a retry resends exactly what the first attempt sent, under the
 * same {@code Idempotency-Key}. Immutable.
 */
public final class FileUpload {
  private final byte[] data;
  private final Path path;
  private final String filename;

  private FileUpload(byte[] data, Path path, String filename) {
    this.data = data;
    this.path = path;
    this.filename = Objects.requireNonNull(filename, "filename");
  }

  /** The file's bytes, sent under {@code filename}. */
  public static FileUpload of(byte[] data, String filename) {
    return new FileUpload(Objects.requireNonNull(data, "data").clone(), null, filename);
  }

  /**
   * Reads {@code stream} to its end now, since a stream can be read only once; the caller still
   * closes it.
   *
   * @throws UncheckedIOException when the stream can't be read
   */
  public static FileUpload of(InputStream stream, String filename) {
    Objects.requireNonNull(stream, "stream");
    try {
      return new FileUpload(stream.readAllBytes(), null, filename);
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  /**
   * The file at {@code path}, sent under its own name. Each call that sends it reads it then, so a
   * file corrected between calls is sent as corrected; a file that can't be read fails the call
   * with an {@link UncheckedIOException} before anything is sent.
   */
  public static FileUpload of(Path path) {
    Objects.requireNonNull(path, "path");
    return new FileUpload(null, path, String.valueOf(path.getFileName()));
  }

  /**
   * Returns the file's bytes: a copy, or for a path, the file as it is now.
   *
   * @throws UncheckedIOException when the file at the path can't be read
   */
  public byte[] data() {
    if (path == null) {
      return data.clone();
    }
    try {
      return Files.readAllBytes(path);
    } catch (IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  /** The name the file is sent under. */
  public String filename() {
    return filename;
  }
}
