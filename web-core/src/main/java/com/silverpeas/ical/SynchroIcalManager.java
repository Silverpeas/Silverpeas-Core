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

package com.silverpeas.ical;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.agenda.control.AgendaRuntimeException;
import com.stratelia.webactiv.agenda.control.AgendaSessionController;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.io.File;
import java.net.URL;

/**
 * @author dle
 */
public class SynchroIcalManager {

  public final static String SYNCHRO_SUCCEEDED = "0";
  private AgendaSessionController agendaSessionController;
  private CalendarBm calendarBm;

  public SynchroIcalManager(AgendaSessionController agendaSessionController) {
    this.agendaSessionController = agendaSessionController;
    setCalendarBm();
  }

  /**
   * Import remote calendar into Silverpeas calendar (update event if necessary)
   * @param urlCalendar
   * @param localCalendar
   * @param loginICalendar
   * @param pwdIcalendar
   * @return ReturnCode
   */
  public String synchroIcalAgenda(URL urlCalendar, File localCalendar,
      String loginICalendar, String pwdIcalendar) {
    String returnCodeSynchro = AgendaSessionController.SYNCHRO_FAILED;
    try {
      // Private iCal URL
      // Use the SyncEngine.listCalendars() method to get URLs
      URL remoteCalendar = urlCalendar;
      // Creates a synchronizer engine
      SyncEngine engine = new SyncEngine();
      // Do the synchronization :
      // Remote Calendar -> localfile Calendar
      String remoteConnect = engine.synchronize(localCalendar, remoteCalendar,
          loginICalendar, pwdIcalendar);
      if (SyncEngine.REMOTE_CONNECT_SUCCEEDED.equals(remoteConnect)) {
        // localfile -> Silverpeas Agenda
        ImportIcalManager impIcalManager = new ImportIcalManager(
            agendaSessionController);
        String returnImport = impIcalManager.importIcalAgenda(localCalendar);
        if (returnImport.equals(AgendaSessionController.IMPORT_FAILED)) {
          returnCodeSynchro = AgendaSessionController.SYNCHRO_FAILED;
        }
        else {
          returnCodeSynchro = AgendaSessionController.SYNCHRO_SUCCEEDED;
        }
      } else {
        returnCodeSynchro = remoteConnect;
      }
    } catch (Exception e) {
      SilverTrace.error("agenda", "SynchroIcalManager.synchroIcalAgenda()", "",
          e.fillInStackTrace());
    }
    return returnCodeSynchro;
  }

  /**
   * Method declaration
   * @see
   */
  private void setCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = EJBUtilitaire.getEJBObjectRef(
            JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class).create();
      } catch (Exception e) {
        throw new AgendaRuntimeException("ImportIcalManager.setCalendarBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }
}