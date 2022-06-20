/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
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

package dev.demeng.pluginbase.commands.bukkit.core;

import dev.demeng.pluginbase.commands.bukkit.BukkitCommandPermission;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.trait.CommandAnnotationHolder;
import dev.demeng.pluginbase.commands.process.PermissionReader;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum BukkitPermissionReader implements PermissionReader {
  INSTANCE;

  @Override
  public @Nullable CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
    dev.demeng.pluginbase.commands.bukkit.annotation.CommandPermission permissionAnn = command.getAnnotation(
        dev.demeng.pluginbase.commands.bukkit.annotation.CommandPermission.class);
    if (permissionAnn == null) {
      return null;
    }
    return new BukkitCommandPermission(
        new Permission(permissionAnn.value(), permissionAnn.defaultAccess()));
  }
}
