package dev.demeng.pluginbase.command.exceptions;

/** A custom command exception, thrown mostly due to annotation or method parameter errors. */
public class CustomCommandException extends RuntimeException {
  public CustomCommandException(String message) {
    super(message);
  }
}
