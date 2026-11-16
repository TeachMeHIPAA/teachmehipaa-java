package com.teachmehipaa.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Javadoc comments from IR text. google-java-format rewraps them afterwards, so each paragraph is
 * written on one line.
 */
final class Javadoc {
  private static final Pattern CODE = Pattern.compile("`([^`]*)`");

  /**
   * IR text as Javadoc: markup characters escaped so doclint accepts any description, and Markdown
   * {@code `code`} as {@code <code>}.
   */
  static String text(String markdown) {
    String escaped =
        markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("@", "&#64;")
            // javac reads backslash-u escapes even inside comments.
            .replace("\\", "&#92;")
            .replace("*/", "*&#47;")
            .replace("\r", "");
    Matcher code = CODE.matcher(escaped);
    StringBuilder html = new StringBuilder();
    while (code.find()) {
      code.appendReplacement(html, Matcher.quoteReplacement("<code>" + code.group(1) + "</code>"));
    }
    code.appendTail(html);
    return html.toString().replaceAll("\n{2,}", "\n<p>").replace('\n', ' ').trim();
  }

  static boolean present(String text) {
    return text != null && !text.isBlank();
  }

  /**
   * A comment block at {@code indent}: paragraphs already in Javadoc form, then block tags. Null
   * and blank paragraphs are skipped; nothing to say gives no comment at all.
   */
  static String block(String indent, List<String> paragraphs, List<String> tags) {
    List<String> body = new ArrayList<>();
    paragraphs.stream()
        .filter(Objects::nonNull)
        .filter(paragraph -> !paragraph.isBlank())
        .forEach(paragraph -> body.add(body.isEmpty() ? paragraph : "<p>" + paragraph));
    if (body.isEmpty() && tags.isEmpty()) {
      return "";
    }
    StringBuilder comment = new StringBuilder(indent).append("/**\n");
    for (int i = 0; i < body.size(); i++) {
      if (i > 0) {
        comment.append(indent).append(" *\n");
      }
      comment.append(indent).append(" * ").append(body.get(i)).append('\n');
    }
    if (!tags.isEmpty()) {
      if (!body.isEmpty()) {
        comment.append(indent).append(" *\n");
      }
      tags.forEach(tag -> comment.append(indent).append(" * ").append(tag).append('\n'));
    }
    return comment.append(indent).append(" */\n").toString();
  }

  static String block(String indent, String... paragraphs) {
    return block(indent, Arrays.asList(paragraphs), List.of());
  }

  private Javadoc() {}
}
