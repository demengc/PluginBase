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

package dev.demeng.pluginbase.libby.managers;

import dev.demeng.pluginbase.exceptions.BaseException;
import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.relocation.Relocation;
import dev.demeng.pluginbase.libby.relocation.RelocationHelper;
import dev.demeng.pluginbase.plugin.BaseLoader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import lombok.NonNull;

/**
 * A runtime dependency manager for plugins.
 *
 * <p>The library manager can resolve a dependency jar through the configured Maven repositories,
 * download it into a local cache, relocate it and then load it into the plugin's classpath.
 *
 * <p>Transitive dependencies for a library aren't downloaded automatically and must be explicitly
 * loaded like every other library.
 *
 * <p>It's recommended that libraries are relocated to prevent any namespace conflicts with
 * different versions of the same library bundled with other plugins or maybe even bundled with the
 * server itself.
 *
 * <p>This class is primarily for internal use. See {@link dev.demeng.pluginbase.plugin.BasePlugin}.
 */
public abstract class LibraryManager {

  protected final Path saveDirectory;
  private final List<String> repositories = new LinkedList<>();
  private RelocationHelper relocator;

  protected LibraryManager(@NonNull Path dataDirectory) {
    saveDirectory = dataDirectory.toAbsolutePath().resolve("lib");
  }

  protected abstract void addToClasspath(Path file);

  /**
   * Gets the currently added repositories used to resolve artifacts.
   *
   * <p>For each library this list is traversed to download artifacts after the direct download
   * URLs have been attempted.
   *
   * @return Current repositories
   */
  public Collection<String> getRepositories() {

    List<String> urls;
    synchronized (repositories) {
      urls = new LinkedList<>(repositories);
    }

    return Collections.unmodifiableList(urls);
  }

  /**
   * Adds a repository URL to this library manager.
   *
   * <p>Artifacts will be resolved using this repository when attempts to locate the artifact
   * through previously added repositories are all unsuccessful.
   *
   * @param url Repository URL to add
   */
  public void addRepository(@NonNull String url) {
    final String repo = url.endsWith("/") ? url : url + '/';
    synchronized (repositories) {
      repositories.add(repo);
    }
  }

  /**
   * Adds the current user's local Maven repository.
   */
  public void addMavenLocal() {
    addRepository(
        Paths.get(System.getProperty("user.home")).resolve(".m2/repository").toUri().toString());
  }

  /**
   * Adds the Maven Central repository.
   */
  public void addMavenCentral() {
    addRepository("https://repo1.maven.org/maven2/");
  }

  /**
   * Adds the Sonatype OSS repository.
   */
  public void addSonatype() {
    addRepository("https://oss.sonatype.org/content/groups/public/");
  }

  /**
   * Adds the Bintray JCenter repository.
   */
  public void addJcenter() {
    addRepository("https://jcenter.bintray.com/");
  }

  /**
   * Adds the JitPack repository.
   */
  public void addJitPack() {
    addRepository("https://jitpack.io/");
  }

  /**
   * Gets all of the possible download URLs for this library. Entries are ordered by direct download
   * URLs first and then repository download URLs.
   *
   * @param library The library to resolve
   * @return Download URLs
   */
  public Collection<String> resolveLibrary(@NonNull Library library) {

    final List<String> urls = new LinkedList<>(library.getUrls());

    for (String repository : getRepositories()) {
      urls.add(repository + library.getPath());
    }

    return Collections.unmodifiableList(urls);
  }

  /**
   * Downloads a library jar and returns the contents as a byte array.
   *
   * @param url The URL to the library jar
   * @return Downloaded jar as byte array or null if nothing was downloaded
   */
  private byte[] downloadLibrary(@NonNull String url) {

    final Logger logger = BaseLoader.getPlugin().getLogger();

    try {
      final URLConnection connection = new URL(url).openConnection();

      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      connection.setRequestProperty("User-Agent", "PluginBase");

      try (InputStream in = connection.getInputStream()) {
        int len;
        final byte[] buf = new byte[8192];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
          while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
          }

        } catch (SocketTimeoutException ex) {
          logger.warning("Download timed out: " + connection.getURL());
          return new byte[0];
        }

        logger.info("Downloaded library " + connection.getURL());
        return out.toByteArray();
      }

    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException(ex);

    } catch (FileNotFoundException ex) {
      logger.warning("File not found: " + url);

    } catch (SocketTimeoutException ex) {
      logger.warning("Connection timed out: " + url);

    } catch (UnknownHostException ex) {
      logger.warning("Unknown host: " + url);

    } catch (IOException ex) {
      logger.warning("Unexpected IOException.");
      ex.printStackTrace();
    }

    return new byte[0];
  }

  /**
   * Downloads a library jar to the save directory if it doesn't already exist and returns the local
   * file path.
   *
   * <p>If the library has a checksum, it will be compared against the downloaded jar's checksum to
   * verify the integrity of the download. If the checksums don't match, a warning is generated and
   * the next download URL is attempted.
   *
   * <p>Checksum comparison is ignored if the library doesn't have a checksum or if the library jar
   * already exists in the save directory.
   *
   * <p>Most of the time it is advised to use {@link #loadLibrary(Library)} instead of this method
   * because this one is only concerned with downloading the jar and returning the local path. It's
   * usually more desirable to download the jar and add it to the plugin's classpath in one
   * operation.
   *
   * @param library the library to download
   * @return local file path to library
   * @see #loadLibrary(Library)
   */
  public Path downloadLibrary(@NonNull Library library) {

    final Logger logger = BaseLoader.getPlugin().getLogger();

    Path file = saveDirectory.resolve(library.getPath());
    if (Files.exists(file)) {
      return file;
    }

    Collection<String> urls = resolveLibrary(library);
    if (urls.isEmpty()) {
      throw new BaseException(
          "Library '" + library + "' could not be resolved, add a repository");
    }

    MessageDigest md = null;
    if (library.hasChecksum()) {
      try {
        md = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException ex) {
        throw new BaseException(ex);
      }
    }

    Path out = file.resolveSibling(file.getFileName() + ".tmp");
    out.toFile().deleteOnExit();

    try {
      Files.createDirectories(file.getParent());

      for (String url : urls) {
        byte[] bytes = downloadLibrary(url);
        if (bytes == null || bytes.length == 0) {
          continue;
        }

        if (md != null) {
          byte[] checksum = md.digest(bytes);
          if (!Arrays.equals(checksum, library.getChecksum())) {
            logger.warning("*** INVALID CHECKSUM ***");
            logger.warning(() -> "Library: " + library);
            logger.warning(() -> "URL: " + url);
            logger.warning(() -> "Actual: " + Base64.getEncoder().encodeToString(checksum));
            logger.warning(
                () -> "Expected: " + Base64.getEncoder().encodeToString(library.getChecksum()));
            continue;
          }
        }

        Files.write(out, bytes);
        Files.move(out, file);

        return file;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      try {
        Files.deleteIfExists(out);
      } catch (IOException ignored) {
        // Ignored
      }
    }

    throw new BaseException("Failed to download library '" + library + "'");
  }

  /**
   * Processes the input jar and generates an output jar with the provided relocation rules applied,
   * then returns the path to the relocated jar.
   *
   * @param in          input jar
   * @param out         output jar
   * @param relocations relocations to apply
   * @return the relocated file
   * @see RelocationHelper#relocate(Path, Path, Collection)
   */
  private Path relocate(@NonNull Path in, @NonNull String out,
      @NonNull Collection<Relocation> relocations) {

    final Path file = saveDirectory.resolve(out);
    if (Files.exists(file)) {
      return file;
    }

    final Path tmpOut = file.resolveSibling(file.getFileName() + ".tmp");
    tmpOut.toFile().deleteOnExit();

    synchronized (this) {
      if (relocator == null) {
        relocator = new RelocationHelper(this);
      }
    }

    try {
      relocator.relocate(in, tmpOut, relocations);
      Files.move(tmpOut, file);

      BaseLoader.getPlugin()
          .getLogger()
          .info(() -> "Relocations applied to " + saveDirectory.getParent().relativize(in));

      return file;

    } catch (IOException e) {
      throw new UncheckedIOException(e);

    } finally {
      try {
        Files.deleteIfExists(tmpOut);

      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  /**
   * Loads a library jar into the plugin's classpath. If the library jar doesn't exist locally, it
   * will be downloaded.
   *
   * <p>If the provided library has any relocations, they will be applied to create a relocated jar
   * and the relocated jar will be loaded instead.
   *
   * @param library the library to load
   * @see #downloadLibrary(Library)
   */
  public void loadLibrary(@NonNull Library library) {

    Path file = downloadLibrary(library);

    if (library.hasRelocations()) {
      file = relocate(file, library.getRelocatedPath(), library.getRelocations());
    }

    addToClasspath(file);
  }
}
