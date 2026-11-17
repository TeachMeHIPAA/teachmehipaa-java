package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.INTROSPECTION_ROUTE;
import static com.teachmehipaa.Fixtures.LEARNER_JSON;
import static com.teachmehipaa.Fixtures.LEARNER_ROUTE;
import static com.teachmehipaa.Fixtures.SANDBOX_BASE;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.introspectionJson;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The README's samples (see {@link DocSampleTest}), and the links of every Markdown page: each
 * relative link must reach a file that exists, and an anchor must name one of its headings.
 */
class ReadmeTest extends DocSampleTest {
  // A link target in a Markdown link or image, skipping absolute URLs and mail links.
  private static final Pattern LINK = Pattern.compile("\\]\\((?!https?:|mailto:)([^)\\s]+)\\)");
  private static final Pattern HEADING = Pattern.compile("^#{1,6} (.+)$", Pattern.MULTILINE);
  private static final Pattern FENCE =
      Pattern.compile("^```.*?^```$", Pattern.MULTILINE | Pattern.DOTALL);

  @Override
  List<Path> docs() {
    return List.of(Path.of("README.md"));
  }

  @Override
  Map<String, Check> checks() {
    return Map.of("quickstart", this::quickstart);
  }

  private void quickstart() throws Exception {
    api.on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)))
        .on(INTROSPECTION_ROUTE, Reply.json(introspectionJson("\"org_acme\"")))
        .on(LEARNER_ROUTE, Reply.json(LEARNER_JSON));

    run("quickstart");

    assertEquals(List.of("Ana Lima in_progress"), printed());
    assertEquals(
        URI.create(SANDBOX_BASE + "/auth/token"), api.requestsTo(TOKEN_ROUTE).get(0).uri());
  }

  static Stream<Path> linkedDocs() {
    return Stream.of(
        Path.of("README.md"),
        Path.of("CONTRIBUTING.md"),
        Path.of("CHANGELOG.md"),
        Path.of("SECURITY.md"),
        Path.of("docs/README.md"),
        Path.of("examples/README.md"));
  }

  @ParameterizedTest
  @MethodSource("linkedDocs")
  void relativeLinksResolve(Path doc) {
    List<String> broken = new ArrayList<>();
    Matcher link = LINK.matcher(FENCE.matcher(read(doc)).replaceAll(""));
    while (link.find()) {
      String target = link.group(1);
      int hash = target.indexOf('#');
      String file = hash < 0 ? target : target.substring(0, hash);
      String anchor = hash < 0 ? null : target.substring(hash + 1);
      Path linked = file.isEmpty() ? doc : doc.resolveSibling(file).normalize();
      boolean resolves =
          Files.exists(linked)
              && (anchor == null
                  || !linked.toString().endsWith(".md")
                  || anchors(linked).contains(anchor));
      if (!resolves) {
        broken.add(target);
      }
    }

    assertEquals(List.of(), broken, doc + " has broken links");
  }

  /** GitHub's heading anchors: lower case, punctuation but hyphens and underscores dropped. */
  private static Set<String> anchors(Path markdown) {
    Matcher heading = HEADING.matcher(FENCE.matcher(read(markdown)).replaceAll(""));
    List<String> found = new ArrayList<>();
    while (heading.find()) {
      found.add(
          heading
              .group(1)
              .strip()
              .toLowerCase(Locale.ROOT)
              .replaceAll("[^\\p{L}\\p{N} _-]", "")
              .replace(' ', '-'));
    }
    return found.stream().collect(Collectors.toSet());
  }
}
