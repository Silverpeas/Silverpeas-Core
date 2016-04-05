/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.calendar.ical;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.tools.agenda.control.AgendaRuntimeException;
import org.silverpeas.core.web.tools.agenda.control.AgendaSessionController;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.Category;
import org.silverpeas.core.calendar.model.Schedulable;
import com.sun.syndication.io.XmlReader;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

public class ImportIcalManager {

  public final static String IMPORT_SUCCEEDED = "0";
  public final static String IMPORT_EMPTY = "1";
  private static final long YEAR = 1000L * 60 * 60 * 24 * 365;
  public static String charset = null;
  private AgendaSessionController agendaSessionController;
  private SilverpeasCalendar calendarBm;

  public ImportIcalManager(AgendaSessionController agendaSessionController) {
    this.agendaSessionController = agendaSessionController;
    setCalendarBm();
  }

  /**
   * IMPORT SilverpeasCalendar in Ical format
   * @param file
   * @return
   * @throws Exception
   */
  public String importIcalAgenda(File file) throws Exception {
    String returnCode = AgendaSessionController.IMPORT_FAILED;
    InputStreamReader inputStream = null;
    XmlReader xr = null;
    try {
      String charsetUsed = agendaSessionController.getSettings().getString("defaultCharset");
      if (StringUtil.isDefined(charset)) {
        charsetUsed = charset;
      }

      // File Encoding detection
      xr = new XmlReader(file);
      if (StringUtil.isDefined(xr.getEncoding())) {
        charsetUsed = xr.getEncoding();
      }
      inputStream = new InputStreamReader(new FileInputStream(file), charsetUsed);
      CalendarBuilder builder = new CalendarBuilder();
      Calendar calendar = builder.build(inputStream);
      // Get all EVENTS
      for (Object o : calendar.getComponents(Component.VEVENT)) {
        VEvent eventIcal = (VEvent) o;
        String name = getFieldEvent(eventIcal.getProperty(Property.SUMMARY));

        String description = null;
        if (StringUtil.isDefined(getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION)))) {
          description = getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION));
        }

        // Name is mandatory in the Silverpeas Agenda
        if (!StringUtil.isDefined(name)) {
          if (StringUtil.isDefined(description)) {
            name = description;
          } else {
            name = " ";
          }
        }

        String priority = getFieldEvent(eventIcal.getProperty(Property.PRIORITY));
        if (!StringUtil.isDefined(priority)) {
          priority = Priority.UNDEFINED.getValue();
        }
        String classification = getFieldEvent(eventIcal.getProperty(Property.CLASS));
        String startDate = getFieldEvent(eventIcal.getProperty(Property.DTSTART));
        String endDate = getFieldEvent(eventIcal.getProperty(Property.DTEND));
        Date startDay = getDay(startDate);
        String startHour = getHour(startDate);
        Date endDay = getDay(endDate);
        String endHour = getHour(endDate);
        // Duration of the event
        long duration = endDay.getTime() - startDay.getTime();
        boolean allDay = false;

        // All day case
        // I don't know why ??
        if (("00:00".equals(startHour) && "00:00".equals(endHour)) ||
            (!StringUtil.isDefined(startHour) && !StringUtil.isDefined(endHour))) {
          // For complete Day
          startHour = "";
          endHour = "";
          allDay = true;
        }

        // Get reccurrent dates
        Collection reccurenceDates = getRecurrenceDates(eventIcal);

        // No reccurent dates
        if (reccurenceDates == null) {
          String idEvent = isExist(eventIcal);
          // update if event already exists, create if does not exist
          if (StringUtil.isDefined(idEvent)) {
            agendaSessionController
                .updateJournal(idEvent, name, description, priority, classification, startDay,
                    startHour, endDay, endHour);
          } else {
            idEvent = agendaSessionController
                .addJournal(name, description, priority, classification, startDay, startHour,
                    endDay, endHour);
          }

          // Get Categories
          processCategories(eventIcal, idEvent);
        } else {
          for (Object reccurenceDate : reccurenceDates) {
            // Reccurent event startDate
            startDay = (DateTime) reccurenceDate;
            // Reccurent event endDate
            long newEndDay = startDay.getTime() + duration;
            endDay = new DateTime(newEndDay);
            if (allDay) {
              // So we have to convert this date to agenda format date
              GregorianCalendar gregCalendar = new GregorianCalendar();
              gregCalendar.setTime(endDay);
              gregCalendar.add(GregorianCalendar.DATE, -1);
              endDay = new Date(gregCalendar.getTime());
            }
            String idEvent = isExist(eventIcal, startDay, endDay, startHour);
            // update if event already exists, create if does not exist
            if (StringUtil.isDefined(idEvent)) {
              agendaSessionController
                  .updateJournal(idEvent, name, description, priority, classification, startDay,
                      startHour, endDay, endHour);
            } else {
              idEvent = agendaSessionController
                  .addJournal(name, description, priority, classification, startDay, startHour,
                      endDay, endHour);
            }
            // Get Categories
            processCategories(eventIcal, idEvent);
          }
        }
      }
      returnCode = AgendaSessionController.IMPORT_SUCCEEDED;
    } catch (Exception e) {
      SilverTrace.error("agenda", "ImportIcalManager.importIcalAgenda()", e.getCause().toString());
      returnCode = AgendaSessionController.IMPORT_FAILED;
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(xr);
    }
    return returnCode;
  }

  /**
   * Verify if the event already exists
   * @param eventIcal
   * @return id or null
   * @throws Exception
   */
  private String isExist(Component eventIcal) throws Exception {
    return isExist(eventIcal, null, null, null);
  }

  /**
   * Verify if the event already exists
   * @param eventIcal
   * @return id or null
   * @throws Exception
   */
  private String isExist(Component eventIcal, Date startDateReccurent, Date endDateReccurent,
      String startHourReccurent) throws Exception {
    String name = getFieldEvent(eventIcal.getProperty(Property.SUMMARY));
    String description = null;
    if (StringUtil.isDefined(getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION)))) {
      description = getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION));
    }
    if (!StringUtil.isDefined(name)) {
      if (StringUtil.isDefined(description)) {
        name = description;
      } else {
        name = " ";
      }
    }
    String startDate =
        DateUtil.date2SQLDate(getDay(getFieldEvent(eventIcal.getProperty(Property.DTSTART))));
    String endDate =
        DateUtil.date2SQLDate(getDay(getFieldEvent(eventIcal.getProperty(Property.DTEND))));
    String startHour = getHour(getFieldEvent(eventIcal.getProperty(Property.DTSTART)));
    // Reccurrent case
    if (startDateReccurent != null) {
      startDate = DateUtil.date2SQLDate(startDateReccurent);
      endDate = DateUtil.date2SQLDate(endDateReccurent);
      startHour = startHourReccurent;
    }

    // Get Events within this period to know if event already exists
    Collection events = calendarBm
        .getPeriodSchedulablesForUser(startDate, endDate, agendaSessionController.getAgendaUserId(),
            null, agendaSessionController.
                getParticipationStatus().getString());
    if (!events.isEmpty()) {
      for (Object obj : events) {
        if (obj instanceof Schedulable) {
          Schedulable eventAgenda = (Schedulable) obj;
          if (eventAgenda.getName().equals(name) &&
              DateUtil.date2SQLDate(eventAgenda.getStartDate()).equals(startDate)) {
            if (StringUtil.isDefined(eventAgenda.getStartHour()) &&
                StringUtil.isDefined(startHour)) {
              if (eventAgenda.getStartHour().equals(startHour)) {
                return eventAgenda.getId();
              }
            } else {
              return eventAgenda.getId();
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Add or update categories of the event
   * @param eventIcal
   * @param idEvent the event identifier
   * @throws Exception
   */
  private void processCategories(Component eventIcal, String idEvent) throws Exception {
    if (eventIcal.getProperty(Property.CATEGORIES) != null) {
      String categories = eventIcal.getProperty(Property.CATEGORIES).getValue();
      StringTokenizer st = new StringTokenizer(categories, ",");
      String[] categoryIds = new String[st.countTokens()];
      int j = 0;
      boolean addCategoryToEvent = false;
      while (st.hasMoreTokens()) {
        String categIcal = st.nextToken();
        // Agenda Categories
        for (Category category : agendaSessionController.getAllCategories()) {
          if (categIcal.equals(EncodeHelper.htmlStringToJavaString(category.getName()))) {
            addCategoryToEvent = true;
            categoryIds[j++] = category.getId();
          }
        }
      }
      if (addCategoryToEvent) {
        agendaSessionController.setJournalCategories(idEvent, categoryIds);
      }
    }
  }

  /**
   *
   */
  private void setCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = ServiceProvider.getService(SilverpeasCalendar.class);
      } catch (Exception e) {
        throw new AgendaRuntimeException("ImportIcalManager.setCalendarBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  /**
   * getDay from a givent string date parameter
   * @param dateTime
   * @return Date from a datetime string
   * @throws ParseException
   */
  private Date getDay(String dateTime) throws ParseException {
    Date day;
    if (dateTime.length() > 8) {
      day = new DateTime(dateTime);
    } else {
      day = new Date(dateTime);
    }
    return day;
  }

  /**
   * Extract hour from a date string
   * @param dateTime
   * @return an extract hour from a date string
   * @throws ParseException
   */
  private String getHour(String dateTime) throws ParseException {
    String hour = null;
    if (dateTime.length() > 8) {
      hour = DateUtil.getFormattedTime(new DateTime(dateTime));
    }
    return hour;
  }

  /**
   * Get value of the Ical property
   * @param property an icalendar property
   * @return String
   */
  private String getFieldEvent(Property property) {
    String fieldValue = null;
    if (property != null) {
      fieldValue = EncodeHelper.transformStringForBD(property.getValue());
    }

    return fieldValue;
  }

  /**
   * Get start dates of a recurrent Event
   * @param event
   * @return Collection of DateTime
   */
  private static final Collection getRecurrenceDates(VEvent event) {
    RRule rule = (RRule) event.getProperty(Property.RRULE);
    if (rule != null) {
      Recur recur = rule.getRecur();
      DateTime startDate = new DateTime(event.getStartDate().getDate());
      long interval = YEAR * 2;
      if (Recur.YEARLY.equals(recur.getFrequency())) {
        interval *= 5;
      }
      DateTime endDate = new DateTime(startDate.getTime() + (interval));
      DateList dates = recur.getDates(startDate, endDate, Value.DATE_TIME);
      return dates;
    }
    return null;
  }
}