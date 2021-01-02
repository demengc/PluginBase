package dev.demeng.pluginbase.libby.relocation;

import dev.demeng.pluginbase.libby.Library;
import dev.demeng.pluginbase.libby.classloader.IsolatedClassLoader;
import dev.demeng.pluginbase.libby.managers.LibraryManager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A reflection-based helper for relocating library jars. It automatically downloads and invokes
 * Luck's Jar Relocator to perform jar relocations.
 *
 * <p>Primarily for internal use.
 *
 * @see <a href="https://github.com/lucko/jar-relocator">Luck's Jar Relocator</a>
 */
public class RelocationHelper {

  private final Constructor<?> jarRelocatorConstructor;
  private final Method jarRelocatorRunMethod;
  private final Constructor<?> relocationConstructor;

  /**
   * Creates a new relocation helper using the provided library manager to download the dependencies
   * required for runtime relocation.
   *
   * @param libraryManager The library manager used to download dependencies
   */
  public RelocationHelper(LibraryManager libraryManager) {
    Objects.requireNonNull(libraryManager, "libraryManager");

    IsolatedClassLoader classLoader = new IsolatedClassLoader();

    // ObjectWeb ASM Commons
    classLoader.addPath(
        libraryManager.downloadLibrary(
            Library.builder()
                .groupId("org.ow2.asm")
                .artifactId("asm-commons")
                .version("6.0")
                .checksum("8bzlxkipagF73NAf5dWa+YRSl/17ebgcAVpvu9lxmr8=")
                .build()));

    // ObjectWeb ASM
    classLoader.addPath(
        libraryManager.downloadLibrary(
            Library.builder()
                .groupId("org.ow2.asm")
                .artifactId("asm")
                .version("6.0")
                .checksum("3Ylxx0pOaXiZqOlcquTqh2DqbEhtxrl7F5XnV2BCBGE=")
                .build()));

    // Luck's Jar Relocator
    classLoader.addPath(
        libraryManager.downloadLibrary(
            Library.builder()
                .groupId("me.lucko")
                .artifactId("jar-relocator")
                .version("1.3")
                .checksum("mmz3ltQbS8xXGA2scM0ZH6raISlt4nukjCiU2l9Jxfs=")
                .build()));

    try {
      Class<?> jarRelocatorClass = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
      Class<?> relocationClass = classLoader.loadClass("me.lucko.jarrelocator.Relocation");

      // me.lucko.jarrelocator.JarRelocator(File, File, Collection)
      jarRelocatorConstructor =
          jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);

      // me.lucko.jarrelocator.JarRelocator#run()
      jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");

      // me.lucko.jarrelocator.Relocation(String, String, Collection, Collection)
      relocationConstructor =
          relocationClass.getConstructor(
              String.class, String.class, Collection.class, Collection.class);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Invokes the jar relocator to process the input jar and generate an output jar with the provided
   * relocation rules applied.
   *
   * @param in input jar
   * @param out output jar
   * @param relocations relocations to apply
   */
  public void relocate(Path in, Path out, Collection<Relocation> relocations) {
    Objects.requireNonNull(in, "in");
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(relocations, "relocations");

    try {
      List<Object> rules = new LinkedList<>();
      for (Relocation relocation : relocations) {
        rules.add(
            relocationConstructor.newInstance(
                relocation.getPattern(),
                relocation.getRelocatedPattern(),
                relocation.getIncludes(),
                relocation.getExcludes()));
      }

      jarRelocatorRunMethod.invoke(
          jarRelocatorConstructor.newInstance(in.toFile(), out.toFile(), rules));
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
