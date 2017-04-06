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

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.persistence.datasource.model.identifier.ExternalStringIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.util.Mutable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;


/**
 * The occurrence of an event in a Silverpeas calendar. It is an instance of an event in the
 * timeline of a calendar; it represents an event starting and ending at a given date or datetime
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
 * {@link #delete()},
 * {@link #deleteSinceMe()}.
 * If an occurrence of a non-recurrent event is modified, then the modification is directly
 * applied to the event itself (as it is a singleton). If an occurrence of a recurrent event is
 * modified, then the modification is applied to the occurrence only and this occurrence is
 * persisted as a modification related to the recurrence rule of the concerned event.
 */
@Entity
@Table(name = "sb_cal_occurrences")
@NamedQueries({
    @NamedQuery(name = "byEventsAndByPeriod", query =
        "SELECT o FROM CalendarEventOccurrence o WHERE o.event in :events AND " +
            "((o.component.period.startDateTime <= :startDateTime AND " +
            "  o.component.period.endDateTime > :startDateTime) OR " +
            "(o.component.period.startDateTime >= :startDateTime AND " +
            "  o.component.period.startDateTime < :endDateTime))"),
    @NamedQuery(name = "byEventSince", query =
        "SELECT o FROM CalendarEventOccurrence o WHERE o.event = :event AND " +
            "o.component.period.startDateTime >= :date"),
    @NamedQuery(name = "byEvent", query = "SELECT o FROM CalendarEventOccurrence o WHERE o.event " +
        "= :event")})
public class CalendarEventOccurrence
    extends BasicJpaEntity<CalendarEventOccurrence, ExternalStringIdentifier>
    implements IdentifiableEntity, Occurrence {

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id")
  private CalendarEvent event;

  @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval =
      true)
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
    setId(generateId(event, startDate));
    this.event = event;
    this.component = event.asCalendarComponent().clone();
    this.component.setPeriod(Period.between(startDate, endDate));
  }

  /**
   * Gets optionally an event occurrence from the specified data.
   * @param event an event.
   * @param occurrenceStartDate a start date.
   * @return the computed occurrence identifier.
   */
  public static Optional<CalendarEventOccurrence> getBy(CalendarEvent event, String occurrenceStartDate) {
    return getBy(event, getDate(occurrenceStartDate));
  }

  /**
   * Gets optionally an event occurrence from the specified data.
   * @param event an event.
   * @param occurrenceStartDate a start date.
   * @return the computed occurrence identifier.
   */
  public static Optional<CalendarEventOccurrence> getBy(CalendarEvent event, Temporal occurrenceStartDate) {
    Temporal startDate = occurrenceStartDate;
    if (startDate instanceof OffsetDateTime) {
      startDate = ((OffsetDateTime) occurrenceStartDate).atZoneSameInstant(ZoneOffset.UTC)
          .toOffsetDateTime();
    }
    return getById(generateId(event, startDate));
  }

  /**
   * Gets optionally an event occurrence by its identifier.
   * <p>If the occurrence exists into the persistence, it is returned. Otherwise it is generated.
   * <p>Otherwise and if start date is valid, the occurrence is generated.
   * @param id the identifier of the aimed occurrence.
   * @return an optional calendar event occurrence.
   */
  public static Optional<CalendarEventOccurrence> getById(final String id) {
    CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
    Mutable<CalendarEventOccurrence> occurrence = Mutable.of(repository.getById(id));
    if (!occurrence.isPresent()) {
      Pair<String, Temporal> explodedId = explodeId(id);
      final String eventId = explodedId.getLeft();
      final Temporal startDate = explodedId.getRight();
      CalendarEvent event = CalendarEvent.getById(eventId);
      if (event != null) {
        final LocalDate occStartDate;
        final LocalDate occEndDate;
        if (startDate instanceof LocalDate) {
          LocalDate date = (LocalDate) startDate;
          occStartDate = date.minusDays(1);
          occEndDate = date.plusDays(1);
        } else {
          OffsetDateTime dateTime = (OffsetDateTime) startDate;
          occStartDate = dateTime.minusDays(1).toLocalDate();
          occEndDate = dateTime.plusDays(1).toLocalDate();
        }
        List<CalendarEventOccurrence> occurrences =
            event.getCalendar().between(occStartDate, occEndDate).getEventOccurrences();
        occurrences.removeIf(o -> !o.getCalendarEvent().getId().equals(eventId) ||
            (!o.getStartDate().equals(startDate)));
        if (occurrences.size() == 1) {
          occurrence.set(occurrences.get(0));
        }
      }
    }
    return Optional.ofNullable(occurrence.orElse(null));
  }

  static CalendarEventOccurrence getByIdFromPersistence(final String id) {
    return Transaction.performInNew(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      return repository.getById(id);
    });
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
  static List<CalendarEventOccurrence> getOccurrencesIn(
      final CalendarTimeWindow timeWindow) {
    List<CalendarEventOccurrence> occurrences = generator().generateOccurrencesIn(timeWindow);
    List<CalendarEventOccurrence> modified = CalendarEventOccurrenceRepository.get()
        .getAll(timeWindow.getEvents(), timeWindow.getPeriod());
    modified.forEach(o -> {
      int idx = occurrences.indexOf(o);
      if (idx < 0) {
        occurrences.add(o);
      } else {
        occurrences.set(idx, o);
      }
    });
    return occurrences;
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

  @Override
  public Temporal getStartDate() {
    return this.component.getPeriod().getStartDate();
  }

  @Override
  public Temporal getEndDate() {
    return this.component.getPeriod().getEndDate();
  }

  /**
   * Gets the original start date of this occurrence. If the start date wasn't modified, then the
   * returning date should be the same than the start date returned by the method
   * {@link CalendarEventOccurrence#getStartDate()}.
   * @return the original start date of this occurrence of calendar event.
   */
  public Temporal getOriginalStartDate() {
    return explodeId(this.getId()).getRight();
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

  public AttendeeSet getAttendees() {
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
  public AttributeSet getAttributes() {
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
  public CategorySet getCategories() {
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
    return this.component.isPersisted() ? this.component.getSequence() : this.event.getSequence();
  }

  /**
   * Gets the {@link CalendarComponent} representation of this occurrence. Any change to the
   * returned calendar component will change also the related occurrence.
   * @return a {@link CalendarComponent} instance representing this event occurrence (without the
   * specific properties related to an event occurrence).
   */
  public CalendarComponent asCalendarComponent() {
    return this.component;
  }

  /**
   * Updates this occurrence and all of the forthcoming occurrences of the same event with the
   * changes in this occurrence.
   *
   * If the event is non recurrent, then the event is itself updated. Otherwise a new event is
   * created for this occurrence and all of the forthcoming occurrences and with the modifications
   * carried by this occurrences. The recurrence of the original event is updated to end up at this
   * occurrence minus one day (the recurrence end date is inclusive).
   *
   * In the case the temporal period of the occurrences is modified, the participation status of
   * all the attendees in the occurrences is cleared.
   *
   * This is equivalent to
   * <pre>{@code EventOperationResult result = event.updateSince(this)}</pre>
   *
   * @see CalendarEvent#updateSince(CalendarEventOccurrence)
   *
   * @return the result of the update.
   */
  public EventOperationResult updateSinceMe() {
    return getCalendarEvent().updateSince(this);
  }

  /**
   * Updates only this occurrence among the occurrences of the event it comes from.
   *
   * If the event is non recurrent, then the event is itself updated. Otherwise the changes in
   * this occurrence are persisted and its sequence number is incremented by one, diverging then
   * from the sequence number of the event it comes from.
   * <p>
   * In the case the temporal period of the occurrence is modified, the participation status
   * of all the attendees in this occurrence is cleared.
   *
   *  This is equivalent to
   * <pre>{@code EventOperationResult result = event.updateOnly(this)}</pre>
   *
   * @see CalendarEvent#updateOnly(CalendarEventOccurrence)
   *
   * @return the result of the update.
   */
  public EventOperationResult update() {
    return getCalendarEvent().updateOnly(this);
  }

  /**
   * Deletes this occurrence and all of the forthcoming occurrences of the same event.
   *
   * If the event is non recurrent, then the event is itself deleted. Otherwise the
   * original starting date of this occurrence and of all of the forthcoming occurrences are added
   * in the exception dates of the event's recurrence rule. If some of the occurrences were
   * persisted, then they are all removed from the persistence context.
   *
   *  This is equivalent to
   * <pre>{@code EventOperationResult result = event.deleteSince(this)}</pre>
   *
   * @see CalendarEvent#deleteSince(CalendarEventOccurrence)
   *
   * @return the result of the deletion.
   */
  public EventOperationResult deleteSinceMe() {
    return getCalendarEvent().deleteSince(this);
  }

  /**
   * Deletes only this occurrence among the occurrences of the event it comes from.
   *
   * If the event is non recurrent, then the event is itself deleted. Otherwise the
   * original starting date of this occurrence is added in the exception dates of the event's
   * recurrence rule. If the occurrence was previously persisted, then it is removed from the
   * persistence context.
   *
   * This is equivalent to
   * <pre>{@code EventOperationResult result = event.deleteOnly(this)}</pre>
   *
   * @see CalendarEvent#deleteOnly(CalendarEventOccurrence)
   *
   * @return the result of the deletion.
   */
  public EventOperationResult delete() {
    return getCalendarEvent().deleteOnly(this);
  }

  /**
   * Converts this occurrence of a calendar event into an unplanned non-recurrent
   * {@link CalendarEvent} instance. This method is dedicated to the {@link CalendarEvent} class.
   * @return a new {@link CalendarEvent} instance from this occurrence.
   * @see CalendarEvent#from(CalendarEventOccurrence)
   */
  CalendarEvent toCalendarEvent() {
    return CalendarEvent.from(this);
  }

  /**
   * Converts this occurrence of a calendar event into an unplanned recurrent {@link CalendarEvent}
   * instance. This method is dedicated to the {@link CalendarEvent} class.
   * <p>
   * If the occurrence comes from a non-recurrent event, then the returned event won't be
   * neither recurrent. Otherwise the recurrence of the new event will start at the start date of
   * this occurrence and will end up at the actual end date of the recurrence of the event of this
   * occurrence.
   * @return a new possibly recurrent {@link CalendarEvent} instance from this occurrence.
   * @see CalendarEvent#from(CalendarEventOccurrence)
   */
  CalendarEvent toRecurrentCalendarEvent() {
    CalendarEvent newEvent = toCalendarEvent();
    if (this.getCalendarEvent().isRecurrent()) {
      Recurrence recurrence = this.getCalendarEvent().getRecurrence().clone();
      recurrence.clearsAllExceptionDates();
      if (!this.getCalendarEvent().getRecurrence().isEndless()) {
        recurrence.until(this.getCalendarEvent().getRecurrence().getEndDate().get());
      }
      newEvent.recur(recurrence);
    }
    return newEvent;
  }

  /**
   * Saves this occurrence of a calendar event into a data source so that it can be get later.
   * This method is dedicated to the {@link CalendarEvent} class.
   * <p>
   * Saving an event occurrence is done when this occurrence has changed from the event or from
   * its original planning in the timeline of a calendar. This is only done with occurrences of
   * a recurrent event as any change to the single occurrence of a non-recurrent event modifies the
   * event itself.
   * @return the persisted event occurrence.
   */
  CalendarEventOccurrence saveIntoPersistence() {
    return Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      this.component.incrementSequence();
      return repository.save(this);
    });
  }

  /**
   * Deletes this occurrence of a calendar event in the data source. If this occurrences wasn't
   * persisted, then nothing is done.
   * This method is dedicated to the {@link CalendarEvent} class.
   */
  void deleteFromPersistence() {
    Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      repository.delete(this);
      return null;
    });
  }

  /**
   * Deletes this occurrence and all the occurrences belonging to the same event of this occurrence
   * and that are after this one. This method is dedicated to the {@link CalendarEvent} class.
   * @return the count of actually occurrences removed from the data source.
   */
  long deleteAllSinceMeFromThePersistence() {
    return Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      return repository.deleteSince(this);
    });
  }

  /**
   * Is the date on which this occurrence is planned has changed from its previous state.
   * @return true if the date of this occurrence has just modified. False otherwise.
   */
  boolean isDateChanged() {
    CalendarEventOccurrence previous = CalendarEventOccurrence.getByIdFromPersistence(this.getId());
    return (previous != null && !previous.getPeriod().equals(this.getPeriod())) ||
        (previous == null && !this.getOriginalStartDate().equals(this.getStartDate()));
  }

  /**
   * Generates an event occurrence identifier from the necessary data.
   * @param event an event.
   * @param occurrenceStartDate a start date.
   * @return the computed occurrence identifier.
   */
  private static String generateId(CalendarEvent event, Temporal occurrenceStartDate) {
    return event.getId() + "@" + occurrenceStartDate;
  }

  private static Pair<String, Temporal> explodeId(String id) {
    final String[] explodedId = id.split("@");
    return Pair.of(explodedId[0], getDate(explodedId[1]));
  }

  private static Temporal getDate(String temporal) {
    if (temporal.contains("T")) {
      return OffsetDateTime.parse(temporal);
    } else {
      return LocalDate.parse(temporal);
    }
  }
}
