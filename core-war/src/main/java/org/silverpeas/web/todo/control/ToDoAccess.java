/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.todo.control;

import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.ToDoHeader;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.Collection;

public class ToDoAccess {

  private static SilverpeasCalendar calendarBm = null;

  private static SilverpeasCalendar getEJB() throws TodoException {
    if (calendarBm == null) {
      try {
        calendarBm = ServiceProvider.getService(SilverpeasCalendar.class);
      } catch (Exception e) {
        throw new TodoException("ToDoAccess.getEJB()", SilverpeasException.ERROR,
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }

  /**
   * getNotCompletedToDos
   * @param userId the user identifier
   * @return Collection of TodoHeaders
   * @throws TodoException
   */
  static public Collection<ToDoHeader> getNotCompletedToDos(String userId) throws TodoException {

    try {
      return getEJB().getNotCompletedToDosForUser(userId);
    } catch (Exception e) {
      throw new TodoException("ToDoAccess.getNotCompletedToDos()", SilverpeasException.ERROR,
          "todo.MSG_CANT_GET_ENDED_TODOS", e);
    }
  }
}