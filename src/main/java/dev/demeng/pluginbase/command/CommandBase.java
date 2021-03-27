package dev.demeng.pluginbase.command;

import dev.demeng.pluginbase.command.annotations.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a custom command. Must be annotated with {@link Command} and registered using {@link
 * CommandManager#register(CommandBase)};
 */
public abstract class CommandBase {

  private String command;

  private final List<String> aliases = new ArrayList<>();
  private final Map<String, String> arguments = new HashMap<>();

  public CommandBase() {}

  public CommandBase(String command, List<String> aliases) {
    this.command = command;
    this.aliases.addAll(aliases);
  }

  /**
   * Gets the command for this command base.
   *
   * @return The command for this command base.
   */
  public String getCommand() {
    return command;
  }

  /**
   * Gets the argument used for the command.
   *
   * @param name The argument name
   * @return The argument value
   */
  public String getArgument(String name) {
    return arguments.get(name);
  }

  /**
   * Adds a new argument.
   *
   * @param name The argument name
   * @param argument The argument value
   */
  public void addArgument(String name, String argument) {
    arguments.put(name, argument);
  }

  /** Clears the stored arguments. */
  public void clearArguments() {
    arguments.clear();
  }

  /**
   * Gets the list of aliases.
   *
   * @return The aliases
   */
  public List<String> getAliases() {
    return aliases;
  }

  /**
   * Sets the list of aliases.
   *
   * @param newAliases The new list of aliases
   */
  public void setAliases(final List<String> newAliases) {
    aliases.addAll(newAliases);
  }
}
