package dev.demeng.pluginbase.libby;

import dev.demeng.pluginbase.libby.relocation.Relocation;

import java.util.*;

/**
 * An immutable representation of a Maven artifact that can be downloaded, relocated and then loaded
 * into a plugin's classpath at runtime.
 */
public class Library {

  private final Collection<String> urls;
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String classifier;
  private final byte[] checksum;
  private final Collection<Relocation> relocations;
  private final String path;
  private final String relocatedPath;

  /**
   * Creates a new library.
   *
   * @param urls Direct download URLs
   * @param groupId Maven group ID
   * @param artifactId Maven artifact ID
   * @param version Artifact version
   * @param classifier Artifact classifier or null
   * @param checksum Binary SHA-256 checksum or null
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

    String path =
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
      path += '-' + classifier;
    }

    this.path = path + ".jar";
    relocatedPath = hasRelocations() ? path + "-relocated.jar" : null;
  }

  /**
   * Gets the direct download URLs for this library.
   *
   * @return Direct download URLs
   */
  public Collection<String> getUrls() {
    return urls;
  }

  /**
   * Gets the Maven group ID for this library.
   *
   * @return Maven group ID
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Gets the Maven artifact ID for this library.
   *
   * @return Maven artifact ID
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * Gets the artifact version for this library.
   *
   * @return Artifact version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Gets the artifact classifier for this library.
   *
   * @return Artifact classifier or null
   */
  public String getClassifier() {
    return classifier;
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
   * Gets the binary SHA-256 checksum of this library's jar file.
   *
   * @return Checksum or null
   */
  public byte[] getChecksum() {
    return checksum;
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
   * Gets the jar relocations to apply to this library.
   *
   * @return Jar relocations to apply
   */
  public Collection<Relocation> getRelocations() {
    return relocations;
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
   * Gets the relative Maven path to this library's artifact.
   *
   * @return Maven path for this library
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the relative path to this library's relocated jar.
   *
   * @return Path to relocated artifact or null if has no relocations
   */
  public String getRelocatedPath() {
    return relocatedPath;
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

  /**
   * Creates a new library builder.
   *
   * @return New library builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Due to the constructor complexity of an immutable {@link Library}, instead this fluent builder
   * is used to configure and then construct a new library.
   */
  public static class Builder {

    private final Collection<String> urls = new LinkedList<>();
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private byte[] checksum;
    private final Collection<Relocation> relocations = new LinkedList<>();

    public Builder url(String url) {
      urls.add(Objects.requireNonNull(url, "url"));
      return this;
    }

    public Builder groupId(String groupId) {
      this.groupId = Objects.requireNonNull(groupId, "groupId");
      return this;
    }

    public Builder artifactId(String artifactId) {
      this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
      return this;
    }

    public Builder version(String version) {
      this.version = Objects.requireNonNull(version, "version");
      return this;
    }

    public Builder classifier(String classifier) {
      this.classifier = Objects.requireNonNull(classifier, "classifier");
      return this;
    }

    public Builder checksum(byte[] checksum) {
      this.checksum = Objects.requireNonNull(checksum, "checksum");
      return this;
    }

    public Builder checksum(String checksum) {
      return checksum(Base64.getDecoder().decode(Objects.requireNonNull(checksum, "checksum")));
    }

    public Builder relocate(Relocation relocation) {
      relocations.add(Objects.requireNonNull(relocation, "relocation"));
      return this;
    }

    public Builder relocate(String pattern, String relocatedPattern) {
      return relocate(new Relocation(pattern, relocatedPattern));
    }

    public Library build() {
      return new Library(urls, groupId, artifactId, version, classifier, checksum, relocations);
    }
  }
}
