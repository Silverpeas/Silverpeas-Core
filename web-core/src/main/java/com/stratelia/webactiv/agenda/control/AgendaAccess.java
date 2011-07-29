/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.agenda.control;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.ParticipationStatus;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 *
 * $Id: AgendaAccess.java,v 1.5 2008/03/12 16:37:44 neysseri Exp $
 *
 * $Log: AgendaAccess.java,v $
 * Revision 1.5  2008/03/12 16:37:44  neysseri
 * no message
 *
 * Revision 1.4.4.1  2008/01/09 09:39:20  cbonin
 * ajout ref RssServlet
 *
 * Revision 1.4  2006/10/18 08:32:34  sfariello
 * Modif du pavé "Aujourd'hui" -> "Mes prochains évènements"
 *
 * Revision 1.3  2005/09/30 14:15:58  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.2  2004/12/22 15:18:31  neysseri
 * Possibilité d'indiquer les jours non sélectionnables
 * + nettoyage sources
 * + précompilation jsp
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.1  2002/01/21 17:34:58  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 *
 */

/**
 * Class declaration
 * @author
 */
public class AgendaAccess {
  static private CalendarBm calendarBm = null;

  /**
   * getEJB
   * @return instance of CalendarBmHome
   */
  static private CalendarBm getEJB() throws AgendaException {
    if (calendarBm == null) {
      try {
        calendarBm = ((CalendarBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class)).create();
      } catch (Exception e) {
        throw new AgendaException("AgendaAccess.getEJB()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @throws AgendaException
   * @see
   */
  static public boolean hasTentativeSchedulables(String userId)
      throws AgendaException {
    try {
      return getEJB().hasTentativeSchedulablesForUser(userId);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.hasTentativeSchedulables()",
          SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + userId, e);
    }
  }

  static public Date getCurrentDay() {
    return new Date();
  }

  static public Collection<JournalHeader> getDaySchedulables(String userId)
      throws AgendaException {
    try {
      return getEJB().getDaySchedulablesForUser(
          DateUtil.date2SQLDate(getCurrentDay()), userId, null,
          ParticipationStatus.ACCEPTED);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.getDaySchedulables()",
          SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + userId, e);
    }
  }

  static public Collection<JournalHeader> getNextDaySchedulables(String userId)
      throws AgendaException {
    try {
      return getEJB().getNextDaySchedulablesForUser(
          DateUtil.date2SQLDate(getCurrentDay()), userId, null,
          ParticipationStatus.ACCEPTED);
    } catch (Exception e) {
      throw new AgendaException("AgendaAccess.getDaySchedulables()",
          SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + userId, e);
    }
  }

  static public Collection<JournalHeader> getJournalHeadersForUserAfterDate(
      String userIdAgenda, java.util.Date startDate, int nbReturned)
      throws AgendaException {
    try {
      return getEJB().getJournalHeadersForUserAfterDate(
          userIdAgenda, startDate, nbReturned);
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaAccess.getJournalHeadersForUserAfterDate()",
          SilverpeasException.ERROR, "agenda.EX_CANT_GET_JOURNAL", "userId="
          + userIdAgenda, e);
    }
  }
}