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
package org.silverpeas.core.importexport.ical.ical4j;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventRecurrence;
import org.silverpeas.core.date.Datable;
import org.silverpeas.core.importexport.EncodingException;
import org.silverpeas.core.importexport.ical.ICalCodec;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.html.HtmlCleaner;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.apache.commons.lang3.CharEncoding;
import org.apache.tika.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An iCal encoder/decoder by using the iCal4J library.
 */
@Singleton
public class ICal4JICalCodec implements ICalCodec {

  private OffLineInetAddressHostInfo hostInfo = new OffLineInetAddressHostInfo();

  @Inject
  private ICal4JDateCodec iCal4JDateCodec;

  @Inject
  private ICal4JRecurrenceCodec iCal4JRecurrenceCodec;

  @Override
  @SuppressWarnings("unchecked")
  public String encode(List<CalendarEvent> events) {

    if (events == null || events.isEmpty()) {
      throw new IllegalArgumentException("The calendar events must be defined to encode them");
    }
    Calendar calendarIcs = new Calendar();
    calendarIcs.getProperties().add(new ProdId("-//Silverpeas//iCal4j 1.1//FR"));
    calendarIcs.getProperties().add(Version.VERSION_2_0);
    calendarIcs.getProperties().add(CalScale.GREGORIAN);
    List<VEvent> iCalEvents = new ArrayList<>();
    ByteArrayOutputStream output = new ByteArrayOutputStream(10240);
    for (CalendarEvent event : events) {
      Date startDate = iCal4JDateCodec.encode(event.getStartDate());
      Date endDate = iCal4JDateCodec.encode(event.getEndDate());
      VEvent iCalEvent;
      if (event.isOnAllDay() && startDate.equals(endDate)) {
        iCalEvent = new VEvent(startDate, event.getTitle());
      } else {
        iCalEvent = new VEvent(startDate, endDate, event.getTitle());
      }

      // Generate UID
      iCalEvent.getProperties().add(generateUid(event));

      // Add recurring data if any
      if (event.isRecurring()) {
        CalendarEventRecurrence eventRecurrence = event.getRecurrence();
        Recur recur = iCal4JRecurrenceCodec.encode(eventRecurrence);
        iCalEvent.getProperties().add(new RRule(recur));
        iCalEvent.getProperties().add(exceptionDatesFrom(eventRecurrence));
      }

      // Add Description if any
      if (StringUtil.isDefined(event.getDescription())) {
        HtmlCleaner cleaner = new HtmlCleaner();
        String plainText = "";
        try {
          plainText = cleaner.cleanHtmlFragment(event.getDescription());
        } catch (Exception e) {
          // do nothing
        }
        iCalEvent.getProperties().add(new Description(plainText));
        iCalEvent.getProperties().add(new Html(event.getDescription()));
      }

      // Add Classification
      iCalEvent.getProperties().add(new Clazz(event.getAccessLevel()));
      // Add Priority
      iCalEvent.getProperties().add(new Priority(event.getPriority()));

      // Add location if any
      if (!event.getLocation().isEmpty()) {
        iCalEvent.getProperties().add(new Location(event.getLocation()));
      }

      // Add event URL if any
      if (event.getUrl() != null) {
        try {
          iCalEvent.getProperties().add(new Url(event.getUrl().toURI()));
        } catch (URISyntaxException ex) {
          throw new EncodingException(ex.getMessage(), ex);
        }
      }

      // Add Categories
      TextList categoryList = new TextList(event.getCategories().asArray());
      if (!categoryList.isEmpty()) {
        iCalEvent.getProperties().add(new Categories(categoryList));
      }
      // Add attendees
      for (String attendee : event.getAttendees().asList()) {
        try {
          iCalEvent.getProperties().add(new Attendee(attendee));
        } catch (URISyntaxException ex) {
          throw new EncodingException("Malformed attendee URI: " + attendee, ex);
        }
      }

      iCalEvents.add(iCalEvent);
    }
    calendarIcs.getComponents().addAll(iCalEvents);
    CalendarOutputter outputter = new CalendarOutputter();
    try {
      outputter.output(calendarIcs, output);
      return output.toString(CharEncoding.UTF_8);
    } catch (Exception ex) {
      throw new EncodingException("The encoding of the events in iCal formatted text has failed!",
          ex);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

  private ExDate exceptionDatesFrom(final CalendarEventRecurrence recurrence) {
    List<Datable<?>> exceptionDates = recurrence.getExceptionDates();
    DateList exDatesList = exceptionDates.stream().map(iCal4JDateCodec::encode)
        .collect(Collectors.toCollection(DateList::new));
    return new ExDate(exDatesList);
  }

  private Uid generateUid(CalendarEvent event) {
    StringBuffer b = new StringBuffer();
    b.append(event.getId());
    if(this.hostInfo != null) {
      b.append('@');
      b.append(this.hostInfo.getHostName());
    }

    return new Uid(b.toString());
  }
}
