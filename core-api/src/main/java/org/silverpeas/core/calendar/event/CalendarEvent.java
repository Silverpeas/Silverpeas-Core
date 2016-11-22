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

package org.silverpeas.core.calendar.event;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.*;
import org.silverpeas.core.calendar.event.notification.CalendarEventLifeCycleEventNotifier;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
        "CalendarEvent e LEFT OUTER JOIN FETCH e.attendees a LEFT OUTER JOIN FETCH e.recurrence r" +
        " where (e" +
        ".createdBy in :participantIds or a.attendeeId in :participantIds) and ((e.period" +
        ".startDateTime <= :startDateTime and e.period.endDateTime >= :startDateTime) or (e" +
        ".period.startDateTime >= :startDateTime and e.period.startDateTime <= :endDateTime) or " +
        "(e.period.endDateTime < :startDateTime and e.recurrence is not null and (e.recurrence" +
        ".endDateTime >= :startDateTime or e.recurrence.endDateTime is null))) order by e" +
        ".createdBy, e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsByCalendarByParticipantsByPeriod", query =
        "select distinct e from " +
        "CalendarEvent e LEFT OUTER JOIN FETCH e.attendees a LEFT OUTER JOIN FETCH e.recurrence r" +
        " where e.calendar in :calendars and (e" +
        ".createdBy in :participantIds or a.attendeeId in :participantIds) and ((e.period" +
        ".startDateTime <= :startDateTime and e.period.endDateTime >= :startDateTime) or (e" +
        ".period.startDateTime >= :startDateTime and e.period.startDateTime <= :endDateTime) or " +
        "(e.period.endDateTime < :startDateTime and e.recurrence is not null and (e.recurrence" +
        ".endDateTime >= :startDateTime or e.recurrence.endDateTime is null))) order by e" +
        ".createdBy, e.period.startDateTime"),
    @NamedQuery(name = "calendarEventsDeleteAll", query =
        "delete from CalendarEvent e where e.calendar = " + ":calendar")})
public class CalendarEvent extends SilverpeasJpaEntity<CalendarEvent, UuidIdentifier>
    implements Plannable, Recurrent, Categorized, Prioritized {

  private static final long serialVersionUID = 1L;

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
    return (this.location == null ? "":this.location);
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
    this.recurrence = null;
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
    getAttributes().add(attrName, attrValue);
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
   * {@link #setId(String)}.<br/>
   * TODO this behavior will be removed...
   */
  public CalendarEvent identifiedBy(String appId, String eventId) {
    if (getId() != null) {
      throw new IllegalStateException("identifier should be null on this method call");
    }
    setId(appId + "-" + eventId);
    return this;
  }

  @Override
  public OffsetDateTime getStartDateTime() {
    return getPeriod().getStartDateTime();
  }

  @Override
  public OffsetDateTime getEndDateTime() {
    return getPeriod().getEndDateTime();
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

  @Override
  public void delete() {
    if (isPersisted()) {
      Transaction.getTransaction().perform(() -> {
        CalendarEventRepository repository = CalendarEventRepository.get();
        repository.delete(this);
        return null;
      });
    }
    notify(ResourceEvent.Type.DELETION, this);
  }

  @Override
  public void update() {
    if (getNativeId() != null) {
      Optional<CalendarEvent> before = getCalendar().event(getId());
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
    clone.recurrence = recurrence.clone();
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
    CalendarEventLifeCycleEventNotifier notifier = CalendarEventLifeCycleEventNotifier.get();
    notifier.notifyEventOn(type, events);
  }
}
