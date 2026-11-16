package com.teachmehipaa.generator;

/** The IR has a shape this generator has no rule for. */
final class GeneratorException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  GeneratorException(String message) {
    super(message);
  }
}
