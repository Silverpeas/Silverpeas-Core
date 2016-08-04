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

package org.silverpeas.core.calendar;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * The event in a calendar. An event in a calendar is a {@link Recurrent} and a {@link Plannable}
 * general business object that can be planned on one and only one given existing {@link Calendar};
 * we ensures an event is unique in a per-calendar basis.
 * It occurs on a {@link Period} and as a such it must be well limited in the time (id est it must
 * have a start and an end dates/date times).
 * It can also be {@link Prioritized}, {@link Categorized}, and it can have some {@link Attendees}.
 * In order to be customized for different kinds of use, some additional information can be set
 * through its {@link Attributes} property.
 */
@Entity
@Table(name = "sb_calendar_event")
@NamedQueries({
    @NamedQuery(name = "byId", query = "from CalendarEvent where id = :id and calendar = " +
        ":calendar"),
    @NamedQuery(name = "byIds", query = "from CalendarEvent where calendar = :calendar and id in " +
        ":ids")})
@EntityListeners(Attributes.SerializationListener.class)
public class CalendarEvent extends AbstractJpaEntity<CalendarEvent, UuidIdentifier>
    implements Plannable, Recurrent, Categorized, Prioritized {

  private static final long serialVersionUID = 1L;

  @Embedded
  private Period period;

  @Embedded
  private Attributes attributes = new Attributes();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false)
  private Calendar calendar;

  @Column(name = "title", nullable = false)
  @Size(min = 1)
  @NotNull
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "visibility")
  @NotNull
  private String visibilityLevel = VisibilityLevel.PUBLIC.name();

  @Column(name = "priority", nullable = false)
  @NotNull
  private Priority priority = Priority.NORMAL;

  @Transient
  private Recurrence recurrence = Recurrence.NO_RECURRENCE;

  @Transient
  private final Categories categories = new Categories();

  @Transient
  private final Attendees attendees = new Attendees();

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

  /**
   * Specifies the visibility level to this event. In generally, it defines the intention of the
   * user about the visibility on the event he accepts to give. Usual values are PUBLIC, PRIVATE or
   * CONFIDENTIAL for example. By default, the visibility level is PUBLIC.
   * @param accessLevel the new visibility level to this event.
   * @return itself.
   */
  public CalendarEvent withVisibilityLevel(VisibilityLevel accessLevel) {
    this.visibilityLevel = accessLevel.name();
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
   * Gets the attendees to this event.
   * @return the attendees to this event.
   */
  public Attendees getAttendees() {
    return this.attendees;
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
    return VisibilityLevel.valueOf(visibilityLevel);
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
   * Gets the title of this event.
   * @return the event title or an empty string if the event has no title.
   */
  public String getTitle() {
    return title;
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

  /**
   * Sets a title to this event.
   * @param title the title to set.
   * @return itself.
   */
  public CalendarEvent withTitle(String title) {
    if (title == null) {
      this.title = "";
    } else {
      this.title = title;
    }
    return this;
  }

  /**
   * Sets a description ot this event.
   * @param description the description to set.
   * @return itself.
   */
  public CalendarEvent withDescription(String description) {
    if (description == null) {
      this.description = "";
    } else {
      this.description = description;
    }
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

  @Override
  public CalendarEvent saveOn(final Calendar calendar) {
    return calendar.getEvents().add(this);
  }

  protected void setCalendar(final Calendar calendar) {
    this.calendar = calendar;
  }

  private Period getPeriod() {
    return this.period;
  }
}
