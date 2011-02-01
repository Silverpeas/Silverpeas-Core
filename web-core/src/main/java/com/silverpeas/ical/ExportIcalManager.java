/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
/**
 *
 */
package com.silverpeas.ical;

import com.silverpeas.calendar.Datable;
import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.Exporter;
import com.silverpeas.export.ExporterFactory;
import com.stratelia.webactiv.util.exception.UtilException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.agenda.control.AgendaException;
import com.stratelia.webactiv.agenda.control.AgendaRuntimeException;
import com.stratelia.webactiv.agenda.control.AgendaSessionController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.ParticipationStatus;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import java.io.FileWriter;
import static com.silverpeas.calendar.CalendarEvent.*;

/**
 * @author dle
 */
public class ExportIcalManager {

  private CalendarBm calendarBm;
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
   * @param userId the unique identifier of the user for which the manager has to be instanciated.
   */
  public ExportIcalManager(String userId) {
    this.userId = userId;
    setCalendarBm();
  }

  /**
   * Gets the unique identifier of the user to which this manager belongs.
   * @return the user identifier.
   */
  private String getUserId() {
    return this.userId;
  }

  /**
   * Gets the language used by the user in Silverpeas.
   * @return the user language.
   */
  private String getLanguage() {
    return this.language;
  }

  /**
   * Exports in iCal the events of the user agenda between the specified interval.
   * Actually, it is the event occurrences that are exported in the iCal file. This iCal file name
   * is built with the user identifier and it the file is generated in the temporary directory.
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
    ExporterFactory exporterFactory = ExporterFactory.getFactory();
    Exporter<CalendarEvent> iCalExporter = exporterFactory.getICalExporter();

    try {
      FileWriter fileWriter = new FileWriter(filePath);
      ExportDescriptor descriptor = new ExportDescriptor(fileWriter);
      List<CalendarEvent> events = getCalendarEvents(startDate, endDate);
      if (events.isEmpty()) {
        returnCode = AgendaSessionController.EXPORT_EMPTY;
      } else {
        iCalExporter.export(descriptor, events);
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
   * Exports in iCal the user agenda.
   * Actually, it is the event occurrences that are exported in the iCal file. This iCal file name
   * is built with the user identifier and it the file is generated in the temporary directory.
   * @return an export status code indicating if it has been successfull or not.
   * @throws AgendaException if an unexpected error occurs while exporting the events.
   */
  public String exportIcalAgenda() throws AgendaException {
    return exportIcalAgenda(null, null);
  }

  /**
   * Exports in iCal the user agenda within a synchronization process.
   * Actually, it is the event occurrences that are exported in the iCal file. This iCal file name
   * is built with the user identifier and it the file is generated in the temporary directory.
   * @return the path of the generated ics file.
   * @throws AgendaException if an unexpected error occurs while exporting the events.
   */
  public String exportIcalAgendaForSynchro() throws AgendaException {
    String calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX
        + getUserId() + ".ics";
    String filePath = null;
    ExporterFactory exporterFactory = ExporterFactory.getFactory();
    Exporter<CalendarEvent> iCalExporter = exporterFactory.getICalExporter();

    try {
      List<CalendarEvent> events = getCalendarEvents(null, null);
      if (!events.isEmpty()) {
        filePath = FileRepositoryManager.getTemporaryPath() + calendarIcsFileName;
        FileWriter fileWriter = new FileWriter(filePath);
        ExportDescriptor descriptor = new ExportDescriptor(fileWriter);
        iCalExporter.export(descriptor, events);
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
   * Get Ical header
   * @return CalendarIcs
   */
//  private Calendar getIcsCalendarHeader() {
//    Calendar calendarIcs = new Calendar();
//    calendarIcs.getProperties().add(new ProdId("-//Silverpeas//iCal4j 1.0//FR"));
//    calendarIcs.getProperties().add(Version.VERSION_2_0);
//    calendarIcs.getProperties().add(CalScale.GREGORIAN);
//    return calendarIcs;
//  }
  /**
   * Get Ical contents
   * @param calendarIcs
   * @param startDate
   * @param endDate
   * @return CalendarIcs
   * @throws ParseException
   * @throws RemoteException
   * @throws SocketException
   */
//  private Calendar getIcsCalendarContent(Calendar calendarIcs,
//      String startDate, String endDate) throws ParseException, RemoteException,
//      SocketException, AgendaException, URISyntaxException {
//    SilverTrace.info("agenda", "ExportIcalManager.getIcsCalendarContent()",
//        "root.MSG_GEN_ENTER_METHOD");
//    SilverTrace.debug("agenda", "ExportIcalManager.getIcsCalendarContent()",
//        "root.MSG_GEN_PARAM_VALUE", "startDate=" + startDate + " endDate="
//        + endDate);
//    boolean exportAll = true;
//    if (StringUtil.isDefined(startDate) && StringUtil.isDefined(startDate)) {
//      exportAll = false;
//    }
//    if (exportAll) {
//      startDate = DateUtil.getInputDate(firstDate, getLanguage());
//      endDate = DateUtil.getInputDate(lastDate, getLanguage());
//      SilverTrace.debug("agenda", "ExportIcalManager.getIcsCalendarContent()",
//          "root.MSG_GEN_PARAM_VALUE", "exportAll");
//      SilverTrace.debug("agenda", "ExportIcalManager.getIcsCalendarContent()",
//          "root.MSG_GEN_PARAM_VALUE", "startDate=" + startDate + " endDate="
//          + endDate);
//    }
//
//    Iterator itSchedules = getSchedulableCalendar(startDate, endDate).iterator();
//    final List events = new ArrayList();
//    while (itSchedules.hasNext()) {
//      Object obj = itSchedules.next();
//
//      if (obj instanceof Schedulable) {
//        Schedulable eventAgenda = (Schedulable) obj;
//
//        SilverTrace.debug("agenda",
//            "ExportIcalManager.getIcsCalendarContent()",
//            "root.MSG_GEN_PARAM_VALUE", "eventAgenda.getStartDay()="
//            + eventAgenda.getStartDay() + " eventAgenda.getStartDate()="
//            + eventAgenda.getStartDate());
//
//        // Conv startDateTime and endDateTime in ICal format
//        DateTime startDateTime = new DateTime(eventAgenda.getStartDate());
//        DateTime endDateTime = new DateTime(eventAgenda.getEndDate());
//
//        // Define event
//        VEvent event = new VEvent(startDateTime, endDateTime, eventAgenda.getName());
//        event.getProperties().add(new UidGenerator(Uid.UID).generateUid());
//
//        // Add Description
//        event.getProperties().add(new Description(eventAgenda.getDescription()));
//        // Add Classification
//        event.getProperties().add(
//            new Clazz(eventAgenda.getClassification().getString()));
//        // Add Priority
//        event.getProperties().add(
//            new Priority(eventAgenda.getPriority().getValue()));
//
//        // Add Categories
//        Collection categories = calendarBm.getJournalCategories(eventAgenda.getId());
//        if (!categories.isEmpty()) {
//          CategoryList categoryList = new CategoryList();
//          Iterator categoriesIt = categories.iterator();
//          while (categoriesIt.hasNext()) {
//            Category categorie = (Category) categoriesIt.next();
//            categoryList.add(categorie.getName());
//          }
//          event.getProperties().add(new Categories(categoryList));
//        }
//
//        // Add attendees
//        Collection attendees = calendarBm.getJournalAttendees(eventAgenda.getId());
//        if (!attendees.isEmpty()) {
//          Iterator attendeesIt = attendees.iterator();
//          while (attendeesIt.hasNext()) {
//            Attendee attendeeAgenda = (Attendee) attendeesIt.next();
//            OrganizationController oc = new OrganizationController();
//            UserDetail user = oc.getUserDetail(attendeeAgenda.getUserId());
//            if (user != null) {
//              String email = user.geteMail();
//              if (StringUtil.isDefined(email)) {
//                event.getProperties().add(
//                    new net.fortuna.ical4j.model.property.Attendee(email));
//              }
//            }
//          }
//        }
//
//        // Add this event
//        events.add(event);
//      }
//    }
//    if (!events.isEmpty()) {
//      calendarIcs.getComponents().addAll(events);
//    }
//    SilverTrace.info("agenda", "ExportIcalManager.getIcsCalendarContent()",
//        "root.MSG_GEN_EXIT_METHOD");
//    return calendarIcs;
//  }
  /**
   * Get All events between the specified interval of time.
   * @param startDate the start date of the interval.
   * @param endDate the end date of the interval.
   * @return a collection of the schedulable events between the interval of time.
   * @throws RemoteException if the remote calendar of the user cannot be accessed.
   * @throws ParseException if the specified dates aren't formatted as expected.
   */
  private Collection<JournalHeader> getSchedulableCalendar(String startDate, String endDate)
      throws RemoteException, ParseException {
    return calendarBm.getPeriodSchedulablesForUser(DateUtil.date2SQLDate(
        startDate, getLanguage()), DateUtil.date2SQLDate(endDate, getLanguage()), getUserId(), null,
        new ParticipationStatus(ParticipationStatus.ACCEPTED).getString());
  }

  /**
   * Gets the events of the user calendar that are comprised within the specified interval of time.
   * @param startDate the start date of the interval of time.
   * @param endDate the end date of the interval of time.
   * @return a list of calendar events.
   * @throws RemoteException if the events cannot be fetched from the user calendar.
   * @throws ParseException if the specified dates are not formatted as expected.
   */
  private List<CalendarEvent> getCalendarEvents(String startDate, String endDate) throws
      RemoteException, ParseException {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    String fromDate = at(startDate, or(firstDate));
    String toDate = at(endDate, or(lastDate));

    Collection<JournalHeader> schedulables = getSchedulableCalendar(fromDate, toDate);
    for (JournalHeader schedulable : schedulables) {
      // creates an event corresponding to the current schedulable object
      Datable<?> eventStartDate = DateUtil.asDatable(schedulable.getStartDate(),
          StringUtil.isDefined(schedulable.getStartHour()));
      Datable<?> eventEndDate = DateUtil.asDatable(schedulable.getEndDate(),
          StringUtil.isDefined(schedulable.getEndHour()));
      CalendarEvent event = anEventAt(eventStartDate, eventEndDate).
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
        OrganizationController oc = new OrganizationController();
        UserDetail user = oc.getUserDetail(attendee.getUserId());
        if (user != null) {
          String email = user.geteMail();
          if (StringUtil.isDefined(email)) {
            event.getAttendees().add(email);
          }
        }
      }

      // add the event with the others
      events.add(event);
    }

    return events;
  }

  /**
   * Gets the remote EJB that provides an access to the user calendar.
   * @return the user calendar.
   */
  private void setCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = ((CalendarBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class)).create();
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