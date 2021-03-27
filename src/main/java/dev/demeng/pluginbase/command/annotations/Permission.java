package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for specifying the required permission to execute the command.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Permission {

  /**
   * The required permission node for the command. No permission checks will be executed if this is
   * set to either null, an empty string, or the word "none".
   *
   * @return The required permission
   */
  String value();
}
