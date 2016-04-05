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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.calendar;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.web.mvc.controller.PeasCoreException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.calendar.service.CalendarException;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.Classification;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.JSONCodec;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ludovic Bertin
 *
 */
public class OutlookSyncCalendarServlet extends HttpServlet {

  private static final int ELEMENT_ADDED = 1;
  private static final int ELEMENT_IGNORED = 0;
  private static final int ELEMENT_UPDATED = 2;
  private static final int SYNCHRO_ERROR = -1;
  private static final int NB_DAYS_BEFORE = 7;
  @Inject
  private SilverpeasCalendar calendarBm;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  public List<CalendarEntry> read(InputStream in) throws IOException {
    try {
      CalendarEntry[] entries = JSONCodec.decode(in, CalendarEntry[].class);
      return Arrays.asList(entries);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    String report = "";
    try {
      // read the serialized JournalHeader list from applet
      List<CalendarEntry> entries = read(new BufferedInputStream(request.getInputStream()));

      // continue the process for registering the student object
      int nbEventsUpdated = 0;
      int nbEventsAdded = 0;
      int nbErrors = 0;
      int nbDeleted = 0;
      Map<String, JournalHeader> existingEvents = null;

      setHeaderDelegators(entries);
      for (CalendarEntry element : entries) {
        if (existingEvents == null) {
          existingEvents = getExternalEvents(element.getDelegatorId());
        }
        switch ((synchronizeJournalHeader(element, existingEvents))) {
          case ELEMENT_ADDED:
            nbEventsAdded++;
            break;
          case ELEMENT_UPDATED:
            nbEventsUpdated++;
            break;
          case SYNCHRO_ERROR:
            nbErrors++;
            break;
          default:
            break;
        }
      }

      // delete no more existing events
      nbDeleted = cleanDeletedEvents(existingEvents);

      report = "";

      if (nbEventsAdded > 0) {
        report += nbEventsAdded + " Evenement(s) nouveau(x)\n";
      }
      if (nbEventsUpdated > 0) {
        report += nbEventsUpdated + " Evenement(s) mis a jour\n";
      }
      if (nbDeleted > 0) {
        report += nbDeleted + " Evenement(s) supprime(s)\n";
      }
      if (nbErrors > 0) {
        report += nbErrors + " erreur(s) rencontree(s)\n";
      }

    } catch (Exception e) {
      SilverTrace.error("importCalendar", "ImportCalendarServlet.doPost", "xx", e);
      report = "Erreur lors du traitement par Silverpeas";
    }

    // send back report to applet
    OutputStream out = response.getOutputStream();
    try {
      out.write(report.getBytes(Charsets.UTF_8));
      out.flush();
      out.close();
    } catch (IOException e) {
      SilverTrace.error("importCalendar", "ImportCalendarServlet.doPost", "xx", e);
    }
  }

  /**
   * For security purpose Journal Headers contains HTTP session id instead of user id directly. This
   * method asks SessionManager which user is using the given session id
   *
   * @param headers	Journal Headers
   * @throws	PeasCoreException if session Id is not valid or has expired
   */
  private void setHeaderDelegators(List<CalendarEntry> headers) throws PeasCoreException {
    String userId = null;
    for (CalendarEntry element : headers) {
      if (userId == null) {
        SessionInfo info = SessionManagementProvider.getSessionManagement()
            .getSessionInfo(element.getDelegatorId());
        userId = info.getUserDetail().getId();
      }
      element.setDelegatorId(userId);
    }
  }

  /**
   * Get all previously imported events (only event for which startdate greater today-7 days)
   *
   * @param userId	for security purpose, session id is given instead of user id directly
   * @return
   * @throws RemoteException
   * @throws ImportCalendarException
   */
  private Map<String, JournalHeader> getExternalEvents(String userId) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, -NB_DAYS_BEFORE);
    Collection<JournalHeader> existingEvents = calendarBm.getExternalJournalHeadersForUserAfterDate(
        userId, cal.getTime());
    Map<String, JournalHeader> events = new HashMap<String, JournalHeader>(existingEvents.size());
    for (JournalHeader event : existingEvents) {
      events.put(event.getExternalId(), event);
    }
    return events;
  }

  /**
   * Add or update given JournalHeader in Silverpeas
   *
   * @param element journalHeader to synchronize
   * @param existingEvents Hashtable with existing external events (previously imported)
   *
   * @return result of operation (ImportCalendarServlet.ELEMENT_ADDED,
   * ImportCalendarServlet.ELEMENT_UPDATED, ImportCalendarServlet.SYNCHRO_ERROR)
   */
  private int synchronizeJournalHeader(CalendarEntry element,
      Map<String, JournalHeader> existingEvents) {
    int result;

    try {
      // check if event already exists
      if (existingEvents.containsKey(element.getExternalId())) {
        JournalHeader oldJournal = existingEvents.get(element.getExternalId());
        if (areDifferent(oldJournal, element)) {
          element.setId(oldJournal.getId());
          calendarBm.updateJournal(convert(element));
          result = ELEMENT_UPDATED;
        } else {
          result = ELEMENT_IGNORED;
        }
        existingEvents.remove(element.getExternalId());
      } // new journal : create it
      else {
        calendarBm.addJournal(convert(element));
        result = ELEMENT_ADDED;
      }
    } catch (Exception e) {
      SilverTrace.error("importCalendar", "ImportCalendarServlet.synchronizeJournalHeader",
          "importCalendar.EX_SYNCHRO_JOURNAL_HEADER", e);
      result = SYNCHRO_ERROR;
    }

    return result;
  }

  private JournalHeader convert(CalendarEntry entry) throws ParseException, CalendarException {
    JournalHeader header = new JournalHeader(entry.getName(), entry.getDelegatorId());
    header.setDescription(entry.getDescription());
    header.setEndDay(entry.getEndDay());
    header.setStartDay(entry.getStartDay());
    header.setStartHour(entry.getStartHour());
    header.setEndHour(entry.getEndHour());
    header.setExternalId(entry.getExternalId());
    header.getPriority().setValue(entry.getPriority());
    header.getClassification().setString(entry.getClassification());
    return header;
  }

  /**
   * Check if event has changed
   *
   * @param oldJournal	event in Silverpeas
   * @param element	event from external application
   * @return
   */
  private boolean areDifferent(JournalHeader oldJournal, CalendarEntry element) {
    if (oldJournal.getClassification().isPrivate() != Classification.PRIVATE.equals(element
        .getClassification())) {
      return true;
    } else if (!oldJournal.getDescription().equals(element.getDescription())) {
      return true;
    } else if (!oldJournal.getEndDay().equals(element.getEndDay())) {
      return true;
    } else if (!oldJournal.getStartDay().equals(element.getStartDay())) {
      return true;
    } else if ((oldJournal.getStartHour() == null) && (element.getStartHour() != null)) {
      return true;
    } else if ((oldJournal.getStartHour() != null) && (element.getStartHour() == null)) {
      return true;
    } else if ((oldJournal.getStartHour() != null) && (element.getStartHour() != null)
        && (!oldJournal.getStartHour().equals(element.getStartHour()))) {
      return true;
    } else if ((oldJournal.getEndHour() != null) && (element.getEndHour() != null) && (!oldJournal
        .getEndHour().equals(element.getEndHour()))) {
      return true;
    } else if (!oldJournal.getName().equals(element.getName())) {
      return true;
    } else if (oldJournal.getPriority().getValue() != element.getPriority()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Remove from Silverpeas events that don't exist anymore in external application
   *
   * @param deletedEvents Hashtable with external events that exist in Silverpeas but no more in
   * external application
   *
   * @return the number of events deleted
   */
  private int cleanDeletedEvents(Map<String, JournalHeader> existingEvents) {
    int nbDeletedEvents = 0;
    if (existingEvents != null) {
      for (JournalHeader event : existingEvents.values()) {
        try {
          calendarBm.removeJournal(event.getId());
          nbDeletedEvents++;
        } catch (Exception e) {
          SilverTrace.error("importCalendar", "ImportCalendarServlet.cleanDeletedEvents",
              "importCalendar.EX_REMOVE_JOURNAL_HEADER", e);
        }
      }
    }
    return nbDeletedEvents;
  }
}
