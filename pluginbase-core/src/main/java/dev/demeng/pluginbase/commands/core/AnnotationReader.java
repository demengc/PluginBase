/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.annotation.Command;
import dev.demeng.pluginbase.commands.annotation.Default;
import dev.demeng.pluginbase.commands.annotation.DistributeOnMethods;
import dev.demeng.pluginbase.commands.annotation.Subcommand;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@AllArgsConstructor
final class AnnotationReader {

  private static final List<Class<? extends Annotation>> COMMAND_ANNOTATIONS = Stream.of(
      Command.class,
      Subcommand.class,
      Default.class
  ).collect(Collectors.toList());

  public static AnnotationReader create(@NotNull final BaseCommandHandler handler,
      @NotNull final AnnotatedElement method) {

    return createReader(handler, method);
  }

  private final AnnotatedElement element;
  private Map<Class<? extends Annotation>, Annotation> annotations;

  public <T extends Annotation> T get(@NotNull final Class<T> annotationType) {
    return (T) annotations.get(annotationType);
  }

  public void add(@NotNull final Annotation annotation) {
    annotations.putIfAbsent(annotation.annotationType(), annotation);
  }

  void replaceAnnotations(final BaseCommandHandler handler) {
    if (handler.annotationReplacers.isEmpty()) {
      return;
    }
    final Map<Class<? extends Annotation>, Annotation> newAnnotations = new HashMap<>(annotations);
    for (final Annotation annotation : annotations.values()) {
      final List<Annotation> replaced = handler.replaceAnnotation(element, annotation);
      if (replaced == null) {
        continue;
      }
      replaced.forEach(a -> newAnnotations.put(a.annotationType(), a));
    }
    // we copy the replaced ones into a new map to avoid stackoverflow
    annotations = newAnnotations;
  }

  public boolean shouldDismiss() {
    if (!(element instanceof Method)) {
      return false;
    }
    if (annotations.isEmpty()) {
      return true;
    }
    return COMMAND_ANNOTATIONS.stream()
        .noneMatch(annotation -> annotations.containsKey(annotation));
  }

  @NotNull
  private static AnnotationReader createReader(@NotNull final BaseCommandHandler handler,
      @NotNull final AnnotatedElement method) {
    final Map<Class<? extends Annotation>, Annotation> annotations = toMap(method.getAnnotations());

    final AnnotationReader reader = new AnnotationReader(method, annotations);
    reader.replaceAnnotations(handler);
    return reader;
  }

  public void distributeAnnotations() {
    if (element instanceof Method) {
      Class<?> top = ((Method) element).getDeclaringClass();
      while (top != null) {
        toMap(top.getAnnotations()).forEach((type, annotation) -> {
          if (type.isAnnotationPresent(DistributeOnMethods.class)) {
            annotations.putIfAbsent(type, annotation);
          }
        });
        top = top.getDeclaringClass();
      }
    }
  }

  public <R, T extends Annotation> R get(@NotNull final Class<T> type, final Function<T, R> f) {
    return get(type, f, () -> null);
  }

  public <R, T extends Annotation> R get(@NotNull final Class<T> type, final Function<T, R> f,
      final Supplier<R> def) {
    final T ann = (T) annotations.get(type);
    if (ann != null) {
      return f.apply(ann);
    }
    return def.get();
  }

  public <T extends Annotation> @NotNull T get(@NotNull final Class<T> type, final String err) {
    final T ann = get(type);
    if (ann == null) {
      throw new IllegalStateException(err);
    }
    return ann;
  }

  public boolean contains(final Class<? extends Annotation> annotation) {
    return annotations.containsKey(annotation);
  }

  private static Map<Class<? extends Annotation>, Annotation> toMap(
      final Annotation[] annotations) {
    final Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();
    for (final Annotation annotation : annotations) {
      map.put(annotation.annotationType(), annotation);
    }
    return map;
  }

  public boolean isEmpty() {
    return annotations.isEmpty();
  }
}
