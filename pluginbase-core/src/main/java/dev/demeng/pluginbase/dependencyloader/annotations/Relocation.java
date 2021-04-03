/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
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
 *
 */

package dev.demeng.pluginbase.dependencyloader.annotations;

import dev.demeng.pluginbase.dependencyloader.dependency.DependencyLoader;
import dev.demeng.pluginbase.dependencyloader.relocation.RelocationInfo;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;


/**
 * Used to define relocations for any imported {@link MavenDependency} dependencies.
 *
 * <p>You can't use '.' or '/' for the package names due to maven/Gradle relocation changing those
 * at compile time. The separator by default is '|' you can change the separator by changing the
 * separator value in this annotation.
 *
 * <p>Examples:</p>
 * <ul>
 *   ~Relocation(from = "com|google|guava", to = "my|package|guava")
 *   ~Relocation(from = "com{}google{}guava", to = "my{}package{}guava", separator = "{}"
 * </ul>
 *
 * @see RelocationInfo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Relocation.List.class)
public @interface Relocation {

  /**
   * The original package location.
   *
   * @return The original package location.
   */
  @NotNull String from();

  /**
   * Where to move the package to.
   *
   * @return Where to move the package to.
   */
  @NotNull String to();

  /**
   * The separator to use.
   *
   * @return The separator to use instead of '.' or '/'.
   */
  @NotNull String separator() default DependencyLoader.DEFAULT_SEPARATOR;

  /**
   * Used to store multiple {@link Relocation} annotations on a single class type.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface List {

    /**
     * An array of {@link Relocation} annotations.
     *
     * @return An array of {@link Relocation} annotations.
     */
    @NotNull Relocation[] value() default {};
  }
}

