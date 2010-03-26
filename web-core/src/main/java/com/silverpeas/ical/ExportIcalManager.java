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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.CategoryList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

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
import com.stratelia.webactiv.calendar.model.ParticipationStatus;
import com.stratelia.webactiv.calendar.model.Schedulable;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

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
   * Constructor for Servlet SubscribeAgenda call
   * @param userId
   */
  public ExportIcalManager(String userId) {
    this.userId = userId;
    setCalendarBm();
  }

  private String getUserId() {
    return this.userId;
  }

  private String getLanguage() {
    return this.language;
  }

  /**
   * Export Calendar in Ical file format
   * @param startDate
   * @param endDate
   * @return ReturnCode
   * @throws Exception
   * @throws AgendaException
   * @throws FileNotFoundException
   * @throws ValidationException
   * @throws IOException
   * @throws ParseException
   */
  public String exportIcalAgenda(String startDate, String endDate)
      throws Exception, AgendaException, FileNotFoundException,
      ValidationException, IOException, ParseException {
    String returnCode = AgendaSessionController.EXPORT_SUCCEEDED;
    String calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX
        + getUserId() + ".ics";

    CalendarOutputter outputter = new CalendarOutputter();
    Calendar calendarIcs = getIcsCalendarHeader();
    calendarIcs = getIcsCalendarContent(calendarIcs, startDate, endDate);
    if (calendarIcs.getComponents().isEmpty())
      returnCode = AgendaSessionController.EXPORT_EMPTY;
    else {
      FileOutputStream fileOutput = new FileOutputStream(FileRepositoryManager
          .getTemporaryPath()
          + calendarIcsFileName);
      try {
        outputter.output(calendarIcs, fileOutput);
        fileOutput.close();
      } catch (Exception e) {
        fileOutput.close();
        FileFolderManager.deleteFile(FileRepositoryManager.getTemporaryPath()
            + calendarIcsFileName);
        throw new AgendaException("ExportIcalManager.exportIcalAgenda()",
            SilverpeasException.ERROR, "agenda.EXPORT_ICAL_FAILED", e);
      }
    }
    return returnCode;
  }

  /**
   * Export Calendar in Ical file format
   * @return
   * @throws Exception
   * @throws AgendaException
   * @throws FileNotFoundException
   * @throws ValidationException
   * @throws IOException
   * @throws ParseException
   */
  public String exportIcalAgenda() throws Exception, AgendaException,
      FileNotFoundException, ValidationException, IOException, ParseException {
    return exportIcalAgenda(null, null);
  }

  /**
   * Export Calendar in Ical format (call by SubscribeAgenda)
   * @param startDate
   * @param endDate
   * @return ReturnCode
   * @throws Exception
   * @throws AgendaException
   * @throws FileNotFoundException
   * @throws ValidationException
   * @throws IOException
   * @throws ParseException
   */
  public String exportIcalAgendaForSynchro() throws Exception, AgendaException,
      FileNotFoundException, ValidationException, IOException, ParseException {
    String calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX
        + getUserId() + ".ics";
    String filePath = null;
    CalendarOutputter outputter = new CalendarOutputter();
    Calendar calendarIcs = getIcsCalendarHeader();
    calendarIcs = getIcsCalendarContent(calendarIcs, null, null);
    if (!calendarIcs.getComponents().isEmpty()) {
      filePath = FileRepositoryManager.getTemporaryPath() + calendarIcsFileName;
      FileOutputStream fileOutput = new FileOutputStream(filePath);
      try {
        outputter.output(calendarIcs, fileOutput);
        fileOutput.flush();
        fileOutput.close();
      } catch (Exception e) {
        fileOutput.close();
        FileFolderManager.deleteFile(FileRepositoryManager.getTemporaryPath()
            + calendarIcsFileName);
        throw new AgendaException("ExportIcalManager.exportIcalAgenda()",
            SilverpeasException.ERROR, "agenda.EXPORT_ICAL_FAILED", e);
      }
    }
    return filePath;
  }

  /**
   * Get Ical header
   * @return CalendarIcs
   */
  private Calendar getIcsCalendarHeader() {
    Calendar calendarIcs = new Calendar();
    calendarIcs.getProperties()
        .add(new ProdId("-//Silverpeas//iCal4j 1.0//FR"));
    calendarIcs.getProperties().add(Version.VERSION_2_0);
    calendarIcs.getProperties().add(CalScale.GREGORIAN);
    return calendarIcs;
  }

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
  private Calendar getIcsCalendarContent(Calendar calendarIcs,
      String startDate, String endDate) throws ParseException, RemoteException,
      SocketException, AgendaException, URISyntaxException {
    SilverTrace.info("agenda", "ExportIcalManager.getIcsCalendarContent()",
        "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.debug("agenda", "ExportIcalManager.getIcsCalendarContent()",
        "root.MSG_GEN_PARAM_VALUE", "startDate=" + startDate + " endDate="
        + endDate);
    boolean exportAll = true;
    if (StringUtil.isDefined(startDate) && StringUtil.isDefined(startDate))
      exportAll = false;
    if (exportAll) {
      startDate = DateUtil.getInputDate(firstDate, getLanguage());
      endDate = DateUtil.getInputDate(lastDate, getLanguage());
      SilverTrace.debug("agenda", "ExportIcalManager.getIcsCalendarContent()",
          "root.MSG_GEN_PARAM_VALUE", "exportAll");
      SilverTrace.debug("agenda", "ExportIcalManager.getIcsCalendarContent()",
          "root.MSG_GEN_PARAM_VALUE", "startDate=" + startDate + " endDate="
          + endDate);
    }

    Iterator itSchedules = getSchedulableCalendar(startDate, endDate)
        .iterator();
    final List events = new ArrayList();
    while (itSchedules.hasNext()) {
      Object obj = itSchedules.next();

      if (obj instanceof Schedulable) {
        Schedulable eventAgenda = (Schedulable) obj;

        SilverTrace.debug("agenda",
            "ExportIcalManager.getIcsCalendarContent()",
            "root.MSG_GEN_PARAM_VALUE", "eventAgenda.getStartDay()="
            + eventAgenda.getStartDay() + " eventAgenda.getStartDate()="
            + eventAgenda.getStartDate());

        // Conv startDateTime and endDateTime in ICal format
        DateTime startDateTime = new DateTime(eventAgenda.getStartDate());
        DateTime endDateTime = new DateTime(eventAgenda.getEndDate());

        // Define event
        VEvent event = new VEvent(startDateTime, endDateTime, eventAgenda
            .getName());
        event.getProperties().add(new UidGenerator(Uid.UID).generateUid());

        // Add Description
        event.getProperties()
            .add(new Description(eventAgenda.getDescription()));
        // Add Classification
        event.getProperties().add(
            new Clazz(eventAgenda.getClassification().getString()));
        // Add Priority
        event.getProperties().add(
            new Priority(eventAgenda.getPriority().getValue()));

        // Add Categories
        Collection categories = calendarBm.getJournalCategories(eventAgenda
            .getId());
        if (!categories.isEmpty()) {
          CategoryList categoryList = new CategoryList();
          Iterator categoriesIt = categories.iterator();
          while (categoriesIt.hasNext()) {
            Category categorie = (Category) categoriesIt.next();
            categoryList.add(categorie.getName());
          }
          event.getProperties().add(new Categories(categoryList));
        }

        // Add attendees
        Collection attendees = calendarBm.getJournalAttendees(eventAgenda
            .getId());
        if (!attendees.isEmpty()) {
          Iterator attendeesIt = attendees.iterator();
          while (attendeesIt.hasNext()) {
            Attendee attendeeAgenda = (Attendee) attendeesIt.next();
            OrganizationController oc = new OrganizationController();
            UserDetail user = oc.getUserDetail(attendeeAgenda.getUserId());
            if (user != null) {
              String email = user.geteMail();
              if (StringUtil.isDefined(email))
                event.getProperties().add(
                    new net.fortuna.ical4j.model.property.Attendee(email));
            }
          }
        }

        // Add this event
        events.add(event);
      }
    }
    if (!events.isEmpty())
      calendarIcs.getComponents().addAll(events);
    SilverTrace.info("agenda", "ExportIcalManager.getIcsCalendarContent()",
        "root.MSG_GEN_EXIT_METHOD");
    return calendarIcs;
  }

  /**
   * Get All events between period
   * @param startDate
   * @param endDate
   * @return Collections of Schedulable events
   * @throws RemoteException
   * @throws ParseException
   */
  private Collection getSchedulableCalendar(String startDate, String endDate)
      throws RemoteException, ParseException {
    return calendarBm.getPeriodSchedulablesForUser(DateUtil.date2SQLDate(
        startDate, getLanguage()), DateUtil
        .date2SQLDate(endDate, getLanguage()), getUserId(), null,
        new ParticipationStatus(ParticipationStatus.ACCEPTED).getString());
  }

  /**
   * Method declaration
   * @see
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

}