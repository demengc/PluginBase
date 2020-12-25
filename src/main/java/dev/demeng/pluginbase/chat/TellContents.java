package dev.demeng.pluginbase.chat;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a "tell" object- stuff to send to a player, typically to notify them of a result of a
 * command or event.
 */
public class TellContents {

  private final String message;
  private final TellContents.Title title;
  private final TellContents.ActionBar actionBar;
  private final TellContents.Sound sound;

  /**
   * New tell contents. Any parameter may be null if you wish to not use the feature.
   *
   * @param message The chat message to send
   * @param title The title to send
   * @param actionBar The action bar to send
   * @param sound The sound to play
   */
  public TellContents(
      String message,
      TellContents.Title title,
      TellContents.ActionBar actionBar,
      TellContents.Sound sound) {
    this.message = message;
    this.title = title;
    this.actionBar = actionBar;
    this.sound = sound;
  }

  /**
   * New tell contents containing only a message.
   *
   * @param message The chat message to send
   */
  public TellContents(String message) {
    this.message = message;
    this.title = null;
    this.actionBar = null;
    this.sound = null;
  }

  /**
   * Gets the chat message to send.
   *
   * @return The chat message to send
   */
  @Nullable
  public String getMessage() {
    return message;
  }

  /**
   * Gets the title to send.
   *
   * @return The title to send
   */
  @Nullable
  public Title getTitle() {
    return title;
  }

  /**
   * Gets the action bar to send.
   *
   * @return The action bar to send
   */
  @Nullable
  public TellContents.ActionBar getActionBar() {
    return actionBar;
  }

  /**
   * Gets the sound to play.
   *
   * @return The sound to play
   */
  @Nullable
  public TellContents.Sound getSound() {
    return sound;
  }

  public String getJson() {

    if (title == null && actionBar == null && sound == null) {

      if (message == null) {
        return "none";
      }

      return message;
    }

    final Map<String, String> map = new LinkedHashMap<>();

    if (message != null) {
      map.put("message", message);
    }

    if (title != null) {
      final Map<String, Object> titleMap = new LinkedHashMap<>();
      titleMap.put("title", title.getTitle());

      if (title.getSubtitle() != null) {
        titleMap.put("subtitle", title.getSubtitle());
      }

      titleMap.put("fadeIn", title.getFadeIn());
      titleMap.put("stay", title.getStay());
      titleMap.put("fadeOut", title.getFadeOut());

      map.put("title", new JSONObject(titleMap).toJSONString());
    }

    if (actionBar != null) {
      final Map<String, Object> actionBarMap = new LinkedHashMap<>();
      actionBarMap.put("text", actionBar.getText());
      actionBarMap.put("duration", actionBar.getDuration());

      map.put("actionBar", new JSONObject(actionBarMap).toJSONString());
    }

    if (sound != null) {
      final Map<String, Object> soundMap = new LinkedHashMap<>();
      soundMap.put("sound", sound.getSound());
      soundMap.put("volume", sound.getVolume());
      soundMap.put("pitch", sound.getPitch());

      map.put("sound", new JSONObject(soundMap).toJSONString());
    }

    return new JSONObject(map).toJSONString();
  }

  public static TellContents fromJson(@Language("JSON") String strJson) {

    if (strJson.equalsIgnoreCase("none")) {
      return new TellContents(null);
    }

    final JSONObject json;

    try {
      json = (JSONObject) new JSONParser().parse(strJson);
    } catch (ParseException ex) {
      return new TellContents(strJson);
    }

    TellContents.Title title = null;
    TellContents.ActionBar actionBar = null;
    TellContents.Sound sound = null;

    try {

      if (json.containsKey("title")) {
        title = titleFromJson((String) json.get("title"));
      }

      if (json.containsKey("actionBar")) {
        actionBar = actionBarFromJson((String) json.get("actionBar"));
      }

      if (json.containsKey("sound")) {
        sound = soundFromJson((String) json.get("sound"));
      }

    } catch (ParseException ex) {
      return new TellContents(strJson);
    }

    return new TellContents((String) json.get("message"), title, actionBar, sound);
  }

  private static TellContents.Title titleFromJson(@Language("JSON") String strJson)
      throws ParseException {
    final JSONObject json = (JSONObject) new JSONParser().parse(strJson);
    return new TellContents.Title(
        (String) json.get("title"),
        (String) json.get("subtitle"),
        (int) json.get("fadeIn"),
        (int) json.get("stay"),
        (int) json.get("fadeOut"));
  }

  private static TellContents.ActionBar actionBarFromJson(@Language("JSON") String strJson)
      throws ParseException {
    final JSONObject json = (JSONObject) new JSONParser().parse(strJson);
    return new TellContents.ActionBar((String) json.get("text"), (int) json.get("duration"));
  }

  private static TellContents.Sound soundFromJson(@Language("JSON") String strJson)
      throws ParseException {
    final JSONObject json = (JSONObject) new JSONParser().parse(strJson);
    return new TellContents.Sound(
        (String) json.get("sound"), (float) json.get("volume"), (float) json.get("pitch"));
  }

  /** A title and/or subtitle complete with fade in time, stay time, and fade out time. */
  public static class Title {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    /**
     * A title to tell.
     *
     * @param title The title
     * @param subtitle The subtitle, lies beneath the title
     * @param fadeIn The number of ticks for the title and/or subtitle to fade in to the player's
     *     screen
     * @param stay The number of ticks the title and/or subtitle will stay on the player's screen
     * @param fadeOut The number of ticks for the title and/or subtitle to fade out of the player's
     *     screen
     */
    public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {

      if (fadeIn < 0 || fadeOut < 0) {
        throw new IllegalArgumentException(
            "Title fade in and fade out time must be a positive integer");
      }

      if (stay < 1) {
        throw new IllegalArgumentException("Title stay time must be greater than 1");
      }

      this.title = title == null ? "" : title;
      this.subtitle = subtitle;
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
    }

    /**
     * The title to send.
     *
     * @return The title to send
     */
    @NotNull
    public String getTitle() {
      return title;
    }

    /**
     * The subtitle to send.
     *
     * @return The subtitle to send
     */
    @Nullable
    public String getSubtitle() {
      return subtitle;
    }

    /**
     * The number of ticks for the title and/or subtitle to fade in to the player's screen.
     *
     * @return The fade in time
     */
    public int getFadeIn() {
      return fadeIn;
    }

    /**
     * The number of ticks the title and/or subtitle will stay on the player's screen.
     *
     * @return The stay time
     */
    public int getStay() {
      return stay;
    }

    /**
     * The number of ticks for the title and/or subtitle to fade out of the player's screen.
     *
     * @return The fade out time
     */
    public int getFadeOut() {
      return fadeOut;
    }
  }

  /** An action bar to send with customizable duration. */
  public static class ActionBar {

    private final String text;
    private final long duration;

    /**
     * A new action bar to send.
     *
     * @param text The text of the action bar
     * @param duration The approximate number of ticks the bar should stay on the player's screen
     *     may be around 40 ticks longer)
     */
    public ActionBar(String text, long duration) {
      Objects.requireNonNull(text, "Action bar text cannot be null");

      if (duration < 1) {
        throw new IllegalArgumentException("Action bar duration must be a positive integer");
      }

      this.text = text;
      this.duration = duration;
    }

    /**
     * The text of the action bar.
     *
     * @return The text of the action bar
     */
    @NotNull
    public String getText() {
      return text;
    }

    /**
     * The number of ticks the action bar stays on the player's screen. This is not very accurate
     * and the action bar could stay for up to 2 more seconds than the specified time.
     *
     * @return The approximate number of ticks this bar should stay on the player's screen ()
     */
    public long getDuration() {
      return duration;
    }
  }

  /** A sound to play for the individual player. * */
  public static class Sound {

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
    public Sound(String sound, float volume, float pitch) {
      Objects.requireNonNull(sound);

      if (volume < 0 || volume > 1 || pitch < 0 || pitch > 1) {
        throw new IllegalArgumentException("Sound volume and pitch must be between 0-1");
      }

      this.sound = sound;
      this.volume = volume;
      this.pitch = pitch;
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
  }
}
