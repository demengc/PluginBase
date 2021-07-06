/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
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

package dev.demeng.pluginbase.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for formatting {@link Duration} into a human-friendly format.
 */
public enum DurationFormatter {

  LONG,
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
   * Formats {@link Duration} into a human readable format.
   *
   * @param duration The duration
   * @return The formatted string
   */
  @NotNull
  public String format(final Duration duration) {

    long seconds = duration.getSeconds();
    int outputSize = 0;

    final StringBuilder output = new StringBuilder();

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
    final String s = unit.name().toLowerCase();
    return " " + s.substring(0, s.length() - 1);
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
