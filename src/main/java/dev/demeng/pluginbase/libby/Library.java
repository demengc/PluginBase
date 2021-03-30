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

package dev.demeng.pluginbase.libby;

import dev.demeng.pluginbase.libby.relocation.Relocation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

/**
 * An immutable representation of a Maven artifact that can be downloaded, relocated and then loaded
 * into a plugin's classpath at runtime.
 */
@Builder
public class Library {

  @Getter private final Collection<String> urls;
  @Getter private final String groupId;
  @Getter private final String artifactId;
  @Getter private final String version;
  @Getter private final String classifier;
  @Getter private final byte[] checksum;
  @Getter private final Collection<Relocation> relocations;
  @Getter private final String path;
  @Getter private final String relocatedPath;

  /**
   * Creates a new library.
   *
   * @param urls        Direct download URLs
   * @param groupId     Maven group ID
   * @param artifactId  Maven artifact ID
   * @param version     Artifact version
   * @param classifier  Artifact classifier or null
   * @param checksum    Binary SHA-256 checksum or null
   * @param relocations Jar relocations or null
   */
  private Library(
      Collection<String> urls,
      String groupId,
      String artifactId,
      String version,
      String classifier,
      byte[] checksum,
      Collection<Relocation> relocations) {

    this.urls =
        urls != null
            ? Collections.unmodifiableList(new LinkedList<>(urls))
            : Collections.emptyList();
    this.groupId = Objects.requireNonNull(groupId, "groupId").replace("{}", ".");
    this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
    this.version = Objects.requireNonNull(version, "version");
    this.classifier = classifier;
    this.checksum = checksum;
    this.relocations =
        relocations != null
            ? Collections.unmodifiableList(new LinkedList<>(relocations))
            : Collections.emptyList();

    String libPath =
        this.groupId.replace('.', '/')
            + '/'
            + artifactId
            + '/'
            + version
            + '/'
            + artifactId
            + '-'
            + version;
    if (hasClassifier()) {
      libPath += '-' + classifier;
    }

    this.path = libPath + ".jar";
    relocatedPath = hasRelocations() ? libPath + "-relocated.jar" : null;
  }

  /**
   * Gets whether this library has an artifact classifier.
   *
   * @return True if library has classifier, false otherwise
   */
  public boolean hasClassifier() {
    return classifier != null;
  }

  /**
   * Gets whether this library has a checksum.
   *
   * @return True if library has checksum, false otherwise
   */
  public boolean hasChecksum() {
    return checksum != null;
  }

  /**
   * Gets whether this library has any jar relocations.
   *
   * @return True if library has relocations, false otherwise
   */
  public boolean hasRelocations() {
    return !relocations.isEmpty();
  }

  /**
   * Gets a concise, human-readable string representation of this library.
   *
   * @return String representation
   */
  @Override
  public String toString() {
    String name = groupId + ':' + artifactId + ':' + version;
    if (hasClassifier()) {
      name += ':' + classifier;
    }

    return name;
  }
}
