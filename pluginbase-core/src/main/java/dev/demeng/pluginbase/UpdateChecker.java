/*
 * MIT License
 *
 * Copyright (c) 2024 Demeng Chen
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

import dev.demeng.pluginbase.text.Text;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * Simple utility for checking for resource updates on SpigotMC.
 */
public class UpdateChecker {

  @Getter private final int resourceId;
  @Getter private final String latestVersion;
  @Getter private final UpdateChecker.Result result;

  /**
   * Creates a new update checker instance and caches the latest version from Spigot. Should be
   * called asynchronously.
   *
   * @param resourceId The resource ID on SpigotMC
   */
  public UpdateChecker(final int resourceId) {
    this.resourceId = resourceId;
    this.latestVersion = retrieveVersionFromSpigot();

    if (latestVersion == null) {
      this.result = UpdateChecker.Result.ERROR;
    } else if (Common.getVersion().equals(latestVersion)) {
      this.result = UpdateChecker.Result.UP_TO_DATE;
    } else {
      this.result = UpdateChecker.Result.OUTDATED;
    }
  }

  /**
   * Notifies the console or a player about a new update. Calling this method if the
   * {@link #getResult()} is anything other than {@link UpdateChecker.Result#OUTDATED} will do
   * nothing.
   *
   * @param sender The console or player to notify, defaults to console if null
   */
  public void notifyResult(@Nullable final CommandSender sender) {

    if (getResult() != Result.OUTDATED) {
      return;
    }

    final CommandSender cs = Common.getOrDefault(sender, Bukkit.getConsoleSender());
    final String line = cs instanceof ConsoleCommandSender
        ? Text.CONSOLE_LINE : Text.CHAT_LINE;

    Text.coloredTell(cs, "&2" + line);
    Text.coloredTell(cs, "&aA new update for " + Common.getName() + " is available!");
    Text.coloredTell(cs, "&aYour version: &f" + Common.getVersion());
    Text.coloredTell(cs, "&aLatest version: &f" + latestVersion);
    Text.coloredTell(cs, "&aDownload: &fhttps://spigotmc.org/resources/" + resourceId);
    Text.coloredTell(cs, "&2" + line);
  }

  private String retrieveVersionFromSpigot() {

    try (final InputStream inputStream = new URL(
        "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream();
        final Scanner scanner = new Scanner(inputStream)) {

      if (scanner.hasNext()) {
        return scanner.next();
      }

    } catch (final IOException ex) {
      return null;
    }

    return null;
  }

  /**
   * The result of an update check.
   */
  public enum Result {
    /**
     * The plugin version matches the one on the resource.
     */
    UP_TO_DATE,
    /**
     * The plugin version does not match the one on the resource.
     */
    OUTDATED,
    /**
     * There was an error whilst checking for updates.
     */
    ERROR
  }
}
