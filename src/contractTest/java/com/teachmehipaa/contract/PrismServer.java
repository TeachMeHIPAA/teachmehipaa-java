package com.teachmehipaa.contract;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A Prism mock of the vendored developer spec, run through npx.
 *
 * <p>Prism serves static responses built from the spec's schemas and examples. Neither {@code
 * --errors} nor {@code --dynamic} is used: both make Prism fail on its own generated responses
 * (static strings ignore {@code pattern}; dynamic numbers break OpenAPI 3.1 {@code
 * exclusiveMinimum}), which says nothing about the SDK. Prism still answers a request that breaks
 * the spec with a 4xx, which the SDK raises, and names each broken rule in the {@code
 * sl-violations} header, which {@link ViolationRecorder} collects.
 */
final class PrismServer implements AutoCloseable {
  private static final String PRISM = "@stoplight/prism-cli@5.16.0";
  private static final String SPEC = "openapi/openapi.developer.json";
  // The first run downloads Prism through npx.
  private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(180);

  private final Process process;
  private final Path log;
  private final int port;

  private PrismServer(Process process, Path log, int port) {
    this.process = process;
    this.log = log;
    this.port = port;
  }

  /** Starts Prism in the project directory, Gradle's working directory for tests. */
  static PrismServer start() throws IOException, InterruptedException {
    int port = freePort();
    Path log = Files.createTempFile("prism", ".log");
    Process process;
    try {
      // Prism's arguments are fixed here, never taken from input.
      process =
          new ProcessBuilder(
                  List.of(
                      "npx",
                      "--yes",
                      PRISM,
                      "mock",
                      SPEC,
                      "-h",
                      "127.0.0.1",
                      "-p",
                      String.valueOf(port)))
              .redirectErrorStream(true)
              .redirectOutput(log.toFile())
              .start();
    } catch (IOException noNode) {
      throw new IllegalStateException(
          "The contract suite needs Node.js: npx could not be started", noNode);
    }
    PrismServer prism = new PrismServer(process, log, port);
    prism.awaitListening();
    return prism;
  }

  /**
   * The base URL to give the client. Prism serves the spec's paths without the server's {@code /v1}
   * prefix, so it is the bare host.
   */
  String baseUrl() {
    return "http://127.0.0.1:" + port;
  }

  private void awaitListening() throws IOException, InterruptedException {
    Instant deadline = Instant.now().plus(STARTUP_TIMEOUT);
    while (!output().contains("Prism is listening")) {
      if (!process.isAlive()) {
        throw new IllegalStateException("Prism exited:\n" + output());
      }
      if (Instant.now().isAfter(deadline)) {
        close();
        throw new IllegalStateException("Prism did not start:\n" + output());
      }
      Thread.sleep(200);
    }
  }

  private String output() throws IOException {
    return Files.readString(log, StandardCharsets.UTF_8);
  }

  @Override
  public void close() throws IOException {
    // npx runs Prism as a grandchild; stopping only npx would leave Prism holding the port.
    List<ProcessHandle> descendants = process.descendants().toList();
    descendants.forEach(ProcessHandle::destroy);
    process.destroy();
    try {
      if (!process.waitFor(30, TimeUnit.SECONDS)) {
        process.destroyForcibly();
      }
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
    descendants.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
    Files.deleteIfExists(log);
  }

  // Releasing the port before Prism binds it is racy in theory, but Prism binds within moments.
  private static int freePort() throws IOException {
    try (ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
      return probe.getLocalPort();
    }
  }
}
