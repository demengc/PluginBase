/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package dev.demeng.pluginbase.commands.process;

import dev.demeng.pluginbase.commands.annotation.Permission;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.command.trait.CommandAnnotationHolder;
import dev.demeng.pluginbase.commands.command.trait.PermissionHolder;
import dev.demeng.pluginbase.commands.core.BaseCommandPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reader for the {@link CommandPermission} using the {@link Permission} annotation.
 */
public class PermissionReader {

  public static final PermissionReader INSTANCE = new PermissionReader();

  /**
   * Returns the specified permission for this command, or {@code null} if this reader does not
   * identify any permission.
   *
   * @param command Command to generate for. This will <em>always</em> be a {@link
   *                PermissionHolder}, and may be a {@link CommandParameter} or an {@link
   *                ExecutableCommand}.
   * @return The permission, or null if not identified.
   */
  @Nullable
  public CommandPermission getPermission(@NotNull CommandAnnotationHolder command) {
    Permission permissionAnn = command.getAnnotation(Permission.class);
    if (permissionAnn == null) {
      return null;
    }
    return new BaseCommandPermission(new org.bukkit.permissions.Permission(permissionAnn.value(),
        permissionAnn.defaultAccess()));
  }
}
