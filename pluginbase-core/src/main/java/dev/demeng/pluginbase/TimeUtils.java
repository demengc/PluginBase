/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
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

import dev.demeng.pluginbase.plugin.BaseManager;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for time/duration parsing and formatting.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {

  /**
   * The date format used for dates and times combined.
   */
  public static final ThreadLocal<DateFormat> DATE_TIME_FORMAT = ThreadLocal.withInitial(
      () -> new SimpleDateFormat(BaseManager.getBaseSettings().dateTimeFormat()));

  /**
   * The date format used for dates.
   */
  public static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(
      () -> new SimpleDateFormat(BaseManager.getBaseSettings().dateFormat()));

  private static final Pattern UNITS_PATTERN = Pattern.compile(
      "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?"
          + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
          + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?"
          + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
          + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?"
          + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
          + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?",
      Pattern.CASE_INSENSITIVE);

  /**
   * Formats the date and time using {@link #DATE_TIME_FORMAT}.
   *
   * @param time The time to format
   * @return The formatted date and time
   */
  public static String formatDateTime(final long time) {
    return DATE_TIME_FORMAT.get().format(time);
  }

  /**
   * Formats the date using {@link #DATE_FORMAT}.
   *
   * @param time The time to format
   * @return The formatted date
   */
  public static String formatDate(final long time) {
    return DATE_FORMAT.get().format(time);
  }

  /**
   * Convert the given duration string into milliseconds. Supported time units include years (y),
   * months (mo), weeks (w), days (d), hours (h), minutes (m), and seconds (s).
   *
   * @param strDuration The duration string
   * @return The parsed duration, in milliseconds
   * @throws IllegalArgumentException If the duration cannot be parsed
   * @author Matej Pacan (Mineacademy.org)
   */
  public static long parseDuration(final String strDuration) {

    final Matcher matcher = UNITS_PATTERN.matcher(strDuration);

    long years = 0;
    long months = 0;
    long weeks = 0;
    long days = 0;
    long hours = 0;
    long minutes = 0;
    long seconds = 0;
    boolean found = false;

    while (matcher.find()) {

      if (matcher.group() == null || matcher.group().isEmpty()) {
        continue;
      }

      for (int i = 0; i < matcher.groupCount(); i++) {
        if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
          found = true;
          break;
        }
      }

      if (found) {
        for (int i = 1; i < 8; i++) {
          if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
            final long output = Long.parseLong(matcher.group(i));

            if (i == 1) {
              checkLimit("years", output, 10);
              years = output;

            } else if (i == 2) {
              checkLimit("months", output, 12 * 100);
              months = output;

            } else if (i == 3) {
              checkLimit("weeks", output, 4 * 100);
              weeks = output;

            } else if (i == 4) {
              checkLimit("days", output, 31 * 100);
              days = output;

            } else if (i == 5) {
              checkLimit("hours", output, 24 * 100);
              hours = output;

            } else if (i == 6) {
              checkLimit("minutes", output, 60 * 100);
              minutes = output;

            } else {
              checkLimit("seconds", output, 60 * 100);
              seconds = output;
            }
          }
        }

        break;
      }
    }

    if (!found) {
      throw new NumberFormatException("Could not parse duration: " + strDuration);
    }

    return (seconds + (minutes * 60) + (hours * 3600) + (days * 86400)
        + (weeks * 7 * 86400) + (months * 30 * 86400) + (years * 365 * 86400)) * 1000;
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

  /**
   * The duration formatters.
   */
  public enum DurationFormatter {

    /**
     * The long format: 3 weeks 2 days 1 hour.
     */
    LONG,

    /**
     * The concise format: 3w 2d 1h.
     */
    CONCISE {
      @Override
      protected String formatUnitPlural(final ChronoUnit unit) {
        return String.valueOf(Character.toLowerCase(unit.name().charAt(0)));
      }

      @Override
      protected String formatUnitSingular(final ChronoUnit unit) {
        return formatUnitPlural(unit);
      }
    },

    /**
     * The concise, but low accuracy (maximum 3 time units) format.
     */
    CONCISE_LOW_ACCURACY(3) {
      @Override
      protected String formatUnitPlural(final ChronoUnit unit) {
        return String.valueOf(Character.toLowerCase(unit.name().charAt(0)));
      }

      @Override
      protected String formatUnitSingular(final ChronoUnit unit) {
        return formatUnitPlural(unit);
      }
    };

    private final Unit[] units = new Unit[]{
        new Unit(ChronoUnit.YEARS),
        new Unit(ChronoUnit.MONTHS),
        new Unit(ChronoUnit.WEEKS),
        new Unit(ChronoUnit.DAYS),
        new Unit(ChronoUnit.HOURS),
        new Unit(ChronoUnit.MINUTES),
        new Unit(ChronoUnit.SECONDS)
    };

    private final int accuracy;

    DurationFormatter() {
      this(Integer.MAX_VALUE);
    }

    DurationFormatter(final int accuracy) {
      this.accuracy = accuracy;
    }

    /**
     * Formats {@code duration} as a string.
     *
     * @param duration The duration
     * @return The formatted string
     */
    public String format(final Duration duration) {
      long seconds = duration.getSeconds();
      final StringBuilder output = new StringBuilder();
      int outputSize = 0;

      for (final Unit unit : this.units) {
        final long n = seconds / unit.duration;
        if (n > 0) {
          seconds -= unit.duration * n;
          output.append(' ').append(n).append(unit.toString(n));
          outputSize++;
        }
        if (seconds <= 0 || outputSize >= this.accuracy) {
          break;
        }
      }

      if (output.length() == 0) {
        return "0" + this.units[this.units.length - 1].stringPlural;
      }
      return output.substring(1);
    }

    protected String formatUnitPlural(final ChronoUnit unit) {
      return " " + unit.name().toLowerCase();
    }

    protected String formatUnitSingular(final ChronoUnit unit) {
      final String str = unit.name().toLowerCase();
      return " " + str.substring(0, str.length() - 1);
    }

    private final class Unit {

      private final long duration;
      private final String stringPlural;
      private final String stringSingular;

      Unit(final ChronoUnit unit) {
        this.duration = unit.getDuration().getSeconds();
        this.stringPlural = formatUnitPlural(unit);
        this.stringSingular = formatUnitSingular(unit);
      }

      public String toString(final long n) {
        return n == 1 ? this.stringSingular : this.stringPlural;
      }
    }
  }
}
