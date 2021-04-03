/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.chat;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.plugin.BaseLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Message-related utilities, including console and chat messages.
 */
@SuppressWarnings("unused")
public class ChatUtils {

  private ChatUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Pattern to amtch our HEX color format for MC 1.16+.
   */
  public static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");

  /**
   * Separation line for players (in-game chat).
   */
  public static final String CHAT_LINE = "&m-----------------------------------------------------";

  /**
   * Separation line for console.
   */
  public static final String CONSOLE_LINE =
      "*-----------------------------------------------------*";

  // ---------------------------------------------------------------------------------
  // FORMATTING
  // ---------------------------------------------------------------------------------

  /**
   * Convenience method to get the current prefix of the plugin.
   *
   * @return The prefix
   */
  @NotNull
  public static String getPrefix() {
    return BaseLoader.getPlugin().getBaseSettings().prefix();
  }

  /**
   * Convert plain string(s) with color codes into a colorized message. Supports HEX colors in the
   * format of {@code <#HEX>}. 1.16+ HEX support requires the server software to be Spigot, or a
   * fork of Spigot.
   *
   * @param strings The plain string(s)
   * @return Colorized strings separated by new lines, or empty if the provided strings are null
   */
  @NotNull
  public static String colorize(String... strings) {

    if (strings == null) {
      return "";
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
  @NotNull
  public static List<String> colorize(List<String> strList) {

    if (strList == null) {
      return Collections.emptyList();
    }

    return strList.stream().map(ChatUtils::colorize).collect(Collectors.toList());
  }

  /**
   * Appends the prefix, and then colorizes.
   *
   * @param strings The plain, non-prefixed string(s)
   * @return Colorized strings separated by new lines, or empty if the provided strings are null
   */
  @NotNull
  public static String format(String... strings) {

    if (strings == null) {
      return "";
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
   * Applies the specified placeholder to the entire list of strings.
   *
   * @param strList     The list that will have its strings replaced
   * @param toReplace   The string to replace
   * @param replaceWith The string that matches will be replaced with
   * @return The replaced list, or an empty list if the provided one is null
   */
  @NotNull
  public static List<String> replace(List<String> strList, String toReplace, String replaceWith) {

    if (strList == null) {
      return Collections.emptyList();
    }

    if (toReplace == null || replaceWith == null) {
      return strList;
    }

    return strList.stream()
        .map(str -> str.replace(toReplace, replaceWith))
        .collect(Collectors.toList());
  }

  /**
   * Applies the map of placeholders to the entire list of strings.
   *
   * @param strList      The list that will have its strings replaced
   * @param placeholders The map of placeholders, with the string to replace as the key and the
   *                     string matches will be replaced with as the value
   * @return The replaced list, or an empty list if the provided one is null
   */
  @NotNull
  public static List<String> replace(List<String> strList, Map<String, String> placeholders) {

    if (strList == null) {
      return Collections.emptyList();
    }

    if (placeholders == null || placeholders.isEmpty()) {
      return strList;
    }

    Stream<String> stream = strList.stream();

    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      stream = stream.map(str -> str.replace(entry.getKey(), entry.getValue()));
    }

    return stream.collect(Collectors.toList());
  }

  /**
   * Fully strip all color from the string.
   *
   * @param str The string to strip
   * @return The stripped string, or an empty string if the provided one is null
   */
  @NotNull
  public static String strip(String str) {

    if (str == null) {
      return "";
    }

    return ChatColor.stripColor(colorize(str));
  }

  /**
   * Fully strip all color from strings.
   *
   * @return The stripped string list, or an empty list if the provided one is null
   * @see #strip(String)
   */
  @NotNull
  public static List<String> strip(List<String> strList) {

    if (strList == null) {
      return Collections.emptyList();
    }

    return strList.stream().map(ChatUtils::strip).collect(Collectors.toList());
  }

  // ---------------------------------------------------------------------------------
  // CONSOLE MESSAGES
  // ---------------------------------------------------------------------------------

  /**
   * Send formatted console messages. Any message equaling "none" will be ignored.
   *
   * @param strings The messages to send
   */
  public static void console(String... strings) {

    if (strings == null) {
      return;
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

    if (strings == null) {
      return;
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

    if (strings == null) {
      return;
    }

    for (String s : strings) {
      BaseLoader.getPlugin().getLogger().info(s);
    }
  }

  /**
   * Log plain messages into the console.
   *
   * @param strings The messages to send
   * @param level   The logging level
   */
  public static void log(Level level, String... strings) {

    if (strings == null) {
      return;
    }

    for (String s : strings) {
      BaseLoader.getPlugin().getLogger().log(level, s);
    }
  }

  // ---------------------------------------------------------------------------------
  // PLAYER MESSAGES
  // ---------------------------------------------------------------------------------

  /**
   * Sends a colored and prefixed message to the command sender.
   *
   * @param sender The command sender that will receive the message
   * @param lines  The lines to send
   */
  public static void tell(@NotNull CommandSender sender, String... lines) {
    if (lines != null) {
      sender.sendMessage(format(lines));
    }
  }

  /**
   * Does the same thing as {@link #tell(CommandSender, String...)}, but without the prefix.
   *
   * @param sender The command sender that will receive the message
   * @param lines  The lines to send
   */
  public static void tellColored(@NotNull CommandSender sender, String... lines) {
    if (lines != null) {
      sender.sendMessage(colorize(lines));
    }
  }

  /**
   * Sends a colored and centered message. May not work if the player has changed their chat size,
   * used a custom font (resource pack), or if the message contains HEX colors.
   *
   * @param player The player that will receive the message
   * @param lines  The lines to send
   */
  public static void tellCentered(@NotNull Player player, String... lines) {

    if (lines == null) {
      return;
    }

    for (String line : lines) {

      if (line == null || line.equals("")) {
        player.sendMessage("");
        continue;
      }

      line = ChatColor.translateAlternateColorCodes('&', line);

      int messagePxSize = 0;
      boolean previousCode = false;
      boolean isBold = false;

      for (char c : line.toCharArray()) {

        if (c == ChatColor.COLOR_CHAR) {
          previousCode = true;

        } else if (previousCode) {
          previousCode = false;
          isBold = c == 'l' || c == 'L';

        } else {
          final DefaultFontInfo dfi = DefaultFontInfo.getDefaultFontInfo(c);
          messagePxSize += isBold ? dfi.getBoldLength() : dfi.getLength();
          messagePxSize++;
        }
      }

      final int halvedMessageSize = messagePxSize / 2;
      final int toCompensate = 154 - halvedMessageSize;
      final int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
      int compensated = 0;

      final StringBuilder sb = new StringBuilder();

      while (compensated < toCompensate) {
        sb.append(" ");
        compensated += spaceLength;
      }

      player.sendMessage(sb.toString() + line);
    }
  }

  /**
   * Broadcasts the message after coloring and formating them.
   *
   * @param permission The permission players must have in order to see this broadcast, or null if
   *                   the broadcast should be seen by everyone
   * @param lines      The lines to send
   */
  public static void broadcast(String permission, String... lines) {

    if (lines == null) {
      return;
    }

    if (permission == null) {
      Bukkit.broadcastMessage(format(lines));
      return;
    }

    Bukkit.broadcast(format(lines), permission);
  }

  /**
   * Same thing as {@link #broadcast(String, String...)}, but without the prefix.
   *
   * @see #broadcast(String, String...)
   */
  public static void broadcastColored(String permission, String... lines) {

    if (lines == null) {
      return;
    }

    if (permission == null) {
      Bukkit.broadcastMessage(colorize(lines));
      return;
    }

    Bukkit.broadcast(colorize(lines), permission);
  }
}
