/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import com.rometools.rome.io.XmlReader;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.RRule;
import org.silverpeas.core.personalorganizer.model.Category;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.tools.agenda.control.AgendaException;
import org.silverpeas.core.web.tools.agenda.control.AgendaSessionController;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.StringTokenizer;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class ImportIcalManager {

  private static final long YEAR = 1000L * 60 * 60 * 24 * 365;
  private static String charset = null;
  private AgendaSessionController agendaSessionController;
  private SilverpeasCalendar calendarService;

  public ImportIcalManager(AgendaSessionController agendaSessionController) {
    this.agendaSessionController = agendaSessionController;
    setCalendarBm();
  }

  public static void setCharset(final String aCharset) {
    charset = aCharset;
  }

  public static String getCharset() {
    return charset;
  }

  /**
   * IMPORT SilverpeasCalendar in Ical format
   * @param file
   * @return
   */
  public String importIcalAgenda(File file) {
    String returnCode;
    String charsetUsed = getCharsetForImport();
    try (XmlReader xr = new XmlReader(file)) {
      // File Encoding detection
      if (isDefined(xr.getEncoding())) {
        charsetUsed = xr.getEncoding();
      }
      returnCode = importFrom(file, charsetUsed);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      returnCode = AgendaSessionController.IMPORT_FAILED;
    }
    return returnCode;
  }

  @Nonnull
  private String importFrom(final File file, final String charsetUsed) {
    String returnCode;
    try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file),
        charsetUsed)) {
      CalendarBuilder builder = new CalendarBuilder();
      Calendar calendar = builder.build(inputStream);
      // Get all EVENTS
      importAllEvents(calendar);
      returnCode = AgendaSessionController.IMPORT_SUCCEEDED;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      returnCode = AgendaSessionController.IMPORT_FAILED;
    }
    return returnCode;
  }

  private void importAllEvents(final Calendar calendar) throws ParseException, AgendaException {
    for (CalendarComponent o : calendar.getComponents(Component.VEVENT)) {
      VEvent eventIcal = (VEvent) o;
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
          (!isDefined(startHour) && !isDefined(endHour))) {
        // For complete Day
        startHour = "";
        endHour = "";
        allDay = true;
      }

      // Get reccurrent dates
      Collection<Date> reccurenceDates = getRecurrenceDates(eventIcal);

      // No reccurent dates
      if (reccurenceDates.isEmpty()) {
        String description = getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION));
        String name = getName(eventIcal, description);
        String priority = getPriority(eventIcal);
        String classification = getFieldEvent(eventIcal.getProperty(Property.CLASS));
        String idEvent = isExist(eventIcal);
        // update if event already exists, create if does not exist
        if (isDefined(idEvent)) {
          agendaSessionController.updateJournal(idEvent, name, description, priority,
              classification, startDay, startHour, endDay, endHour);
        } else {
          idEvent = agendaSessionController.addJournal(name, description, priority, classification,
              startDay, startHour, endDay, endHour);
        }

        // Get Categories
        processCategories(eventIcal, idEvent);
      } else {
        importAllRecurrenceDates(eventIcal, startHour,
            endHour, duration, allDay, reccurenceDates);
      }
    }
  }

  private void importAllRecurrenceDates(final VEvent eventIcal,
      final String startHour, final String endHour, final long duration, final boolean allDay,
      final Collection<Date> recurrenceDates) throws ParseException, AgendaException {
    Date endDay;
    Date startDay;
    String description = getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION));
    String name = getName(eventIcal, description);
    String priority = getPriority(eventIcal);
    String classification = getFieldEvent(eventIcal.getProperty(Property.CLASS));
    for (Date recurrenceDate : recurrenceDates) {
      // Reccurent event startDate
      startDay = recurrenceDate;
      // Reccurent event endDate
      long newEndDay = startDay.getTime() + duration;
      endDay = new DateTime(newEndDay);
      if (allDay) {
        // So we have to convert this date to agenda format date
        GregorianCalendar gregCalendar = new GregorianCalendar();
        gregCalendar.setTime(endDay);
        gregCalendar.add(java.util.Calendar.DATE, -1);
        endDay = new Date(gregCalendar.getTime());
      }
      String idEvent = isExist(eventIcal, startDay, endDay, startHour);
      // update if event already exists, create if does not exist
      if (isDefined(idEvent)) {
        agendaSessionController.updateJournal(idEvent, name, description, priority, classification,
            startDay, startHour, endDay, endHour);
      } else {
        idEvent = agendaSessionController.addJournal(name, description, priority, classification,
            startDay, startHour, endDay, endHour);
      }
      // Get Categories
      processCategories(eventIcal, idEvent);
    }
  }

  private String getPriority(final VEvent eventIcal) {
    String priority = getFieldEvent(eventIcal.getProperty(Property.PRIORITY));
    if (!isDefined(priority)) {
      priority = Priority.UNDEFINED.getValue();
    }
    return priority;
  }

  private String getCharsetForImport() {
    final String charsetUsed;
    if (isDefined(charset)) {
      charsetUsed = charset;
    } else {
      charsetUsed = agendaSessionController.getSettings().getString("defaultCharset");
    }
    return charsetUsed;
  }

  /**
   * Verify if the event already exists
   * @param eventIcal
   * @return id or null
   * @throws Exception
   */
  private String isExist(Component eventIcal) throws ParseException {
    return isExist(eventIcal, null, null, null);
  }

  /**
   * Verify if the event already exists
   * @param eventIcal
   * @return id or null
   * @throws ParseException
   */
  private String isExist(Component eventIcal, Date startDateReccurent, Date endDateReccurent,
      String startHourReccurent) throws ParseException {
    String description = getFieldEvent(eventIcal.getProperty(Property.DESCRIPTION));
    String name = getName(eventIcal, description);
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
    return findEvent(name, startDate, startHour, endDate);
  }

  private String findEvent(final String name, final String startDate, final String startHour,
      final String endDate) {
    Collection<JournalHeader> events =
        calendarService.getPeriodSchedulablesForUser(startDate, endDate,
            agendaSessionController.getAgendaUserId(), null, agendaSessionController.
                getParticipationStatus().getString());
    for (JournalHeader eventAgenda : events) {
      if (eventAgenda.getName().equals(name) &&
          DateUtil.date2SQLDate(eventAgenda.getStartDate()).equals(startDate)) {
        if (isDefined(eventAgenda.getStartHour()) && isDefined(startHour)) {
          if (eventAgenda.getStartHour().equals(startHour)) {
            return eventAgenda.getId();
          }
        } else {
          return eventAgenda.getId();
        }
      }
    }
    return null;
  }

  private String getName(final Component eventIcal, final String defaultName) {
    String name = getFieldEvent(eventIcal.getProperty(Property.SUMMARY));
    if (!isDefined(name)) {
      if (isDefined(defaultName)) {
        name = defaultName;
      } else {
        name = " ";
      }
    }
    return name;
  }

  /**
   * Add or update categories of the event
   * @param eventIcal
   * @param idEvent the event identifier
   * @throws Exception
   */
  private void processCategories(Component eventIcal, String idEvent) throws AgendaException {
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
          if (categIcal.equals(WebEncodeHelper.htmlStringToJavaString(category.getName()))) {
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
    if (calendarService == null) {
      calendarService = ServiceProvider.getService(SilverpeasCalendar.class);
    }
  }

  /**
   * getDay from a givent string date parameter
   * @param dateTime
   * @return Date from a datetime string
   * @throws ParseException
   */
  private Date getDay(String dateTime) throws ParseException {
    Objects.requireNonNull(dateTime);
    final Date day;
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
    Objects.requireNonNull(dateTime);
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
      fieldValue = transformStringForBD(property.getValue());
    }

    return fieldValue;
  }

  /**
   * Get start dates of a recurrent Event
   * @param event
   * @return Collection of DateTime
   */
  private static Collection<Date> getRecurrenceDates(VEvent event) {
    RRule rule = event.getProperty(Property.RRULE);
    if (rule != null) {
      Recur recur = rule.getRecur();
      DateTime startDate = new DateTime(event.getStartDate().getDate());
      long interval = YEAR * 2;
      if (Recur.Frequency.YEARLY.equals(recur.getFrequency())) {
        interval *= 5;
      }
      DateTime endDate = new DateTime(startDate.getTime() + (interval));
      return recur.getDates(startDate, endDate, Value.DATE_TIME);
    }
    return Collections.emptyList();
  }

  /**
   * This method transforms a string to replace the 'special' caracters to store them correctly in
   * the database
   * @param sText a single text which may contains 'special' caracters
   * @return Returns the transformed text without specific codes.
   */
  public static String transformStringForBD(String sText) {
    if (!isDefined(sText)) {
      return "";
    }

    int nStringLength = sText.length();
    StringBuilder resSB = new StringBuilder(nStringLength + 10);

    for (int i = 0; i < nStringLength; i++) {
      switch (sText.charAt(i)) {
        case '€':
          resSB.append('\u20ac'); // Euro Symbol
          break;
        // case '’':
        case '\u2019':
          resSB.append('\''); // ’ quote word
          break;
        default:
          resSB.append(sText.charAt(i));
      }
    }


    return resSB.toString();
  }
}