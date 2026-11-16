package com.teachmehipaa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.teachmehipaa.generator.HandWritten;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * {@code docs/reference/resources.md} names exactly the public service methods the compiled library
 * has, overload by overload, with their return types. {@link GeneratedSurfaceTest} proves the file
 * is what the generator writes; this proves what the generator writes is true.
 */
class ResourceReferenceTest {
  private static final Pattern SECTION =
      Pattern.compile(
          "^### `(\\w+)\\(\\)\\.(\\w+)`\\n.*?```java\\n(.*?)```",
          Pattern.MULTILINE | Pattern.DOTALL);

  @Test
  void listsEveryPublicServiceMethodAsCompiled() throws IOException {
    String reference = Files.readString(Path.of("docs/reference/resources.md"));

    Set<String> documented = new TreeSet<>();
    Matcher section = SECTION.matcher(reference);
    while (section.find()) {
      String accessor = section.group(1);
      section
          .group(3)
          .lines()
          .forEach(signature -> documented.add(accessor + "() " + withoutNames(signature)));
    }

    Set<String> compiled = new TreeSet<>();
    for (Method accessor : TeachMeHipaaClient.class.getMethods()) {
      Class<?> service = accessor.getReturnType();
      if (!service.getPackageName().equals("com.teachmehipaa.service")) {
        continue;
      }
      for (Method method : service.getMethods()) {
        boolean ownMethod =
            method.getDeclaringClass().getPackageName().equals("com.teachmehipaa.service");
        boolean handWritten =
            HandWritten.methods().contains(accessor.getName() + "." + method.getName());
        if (ownMethod && !handWritten && !Modifier.isStatic(method.getModifiers())) {
          compiled.add(accessor.getName() + "() " + signature(method));
        }
      }
    }

    assertEquals(compiled, documented);
  }

  private static String signature(Method method) {
    return render(method.getGenericReturnType())
        + " "
        + method.getName()
        + "("
        + Arrays.stream(method.getParameters())
            .map(parameter -> render(parameter.getParameterizedType()))
            .collect(Collectors.joining(", "))
        + ")";
  }

  /**
   * {@code Page<Learner> list(LearnerListParams params)} as {@code Page<Learner>
   * list(LearnerListParams)}: the library is compiled without {@code -parameters}, so reflection
   * can't check the names.
   */
  private static String withoutNames(String signature) {
    int open = signature.indexOf('(');
    String parameters = signature.substring(open + 1, signature.length() - 1);
    return signature.substring(0, open + 1)
        + (parameters.isEmpty()
            ? ""
            : Arrays.stream(parameters.split(", "))
                .map(parameter -> parameter.substring(0, parameter.lastIndexOf(' ')))
                .collect(Collectors.joining(", ")))
        + ")";
  }

  private static String render(Type type) {
    if (type instanceof ParameterizedType generic) {
      return render(generic.getRawType())
          + Arrays.stream(generic.getActualTypeArguments())
              .map(ResourceReferenceTest::render)
              .collect(Collectors.joining(", ", "<", ">"));
    }
    return ((Class<?>) type).getSimpleName();
  }
}
