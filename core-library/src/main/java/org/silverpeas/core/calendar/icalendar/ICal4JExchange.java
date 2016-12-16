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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.calendar.icalendar;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.*;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.event.Attendee.ParticipationStatus;
import org.silverpeas.core.calendar.event.Attendee.PresenceStatus;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.InternalAttendee;
import org.silverpeas.core.calendar.ical4j.Html;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.html.HtmlCleaner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.silverpeas.core.calendar.event.Attendee.ParticipationStatus.ACCEPTED;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Implementation of {@link ICalendarExchange} interface based on the use of ICal4J API.
 * @author Yohann Chastagnier
 */
@Singleton
public class ICal4JExchange implements ICalendarExchange {

  private final ICal4JDateCodec iCal4JDateCodec;
  private final ICal4JRecurrenceCodec iCal4JRecurrenceCodec;

  @Inject
  public ICal4JExchange(final ICal4JDateCodec iCal4JDateCodec,
      final ICal4JRecurrenceCodec iCal4JRecurrenceCodec) {
    this.iCal4JDateCodec = iCal4JDateCodec;
    this.iCal4JRecurrenceCodec = iCal4JRecurrenceCodec;
  }

  @Override
  public void export(final ICalendarExport export) throws ICalendarException {
    try {

      Calendar calendarIcs = new Calendar();
      calendarIcs.getProperties().add(new ProdId("-//Silverpeas//iCal4j 2.0//FR"));
      calendarIcs.getProperties().add(Version.VERSION_2_0);
      calendarIcs.getProperties().add(CalScale.GREGORIAN);

      // Adding VTimeZone component (mandatory with Outlook)
      TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
      TimeZone tz = registry.getTimeZone("UTC");
      calendarIcs.getComponents().add(tz.getVTimeZone());

      try (Stream<CalendarEvent> events = export.streamCalendarEvents()) {
        events.forEach(event -> {

          long durationInSeconds =
              event.getStartDateTime().until(event.getEndDateTime(), ChronoUnit.SECONDS);

          // ICal4J period
          Date startDate = iCal4JDateCodec.encode(event.getStartDateTime());
          Date endDate = iCal4JDateCodec.encode(event.getEndDateTime());
          DateTime createdDate =
              iCal4JDateCodec.encode(event.getCreateDate().toInstant().atOffset(ZoneOffset.UTC));
          DateTime lastUpdateDate = iCal4JDateCodec
              .encode(event.getLastUpdateDate().toInstant().atOffset(ZoneOffset.UTC));

          // ICal4J event
          VEvent iCalEvent = new VEvent(startDate, endDate, event.getTitle());
          iCalEvent.getProperties().add(new Created(createdDate));
          iCalEvent.getProperties().add(new LastModified(lastUpdateDate));
          iCalEvent.getProperties().add(new Sequence(event.getVersion().intValue()));

          // Set UID which is the one of the event
          iCalEvent.getProperties().add(new Uid(event.getId()));

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
          iCalEvent.getProperties().add(new Clazz(event.getVisibilityLevel().name()));

          // Add Priority
          iCalEvent.getProperties().add(new Priority(event.getPriority().ordinal()));

          // Add location if any
          if (isDefined(event.getLocation())) {
            iCalEvent.getProperties().add(new Location(event.getLocation()));
          }

          // Add event URL if any
          Optional<String> url = event.getAttributes().get("url");
          if (url.isPresent()) {
            try {
              iCalEvent.getProperties().add(new Url(new URI(url.get())));
            } catch (URISyntaxException ex) {
              throw new SilverpeasRuntimeException(ex.getMessage(), ex);
            }
          }

          // Add Categories
          TextList categoryList = new TextList(event.getCategories().asArray());
          if (!categoryList.isEmpty()) {
            iCalEvent.getProperties().add(new Categories(categoryList));
          }
          // Add attendees
          Map<OffsetDateTime, List<Pair<org.silverpeas.core.calendar.event.Attendee, ParticipationStatus>>>
              participationRecurrence = new HashMap<>();
          if (event.getAttendees().isEmpty()) {
            iCalEvent.getProperties().add(Status.VEVENT_CONFIRMED);
          } else {
            iCalEvent.getProperties().add(convertOrganizer(event.getCreator()));
            final Mutable<Status> mutableStatus = Mutable.of(Status.VEVENT_CONFIRMED);
            event.getAttendees().stream()
                .sorted(Comparator.comparing(org.silverpeas.core.calendar.event.Attendee::getId))
                .forEach(attendee -> {
                  iCalEvent.getProperties().add(convertAttendee(attendee));

                  if (ACCEPTED != attendee.getParticipationStatus()) {
                    mutableStatus.set(Status.VEVENT_TENTATIVE);
                  }
                  if (event.isRecurrent()) {
                    attendee.getParticipationOn().getAll().entrySet().stream()
                        .sorted(Comparator.comparing(Map.Entry::getKey)).forEach(entry -> {
                      OffsetDateTime date = entry.getKey();
                      ParticipationStatus status = entry.getValue();
                      if (ACCEPTED != status) {
                        mutableStatus.set(Status.VEVENT_TENTATIVE);
                      }
                      participationRecurrence.computeIfAbsent(date, dateTime -> {
                        List<Pair<org.silverpeas.core.calendar.event.Attendee, ParticipationStatus>>
                            attSts = new ArrayList<>();
                        event.getAttendees().stream().sorted(Comparator
                            .comparing(org.silverpeas.core.calendar.event.Attendee::getId))
                            .forEach(a -> attSts.add(Pair.of(a, a.getParticipationStatus())));
                        return attSts;
                      });
                      participationRecurrence.computeIfPresent(date, (dateTime, pairs) -> {
                        ListIterator<Pair<org.silverpeas.core.calendar.event.Attendee, ParticipationStatus>>
                            it = pairs.listIterator();
                        while (it.hasNext()) {
                          if (it.next().getFirst().equals(attendee)) {
                            it.set(Pair.of(attendee, status));
                            break;
                          }
                        }
                        return pairs;
                      });
                    });
                  }
                });
            iCalEvent.getProperties().add(mutableStatus.get());
          }

          participationRecurrence.entrySet().stream()
              .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey())).forEach(entry -> {
            try {
              VEvent iCalAttPartRec = (VEvent) iCalEvent.copy();
              final OffsetDateTime dateKey = entry.getKey();
              final DateTime startDateAttPartRec = iCal4JDateCodec.encode(dateKey);
              final DateTime endDateAttPartRec =
                  iCal4JDateCodec.encode(dateKey.plusSeconds(durationInSeconds));
              iCalAttPartRec.getProperties().add(new RecurrenceId(startDateAttPartRec));
              ((DtStart) iCalAttPartRec.getProperty(Property.DTSTART)).setDate(startDateAttPartRec);
              ((DtEnd) iCalAttPartRec.getProperty(Property.DTEND)).setDate(endDateAttPartRec);
              iCalAttPartRec.getProperties()
                  .removeIf(property -> property.getName().equals(Property.ATTENDEE));
              entry.getValue().forEach(
                  p -> iCalAttPartRec.getProperties().add(convertAttendee(p.getFirst(), p.getSecond())));
              calendarIcs.getComponents().add(iCalAttPartRec);
            } catch (ParseException | IOException | URISyntaxException e) {
              throw new SilverpeasRuntimeException(e);
            }
          });

          // Add recurring data if any
          if (event.isRecurrent()) {
            Recurrence eventRecurrence = event.getRecurrence();
            Recur recur = iCal4JRecurrenceCodec.encode(eventRecurrence);
            iCalEvent.getProperties().add(new RRule(recur));
            final Set<OffsetDateTime> exceptionDates = eventRecurrence.getExceptionDates();
            if (!exceptionDates.isEmpty()) {
              iCalEvent.getProperties().add(new ExDate(iCal4JDateCodec.encode(exceptionDates)));
            }
          }

          calendarIcs.getComponents().add(iCalEvent);
        });
      }

      CalendarOutputter outputter = new CalendarOutputter();
      try {
        outputter.output(calendarIcs, export.getOutput());
      } catch (Exception ex) {
        throw new SilverpeasRuntimeException(
            "The encoding of the events in iCal formatted text has failed!", ex);
      }
    } catch (SilverpeasRuntimeException se) {
      throw new ICalendarException(se);
    }
  }

  /**
   * Converts a Silverpeas user into an ICal4J organizer.
   * @param user the user which is the organizer.
   * @return the corresponding ICal4J organizer.
   */
  private Organizer convertOrganizer(final User user) {
    try {
      final Organizer iCalEventOrganizer =
          isDefined(user.geteMail()) ? new Organizer("mailto:" + user.geteMail()) : new Organizer();
      iCalEventOrganizer.getParameters().add(new Cn(user.getDisplayedName()));
      return iCalEventOrganizer;
    } catch (URISyntaxException ex) {
      throw new SilverpeasRuntimeException("Malformed organizer URI: " + user, ex);
    }
  }

  /**
   * Converts a Silverpeas attendee into an ICal4J one.
   * @param attendee a silverpeas attendee.
   * @return the corresponding ICal4J attendee.
   */
  private Attendee convertAttendee(final org.silverpeas.core.calendar.event.Attendee attendee) {
    return convertAttendee(attendee, attendee.getParticipationStatus());
  }

  /**
   * Converts a Silverpeas attendee into an ICal4J one.
   * @param attendee a silverpeas attendee.
   * @param participationStatus the participation status to set
   * @return the corresponding ICal4J attendee.
   */
  private Attendee convertAttendee(final org.silverpeas.core.calendar.event.Attendee attendee,
      final ParticipationStatus participationStatus) {
    final Attendee iCalEventAttendee;
    try {
      if (attendee instanceof InternalAttendee) {
        InternalAttendee internalAttendee = (InternalAttendee) attendee;
        iCalEventAttendee = isDefined(internalAttendee.getUser().geteMail()) ?
            new Attendee("mailto:" + internalAttendee.getUser().geteMail()) : new Attendee();
        iCalEventAttendee.getParameters()
            .add(new Cn(internalAttendee.getUser().getDisplayedName()));
      } else {
        iCalEventAttendee = new Attendee("mailto:" + attendee.getId());
        iCalEventAttendee.getParameters().add(new Cn(attendee.getId()));
      }
      convertPresenceStatus(attendee.getPresenceStatus())
          .ifPresent(role -> iCalEventAttendee.getParameters().add(role));
      convertParticipationStatus(participationStatus)
          .ifPresent(partStat -> iCalEventAttendee.getParameters().add(partStat));
      return iCalEventAttendee;
    } catch (URISyntaxException ex) {
      throw new SilverpeasRuntimeException("Malformed attendee URI: " + attendee, ex);
    }
  }

  /**
   * Converts a Silverpeas presence status into a ICal4J one.
   * @param status the silverpeas presence status.
   * @return the optional corresponding ICal4J presence status.
   */
  private Optional<Role> convertPresenceStatus(PresenceStatus status) {
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
  private Optional<PartStat> convertParticipationStatus(ParticipationStatus status) {
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
}
