package dev.demeng.pluginbase.command.objects;

import dev.demeng.pluginbase.command.CommandBase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stores all of the relevant data for a command. Primarily for internal use. */
public final class CommandData {

  private final CommandBase commandBase;
  private final List<Class<?>> arguments = new ArrayList<>();
  private final List<String> argumentNames = new ArrayList<>();
  private final Map<Integer, String> completions = new HashMap<>();

  private String name;
  private Method method;
  private boolean def;
  private Class<?> senderClass;
  private String permission;
  private Method completionMethod;
  private boolean optional;

  public CommandData(CommandBase commandBase) {
    this.commandBase = commandBase;
  }

  public CommandData(
      CommandBase commandBase,
      String name,
      Method method,
      boolean def,
      Class<?> senderClass,
      String permission,
      Method completionMethod,
      boolean optional) {
    this.commandBase = commandBase;
    this.name = name;
    this.method = method;
    this.def = def;
    this.senderClass = senderClass;
    this.permission = permission;
    this.completionMethod = completionMethod;
    this.optional = optional;
  }

  public CommandBase getCommandBase() {
    return commandBase;
  }

  public List<Class<?>> getArguments() {
    return arguments;
  }

  public List<String> getArgumentNames() {
    return argumentNames;
  }

  public Map<Integer, String> getCompletions() {
    return completions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public boolean isDef() {
    return def;
  }

  public void setDef(boolean def) {
    this.def = def;
  }

  public Class<?> getSenderClass() {
    return senderClass;
  }

  public void setSenderClass(Class<?> senderClass) {
    this.senderClass = senderClass;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public Method getCompletionMethod() {
    return completionMethod;
  }

  public void setCompletionMethod(Method completionMethod) {
    this.completionMethod = completionMethod;
  }

  public boolean hasOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }
}
