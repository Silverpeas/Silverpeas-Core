/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.memory;

import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;

import java.math.BigDecimal;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * User: Yohann Chastagnier
 * Date: 15/11/13
 */
public enum MemoryUnit {
  B(1, "o", "bytes"), KB(2, "ko", "Kb"), MB(3, "mo", "Mb"), GB(4, "go", "Gb"), TB(5, "to", "Tb");
  private final String bundleKey;
  private final String bundleDefault;
  private BigDecimal limit = null;
  private final int power;

  static BigDecimal byteMultiplier = new BigDecimal(String.valueOf(1024));

  MemoryUnit(int power, final String bundleKey, final String bundleDefault) {
    this.bundleKey = bundleKey;
    this.bundleDefault = bundleDefault;
    this.limit = null;
    this.power = power;
  }

  protected String getBundleKey() {
    return bundleKey;
  }

  protected String getBundleDefault() {
    return bundleDefault;
  }

  public String getLabel() {
    return defaultStringIfNotDefined(getStringTranslation(getBundleKey()), getBundleDefault());
  }

  public String getLabel(final String language) {
    return defaultStringIfNotDefined(getStringTranslation(getBundleKey(), language),
        getBundleDefault());
  }

  public BigDecimal getLimit() {
    if (limit == null) {
      limit = byteMultiplier.pow(power);
    }
    return limit;
  }

  public int getPower() {
    return power;
  }

  private static String getStringTranslation(final String key) {
    return getStringTranslation(key, MessageManager.getLanguage());
  }

  private static String getStringTranslation(final String key, final String language) {
    LocalizationBundle rl =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.util", language);
    return rl.getString(key);
  }
}
