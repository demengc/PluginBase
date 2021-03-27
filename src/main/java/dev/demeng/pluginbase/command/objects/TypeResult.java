package dev.demeng.pluginbase.command.objects;

/**
 * Essentially a pair of objects- a command argument's resolved/mapped value, and the argument
 * argument name. Primarily for internal use.
 */
public final class TypeResult {

  private final Object resolvedValue;
  private final String argumentName;

  /**
   * Creates a new type result that has already been resolved..
   *
   * @param resolvedValue The resolved value
   * @param argumentName The argument name
   */
  public TypeResult(Object resolvedValue, final Object argumentName) {
    this.resolvedValue = resolvedValue;
    this.argumentName = String.valueOf(argumentName);
  }

  /**
   * Creates a new type result with a null resolve value.
   *
   * @param argumentName The argument name
   */
  public TypeResult(final Object argumentName) {
    this(null, argumentName);
  }

  /**
   * Gets the resolved value.
   *
   * @return The resolved value
   */
  public Object getResolvedValue() {
    return resolvedValue;
  }

  /**
   * Gets the argument name.
   *
   * @return The argument name
   */
  public String getArgumentName() {
    return argumentName;
  }
}
