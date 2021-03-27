/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matthew Harris
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

package dev.demeng.pluginbase.libby.classloader;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * A reflection-based wrapper around {@link URLClassLoader} for adding URLs to the classpath.
 *
 * <p>Mainly for internal use.
 */
public class URLClassLoaderHelper {

  /** The class loader being managed by this helper. */
  private final URLClassLoader classLoader;

  /** A reflected method in {@link URLClassLoader}, when invoked adds a URL to the classpath. */
  private final Method addURLMethod;

  /**
   * Creates a new URL class loader helper.
   *
   * @param classLoader The class loader to manage
   * @throws RuntimeException NoSuchMethodException
   */
  public URLClassLoaderHelper(URLClassLoader classLoader) throws RuntimeException {
    this.classLoader = requireNonNull(classLoader, "classLoader");

    try {
      addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURLMethod.setAccessible(true);

    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Adds a URL to the class loader's classpath.
   *
   * @param url The URL to add
   * @throws RuntimeException ReflectiveOperationException
   */
  public void addToClasspath(URL url) throws RuntimeException {
    try {
      addURLMethod.invoke(classLoader, requireNonNull(url, "url"));

    } catch (ReflectiveOperationException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Adds a path to the class loader's classpath.
   *
   * @param path The path to add
   * @throws IllegalArgumentException MalformedURLException
   */
  public void addToClasspath(Path path) throws RuntimeException {
    try {
      addToClasspath(requireNonNull(path, "path").toUri().toURL());

    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
