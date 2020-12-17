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
