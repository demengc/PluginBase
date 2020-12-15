package dev.demeng.pluginbase.libloader;

import java.util.Objects;

/**
 * Represents a relocation rule.
 *
 * <p>Credit: https://github.com/ReflxctionDev/PluginLib
 */
public final class Relocation {

  private final String pattern;
  private final String shadedPattern;

  /**
   * Creates a new relocation pattern. To prevent your build tool from messing this up, use / or #
   * instead of a period.
   *
   * @param pattern The original package
   * @param shadedPattern The new package
   */
  public Relocation(String pattern, String shadedPattern) {
    this.pattern = pattern.replace('/', '.').replace('#', '.');
    this.shadedPattern = shadedPattern.replace('/', '.').replace('#', '.');
  }

  /**
   * The original package. Note that the separator is now a period, not / or #.
   *
   * @return The original package
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * The new package. Note that the separator is now a period, not / or #.
   *
   * @return The new package
   */
  public String getShadedPattern() {
    return shadedPattern;
  }

  @Override
  public String toString() {
    return String.format("Relocation{path='%s', newPath='%s'}", pattern, shadedPattern);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Relocation)) {
      return false;
    }

    final Relocation that = (Relocation) o;

    return Objects.equals(pattern, that.pattern)
        && Objects.equals(shadedPattern, that.shadedPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, shadedPattern);
  }
}
