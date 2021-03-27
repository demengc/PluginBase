package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for marking the command method as a sub-command of the base
 * command.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {

  /**
   * The name of the sub-command.
   *
   * @return The name of the sub-command
   */
  String value();
}
