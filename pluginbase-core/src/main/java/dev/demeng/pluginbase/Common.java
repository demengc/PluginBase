/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

package dev.demeng.pluginbase;

import com.cryptomorin.xseries.XMaterial;
import dev.demeng.pluginbase.exceptions.PluginErrorException;
import dev.demeng.pluginbase.plugin.BaseManager;
import dev.demeng.pluginbase.text.TextUtils;
import java.util.Arrays;
import java.util.function.IntConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Commonly used methods and utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Common {

  /**
   * If the server software the plugin is running on is Spigot or a fork of Spigot. Used internally
   * for some Spigot-only features or optimizations.
   */
  public static final boolean SPIGOT = Validate.checkClass("net.md_5.bungee.api.ChatColor") != null;

  /**
   * The error message for players when an internal error occurs.
   */
  public static final String PLAYERS_ERROR_MESSAGE = "&6An internal error has occurred in "
      + Common.getName()
      + ". Further details have been printed in console.";

  private static final String STR_DECIMAL_FORMAT = "%.2f";

  // ---------------------------------------------------------------------------------
  // PLUGIN INFORMATION
  // ---------------------------------------------------------------------------------

  /**
   * Gets the name of the plugin, as defined in plugin.yml.
   *
   * @return The name of the plugin
   */
  @NotNull
  public static String getName() {
    return BaseManager.getPlugin().getDescription().getName();
  }

  /**
   * Gets the version string, as defined in plugin.yml.
   *
   * @return The version of the plugin
   */
  @NotNull
  public static String getVersion() {
    return BaseManager.getPlugin().getDescription().getVersion();
  }

  // ---------------------------------------------------------------------------------
  // SERVER INFORMATION
  // ---------------------------------------------------------------------------------

  /**
   * Gets the server's major Minecraft version.
   *
   * <p>For example, if a server is running 1.16.4, this will return 16.
   *
   * @return The server's major Minecraft version.
   */
  public static int getServerMajorVersion() {
    return XMaterial.getVersion();
  }

  /**
   * Checks if the server's major version is at least the specified version.
   *
   * @param version The minimum major version
   * @return True if equal or greater to the provided version, false otherwise
   */
  public static boolean isServerVersionAtLeast(final int version) {
    return getServerMajorVersion() >= version;
  }

  // ---------------------------------------------------------------------------------
  // MISC
  // ---------------------------------------------------------------------------------

  /**
   * Formats a decimal into a human-friendly string that rounds the double to 2 decimal places.
   *
   * @param d THe double to format
   * @return The formatted double
   */
  @NotNull
  public static String formatDecimal(final double d) {
    return String.format(STR_DECIMAL_FORMAT, d);
  }

  /**
   * Returns the nullable value if not null, or the default value if it is null.
   *
   * @param <T>      The object type being checked and returned
   * @param nullable The nullable value
   * @param def      The default value
   * @return The nullable if not null, default otherwise
   */
  @NotNull
  public static <T> T getOrDefault(@Nullable final T nullable, @NotNull final T def) {
    return nullable != null ? nullable : def;
  }

  /**
   * Returns the nullable value if not null, or throws a runtime exception with error if it is
   * null.
   *
   * @param <T>         The object type being checked and returned
   * @param nullable    The nullable value
   * @param description The error description if the value is null
   * @param disable     If the plugin should disable if the value is null
   * @return The nullable if not null
   */
  @NotNull
  public static <T> T getOrError(
      @Nullable final T nullable,
      @NotNull final String description,
      final boolean disable
  ) {

    if (nullable == null) {
      throw new PluginErrorException(description, disable);
    }

    return nullable;
  }

  /**
   * Checks if the specified command sender has the permission node. If the permission node is null,
   * empty, or equal to "none", the method will return true.
   *
   * @param sender     The command sender to check
   * @param permission The permission to check
   * @return True if the command sender has the permission, false otherwise
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean hasPermission(
      @NotNull final CommandSender sender,
      @Nullable final String permission
  ) {
    return permission == null
        || permission.isEmpty()
        || permission.equalsIgnoreCase("none")
        || sender.hasPermission(permission);
  }

  /**
   * Reports an error in the plugin.
   *
   * @param throwable   The throwable
   * @param description A brief description of the error
   * @param disable     If the plugin should be disabled
   * @param players     Any players associated with this error, a message will be sent to them
   */
  public static void error(
      @Nullable final Throwable throwable,
      @NotNull final String description,
      final boolean disable,
      @NotNull final CommandSender... players
  ) {

    if (throwable != null) {
      throwable.printStackTrace();
    }

    TextUtils.coloredConsole("&4" + TextUtils.CONSOLE_LINE);
    TextUtils.coloredConsole("&cAn internal error has occurred in " + Common.getName() + "!");
    TextUtils.coloredConsole("&cContact the plugin author if you cannot fix this error.");
    TextUtils.coloredConsole("&cDescription: &6" + description);
    TextUtils.coloredConsole("&4" + TextUtils.CONSOLE_LINE);

    Arrays.stream(players).filter(Player.class::isInstance)
        .forEach(p -> TextUtils.coloredTell(p, PLAYERS_ERROR_MESSAGE));

    if (disable && Bukkit.getPluginManager().isPluginEnabled(BaseManager.getPlugin())) {
      Bukkit.getPluginManager().disablePlugin(BaseManager.getPlugin());
    }
  }

  /**
   * Parses a string sequence of integers and accepts a consumer for each integer. The sequence can
   * either be a single integer (1), a range of integers (1-10), or a list of integers (1,2,3).
   *
   * @param str      The integer sequence to parse
   * @param consumer The consumer to accept for each integer
   * @throws IllegalArgumentException If the sequenece is invalid
   */
  public static void forEachInt(final String str, final IntConsumer consumer)
      throws IllegalArgumentException {

    final Integer singleNum = Validate.checkInt(str);

    // Single integer.
    if (singleNum != null) {
      consumer.accept(singleNum);
      return;
    }

    // List of integers.
    final String[] listArr = str.split(",");

    if (listArr.length > 1) {
      for (final String s : listArr) {
        forEachInt(s, consumer);
      }

      return;
    }

    // Range of integers.
    final String[] rangeArr = str.split("-");

    if (rangeArr.length == 2) {
      final Integer start = Validate.checkInt(rangeArr[0]);
      final Integer end = Validate.checkInt(rangeArr[1]);

      if (start != null && end != null) {
        for (int i = start; i <= end; i++) {
          consumer.accept(i);
        }

        return;
      }
    }

    throw new IllegalArgumentException("Invalid integer sequence: " + str);
  }
}
