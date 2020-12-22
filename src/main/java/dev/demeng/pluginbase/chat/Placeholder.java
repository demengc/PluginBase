package dev.demeng.pluginbase.chat;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Represents a message placeholder. */
public class Placeholder {

  private final String toReplace;
  private final String replaced;

  /**
   * Creates a new placeholder.
   *
   * @param toReplace The string that will be replaced
   * @param replaced The string that matches will be replaced with
   */
  public Placeholder(String toReplace, String replaced) {

    Objects.requireNonNull(toReplace, "String to replace is null");
    Objects.requireNonNull(replaced, "Replaced string is null");

    this.toReplace = toReplace;
    this.replaced = replaced;
  }

  /**
   * Gets the string that will be replaced.
   *
   * @return The string that will be replaced
   */
  @NotNull
  public String getToReplace() {
    return toReplace;
  }

  /**
   * Gets the string that matches will be replaced with.
   *
   * @return The string that matches will be replaced with
   */
  @NotNull
  public String getReplaced() {
    return replaced;
  }

  /**
   * Apply the placeholder to the specified string.
   *
   * @param original The string tht placeholders will be applied to
   * @return The replaced string
   */
  @NotNull
  public String apply(String original) {
    return original.replace(toReplace, replaced);
  }
}
