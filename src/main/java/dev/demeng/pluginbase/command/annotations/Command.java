package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for marking {@link dev.demeng.pluginbase.command.CommandBase}
 * classes as an actual command. This annotation is required for all CommandBase classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

  /**
   * The command name.
   *
   * @return The command name
   */
  String value();
}
