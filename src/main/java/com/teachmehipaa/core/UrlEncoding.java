package com.teachmehipaa.core;

import java.nio.charset.StandardCharsets;

final class UrlEncoding {
  private static final char[] HEX = "0123456789ABCDEF".toCharArray();

  /** RFC 3986 percent-encoding: everything but unreserved characters, so ids can't add segments. */
  static String percentEncode(String value) {
    StringBuilder encoded = new StringBuilder();
    for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
      int c = b & 0xFF;
      if (isUnreserved(c)) {
        encoded.append((char) c);
      } else {
        encoded.append('%').append(HEX[c >> 4]).append(HEX[c & 0xF]);
      }
    }
    return encoded.toString();
  }

  private static boolean isUnreserved(int c) {
    return (c >= 'A' && c <= 'Z')
        || (c >= 'a' && c <= 'z')
        || (c >= '0' && c <= '9')
        || c == '-'
        || c == '.'
        || c == '_'
        || c == '~';
  }

  private UrlEncoding() {}
}
