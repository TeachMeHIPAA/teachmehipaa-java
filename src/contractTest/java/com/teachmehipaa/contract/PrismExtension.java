package com.teachmehipaa.contract;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Hands tests the one {@link PrismServer} of the run. It starts on first use and is kept in the
 * root store, which closes it, stopping Prism, once every test has run.
 */
final class PrismExtension implements ParameterResolver {
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(PrismExtension.class);

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    return parameter.getParameter().getType() == PrismServer.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
    return context
        .getRoot()
        .getStore(NAMESPACE)
        .getOrComputeIfAbsent("prism", key -> start(), PrismServer.class);
  }

  private static PrismServer start() {
    try {
      return PrismServer.start();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while starting Prism", interrupted);
    } catch (IOException failed) {
      throw new UncheckedIOException(failed);
    }
  }
}
