package dev.demeng.pluginbase.command.resolvers;

import java.util.List;

/** Simple functional interface of resolving command completions. Primarily for internal use. */
@FunctionalInterface
public interface CompletionResolver {

  /**
   * Resolves the command completions based on the input.
   *
   * @param input An input from the completion event
   * @return A string list with all the completion values
   */
  List<String> resolve(Object input);
}
