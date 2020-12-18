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
   * @return True if the class exists, false otherwise
   */
  public static boolean checkClass(String className) {

    try {
      Class.forName(className);
    } catch (ClassNotFoundException ex) {
      return false;
    }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------
  // NUMBER CHECK
  // -----------------------------------------------------------------------------------------------------

  /**
   * Checks if the provided string is an integer.
   *
   * @param str The string to check
   * @return True if the value is an integer, false otherwise
   */
  public static boolean checkInt(String str) {
    Objects.requireNonNull(str, "String to check is null");

    try {
      Integer.parseInt(str);
    } catch (NumberFormatException ex) {
      return false;
    }

    return true;
  }

  /**
   * Checks if the provided string is a long.
   *
   * @param str The string to check
   * @return True if the value is a long, false otherwise
   */
  public static boolean checkLong(String str) {
    Objects.requireNonNull(str, "String to check is null");

    try {
      Long.parseLong(str);
    } catch (NumberFormatException ex) {
      return false;
    }

    return true;
  }

  /**
   * Checks if the provided string is a double.
   *
   * @param str The string to check
   * @return True if the value is a double, false otherwise
   */
  public static boolean checkDouble(String str) {
    Objects.requireNonNull(str, "String to check is null");

    try {
      Double.parseDouble(str);
    } catch (NumberFormatException ex) {
      return false;
    }

    return true;
  }
}
