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
import dev.demeng.pluginbase.chat.ChatUtils;
import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for a Minecraft "title", containing the actual title, subtitle, as well as the fade in,
 * stay, and fade out durations. Also contains some utilities, including multi-version support,
 * automatic colorization and broadcasting to the entire server.
 *
 * @see org.bukkit.entity.Player#sendTitle(String, String, int, int, int)
 */
@Data
public class BaseTitle {

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
  public BaseTitle(@Nullable String title, @Nullable String subtitle,
      int fadeIn, int stay, int fadeOut) {
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
  public BaseTitle(@Nullable String title, @Nullable String subtitle) {
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
  public void send(Player p) {
    Titles.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
  }

  /**
   * Sends the title to all players on the server.
   */
  public void sendAll() {
    Players.forEach(this::send);
  }
}
