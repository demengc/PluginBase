/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) 2019 Matt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.pluginbase.command.handlers;

import dev.demeng.pluginbase.Validate;
import dev.demeng.pluginbase.command.objects.CommandData;
import dev.demeng.pluginbase.command.objects.TypeResult;
import dev.demeng.pluginbase.command.resolvers.ParameterResolver;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles and maps command arguments from string to the expected data type in method parameters.
 * Primarily for internal use.
 */
public final class ArgumentHandler {

  private final Map<Class<?>, ParameterResolver> registeredTypes = new HashMap<>();

  public ArgumentHandler() {

    register(
        Short.class,
        arg -> {
          final Integer integer = Validate.checkInt(String.valueOf(arg));
          return integer == null ? new TypeResult(arg) : new TypeResult(integer.shortValue(), arg);
        });

    register(Integer.class, arg -> new TypeResult(Validate.checkInt(String.valueOf(arg)), arg));
    register(Long.class, arg -> new TypeResult(Validate.checkLong(String.valueOf(arg)), arg));
    // register(Float.class, arg -> new TypeResult(Floats.tryParse(String.valueOf(arg)), arg));
    register(Double.class, arg -> new TypeResult(Validate.checkDouble(String.valueOf(arg)), arg));

    register(
        String.class,
        arg -> arg instanceof String ? new TypeResult(arg, arg) : new TypeResult(arg));

    register(
        String[].class,
        arg -> arg instanceof String[] ? new TypeResult(arg, arg) : new TypeResult(arg));

    register(Boolean.class, arg -> new TypeResult(Boolean.valueOf(String.valueOf(arg)), arg));
    register(boolean.class, arg -> new TypeResult(Boolean.valueOf(String.valueOf(arg)), arg));

    register(Player.class, arg -> new TypeResult(Bukkit.getPlayer(String.valueOf(arg)), arg));

    /*
    register(
        Material.class, arg -> new TypeResult(XMaterial.matchXMaterial(String.valueOf(arg)), arg));
     */

    register(
        Sound.class,
        arg -> {
          final String soundValue =
              Arrays.stream(Sound.values())
                  .map(Enum::name)
                  .filter(name -> name.equalsIgnoreCase(String.valueOf(arg)))
                  .findFirst()
                  .orElse(null);

          return soundValue == null
              ? new TypeResult(null, arg)
              : new TypeResult(Sound.valueOf(soundValue), arg);
        });

    register(World.class, arg -> new TypeResult(Bukkit.getWorld(String.valueOf(arg)), arg));
  }

  /**
   * Registers a new argument type that can be used in method parameters.
   *
   * @param clazz The type to be added
   * @param parameterResolver The parameter resolver that will process this data type
   */
  public void register(Class<?> clazz, ParameterResolver parameterResolver) {
    registeredTypes.put(clazz, parameterResolver);
  }

  /**
   * Gets a type result based on the class type.
   *
   * @param clazz The class to check
   * @param object The input object of the functional interface
   * @param data The command data
   * @param parameterName The parameter name from the method
   * @return The output object of the functional interface
   */
  Object getTypeResult(Class<?> clazz, Object object, CommandData data, String parameterName) {
    final TypeResult result = registeredTypes.get(clazz).resolve(object);
    data.getCommandBase().addArgument(parameterName, result.getArgumentName());

    return result.getResolvedValue();
  }

  /**
   * Checks if the type has been registered or not.
   *
   * @param clazz The class type to check
   * @return True if registered, false otherwise
   */
  boolean isRegisteredType(Class<?> clazz) {
    return registeredTypes.get(clazz) != null;
  }
}
