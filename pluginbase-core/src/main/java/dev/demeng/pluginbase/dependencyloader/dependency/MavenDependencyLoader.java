/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2021 Justin Heflin
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) lucko/helper contributors
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

package dev.demeng.pluginbase.dependencyloader.dependency;

import dev.demeng.pluginbase.dependencyloader.annotations.LoaderPriority;
import dev.demeng.pluginbase.dependencyloader.annotations.MavenDependency;
import dev.demeng.pluginbase.dependencyloader.annotations.MavenRepository;
import dev.demeng.pluginbase.dependencyloader.annotations.Relocation;
import dev.demeng.pluginbase.dependencyloader.dependency.builder.DependencyProvider;
import dev.demeng.pluginbase.dependencyloader.dependency.builder.MavenDependencyProvider;
import dev.demeng.pluginbase.dependencyloader.exceptions.DependencyLoadException;
import dev.demeng.pluginbase.dependencyloader.exceptions.InvalidDependencyException;
import dev.demeng.pluginbase.dependencyloader.relocation.RelocatableDependencyLoader;
import dev.demeng.pluginbase.dependencyloader.relocation.RelocationInfo;
import dev.demeng.pluginbase.dependencyloader.relocation.Relocator;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * The dependency loader responsible for loading Maven dependencies.
 */
@LoaderPriority
public final class MavenDependencyLoader extends
    RelocatableDependencyLoader<@NotNull MavenDependencyInfo> {

  /**
   * This is used as the user agent for requesting the direct download jar link.
   */
  private static final SimpleImmutableEntry<String, String> REQUEST_USER_AGENT =
      new SimpleImmutableEntry<>("User-Agent",
          "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 "
              + "Firefox/3.5.2" + " (.NET CLR 3.5.30729)"
      );

  /**
   * The repos attached to these dependencies.
   */
  private final @NotNull Set<MavenRepositoryInfo> repos;
  /**
   * Any relocations that should be ran on the incoming dependencies.
   */
  private final @NotNull Set<RelocationInfo> relocations;

  /**
   * Creates a new Maven dependency loader with the specified base path.
   *
   * @param basePath The base path used to resolve relative file names.
   */
  public MavenDependencyLoader(final @NotNull Path basePath) {
    this(basePath, "maven");
  }

  /**
   * Creates a new Maven dependency loader with the specified base path and destination directory
   * name.
   *
   * @param basePath           The base path used to resolve relative file names.
   * @param storageDestination relative location for Maven dependencies to be stored.
   */
  public MavenDependencyLoader(
      final @NotNull Path basePath,
      final @NotNull String storageDestination
  ) {
    super(basePath, storageDestination);
    this.repos = new HashSet<>();
    this.repos.add(MavenRepositoryInfo.of("https://repo1.maven.org/maven2/"));
    this.relocations = new HashSet<>();
  }

  @Override
  public void loadDependenciesFrom(final @NotNull Class<@NotNull ?> clazz) {
    if (clazz.isAnnotationPresent(MavenRepository.class)) {
      this.repos.add(MavenRepositoryInfo.of(clazz.getAnnotation(MavenRepository.class)));
    }

    if (clazz.isAnnotationPresent(MavenRepository.List.class)) {
      Arrays.stream(clazz.getAnnotation(MavenRepository.List.class).value())
          .map(MavenRepositoryInfo::of)
          .forEach(this.repos::add);
    }

    if (clazz.isAnnotationPresent(Relocation.class)) {
      this.relocations.add(RelocationInfo.of(clazz.getAnnotation(Relocation.class)));
    }

    if (clazz.isAnnotationPresent(Relocation.List.class)) {
      this.relocations.addAll(
          Arrays.stream(clazz.getAnnotation(Relocation.List.class).value())
              .map(RelocationInfo::of)
              .collect(Collectors.toList())
      );
    }

    if (clazz.isAnnotationPresent(MavenDependency.class)) {
      super.addDependency(MavenDependencyInfo.of(clazz.getAnnotation(MavenDependency.class)));
    }

    if (clazz.isAnnotationPresent(MavenDependency.List.class)) {
      Arrays.stream(clazz.getAnnotation(MavenDependency.List.class).value())
          .map(MavenDependencyInfo::of)
          .forEach(super::addDependency);
    }
  }

  @Override
  public void loadDependenciesFrom(
      final @NotNull DependencyProvider<@NotNull MavenDependencyInfo> dependencyProvider
  ) {
    final MavenDependencyProvider provider = (MavenDependencyProvider) dependencyProvider;

    this.repos.addAll(provider.getRepositories());
    this.relocations.addAll(provider.getRelocations());
    provider.getDependencies().forEach(super::addDependency);
  }

  private @NotNull Optional<SimpleImmutableEntry<String, URL>> findRepoForDependency(
      final @NotNull MavenDependencyInfo dependency
  ) throws IOException {
    Optional<SimpleImmutableEntry<String, URL>> downloadUrl = Optional.empty();
    for (final MavenRepositoryInfo repo : this.repos) {
      final URL tempUrl = new URL((repo.getUrl().endsWith("/")
          ? repo.getUrl()
          : repo.getUrl() + "/") + dependency.getRelativeDownloadString());

      final HttpURLConnection connection = (HttpURLConnection) tempUrl.openConnection();
      connection.setInstanceFollowRedirects(false);
      connection.setRequestProperty(
          MavenDependencyLoader.REQUEST_USER_AGENT.getKey(),
          MavenDependencyLoader.REQUEST_USER_AGENT.getValue()
      );
      connection.setRequestMethod("HEAD");

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
          || connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
        downloadUrl = Optional.of(new SimpleImmutableEntry<>(repo.getUrl(), tempUrl));
        break;
      }
    }

    return downloadUrl;
  }

  @Override
  public void downloadDependencies() {
    super.getDependencies()
        .parallelStream()
        .forEach(dependency -> {
          if (dependency.isLoaded()) {
            return;
          }

          final Path downloadLocation = super.getBasePath()
              .resolve(dependency.getDownloadedFileName());

          if (!Files.exists(downloadLocation)
              && !Files.exists(super.getBasePath().resolve(dependency.getRelocatedFileName()))) {

            try {
              final Optional<SimpleImmutableEntry<String, URL>> downloadUrl =
                  this.findRepoForDependency(dependency);

              if (!downloadUrl.isPresent()) {
                throw new InvalidDependencyException(String.format(
                    "Couldn't download dependency: '%s'.",
                    dependency.getName()
                ));
              }

              try (final InputStream is = downloadUrl.get().getValue().openStream()) {
                Files.createDirectories(downloadLocation.getParent());
                Files.deleteIfExists(downloadLocation);
                Files.copy(is, downloadLocation);
              }
            } catch (final IOException ex) {
              super.addError(new DependencyLoadException(dependency, ex));
            }
          }
        });
  }

  private void relocateDependency(
      final @NotNull Relocator relocator,
      final @NotNull MavenDependencyInfo dependency
  ) {
    try {
      final Path relocatedLocation = super.getBasePath().resolve(dependency.getRelocatedFileName());

      if (!Files.exists(relocatedLocation)) {
        relocator.relocate(this.relocations, dependency);

        Files.delete(super.getBasePath().resolve(dependency.getDownloadedFileName()));
      }
    } catch (final DependencyLoadException ex) {
      super.addError(ex);
    } catch (final Exception ex) {
      super.addError(new DependencyLoadException(dependency, ex));
    }
  }

  @Override
  public void relocateDependencies() {
    try {
      final Relocator relocator = new Relocator(super.getBasePath());

      super.getDependencies()
          .parallelStream()
          .forEach(dependency -> {
            if (dependency.isLoaded()) {
              return;
            }
            this.relocateDependency(relocator, dependency);
          });
    } catch (final DependencyLoadException ex) {
      super.addError(ex);
    }
  }

  @Override
  public void loadDependencies(final @NotNull URLClassLoader classLoader) {
    try {

      final URLClassLoaderAccess urlInjector = URLClassLoaderAccess.create(classLoader);

      super.getDependencies()
          .parallelStream()
          .forEach(dependency -> {
            if (dependency.isLoaded()) {
              return;
            }

            try {
              final Path downloadLocation = super.getBasePath()
                  .resolve(dependency.getDownloadedFileName());
              final Path relocatedLocation = super.getBasePath()
                  .resolve(dependency.getRelocatedFileName());

              if (Files.exists(relocatedLocation)) {
                urlInjector.addURL(relocatedLocation.toUri().toURL());
              } else {
                urlInjector.addURL(downloadLocation.toUri().toURL());
              }

              dependency.setLoaded(true);

            } catch (final MalformedURLException ex) {
              super.addError(new DependencyLoadException(dependency, ex));
            }
          });
    } catch (final Exception ex) {
      super.addError(ex);
    }
  }

  @Override
  public @NotNull Class<@NotNull MavenDependencyInfo> getGenericType() {
    return MavenDependencyInfo.class;
  }
}
