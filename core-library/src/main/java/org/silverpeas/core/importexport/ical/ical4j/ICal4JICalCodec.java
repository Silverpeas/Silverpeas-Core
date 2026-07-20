/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.importexport.ical.ical4j;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.ical4j.HtmlProperty;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.importexport.EncodingException;
import org.silverpeas.core.importexport.ical.ICalCodec;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.html.HtmlCleaner;
import org.silverpeas.kernel.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An iCal encoder/decoder by using the iCal4J library.
 */
@Bean
public class ICal4JICalCodec implements ICalCodec {

  private final OffLineInetAddressHostInfo hostInfo = new OffLineInetAddressHostInfo();

  private final ICal4JDateCodec iCal4JDateCodec;
  private final ICal4JRecurrenceCodec iCal4JRecurrenceCodec;

  @Inject
  public ICal4JICalCodec(final ICal4JDateCodec iCal4JDateCodec,
      final ICal4JRecurrenceCodec iCal4JRecurrenceCodec) {
    this.iCal4JDateCodec = iCal4JDateCodec;
    this.iCal4JRecurrenceCodec = iCal4JRecurrenceCodec;
  }

  @Override
  public String encode(List<CalendarEvent> events) {

    if (events == null || events.isEmpty()) {
      throw new IllegalArgumentException("The calendar events must be defined to encode them");
    }

    Calendar calendarIcs = new Calendar();
    calendarIcs.add(new ProdId("-//Silverpeas//iCal4j 1.1//FR"));
    calendarIcs.add(ImmutableVersion.VERSION_2_0);
    calendarIcs.add(ImmutableCalScale.GREGORIAN);

    // adding VTimeZone component (mandatory with Outlook)
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    VTimeZone tz = registry.getTimeZone("Europe/Paris").getVTimeZone();
    calendarIcs.add(tz);

    List<VEvent> iCalEvents = new ArrayList<>();
    ByteArrayOutputStream output = new ByteArrayOutputStream(10240);
    for (CalendarEvent event : events) {
      Temporal startDate = iCal4JDateCodec.encode(event.getStartDate());
      Temporal endDate = iCal4JDateCodec.encode(event.getEndDate());
      VEvent iCalEvent = getICalEvent(event, startDate, endDate);
      iCalEvents.add(iCalEvent);
    }
    iCalEvents.forEach(calendarIcs::add);
    CalendarOutputter outputter = new CalendarOutputter();
    try {
      outputter.output(calendarIcs, output);
      return output.toString(Charsets.UTF_8);
    } catch (Exception ex) {
      throw new EncodingException("The encoding of the events in iCal formatted text has failed!",
          ex);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

  @Nonnull
  private VEvent getICalEvent(final CalendarEvent event, final Temporal startDate,
      final Temporal endDate) {
    VEvent iCalEvent;
    if (event.isOnAllDay() && startDate.equals(endDate)) {
      iCalEvent = new VEvent(startDate, event.getTitle());
    } else {
      iCalEvent = new VEvent(startDate, endDate, event.getTitle());
    }
    // Generate UID
    iCalEvent.add(generateUid(event));

    // Add recurring data if any
    if (event.isRecurrent()) {
      Recur<Temporal> recur = iCal4JRecurrenceCodec.encode(event);
      iCalEvent.add(new RRule<>(recur));
      iCalEvent.add(iCal4JRecurrenceCodec.exceptionDates(event));
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
      iCalEvent.add(new Description(plainText));
      iCalEvent.add(new HtmlProperty(event.getDescription()));
    }

    // Add Visibility
    iCalEvent.add(new Clazz(event.getVisibilityLevel().name()));
    // Add Priority
    iCalEvent.add(new Priority(event.getPriority().getICalLevel()));

    // Add location if any
    Optional<String> location = Optional.ofNullable(event.getLocation());
    location.ifPresent(s -> iCalEvent.add(new Location(s)));

    // Add event URL if any
    Optional<String> url = event.getAttributes().get("url");
    if (url.isPresent()) {
      try {
        iCalEvent.add(new Url(new URI(url.get())));
      } catch (URISyntaxException ex) {
        throw new EncodingException(ex.getMessage(), ex);
      }
    }

    // Add Categories
    TextList categoryList = new TextList(java.util.List.of(event.getCategories().asArray()));
    if (!categoryList.getTexts().isEmpty()) {
      iCalEvent.add(new Categories(categoryList));
    }
    // Add attendees
    event.getAttendees().forEach(a -> iCalEvent.add(new Attendee(a.getId())));

    return iCalEvent;
  }

  private Uid generateUid(CalendarEvent event) {
    StringBuilder b = new StringBuilder();
    b.append(event.getId());
    if(StringUtil.isDefined(this.hostInfo.getHostName())) {
      b.append('@');
      b.append(this.hostInfo.getHostName());
    }

    return new Uid(b.toString());
  }
}
