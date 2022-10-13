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
package org.silverpeas.core.web.calendar.ical;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.importexport.ical.ExportableCalendar;
import org.silverpeas.core.importexport.ical.ICalExporterProvider;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.Category;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.model.ParticipationStatus;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.tools.agenda.control.AgendaException;
import org.silverpeas.core.web.tools.agenda.control.AgendaRuntimeException;
import org.silverpeas.core.web.tools.agenda.control.AgendaSessionController;

import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

/**
 * @author dle
 */
public class ExportIcalManager {

  private SilverpeasCalendar calendar;
  private static final String FIRST_DATE = "1970/01/01";
  private static final String LAST_DATE = "2050/01/01";
  private final String userId;
  private String language = "fr";

  public ExportIcalManager(AgendaSessionController agendaSessionController) {
    this.userId = agendaSessionController.getUserId();
    this.language = agendaSessionController.getLanguage();
    setCalendarService();
  }

  /**
   * Constructs a new manager of iCal export processes for the agenda core component.
   *
   * @param userId the unique identifier of the user for which the manager has to be instantiated.
   */
  public ExportIcalManager(String userId) {
    this.userId = userId;
    setCalendarService();
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
    Exporter<ExportableCalendar> iCalExporter = ICalExporterProvider.getICalExporter();

    try {
      FileWriter fileWriter = new FileWriter(filePath);
      ExportDescriptor descriptor = ExportDescriptor.withWriter(fileWriter);
      List<CalendarEvent> events = getCalendarEvents(startDate, endDate);
      if (events.isEmpty()) {
        returnCode = AgendaSessionController.EXPORT_EMPTY;
      } else {
        iCalExporter.exports(descriptor, () -> ExportableCalendar.with(events));
      }
    } catch (Exception ex) {
      try {
        SilverLogger.getLogger(this).error(ex);
        FileFolderManager.deleteFile(filePath);
      } catch (org.silverpeas.core.util.UtilException ex1) {
        SilverLogger.getLogger(this).error(ex1);
      }
      throw new AgendaException(ex);
    }

    return returnCode;
  }

  /**
   * Exports in iCal the user agenda within a synchronization process. Actually, it is the event
   * occurrences that are exported in the iCal file. This iCal file name is built withWriter the
   * user identifier and the file is generated in the temporary directory.
   *
   * @return the path of the generated ics file.
   * @throws AgendaException if an unexpected error occurs while exporting the events.
   */
  public String exportIcalAgendaForSynchro() throws AgendaException {
    String calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX
        + getUserId() + ".ics";
    String filePath = null;
    Exporter<ExportableCalendar> iCalExporter = ICalExporterProvider.getICalExporter();

    try {
      List<CalendarEvent> events = getCalendarEvents(null, null);
      if (!events.isEmpty()) {
        filePath = FileRepositoryManager.getTemporaryPath() + calendarIcsFileName;
        FileWriter fileWriter = new FileWriter(filePath);
        ExportDescriptor descriptor = ExportDescriptor.withWriter(fileWriter);
        iCalExporter.exports(descriptor, () -> ExportableCalendar.with(events));
      }
    } catch (Exception ex) {
      try {
        SilverLogger.getLogger(this).error(ex);
        FileFolderManager.deleteFile(filePath);
      } catch (org.silverpeas.core.util.UtilException ex1) {
        SilverLogger.getLogger(this).error(ex1);
      }
      throw new AgendaException(ex);
    }

    return filePath;
  }

  /**
   * Get All events between the specified interval of time.
   *
   * @param startDate the start date of the interval.
   * @param endDate the end date of the interval.
   * @return a collection of the schedulable events between the interval of time.
   * @throws ParseException if the specified dates aren't formatted as expected.
   */
  private Collection<JournalHeader> getSchedulableCalendar(String startDate, String endDate)
      throws ParseException {
    return calendar.getPeriodSchedulablesForUser(DateUtil.date2SQLDate(
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
   * @throws ParseException if the specified dates are not formatted as expected.
   */
  private List<CalendarEvent> getCalendarEvents(String startDate, String endDate) throws
      ParseException {
    List<CalendarEvent> events = new ArrayList<>();
    String fromDate = at(startDate, or(FIRST_DATE));
    String toDate = at(endDate, or(LAST_DATE));

    Collection<JournalHeader> schedulables = getSchedulableCalendar(fromDate, toDate);
    for (JournalHeader schedulable : schedulables) {
      // creates an event corresponding to the current schedulable object
      OffsetDateTime startDateTime =
          OffsetDateTime.ofInstant(schedulable.getStartDate().toInstant(),
              TimeZone.getDefault().toZoneId());
      OffsetDateTime endDateTime = OffsetDateTime.ofInstant(schedulable.getEndDate().toInstant(),
          TimeZone.getDefault().toZoneId());
      boolean allDay = StringUtil.isDefined(schedulable.getStartHour()) &&
          StringUtil.isDefined(schedulable.getEndHour());
      CalendarEvent event = getCalendarEvent(startDateTime, endDateTime, allDay);
      event.identifiedBy("event", schedulable.getId()).
          withTitle(schedulable.getName()).
          withDescription(schedulable.getDescription());

      // set access level (confidential, private or public) and the event priority
      event.withVisibilityLevel(
          VisibilityLevel.valueOf(schedulable.getClassification().getString().toUpperCase()));
      event.withPriority(Priority.valueOf(schedulable.getPriority().getValue()));

      // set the categories in which the event is
      Collection<Category> categories = calendar.getJournalCategories(schedulable.getId());
      for (Category category : categories) {
        event.getCategories().add(category.getName());
      }

      // set the attendees to the event
      Collection<Attendee> attendees = calendar.getJournalAttendees(schedulable.getId());
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

  @Nonnull
  private CalendarEvent getCalendarEvent(final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime, final boolean allDay) {
    CalendarEvent event;
    if (allDay) {
      event = CalendarEvent.on(
          Period.between(startDateTime.toLocalDate(), endDateTime.toLocalDate()));
    } else {
      event = CalendarEvent.on(Period.between(startDateTime, endDateTime));
    }
    return event;
  }

  /**
   * Gets the remote EJB that provides an access to the user calendar.
   */
  private void setCalendarService() {
    if (calendar == null) {
      try {
        calendar = ServiceProvider.getService(SilverpeasCalendar.class);
      } catch (Exception e) {
        throw new AgendaRuntimeException(e);
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