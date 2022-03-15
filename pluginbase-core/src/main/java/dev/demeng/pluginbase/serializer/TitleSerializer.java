/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

package dev.demeng.pluginbase.serializer;

import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.model.BaseTitle;
import dev.demeng.pluginbase.serializer.type.YamlSerializable;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * The serializer for {@link BaseTitle}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TitleSerializer implements YamlSerializable<BaseTitle> {

  private static final TitleSerializer NOOP = new TitleSerializer();

  /**
   * Gets the singular instance of this serializer.
   *
   * @return The serializer instance
   */
  public static TitleSerializer get() {
    return NOOP;
  }

  @Override
  public void serialize(
      @NotNull final BaseTitle obj,
      @NotNull final YamlConfig configFile,
      @NotNull final String path
  ) throws IOException {

    if (obj.getTitle() != null) {
      configFile.getConfig().set(path + ".title", obj.getTitle());
    }

    if (obj.getSubtitle() != null) {
      configFile.getConfig().set(path + ".subtitle", obj.getSubtitle());
    }

    if (obj.getFadeIn() != BaseTitle.DEFAULT_FADE_IN) {
      configFile.getConfig().set(path + ".fade-in", obj.getFadeIn());
    }

    if (obj.getStay() != BaseTitle.DEFAULT_STAY) {
      configFile.getConfig().set(path + ".stay", obj.getStay());
    }

    if (obj.getFadeOut() != BaseTitle.DEFAULT_FADE_OUT) {
      configFile.getConfig().set(path + ".fade-out", obj.getFadeOut());
    }

    configFile.save();
  }

  @Override
  public BaseTitle deserialize(@NotNull final ConfigurationSection section) {
    return new BaseTitle(
        section.getString("title"),
        section.getString("subtitle"),
        section.getInt("fade-in", BaseTitle.DEFAULT_FADE_IN),
        section.getInt("stay", BaseTitle.DEFAULT_STAY),
        section.getInt("fade-out", BaseTitle.DEFAULT_FADE_OUT));
  }
}
