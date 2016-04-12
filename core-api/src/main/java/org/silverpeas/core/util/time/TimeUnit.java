/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * User: Yohann Chastagnier
 * Date: 15/11/13
 */ /* Byte, Kilo-Byte, Mega-Byte, ... */
public enum TimeUnit {
  MILLI("ms"), SEC("s"), MIN("m"), HOUR("h"), DAY("d"), WEEK("w"), MONTH("M"), YEAR("y");
  private final String bundleDefault;

  /**
   * The complete conversion board.
   */
  private final static Map<TimeConversionBoardKey, BigDecimal> conversionBoard = new HashMap<>();

  static {
    // From milliseconds
    conversionBoard.put(new TimeConversionBoardKey(MILLI, SEC), new BigDecimal("1000"));
    conversionBoard.put(new TimeConversionBoardKey(MILLI, MIN),
        new BigDecimal("1000").multiply(new BigDecimal("60")));
    conversionBoard.put(new TimeConversionBoardKey(MILLI, HOUR),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60")));
    conversionBoard.put(new TimeConversionBoardKey(MILLI, DAY),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")));
    conversionBoard.put(new TimeConversionBoardKey(MILLI, WEEK),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")).multiply(new BigDecimal("7")));
    conversionBoard.put(new TimeConversionBoardKey(MILLI, MONTH),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")).multiply(
            new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new TimeConversionBoardKey(MILLI, YEAR),
        new BigDecimal("1000").multiply(new BigDecimal("60")).multiply(new BigDecimal("60"))
            .multiply(new BigDecimal("24")).multiply(new BigDecimal("365")));

    // From seconds
    conversionBoard.put(new TimeConversionBoardKey(SEC, MIN), new BigDecimal("60"));
    conversionBoard.put(new TimeConversionBoardKey(SEC, HOUR),
        new BigDecimal("60").multiply(new BigDecimal("60")));
    conversionBoard.put(new TimeConversionBoardKey(SEC, DAY),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24")));
    conversionBoard.put(new TimeConversionBoardKey(SEC, WEEK),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24"))
            .multiply(new BigDecimal("7")));
    conversionBoard.put(new TimeConversionBoardKey(SEC, MONTH),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24")).multiply(
            new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new TimeConversionBoardKey(SEC, YEAR),
        new BigDecimal("60").multiply(new BigDecimal("60")).multiply(new BigDecimal("24"))
            .multiply(new BigDecimal("365")));

    // From minutes
    conversionBoard.put(new TimeConversionBoardKey(MIN, HOUR), new BigDecimal("60"));
    conversionBoard.put(new TimeConversionBoardKey(MIN, DAY),
        new BigDecimal("60").multiply(new BigDecimal("24")));
    conversionBoard.put(new TimeConversionBoardKey(MIN, WEEK),
        new BigDecimal("60").multiply(new BigDecimal("24")).multiply(new BigDecimal("7")));
    conversionBoard.put(new TimeConversionBoardKey(MIN, MONTH),
        new BigDecimal("60").multiply(new BigDecimal("24")).multiply(
            new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new TimeConversionBoardKey(MIN, YEAR),
        new BigDecimal("60").multiply(new BigDecimal("24")).multiply(new BigDecimal("365")));

    // From hours
    conversionBoard.put(new TimeConversionBoardKey(HOUR, DAY), new BigDecimal("24"));
    conversionBoard.put(new TimeConversionBoardKey(HOUR, WEEK),
        new BigDecimal("24").multiply(new BigDecimal("7")));
    conversionBoard.put(new TimeConversionBoardKey(HOUR, MONTH), new BigDecimal("24").multiply(
        new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN)));
    conversionBoard.put(new TimeConversionBoardKey(HOUR, YEAR),
        new BigDecimal("24").multiply(new BigDecimal("365")));

    // From days
    conversionBoard.put(new TimeConversionBoardKey(DAY, WEEK), new BigDecimal("7"));
    conversionBoard.put(new TimeConversionBoardKey(DAY, MONTH),
        new BigDecimal("365").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN));
    conversionBoard.put(new TimeConversionBoardKey(DAY, YEAR), new BigDecimal("365"));

    // From weeks
    conversionBoard.put(new TimeConversionBoardKey(WEEK, MONTH),
        new BigDecimal("52").divide(new BigDecimal("12"), 30, BigDecimal.ROUND_HALF_DOWN));
    conversionBoard.put(new TimeConversionBoardKey(WEEK, YEAR), new BigDecimal("52"));

    // From months
    conversionBoard.put(new TimeConversionBoardKey(MONTH, YEAR), new BigDecimal("12"));
  }

  TimeUnit(final String bundleDefault) {
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

  public BigDecimal getMultiplier(TimeUnit to) {
    return conversionBoard.get(new TimeConversionBoardKey(this, to));
  }

  /**
   * Gets the translation of an element
   * @param key
   * @return
   */
  private static String getStringTranslation(final String key) {
    String language = MessageManager.getLanguage();
    LocalizationBundle localizedUnits =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.util", language);
    return localizedUnits.getString(key);
  }

}
