package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for marking a method as the tab-completion processing method
 * for the command.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface CompleteFor {

  /**
   * The name of the command or sub-command to complete for.
   *
   * @return The name of the command or sub-command to complete for
   */
  String value();
}
