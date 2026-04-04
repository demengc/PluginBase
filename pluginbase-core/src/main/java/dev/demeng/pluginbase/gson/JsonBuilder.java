/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) lucko/helper contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package dev.demeng.pluginbase.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/** Builder utilities for creating GSON Objects/Arrays. */
public final class JsonBuilder {

  /**
   * Creates a new object builder
   *
   * <p>If copy is not true, the passed object will be mutated by the builder methods.
   *
   * @param object the object to base the new builder upon
   * @param copy if the object should be deep copied, or just referenced.
   * @return a new builder
   */
  public static JsonObjectBuilder object(final JsonObject object, final boolean copy) {
    Objects.requireNonNull(object, "object");

    if (copy) {
      return object().addAll(object, true);
    } else {
      return new JsonObjectBuilderImpl(object);
    }
  }

  /**
   * Creates a new object builder, without copying the passed object.
   *
   * <p>Equivalent to calling {@link #object(JsonObject, boolean)} with copy = false.
   *
   * @param object the object to base the new builder upon
   * @return a new builder
   */
  public static JsonObjectBuilder object(final JsonObject object) {
    Objects.requireNonNull(object, "object");
    return object(object, false);
  }

  /**
   * Creates a new object builder, with no initial values
   *
   * @return a new builder
   */
  public static JsonObjectBuilder object() {
    return object(new JsonObject());
  }

  /**
   * Creates a new array builder
   *
   * <p>If copy is not true, the passed array will be mutated by the builder methods.
   *
   * @param array the array to base the new builder upon
   * @param copy if the array should be deep copied, or just referenced.
   * @return a new builder
   */
  public static JsonArrayBuilder array(final JsonArray array, final boolean copy) {
    Objects.requireNonNull(array, "array");

    if (copy) {
      return array().addAll(array, true);
    } else {
      return new JsonArrayBuilderImpl(array);
    }
  }

  /**
   * Creates a new array builder, without copying the passed array.
   *
   * <p>Equivalent to calling {@link #array(JsonArray, boolean)} with copy = false.
   *
   * @param array the array to base the new builder upon
   * @return a new builder
   */
  public static JsonArrayBuilder array(final JsonArray array) {
    Objects.requireNonNull(array, "array");
    return array(array, false);
  }

  /**
   * Creates a new array builder, with no initial values
   *
   * @return a new builder
   */
  public static JsonArrayBuilder array() {
    return array(new JsonArray());
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, {@link #nullValue()} is returned.
   *
   * @param value the value
   * @return a json primitive for the value
   */
  public static JsonElement primitive(@Nullable final String value) {
    return value == null ? nullValue() : new JsonPrimitive(value);
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, {@link #nullValue()} is returned.
   *
   * @param value the value
   * @return a json primitive for the value
   */
  public static JsonElement primitive(@Nullable final Number value) {
    return value == null ? nullValue() : new JsonPrimitive(value);
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, {@link #nullValue()} is returned.
   *
   * @param value the value
   * @return a json primitive for the value
   */
  public static JsonElement primitive(@Nullable final Boolean value) {
    return value == null ? nullValue() : new JsonPrimitive(value);
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, {@link #nullValue()} is returned.
   *
   * @param value the value
   * @return a json primitive for the value
   */
  public static JsonElement primitive(@Nullable final Character value) {
    return value == null ? nullValue() : new JsonPrimitive(value);
  }

  /**
   * Returns an instance of {@link JsonNull}.
   *
   * @return a json null instance
   */
  public static JsonNull nullValue() {
    return JsonNull.INSTANCE;
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, a {@link NullPointerException} will be thrown.
   *
   * @param value the value
   * @return a json primitive for the value
   * @throws NullPointerException if value is null
   */
  public static JsonPrimitive primitiveNonNull(final String value) {
    Objects.requireNonNull(value, "value");
    return new JsonPrimitive(value);
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, a {@link NullPointerException} will be thrown.
   *
   * @param value the value
   * @return a json primitive for the value
   * @throws NullPointerException if value is null
   */
  public static JsonPrimitive primitiveNonNull(final Number value) {
    Objects.requireNonNull(value, "value");
    return new JsonPrimitive(value);
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, a {@link NullPointerException} will be thrown.
   *
   * @param value the value
   * @return a json primitive for the value
   * @throws NullPointerException if value is null
   */
  public static JsonPrimitive primitiveNonNull(final Boolean value) {
    Objects.requireNonNull(value, "value");
    return new JsonPrimitive(value);
  }

  /**
   * Creates a JsonPrimitive from the given value.
   *
   * <p>If the value is null, a {@link NullPointerException} will be thrown.
   *
   * @param value the value
   * @return a json primitive for the value
   * @throws NullPointerException if value is null
   */
  public static JsonPrimitive primitiveNonNull(final Character value) {
    Objects.requireNonNull(value, "value");
    return new JsonPrimitive(value);
  }

  /**
   * Returns a collector which forms a JsonObject using the key and value mappers
   *
   * @param keyMapper the function to map from T to {@link String}
   * @param valueMapper the function to map from T to {@link JsonElement}
   * @param <T> the type
   * @return a new collector
   */
  public static <T> Collector<T, JsonObjectBuilder, JsonObject> collectToObject(
      final Function<? super T, String> keyMapper,
      final Function<? super T, JsonElement> valueMapper) {
    return Collector.of(
        JsonBuilder::object,
        (r, t) -> r.add(keyMapper.apply(t), valueMapper.apply(t)),
        (l, r) -> l.addAll(r.build()),
        JsonObjectBuilder::build);
  }

  /**
   * Returns a collector which forms a JsonArray using the value mapper
   *
   * @param valueMapper the function to map from T to {@link JsonElement}
   * @param <T> the type
   * @return a new collector
   */
  public static <T> Collector<T, JsonArrayBuilder, JsonArray> collectToArray(
      final Function<? super T, JsonElement> valueMapper) {
    return Collector.of(
        JsonBuilder::array,
        (r, t) -> r.add(valueMapper.apply(t)),
        (l, r) -> l.addAll(r.build()),
        JsonArrayBuilder::build);
  }

  /**
   * Returns a collector which forms a JsonArray from JsonElements
   *
   * @return a new collector
   */
  public static Collector<JsonElement, JsonArrayBuilder, JsonArray> collectToArray() {
    return Collector.of(
        JsonBuilder::array,
        JsonArrayBuilder::add,
        (l, r) -> l.addAll(r.build()),
        JsonArrayBuilder::build);
  }

  /** A {@link JsonObject} builder utility */
  public interface JsonObjectBuilder
      extends BiConsumer<String, JsonElement>, Consumer<Map.Entry<String, JsonElement>> {

    @Override
    default void accept(final Map.Entry<String, JsonElement> entry) {
      Objects.requireNonNull(entry, "entry");
      add(entry.getKey(), entry.getValue());
    }

    @Override
    default void accept(final String property, final JsonElement value) {
      add(property, value);
    }

    /**
     * Adds a property with the given name and value.
     *
     * <p>If {@code copy} is true, {@link JsonObject} and {@link JsonArray} values are deep copied
     * before being added; otherwise the element is referenced directly.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @param copy whether to deep copy object and array values
     * @return this builder
     */
    JsonObjectBuilder add(String property, @Nullable JsonElement value, boolean copy);

    /**
     * Adds a property with the given name and value without copying.
     *
     * <p>Equivalent to calling {@link #add(String, JsonElement, boolean)} with {@code copy =
     * false}.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder add(final String property, @Nullable final JsonElement value) {
      return add(property, value, false);
    }

    /**
     * Adds a string property, wrapping the value via {@link JsonBuilder#primitive(String)}.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder add(final String property, @Nullable final String value) {
      return add(property, primitive(value));
    }

    /**
     * Adds a number property, wrapping the value via {@link JsonBuilder#primitive(Number)}.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder add(final String property, @Nullable final Number value) {
      return add(property, primitive(value));
    }

    /**
     * Adds a boolean property, wrapping the value via {@link JsonBuilder#primitive(Boolean)}.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder add(final String property, @Nullable final Boolean value) {
      return add(property, primitive(value));
    }

    /**
     * Adds a character property, wrapping the value via {@link JsonBuilder#primitive(Character)}.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder add(final String property, @Nullable final Character value) {
      return add(property, primitive(value));
    }

    /**
     * Adds a property only if no property with the same name is already present.
     *
     * <p>If {@code copy} is true, {@link JsonObject} and {@link JsonArray} values are deep copied
     * before being added; otherwise the element is referenced directly.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @param copy whether to deep copy object and array values
     * @return this builder
     */
    JsonObjectBuilder addIfAbsent(String property, @Nullable JsonElement value, boolean copy);

    /**
     * Adds a property only if no property with the same name is already present, without copying.
     *
     * <p>Equivalent to calling {@link #addIfAbsent(String, JsonElement, boolean)} with {@code copy
     * = false}.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder addIfAbsent(
        final String property, @Nullable final JsonElement value) {
      return addIfAbsent(property, value, false);
    }

    /**
     * Adds a string property only if no property with the same name is already present.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder addIfAbsent(final String property, @Nullable final String value) {
      return addIfAbsent(property, primitive(value));
    }

    /**
     * Adds a number property only if no property with the same name is already present.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder addIfAbsent(final String property, @Nullable final Number value) {
      return addIfAbsent(property, primitive(value));
    }

    /**
     * Adds a boolean property only if no property with the same name is already present.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder addIfAbsent(final String property, @Nullable final Boolean value) {
      return addIfAbsent(property, primitive(value));
    }

    /**
     * Adds a character property only if no property with the same name is already present.
     *
     * @param property the property name
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonObjectBuilder addIfAbsent(final String property, @Nullable final Character value) {
      return addIfAbsent(property, primitive(value));
    }

    /**
     * Adds all entries from the iterable.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped. If {@code
     * deepCopy} is true, object and array values are deep copied before being added.
     *
     * @param iterable the entries to add
     * @param deepCopy whether to deep copy object and array values
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAll(
        final Iterable<Map.Entry<String, T>> iterable, final boolean deepCopy) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, T> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        add(e.getKey(), e.getValue(), deepCopy);
      }
      return this;
    }

    /**
     * Adds all entries from the iterable without copying.
     *
     * <p>Equivalent to calling {@link #addAll(Iterable, boolean)} with {@code deepCopy = false}.
     *
     * @param iterable the entries to add
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAll(
        final Iterable<Map.Entry<String, T>> iterable) {
      return addAll(iterable, false);
    }

    /**
     * Adds all entries from the stream.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped. If {@code
     * deepCopy} is true, object and array values are deep copied before being added.
     *
     * @param stream the entries to add
     * @param deepCopy whether to deep copy object and array values
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAll(
        final Stream<Map.Entry<String, T>> stream, final boolean deepCopy) {
      Objects.requireNonNull(stream, "stream");
      stream.forEach(
          e -> {
            if (e == null || e.getKey() == null) {
              return;
            }
            add(e.getKey(), e.getValue(), deepCopy);
          });
      return this;
    }

    /**
     * Adds all entries from the stream without copying.
     *
     * <p>Equivalent to calling {@link #addAll(Stream, boolean)} with {@code deepCopy = false}.
     *
     * @param stream the entries to add
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAll(
        final Stream<Map.Entry<String, T>> stream) {
      return addAll(stream, false);
    }

    /**
     * Adds all entries from another {@link JsonObject}.
     *
     * <p>If {@code deepCopy} is true, object and array values are deep copied before being added.
     *
     * @param object the source object
     * @param deepCopy whether to deep copy object and array values
     * @return this builder
     */
    default JsonObjectBuilder addAll(final JsonObject object, final boolean deepCopy) {
      Objects.requireNonNull(object, "object");
      return addAll(object.entrySet(), deepCopy);
    }

    /**
     * Adds all entries from another {@link JsonObject} without copying.
     *
     * <p>Equivalent to calling {@link #addAll(JsonObject, boolean)} with {@code deepCopy = false}.
     *
     * @param object the source object
     * @return this builder
     */
    default JsonObjectBuilder addAll(final JsonObject object) {
      return addAll(object, false);
    }

    /**
     * Adds all string entries from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(String)}.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @return this builder
     */
    default JsonObjectBuilder addAllStrings(final Iterable<Map.Entry<String, String>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, String> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        add(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all number entries from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(Number)}.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @param <T> the number type
     * @return this builder
     */
    default <T extends Number> JsonObjectBuilder addAllNumbers(
        final Iterable<Map.Entry<String, T>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, T> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        add(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all boolean entries from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(Boolean)}.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @return this builder
     */
    default JsonObjectBuilder addAllBooleans(final Iterable<Map.Entry<String, Boolean>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, Boolean> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        add(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all character entries from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(Character)}.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @return this builder
     */
    default JsonObjectBuilder addAllCharacters(
        final Iterable<Map.Entry<String, Character>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, Character> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        add(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all entries from the iterable, skipping any whose key is already present.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped. If {@code
     * deepCopy} is true, object and array values are deep copied before being added.
     *
     * @param iterable the entries to add
     * @param deepCopy whether to deep copy object and array values
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(
        final Iterable<Map.Entry<String, T>> iterable, final boolean deepCopy) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, T> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        addIfAbsent(e.getKey(), e.getValue(), deepCopy);
      }
      return this;
    }

    /**
     * Adds all entries from the iterable without copying, skipping any whose key is already
     * present.
     *
     * <p>Equivalent to calling {@link #addAllIfAbsent(Iterable, boolean)} with {@code deepCopy =
     * false}.
     *
     * @param iterable the entries to add
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(
        final Iterable<Map.Entry<String, T>> iterable) {
      return addAllIfAbsent(iterable, false);
    }

    /**
     * Adds all entries from the stream, skipping any whose key is already present.
     *
     * <p>Entries with a {@code null} key or a {@code null} entry itself are skipped. If {@code
     * deepCopy} is true, object and array values are deep copied before being added.
     *
     * @param stream the entries to add
     * @param deepCopy whether to deep copy object and array values
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(
        final Stream<Map.Entry<String, T>> stream, final boolean deepCopy) {
      Objects.requireNonNull(stream, "stream");
      stream.forEach(
          e -> {
            if (e == null || e.getKey() == null) {
              return;
            }
            addIfAbsent(e.getKey(), e.getValue(), deepCopy);
          });
      return this;
    }

    /**
     * Adds all entries from the stream without copying, skipping any whose key is already present.
     *
     * <p>Equivalent to calling {@link #addAllIfAbsent(Stream, boolean)} with {@code deepCopy =
     * false}.
     *
     * @param stream the entries to add
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(
        final Stream<Map.Entry<String, T>> stream) {
      return addAllIfAbsent(stream, false);
    }

    /**
     * Adds all entries from another {@link JsonObject}, skipping any whose key is already present.
     *
     * <p>If {@code deepCopy} is true, object and array values are deep copied before being added.
     *
     * @param object the source object
     * @param deepCopy whether to deep copy object and array values
     * @return this builder
     */
    default JsonObjectBuilder addAllIfAbsent(final JsonObject object, final boolean deepCopy) {
      Objects.requireNonNull(object, "object");
      return addAllIfAbsent(object.entrySet(), deepCopy);
    }

    /**
     * Adds all entries from another {@link JsonObject} without copying, skipping any whose key is
     * already present.
     *
     * <p>Equivalent to calling {@link #addAllIfAbsent(JsonObject, boolean)} with {@code deepCopy =
     * false}.
     *
     * @param object the source object
     * @return this builder
     */
    default JsonObjectBuilder addAllIfAbsent(final JsonObject object) {
      return addAllIfAbsent(object, false);
    }

    /**
     * Adds all string entries from the iterable, skipping any whose key is already present.
     *
     * <p>Each value is wrapped via {@link JsonBuilder#primitive(String)}. Entries with a {@code
     * null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @return this builder
     */
    default JsonObjectBuilder addAllStringsIfAbsent(
        final Iterable<Map.Entry<String, String>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, String> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        addIfAbsent(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all number entries from the iterable, skipping any whose key is already present.
     *
     * <p>Each value is wrapped via {@link JsonBuilder#primitive(Number)}. Entries with a {@code
     * null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @param <T> the number type
     * @return this builder
     */
    default <T extends Number> JsonObjectBuilder addAllNumbersIfAbsent(
        final Iterable<Map.Entry<String, T>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, T> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        addIfAbsent(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all boolean entries from the iterable, skipping any whose key is already present.
     *
     * <p>Each value is wrapped via {@link JsonBuilder#primitive(Boolean)}. Entries with a {@code
     * null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @return this builder
     */
    default JsonObjectBuilder addAllBooleansIfAbsent(
        final Iterable<Map.Entry<String, Boolean>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, Boolean> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        addIfAbsent(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Adds all character entries from the iterable, skipping any whose key is already present.
     *
     * <p>Each value is wrapped via {@link JsonBuilder#primitive(Character)}. Entries with a {@code
     * null} key or a {@code null} entry itself are skipped.
     *
     * @param iterable the entries to add
     * @return this builder
     */
    default JsonObjectBuilder addAllCharactersIfAbsent(
        final Iterable<Map.Entry<String, Character>> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Map.Entry<String, Character> e : iterable) {
        if (e == null || e.getKey() == null) {
          continue;
        }
        addIfAbsent(e.getKey(), e.getValue());
      }
      return this;
    }

    /**
     * Returns the built {@link JsonObject}.
     *
     * @return the built object
     */
    JsonObject build();
  }

  /** A {@link JsonArray} builder utility */
  public interface JsonArrayBuilder extends Consumer<JsonElement> {

    @Override
    default void accept(final JsonElement value) {
      add(value);
    }

    /**
     * Appends an element to the array.
     *
     * <p>If {@code copy} is true, {@link JsonObject} and {@link JsonArray} values are deep copied
     * before being added; otherwise the element is referenced directly.
     *
     * @param value the element to add, or {@code null} to add {@link JsonNull}
     * @param copy whether to deep copy object and array values
     * @return this builder
     */
    JsonArrayBuilder add(@Nullable JsonElement value, boolean copy);

    /**
     * Appends an element to the array without copying.
     *
     * <p>Equivalent to calling {@link #add(JsonElement, boolean)} with {@code copy = false}.
     *
     * @param value the element to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonArrayBuilder add(@Nullable final JsonElement value) {
      return add(value, false);
    }

    /**
     * Appends a string element, wrapping the value via {@link JsonBuilder#primitive(String)}.
     *
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonArrayBuilder add(@Nullable final String value) {
      return add(primitive(value));
    }

    /**
     * Appends a number element, wrapping the value via {@link JsonBuilder#primitive(Number)}.
     *
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonArrayBuilder add(@Nullable final Number value) {
      return add(primitive(value));
    }

    /**
     * Appends a boolean element, wrapping the value via {@link JsonBuilder#primitive(Boolean)}.
     *
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonArrayBuilder add(@Nullable final Boolean value) {
      return add(primitive(value));
    }

    /**
     * Appends a character element, wrapping the value via {@link JsonBuilder#primitive(Character)}.
     *
     * @param value the value to add, or {@code null} to add {@link JsonNull}
     * @return this builder
     */
    default JsonArrayBuilder add(@Nullable final Character value) {
      return add(primitive(value));
    }

    /**
     * Appends all elements from the iterable.
     *
     * <p>If {@code copy} is true, object and array values are deep copied before being added.
     *
     * @param iterable the elements to add
     * @param copy whether to deep copy object and array values
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonArrayBuilder addAll(
        final Iterable<T> iterable, final boolean copy) {
      Objects.requireNonNull(iterable, "iterable");
      for (final T e : iterable) {
        add(e, copy);
      }
      return this;
    }

    /**
     * Appends all elements from the iterable without copying.
     *
     * <p>Equivalent to calling {@link #addAll(Iterable, boolean)} with {@code copy = false}.
     *
     * @param iterable the elements to add
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonArrayBuilder addAll(final Iterable<T> iterable) {
      return addAll(iterable, false);
    }

    /**
     * Appends all elements from the stream.
     *
     * <p>If {@code copy} is true, object and array values are deep copied before being added.
     *
     * @param stream the elements to add
     * @param copy whether to deep copy object and array values
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonArrayBuilder addAll(
        final Stream<T> stream, final boolean copy) {
      Objects.requireNonNull(stream, "iterable");
      stream.forEach(e -> add(e, copy));
      return this;
    }

    /**
     * Appends all elements from the stream without copying.
     *
     * <p>Equivalent to calling {@link #addAll(Stream, boolean)} with {@code copy = false}.
     *
     * @param stream the elements to add
     * @param <T> the element type
     * @return this builder
     */
    default <T extends JsonElement> JsonArrayBuilder addAll(final Stream<T> stream) {
      return addAll(stream, false);
    }

    /**
     * Appends all strings from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(String)}.
     *
     * @param iterable the values to add
     * @return this builder
     */
    default JsonArrayBuilder addStrings(final Iterable<String> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final String e : iterable) {
        add(e);
      }
      return this;
    }

    /**
     * Appends all numbers from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(Number)}.
     *
     * @param iterable the values to add
     * @param <T> the number type
     * @return this builder
     */
    default <T extends Number> JsonArrayBuilder addNumbers(final Iterable<T> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final T e : iterable) {
        add(e);
      }
      return this;
    }

    /**
     * Appends all booleans from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(Boolean)}.
     *
     * @param iterable the values to add
     * @return this builder
     */
    default JsonArrayBuilder addBooleans(final Iterable<Boolean> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Boolean e : iterable) {
        add(e);
      }
      return this;
    }

    /**
     * Appends all characters from the iterable, wrapping each value via {@link
     * JsonBuilder#primitive(Character)}.
     *
     * @param iterable the values to add
     * @return this builder
     */
    default JsonArrayBuilder addCharacters(final Iterable<Character> iterable) {
      Objects.requireNonNull(iterable, "iterable");
      for (final Character e : iterable) {
        add(e);
      }
      return this;
    }

    /**
     * Returns the built {@link JsonArray}.
     *
     * @return the built array
     */
    JsonArray build();
  }

  private static final class JsonObjectBuilderImpl implements JsonObjectBuilder {

    private final JsonObject handle;

    private JsonObjectBuilderImpl(final JsonObject handle) {
      this.handle = handle;
    }

    @Override
    public JsonObjectBuilder add(
        final String property, @Nullable JsonElement value, final boolean copy) {
      Objects.requireNonNull(property, "property");
      if (value == null) {
        value = nullValue();
      }

      if (copy && value.isJsonObject()) {
        this.handle.add(property, object(value.getAsJsonObject(), true).build());
      } else if (copy && value.isJsonArray()) {
        this.handle.add(property, array(value.getAsJsonArray(), true).build());
      } else {
        this.handle.add(property, value);
      }
      return this;
    }

    @Override
    public JsonObjectBuilder addIfAbsent(
        final String property, @Nullable final JsonElement value, final boolean copy) {
      Objects.requireNonNull(property, "property");
      if (this.handle.has(property)) {
        return this;
      }
      return add(property, value, copy);
    }

    @Override
    public JsonObject build() {
      return this.handle;
    }
  }

  private static final class JsonArrayBuilderImpl implements JsonArrayBuilder {

    private final JsonArray handle;

    private JsonArrayBuilderImpl(final JsonArray handle) {
      this.handle = handle;
    }

    @Override
    public JsonArrayBuilder add(@Nullable JsonElement value, final boolean copy) {
      if (value == null) {
        value = nullValue();
      }

      if (copy && value.isJsonObject()) {
        this.handle.add(object(value.getAsJsonObject(), true).build());
      } else if (copy && value.isJsonArray()) {
        this.handle.add(array(value.getAsJsonArray(), true).build());
      } else {
        this.handle.add(value);
      }

      return this;
    }

    @Override
    public JsonArray build() {
      return this.handle;
    }
  }

  private JsonBuilder() {
    throw new UnsupportedOperationException("This class cannot be instantiated");
  }
}
