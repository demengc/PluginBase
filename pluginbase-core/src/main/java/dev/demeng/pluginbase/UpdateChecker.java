package dev.demeng.pluginbase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * Simple utility for checking for resource updates on SpigotMC.
 */
@SuppressWarnings("unused")
public class UpdateChecker {

  private UpdateChecker() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Gets the latest version of the resource with the specified resource ID.
   *
   * @param resourceId The ID of the resource to check
   * @return The latest version string, or null if failed to retrieve version
   */
  public static String getLatestVersion(final int resourceId) {

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
   * Checks if this plugin version is the same as the one with the specified resource ID.
   *
   * @param resourceId The ID of the resource to check
   * @return The update check result
   */
  public static UpdateChecker.Result isUpToDate(final int resourceId) {

    final String latest = getLatestVersion(resourceId);

    if (latest == null) {
      return UpdateChecker.Result.ERROR;
    }

    if (Common.getVersion().equals(latest)) {
      return UpdateChecker.Result.UP_TO_DATE;
    }

    return UpdateChecker.Result.OUTDATED;
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
