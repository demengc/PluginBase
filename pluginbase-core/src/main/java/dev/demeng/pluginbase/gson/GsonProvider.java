/*
 * MIT License
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) lucko/helper contributors
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

package dev.demeng.pluginbase.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/** Provides static instances of {@link Gson}. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GsonProvider {

  private static final Gson STANDARD_GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  private static final Gson PRETTY_PRINT_GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create();

  @SuppressWarnings("deprecation")
  private static final JsonParser PARSER = new JsonParser();

  /**
   * Returns the standard {@link Gson} instance configured with null serialization and HTML escaping
   * disabled.
   *
   * @return the standard Gson instance
   */
  @NotNull
  public static Gson standard() {
    return STANDARD_GSON;
  }

  /**
   * Returns a {@link Gson} instance configured with pretty printing, null serialization, and HTML
   * escaping disabled.
   *
   * @return the pretty-printing Gson instance
   */
  @NotNull
  public static Gson prettyPrinting() {
    return PRETTY_PRINT_GSON;
  }

  /**
   * Returns the shared {@link JsonParser} instance.
   *
   * @return the shared JsonParser
   */
  @SuppressWarnings("deprecation")
  @NotNull
  public static JsonParser parser() {
    return PARSER;
  }

  /**
   * Parses the content of the given {@link Reader} into a {@link JsonObject}.
   *
   * @param reader the reader supplying the JSON content
   * @return the parsed JsonObject
   */
  @SuppressWarnings("deprecation")
  @NotNull
  public static JsonObject readObject(@NotNull final Reader reader) {
    return PARSER.parse(reader).getAsJsonObject();
  }

  /**
   * Parses a JSON string into a {@link JsonObject}.
   *
   * @param s the JSON string to parse
   * @return the parsed JsonObject
   */
  @SuppressWarnings("deprecation")
  @NotNull
  public static JsonObject readObject(@NotNull final String s) {
    return PARSER.parse(s).getAsJsonObject();
  }

  /**
   * Serializes a {@link JsonObject} to the given {@link Appendable} using standard formatting.
   *
   * @param writer the target to write the JSON to
   * @param object the object to serialize
   */
  public static void writeObject(
      @NotNull final Appendable writer, @NotNull final JsonObject object) {
    standard().toJson(object, writer);
  }

  /**
   * Serializes a {@link JsonObject} to the given {@link Appendable} using pretty-print formatting.
   *
   * @param writer the target to write the JSON to
   * @param object the object to serialize
   */
  public static void writeObjectPretty(
      @NotNull final Appendable writer, @NotNull final JsonObject object) {
    prettyPrinting().toJson(object, writer);
  }

  /**
   * Serializes a {@link JsonElement} to the given {@link Appendable} using standard formatting.
   *
   * @param writer the target to write the JSON to
   * @param element the element to serialize
   */
  public static void writeElement(
      @NotNull final Appendable writer, @NotNull final JsonElement element) {
    standard().toJson(element, writer);
  }

  /**
   * Serializes a {@link JsonElement} to the given {@link Appendable} using pretty-print formatting.
   *
   * @param writer the target to write the JSON to
   * @param element the element to serialize
   */
  public static void writeElementPretty(
      @NotNull final Appendable writer, @NotNull final JsonElement element) {
    prettyPrinting().toJson(element, writer);
  }

  /**
   * Serializes a {@link JsonElement} to a string using standard formatting.
   *
   * @param element the element to serialize
   * @return the JSON string representation
   */
  @NotNull
  public static String toString(@NotNull final JsonElement element) {
    return Objects.requireNonNull(standard().toJson(element));
  }

  /**
   * Serializes a {@link JsonElement} to a pretty-printed string.
   *
   * @param element the element to serialize
   * @return the pretty-printed JSON string representation
   */
  @NotNull
  public static String toStringPretty(@NotNull final JsonElement element) {
    return Objects.requireNonNull(prettyPrinting().toJson(element));
  }
}
