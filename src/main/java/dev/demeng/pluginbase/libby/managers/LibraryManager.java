package dev.demeng.pluginbase.libby.managers;

import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.relocation.Relocation;
import dev.demeng.pluginbase.libby.relocation.RelocationHelper;
import dev.demeng.pluginbase.plugin.BaseLoader;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

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
 * <p>This class is primarily for internal use. See {@link DemPlugin}.
 *
 * <p>Credit: https://github.com/Byteflux/libby
 */
public abstract class LibraryManager {

  protected final Path saveDirectory;
  private final List<String> repositories = new LinkedList<>();
  private RelocationHelper relocator;

  protected LibraryManager(Path dataDirectory) {
    saveDirectory =
        Objects.requireNonNull(dataDirectory, "dataDirectory").toAbsolutePath().resolve("lib");
  }

  protected abstract void addToClasspath(Path file);

  /**
   * Gets the currently added repositories used to resolve artifacts.
   *
   * <p>For each library this list is traversed to download artifacts after the direct download URLs
   * have been attempted.
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
  public void addRepository(String url) {
    final String repo = Objects.requireNonNull(url, "url").endsWith("/") ? url : url + '/';
    synchronized (repositories) {
      repositories.add(repo);
    }
  }

  /** Adds the current user's local Maven repository. */
  public void addMavenLocal() {
    addRepository(
        Paths.get(System.getProperty("user.home")).resolve(".m2/repository").toUri().toString());
  }

  /** Adds the Maven Central repository. */
  public void addMavenCentral() {
    addRepository("https://repo1.maven.org/maven2/");
  }

  /** Adds the Sonatype OSS repository. */
  public void addSonatype() {
    addRepository("https://oss.sonatype.org/content/groups/public/");
  }

  /** Adds the Bintray JCenter repository. */
  public void addJCenter() {
    addRepository("https://jcenter.bintray.com/");
  }

  /** Adds the JitPack repository. */
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
  public Collection<String> resolveLibrary(Library library) {
    List<String> urls = new LinkedList<>(Objects.requireNonNull(library, "library").getUrls());
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
  private byte[] downloadLibrary(String url) {

    final Logger logger = BaseLoader.getPlugin().getLogger();

    try {
      final URLConnection connection = new URL(Objects.requireNonNull(url, "url")).openConnection();

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
          return null;
        }

        logger.info("Downloaded library " + connection.getURL());
        return out.toByteArray();
      }

    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException(ex);

    } catch (IOException ex) {
      if (ex instanceof FileNotFoundException) {
        logger.warning("File not found: " + url);

      } else if (ex instanceof SocketTimeoutException) {
        logger.warning("Connect timed out: " + url);

      } else if (ex instanceof UnknownHostException) {
        logger.warning("Unknown host: " + url);

      } else {
        logger.warning("Unexpected IOException.");
        ex.printStackTrace();
      }

      return null;
    }
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
  public Path downloadLibrary(Library library) {

    final Logger logger = BaseLoader.getPlugin().getLogger();

    Path file = saveDirectory.resolve(Objects.requireNonNull(library, "library").getPath());
    if (Files.exists(file)) {
      return file;
    }

    Collection<String> urls = resolveLibrary(library);
    if (urls.isEmpty()) {
      throw new RuntimeException(
          "Library '" + library + "' couldn't be resolved, add a repository");
    }

    MessageDigest md = null;
    if (library.hasChecksum()) {
      try {
        md = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }

    Path out = file.resolveSibling(file.getFileName() + ".tmp");
    out.toFile().deleteOnExit();

    try {
      Files.createDirectories(file.getParent());

      for (String url : urls) {
        byte[] bytes = downloadLibrary(url);
        if (bytes == null) {
          continue;
        }

        if (md != null) {
          byte[] checksum = md.digest(bytes);
          if (!Arrays.equals(checksum, library.getChecksum())) {
            logger.warning("*** INVALID CHECKSUM ***");
            logger.warning(" Library :  " + library);
            logger.warning(" URL :  " + url);
            logger.warning(
                " Expected :  " + Base64.getEncoder().encodeToString(library.getChecksum()));
            logger.warning(" Actual :  " + Base64.getEncoder().encodeToString(checksum));
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
      }
    }

    throw new RuntimeException("Failed to download library '" + library + "'");
  }

  /**
   * Processes the input jar and generates an output jar with the provided relocation rules applied,
   * then returns the path to the relocated jar.
   *
   * @param in input jar
   * @param out output jar
   * @param relocations relocations to apply
   * @return the relocated file
   * @see RelocationHelper#relocate(Path, Path, Collection)
   */
  private Path relocate(Path in, String out, Collection<Relocation> relocations) {
    Objects.requireNonNull(in, "in");
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(relocations, "relocations");

    Path file = saveDirectory.resolve(out);
    if (Files.exists(file)) {
      return file;
    }

    Path tmpOut = file.resolveSibling(file.getFileName() + ".tmp");
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
          .info("Relocations applied to " + saveDirectory.getParent().relativize(in));

      return file;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      try {
        Files.deleteIfExists(tmpOut);
      } catch (IOException ignored) {
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
  public void loadLibrary(Library library) {
    Path file = downloadLibrary(Objects.requireNonNull(library, "library"));
    if (library.hasRelocations()) {
      file = relocate(file, library.getRelocatedPath(), library.getRelocations());
    }

    addToClasspath(file);
  }
}
