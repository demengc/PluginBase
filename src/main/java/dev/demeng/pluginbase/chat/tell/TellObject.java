package dev.demeng.pluginbase.chat.tell;

import dev.demeng.pluginbase.JsonSerializable;
import org.bukkit.command.CommandSender;

/**
 * Represents a tell object, which can be sent to a CommandSender and serialized/deserialized with
 * GSON.
 *
 * @param <T>
 */
public interface TellObject<T> extends JsonSerializable<T> {

  /**
   * Sends the object to a command sender.
   *
   * @param sender The command sender that should receive this tell object
   */
  void tell(CommandSender sender);
}
