/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.calendar;

/**
 * It defines the property of an object of being potentially prioritized. By default,
 * a {@link Plannable} object isn't a priority; in such a case, it has a normal priority.
 * @author mmoquillon
 */
public interface Prioritized {

  /**
   * Sets a priority to this prioritized object.
   * @param priority a priority level.
   * @return itself.
   */
  Prioritized withPriority(Priority priority);

  /**
   * Gets the priority of this object.
   * @return a priority level.
   */
  Priority getPriority();

  /**
   * Is this object a priority?
   * @return true if this object has a priority level other than normal. False otherwise.
   */
  default boolean isPriority() {
    return getPriority() != Priority.NORMAL;
  }
}
