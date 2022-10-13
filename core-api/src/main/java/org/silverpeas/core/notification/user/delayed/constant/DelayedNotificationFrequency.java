/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.delayed.constant;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The different recurrence rule for a delayed notification. It is used for a users having chosen
 * to be notified regularly at once instead of at each notification sent.
 * @author Yohann Chastagnier
 */
public enum DelayedNotificationFrequency {
  NONE("N"), DAILY("D"), WEEKLY("W"), MONTHLY("M");

  private String code;

  DelayedNotificationFrequency(final String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name();
  }

  @Override
  public String toString() {
    return code;
  }

  public static DelayedNotificationFrequency decode(String code) {
    DelayedNotificationFrequency result = null;
    if (code != null) {
      code = code.trim();
      if (NONE.code.equals(code)) {
        result = NONE;
      } else if (DAILY.code.equals(code)) {
        result = DAILY;
      } else if (WEEKLY.code.equals(code)) {
        result = WEEKLY;
      } else if (MONTHLY.code.equals(code)) {
        result = MONTHLY;
      }
    }
    return result;
  }

  /**
   * Transforming the given collection into a collection of code, each of them representing
   * a frequency..
   * @param delayedNotificationFrequencies the different frequencies to encode in String.
   * @return a collection of a string representation of the specified frequencies.
   */
  public static Collection<String> toCodes(
      final Collection<DelayedNotificationFrequency> delayedNotificationFrequencies) {
    final Collection<String> result = new ArrayList<>();
    if (delayedNotificationFrequencies != null) {
      for (final DelayedNotificationFrequency delayedNotificationFrequency : delayedNotificationFrequencies) {
        result.add(delayedNotificationFrequency.getCode());
      }
    }
    return result;
  }
}
