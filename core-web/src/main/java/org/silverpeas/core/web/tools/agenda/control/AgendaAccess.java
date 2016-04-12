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
package org.silverpeas.core.web.tools.agenda.control;

import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.calendar.model.ParticipationStatus;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.Collection;
import java.util.Date;

public class AgendaAccess {

  private static SilverpeasCalendar silverpeasCalendar = null;

  private static SilverpeasCalendar getSilverpeasCalendar() throws AgendaException {
    if (silverpeasCalendar == null) {
      try {
        silverpeasCalendar = ServiceProvider.getService(SilverpeasCalendar.class);
      } catch (Exception e) {
        throw new AgendaException("AgendaAccess.getSilverpeasCalendar()", SilverpeasException.ERROR,
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return silverpeasCalendar;
  }

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @throws AgendaException
   * @see
   */
  static public boolean hasTentativeSchedulables(String userId) throws AgendaException {
    try {
      return getSilverpeasCalendar().hasTentativeSchedulablesForUser(userId);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.hasTentativeSchedulables()", SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + userId, e);
    }
  }

  static public Date getCurrentDay() {
    return new Date();
  }

  static public Collection<JournalHeader> getDaySchedulables(String userId) throws AgendaException {
    try {
      return getSilverpeasCalendar().getDaySchedulablesForUser(
          DateUtil.date2SQLDate(getCurrentDay()), userId,
          null, ParticipationStatus.ACCEPTED);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.getDaySchedulables()", SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + userId, e);
    }
  }

  static public Collection<JournalHeader> getNextDaySchedulables(String userId) throws
      AgendaException {
    try {
      return getSilverpeasCalendar().getNextDaySchedulablesForUser(
          DateUtil.date2SQLDate(getCurrentDay()), userId,
          null, ParticipationStatus.ACCEPTED);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.getDaySchedulables()", SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + userId, e);
    }
  }

  static public Collection<JournalHeader> getJournalHeadersForUserAfterDate(String userIdAgenda,
      Date startDate, int nbReturned) throws AgendaException {
    try {
      return getSilverpeasCalendar().getJournalHeadersForUserAfterDate(userIdAgenda, startDate,
          nbReturned);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.getJournalHeadersForUserAfterDate()",
          SilverpeasException.ERROR, "agenda.EX_CANT_GET_JOURNAL", "userId=" + userIdAgenda, e);
    }
  }
}