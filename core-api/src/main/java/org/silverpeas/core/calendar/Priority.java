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
package org.silverpeas.core.calendar;

/**
 * Priority of a {@link PlannableOnCalendar}. A priority is a set value from NORMAL (meaning no further
 * priority) to different level of prioritisation.
 * <p>
 * ICAL specifications : A CUA with a three-level priority scheme of "HIGH", "MEDIUM" and
 * "LOW" is mapped into this property such that a property value in the range of one (US-ASCII
 * decimal 49) to four (US-ASCII decimal 52) specifies "HIGH" priority. A value of five (US-ASCII
 * decimal 53) is the normal or "MEDIUM" priority. A value in the range of six (US- ASCII decimal
 * 54) to nine (US-ASCII decimal 58) is "LOW" priority.<br>
 * As Silverpeas Handles for now only HIGH or NORMAL, Silverpeas considers LOW as NORMAL.
 * </p>
 * @author mmoquillon
 */
public enum Priority {

  /**
   * A normal priority means in fact no priority. By default, all {@link PlannableOnCalendar} have a normal
   * priority.
   */
  NORMAL,

  /**
   * A high priority means the {@link PlannableOnCalendar} is a priority. High priority is the lowest
   * priority of a priority {@link PlannableOnCalendar}.
   */
  HIGH;

  private static final int HIGH_ICAL_LEVEL = 1;
  private static final int HIGH_ICAL_LEVEL_THRESHOLD = 4;

  /**
   * Computes the correct Priority instance from specified the priority level indicated as a number.
   * If the specified ordinal isn't valid, an {@link IllegalArgumentException} is thrown.
   * @param ordinal the priority level indicated as a positive number.
   * @return a Priority instance matching the specified level.
   */
  public static Priority valueOf(int ordinal) {
    if (ordinal == NORMAL.ordinal()) {
      return NORMAL;
    } else if (ordinal >= HIGH.ordinal()) {
      return HIGH;
    } else {
      throw new IllegalArgumentException("The specified ordinal, " + ordinal + ", isn't supported");
    }
  }

  /**
   * Gets the Silverpeas priority definition from given ICal priority level.
   * @param iCalLevel the ICal priority level.
   * @return the corresponding {@link Priority}.
   */
  public static Priority fromICalLevel(int iCalLevel) {
    return 0 < iCalLevel && iCalLevel <= HIGH_ICAL_LEVEL_THRESHOLD ? Priority.HIGH :
        Priority.NORMAL;
  }

  /**
   * Gets the ICal level according to the specifications and the Silverpeas rule.
   * @return ICal priority level.
   */
  public int getICalLevel() {
    return this == HIGH ? HIGH_ICAL_LEVEL : 0;
  }
}
