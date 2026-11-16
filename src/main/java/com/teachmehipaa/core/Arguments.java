package com.teachmehipaa.core;

import java.util.Objects;

/** Internal: argument checks shared by the public builders, so they reject input the same way. */
public final class Arguments {
  /**
   * @return {@code value}
   * @throws NullPointerException when it is {@code null}
   * @throws IllegalArgumentException when it is empty or only whitespace
   */
  public static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  private Arguments() {}
}
