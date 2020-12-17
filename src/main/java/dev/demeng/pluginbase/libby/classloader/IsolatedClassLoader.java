package dev.demeng.pluginbase.libby.classloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * This class loader is a simple child of {@link URLClassLoader} that uses the JVM's Extensions
 * Class Loader as the parent instead of the system class loader to provide an unpolluted classpath.
 *
 * <p>Mainly for internal use.
 */
public class IsolatedClassLoader extends URLClassLoader {

  static {
    ClassLoader.registerAsParallelCapable();
  }

  /**
   * Creates a new isolated class loader for the given URLs.
   *
   * @param urls The URLs to add to the classpath
   */
  public IsolatedClassLoader(URL... urls) {
    super(requireNonNull(urls, "urls"), ClassLoader.getSystemClassLoader().getParent());
  }

  /**
   * Adds a URL to the classpath.
   *
   * @param url The URL to add
   */
  @Override
  public void addURL(URL url) {
    super.addURL(url);
  }

  /**
   * Adds a path to the classpath.
   *
   * @param path The path to add
   */
  public void addPath(Path path) {
    try {
      addURL(requireNonNull(path, "path").toUri().toURL());
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
