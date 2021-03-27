package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for specifying the correct usage of the command. Has no
 * internal use, just used for incorrect usage error messages and such.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Usage {

  /**
   * The command usage. Has no internal use, just used for incorrect usage error messages and such.
   *
   * @return The command usage
   */
  String value();
}
