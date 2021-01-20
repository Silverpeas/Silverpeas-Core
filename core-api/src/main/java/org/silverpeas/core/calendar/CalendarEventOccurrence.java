/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.persistence.datasource.model.identifier.ExternalStringIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.reminder.WithReminder;
import org.silverpeas.core.util.Mutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.persistence.datasource.model.jpa.JpaEntityReflection.setCreationData;
import static org.silverpeas.core.persistence.datasource.model.jpa.JpaEntityReflection.setUpdateData;

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
    @NamedQuery(name = "occurrenceByEventsAndByPeriod", query =
        "SELECT o FROM CalendarEventOccurrence o WHERE o.event in :events AND " +
            "((o.component.period.startDateTime <= :startDateTime AND " +
            "  o.component.period.endDateTime > :startDateTime) OR " +
            "(o.component.period.startDateTime >= :startDateTime AND " +
            "  o.component.period.startDateTime < :endDateTime))"),
    @NamedQuery(name = "occurrenceByEventSince", query =
        "SELECT o FROM CalendarEventOccurrence o WHERE o.event = :event AND " +
            "o.component.period.startDateTime >= :date"),
    @NamedQuery(name = "occurrenceByEvent", query = "SELECT o FROM CalendarEventOccurrence o WHERE o.event " +
        "= :event")})
public class CalendarEventOccurrence
    extends BasicJpaEntity<CalendarEventOccurrence, ExternalStringIdentifier>
    implements IdentifiableEntity, Occurrence, Contribution, WithReminder {

  public static final String TYPE = "CalendarEventOccurrence";

  public static final Comparator<CalendarEventOccurrence> COMPARATOR_BY_ORIGINAL_DATE_ASC =
      Comparator.comparing(o -> o.getOriginalStartDate().toString());
  public static final Comparator<CalendarEventOccurrence> COMPARATOR_BY_DATE_ASC =
      Comparator.comparing(o -> o.getStartDate().toString());
  public static final Comparator<CalendarEventOccurrence> COMPARATOR_BY_DATE_DESC =
      (o1, o2) -> o2.getStartDate().toString().compareTo(o1.getStartDate().toString());

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "eventId", referencedColumnName = "id")
  private CalendarEvent event;

  @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval =
      true)
  @JoinColumn(name = "componentId", referencedColumnName = "id", unique = true)
  private CalendarComponent component;
  @Transient
  private CalendarEventOccurrence previousState;

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
    this.component = event.asCalendarComponent().copy();
    setCreationData(this.component, event.getCreator(), event.getCreationDate());
    setUpdateData(this.component, event.getLastUpdater(), event.getLastUpdateDate());
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
    final CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
    final Mutable<CalendarEventOccurrence> occurrence = Mutable.ofNullable(repository.getById(id));
    if (!occurrence.isPresent()) {
      final Pair<String, Temporal> explodedId = explodeId(id);
      final String eventId = explodedId.getLeft();
      final Temporal startDate = explodedId.getRight();
      final Optional<CalendarEvent> event = Optional.ofNullable(CalendarEvent.getById(eventId));
      event.ifPresent(e -> {
        if (e.isRecurrent()) {
          final LocalDate occStartDate;
          final LocalDate occEndDate;
          if (startDate instanceof LocalDate) {
            final LocalDate date = (LocalDate) startDate;
            occStartDate = date.minusDays(1);
            occEndDate = date.plusDays(1);
          } else {
            final OffsetDateTime dateTime = (OffsetDateTime) startDate;
            occStartDate = dateTime.minusDays(1).toLocalDate();
            occEndDate = dateTime.plusDays(1).toLocalDate();
          }
          final List<CalendarEventOccurrence> occurrences =
              e.getCalendar().between(occStartDate, occEndDate).getEventOccurrences();
          occurrences.removeIf(o -> !o.getCalendarEvent().getId().equals(eventId) ||
              (!o.getStartDate().equals(startDate)));
          if (occurrences.size() == 1) {
            occurrence.set(occurrences.get(0));
          }
        } else {
          occurrence.set(new CalendarEventOccurrence(e, e.getStartDate(), e.getEndDate()));
        }
      });
    }
    return Optional.ofNullable(occurrence.orElse(null));
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
   * Gets the next occurrence of the given event since the specified datetime. If the datetime is
   * before the event start date, then the first occurrence of the event is returned. If the event
   * has no occurrence since the specified datetime, then nothing is returned.
   * @param event the event the event for which the next occurrence is asked.
   * @param dateTime the datetime after which the next occurrence should occur.
   * @return optionally the next event's occurrence occurring after the specified datetime or
   * nothing if there is no more occurrences after that datetime.
   */
  static Optional<CalendarEventOccurrence> getNextOccurrence(final CalendarEvent event,
      final ZonedDateTime dateTime) {
    final CalendarEventOccurrence occurrence =
        generator().generateNextOccurrenceOf(event, dateTime);
    if (occurrence != null) {
      CalendarEventOccurrence modifiedOccurrence =
          CalendarEventOccurrenceRepository.get().getById(occurrence.getId());
      return Optional.of(modifiedOccurrence == null ? occurrence : modifiedOccurrence);
    }
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<CalendarEvent> getParent() {
    return Optional.ofNullable(getCalendarEvent());
  }

  @Override
  public ContributionIdentifier getContributionId() {
    return ContributionIdentifier
        .from(getCalendarEvent().getCalendar().getComponentInstanceId(), getId(),
            getContributionType());
  }

  @Override
  public User getCreator() {
    return component.getCreator();
  }

  @Override
  public Date getCreationDate() {
    return component.getCreationDate();
  }

  @Override
  public User getLastModifier() {
    return component.getLastUpdater();
  }

  @Override
  public Date getLastModificationDate() {
    return component.getLastUpdateDate();
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return getCalendarEvent().canBeAccessedBy(user);
  }

  @Override
  public String getContributionType() {
    return TYPE;
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
  @Override
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
  @Override
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
    return getPeriod().isInDays();
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
   * Gets the last date at which this occurrence has been updated.
   * @return the last update date.
   */
  public Date getLastUpdateDate() {
    return this.component.getLastUpdateDate();
  }

  /**
   * Gets the content of this event occurrence. The content is the one of the event and it cannot
   * be modified per occurrence but for the whole event occurrence(s).
   * @return optionally the content of the event.
   */
  public Optional<WysiwygContent> getContent() {
    return getCalendarEvent().getContent();
  }

  /**
   * Is this occurrence actually occurs before the specified occurrence?
   * @param occurrence another occurrence with which the start date is compared.
   * @return true if this occurrence starts before the specified another one. False otherwise.
   */
  public boolean isBefore(final CalendarEventOccurrence occurrence) {
    return getStartDate().toString().compareTo(occurrence.getStartDate().toString()) < 0;
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
   * Updates this occurrence with the state of the specified occurrence.
   * @param occurrence an event occurrence from which this occurrence will be updated.
   * @return the result of the update.
   */
  public EventOperationResult updateFrom(final CalendarEventOccurrence occurrence) {
    occurrence.asCalendarComponent().copyTo(this.component);
    return update();
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

  CalendarEventOccurrence copyWithEvent(final CalendarEvent event) {
    CalendarEventOccurrence newOccurrence = new CalendarEventOccurrence();
    newOccurrence.setId(generateId(event, getOriginalStartDate()));
    newOccurrence.event = event;
    newOccurrence.component = this.component.copy();
    newOccurrence.component.setCalendar(event.getCalendar());
    return newOccurrence;
  }

  /**
   * Is the properties of this event occurrence was modified since its last specified state?
   * The attendees in this event occurrence aren't taken into account as they aren't considered as
   * a property of an event occurrence.
   * @param previous a previous state of this event occurrence.
   * @return true if the state of this event occurrence is different with the specified one.
   */
  public boolean isModifiedSince(final CalendarEventOccurrence previous) {
    if (!this.getId().equals(previous.getId())) {
      throw new IllegalArgumentException(
          "The event occurrence of id " + previous.getId() + " isn't the expected one " +
              this.getId());
    }
    if (this.getVisibilityLevel() != previous.getVisibilityLevel() ||
        !this.getCategories().equals(previous.getCategories())) {
      return true;
    }

    return this.asCalendarComponent().isModifiedSince(previous.asCalendarComponent());
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
      Recurrence recurrence = this.getCalendarEvent().getRecurrence().copy();
      recurrence.clearsAllExceptionDates();
      if (!this.getCalendarEvent().getRecurrence().isEndless()) {
        recurrence.until(this.getCalendarEvent().getRecurrence().getEndDate()
            .orElseThrow(IllegalArgumentException::new));
      }
      newEvent.recur(recurrence);
    }
    return newEvent;
  }

  /**
   * Sets the event from which this occurrence comes from. This method is used for internal
   * mechanisms of the Silverpeas Calendar Engine when working with copies of calendar event
   * occurrences.
   * @param event the event of this occurrence.
   */
  final void setCalendarEvent(final CalendarEvent event) {
    setId(generateId(event, getOriginalStartDate()));
    this.event = event;
    this.component.setCalendar(event.getCalendar());
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
      final CalendarEventOccurrence previous = getPreviousState();
      final CalendarComponent pcc;
      final boolean modifiedSince;
      if (previous != null) {
        pcc = previous.asCalendarComponent();
        modifiedSince = isModifiedSince(previous);
      } else {
        pcc = event.asCalendarComponent();
        // Making a clone of event component and setting to it the component period in order to
        // not detect a modification about the period when performing method #isModifiedSince().
        // The clone permits to not imply a modification of event component into persistence. As
        // the below treatment is performed into the case of creation, the original start date is
        // compared to the current start date in order to try to detect a real period modification.
        final CalendarComponent pccClone = pcc.copy();
        pccClone.setPeriod(component.getPeriod());
        modifiedSince = component.isModifiedSince(pccClone) || !getOriginalStartDate().toString().equals(getStartDate().toString());
      }
      if (!modifiedSince && getAttendees().onlyAttendeePropertyChange(pcc.getAttendees())) {
        // we don't want update properties to be modified on participation answer or presence
        // status change in the attendees
        this.component.createdBy(pcc.getCreator(), pcc.getCreationDate()).updatedBy(pcc.getLastUpdater(), pcc.getLastUpdateDate());
      } else {
        this.component.incrementSequence();
      }
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
      List<CalendarEventOccurrence> occurrences = repository.getAllSince(this);
      repository.delete(occurrences);
      return occurrences.size();
    });
  }

  /**
   * Is the date on which this occurrence is planned has changed from its previous state.
   * @return true if the date of this occurrence has just modified. False otherwise.
   */
  boolean isDateChanged() {
    CalendarEventOccurrence previous = getPreviousState();
    return (previous != null && !previous.getPeriod().equals(this.getPeriod())) ||
        (previous == null && !this.getOriginalStartDate().equals(this.getStartDate()));
  }

  private CalendarEventOccurrence getPreviousState() {
    if (previousState == null && this.getId() != null) {
      previousState = Transaction.performInNew(() -> {
        CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
        return repository.getById(this.getId());
      });
    }
    return previousState;
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
