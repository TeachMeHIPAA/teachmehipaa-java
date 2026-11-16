package com.teachmehipaa.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * The generator's command line.
 *
 * <pre>
 * Generate &lt;repo root&gt;          rewrite the generated files
 * Generate --check &lt;repo root&gt;  fail if they are stale
 * </pre>
 */
public final class Generate {
  public static void main(String[] args) throws IOException {
    boolean check = args.length == 2 && args[0].equals("--check");
    if (args.length != (check ? 2 : 1)) {
      System.err.println("usage: Generate [--check] <repo root>");
      System.exit(2);
    }
    Path root = Path.of(args[args.length - 1]);
    Map<String, String> fresh;
    try {
      fresh = Generator.render(root);
    } catch (GeneratorException noRule) {
      System.err.println("generate: " + noRule.getMessage());
      System.exit(1);
      return;
    }
    Map<String, String> committed = committed(root);
    if (check) {
      TreeSet<String> stale = new TreeSet<>();
      fresh.forEach(
          (path, source) -> {
            if (!source.equals(committed.get(path))) {
              stale.add(path);
            }
          });
      committed.keySet().stream().filter(path -> !fresh.containsKey(path)).forEach(stale::add);
      if (!stale.isEmpty()) {
        System.err.println("generate: stale generated files; run ./gradlew generate");
        stale.forEach(path -> System.err.println("  " + path));
        System.exit(1);
      }
      return;
    }
    for (String path : committed.keySet()) {
      if (!fresh.containsKey(path)) {
        Files.delete(root.resolve(path));
      }
    }
    for (Map.Entry<String, String> file : fresh.entrySet()) {
      Path target = root.resolve(file.getKey());
      Files.createDirectories(target.getParent());
      if (!file.getValue().equals(committed.get(file.getKey()))) {
        Files.writeString(target, file.getValue());
      }
    }
  }

  private static Map<String, String> committed(Path root) throws IOException {
    Map<String, String> files = new java.util.TreeMap<>();
    for (String directory : Generator.OWNED_DIRECTORIES) {
      Path owned = root.resolve(directory);
      if (!Files.isDirectory(owned)) {
        continue;
      }
      try (Stream<Path> listing = Files.list(owned)) {
        for (Path file : listing.toList()) {
          String path = root.relativize(file).toString();
          if (!HandWritten.files().contains(path)) {
            files.put(path, Generator.read(file));
          }
        }
      }
    }
    files.put(Generator.CLIENT_FILE, Generator.read(root.resolve(Generator.CLIENT_FILE)));
    Path reference = root.resolve(Generator.REFERENCE_FILE);
    if (Files.exists(reference)) {
      files.put(Generator.REFERENCE_FILE, Generator.read(reference));
    }
    return files;
  }

  private Generate() {}
}
