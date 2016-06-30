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

package org.silverpeas.core.personalorganizer.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.personalorganizer.service.CalendarException;

import java.io.Serializable;

public class Priority implements Serializable, Comparable<Priority> {

  private static final long serialVersionUID = -5520032042704711631L;
  private static final int[] PRIORITIES = { 0, 1, 2, 3 };

  /**
   * The minimum allowable priority value. Please note that this refers to the case where no
   * priority is set. This case happens to be represented by the value 0 which is the lowest integer
   * value allowable.
   */
  public static final int MINIMUM_PRIORITY = 0;

  /**
   * The maximum allowable priority value. Please note that this refers to the highest possible
   * integer value for priority. When interpreting this value it is seen as the lowest priority
   * because the value 1 is the highest priority value.
   */
  public static final int MAXIMUM_PRIORITY = 9;

  private int priority = 2;

  public static int[] getAllPriorities() {
    return PRIORITIES;
  }

  /**
   * This is the default constructor. It is used by Castor. You should probably use the constructor
   * that takes an integer argument in your application code.
   */
  public Priority() {
    // for Castor
  }

  /**
   * The purpose of this method is to create a new priority property with the given initial value.

   *
   * @param newval The initial value of the priority property
   * @throws CalendarException
   */
  public Priority(int newval) throws CalendarException {
    setValue(newval);
  }

  /**
   * The purpose of this method is to set the value of the priority property.
   * @param newval The new value for the priority property
   */

  public final void setValue(int newval) throws CalendarException {
    if (newval > MAXIMUM_PRIORITY) {
      priority = MAXIMUM_PRIORITY;
    } else if (newval < MINIMUM_PRIORITY) {
      priority = MINIMUM_PRIORITY;
    } else {
      priority = newval;
    }
  }

  public int getValue() {
    return priority;
  }

  @Override
  public int compareTo(Priority other) {
    if (other == null) {
      return 1;
    }
    if ((getValue() == 0) && (other.getValue() != 0)) {
      return -1;
    }
    if ((getValue() != 0) && (other.getValue() == 0)) {
      return 1;
    }
    return other.getValue() - getValue();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Priority)) {
      return false;
    }
    return compareTo((Priority) o) == 0;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getValue()).toHashCode();
  }
}
