/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.event;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.CalendarTimeWindow;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;


/**
 * The occurrence of an event in a Silverpeas calendar. An event occurrence starts and ends at a
 * given date and it represents an event that has occurred or that is going to occur at a given
 * moment in time.
 */
public class CalendarEventOccurrence {

  private String id;
  private Period period;
  private CalendarEvent event;

  private static CalendarEventOccurrenceGenerator generator() {
    return CalendarEventOccurrenceGenerator.get();
  }

  /**
   * Gets the event occurrences that occur in the specified window of time of a given calendar.
   * @param timeWindow a window of time of a calendar.
   * @return a list of event occurrences or an empty list if there is no occurrences of events
   * in the specified window of time.
   */
  public static List<CalendarEventOccurrence> getOccurrencesIn(
      final CalendarTimeWindow timeWindow) {
    return generator().generateOccurrencesIn(timeWindow);
  }

  /**
   * Constructs a new occurrence from the specified calendar event, starting and ending at the
   * specified dates.
   * @param event the event from which the occurrence is instantiated.
   * @param startDateTime the start date and time of the occurrence.
   * @param endDateTime the end date and time of the occurrence.
   */
  CalendarEventOccurrence(final CalendarEvent event, final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    this.id = event.getId() + "@" + startDateTime;
    this.event = event;
    this.period = Period.between(startDateTime, endDateTime);
  }

  /**
   * Gets the event from which this occurrence was spawned.
   *
   * From the returned event, the title, the description or any other event properties can be
   * modified. Nevertheless, the change can be effective only by invoking the {@code update} method
   * of either the {@link CalendarEvent} to apply the modifications to all occurrences or this
   * occurrence to apply the modifications only to this occurrence. Only the period at which the
   * event occur in the calendar cannot be used to update this occurrence. For doing, please use
   * either the {@code setPeriod} or the {@code setDay} method of {@link CalendarEventOccurrence}.
   * @return the event from which this occurrence is instanciated.
   */
  public CalendarEvent getCalendarEvent() {
    return this.event;
  }

  /**
   * Gets the date and time at which this occurrence should starts
   * @return the start date of the event occurrence.
   */
  public OffsetDateTime getStartDateTime() {
    return period.getStartDateTime();
  }

  /**
   * Gets the date at which this event should ends
   * @return the end date of the event occurrence.
   */
  public OffsetDateTime getEndDateTime() {
    return period.getEndDateTime();
  }

  /**
   * Gets the unique identifier of this occurrence.
   * @return the unique identifier of this occurrence.
   */
  public String getId() {
    return String.valueOf(id);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarEventOccurrence)) {
      return false;
    }

    final CalendarEventOccurrence that = (CalendarEventOccurrence) o;
    return id.equals(that.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Changes the planning of this occurrence in the calendar. The change will be effectively
   * performed once the {@code update} method invoked.
   * @param newPeriod a new period of time on which this occurrence will occur or has actually
   * occurred.
   */
  public void setPeriod(final Period newPeriod) {
    this.period = newPeriod;
  }

  /**
   * Changes the planning of this occurrence in the calendar. The change will be effectively
   * performed once the {@code update} method invoked.
   * @param newDay the new day at which this occurrence will occur or has actually occurred.
   */
  public void setDay(final LocalDate newDay) {
    Period newPeriod = Period.between(newDay, newDay);
    setPeriod(newPeriod);
  }

  /**
   * Deletes this event occurrence.
   * <ul>
   * <li>If the occurrence is the single one of the event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of the event, then the date time at which this
   * occurrence starts is added as an exception in the recurrence rule of the event.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   */
  public void delete() {
    doEitherOr(() -> getCalendarEvent().delete(), this::excludeMe);
  }

  /**
   * Applies the change done to this occurrence. According to the state of the event, this will
   * either create a new non-recurrent event or update directly the event from which this occurrence
   * was spawned:
   * <ul>
   * <li>The event is recurrent: the occurrence start date time before the change is set as an
   * exception date in the event's recurrence and a new event is created with the modifications</li>
   * <li>It is the only occurrence of the event: the event is then directly modified.</li>
   * </ul>
   */
  public void update() {
    doEitherOr(() -> {
      CalendarEvent event = getCalendarEvent();
      event.setPeriod(Period.between(this.getStartDateTime(), this.getEndDateTime()));
      event.update();
    }, () -> {
      excludeMe();
      createNewEventFromMe();
    });
  }

  /**
   * Change the event from which this occurrence was spawned. This is for updates that come to
   * the creation of a new event.
   * @param event the event to set.
   */
  void setCalendarEvent(final CalendarEvent event) {
    this.event = event;
  }

  /**
   * Gets the date and time at which this occurrence originally starts before any changes.
   * @return the start date of the event occurrence before any recent change.
   */
  OffsetDateTime getLastStartDateTime() {
    return OffsetDateTime.parse(getId().split("@")[1]);
  }

  private void excludeMe() {
    CalendarEvent event = getCalendarEvent();
    event.getRecurrence().excludeEventOccurrencesStartingAt(getLastStartDateTime());
    event.update();
  }

  private void createNewEventFromMe() {
    CalendarEvent newEvent =
        getCalendarEvent().clone().createdBy(getCalendarEvent().getLastUpdatedBy());
    newEvent.unsetRecurrence();
    newEvent.setPeriod(Period.between(getStartDateTime(), getEndDateTime()));
    newEvent.planOn(getCalendarEvent().getCalendar());
    setCalendarEvent(newEvent);
  }

  private OffsetDateTime endDateTimeOf(final Recurrence recurrence,
      final OffsetDateTime fromRecurrenceStart) {
    return recurrence.getEndDate().orElseGet(() -> {
      RecurrencePeriod frequency = recurrence.getFrequency();
      long timeCount = frequency.getInterval() * recurrence.getRecurrenceCount();
      switch (frequency.getUnit()) {
        case DAY:
          return fromRecurrenceStart.plusDays(timeCount);
        case WEEK:
          return fromRecurrenceStart.plusWeeks(timeCount);
        case MONTH:
          return fromRecurrenceStart.plusMonths(timeCount);
        case YEAR:
          return fromRecurrenceStart.plusYears(timeCount);
        default:
          throw new SilverpeasRuntimeException("Unsupported unit: " + frequency.getUnit());
      }
    });
  }

  private void doEitherOr(Runnable ifSingleOccurrence, Runnable ifManyOccurrences) {
    Transaction.getTransaction().perform(() -> {
      CalendarEvent event = this.getCalendarEvent();
      if (event.isRecurrent() && event.getRecurrence().isEndless()) {
        ifManyOccurrences.run();
      } else if (event.isRecurrent()) {
        OffsetDateTime recurrenceStart = event.getStartDateTime();
        OffsetDateTime recurrenceEnd = endDateTimeOf(event.getRecurrence(), recurrenceStart);
        List<CalendarEventOccurrence> occurrences =
            generator().generateOccurrencesOf(Collections.singletonList(event),
                Period.between(recurrenceStart, recurrenceEnd));
        if (occurrences.size() == 1 && occurrences.get(0).equals(this)) {
          ifSingleOccurrence.run();
        } else {
          ifManyOccurrences.run();
        }
      } else {
        ifSingleOccurrence.run();
      }
      return null;
    });
  }
}
