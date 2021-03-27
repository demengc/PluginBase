package dev.demeng.pluginbase.command.resolvers;

import dev.demeng.pluginbase.command.objects.TypeResult;

/**
 * Simple functional interface of resolving method parameters (command arguments). Primarily for
 * internal use.
 */
@FunctionalInterface
public interface ParameterResolver {

  /**
   * Resolves the type of class and returns the function registered.
   *
   * @param argument The object to be resolved
   * @return The result of the function
   */
  TypeResult resolve(Object argument);
}
