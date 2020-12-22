package dev.demeng.pluginbase.chat;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.plugin.DemLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Message-related utilities, including console and chat messages. */
public class ChatUtils {

  /** Pattern to amtch our HEX color format for MC 1.16+. */
  public static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");

  /** Separation line for players (in-game chat). */
  public static final String CHAT_LINE = "&m-----------------------------------------------------";

  /** Separation line for console. */
  public static final String CONSOLE_LINE =
      "*-----------------------------------------------------*";

  // -----------------------------------------------------------------------------------------------------
  // FORMATTING
  // -----------------------------------------------------------------------------------------------------

  /**
   * Convenience method to get the current prefix of the plugin.
   *
   * @return The prefix
   */
  public static String getPrefix() {
    return DemLoader.getPlugin().getBaseSettings().prefix();
  }

  /**
   * Convert plain string(s) with color codes into a colorized message. Supports HEX colors in the
   * format of {@code <#HEX>}. 1.16+ HEX support requires the server software to be Spigot, or a
   * fork of Spigot.
   *
   * @param strings The plain string(s)
   * @return The colorized string(s), separated by a new line
   */
  public static String colorize(String... strings) {

    if (strings == null || strings.length < 1) {
      throw new IllegalArgumentException("No strings to colorize");
    }

    String message = String.join("\n", strings);

    if (Common.SPIGOT && Common.isServerVersionAtLeast(16)) {

      Matcher matcher = HEX_PATTERN.matcher(message);

      while (matcher.find()) {
        final net.md_5.bungee.api.ChatColor hexColor =
            net.md_5.bungee.api.ChatColor.of(
                matcher.group().substring(1, matcher.group().length() - 1));
        final String before = message.substring(0, matcher.start());
        final String after = message.substring(matcher.end());

        message = before + hexColor + after;
        matcher = HEX_PATTERN.matcher(message);
      }
    }

    return ChatColor.translateAlternateColorCodes('&', message);
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
   * @param strings The plain, non-prefixed string(s)
   * @return The colorized string(s), separated by a new line
   */
  public static String format(String... strings) {

    if (strings == null || strings.length < 1) {
      throw new IllegalArgumentException("No strings to format");
    }

    if (strings.length == 1) {
      return colorize(getPrefix() + strings[0]);
    }

    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < strings.length; i++) {
      builder.append(getPrefix()).append(strings[i]);

      if (i != strings.length - 1) {
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

  /**
   * Fully strip all color from strings.
   *
   * @see #strip(String)
   */
  public static List<String> strip(List<String> strList) {
    Objects.requireNonNull(strList, "No strings to strip");
    return strList.stream().map(ChatUtils::strip).collect(Collectors.toList());
  }

  // -----------------------------------------------------------------------------------------------------
  // CONSOLE MESSAGES
  // -----------------------------------------------------------------------------------------------------

  /**
   * Send formatted console messages. Any message equaling "none" will be ignored.
   *
   * @param strings The messages to send
   */
  public static void console(String... strings) {

    if (strings == null || strings.length < 1) {
      throw new IllegalArgumentException("No messages to send to console");
    }

    for (String s : strings) {
      if (!s.equalsIgnoreCase("none")) {
        Bukkit.getConsoleSender().sendMessage(format(s));
      }
    }
  }

  /**
   * Send colored console messages. Any message equaling "none" will be ignored.
   *
   * @param strings The messages to send
   */
  public static void coloredConsole(String... strings) {

    if (strings == null || strings.length < 1) {
      throw new IllegalArgumentException("No messages to send to console");
    }

    for (String s : strings) {
      if (!s.equalsIgnoreCase("none")) {
        Bukkit.getConsoleSender().sendMessage(colorize(s));
      }
    }
  }

  /**
   * Log plain messages into the console.
   *
   * @param strings The messages to send
   */
  public static void log(String... strings) {

    if (strings == null || strings.length < 1) {
      throw new IllegalArgumentException("No messages to log to console");
    }

    for (String s : strings) {
      DemLoader.getPlugin().getLogger().info(s);
    }
  }

  /**
   * Log plain messages into the console.
   *
   * @param strings The messages to send
   * @param level The logging level
   */
  public static void log(Level level, String... strings) {

    if (strings == null || strings.length < 1) {
      throw new IllegalArgumentException("No messages to log to console");
    }

    for (String s : strings) {
      DemLoader.getPlugin().getLogger().log(level, s);
    }
  }


  // -----------------------------------------------------------------------------------------------------
  // PLAYER MESSAGES
  // -----------------------------------------------------------------------------------------------------

  // TODO Send a JSON message supporting color codes, bossbars, and a bunch of other cool stuff.
}
