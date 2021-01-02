package dev.demeng.pluginbase.chat;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.chat.tell.TellContents;
import dev.demeng.pluginbase.plugin.DemLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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
  @NotNull
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
   * @return The colorized string(s), separated by a new line
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
   * Applies placeholders to the specified string.
   *
   * @param str The string placeholders will be applied to
   * @param placeholders The placeholders
   * @return The replaced string
   */
  @NotNull
  public static String replace(String str, Placeholder... placeholders) {

    if (str == null) {
      return "";
    }

    if (placeholders == null || placeholders.length < 1) {
      return str;
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
  @NotNull
  public static List<String> replace(List<String> strList, Placeholder... placeholders) {

    if (strList == null) {
      return Collections.emptyList();
    }

    if (placeholders == null || placeholders.length < 1) {
      return strList;
    }

    return strList.stream().map(str -> replace(str, placeholders)).collect(Collectors.toList());
  }

  /**
   * Fully strip all color from the string.
   *
   * @param str The string to strip
   * @return The stripped string
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
   * @see #strip(String)
   */
  @NotNull
  public static List<String> strip(List<String> strList) {

    if (strList == null) {
      return Collections.emptyList();
    }

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

    if (strings == null) {
      return;
    }

    for (String s : strings) {
      DemLoader.getPlugin().getLogger().log(level, s);
    }
  }

  // -----------------------------------------------------------------------------------------------------
  // PLAYER MESSAGES
  // -----------------------------------------------------------------------------------------------------

  /**
   * Sends a colored and prefixed message to the command sender.
   *
   * @param sender The command sender that will receive the message
   * @param lines The lines to send
   */
  public static void tellMessage(CommandSender sender, String... lines) {
    Objects.requireNonNull(sender, "Sender is null");

    if (lines == null) {
      return;
    }

    sender.sendMessage(format(lines));
  }

  /**
   * Does the same thing as {@link #tellMessage(CommandSender, String...)}, but without the prefix.
   *
   * @param sender The command sender that will receive the message
   * @param lines The lines to send
   */
  public static void tellMessageColored(CommandSender sender, String... lines) {
    Objects.requireNonNull(sender, "Sender is null");

    if (lines == null) {
      return;
    }

    sender.sendMessage(colorize(lines));
  }

  public static void tell(CommandSender sender, @Language("JSON") String json) {
    Objects.requireNonNull(sender, "Sender is null");

    if (json == null) {
      return;
    }

    TellContents.fromJson(json).tell(sender);
  }

  /**
   * Sends a colored and centered message. May not work if the player has changed their chat size,
   * used a custom font (resource pack), or if the message contains HEX colors.
   *
   * @param player The player that will receive the message
   * @param lines The lines to send
   */
  public static void tellCentered(Player player, String... lines) {
    Objects.requireNonNull(player, "Player is null");

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
          DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
          messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
          messagePxSize++;
        }
      }

      int halvedMessageSize = messagePxSize / 2;
      int toCompensate = 154 - halvedMessageSize;
      int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
      int compensated = 0;

      StringBuilder sb = new StringBuilder();

      while (compensated < toCompensate) {
        sb.append(" ");
        compensated += spaceLength;
      }

      player.sendMessage(sb.toString() + line);
    }
  }
}
