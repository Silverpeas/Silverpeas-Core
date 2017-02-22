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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import org.silverpeas.core.date.Period;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;


/**
 * The occurrence of an event in a Silverpeas calendar. It is an instance of an event in the
 * timeline of a calendar; it represents an event starting and ending at a given date or date time
 * in the calendar.
 *
 * A non-recurrent event is a singleton, meaning that is has only one single instance occurring in
 * the calendar (so the name occurrence). A recurrent event has one or more occurrences in the
 * timeline. It occurs several time in the calendar in a regular way according to its recurrence
 * rule; at each time such an event occurs is represented by an occurrence.
 *
 * By default, the occurrences of an event aren't persisted but they are generated from the period
 * of time at which occurs the event and, if any, from its recurrence rule. If an occurrence of a
 * non-recurrent event is deleted, then the related event is deleted. If an occurrence of a
 * recurrent event is deleted, then an exception is added into the recurrence rule of the event.
 * This operation is done with one of the following methods:
 * {@link CalendarEvent#delete(CalendarEventOccurrenceReference)},
 * {@link CalendarEvent#deleteFrom(CalendarEventOccurrenceReference)}.
 * If an occurrence of a non-recurrent event is modified, then the modification is directly
 * applied to the event itself (as it is a singleton). If an occurrence of a recurrent event is
 * modified, then the modification is applied to the occurrence only and this occurrence is
 * persisted as a modification related to the recurrence rule of the concerned event.
 */
@Entity
@Table(name = "sb_cal_occurrences")
public class CalendarEventOccurrence implements Serializable {

  @Id
  private String id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id")
  private CalendarEvent event;

  @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "componentId", referencedColumnName = "id", unique = true)
  private CalendarComponent component;

  /**
   * Constructor for only persistence context.
   */
  protected CalendarEventOccurrence() {
    // this constructor is dedicated to be used by the persistence context.
  }

  /**
   * Constructs a new occurrence from the specified calendar event, starting and ending at the
   * specified dates.
   * @param event the event from which the occurrence is instantiated.
   * @param startDate the start date (and time if offset) of the occurrence.
   * @param endDate the end date (and time if offset) of the occurrence.
   */
  CalendarEventOccurrence(final CalendarEvent event, final Temporal startDate,
      final Temporal endDate) {
    this.id = event.getId() + "@" + startDate;
    this.event = event;
    this.component = event.asCalendarComponent().clone();
    this.component.setPeriod(Period.between(startDate, endDate));
  }

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
   * Gets the date (and time if not on all day) from an occurrence identifier.
   * @param occurrenceId an occurrence identifier.
   * @return the start date (and time if not on all day) of the event occurrence before any
   * recent change.
   */
  static Temporal getLastStartDateFrom(String occurrenceId) {
    String lastOccurrenceDate = occurrenceId.split("@")[1];
    try {
      return LocalDate.parse(lastOccurrenceDate);
    } catch (Exception e) {
      return OffsetDateTime.parse(lastOccurrenceDate);
    }
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
   * Gets the date or date time at which this occurrence should starts.
   * @return the start date or date time of the event occurrence.
   */
  public Temporal getStartDate() {
    return this.component.getPeriod().getStartDate();
  }

  /**
   * Gets the date or date time at which this event should ends.
   * @return the end date or date time of the event occurrence.
   */
  public Temporal getEndDate() {
    return this.component.getPeriod().getEndDate();
  }

  /**
   * Gets the unique identifier of this occurrence.
   * @return the unique identifier of this occurrence.
   */
  public String getId() {
    return String.valueOf(id);
  }

  /**
   * Gets the date or the date and time at which this occurrence originally starts before any
   * changes.
   * @return the start date of the event occurrence before any recent change.
   */
  public Temporal getLastStartDate() {
    return getLastStartDateFrom(getId());
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
    this.component.setPeriod(newPeriod);
  }

  /**
   * Gets the period of this occurrence in the calendar.
   * @return a period.
   */
  public Period getPeriod() {
    return this.component.getPeriod();
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
   * Gets the title of this event occurrence. The title is either the one of the related event or
   * the one that was modified for this occurrence.
   * @return the title of the event occurrence.
   */
  public String getTitle() {
    return this.component.getTitle();
  }

  public void setTitle(final String title) {
    this.component.setTitle(title);
  }

  public Set<Attendee> getAttendees() {
    return this.component.getAttendees();
  }

  /**
   * Gets the description of this event occurrence. The description is either the one of the
   * related event or the one that was modified for this occurrence.
   * @return the description of the event occurrence.
   */
  public String getDescription() {
    return this.component.getDescription();
  }

  /**
   * Sets a new description for this event occurrence.
   * @param description a new description related to this event occurrence.
   */
  public void setDescription(String description) {
    this.component.setDescription(description);
  }

  /**
   * Gets the location of this event occurrence. The location is either the one of the related
   * event or the one that was modified for this occurrence.
   * @return the location of the event occurrence.
   */
  public String getLocation() {
    return this.component.getLocation();
  }

  /**
   * Sets a new location for this event occurrence.
   * @param location the new location where this occurrence should take place.
   */
  public void setLocation(String location) {
    this.component.setLocation(location);
  }

  /**
   * Gets the attributes of this event occurrence. The attributes are either those related
   * to the event or those that were modified for this occurrence.
   * @return the extra attributes of the event occurrence.
   */
  public Attributes getAttributes() {
    return this.component.getAttributes();
  }

  /**
   * Gets the priority of this event occurrence. The priority is the one that is set for the event.
   * @return the priority of the event occurrence.
   */
  public Priority getPriority() {
    return this.component.getPriority();
  }

  /**
   * Sets a new priority to this event occurrence.
   * @param priority the new priority of this event occurrence.
   */
  public void setPriority(final Priority priority) {
    this.component.setPriority(priority);
  }

  /**
   * Gets the categories of this event occurrence. The categories are those that are set for the
   * event.
   * @return the categories of the event occurrence.
   */
  public Categories getCategories() {
    return this.event.getCategories();
  }

  /**
   * Gets the level of visibility of this event occurrence. The visibility level is the one that is
   * set for the event.
   * @return the level of visibility of the event occurrence.
   */
  public VisibilityLevel getVisibilityLevel() {
    return this.event.getVisibilityLevel();
  }

  /**
   * Is this event occurrence occurring on all the day(s)?
   * @return true if this event occurrence is occurring on all its day(s).
   */
  public boolean isOnAllDay() {
    return getCalendarEvent().isOnAllDay();
  }

  /**
   * Gets the revision sequence number of this event occurrence within a sequence of revisions.
   * Any changes to some properties of this event occurrence increment this sequence number. This
   * number is mainly dedicated with the synchronization or syndication mechanism of event instances
   * with external calendars. Its meaning comes from the icalendar specification.
   * @return the sequence number of this event occurrence.
   * @see CalendarComponent#getSequence()
   */
  public long getSequence() {
    return (this.component.isPersisted() ? this.component.getSequence() : this.event.getSequence());
  }
}
