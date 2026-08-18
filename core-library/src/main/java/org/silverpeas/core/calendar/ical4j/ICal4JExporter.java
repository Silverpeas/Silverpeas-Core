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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableMethod;
import net.fortuna.ical4j.model.property.immutable.ImmutableStatus;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.InternalAttendee;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.calendar.icalendar.ICalendarExporter;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.html.HtmlCleaner;
import org.silverpeas.kernel.logging.SilverLogger;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.ACCEPTED;
import static org.silverpeas.core.calendar.CalendarEventUtil.formatTitle;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Implementation of the {@link ICalendarExporter} interface by using the iCal4J library to perform
 * the serialization of the events of a calendar in text in the iCalendar format.
 * @author mmoquillon
 */
@Service
public class ICal4JExporter implements ICalendarExporter {

  private static final String MAIL_TO = "mailto:";
  private static final String HIDDEN_DATA = "";
  /**
   * The length at which the lines of the produced iCalendar text are folded. It is set explicitly
   * because, otherwise, iCal4J deduces it from the Outlook compatibility hint, and this hint is a
   * process wide setting that is enabled by {@link ICal4JImporter} for its own parsing needs.
   */
  private static final int FOLD_LENGTH = FoldingWriter.REDUCED_FOLD_LENGTH;

  @Inject
  private ICal4JDateCodec iCal4JDateCodec;
  @Inject
  private ICal4JRecurrenceCodec iCal4JRecurrenceCodec;

  @PostConstruct
  private void init() {
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
  }

  @Override
  public void exports(final ExportDescriptor descriptor,
      final Supplier<Stream<CalendarEvent>> supplier) throws ExportException {
    try {
      Calendar calendar = descriptor.getParameter(CALENDAR);

      net.fortuna.ical4j.model.Calendar iCalCalendar = new net.fortuna.ical4j.model.Calendar();
      iCalCalendar.add(new ProdId("-//Silverpeas//iCal4j 2.0//FR"));
      iCalCalendar.add(ImmutableVersion.VERSION_2_0);
      iCalCalendar.add(ImmutableCalScale.GREGORIAN);
      iCalCalendar.add(ImmutableMethod.PUBLISH);
      iCalCalendar.add(new Name(calendar.getTitle()));

      // Adding VTimeZone component (mandatory with Outlook)
      TimeZone tz = iCal4JDateCodec.getTimeZone(calendar.getZoneId());
      iCalCalendar.add(tz.getVTimeZone());

      // X Properties
      iCalCalendar.add(new XProperty("X-WR-CALNAME", calendar.getTitle()));
      iCalCalendar.add(new XProperty("X-WR-TIMEZONE", tz.getID()));

      try (Stream<CalendarEvent> events = supplier.get()) {
        events.forEach(event -> {
          VEvent iCalEvent =
              convertToICalEvent(descriptor, calendar, event, event.asCalendarComponent());
          if (event.isRecurrent()) {
            setICalRecurrence(event, iCalEvent);
            event.getPersistedOccurrences().stream()
                .sorted(CalendarEventOccurrence.COMPARATOR_BY_DATE_DESC)
                .forEach(occurrence -> {
                  VEvent occICalEvent = convertToICalEvent(descriptor, calendar, occurrence);
                  iCalCalendar.add(occICalEvent);
                });
          }
          iCalCalendar.add(iCalEvent);
        });
      }

      CalendarOutputter writer = new CalendarOutputter(true, FOLD_LENGTH);
      writer.output(iCalCalendar, descriptor.getOutputStream());
    } catch (Exception e) {
      throw new ExportException("The export of the events in iCal formatted text has failed!",
          e);
    }
  }

  private VEvent convertToICalEvent(final ExportDescriptor descriptor, final Calendar calendar,
      CalendarEventOccurrence occurrence) {
    final CalendarComponent occComponent = occurrence.asCalendarComponent();
    final CalendarComponent evtComponent = occurrence.getCalendarEvent().asCalendarComponent();
    VEvent occICalEvent =
        convertToICalEvent(descriptor, calendar, occurrence.getCalendarEvent(), occComponent);
    final Temporal occOrigStartDate =
        iCal4JDateCodec.encode(true, evtComponent, occurrence.getOriginalStartDate());
    occICalEvent.add(new RecurrenceId<>(occOrigStartDate));
    return occICalEvent;
  }

  private VEvent convertToICalEvent(final ExportDescriptor descriptor, final Calendar calendar,
      CalendarEvent event, final CalendarComponent component) {
    boolean mustHideData = mustHideData(descriptor, event);
    VEvent iCalEvent = initICalEvent(calendar, event, component, mustHideData);
    setICalUuid(event, iCalEvent);
    setICalDescription(component, iCalEvent, mustHideData);
    setICalVisibility(event, iCalEvent);
    setICalPriority(component, iCalEvent);
    setICalLocation(component, iCalEvent, mustHideData);
    setICalUrl(component, iCalEvent, mustHideData);
    setICalCategories(event, iCalEvent, mustHideData);
    setICalAttendees(component, iCalEvent, mustHideData);
    return iCalEvent;
  }

  private void setICalRecurrence(final CalendarEvent event, final VEvent iCalEvent) {
    Recur<Temporal> recur = iCal4JRecurrenceCodec.encode(event);
    iCalEvent.add(new RRule<>(recur));
    if (!event.getRecurrence().getExceptionDates().isEmpty()) {
      iCalEvent.add(iCal4JRecurrenceCodec.exceptionDates(event));
    }
  }

  private void setICalPriority(final CalendarComponent component, final VEvent iCalEvent) {
    iCalEvent.add(new Priority(component.getPriority().getICalLevel()));
  }

  private void setICalVisibility(final CalendarEvent event, final VEvent iCalEvent) {
    iCalEvent.add(new Clazz(event.getVisibilityLevel().name()));
  }

  private void setICalLocation(final CalendarComponent component, final VEvent iCalEvent,
      final boolean hideData) {
    if (!hideData && isDefined(component.getLocation())) {
      iCalEvent.add(new Location(component.getLocation()));
    }
  }

  private void setICalAttendees(final CalendarComponent component, final VEvent iCalEvent,
      final boolean hideData) {
    if (!hideData && component.getAttendees().isEmpty()) {
      iCalEvent.add(ImmutableStatus.VEVENT_CONFIRMED);
    } else if (!hideData) {
      iCalEvent.add(convertOrganizer(component.getCreator()));
      final Mutable<Status> mutableStatus = Mutable.of(ImmutableStatus.VEVENT_CONFIRMED);
      component.getAttendees().stream()
          .sorted(Comparator.comparing(org.silverpeas.core.calendar.Attendee::getId))
          .forEach(attendee -> {
            iCalEvent.add(convertAttendee(attendee));
            if (ACCEPTED != attendee.getParticipationStatus()) {
              mutableStatus.set(ImmutableStatus.VEVENT_TENTATIVE);
            }
          });
      iCalEvent.add(mutableStatus.get());
    }
  }

  private void setICalCategories(final CalendarEvent event, final VEvent iCalEvent,
      final boolean hideData) {
    if (!hideData) {
      TextList categoryList = new TextList(java.util.List.of(event.getCategories().asArray()));
      if (!categoryList.getTexts().isEmpty()) {
        iCalEvent.add(new Categories(categoryList));
      }
    }
  }

  private void setICalUrl(final CalendarComponent component, final VEvent iCalEvent,
      final boolean hideData) {
    Optional<String> url = hideData ? Optional.empty() : component.getAttributes().get("url");
    if (url.isPresent()) {
      try {
        iCalEvent.add(new Url(new URI(url.get())));
      } catch (URISyntaxException ex) {
        throw new SilverpeasRuntimeException(ex.getMessage(), ex);
      }
    }
  }

  private VEvent initICalEvent(final Calendar calendar, final CalendarEvent event,
      final CalendarComponent component, final boolean hideData) {
    // ICal4J period
    final Temporal startDate = iCal4JDateCodec
        .encode(event.isRecurrent(), component, component.getPeriod().getStartDate());
    final Temporal endDate =
        iCal4JDateCodec.encode(event.isRecurrent(), component, component.getPeriod().getEndDate());

    Instant createdDate = component.getCreationDate().toInstant();
    Instant lastUpdateDate = component.getLastUpdateDate().toInstant();

    // ICal4J event
    final String title = hideData ? HIDDEN_DATA :
        formatTitle(component, calendar.getComponentInstanceId(), true);
    VEvent iCalEvent = component.getPeriod().isInDays() && startDate.equals(endDate) ?
        new VEvent(startDate, title) : new VEvent(startDate, endDate, title);
    iCalEvent.add(new Created(createdDate));
    iCalEvent.add(new LastModified(lastUpdateDate));
    iCalEvent.add(new Sequence((int) component.getSequence()));
    return iCalEvent;
  }

  private void setICalDescription(final CalendarComponent component, final VEvent iCalEvent,
      final boolean hideData) {
    final String description = hideData ? HIDDEN_DATA : component.getDescription();
    if (StringUtil.isDefined(description)) {
      HtmlCleaner cleaner = new HtmlCleaner();
      String plainText = "";
      try {
        plainText = cleaner.cleanHtmlFragment(description);
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
      }
      iCalEvent.add(new Description(plainText));
      iCalEvent.add(new HtmlProperty(description));
    }
  }

  private void setICalUuid(final CalendarEvent event, final VEvent iCalEvent) {
    final String eventId = event.getExternalId() != null ? event.getExternalId() : event.getId();
    iCalEvent.add(new Uid(eventId));
  }

  /**
   * Converts a Silverpeas user into an ICal4J organizer.
   * @param user the user which is the organizer.
   * @return the corresponding ICal4J organizer.
   */
  private Organizer convertOrganizer(final User user) {
    final Organizer iCalEventOrganizer =
        isDefined(user.getEmailAddress()) ? new Organizer(MAIL_TO + user.getEmailAddress()) :
            new Organizer();
    iCalEventOrganizer.add(new Cn(user.getDisplayedName()));
    return iCalEventOrganizer;
  }

  /**
   * Converts a Silverpeas attendee into an ICal4J one.
   * @param attendee a silverpeas attendee.
   * @return the corresponding ICal4J attendee.
   */
  private Attendee convertAttendee(final org.silverpeas.core.calendar.Attendee attendee) {
    return convertAttendee(attendee, attendee.getParticipationStatus());
  }

  /**
   * Converts a Silverpeas attendee into an ICal4J one.
   * @param attendee a silverpeas attendee.
   * @param participationStatus the participation status to set
   * @return the corresponding ICal4J attendee.
   */
  private Attendee convertAttendee(final org.silverpeas.core.calendar.Attendee attendee,
      final org.silverpeas.core.calendar.Attendee.ParticipationStatus participationStatus) {
    final Attendee iCalEventAttendee;
    if (attendee instanceof InternalAttendee internalAttendee) {
      final String displayedName = internalAttendee.getUser().getDisplayedName();
      iCalEventAttendee = isDefined(internalAttendee.getUser().getEmailAddress()) ?
          new Attendee(MAIL_TO + internalAttendee.getUser().getEmailAddress()) :
          new Attendee(MAIL_TO + displayedName);
      iCalEventAttendee.add(new Cn(displayedName));
    } else {
      iCalEventAttendee = new Attendee(MAIL_TO + attendee.getId());
      iCalEventAttendee.add(new Cn(attendee.getId()));
    }
    iCalEventAttendee.add(CuType.INDIVIDUAL);
    iCalEventAttendee.add(Rsvp.TRUE);
    convertPresenceStatus(attendee.getPresenceStatus())
        .ifPresent(role -> iCalEventAttendee.add(role));
    convertParticipationStatus(participationStatus)
        .ifPresent(partStat -> iCalEventAttendee.add(partStat));
    return iCalEventAttendee;
  }

  /**
   * Converts a Silverpeas presence status into a ICal4J one.
   * @param status the silverpeas presence status.
   * @return the optional corresponding ICal4J presence status.
   */
  private Optional<Role> convertPresenceStatus(
      org.silverpeas.core.calendar.Attendee.PresenceStatus status) {
    switch (status) {
      case REQUIRED:
        return Optional.of(Role.REQ_PARTICIPANT);
      case OPTIONAL:
        return Optional.of(Role.OPT_PARTICIPANT);
      default:
        return Optional.empty();
    }
  }

  /**
   * Converts a Silverpeas participation status into a ICal4J one.
   * @param status the silverpeas participation status.
   * @return the optional corresponding ICal4J participation status.
   */
  private Optional<PartStat> convertParticipationStatus(
      org.silverpeas.core.calendar.Attendee.ParticipationStatus status) {
    switch (status) {
      case ACCEPTED:
        return Optional.of(PartStat.ACCEPTED);
      case DECLINED:
        return Optional.of(PartStat.DECLINED);
      case TENTATIVE:
        return Optional.of(PartStat.TENTATIVE);
      case DELEGATED:
        return Optional.of(PartStat.DELEGATED);
      default:
        return Optional.of(PartStat.NEEDS_ACTION);
    }
  }

  /**
   * Indicates from the descriptor data and the event if data must be hidden.
   * @param descriptor the descriptor data.
   * @param event the event data.
   * @return true if data must be hidden, false otherwise.
   */
  private boolean mustHideData(ExportDescriptor descriptor, CalendarEvent event) {
    final Object value = descriptor.getParameter(HIDE_PRIVATE_DATA);
    final boolean required = (value instanceof Boolean && (Boolean) value) ||
        (value instanceof String && StringUtil.getBooleanValue((String) value));
    return required && VisibilityLevel.PRIVATE == event.getVisibilityLevel();
  }
}
