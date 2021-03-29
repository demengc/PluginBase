/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matt
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

package dev.demeng.pluginbase.command.objects;

import dev.demeng.pluginbase.command.CommandBase;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all of the relevant data for a command. Primarily for internal use.
 */
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
