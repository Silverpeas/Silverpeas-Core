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
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserFactory;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.CalendarEventOccurrenceBuilder;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.calendar.icalendar.ICalendarImporter;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaEntityReflection;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Implementation of the {@link ICalendarImporter} interface by using the iCal4J library to perform
 * the deserialization of calendar events in the iCalendar format.
 * @author mmoquillon
 */
@Service
public class ICal4JImporter implements ICalendarImporter {

  private static final String CALENDAR_SETTINGS = "org.silverpeas.calendar.settings.calendar";

  @Inject
  private ICal4JDateCodec iCal4JDateCodec;
  @Inject
  private ICal4JRecurrenceCodec iCal4JRecurrenceCodec;

  @PostConstruct
  private void init() {
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
  }

  @Override
  public void imports(final ImportDescriptor descriptor,
      final Consumer<Stream<Pair<CalendarEvent, List<CalendarEventOccurrence>>>> consumer)
      throws ImportException {
    try {
      final CalendarBuilder builder =
          new CalendarBuilder(CalendarParserFactory.getInstance().get());
      final Calendar calendar = builder.build(getCalendarInputStream(descriptor));
      if (calendar.getComponents().isEmpty()) {
        consumer.accept(Stream.empty());
        return;
      }
      calendar.validate();

      final Map<String, List<VEvent>> readEvents = new LinkedHashMap<>();
      calendar.getComponents().forEach(component -> {
        if (component instanceof VEvent) {
          VEvent vEvent = (VEvent) component;
          String vEventId = vEvent.getUid().getValue();
          List<VEvent> vEvents = readEvents.computeIfAbsent(vEventId, k -> new ArrayList<>());
          ofNullable(vEvent.getRecurrenceId()).ifPresentOrElse(
              r -> vEvents.add(vEvent),
              () -> vEvents.add(0, vEvent));
        } else {
          SilverLogger.getLogger(this)
              .debug(() -> component instanceof VTimeZone ?
                  format("iCalendar VTimeZone '%s' detected", component.getName()) :
                  format("iCalendar component '%s' is not handled", component.getName()));
        }
      });
      List<Pair<CalendarEvent, List<CalendarEventOccurrence>>> events =
          new ArrayList<>(readEvents.size());
      readEvents.forEach((vEventId, vEvents) -> {

        // For now the following stuffs are not handled:
        // - the attendees
        // - triggers
        VEvent vEvent = vEvents.remove(0);
        CalendarEvent event = eventFromICalEvent(vEventId, vEvent);

        // Occurrences
        List<CalendarEventOccurrence> occurrences = new ArrayList<>(vEvents.size());
        vEvents.forEach(v -> {
          CalendarEventOccurrence occurrence = occurrenceFromICalEvent(event, v);
          occurrences.add(occurrence);
        });

        if (!event.isRecurrent() && !occurrences.isEmpty()) {
          SilverLogger.getLogger(this).warn(
              "event with uuid {0} has no recurrence set whereas {1,choice, 1#one linked " +
                  "occurrence exists| 1<{1} linked occurrences exist}... Setting a default " +
                  "recurrence (RRULE:FREQ=DAILY;COUNT=1) to get correct data for Silverpeas",
              event.getExternalId(), occurrences.size());
          event.recur(Recurrence.every(1, TimeUnit.DAY).until(1));
        }

        // New event to perform
        events.add(Pair.of(event, occurrences));
      });

      // The events will be performed by the caller
      consumer.accept(events.stream());

    } catch (IOException | ParserException e) {
      throw new ImportException(e);
    }
  }

  private InputStream getCalendarInputStream(final ImportDescriptor descriptor) throws IOException {
    final SettingBundle settings = ResourceLocator.getSettingBundle(CALENDAR_SETTINGS);
    final String replacements =
        settings.getString("calendar.import.ics.file.replace.before.process", "");
    if (isDefined(replacements)) {
      Mutable<String> icsContent = Mutable.of(IOUtils.toString(descriptor.getInputStream(),
          Charsets.UTF_8));
      Arrays.stream(replacements.split(";")).map(r -> {
        String[] replacement = r.split("[/]");
        return Pair.of(replacement[0], replacement[1]);
      }).forEach(r -> {
        String previous = icsContent.get();
        icsContent.set(previous.replaceAll(r.getLeft(), r.getRight()));
      });
      return toInputStream(icsContent.get(), Charsets.UTF_8);
    }
    return descriptor.getInputStream();
  }

  private CalendarEventOccurrence occurrenceFromICalEvent(final CalendarEvent event,
      final VEvent vEvent) {
    // The original start date
    Temporal originalStartDate = iCal4JDateCodec.decode(vEvent.getRecurrenceId().getDate());
    // The occurrence
    CalendarEventOccurrence occurrence = CalendarEventOccurrenceBuilder
        .forEvent(event)
        .startingAt(originalStartDate)
        .endingAt(originalStartDate.plus(1, ChronoUnit.DAYS))
        .build();
    // The period
    occurrence.setPeriod(extractPeriod(vEvent));
    // Component data
    copyICalEventToComponent(vEvent, occurrence.asCalendarComponent());
    return occurrence;
  }

  private CalendarEvent eventFromICalEvent(final String vEventId, final VEvent vEvent) {
    // The period
    Period period = extractPeriod(vEvent);
    CalendarEvent event = CalendarEvent.on(period);

    // External Id
    event.withExternalId(vEventId);

    // Visibility
    if (vEvent.getClassification() != null) {
      event.withVisibilityLevel(VisibilityLevel.valueOf(vEvent.getClassification().getValue()));
    }

    // Categories
    if (vEvent.getProperty(Property.CATEGORIES) != null) {
      Categories categories = vEvent.getProperty(Property.CATEGORIES);
      Iterator<String> categoriesIt = categories.getCategories().iterator();
      while (categoriesIt.hasNext()) {
        event.getCategories().add(categoriesIt.next());
      }
    }

    // Recurrence
    if (vEvent.getProperty(Property.RRULE) != null) {
      Recurrence recurrence = iCal4JRecurrenceCodec.decode(vEvent);
      event.recur(recurrence);
    }

    // Component data
    copyICalEventToComponent(vEvent, event.asCalendarComponent());
    return event;
  }

  private void copyICalEventToComponent(final VEvent vEvent, final CalendarComponent component) {
    // Title
    if (vEvent.getSummary() != null) {
      component.setTitle(vEvent.getSummary().getValue().trim());
    }

    // Description
    Property description = vEvent.getProperty(HtmlProperty.X_ALT_DESC);
    if (description == null) {
      description =
          vEvent.getDescription() != null ? vEvent.getDescription() : new Description("");
    }
    component.setDescription(description.getValue().trim());

    // Location
    if (vEvent.getLocation() != null) {
      component.setLocation(vEvent.getLocation().getValue().trim());
    }

    // URL
    if (vEvent.getUrl() != null) {
      component.getAttributes().set("url", vEvent.getUrl().getValue().trim());
    }

    // Priority
    if (vEvent.getPriority() != null) {
      component.setPriority(
          org.silverpeas.core.calendar.Priority.fromICalLevel(vEvent.getPriority().getLevel()));
    }

    // Technical data which are used for detection of modifications.
    // THESE DATES MUST NOT HAVE TO BE REGISTERED INTO THE PERSISTENCE.
    // Indeed it is used by ICalendarEventImportProcessor#wasUpdated() in order to detect the
    // events modified into external calendar repository.
    if (vEvent.getCreated() != null) {
      JpaEntityReflection.setCreationData(component, OperationContext.getFromCache().getUser(),
          vEvent.getCreated().getDate());
    }
    if (vEvent.getLastModified() != null) {
      JpaEntityReflection.setUpdateData(component, OperationContext.getFromCache().getUser(),
          vEvent.getLastModified().getDate());
    }
  }

  private Period extractPeriod(final VEvent vEvent) {
    final Date startDate = vEvent.getStartDate().getDate();
    Date endDate = vEvent.getEndDate().getDate();
    if (endDate == null && !(startDate instanceof DateTime)) {
      endDate = startDate;
    }
    final Temporal startTemporal = iCal4JDateCodec.decode(startDate);
    Temporal endTemporal = iCal4JDateCodec.decode(endDate);
    if (endTemporal instanceof OffsetDateTime && startTemporal.equals(endTemporal)) {
      endTemporal = endTemporal.plus(1, ChronoUnit.HOURS);
    }
    return Period.between(startTemporal, endTemporal);
  }

}
