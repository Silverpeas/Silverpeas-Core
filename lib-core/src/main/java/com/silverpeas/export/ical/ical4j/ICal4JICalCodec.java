/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.export.ical.ical4j;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.calendar.CalendarEventRecurrence;
import com.silverpeas.calendar.Datable;
import com.silverpeas.export.EncodingException;
import com.silverpeas.export.ical.ICalCodec;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang3.CharEncoding;
import org.apache.tika.io.IOUtils;

import static com.silverpeas.export.ical.ical4j.ICal4JDateCodec.anICal4JDateCodec;
import static com.silverpeas.export.ical.ical4j.ICal4JRecurrenceCodec.anICal4JRecurrenceCodec;

/**
 * An iCal encoder/decoder by using the iCal4J library.
 */
@Named("iCalCodec")
public class ICal4JICalCodec implements ICalCodec {

  private UidGenerator generator = new UidGenerator(new OffLineInetAddressHostInfo(), Uid.UID);

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
    List<VEvent> iCalEvents = new ArrayList<VEvent>();
    ByteArrayOutputStream output = new ByteArrayOutputStream(10240);
    for (CalendarEvent event : events) {
      Date startDate = anICal4JDateCodec().encode(event.getStartDate());
      Date endDate = anICal4JDateCodec().encode(event.getEndDate());
      VEvent iCalEvent;
      if (event.isOnAllDay() && startDate.equals(endDate)) {
        iCalEvent = new VEvent(startDate, event.getTitle());
      } else {
        iCalEvent = new VEvent(startDate, endDate, event.getTitle());
      }

      // Generate UID
      iCalEvent.getProperties().add(generator.generateUid());
      // Add recurring data if any
      if (event.isRecurring()) {
        CalendarEventRecurrence eventRecurrence = event.getRecurrence();
        Recur recur = anICal4JRecurrenceCodec().encode(eventRecurrence);
        iCalEvent.getProperties().add(new RRule(recur));
        iCalEvent.getProperties().add(exceptionDatesFrom(eventRecurrence));
      }
      // Add Description
      iCalEvent.getProperties().add(new Description(event.getDescription()));
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
    DateList exDatesList = new DateList();
    ICal4JDateCodec dateCodec = ICal4JDateCodec.anICal4JDateCodec();
    for (Datable<?> anExceptionDate : exceptionDates) {
      exDatesList.add(dateCodec.encode(anExceptionDate));
    }
    return new ExDate(exDatesList);
  }
}
