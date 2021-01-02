package dev.demeng.pluginbase.chat.tell;

import org.bukkit.command.CommandSender;

/** Represents a tell object, which can be sent to a CommandSender. */
public interface TellObject {

  /**
   * Sends the object to a command sender.
   *
   * @param sender The command sender that should receive this tell object
   */
  void tell(CommandSender sender);
}
