package dev.demeng.pluginbase.chat.tell;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.demeng.pluginbase.chat.tell.objects.TellMessage;
import dev.demeng.pluginbase.chat.tell.objects.TellSound;
import dev.demeng.pluginbase.chat.tell.objects.TellTitle;
import org.bukkit.command.CommandSender;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a "tell" object- stuff to send to a player, typically to notify them of a result of a
 * command or event.
 */
public class TellContents implements TellObject {

  private final TellMessage message;
  private final TellTitle title;
  private final TellSound sound;

  /**
   * New tell contents.
   *
   * @param message The message to tell
   * @param title The title to tell
   * @param sound THe sound to tell
   */
  public TellContents(TellMessage message, TellTitle title, TellSound sound) {
    this.message = message;
    this.title = title;
    this.sound = sound;
  }

  /**
   * New tell contents.
   *
   * @param lines The message's lines
   */
  public TellContents(String... lines) {
    this.message = new TellMessage(lines);
    this.title = null;
    this.sound = null;
  }

  @Override
  public void tell(CommandSender sender) {

    if (message != null) {
      message.tell(sender);
    }

    if (title != null) {
      title.tell(sender);
    }

    if (sound != null) {
      sound.tell(sender);
    }
  }

  /**
   * Gets the chat message to send.
   *
   * @return The chat message to send
   */
  @Nullable
  public TellMessage getMessage() {
    return message;
  }

  /**
   * Gets the title to send.
   *
   * @return The title to send
   */
  @Nullable
  public TellTitle getTitle() {
    return title;
  }

  /**
   * Gets the sound to play.
   *
   * @return The sound to play
   */
  @Nullable
  public TellSound getSound() {
    return sound;
  }

  /**
   * Converts the object into a (JSON) format for storage. If the contents is only a single message
   * with default message settings, this method returns just that 1 line of message for more
   * user-friendly editing.
   *
   * @return The serialized JSON
   */
  @NotNull
  public String toJson() {

    if (title == null && sound == null) {

      if (message == null) {
        return "none";
      }

      return minimizeMessage(message);
    }

    return new Gson().toJson(this);
  }

  /**
   * Converts JSON back to a TellContents object. Supports the "none" value.
   *
   * @param json The JSON to deserialize
   * @return The equivalent TellContents object, or a new TellContents object with its message as
   *     the JSON parameter if the provided JSON is invalid
   */
  @NotNull
  public static TellContents fromJson(@Language("JSON") String json) {

    if (json.equalsIgnoreCase("none")) {
      return new TellContents(null, null, null);
    }

    final TellContents obj;

    try {
      obj = new Gson().fromJson(json, TellContents.class);
    } catch (JsonSyntaxException ex) {
      return new TellContents(json);
    }

    return obj;
  }

  private String minimizeMessage(TellMessage message) {

    if (!message.isJson()
        && message.getLines().length <= 1
        && message.isAddPrefix()
        && !message.isCenter()) {

      if (message.getLines().length == 0) {
        return "none";
      }

      return message.getLines()[0];
    }

    return message.toJson();
  }
}
