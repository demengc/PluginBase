package dev.demeng.pluginbase.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command mangement annotation used for marking a command method as the default (base) command of a
 * {@link dev.demeng.pluginbase.command.CommandBase} class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Default {}
