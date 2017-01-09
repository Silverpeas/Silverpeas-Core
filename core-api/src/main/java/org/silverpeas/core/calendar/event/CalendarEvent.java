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
package org.silverpeas.core.calendar.event;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.*;
import org.silverpeas.core.calendar.event.notification.CalendarEventLifeCycleEventNotifier;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.silverpeas.core.calendar.VisibilityLevel.PUBLIC;

/**
 * The event in a calendar. An event in a calendar is a {@link Recurrent} and a {@link Plannable}
 * general business object that can be planned on one and only one given existing {@link Calendar};
 * we ensures an event is unique in a per-calendar basis.
 * It occurs on a {@link Period} and as a such it must be well limited in the time (id est it must
 * have a start and an end dates/date times).
 * It can also be {@link Prioritized}, {@link Categorized}, and it can have some {@link Attendee}.
 * In order to be customized for different kinds of use, some additional information can be set
 * through its {@link Attributes} property.
 */
@Entity
@Table(name = "sb_cal_event")
@NamedQueries({
    @NamedQuery(name = "calendarEventCount", query =
        "select count(e) from CalendarEvent e where e.calendar = :calendar"),
    @NamedQuery(name = "calendarEvents", query = "select e from CalendarEvent e"),
    @NamedQuery(name = "calendarEventByCalendarAndExternalId", query =
        "select e from CalendarEvent e " +
        "where e.calendar = :calendar and e.externalId = :externalId"),
    @NamedQuery(name = "calendarEventsByCalendar", query =
        "select e from CalendarEvent e where e.calendar in :calendars"),
    @NamedQuery(name = "calendarEventsByParticipants", query =
        "select distinct e from CalendarEvent e LEFT OUTER JOIN e.attendees a " +
        "where (e.createdBy in :participantIds or a.attendeeId in :participantIds) " +
        "order by e.createdBy, e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipants", query =
        "select distinct e from CalendarEvent e LEFT OUTER JOIN e.attendees a " +
        "where e.calendar in :calendars " +
        "and (e.createdBy in :participantIds or a.attendeeId in :participantIds) " +
        "order by e.createdBy, e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsByCalendarByPeriod", query =
        "select e from CalendarEvent e LEFT OUTER JOIN FETCH e.recurrence r " +
            "where e.calendar in :calendars and (" +
            "(e.period.startDateTime <= :startDateTime and e.period.endDateTime >= " +
            ":startDateTime) " +
            "or (e.period.startDateTime >= :startDateTime and e.period.startDateTime <= " +
            ":endDateTime)" +
            " or (e.period.endDateTime < :startDateTime and e.recurrence is not null and " +
            "(e.recurrence.endDateTime >= :startDateTime or e.recurrence.endDateTime is null))" +
            ") order by e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsByPeriod", query =
        "select e from CalendarEvent e LEFT OUTER JOIN FETCH e.recurrence r " +
            "where (e.period.startDateTime <= :startDateTime and e.period.endDateTime >= " +
            ":startDateTime) " +
            "or (e.period.startDateTime >= :startDateTime and e.period.startDateTime <= " +
            ":endDateTime)" +
            " or (e.period.endDateTime < :startDateTime and e.recurrence is not null and " +
            "(e.recurrence.endDateTime >= :startDateTime or e.recurrence.endDateTime is null))" +
            "order by e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsByParticipantsByPeriod", query = "select distinct e from " +
        "CalendarEvent e LEFT OUTER JOIN e.attendees a LEFT OUTER JOIN FETCH e.recurrence r" +
        " where (e" +
        ".createdBy in :participantIds or a.attendeeId in :participantIds) and ((e.period" +
        ".startDateTime <= :startDateTime and e.period.endDateTime >= :startDateTime) or (e" +
        ".period.startDateTime >= :startDateTime and e.period.startDateTime <= :endDateTime) or " +
        "(e.period.endDateTime < :startDateTime and e.recurrence is not null and (e.recurrence" +
        ".endDateTime >= :startDateTime or e.recurrence.endDateTime is null))) order by e" +
        ".createdBy, e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipantsByPeriod", query =
        "select distinct e from " +
        "CalendarEvent e LEFT OUTER JOIN e.attendees a LEFT OUTER JOIN FETCH e.recurrence r" +
        " where e.calendar in :calendars and (e" +
        ".createdBy in :participantIds or a.attendeeId in :participantIds) and ((e.period" +
        ".startDateTime <= :startDateTime and e.period.endDateTime >= :startDateTime) or (e" +
        ".period.startDateTime >= :startDateTime and e.period.startDateTime <= :endDateTime) or " +
        "(e.period.endDateTime < :startDateTime and e.recurrence is not null and (e.recurrence" +
        ".endDateTime >= :startDateTime or e.recurrence.endDateTime is null))) order by e" +
        ".createdBy, e.period.startDateTime")})
public class CalendarEvent extends SilverpeasJpaEntity<CalendarEvent, UuidIdentifier>
    implements Plannable, Recurrent, Categorized, Prioritized, Securable {

  private static final long serialVersionUID = 1L;

  @Column(name = "externalId")
  private String externalId;

  @Embedded
  private Period period;

  @Embedded
  private Attributes attributes = new Attributes();

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false)
  private Calendar calendar;

  @Column(name = "title", nullable = false)
  @Size(min = 1)
  @NotNull
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "location")
  private String location;

  @Column(name = "visibility")
  @Enumerated(EnumType.STRING)
  @NotNull
  private VisibilityLevel visibilityLevel = VisibilityLevel.PUBLIC;

  @Column(name = "priority", nullable = false)
  @NotNull
  private Priority priority = Priority.NORMAL;

  @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
  @JoinColumn(name = "recurrenceId", referencedColumnName = "id", unique = true)
  private Recurrence recurrence = Recurrence.NO_RECURRENCE;

  @Embedded
  private Categories categories = new Categories();

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<Attendee> attendees = new HashSet<>();

  protected CalendarEvent(Period period) {
    this.period = period;
  }

  protected CalendarEvent() {
  }

  /**
   * Gets a calendar event by its identifier.
   * @param id the identifier of the aimed calendar event.
   * @return the instance of the aimed calendar event or null if it does not exist.
   */
  public static CalendarEvent getById(final String id) {
    CalendarEventRepository calendarEventRepository = CalendarEventRepository.get();
    return calendarEventRepository.getById(id);
  }

  /**
   * Gets a calendar event by its external identifier and the calendar it belongs.
   * @param calendar the calendar repository.
   * @param externalId the external identifier of the aimed calendar event.
   * @return the instance of the aimed calendar event or null if it does not exist.
   */
  public static CalendarEvent getByExternalId(final Calendar calendar, final String externalId) {
    CalendarEventRepository calendarEventRepository = CalendarEventRepository.get();
    return calendarEventRepository.getByExternalId(calendar, externalId);
  }

  /**
   * Creates a new calendar event that is spanning on the specified period of time.
   * @param period the period on which the event occurs.
   * @return a calendar event occurring on the specified period.
   */
  public static CalendarEvent on(Period period) {
    return new CalendarEvent(period);
  }

  /**
   * Creates a new calendar event that is on all the specified day.
   * @param day the day on which the event will occur.
   * @return a calendar event spanning on all the specified day.
   */
  public static CalendarEvent on(final LocalDate day) {
    return new CalendarEvent(Period.between(day, day));
  }

  private static CalendarEventOccurrenceGenerator generator() {
    return CalendarEventOccurrenceGenerator.get();
  }

  /**
   * Gets the calendar to which this event is related. A calendar event can only be persisted into
   * a given existing calendar.
   * @return either the calendar to which this event belongs or null if this event isn't yet
   * saved into a given calendar.
   */
  public Calendar getCalendar() {
    return calendar;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    if (title == null) {
      this.title = "";
    } else {
      this.title = title;
    }
  }

  /**
   * Gets the external identifier.<br/>
   * This identifier is set when an event is coming outside from Silverpeas.<br/>
   * This data is typically processed during export/import treatments.
   * @return the external identifier as string.
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * Specifies an external identifier.
   * @param externalId an external identifier as string.
   */
  public void withExternalId(final String externalId) {
    if (externalId != null && externalId.equals(getId())) {
      throw new IllegalArgumentException("externalId must be different from the id");
    }
    this.externalId = StringUtil.isDefined(externalId) ? externalId : null;
  }

  /**
   * Indicates if the event is coming from another platform than Silverpeas.
   * @return true if the event is coming from another platform, false otherwise.
   */
  public boolean isExternal() {
    return this.externalId != null;
  }

  /**
   * Specifies a title to this event.
   * @param title the title of the event
   * @return itself.
   */
  public CalendarEvent withTitle(String title) {
    setTitle(title);
    return this;
  }

  /**
   * Specifies a location where this event will occur.
   * @param location a location: an address, a designation, a GPS coordinates, ...
   * @return itself.
   */
  public CalendarEvent inLocation(String location) {
    setLocation(location);
    return this;
  }

  /**
   * Specifies the visibility level to this event. In generally, it defines the intention of the
   * user about the visibility on the event he accepts to give. Usual values are PUBLIC, PRIVATE or
   * CONFIDENTIAL for example. By default, the visibility level is PUBLIC.
   * @param accessLevel the new visibility level to this event.
   * @return itself.
   */
  public CalendarEvent withVisibilityLevel(VisibilityLevel accessLevel) {
    this.visibilityLevel = accessLevel;
    return this;
  }

  /**
   * Sets a priority to this event.
   * @param priority an event priority.
   * @return itself.
   */
  @Override
  public CalendarEvent withPriority(Priority priority) {
    this.priority = priority;
    return this;
  }

  /**
   * Gets the categories to which this event belongs.
   * @return the categories of this event.
   */
  @Override
  public Categories getCategories() {
    return categories;
  }

  /**
   * Gets the visibility level of this event.
   * @return the visibility level of this event.
   */
  public VisibilityLevel getVisibilityLevel() {
    return visibilityLevel;
  }

  /**
   * Gets a description about this event.
   * @return a description about this event or an empty string if no description is attached to this
   * event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets a new description to this event.
   * @param description a new description of the event.
   */
  public void setDescription(final String description) {
    if (description == null) {
      this.description = "";
    } else {
      this.description = description;
    }
  }

  /**
   * Gets the priority of this event.
   * @return the priority of the event.
   */
  @Override
  public Priority getPriority() {
    return priority;
  }

  /**
   * Gets the different additional attributes set to this event.
   * @return the additional attributes of this event.
   */
  public Attributes getAttributes() {
    return attributes;
  }

  /**
   * Gets the location where the event occurs. It can be an address, a designation or a GPS
   * coordinates.
   * @return the event's location.
   */
  public String getLocation() {
    return (this.location == null ? "" : this.location);
  }

  /**
   * Sets a new location for this event. It can be an address, a designation or a GPS coordinates.
   * @param location a location where the event occurs.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Recurs this event with the specified event recurrence.
   * @param recurrence the recurrence defining the recurring property of this event.
   * @return itself.
   */
  @Override
  public CalendarEvent recur(final Recurrence recurrence) {
    if (isOnAllDay() && recurrence.getFrequency().isHourly()) {
      throw new IllegalArgumentException("Impossible to recur hourly an event on all day!");
    }
    this.recurrence = recurrence;
    return this;
  }

  @Override
  public void unsetRecurrence() {
    this.recurrence = Recurrence.NO_RECURRENCE;
  }

  /**
   * Sets a description ot this event.
   * @param description the description to set.
   * @return itself.
   */
  public CalendarEvent withDescription(String description) {
    setDescription(description);
    return this;
  }

  /**
   * Adds the specified attribute among the attributes of this event. Same as
   * <pre>{@code
   * getAttributes().add(attrName, attrValue);
   * return this;}</pre>
   * @param attrName the name of the attribute to add.
   * @param attrValue the value of the attribute to add.
   * @return itself.
   */
  public CalendarEvent withAttribute(String attrName, String attrValue) {
    getAttributes().set(attrName, attrValue);
    return this;
  }

  /**
   * Adds the specified categories to the event. Same as
   * <pre>{@code
   * getCategories().addAll(categories);
   * return this;}</pre>
   * @param categories one or more categories with which this event will be categorized.
   * @return itself.
   */
  public CalendarEvent withCategories(String ...categories) {
    getCategories().addAll(categories);
    return this;
  }

  /**
   * Gets the recurrence of this recurring event. If the event isn't a recurring one, then returns
   * NO_RECURRENCE.
   * @return this event recurrence or NO_RECURRENCE.
   */
  @Override
  public Recurrence getRecurrence() {
    return this.recurrence;
  }

  /**
   * Is this event occurring on all the day(s)?
   * @return true if this event is occurring on all its day(s).
   */
  public boolean isOnAllDay() {
    return getPeriod().isInDays();
  }

  /**
   * BE CAREFUL, it is not possible to use the persistence when the ID is set by this method or
   * {@link #setId(String)}.<br>
   * TODO this behavior will be removed...
   * @param appId the identifier of the app
   * @param eventId the identifier of the event
   * @return itself
   */
  public CalendarEvent identifiedBy(String appId, String eventId) {
    if (getId() != null) {
      throw new IllegalStateException("identifier should be null on this method call");
    }
    setId(appId + "-" + eventId);
    return this;
  }

  @Override
  public Temporal getStartDate() {
    return getPeriod().getStartDate();
  }

  @Override
  public Temporal getEndDate() {
    return getPeriod().getEndDate();
  }

  /**
   * Changes the planning of this event in the calendar.
   * The change will be effective only once the {@code update} method invoked.
   * @param newPeriod a new period of time on which this event will occur or has actually occurred.
   */
  public void setPeriod(final Period newPeriod) {
    this.period = newPeriod;
  }

  /**
   * Changes the planning of this event in the calendar.
   * The change will be effective only once the {@code update} method invoked.
   * @param newDay the new day at which this event will occur or has actually occurred.
   */
  public void setDay(final LocalDate newDay) {
    this.period = Period.between(newDay, newDay);
  }

  @Override
  public CalendarEvent planOn(final Calendar calendar) {
    CalendarEvent event = Transaction.getTransaction().perform(() -> {
      if (!isPersisted()) {
        CalendarEventRepository repository = CalendarEventRepository.get();
        setCalendar(calendar);
        return repository.save(this);
      }
      return this;
    });
    notify(ResourceEvent.Type.CREATION, event);
    return event;
  }

  @Override
  public boolean isPlanned() {
    return isPersisted();
  }

  private void deleteFromPersistence() {
    if (isPersisted()) {
      Transaction.getTransaction().perform(() -> {
        CalendarEventRepository repository = CalendarEventRepository.get();
        repository.delete(this);
        return null;
      });
    }
    notify(ResourceEvent.Type.DELETION, this);
  }

  private void updateIntoPersistence() {
    if (getNativeId() != null) {
      Optional<CalendarEvent> before = Transaction.performInNew(() -> getCalendar().event(getId()));
      if (before.isPresent()) {
        CalendarEvent event = Transaction.getTransaction().perform(() -> {
          CalendarEventRepository repository = CalendarEventRepository.get();
          return repository.save(this);
        });
        notify(ResourceEvent.Type.UPDATE, before.get(), event);
      }
    }
  }

  /**
   * Gets the attendees. The adding or the removing of an attendee should be done
   * only by the creator of this event. Nevertheless, there is actually no validation of this
   * rule and it is left to the services to perform such a rule validation according to their own
   * requirements.
   * @return a set of attendees to this event.
   */
  public Set<Attendee> getAttendees() {
    return this.attendees;
  }

  /**
   * Adds an attendee in this event and returns itself. It is a short write of
   * {@code event.getAttendees().add(InternalAttendee.fromUser(user).to(event))}
   * @param user the user in Silverpeas whose participation in this event is required.
   * @return the event itself.
   */
  public CalendarEvent withAttendee(User user) {
    getAttendees().add(InternalAttendee.fromUser(user).to(this));
    return this;
  }

  /**
   * Adds an attendee in this event and returns itself. It is a short write of
   * {@code event.getAttendees().add(ExternalAttendee.withEmail(email).to(event))}
   * @param email the email of a user external to Silverpeas and whose the participation in this
   * event is required.
   * @return the event itself.
   */
  public CalendarEvent withAttendee(String email) {
    getAttendees().add(ExternalAttendee.withEmail(email).to(this));
    return this;
  }

  @Override
  public CalendarEvent clone() {
    CalendarEvent clone = super.clone();
    clone.externalId = null;
    if (recurrence == Recurrence.NO_RECURRENCE) {
      clone.recurrence = Recurrence.NO_RECURRENCE;
    } else {
      clone.recurrence = recurrence.clone();
    }
    clone.period = period.clone();
    clone.categories = categories.clone();
    clone.attributes = attributes.clone();
    clone.categories = categories.clone();
    clone.location = this.location;
    clone.attendees = new HashSet<>();
    attendees.forEach(a -> a.cloneFor(clone));
    return clone;
  }

  protected void setCalendar(final Calendar calendar) {
    this.calendar = calendar;
  }

  private Period getPeriod() {
    return this.period;
  }

  private void notify(ResourceEvent.Type type, CalendarEvent... events) {
    Transaction.performInNew(() -> {
      CalendarEventLifeCycleEventNotifier notifier = CalendarEventLifeCycleEventNotifier.get();
      notifier.notifyEventOn(type, events);
      return null;
    });
  }

  /**
   * Handles the simple deletion of an event.
   */
  @Transient
  private final Function<CalendarEvent, CalendarEventModificationResult> simpleDeletion =
      (previousEvent) -> {
        if (previousEvent != null) {
          previousEvent.deleteFromPersistence();
        }
        return new CalendarEventModificationResult();
      };

  /**
   * Deletes entirely the event referenced by this occurrence.
   */
  @Override
  public CalendarEventModificationResult delete() {
    return Transaction.performInOne(() -> simpleDeletion.apply(getPreviousEventData()));
  }

  /**
   * Deletes from this event the occurrence represented by the given occurrence identifier.
   * <ul>
   * <li>If the occurrence is the single one of the event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of the event, then the date time at which this
   * occurrence starts is added as an exception in the recurrence rule of the event.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   * @param data the occurrence necessary data to perform the operation.
   */
  public CalendarEventModificationResult delete(CalendarEventOccurrenceReferenceData data) {
    return doEitherOr(data, simpleDeletion, (previousEvent) -> {
      previousEvent.getRecurrence()
          .excludeEventOccurrencesStartingAt(data.getOriginalStartDate());
      previousEvent.getAttendees()
          .forEach(attendee -> attendee.resetParticipationOn(data.getOriginalStartDate()));
      previousEvent.updateIntoPersistence();
      return new CalendarEventModificationResult(previousEvent);
    });
  }

  /**
   * Deletes from this event the occurrence represented by the given occurrence identifier and
   * all the ones after.
   * <ul>
   * <li>If the occurrence is the single one of the event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of the event, then recurrence end date time is
   * updated.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   * @param data the occurrence necessary data to perform the operation.
   */
  public CalendarEventModificationResult deleteFrom(CalendarEventOccurrenceReferenceData data) {
    return doEitherOr(data, simpleDeletion, (previousEvent) -> {
      final Temporal endDate = data.getOriginalStartDate().minus(1, ChronoUnit.DAYS);
      previousEvent.getRecurrence().upTo(endDate);
      previousEvent.getAttendees()
          .forEach(attendee -> attendee.resetParticipationFrom(endDate));
      previousEvent.updateIntoPersistence();
      return new CalendarEventModificationResult(previousEvent);
    });
  }

  /**
   * Handles the simple update of an event.
   */
  @Transient
  private final Function<CalendarEvent, CalendarEventModificationResult> simpleUpdate =
      (previousEvent) -> {
        if (previousEvent == null) {
          return new CalendarEventModificationResult();
        }
        final CalendarEvent me = this;

        // Verify date changes
        final boolean dateChanged =
            !previousEvent.getStartDate().equals(me.getStartDate()) ||
            !previousEvent.getEndDate().equals(me.getEndDate());

        final boolean recurrenceChanged;
        final Recurrence currentRecurrence = me.getRecurrence();
        final Recurrence previousRecurrence = previousEvent.getRecurrence();
        if ((currentRecurrence != null && previousRecurrence == null) ||
            (currentRecurrence == null && previousRecurrence != null)) {
          recurrenceChanged = true;
        } else if (currentRecurrence != null) {
          if (!currentRecurrence.getDaysOfWeek().equals(previousRecurrence.getDaysOfWeek()) ||
              !currentRecurrence.getFrequency().equals(previousRecurrence.getFrequency()) ||
              currentRecurrence.getRecurrenceCount() != previousRecurrence.getRecurrenceCount()) {
            recurrenceChanged = true;
          } else {
            recurrenceChanged = false;
            if (!currentRecurrence.getExceptionDates()
                .equals(previousRecurrence.getExceptionDates())) {
              // Removing participation dates
              currentRecurrence.getExceptionDates().forEach(dateTime -> getAttendees()
                  .forEach(attendee -> attendee.resetParticipationOn(dateTime)));
            }
            if (currentRecurrence.getEndDate().isPresent() &&
                !currentRecurrence.getEndDate().equals(previousRecurrence.getEndDate())) {
              // Removing also participation dates
              getAttendees().forEach(attendee -> attendee
                  .resetParticipationFrom(currentRecurrence.getEndDate().get()));
            }
          }
        } else {
          recurrenceChanged = false;
        }

        // Clears exception dates when switching on all day data
        if (getRecurrence() != null && previousEvent.isOnAllDay() != isOnAllDay()) {
          getRecurrence().clearsAllExceptionDates();
        }

        // If it exists date or recurrence changes, participation of attendees are reset.
        if (dateChanged || recurrenceChanged) {
          me.getAttendees().forEach(Attendee::resetParticipation);
        }

        final boolean hasCalendarChanged =
            !previousEvent.getCalendar().getId().equals(me.getCalendar().getId());
        if (hasCalendarChanged) {
          // New event is created on other calendar
          CalendarEvent newEvent = me.clone();
          newEvent.planOn(me.getCalendar());
          // Deleting previous event
          simpleDeletion.apply(previousEvent);
          return new CalendarEventModificationResult(me);
        } else {
          me.updateIntoPersistence();
          return new CalendarEventModificationResult(me);
        }
      };

  /**
   * Merges the data of the given event into the data of current event.<br/>
   * It is not possible to merge the data of calendar which represents the repository of the event.
   * @param event the event to merge into current one.
   */
  public CalendarEventModificationResult merge(CalendarEvent event) {
    this.externalId = event.getExternalId();
    this.period = event.period;
    this.attributes = event.attributes;
    this.title = event.title;
    this.description = event.description;
    this.location = event.location;
    this.visibilityLevel = event.visibilityLevel;
    this.priority = event.priority;
    this.recurrence = event.recurrence;
    this.attendees.clear();
    this.attendees.addAll(event.attendees);
    this.categories = event.categories;
    return update();
  }

  /**
   * Updates entirely the event referenced by this occurrence.
   */
  @Override
  public CalendarEventModificationResult update() {
    return Transaction.performInOne(() -> simpleUpdate.apply(getPreviousEventData()));
  }

  /**
   * Applies the change done to this event the occurrence represented by the given occurrence
   * identifier. According to the state of the event, this will either create a new non-recurrent
   * event or update directly the event from which this occurrence was spawned:
   * <ul>
   * <li>The event is recurrent: the occurrence start date time before the change is set as an
   * exception date in the event's recurrence and a new event is created with the
   * modifications</li>
   * <li>It is the only occurrence of the event: the event is then directly modified.</li>
   * </ul>
   * @param data the occurrence necessary data to perform the operation.
   * event.
   */
  public CalendarEventModificationResult update(CalendarEventOccurrenceReferenceData data) {
    return doEitherOr(data, simpleUpdate, (previousEvent) -> {
      final CalendarEvent createdEvent = createNewEventFromMe(data.getPeriod(), true);
      previousEvent.getRecurrence()
          .excludeEventOccurrencesStartingAt(data.getOriginalStartDate());
      previousEvent.getAttendees()
          .forEach(attendee -> attendee.resetParticipationOn(data.getOriginalStartDate()));
      previousEvent.updateIntoPersistence();
      return new CalendarEventModificationResult(previousEvent, createdEvent);
    });
  }

  /**
   * Applies the change done to this occurrence from this occurrence period. According to the
   * state of the event, this will either create a new non-recurrent event or update directly the
   * event from which this occurrence was spawned:
   * <ul>
   * <li>The event is recurrent: the occurrence start date time before the change is set as the
   * end date minus one day of the event's recurrence and a new event is created with the
   * modifications</li>
   * <li>It is the only occurrence of the event: the event is then directly modified.</li>
   * </ul>
   * @param data the occurrence necessary data to perform the operation.
   * event.
   */
  public CalendarEventModificationResult updateFrom(CalendarEventOccurrenceReferenceData data) {
    return doEitherOr(data, simpleUpdate, (previousEvent) -> {
      final CalendarEvent createdEvent = createNewEventFromMe(data.getPeriod(), false);
      final Temporal endDate = data.getOriginalStartDate().minus(1, ChronoUnit.DAYS);
      previousEvent.getRecurrence().upTo(endDate);
      previousEvent.getAttendees()
          .forEach(attendee -> attendee.resetParticipationFrom(endDate));
      previousEvent.updateIntoPersistence();
      return new CalendarEventModificationResult(previousEvent, createdEvent);
    });
  }

  private CalendarEvent createNewEventFromMe(Period newPeriod, final boolean unsetRecurrence) {
    CalendarEvent me = this;
    CalendarEvent newEvent = me.clone();
    if (unsetRecurrence) {
      newEvent.unsetRecurrence();
    }
    newEvent.getAttendees().forEach(Attendee::resetParticipation);
    newEvent.setPeriod(newPeriod);
    newEvent.planOn(me.getCalendar());
    return newEvent;
  }

  private OffsetDateTime endDateTimeOf(final Recurrence recurrence,
      final OffsetDateTime fromRecurrenceStart) {
    return recurrence.getEndDate().orElseGet(() -> {
      RecurrencePeriod frequency = recurrence.getFrequency();
      long timeCount = frequency.getInterval() * recurrence.getRecurrenceCount();
      final OffsetDateTime result;
      switch (frequency.getUnit()) {
        case DAY:
          result = fromRecurrenceStart.plusDays(timeCount);
          break;
        case WEEK:
          result = fromRecurrenceStart.plusWeeks(timeCount);
          break;
        case MONTH:
          result = fromRecurrenceStart.plusMonths(timeCount);
          break;
        case YEAR:
          result = fromRecurrenceStart.plusYears(timeCount);
          break;
        default:
          throw new SilverpeasRuntimeException("Unsupported unit: " + frequency.getUnit());
      }
      return result;
    });
  }

  /**
   * Gets the previous data of an event from the persistence.
   * @return an instance of calendar event which contains data loaded from the persistence
   * without taking into account entity manager caches.
   */
  private CalendarEvent getPreviousEventData() {
    if (StringUtil.isNotDefined(this.getId())) {
      return null;
    }

    // Getting previous data from the persistence.
    final CalendarEvent event = Transaction.performInNew(() -> CalendarEvent.getById(this.getId()));

    // Checking that previous and new data concerns the same scope specified by the identifier of
    // a component instance.
    if (!event.getCalendar().getComponentInstanceId()
        .equals(this.getCalendar().getComponentInstanceId())) {
      throw new IllegalArgumentException("Impossible to recur hourly an event on all day!");
    }

    return event;
  }

  private CalendarEventModificationResult doEitherOr(CalendarEventOccurrenceReferenceData data,
      Function<CalendarEvent, CalendarEventModificationResult> ifSingleOccurrence,
      Function<CalendarEvent, CalendarEventModificationResult> ifManyOccurrences) {

    // Getting previous data from the persistence.
    final CalendarEvent previousEvent = getPreviousEventData();
    if (previousEvent == null) {
      return new CalendarEventModificationResult();
    }

    // Performing the treatment
    return Transaction.getTransaction().perform(() -> {
      final CalendarEventModificationResult result;
      if (previousEvent.isRecurrent() && previousEvent.getRecurrence().isEndless()) {
        result = ifManyOccurrences.apply(previousEvent);
      } else if (previousEvent.isRecurrent()) {
        OffsetDateTime recurrenceStart = Period.asOffsetDateTime(previousEvent.getStartDate());
        OffsetDateTime recurrenceEnd = endDateTimeOf(previousEvent.getRecurrence(), recurrenceStart);
        List<CalendarEventOccurrence> occurrences = generator()
            .generateOccurrencesOf(Collections.singletonList(previousEvent),
                Period.between(recurrenceStart, recurrenceEnd));
        if (occurrences.size() == 1 && data.concerns(occurrences.get(0))) {
          setPeriod(data.getPeriod());
          result = ifSingleOccurrence.apply(previousEvent);
        } else {
          result = ifManyOccurrences.apply(previousEvent);
        }
      } else {
        setPeriod(data.getPeriod());
        result = ifSingleOccurrence.apply(previousEvent);
      }
      return result;
    });
  }

  public static class CalendarEventModificationResult extends ModificationResult<CalendarEvent> {

    CalendarEventModificationResult() {
      this(null);
    }

    CalendarEventModificationResult(final CalendarEvent updatedEvent) {
      this(updatedEvent, null);
    }

    CalendarEventModificationResult(final CalendarEvent updatedEvent,
        final CalendarEvent createdEvent) {
      super(updatedEvent, createdEvent);
    }
  }

  @Transient
  private final Predicate<User> isUserParticipant =
      (user) -> !getAttendees().stream().filter(attendee -> attendee.getId().equals(user.getId()))
          .collect(Collectors.toList()).isEmpty();

  @Override
  public boolean canBeAccessedBy(final User user) {
    boolean canBeAccessed = getCalendar().canBeAccessedBy(user) || isUserParticipant.test(user);
    if (!canBeAccessed && PUBLIC == getVisibilityLevel()) {
      SilverpeasComponentInstance componentInstance =
          SilverpeasComponentInstance.getById(getCalendar().getComponentInstanceId()).orElse(null);
      if (componentInstance != null) {
        canBeAccessed = componentInstance.isPublic() || componentInstance.isPersonal();
      }
    }
    return canBeAccessed;
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController(ComponentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getCalendar().getComponentInstanceId(),
        AccessControlContext.init().onOperationsOf(AccessControlOperation.modification));
  }
}
