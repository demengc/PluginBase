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

package dev.demeng.pluginbase.dependencyloader.dependency.builder;

import dev.demeng.pluginbase.dependencyloader.dependency.DependencyLoader;
import dev.demeng.pluginbase.dependencyloader.dependency.MavenDependencyInfo;
import dev.demeng.pluginbase.dependencyloader.dependency.MavenDependencyLoader;
import dev.demeng.pluginbase.dependencyloader.dependency.MavenRepositoryInfo;
import dev.demeng.pluginbase.dependencyloader.relocation.RelocationInfo;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Used to build a dependency provider that should be attached to a {@link MavenDependencyLoader}
 * then loaded.
 */
public final class MavenDependencyProviderBuilder implements
    DependencyBuilder<MavenDependencyInfo> {

  /**
   * The set of repositories to add to the provider.
   */
  private final @NotNull Set<MavenRepositoryInfo> repositories;

  /**
   * The set of Maven dependencies to add to the provider.
   */
  private final @NotNull Set<MavenDependencyInfo> dependencies;

  /**
   * The set of relocations to add to the provider.
   */
  private final @NotNull Set<RelocationInfo> relocations;

  /**
   * Creates a new dependency builder.
   */
  public MavenDependencyProviderBuilder() {
    this.repositories = new HashSet<>();
    this.dependencies = new HashSet<>();
    this.relocations = new HashSet<>();
  }

  @Contract("_ -> this")
  @Override
  public @NotNull MavenDependencyProviderBuilder dependency(
      final @NotNull MavenDependencyInfo dependency
  ) {
    this.dependencies.add(dependency);
    return this;
  }

  /**
   * Creates a new Maven dependency provider builder.
   *
   * @param groupId    The group ID of the Maven dependency
   * @param artifactId the artifact ID of the Maven dependency
   * @param version    the version of the Maven dependency
   * @return this
   */
  @Contract("_,_,_ -> this")
  public @NotNull MavenDependencyProviderBuilder dependency(
      final @NotNull String groupId,
      final @NotNull String artifactId,
      final @NotNull String version
  ) {
    return this.dependency(MavenDependencyInfo.of(
        DependencyLoader.DEFAULT_SEPARATOR,
        groupId,
        artifactId,
        version
    ));
  }

  /**
   * Creates a new Maven dependency provider builder.
   *
   * @param separator  The separator used for package names
   * @param groupId    The group ID of the Maven dependency
   * @param artifactId the artifact ID of the Maven dependency
   * @param version    the version of the Maven dependency
   * @return this
   */
  @Contract("_,_,_,_ -> this")
  public @NotNull MavenDependencyProviderBuilder dependency(
      final @NotNull String separator,
      final @NotNull String groupId,
      final @NotNull String artifactId,
      final @NotNull String version
  ) {
    return this.dependency(MavenDependencyInfo.of(separator, groupId, artifactId, version));
  }

  /**
   * Creates a new Maven dependency provider builder.
   *
   * @param separator            the separator used for package separation
   * @param singleLineDependency The Gradle style single line dependency string
   * @return this
   */
  @Contract("_,_ -> this")
  public @NotNull MavenDependencyProviderBuilder dependency(
      final @NotNull String separator,
      final @NotNull String singleLineDependency
  ) {
    return this.dependency(MavenDependencyInfo.of(separator, singleLineDependency));
  }

  /**
   * Creates a new Maven dependency provider builder.
   *
   * @param singleLineDependency The Gradle style single line dependency string
   * @return this
   */
  @Contract("_ -> this")
  public @NotNull MavenDependencyProviderBuilder dependency(
      final @NotNull String singleLineDependency
  ) {
    return this.dependency(
        MavenDependencyInfo.of(DependencyLoader.DEFAULT_SEPARATOR, singleLineDependency));
  }

  /**
   * Creates a new Maven dependency provider builder.
   *
   * @param repository The repository.
   * @return The same instance.
   * @see MavenRepositoryInfo
   */
  @Contract("_ -> this")
  public MavenDependencyProviderBuilder repository(final @NotNull MavenRepositoryInfo repository) {
    this.repositories.add(repository);
    return this;
  }

  /**
   * Sets the repository URL.
   *
   * @param url String URL representation of a Maven repository.
   * @return The same instance.
   */
  @Contract("_ -> this")
  public MavenDependencyProviderBuilder repository(final @NotNull String url) {
    return this.repository(MavenRepositoryInfo.of(url));
  }

  /**
   * Sets the repository URL.
   *
   * @param url URL representation of a Maven repository.
   * @return The same instance.
   */
  @Contract("_ -> this")
  public MavenDependencyProviderBuilder repository(final @NotNull URL url) {
    return this.repository(MavenRepositoryInfo.of(url));
  }

  /**
   * Sets the relocation.
   *
   * @param relocation The relocation.
   * @return The same instance.
   * @see RelocationInfo
   */
  @Contract("_ -> this")
  public MavenDependencyProviderBuilder relocation(final @NotNull RelocationInfo relocation) {
    this.relocations.add(relocation);
    return this;
  }

  /**
   * Sets the relocation.
   *
   * @param from      Original package destination name.
   * @param to        Target package destination name.
   * @param separator The separator to use instead of '.' or '/'.
   * @return The same instance.
   */
  @Contract("_,_,_ -> this")
  public MavenDependencyProviderBuilder relocation(
      final @NotNull String from,
      final @NotNull String to,
      final @NotNull String separator
  ) {
    return this.relocation(RelocationInfo.of(from, to, separator));
  }

  /**
   * Sets the relocation.
   *
   * @param from the package to relocate from
   * @param to   the package to relocate to
   * @return this
   */
  @Contract("_,_ -> this")
  public MavenDependencyProviderBuilder relocation(
      final @NotNull String from,
      final @NotNull String to
  ) {
    return this.relocation(RelocationInfo.of(from, to, DependencyLoader.DEFAULT_SEPARATOR));
  }

  @Contract("-> new")
  @Override
  public @NotNull DependencyProvider<MavenDependencyInfo> build() {
    return new MavenDependencyProvider(this.repositories, this.dependencies, this.relocations);
  }
}
