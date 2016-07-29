/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.calendar;

/**
 * Priority of a {@link Plannable}. A priority is a set value from NORMAL (meaning no further
 * priority) to different level of prioritisation.
 * @author mmoquillon
 */
public enum Priority {

  /**
   * A normal priority means in fact no priority. By default, all {@link Plannable} have a normal
   * priority.
   */
  NORMAL,

  /**
   * A high priority means the {@link Plannable} is a priority. High priority is the lowest
   * priority of a priority {@link Plannable}.
   */
  HIGH;

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
}
