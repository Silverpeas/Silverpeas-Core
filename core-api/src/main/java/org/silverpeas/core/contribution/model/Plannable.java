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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.model;

import org.silverpeas.core.date.Period;

/**
 * An interface that indicates a resource or a contribution is plannable in the time like an event
 * or a task for example.
 * @author mmoquillon
 */
public interface Plannable {

  /**
   * Gets the lapse of time on which this object is spanning.
   * @return the period of time during which this object takes place.
   */
  Period getPeriod();

  /**
   * Sets the lapse of time on which this object is spanning.
   * @param period the period of time during which this object takes place.
   */
  void setPeriod(final Period period);
}
