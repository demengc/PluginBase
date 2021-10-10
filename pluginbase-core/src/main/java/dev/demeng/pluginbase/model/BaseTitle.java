/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.model;

import com.cryptomorin.xseries.messages.Titles;
import dev.demeng.pluginbase.Players;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.serializer.type.YamlSerializable;
import java.io.IOException;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper and utility for a Minecraft "title", containing the actual title, subtitle, as well as
 * the fade in, stay, and fade out durations.
 *
 * @see org.bukkit.entity.Player#sendTitle(String, String, int, int, int)
 */
@Data
public class BaseTitle implements YamlSerializable<BaseTitle> {

  private static final int DEFAULT_FADE_IN = 10;
  private static final int DEFAULT_STAY = 60;
  private static final int DEFAULT_FADE_OUT = 15;

  /**
   * The actual title to be sent. Ignored if null.
   */
  @Nullable private final String title;

  /**
   * The subtitle ot be sent. Ignored if null.
   */
  @Nullable private final String subtitle;

  /**
   * The number of ticks before the title fully fades in.
   */
  private final int fadeIn;

  /**
   * The number of ticks the player stays static on the player's screen.
   */
  private final int stay;

  /**
   * The number of ticks the title will take to fully fade out.
   */
  private final int fadeOut;

  /**
   * Creates a new title.
   *
   * @param title    The title
   * @param subtitle The subtitle
   * @param fadeIn   The fade in duration, in ticks
   * @param stay     The stay duration, in ticks
   * @param fadeOut  The fade out duration, in ticks
   */
  public BaseTitle(@Nullable final String title, @Nullable final String subtitle,
      final int fadeIn, final int stay, final int fadeOut) {
    this.title = title == null ? null : ChatUtils.colorize(title);
    this.subtitle = subtitle == null ? null : ChatUtils.colorize(subtitle);
    this.fadeIn = fadeIn;
    this.stay = stay;
    this.fadeOut = fadeOut;
  }

  /**
   * Creates a new title from default durations.
   *
   * @param title    The title
   * @param subtitle The subtitle
   */
  public BaseTitle(@Nullable final String title, @Nullable final String subtitle) {
    this.title = title == null ? null : ChatUtils.colorize(title);
    this.subtitle = subtitle == null ? null : ChatUtils.colorize(subtitle);
    this.fadeIn = DEFAULT_FADE_IN;
    this.stay = DEFAULT_STAY;
    this.fadeOut = DEFAULT_FADE_OUT;
  }

  /**
   * Sends the title to the specified player.
   *
   * @param p The player to receive the title
   */
  public void send(final Player p) {
    Titles.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
  }

  /**
   * Sends the title to all players on the server.
   */
  public void sendAll() {
    Players.forEach(this::send);
  }

  @Override
  public void serialize(
      @NotNull final BaseTitle obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path
  ) throws IOException {

    if (title != null) {
      configFile.getConfig().set(path + ".title", obj.getTitle());
    }

    if (subtitle != null) {
      configFile.getConfig().set(path + ".subtitle", obj.getSubtitle());
    }

    if (fadeIn != DEFAULT_FADE_IN) {
      configFile.getConfig().set(path + ".fade-in", obj.getFadeIn());
    }

    if (stay != DEFAULT_STAY) {
      configFile.getConfig().set(path + ".stay", obj.getStay());
    }

    if (fadeOut != DEFAULT_FADE_OUT) {
      configFile.getConfig().set(path + ".fade-out", obj.getFadeOut());
    }

    configFile.save();
  }

  @Override
  public BaseTitle deserialize(@NotNull final ConfigurationSection section) {
    return new BaseTitle(
        section.getString("title"),
        section.getString("subtitle"),
        section.getInt("fade-in", DEFAULT_FADE_IN),
        section.getInt("stay", DEFAULT_STAY),
        section.getInt("fade-out", DEFAULT_FADE_OUT));
  }
}
