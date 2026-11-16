package com.teachmehipaa.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the filename from an RFC 6266 {@code Content-Disposition} header. */
final class ContentDisposition {
  private static final Pattern PARAMETER =
      Pattern.compile(";\\s*([^=;\\s]+)\\s*=\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([^;]*))");
  // RFC 8187: charset'language'percent-encoded-bytes.
  private static final Pattern EXTENDED_VALUE =
      Pattern.compile("([A-Za-z0-9!#$&+^_`{}~.-]+)'[^']*'([A-Za-z0-9!#$&+^_`|~.%-]*)");

  /**
   * The {@code filename*} when it decodes, since it is the only form that can carry non-ASCII
   * names, else the plain {@code filename}, else {@code null}.
   */
  static String filename(String disposition) {
    String plain = null;
    String extended = null;
    Matcher parameter = PARAMETER.matcher(disposition);
    while (parameter.find()) {
      String name = parameter.group(1).toLowerCase(Locale.ROOT);
      String value =
          parameter.group(2) != null
              ? parameter.group(2).replaceAll("\\\\(.)", "$1")
              : parameter.group(3).trim();
      if (name.equals("filename*") && extended == null) {
        extended = decode(value);
      } else if (name.equals("filename") && plain == null) {
        plain = value;
      }
    }
    return extended != null ? extended : plain;
  }

  private static String decode(String value) {
    Matcher matcher = EXTENDED_VALUE.matcher(value);
    if (!matcher.matches()) {
      return null;
    }
    Charset charset;
    try {
      charset = Charset.forName(matcher.group(1));
    } catch (IllegalCharsetNameException | UnsupportedCharsetException unknown) {
      return null;
    }
    String encoded = matcher.group(2);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    for (int i = 0; i < encoded.length(); i++) {
      char c = encoded.charAt(i);
      if (c != '%') {
        bytes.write(c);
      } else if (isHex(encoded, i + 1) && isHex(encoded, i + 2)) {
        bytes.write(HexFormat.fromHexDigits(encoded, i + 1, i + 3));
        i += 2;
      } else {
        return null;
      }
    }
    return bytes.toString(charset);
  }

  private static boolean isHex(String text, int index) {
    return index < text.length() && HexFormat.isHexDigit(text.charAt(index));
  }

  private ContentDisposition() {}
}
