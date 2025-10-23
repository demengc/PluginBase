/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) lucko/helper contributors
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

package dev.demeng.pluginbase;

import com.google.common.collect.ImmutableMap;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for time/duration parsing and formatting.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Time {

  /**
   * The date format used for dates and times combined.
   */
  public static final ThreadLocal<DateFormat> DATE_TIME_FORMAT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat(
          BaseManager.getBaseSettings().dateTimeFormat()));

  /**
   * The date format used for dates.
   */
  public static final ThreadLocal<DateFormat> DATE_FORMAT =
      ThreadLocal.withInitial(() -> new SimpleDateFormat(
          BaseManager.getBaseSettings().dateFormat()));

  private static final Map<ChronoUnit, String> UNITS_PATTERNS = ImmutableMap.<ChronoUnit, String>builder()
      .put(ChronoUnit.YEARS, "y(?:ear)?s?")
      .put(ChronoUnit.MONTHS, "mo(?:nth)?s?")
      .put(ChronoUnit.WEEKS, "w(?:eek)?s?")
      .put(ChronoUnit.DAYS, "d(?:ay)?s?")
      .put(ChronoUnit.HOURS, "h(?:our|r)?s?")
      .put(ChronoUnit.MINUTES, "m(?:inute|in)?s?")
      .put(ChronoUnit.SECONDS, "(?:s(?:econd|ec)?s?)?")
      .build();

  private static final ChronoUnit[] UNITS = UNITS_PATTERNS.keySet().toArray(new ChronoUnit[0]);

  private static final String PATTERN_STRING = UNITS_PATTERNS.values().stream()
      .map(pattern -> "(?:(\\d+)\\s*" + pattern + "[,\\s]*)?")
      .collect(Collectors.joining());

  private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING, Pattern.CASE_INSENSITIVE);

  /**
   * Formats the date and time using {@link #DATE_TIME_FORMAT}.
   *
   * @param time The time to format
   * @return The formatted date and time
   */
  public static String formatDateTime(final long time) {
    final String pattern = BaseManager.getBaseSettings().dateTimeFormat();
    return refreshFormat(DATE_TIME_FORMAT, pattern).format(time);
  }

  /**
   * Formats the date using {@link #DATE_FORMAT}.
   *
   * @param time The time to format
   * @return The formatted date
   */
  public static String formatDate(final long time) {
    final String pattern = BaseManager.getBaseSettings().dateFormat();
    return refreshFormat(DATE_FORMAT, pattern).format(time);
  }

  /**
   * Converts the given duration string into milliseconds and wraps it inside an Optional. Supported
   * time units include years (y), months (mo), weeks (w), days (d), hours (h), minutes (m), and
   * seconds (s).
   *
   * @param input The duration string
   * @return The parsed duration
   * @throws IllegalArgumentException If the input could not be parsed
   */
  @NotNull
  public static Duration parse(@NotNull final String input) throws IllegalArgumentException {

    final Matcher matcher = PATTERN.matcher(input);

    while (matcher.find()) {
      if (matcher.group() == null || matcher.group().isEmpty()) {
        continue;
      }

      Duration duration = Duration.ZERO;

      for (int i = 0; i < UNITS.length; i++) {
        final ChronoUnit unit = UNITS[i];
        final int g = i + 1;

        if (matcher.group(g) != null && !matcher.group(g).isEmpty()) {
          final int n = Integer.parseInt(matcher.group(g));

          if (n > 0) {
            duration = duration.plus(unit.getDuration().multipliedBy(n));
          }
        }
      }

      return duration;
    }

    throw new IllegalArgumentException("Unable to parse duration: " + input);
  }

  /**
   * Attempts to parse a {@link Duration} and returns the result as an {@link Optional}-wrapped
   * object.
   *
   * @param input The input string
   * @return An Optional Duration
   * @see #parse(String)
   */
  @NotNull
  public static Optional<Duration> parseSafely(@NotNull final String input) {
    try {
      return Optional.of(parse(input));
    } catch (final IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }

  /**
   * Formats the duration using the selected formatter.
   *
   * @param formatter The formatter to use
   * @param duration  The duration to format
   * @return The formatted duration
   */
  public static String formatDuration(final DurationFormatter formatter, final long duration) {
    return formatter.format(Duration.ofMillis(duration));
  }

  /**
   * Converts the long timestamp into an SQL timestamp.
   *
   * @param timestamp The long timestamp
   * @return The equivalent timestamp, as one used by SQL
   */
  public static Timestamp toSqlTimestamp(final long timestamp) {
    return new Timestamp(new Date(timestamp).getTime());
  }

  /**
   * Converts the SQL timestamp into a long.
   *
   * @param timestamp The SQL timestamp
   * @return The equivalent timestamp, as a long
   */
  public static long fromSqlTimestamp(@NotNull final String timestamp) {
    return Timestamp.valueOf(timestamp).getTime();
  }

  private static void checkLimit(final String unit, final long value, final int limit) {
    if (value > limit) {
      throw new IllegalArgumentException(
          "Unit " + unit + " is out of bounds: " + value + " exceeds " + limit);
    }
  }

  private static SimpleDateFormat refreshFormat(
      final ThreadLocal<DateFormat> formatHolder,
      final String pattern) {

    final DateFormat dateFormat = formatHolder.get();

    if (!(dateFormat instanceof SimpleDateFormat)) {
      throw new IllegalStateException("Expected ThreadLocal to hold SimpleDateFormat");
    }

    final SimpleDateFormat format = (SimpleDateFormat) dateFormat;

    if (!format.toPattern().equals(pattern)) {
      format.applyPattern(pattern);
    }

    return format;
  }

  /**
   * The duration formatters.
   */
  public enum DurationFormatter {

    /**
     * The long format: 3 weeks 2 days 1 hour.
     */
    LONG(false, Integer.MAX_VALUE),
    /**
     * The concise format: 3w 2d 1h.
     */
    CONCISE(true, Integer.MAX_VALUE),
    /**
     * The concise, but low accuracy (maximum 3 time units) format.
     */
    CONCISE_LOW_ACCURACY(true, 3);

    private static final Unit[] UNITS = new Unit[]{
        new Unit(ChronoUnit.YEARS),
        new Unit(ChronoUnit.MONTHS),
        new Unit(ChronoUnit.WEEKS),
        new Unit(ChronoUnit.DAYS),
        new Unit(ChronoUnit.HOURS),
        new Unit(ChronoUnit.MINUTES),
        new Unit(ChronoUnit.SECONDS)
    };

    private final int accuracy;
    private final boolean concise;

    DurationFormatter(final boolean concise, final int accuracy) {
      this.concise = concise;
      this.accuracy = accuracy;
    }

    /**
     * Convenience method for accessing {@link #format(Duration, boolean, int)} through existing
     * enumeration implementations.
     *
     * @param duration the duration
     * @return the formatted string
     */
    public String format(final Duration duration) {
      return format(duration, concise, accuracy);
    }

    /**
     * Formats {@code duration} as a string, either in a {@code concise} (1 letter) or full length
     * format, displaying up to the specified number of {@code elements}.
     *
     * @param duration The duration
     * @param concise  If the output should be concisely formatted
     * @param elements The maximum number of elements to display
     * @return the formatted string
     */
    public static String format(final Duration duration, final boolean concise,
        final int elements) {
      long seconds = duration.getSeconds();
      final StringBuilder output = new StringBuilder();
      int outputSize = 0;

      for (final Unit unit : UNITS) {
        final long n = seconds / unit.duration;
        if (n > 0) {
          seconds -= unit.duration * n;
          output.append(' ').append(n).append(unit.toString(concise, n));
          outputSize++;
        }
        if (seconds <= 0 || outputSize >= elements) {
          break;
        }
      }

      if (output.length() == 0) {
        return "0" + (UNITS[UNITS.length - 1].toString(concise, 0));
      }
      return output.substring(1);
    }

    /**
     * Formats {@code duration} as a string, either in a {@code concise} (1 letter) or full length
     * format, displaying all possible elements.
     *
     * @param duration The duration
     * @param concise  If the output should be concisely formatted
     * @return The formatted string
     */
    public static String format(final Duration duration, final boolean concise) {
      return format(duration, concise, Integer.MAX_VALUE);
    }

    private static final class Unit {

      private final long duration;
      private final String formalStringPlural;
      private final String formalStringSingular;
      private final String conciseString;

      Unit(final ChronoUnit unit) {
        this.duration = unit.getDuration().getSeconds();
        this.formalStringPlural = " " + unit.name().toLowerCase();
        this.formalStringSingular =
            " " + unit.name().substring(0, unit.name().length() - 1).toLowerCase();
        this.conciseString = String.valueOf(Character.toLowerCase(unit.name().charAt(0)));
      }

      public String toString(final boolean concise, final long n) {
        if (concise) {
          return this.conciseString;
        }
        return n == 1 ? this.formalStringSingular : this.formalStringPlural;
      }
    }
  }
}
