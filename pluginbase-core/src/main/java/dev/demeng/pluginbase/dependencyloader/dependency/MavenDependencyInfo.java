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

package dev.demeng.pluginbase.dependencyloader.dependency;

import dev.demeng.pluginbase.dependencyloader.annotations.MavenDependency;
import dev.demeng.pluginbase.dependencyloader.exceptions.InvalidDependencyException;
import dev.demeng.pluginbase.dependencyloader.relocation.RelocatableDependency;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is the backend class for {@link MavenDependency} dependencies. It's responsible for
 * providing basic information needed to download and relocate a dependency jar.
 *
 * @see MavenDependency
 */
public final class MavenDependencyInfo implements RelocatableDependency {

  /**
   * This is used to validate a single line dependency. A single line dependency should have a size
   * of 3 always.
   */
  private static final int DEPENDENCY_SPLIT_SIZE = 3;

  /**
   * This is used to make sure a dependency string is valid before we try to do extra processing on
   * it.
   */
  private static final @NotNull Pattern DEPENDENCY_PATTERN = Pattern.compile("^([\\w.\\-])+$");

  /**
   * The group ID of this Maven dependency.
   */
  @Getter private @Nullable String groupId;

  /**
   * The artifact ID of this Maven dependency.
   */
  @Getter private @Nullable String artifactId;

  /**
   * The version of this Maven dependency.
   */
  @Getter private @Nullable String version;

  /**
   * Whether or not this dependency has been loaded.
   */
  @Getter @Setter private boolean loaded;

  private MavenDependencyInfo(
      final @NotNull String separator,
      final @NotNull String groupId,
      final @NotNull String artifactId,
      final @NotNull String version
  ) {
    this.validateAndSetValues(separator, groupId, artifactId, version);
  }

  private MavenDependencyInfo(
      final @NotNull String separator,
      final @NotNull String singleLineDependency
  ) {
    if (singleLineDependency.isEmpty()) {
      throw new InvalidDependencyException(String.format(
          "Invalid single line dependency passed. dependency = '%s'.",
          singleLineDependency
      ));
    }

    final List<String> values = new ArrayList<>(MavenDependencyInfo.DEPENDENCY_SPLIT_SIZE);
    values.addAll(Arrays.asList(singleLineDependency.split(":")));

    if (values.size() != MavenDependencyInfo.DEPENDENCY_SPLIT_SIZE
        || values.get(0) == null
        || values.get(1) == null
        || values.get(2) == null) {
      throw new InvalidDependencyException(String.format(
          "Invalid dependency format '%s'",
          singleLineDependency
      ));
    }

    this.validateAndSetValues(separator, values.get(0), values.get(1), values.get(2));
  }

  private static boolean hasInvalidCharacters(final @NotNull String validate) {
    return validate.contains(".") || validate.contains("/");
  }

  private static boolean patternMismatchString(final @NotNull String validate) {
    return !DEPENDENCY_PATTERN.matcher(validate).matches();
  }

  /**
   * Creates a new {@link MavenDependencyInfo} based off the passed in information.
   *
   * @param separator  The separator to use instead of '.' or '/'.
   * @param groupId    The group ID of the dependency.
   * @param artifactId The artifact ID of the dependency.
   * @param version    The version of the dependency.
   * @return A {@link MavenDependencyInfo} representing the passed in arguments.
   */
  @Contract("_,_,_,_ -> new")
  public static @NotNull MavenDependencyInfo of(
      final @NotNull String separator,
      final @NotNull String groupId,
      final @NotNull String artifactId,
      final @NotNull String version
  ) {
    return new MavenDependencyInfo(separator, groupId, artifactId, version);
  }

  /**
   * Create a new dependency information object.
   *
   * @param separator            The separator to use instead of '.' or '/'.
   * @param singleLineDependency A Gradle style single line dependency.
   * @return A {@link MavenDependencyInfo} representing the passed in arguments.
   */
  @Contract("_,_ -> new")
  public static @NotNull MavenDependencyInfo of(
      final @NotNull String separator,
      final @NotNull String singleLineDependency
  ) {
    return new MavenDependencyInfo(separator, singleLineDependency);
  }

  /**
   * Creates a new {@link MavenDependencyInfo} based off of an {@link MavenDependency} annotation.
   *
   * @param dependency The {@link MavenDependency} annotation.
   * @return A {@link MavenDependencyInfo} representing the passed in arguments.
   */
  @Contract("_ -> new")
  public static @NotNull MavenDependencyInfo of(
      final @NotNull MavenDependency dependency
  ) {
    return dependency.value().isEmpty()
        ? MavenDependencyInfo.of(
        dependency.separator(),
        dependency.groupId(),
        dependency.artifactId(),
        dependency.version()
    )
        : MavenDependencyInfo.of(
            dependency.separator(),
            dependency.value()
        );
  }

  private void validateAndSetValues(
      final @NotNull String separator,
      final @NotNull String groupId,
      final @NotNull String artifactId,
      final @NotNull String version
  ) {
    if (separator.isEmpty() || MavenDependencyInfo.hasInvalidCharacters(separator)) {
      throw new InvalidDependencyException(String.format(
          "Separator '%s' cannot contain '.' or '/'",
          separator
      ));
    }

    if (groupId.isEmpty() || MavenDependencyInfo.hasInvalidCharacters(groupId)) {
      throw new InvalidDependencyException(String.format(
          "Group ID '%s' cannot contain '.' or '/', use a separator instead",
          groupId
      ));
    }

    if (artifactId.isEmpty() || MavenDependencyInfo.hasInvalidCharacters(artifactId)) {
      throw new InvalidDependencyException(String.format(
          "Artifact ID '%s' cannot contain '.' or '/', use a separator instead",
          artifactId
      ));
    }

    if (version.isEmpty()) {
      throw new InvalidDependencyException("Dependency version cannot be empty");
    }

    final String validateGroupId = groupId.replace(separator, ".");
    final String validateArtifactId = artifactId.replace(separator, ".");

    if (MavenDependencyInfo.patternMismatchString(validateGroupId)) {
      throw new InvalidDependencyException(String.format(
          "Group ID '%s' contains invalid characters",
          validateGroupId
      ));
    }

    if (MavenDependencyInfo.patternMismatchString(validateArtifactId)) {
      throw new InvalidDependencyException(String.format(
          "Artifact ID '%s' contains invalid characters",
          validateArtifactId
      ));
    }

    if (MavenDependencyInfo.patternMismatchString(version)) {
      throw new InvalidDependencyException(String.format(
          "Version '%s' contains invalid characters",
          version
      ));
    }

    this.groupId = validateGroupId;
    this.artifactId = validateArtifactId;
    this.version = version;
  }

  @Override
  public @NotNull String getManualDownloadString() {
    return this.getRelativeDownloadString();
  }

  @Override
  public @NotNull String getRelativeDownloadString() {
    return String.format(
        "%s/%s/%s/%s-%s.jar",
        Objects.requireNonNull(this.groupId).replace(".", "/"),
        Objects.requireNonNull(this.artifactId),
        Objects.requireNonNull(this.version),
        Objects.requireNonNull(this.artifactId),
        Objects.requireNonNull(this.version)
    );
  }

  @Override
  public @NotNull String getDownloadedFileName() {
    return this.getName() + ".jar";
  }

  @Override
  public @NotNull String getName() {
    return this.artifactId + "-" + this.version;
  }

  @Override
  public @NotNull String getRelocatedFileName() {
    return this.getName() + "-relocated.jar";
  }
}
