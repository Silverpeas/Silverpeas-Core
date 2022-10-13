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
package org.silverpeas.core.calendar;

import javax.persistence.Transient;

/**
 * This interface is dedicated to be applied on {@link PlannableOnCalendar} implementations. It qualifies
 * a {@link PlannableOnCalendar} can be recurrent, periodic, over the timeline. By default, a recurrent
 * {@link PlannableOnCalendar} has no defined recurrence rules; hence it is no recurrent.
 * @author mmoquillon
 */
public interface Recurrent {

  /**
   * Recurs this recurrent object with the specified recurrence rules.
   * @param recurrence the recurrence defining the recurring rules to apply in the planning of this
   * {@link PlannableOnCalendar}.
   * @return itself.
   */
  Recurrent recur(final Recurrence recurrence);

  /**
   * Gets the actual recurrence rules. If no recurrence has been set, then returns NO_RECURRENCE.
   * @return the actual recurrence of this {@link PlannableOnCalendar} or NO_RECURRENCE.
   */
  Recurrence getRecurrence();

  /**
   * Unset the recurrence of this possibly recurrent object. It is no more recurrent.
   */
  void unsetRecurrence();

  /**
   * Is this {@link PlannableOnCalendar} recurrent?
   * @return true if a recurrence has been set for this object, false otherwise.
   */
  @Transient
  default boolean isRecurrent() {
    return getRecurrence() != Recurrence.NO_RECURRENCE;
  }
}
