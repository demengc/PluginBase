package dev.demeng.pluginbase.model;

import dev.demeng.pluginbase.DynamicPlaceholders;
import java.util.function.UnaryOperator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * A shorthand version of {@link DynamicPlaceholders}, allowing for string placeholders to be easily
 * applied on strings, lists, and item stacks.
 */
@Data(staticConstructor = "of")
public class Placeholders implements DynamicPlaceholders {

  private final UnaryOperator<String> placeholderReplacer;

  @Override
  public @NotNull String setPlaceholders(@NotNull final String str) {
    return placeholderReplacer.apply(str);
  }
}
