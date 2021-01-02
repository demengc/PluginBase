package dev.demeng.pluginbase.chat.tell.objects;

import com.google.gson.Gson;
import dev.demeng.pluginbase.SoundUtils;
import dev.demeng.pluginbase.chat.tell.TellObject;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** A sound to play for the individual player. * */
public class TellSound implements TellObject {

  private final String sound;
  private final float volume;
  private final float pitch;

  /**
   * A new sound to play.
   *
   * @param sound The sound enum's name, or a custom sound prefixed with "custom:"
   * @param volume The volume, must be between 0-1
   * @param pitch THe pitch, must be between 0-1
   */
  public TellSound(String sound, float volume, float pitch) {
    Objects.requireNonNull(sound);

    if (volume < 0 || volume > 1 || pitch < 0 || pitch > 1) {
      throw new IllegalArgumentException("Sound volume and pitch must be between 0-1");
    }

    this.sound = sound;
    this.volume = volume;
    this.pitch = pitch;
  }

  @Override
  public void tell(CommandSender sender) {

    if (!(sender instanceof Player)) {
      return;
    }

    SoundUtils.playToPlayer((Player) sender, sound, volume, pitch);
  }

  /**
   * Converts the sound into JSON, for storage.
   *
   * @return The equivalent JSON sound
   */
  @NotNull
  public String toJson() {
    return new Gson().toJson(this);
  }

  /**
   * Converts the JSON into a TellSound object.
   *
   * @param json The JSON to convert
   * @return The equivalent TellSound object
   */
  @NotNull
  public static TellSound fromJson(@Language("JSON") String json) {
    return new Gson().fromJson(json, TellSound.class);
  }

  /**
   * The sound to play. Can be a sound enum's name, or a custom sound prefixed with "custom:".
   *
   * @return The sound to play
   */
  @NotNull
  public String getSound() {
    return sound;
  }

  /**
   * The volume of the sound, with 0 being the quitest and 1 being the loudest.
   *
   * @return The volume of the sound
   */
  public float getVolume() {
    return volume;
  }

  /**
   * The pitch of the sound, with 0 being the lowest and 1 being the highest.
   *
   * @return The pitch of the sound
   */
  public float getPitch() {
    return pitch;
  }

  /** Builder for this object. */
  private static class Builder {

    private String sound;
    private float volume;
    private float pitch;

    @NotNull
    public Builder sound(String sound) {
      this.sound = sound;
      return this;
    }

    @NotNull
    public Builder sound(Sound sound) {
      this.sound = sound.name();
      return this;
    }

    @NotNull
    public Builder volume(float volume) {
      this.volume = volume;
      return this;
    }

    @NotNull
    public Builder pitch(float pitch) {
      this.pitch = pitch;
      return this;
    }

    @NotNull
    public TellSound build() {
      return new TellSound(sound, volume, pitch);
    }
  }
}
