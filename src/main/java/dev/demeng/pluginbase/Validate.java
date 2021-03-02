package dev.demeng.pluginbase;

import java.util.Objects;

/** Utlity class for checking and validating objects. */
public class Validate {

  // -----------------------------------------------------------------------------------------------------
  // GENERAL
  // -----------------------------------------------------------------------------------------------------

  /**
   * Simple method to check if a class exists.
   *
   * @param className The class's package and name (Example: dev.demeng.pluginbase.Validate)
   * @return The actul class if the class exists, null otherwise
   */
  public static Class<?> checkClass(String className) {
    Objects.requireNonNull(className, "Class name to check is null");

    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

  // -----------------------------------------------------------------------------------------------------
  // NUMBER CHECK
  // -----------------------------------------------------------------------------------------------------

  /**
   * Checks if the provided string is an integer.
   *
   * @param str The string to check
   * @return The actual integer if the string is an integer, null otherwise
   */
  public static Integer checkInt(String str) {
    Objects.requireNonNull(str, "String to check is null");

    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a long.
   *
   * @param str The string to check
   * @return The actual long if the string is an long, null otherwise
   */
  public static Long checkLong(String str) {
    Objects.requireNonNull(str, "String to check is null");

    try {
      return Long.parseLong(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Checks if the provided string is a double.
   *
   * @param str The string to check
   * @return The actual double if the string is an double, null otherwise
   */
  public static Double checkDouble(String str) {
    Objects.requireNonNull(str, "String to check is null");

    try {
      return Double.parseDouble(str);
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
