package com.teachmehipaa.generator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Every hand-written file inside the generated packages and what it adds, in one table. The
 * generator reads it to know what to extend and what to leave alone; the generated-surface test
 * reads it to know which public methods have no IR method of their own.
 */
public final class HandWritten {
  private static final String SOURCE_ROOT = "src/main/java/com/teachmehipaa/";

  /** One hand-written file, by path under {@code com/teachmehipaa/}. */
  sealed interface Part {
    String file();
  }

  /**
   * A service with methods of its own. The generator writes the IR's methods into a sealed {@code
   * <Service>Base}; this file extends it and is what the client returns.
   *
   * @param resource the IR resource path, dotted
   * @param methods the public methods it adds, which no IR method backs
   */
  record ExtendedService(String resource, String file, List<String> methods) implements Part {}

  /**
   * A model with behaviour. The generator writes its fields into a sealed {@code <Model>Base}; this
   * file extends it and is what the API reads.
   *
   * @param model the IR type name
   */
  record ExtendedModel(String model, String file) implements Part {}

  /**
   * A subclass of {@code Operation} for a {@code response} the spec leaves untyped but whose shape
   * is known: the methods for these operation ids return it.
   */
  record OperationModel(String file, List<String> operationIds) implements Part {
    String className() {
      return file.substring(file.lastIndexOf('/') + 1, file.length() - ".java".length());
    }
  }

  /** A type the other hand-written files use. */
  record Supporting(String file) implements Part {}

  private static final List<Part> PARTS =
      List.of(
          new ExtendedService("events", "service/EventService.java", List.of("tail")),
          new Supporting("param/EventTailParams.java"),
          new ExtendedModel("Operation", "model/Operation.java"),
          new OperationModel(
              "model/WebhookDeliveryOperation.java",
              List.of("redriveWebhookSubscription", "testWebhookSubscription")),
          new Supporting("model/WebhookDeliveryResult.java"),
          new Supporting("model/ExportResult.java"));

  /**
   * The hand-written files, by path from the repository root; a generator run leaves them alone.
   */
  public static Set<String> files() {
    return PARTS.stream().map(part -> SOURCE_ROOT + part.file()).collect(Collectors.toSet());
  }

  /** The public methods no IR method backs, as {@code resource.method} paths. */
  public static Set<String> methods() {
    return parts(ExtendedService.class)
        .flatMap(
            service -> service.methods().stream().map(method -> service.resource() + "." + method))
        .collect(Collectors.toSet());
  }

  static Set<String> extendedResources() {
    return parts(ExtendedService.class).map(ExtendedService::resource).collect(Collectors.toSet());
  }

  static Set<String> extendedModels() {
    return parts(ExtendedModel.class).map(ExtendedModel::model).collect(Collectors.toSet());
  }

  /** The hand-written Operation subclass each operation id returns. */
  static Map<String, String> operationModels() {
    return parts(OperationModel.class)
        .flatMap(model -> model.operationIds().stream().map(id -> Map.entry(id, model.className())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static <T extends Part> Stream<T> parts(Class<T> kind) {
    return PARTS.stream().filter(kind::isInstance).map(kind::cast);
  }

  private HandWritten() {}
}
