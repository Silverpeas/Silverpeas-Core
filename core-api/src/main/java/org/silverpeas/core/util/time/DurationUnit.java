/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.time;

import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * Technical enum used by {@link Duration} class.
 */
enum DurationUnit {
  MILLI("ms"), SEC("s"), MIN("m"), HOUR("h"), DAY("d"), WEEK("w"), MONTH("M"), YEAR("y");
  private final String bundleDefault;

  /**
   * The complete conversion board.
   */
  private static final Map<DurationConversionBoardKey, BigDecimal> conversionBoard = new HashMap<>();

  static {
    // From milliseconds
    conversionBoard.put(new DurationConversionBoardKey(MILLI, SEC), new BigDecimal("1000"));
    conversionBoard.put(new DurationConversionBoardKey(MILLI, MIN),
        new BigDecimal("1000").multiply(new BigDecimal("60")));
    conversionBoard.put(new DurationConversionBoardKey(MILLI, HOUR),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60")));
    conversionBoard.put(new DurationConversionBoardKey(MILLI, DAY),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")));
    conversionBoard.put(new DurationConversionBoardKey(MILLI, WEEK),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")).multiply(new BigDecimal("7")));
    conversionBoard.put(new DurationConversionBoardKey(MILLI, MONTH),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")).multiply(
            new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new DurationConversionBoardKey(MILLI, YEAR),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")).multiply(new BigDecimal("365")));

    // From seconds
    conversionBoard.put(new DurationConversionBoardKey(SEC, MIN), new BigDecimal("60"));
    conversionBoard.put(new DurationConversionBoardKey(SEC, HOUR),
        new BigDecimal("60").multiply(new BigDecimal("60")));
    conversionBoard.put(new DurationConversionBoardKey(SEC, DAY),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24")));
    conversionBoard.put(new DurationConversionBoardKey(SEC, WEEK),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24"))
            .multiply(new BigDecimal("7")));
    conversionBoard.put(new DurationConversionBoardKey(SEC, MONTH),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24")).multiply(
            new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new DurationConversionBoardKey(SEC, YEAR),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24"))
            .multiply(new BigDecimal("365")));

    // From minutes
    conversionBoard.put(new DurationConversionBoardKey(MIN, HOUR), new BigDecimal("60"));
    conversionBoard.put(new DurationConversionBoardKey(MIN, DAY),
        new BigDecimal("60").multiply(new BigDecimal("24")));
    conversionBoard.put(new DurationConversionBoardKey(MIN, WEEK),
        new BigDecimal("60").multiply(new BigDecimal("24")).multiply(new BigDecimal("7")));
    conversionBoard.put(new DurationConversionBoardKey(MIN, MONTH),
        new BigDecimal("60").multiply(new BigDecimal("24")).multiply(
            new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new DurationConversionBoardKey(MIN, YEAR),
        new BigDecimal("60").multiply(new BigDecimal("24")).multiply(new BigDecimal("365")));

    // From hours
    conversionBoard.put(new DurationConversionBoardKey(HOUR, DAY), new BigDecimal("24"));
    conversionBoard.put(new DurationConversionBoardKey(HOUR, WEEK),
        new BigDecimal("24").multiply(new BigDecimal("7")));
    conversionBoard.put(new DurationConversionBoardKey(HOUR, MONTH), new BigDecimal("24").multiply(
        new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new DurationConversionBoardKey(HOUR, YEAR),
        new BigDecimal("24").multiply(new BigDecimal("365")));

    // From days
    conversionBoard.put(new DurationConversionBoardKey(DAY, WEEK), new BigDecimal("7"));
    conversionBoard.put(new DurationConversionBoardKey(DAY, MONTH),
        new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN));
    conversionBoard.put(new DurationConversionBoardKey(DAY, YEAR), new BigDecimal("365"));

    // From weeks
    conversionBoard.put(new DurationConversionBoardKey(WEEK, MONTH),
        new BigDecimal("52").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN));
    conversionBoard.put(new DurationConversionBoardKey(WEEK, YEAR), new BigDecimal("52"));

    // From months
    conversionBoard.put(new DurationConversionBoardKey(MONTH, YEAR), new BigDecimal("12"));
  }

  DurationUnit(final String bundleDefault) {
    this.bundleDefault = bundleDefault;
  }

  protected String getBundleKey() {
    return name();
  }

  protected String getBundleDefault() {
    return bundleDefault;
  }

  public String getLabel() {
    return defaultStringIfNotDefined(getStringTranslation(getBundleKey()), getBundleDefault());
  }

  public BigDecimal getMultiplier(DurationUnit to) {
    return conversionBoard.get(new DurationConversionBoardKey(this, to));
  }

  /**
   * Gets the translation of an element
   * @param key the key
   * @return the right translation.
   */
  private static String getStringTranslation(final String key) {
    String language = MessageManager.getLanguage();
    LocalizationBundle localizedUnits =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.util", language);
    return localizedUnits.getString(key);
  }

  /**
   * Gets the duration unit corresponding to a given {@link TimeUnit}.
   * @param unit a {@link TimeUnit} instance.
   * @return a {@link DurationUnit} instance.
   */
  static DurationUnit from(TimeUnit unit) {
    return DurationUnit.values()[unit.ordinal()];
  }
}
