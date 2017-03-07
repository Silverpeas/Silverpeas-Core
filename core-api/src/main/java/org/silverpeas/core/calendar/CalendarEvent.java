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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.notification.CalendarEventLifeCycleEventNotifier;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.silverpeas.core.calendar.VisibilityLevel.PUBLIC;

/**
 * An event planned in a calendar.
 *
 * An event in a calendar is a possibly {@link Recurrent} and a {@link Plannable} general business
 * component that can be planned on one and only one given existing {@link Calendar};
 * we ensures an event is unique in a per-calendar basis.
 * It occurs on a {@link Period} and as a such it must be well limited in the time (id est it must
 * have a start and an end dates/datetimes).
 * It can also be {@link Prioritized}, {@link Categorized}, and it can have some {@link Attendee}s.
 * In order to be customized for different kinds of use, some additional information can be set
 * through its {@link Attributes} property.
 *
 * An event in a calendar in Silverpeas can be originated from an external calendar. This comes
 * from an export process of the events planned in an external calendar (for example from a calendar
 * in Google Calendar). When such an event is in a Silverpeas's calendar, then the identifier of
 * this event on the external calendar can be get with the {@link CalendarEvent#getExternalId()}
 * method.
 *
 * When a list of events is retrieved from one or more calendars, they are all ordered by the
 * component instance that owns the calendar, then by the calendar on which they are planned, then
 * by the user who authored them, and finally by their starting  date in the timeline of the
 * calendar.
 */
@Entity
@Table(name = "sb_cal_event")
@NamedQueries({
    @NamedQuery(name = "calendarEventCount", query =
        "SELECT COUNT(e) FROM CalendarEvent e WHERE e.component.calendar = :calendar"),
    @NamedQuery(name = "calendarEvents", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventByCalendarAndExternalId", query =
        "SELECT e FROM CalendarEvent e " +
            "WHERE e.component.calendar = :calendar AND e.externalId = :externalId"),
    @NamedQuery(name = "calendarEventsByCalendar", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "WHERE c IN :calendars " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByParticipants", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN cmp.attendees a " +
            "WHERE (cmp.createdBy IN :participantIds OR a.attendeeId IN :participantIds) " +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipants", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN cmp.attendees a " +
            "WHERE c IN :calendars " +
            "AND (cmp.createdBy IN :participantIds OR a.attendeeId IN :participantIds)" +
            "ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarByPeriod", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE c IN :calendars AND (" +
            "(cmp.period.startDateTime <= :endDateTime AND " +
            "  cmp.period.endDateTime >= :startDateTime) OR " +
            "(cmp.period.endDateTime < :startDateTime AND e.recurrence IS NOT NULL AND " +
            "  (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL)))" +
            " ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByPeriod", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE (cmp.period.startDateTime <= :endDateTime AND " +
            "  cmp.period.endDateTime >= :startDateTime) OR " +
            "(cmp.period.endDateTime < :startDateTime AND e.recurrence IS NOT NULL AND " +
            "  (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL))"  +
            " ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByParticipantsByPeriod", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN cmp.attendees a " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE (cmp.createdBy IN :participantIds OR a.attendeeId in :participantIds) AND " +
            "((cmp.period.startDateTime <= :endDateTime AND " +
            "   cmp.period.endDateTime >= :startDateTime) OR " +
            " (cmp.period.endDateTime < :startDateTime AND e.recurrence IS NOT NULL AND " +
            "   (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL))" +
            ")" +
            " ORDER BY ob_1, ob_2, ob_3"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipantsByPeriod", query =
        "SELECT distinct e" +
            ", c.componentInstanceId as ob_1" +
            ", c.id as ob_2" +
            ", cmp.period.startDateTime as ob_3 " +
            "FROM CalendarEvent e " +
            "JOIN e.component cmp " +
            "JOIN cmp.calendar c " +
            "LEFT OUTER JOIN cmp.attendees a " +
            "LEFT OUTER JOIN FETCH e.recurrence r " +
            "WHERE c IN :calendars AND " +
            "(cmp.createdBy IN :participantIds OR a.attendeeId IN :participantIds) AND " +
            "((cmp.period.startDateTime <= :endDateTime AND " +
            "   cmp.period.endDateTime >= :startDateTime) OR " +
            " (cmp.period.endDateTime < :startDateTime AND e.recurrence IS NOT NULL AND " +
            "   (e.recurrence.endDateTime >= :startDateTime OR e.recurrence.endDateTime IS NULL)))"
            +
            " ORDER BY ob_1, ob_2, ob_3")})
public class CalendarEvent extends BasicJpaEntity<CalendarEvent, UuidIdentifier>
    implements Plannable, Recurrent, Categorized, Prioritized, Securable {

  private static final long serialVersionUID = 1L;

  @Column(name = "externalId")
  private String externalId;

  @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "componentId", referencedColumnName = "id", unique = true)
  private CalendarComponent component;

  @Column(name = "visibility")
  @Enumerated(EnumType.STRING)
  @NotNull
  private VisibilityLevel visibilityLevel = VisibilityLevel.PUBLIC;

  @Embedded
  private Categories categories = new Categories();

  @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
  @JoinColumn(name = "recurrenceId", referencedColumnName = "id", unique = true)
  private Recurrence recurrence = Recurrence.NO_RECURRENCE;

  private static CalendarEventOccurrenceGenerator generator() {
    return CalendarEventOccurrenceGenerator.get();
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
   * An external identifier is the identifier of an event that was imported from a external
   * calendar into a calendar in Silverpeas. This identifier is the one of the event in the
   * external calendar.
   * @param calendar the calendar repository.
   * @param externalId the identifier of the calendar event in the external calendar from which it
   * was imported.
   * @return the instance of the asked calendar event or null if it does not exist.
   */
  @SuppressWarnings("unused")
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

  /**
   * Constructs a new calendar event that spawns to the specified period of time.
   * @param period a period of time in which this event occurs.
   */
  protected CalendarEvent(Period period) {
    this.component = new CalendarComponent(period);
  }

  /**
   * Constructs an empty calendar event. This constructor is dedicated to the persistence engine
   * when loading events from the data source.
   */
  protected CalendarEvent() {
    // this constructor is for the persistence engine.
  }

  /**
   * This event is created by the specified user.
   * @param user the user to set as the creator of this event.
   * @return itself.
   */
  public CalendarEvent createdBy(final User user) {
    this.component.createdBy(user);
    return this;
  }

  /**
   * This event is created by the specified user.
   * @param userId the unique identifier of the user to set as the creator of this event.
   * @return itself.
   */
  public CalendarEvent createdBy(final String userId) {
    this.component.createdBy(userId);
    return this;
  }

  /**
   * Gets the user who created and planned this event.
   * @return the user that has authored this event.
   */
  public User getCreator() {
    return this.component.getCreator();
  }

  /**
   * Gets the last user who updated this planned event.
   * @return the user who has last updated this event.
   */
  public User getLastUpdater() {
    return this.component.getLastUpdater();
  }

  public Date getCreationDate() {
    return this.component.getCreateDate();
  }

  public Date getLastUpdateDate() {
    return this.component.getLastUpdateDate();
  }

  /**
   * Gets the calendar to which this event is related. A calendar event can only be persisted into
   * a given existing calendar.
   * @return either the calendar to which this event belongs or null if this event isn't yet
   * saved into a given calendar.
   */
  @Override
  public Calendar getCalendar() {
    return this.component.getCalendar();
  }

  @Override
  public String getTitle() {
    return component.getTitle();
  }

  @Override
  public void setTitle(String title) {
    this.component.setTitle(title);
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
  @SuppressWarnings("WeakerAccess")
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
    this.component.setPriority(priority);
    return this;
  }

  /**
   * Gets the categories to which this event belongs.
   * @return the categories of this event.
   */
  @Override
  public Categories getCategories() {
    return this.categories;
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
    return component.getDescription();
  }

  /**
   * Sets a new description to this event.
   * @param description a new description of the event.
   */
  public void setDescription(final String description) {
    this.component.setDescription(description);
  }

  /**
   * Gets the priority of this event.
   * @return the priority of the event.
   */
  @Override
  public Priority getPriority() {
    return this.component.getPriority();
  }

  /**
   * Gets the different additional attributes set to this event.
   * @return the additional attributes of this event.
   */
  public Attributes getAttributes() {
    return this.component.getAttributes();
  }

  /**
   * Gets the revision sequence number of this calendar event within a sequence of revisions.
   * Any changes to some properties of a calendar event increment this sequence number. This
   * number is mainly dedicated with the synchronization or syndication mechanism of calendar
   * events with external calendars. Its meaning comes from the icalendar specification.
   * @return the sequence number of this event.
   * @see CalendarComponent#getSequence()
   */
  public long getSequence() {
    return this.component.getSequence();
  }

  /**
   * Gets the location where the event occurs. It can be an address, a designation or a GPS
   * coordinates.
   * @return the event's location.
   */
  public String getLocation() {
    return this.component.getLocation();
  }

  /**
   * Sets a new location for this event. It can be an address, a designation or a GPS coordinates.
   * @param location a location where the event occurs.
   */
  public void setLocation(String location) {
    this.component.setLocation(location);
  }

  /**
   * Recurs this event with the specified event recurrence.
   *
   * If the recurrence ends up at a given date or datetime, then this value is updated according to
   * the period of time of this event:
   *
   * <ul>
   *  <li>
   *    The event is on all the day: the recurrence rule is updated to end at the given recurrence
   *    ending date; the time part is removed.
   *  </li>
   *  <li>
   *    The event starts and ends at a given datetime: the recurrence rule is updated to end at
   *    a datetime with as date the given recurrence ending date and as time the time at which
   *    this event usually starts.
   *  </li>
   * </ul>
   * @param recurrence the recurrence defining the recurring property of this event.
   * @return itself.
   */
  @Override
  public CalendarEvent recur(final Recurrence recurrence) {
    if (isOnAllDay() && recurrence.getFrequency().isHourly()) {
      throw new IllegalArgumentException("Impossible to recur hourly an event on all day!");
    }
    this.recurrence = recurrence.startingAt(this.getStartDate());
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
    this.component.setPeriod(newPeriod);
    if (this.recurrence != null) {
      this.recurrence = this.recurrence.startingAt(newPeriod.getStartDate());
    }
  }

  /**
   * Changes the planning of this event in the calendar.
   * The change will be effective only once the {@code update} method invoked.
   * @param newDay the new day at which this event will occur or has actually occurred.
   */
  public void setDay(final LocalDate newDay) {
    this.component.setPeriod(Period.between(newDay, newDay));
    if (this.recurrence != null) {
      this.recurrence = this.recurrence.startingAt(newDay);
    }
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

  /**
   * Gets the attendees. The adding or the removing of an attendee should be done
   * only by the creator of this event. Nevertheless, there is actually no validation of this
   * rule and it is left to the services to perform such a rule validation according to their own
   * requirements.
   * @return a set of attendees to this event.
   */
  public Set<Attendee> getAttendees() {
    return this.component.getAttendees();
  }

  /**
   * Adds an attendee in this event and returns itself. It is a short write of
   * {@code event.getAttendees().add(InternalAttendee.fromUser(user).to(event))}
   * @param user the user in Silverpeas whose participation in this event is required.
   * @return the event itself.
   */
  public CalendarEvent withAttendee(User user) {
    getAttendees().add(InternalAttendee.fromUser(user).to(asCalendarComponent()));
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
    getAttendees().add(ExternalAttendee.withEmail(email).to(asCalendarComponent()));
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
    clone.categories = categories.clone();
    clone.component = component.clone();
    clone.visibilityLevel = visibilityLevel;
    return clone;
  }

  /**
   * Deletes entirely the event referenced by this occurrence.
   */
  @Override
  public EventOperationResult delete() {
    return Transaction.performInOne(() -> simpleDeletion.apply(getEventPreviousState()));
  }

  /**
   * Deletes from this event the occurrence referred by the given occurrence reference.
   *
   * <ul>
   * <li>If the occurrence is the single one of the event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of the event, then the datetime at which this
   * occurrence starts is added as an exception in the recurrence rule of the event.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   * @param occurrence a reference to the occurrence to delete.
   */
  public EventOperationResult deleteOnly(CalendarEventOccurrence occurrence) {
    return doEitherOr(occurrence, simpleDeletion, (me) -> {
      me.getRecurrence()
          .excludeEventOccurrencesStartingAt(occurrence.getOriginalStartDate());
      occurrence.delete();
      me.updateIntoPersistence();
      return new EventOperationResult().withUpdated(me);
    });
  }

  /**
   * Deletes from this event all the occurrences since the occurrence referred by the specified
   * occurrence reference.
   *
   * <ul>
   * <li>If the occurrence is the single one of the event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of the event, then the recurrence end datetime is
   * updated.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   * @param occurrence a reference to the occurrence since which all the occurrences (with the
   * referred occurrence) will be deleted.
   */
  public EventOperationResult deleteSince(CalendarEventOccurrence occurrence) {
    return doEitherOr(occurrence, simpleDeletion, (me) -> {
      final Temporal endDate = occurrence.getOriginalStartDate().minus(1, ChronoUnit.DAYS);
      me.getRecurrence().until(endDate);
      occurrence.deleteAllSinceMe();
      me.updateIntoPersistence();
      return new EventOperationResult().withUpdated(me);
    });
  }

  /**
   * Merges the data of the given event into the data of current event.<br/>
   * It is not possible to merge the data of calendar which represents the repository of the event.
   * @param event the event to merge into current one.
   */
  public EventOperationResult merge(CalendarEvent event) {
    this.externalId = event.getExternalId();
    this.component = event.component;
    this.visibilityLevel = event.visibilityLevel;
    this.recurrence = event.recurrence;
    this.categories = event.categories;
    return update();
  }

  /**
   * Updates this event. The modifications to this event are saved for all its occurrences.
   * Its sequence number is incremented by one.
   */
  @Override
  public EventOperationResult update() {
    return Transaction.performInOne(() -> simpleUpdate.apply(getEventPreviousState()));
  }

  /**
   * Updates all the occurrences of this event since and including the specified occurrence with
   * the modifications to this event. The modifications to this event are saved for all the
   * occurrences since and including the specified occurrence. The occurrences occurring before the
   * specified occurrence won't be updated.
   *
   * If the specified occurrence is in fact the single one of this event, then this event is
   * itself updated (this is equivalent to the {@link CalendarEvent#update()} method. Otherwise a
   * new event is created from the modifications to this event.
   * @param occurrence the occurrence of the event since which the changes to the event should be
   * applied.
   */
  public EventOperationResult updateSince(CalendarEventOccurrence occurrence) {
    return doEitherOr(occurrence, simpleUpdate,
        (me) -> {
          final CalendarEvent createdEvent = createNewEventFromMeSince(occurrence);
          final Temporal endDate = occurrence.getOriginalStartDate().minus(1, ChronoUnit.DAYS);
          me.getRecurrence().until(endDate);
          occurrence.deleteAllSinceMe();
          me.updateIntoPersistence();
          return new EventOperationResult().withUpdated(me).withCreated(createdEvent);
        });
  }

  /**
   * Updates only the specified occurrence among the occurrences of this event. If the given
   * occurrence is the single one of the event then the event is itself updated. Otherwise the
   * changes in the occurrence are persisted and its sequence number is incremented by one,
   * diverging then from the sequence number of the event it comes from.
   * <p>
   * In the case the date at which the occurrence starts is modified, the participation status
   * of all the attendees in this occurrence is cleared.
   * @param occurrence a reference to an occurrence of the event with the data modified.
   */
  public EventOperationResult updateOnly(CalendarEventOccurrence occurrence) {
    return doEitherOr(occurrence, simpleUpdate,
        (previousEvent) -> {
          CalendarEventOccurrence previous = CalendarEventOccurrence.getById(occurrence.getId());
          if ((previous != null && !previous.getPeriod().equals(occurrence.getPeriod())) ||
              (previous == null &&
                  !occurrence.getOriginalStartDate().equals(occurrence.getStartDate()))) {
            occurrence.getAttendees().forEach(Attendee::resetParticipation);
          }
          occurrence.save();
          return new EventOperationResult().withInstance(occurrence);
    });
  }

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

  /**
   * Gets the {@link CalendarComponent} representation of this event. Any change to the returned
   * calendar component will change also the related event.
   * @return a {@link CalendarComponent} instance representing this event (without the specific
   * properties related to a calendar event).
   */
  public CalendarComponent asCalendarComponent() {
    return this.component;
  }

  /**
   * Sets a new calendar to this event. This moves the event from its initial calendar to the
   * specified calendar. This will be effective once the {@link CalendarEvent#update()} method
   * invoked.
   * @param calendar the new calendar into which the event has to move.
   */
  protected void setCalendar(final Calendar calendar) {
    this.component.setCalendar(calendar);
  }

  @PostLoad
  protected void afterLoadingFromPersistenceContext() {
    if (this.recurrence != null) {
      this.recurrence = this.recurrence.startingAt(this.getStartDate());
    }
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

  private Period getPeriod() {
    return this.component.getPeriod();
  }

  private void notify(ResourceEvent.Type type, CalendarEvent... events) {
    Transaction.performInNew(() -> {
      CalendarEventLifeCycleEventNotifier notifier = CalendarEventLifeCycleEventNotifier.get();
      notifier.notifyEventOn(type, events);
      return null;
    });
  }

  private CalendarEvent createNewEventFromMeSince(final CalendarEventOccurrence occurrence) {
    CalendarEvent newEvent = this.clone();
    newEvent.asCalendarComponent().incrementSequence();
    newEvent.getAttendees().forEach(Attendee::resetParticipation);
    newEvent.setPeriod(occurrence.getPeriod());
    newEvent.planOn(this.getCalendar());
    return newEvent;
  }

  private OffsetDateTime endDateTimeOf(final Recurrence recurrence,
      final OffsetDateTime fromRecurrenceStart) {
    return Period.asOffsetDateTime(recurrence.getEndDate().orElseGet(() -> {
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
    }));
  }

  /**
   * Gets the previous state of an event from the persistence.
   * @return an instance of this calendar event but with its states loaded from the persistence
   * context and without taking into account entity manager caches.
   */
  private CalendarEvent getEventPreviousState() {
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

  private EventOperationResult doEitherOr(CalendarEventOccurrence occurrence,
      Function<CalendarEvent, EventOperationResult> ifSingleOccurrence,
      Function<CalendarEvent, EventOperationResult> ifManyOccurrences) {

    // Getting previous data from the persistence.
    final CalendarEvent eventPreviousState = getEventPreviousState();
    if (eventPreviousState == null) {
      return new EventOperationResult();
    }

    // Performing the treatment
    return Transaction.getTransaction().perform(() -> {
      final EventOperationResult result;
      if (eventPreviousState.isRecurrent() && eventPreviousState.getRecurrence().isEndless()) {
        result = ifManyOccurrences.apply(eventPreviousState);
      } else if (eventPreviousState.isRecurrent()) {
        OffsetDateTime recurrenceStart = Period.asOffsetDateTime(eventPreviousState.getStartDate());
        OffsetDateTime recurrenceEnd =
            endDateTimeOf(eventPreviousState.getRecurrence(), recurrenceStart);
        List<CalendarEventOccurrence> occurrences = generator()
            .generateOccurrencesOf(Stream.of(eventPreviousState),
                Period.between(recurrenceStart, recurrenceEnd));
        if (occurrences.size() == 1 && occurrence.equals(occurrences.get(0))) {
          setPeriod(occurrence.getPeriod());
          result = ifSingleOccurrence.apply(eventPreviousState);
        } else {
          result = ifManyOccurrences.apply(eventPreviousState);
        }
      } else {
        setPeriod(occurrence.getPeriod());
        result = ifSingleOccurrence.apply(eventPreviousState);
      }
      return result;
    });
  }

  /**
   * Handles the simple deletion of an event.
   */
  @Transient
  private final Function<CalendarEvent, EventOperationResult> simpleDeletion =
      (previousState) -> {
        if (previousState != null) {
          previousState.deleteFromPersistence();
        }
        return new EventOperationResult();
      };

  /**
   * Handles the simple update of an event.
   */
  @Transient
  private final Function<CalendarEvent, EventOperationResult> simpleUpdate =
      (previousState) -> {
        if (previousState == null) {
          return new EventOperationResult();
        }
        final CalendarEvent me = this;

        // Verify date changes
        final boolean dateChanged =
            !previousState.getStartDate().equals(me.getStartDate()) ||
                !previousState.getEndDate().equals(me.getEndDate());

        boolean recurrenceChanged = false;
        final Recurrence currentRecurrence = me.getRecurrence();
        final Recurrence previousRecurrence = previousState.getRecurrence();
        if ((currentRecurrence != null && previousRecurrence == null) ||
            (currentRecurrence == null && previousRecurrence != null)) {
          recurrenceChanged = true;
        } else if (currentRecurrence != null) {
          if (!currentRecurrence.getDaysOfWeek().equals(previousRecurrence.getDaysOfWeek()) ||
              !currentRecurrence.getFrequency().equals(previousRecurrence.getFrequency()) ||
              currentRecurrence.getRecurrenceCount() != previousRecurrence.getRecurrenceCount()) {
            recurrenceChanged = true;
          }
        }

        // Clears exception dates when switching on all day data
        if (getRecurrence() != null && previousState.isOnAllDay() != isOnAllDay()) {
          getRecurrence().clearsAllExceptionDates();
        }

        // If it exists date or recurrence changes, participation of attendees are reset.
        if (dateChanged || recurrenceChanged) {
          me.getAttendees().forEach(Attendee::resetParticipation);
        }

        final boolean hasCalendarChanged =
            !previousState.getCalendar().getId().equals(me.getCalendar().getId());
        if (hasCalendarChanged) {
          // New event is created on other calendar
          CalendarEvent newEvent = me.clone();
          newEvent.planOn(me.getCalendar());
          // Deleting previous event
          simpleDeletion.apply(previousState);
          return new EventOperationResult().withCreated(newEvent);
        } else {
          me.component.markAsModified();
          me.updateIntoPersistence();
          return new EventOperationResult().withUpdated(me);
        }
      };

  @Transient
  private final Predicate<User> isUserParticipant =
      (user) -> !getAttendees().stream().filter(attendee -> attendee.getId().equals(user.getId()))
          .collect(Collectors.toList()).isEmpty();

  public static class EventOperationResult
      extends OperationResult<CalendarEvent, CalendarEventOccurrence> {

  }

}
