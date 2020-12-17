package dev.demeng.pluginbase.chat;

import dev.demeng.pluginbase.plugin.DemLoader;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Message-related utilities, including console and chat messages. */
public class ChatUtils {

  /**
   * Convenience method to get the current prefix of the plugin.
   *
   * @return The prefix
   */
  public static String getPrefix() {
    return DemLoader.getPlugin().getBaseSettings().prefix();
  }

  /**
   * Convert plain string(s) with color codes into a colorized message.
   *
   * @param str The plain string(s)
   * @return The colorized string(s), separated by a new line
   */
  public static String colorize(String... str) {

    if (str == null || str.length < 1) {
      throw new IllegalArgumentException("No strings to colorize");
    }

    return ChatColor.translateAlternateColorCodes('&', String.join("\n", str));
  }

  /**
   * Colorizes a list of plain strings.
   *
   * @see #colorize(String...)
   */
  public static List<String> colorize(List<String> strList) {

    Objects.requireNonNull(strList, "No strings to colorize");

    return strList.stream().map(ChatUtils::colorize).collect(Collectors.toList());
  }

  /**
   * Appends the prefix, and then colorizes.
   *
   * @param str The plain, non-prefixed string(s)
   * @return The colorized string(s), separated by a new line
   */
  public static String format(String... str) {

    if (str == null || str.length < 1) {
      throw new IllegalArgumentException("No strings to format");
    }

    if (str.length == 1) {
      return colorize(getPrefix() + str[0]);
    }

    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < str.length; i++) {
      builder.append(getPrefix()).append(str[i]);

      if (i != str.length - 1) {
        builder.append("\n");
      }
    }

    return builder.toString();
  }

  /**
   * Applies placeholders to the specified string.
   *
   * @param str The string placeholders will be applied to
   * @param placeholders The placeholders
   * @return The replaced string
   */
  public static String replace(String str, Placeholder... placeholders) {

    if (str == null || placeholders == null || placeholders.length < 1) {
      throw new IllegalArgumentException("No string to replace or no placeholders to apply");
    }

    String replaced = str;

    for (Placeholder placeholder : placeholders) {
      replaced = placeholder.apply(replaced);
    }

    return replaced;
  }

  /**
   * Applies placeholders to a list of strings.
   *
   * @see #replace(String, Placeholder...)
   */
  public static List<String> replace(List<String> strList, Placeholder... placeholders) {

    if (strList == null || placeholders == null || placeholders.length < 1) {
      throw new IllegalArgumentException("No strings to replace or no placeholders to apply");
    }

    return strList.stream().map(str -> replace(str, placeholders)).collect(Collectors.toList());
  }

  /**
   * Fully strip all color from the string.
   *
   * @param str The string to strip
   * @return The stripped string
   */
  public static String strip(String str) {
    Objects.requireNonNull(str, "No string to strip");
    return ChatColor.stripColor(colorize(str));
  }

  public static List<String> strip(List<String> strList) {
    Objects.requireNonNull(strList, "No strings to strip");
    return strList.stream().map(ChatUtils::strip).collect(Collectors.toList());
  }
}
