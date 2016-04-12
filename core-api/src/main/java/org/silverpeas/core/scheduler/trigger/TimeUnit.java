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

package org.silverpeas.core.scheduler.trigger;

/**
 * The unit of times as used and supported by the scheduling system in Silverpeas.
 */
public enum TimeUnit {

  /**
   * The second.
   */
  SECOND(1000),
  /**
   * The minute.
   */
  MINUTE(60000),
  /**
   * The hour.
   */
  HOUR(3600000);

  /**
   * How many milliseconds this unit of time is made.
   * @return the conversion in milliseconds of this unit of time.
   */
  public long inMilliSeconds() {
    return this.duration;
  }

  /**
   * Creates a TimeUnit instance by specifying the duration of this unit in milliseconds.
   * @param duration the duration the unit takes in milliseconds.
   */
  private TimeUnit(int duration) {
    this.duration = duration;
  }

  private int duration;
}
