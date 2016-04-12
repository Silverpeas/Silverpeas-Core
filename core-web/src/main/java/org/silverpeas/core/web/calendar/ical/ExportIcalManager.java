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
package org.silverpeas.core.web.calendar.ical;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.date.Datable;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.importexport.ExporterProvider;
import org.silverpeas.core.importexport.ical.ExportableCalendar;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.tools.agenda.control.AgendaException;
import org.silverpeas.core.web.tools.agenda.control.AgendaRuntimeException;
import org.silverpeas.core.web.tools.agenda.control.AgendaSessionController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.Attendee;
import org.silverpeas.core.calendar.model.Category;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.calendar.model.ParticipationStatus;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;

import java.io.FileWriter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.core.calendar.CalendarEvent.anEventAt;

/**
 * @author dle
 */
public class ExportIcalManager {

  private SilverpeasCalendar calendarBm;
  private static final String firstDate = "1970/01/01";
  private static final String lastDate = "2050/01/01";
  private String userId;
  private String language = "fr";

  public ExportIcalManager(AgendaSessionController agendaSessionController) {
    this.userId = agendaSessionController.getUserId();
    this.language = agendaSessionController.getLanguage();
    setCalendarBm();
  }

  /**
   * Constructs a new manager of iCal export processes for the agenda core component.
   *
   * @param userId the unique identifier of the user for which the manager has to be instanciated.
   */
  public ExportIcalManager(String userId) {
    this.userId = userId;
    setCalendarBm();
  }

  /**
   * Gets the unique identifier of the user to which this manager belongs.
   *
   * @return the user identifier.
   */
  private String getUserId() {
    return this.userId;
  }

  /**
   * Gets the language used by the user in Silverpeas.
   *
   * @return the user language.
   */
  private String getLanguage() {
    return this.language;
  }

  /**
   * Exports in iCal the events of the user agenda between the specified interval. Actually, it is
   * the event occurrences that are exported in the iCal file. This iCal file name is built
   * withWriter the user identifier and it the file is generated in the temporary directory.
   *
   * @param startDate the start date of the interval.
   * @param endDate the end date of the interval.
   * @return an export status code indicating if it has been successfull or not.
   * @throws AgendaException if an unexpected error occurs while exporting the events.
   */
  public String exportIcalAgenda(String startDate, String endDate) throws AgendaException {
    String returnCode = AgendaSessionController.EXPORT_SUCCEEDED;
    String calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX
        + getUserId() + ".ics";
    String filePath = FileRepositoryManager.getTemporaryPath() + calendarIcsFileName;
    Exporter<ExportableCalendar> iCalExporter = ExporterProvider.getICalExporter();

    try {
      FileWriter fileWriter = new FileWriter(filePath);
      ExportDescriptor descriptor = ExportDescriptor.withWriter(fileWriter);
      List<CalendarEvent> events = getCalendarEvents(startDate, endDate);
      if (events.isEmpty()) {
        returnCode = AgendaSessionController.EXPORT_EMPTY;
      } else {
        iCalExporter.export(descriptor, ExportableCalendar.with(events));
      }
    } catch (Exception ex) {
      try {
        SilverTrace.error("agenda", getClass().getSimpleName() + ".exportIcalAgenda()",
            "root.EX_NO_MESSAGE", ex);
        FileFolderManager.deleteFile(filePath);
      } catch (UtilException ex1) {
        SilverTrace.error("agenda", getClass().getSimpleName() + ".exportIcalAgenda()",
            "root.EX_NO_MESSAGE", ex1);
      }
      throw new AgendaException("ExportIcalManager.exportIcalAgenda()",
          SilverpeasException.ERROR, "agenda.EXPORT_ICAL_FAILED", ex);
    }

    return returnCode;
  }

  /**
   * Exports in iCal the user agenda. Actually, it is the event occurrences that are exported in the
   * iCal file. This iCal file name is built withWriter the user identifier and it the file is
   * generated in the temporary directory.
   *
   * @return an export status code indicating if it has been successfull or not.
   * @throws AgendaException if an unexpected error occurs while exporting the events.
   */
  public String exportIcalAgenda() throws AgendaException {
    return exportIcalAgenda(null, null);
  }

  /**
   * Exports in iCal the user agenda within a synchronization process. Actually, it is the event
   * occurrences that are exported in the iCal file. This iCal file name is built withWriter the
   * user identifier and it the file is generated in the temporary directory.
   *
   * @return the path of the generated ics file.
   * @throws AgendaException if an unexpected error occurs while exporting the events.
   */
  public String exportIcalAgendaForSynchro() throws AgendaException {
    String calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX
        + getUserId() + ".ics";
    String filePath = null;
    Exporter<ExportableCalendar> iCalExporter = ExporterProvider.getICalExporter();

    try {
      List<CalendarEvent> events = getCalendarEvents(null, null);
      if (!events.isEmpty()) {
        filePath = FileRepositoryManager.getTemporaryPath() + calendarIcsFileName;
        FileWriter fileWriter = new FileWriter(filePath);
        ExportDescriptor descriptor = ExportDescriptor.withWriter(fileWriter);
        iCalExporter.export(descriptor, ExportableCalendar.with(events));
      }
    } catch (Exception ex) {
      try {
        SilverTrace.error("agenda", getClass().getSimpleName() + ".exportIcalAgenda()",
            "root.EX_NO_MESSAGE", ex);
        FileFolderManager.deleteFile(filePath);
      } catch (UtilException ex1) {
        SilverTrace.error("agenda", getClass().getSimpleName() + ".exportIcalAgenda()",
            "root.EX_NO_MESSAGE", ex1);
      }
      throw new AgendaException("ExportIcalManager.exportIcalAgendaForSynchro()",
          SilverpeasException.ERROR, "agenda.EXPORT_ICAL_FAILED", ex);
    }

    return filePath;
  }

  /**
   * Get All events between the specified interval of time.
   *
   * @param startDate the start date of the interval.
   * @param endDate the end date of the interval.
   * @return a collection of the schedulable events between the interval of time.
   * @throws RemoteException if the remote calendar of the user cannot be accessed.
   * @throws ParseException if the specified dates aren't formatted as expected.
   */
  private Collection<JournalHeader> getSchedulableCalendar(String startDate, String endDate)
      throws ParseException {
    return calendarBm.getPeriodSchedulablesForUser(DateUtil.date2SQLDate(
        startDate, getLanguage()), DateUtil.date2SQLDate(endDate, getLanguage()), getUserId(),
        null,
        new ParticipationStatus(ParticipationStatus.ACCEPTED).getString());
  }

  /**
   * Gets the events of the user calendar that are comprised within the specified interval of time.
   *
   * @param startDate the start date of the interval of time.
   * @param endDate the end date of the interval of time.
   * @return a list of calendar events.
   * @throws RemoteException if the events cannot be fetched from the user calendar.
   * @throws ParseException if the specified dates are not formatted as expected.
   */
  @SuppressWarnings("rawtypes")
  private List<CalendarEvent> getCalendarEvents(String startDate, String endDate) throws
      ParseException {
    List<CalendarEvent> events = new ArrayList<>();
    String fromDate = at(startDate, or(firstDate));
    String toDate = at(endDate, or(lastDate));

    Collection<JournalHeader> schedulables = getSchedulableCalendar(fromDate, toDate);
    for (JournalHeader schedulable : schedulables) {
      // creates an event corresponding to the current schedulable object
      Datable<?> eventStartDate = DateUtil.asDatable(schedulable.getStartDate(),
          StringUtil.isDefined(schedulable.getStartHour()));
      Datable<?> eventEndDate = DateUtil.asDatable(schedulable.getEndDate(),
          StringUtil.isDefined(schedulable.getEndHour()));
      CalendarEvent event = anEventAt((Datable) eventStartDate, eventEndDate).
          identifiedBy("event", schedulable.getId()).
          withTitle(schedulable.getName()).
          withDescription(schedulable.getDescription());

      // set access level (confidential, private or public) and the event priority
      event.withAccessLevel(schedulable.getClassification().getString());
      event.withPriority(schedulable.getPriority().getValue());

      // set the categories in which the event is
      Collection<Category> categories = calendarBm.getJournalCategories(schedulable.getId());
      for (Category category : categories) {
        event.getCategories().add(category.getName());
      }

      // set the attendees to the event
      Collection<Attendee> attendees = calendarBm.getJournalAttendees(schedulable.getId());
      for (Attendee attendee : attendees) {
        UserDetail user = OrganizationControllerProvider
            .getOrganisationController().getUserDetail(attendee.getUserId());
        if (user != null) {
          String email = user.geteMail();
          if (StringUtil.isDefined(email)) {
            event.getAttendees().add(email);
          }
        }
      }

      // add the event withWriter the others
      events.add(event);
    }

    return events;
  }

  /**
   * Gets the remote EJB that provides an access to the user calendar.
   */
  private void setCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = ServiceProvider.getService(SilverpeasCalendar.class);
      } catch (Exception e) {
        throw new AgendaRuntimeException("ExportIcalManager.setCalendarBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  private String at(String aDate, String defaultDate) throws ParseException {
    String at = aDate;
    if (!StringUtil.isDefined(aDate)) {
      at = DateUtil.getInputDate(defaultDate, getLanguage());
    }
    return at;
  }

  private String or(String defaultDate) {
    return defaultDate;
  }
}