package dev.demeng.pluginbase.chat.tell.objects;

import com.cryptomorin.xseries.messages.Titles;
import dev.demeng.pluginbase.chat.tell.TellObject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A title and/or subtitle complete with fade in time, stay time, and fade out time. */
public class TellTitle implements TellObject<TellTitle> {

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
  public TellTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {

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

  @Override
  public void tell(CommandSender sender) {

    if(sender instanceof Player) {
      Titles.sendTitle((Player) sender, fadeIn, stay, fadeOut, title, subtitle);
    }
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

  /**
   * Creates a new builder.
   *
   * @return A new builder
   */
  @NotNull
  public TellTitle.Builder builder() {
    return new TellTitle.Builder();
  }

  /** Builder for this object. */
  private static class Builder {

    private String title = "";
    private String subtitle = null;
    private int fadeIn = 10;
    private int stay = 60;
    private int fadeOut = 10;

    @NotNull
    public Builder title(String title) {
      this.title = title;
      return this;
    }

    @NotNull
    public Builder subtitle(String subtitle) {
      this.subtitle = subtitle;
      return this;
    }

    @NotNull
    public Builder fadeIn(int fadeIn) {
      this.fadeIn = fadeIn;
      return this;
    }

    @NotNull
    public Builder stay(int stay) {
      this.stay = stay;
      return this;
    }

    @NotNull
    public Builder fadeOut(int fadeOut) {
      this.fadeOut = fadeOut;
      return this;
    }

    @NotNull
    public TellTitle build() {
      return new TellTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
  }
}
