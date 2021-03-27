package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for specifying the tab-completions for the annotated method or
 * parameter (command argument).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Completion {

  /**
   * The completions for the command (if annotated on method) or a single argument (if annotated on
   * parameter).
   *
   * @return The completions for the command or argument
   */
  String[] value();
}
