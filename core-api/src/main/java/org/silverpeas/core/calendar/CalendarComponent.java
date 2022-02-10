/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.silverpeas.core.persistence.datasource.OperationContext.State.IMPORT;

/**
 * A calendar component is a set of properties that express a common semantic for all objects
 * planned in a calendar. Those objects can be an event, a to-do, and so one and they share a
 * common set of properties that are defined in this class.
 *
 * This class is dedicated to be used into an implementation composition by the more concrete
 * representations of a calendar component (like for example {@link CalendarEvent}). We recommend
 * strongly to force the update of the {@link CalendarComponent} instance when a property is
 * modified in the outer object; this is a requirement for date properties (like the recurrence
 * rule for a recurrent outer object).
 * @author mmoquillon
 */
@Entity
@Table(name = "sb_cal_components")
public class CalendarComponent extends SilverpeasJpaEntity<CalendarComponent, UuidIdentifier> {

  static final int DESCRIPTION_MAX_LENGTH = 2000;
  static final int TITLE_MAX_LENGTH = 255;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false)
  private Calendar calendar;

  @Embedded
  private Period period;

  @Column(name = "title", nullable = false)
  @Size(min = 1, max = TITLE_MAX_LENGTH)
  @NotNull
  private String title;

  @Column(name = "description")
  @Size(max = DESCRIPTION_MAX_LENGTH)
  private String description;

  @Column(name = "location")
  @Size(max = TITLE_MAX_LENGTH)
  private String location;

  @Column(name = "priority", nullable = false)
  @NotNull
  private Priority priority = Priority.NORMAL;

  @Embedded
  private AttributeSet attributes = new AttributeSet();

  @Column(nullable = false)
  private long sequence = 0L;

  @Transient
  private boolean sequenceUpdated = false;

  @Embedded
  private AttendeeSet attendees = new AttendeeSet(this);

  /**
   * Constructs an empty calendar component. This method is dedicated to the persistence engine
   * when loading the components from the data source.
   */
  protected CalendarComponent() {
    // this constructor is for the persistence engine.
  }

  /**
   * Constructs a new calendar component for the specified period of time.
   * This constructor shouldn't invoked directly but it is dedicated to the different more concrete
   * calendar components like the {@link CalendarEvent} class for example.
   * @param period the period of time in which this calendar component occurs.
   */
  CalendarComponent(final Period period) {
    this.period = period;
  }

  /**
   * Gets the calendar to which this component is related. A calendar component can only be in one
   * existing calendar.
   * @return either the calendar into which this component is or null if this component wasn't yet
   * put into a given calendar.
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Sets a calendar to this component. This puts this component into the specified calendar. If
   * the component is already in another calendar, then this is like to move the component to the
   * specified calendar.
   * @param calendar the calendar into which this component has to move.
   */
  public void setCalendar(final Calendar calendar) {
    this.calendar = calendar;
  }

  /**
   * Gets the period in the calendar of this calendar component.
   * @return a period of time this component occurs in the calendar.
   */
  public Period getPeriod() {
    return period;
  }

  /**
   * Sets a new period for this calendar component. This moves the component in the timeline of
   * the calendar.
   * @param newPeriod a new period to set for this calendar component.
   */
  public void setPeriod(final Period newPeriod) {
    this.period = newPeriod;
  }

  /**
   * Gets the title of this calendar component.
   * @return the title of this calendar component.
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Sets a new title for this calendar component.
   * @param title the new title to set.
   */
  public void setTitle(final String title) {
    if (title == null) {
      this.title = "";
    } else {
      this.title = title;
    }
  }

  /**
   * Gets the description of this calendar component.
   * @return the description of this calendar component.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets a new description for this calendar component.
   * @param description the new description to set.
   */
  public void setDescription(String description) {
    if (description == null) {
      this.description = "";
    } else {
      this.description = description;
    }
  }

  /**
   * Gets the location of this calendar component. The location is where this component takes place.
   * It can be an address, a designation or a GPS coordinates.
   * According to the semantic expressed by the component, the location can be meaningless.
   * @return the location where this component takes place.
   */
  public String getLocation() {
    return this.location == null ? "" : this.location;
  }

  /**
   * Sets a new location for calendar component. The location is where this component takes place.
   * It can be an address, a designation or a GPS coordinates.
   * According to the semantic expressed by the component, the location can be meaningless.
   * @param location the new location to set.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Gets the priority of this calendar component.
   * @return the priority of this calendar component.
   */
  public Priority getPriority() {
    return this.priority;
  }

  /**
   * Sets a new priority for this calendar component.
   * @param priority the new priority to set.
   */
  public void setPriority(final Priority priority) {
    this.priority = priority;
  }

  /**
   * Gets the additional and custom attributes of this calendar component. The attributes can be
   * directly managed through the returned {@link AttributeSet} instance.
   * @return the additional attributes of this calendar component.
   */
  public AttributeSet getAttributes() {
    return this.attributes;
  }

  /**
   * Gets the attendees in this calendar component. An attendee can be either an internal user in
   * Silverpeas ({@link InternalAttendee}) or a user external from Silverpeas.
   * ({@link ExternalAttendee}). The creator of a calendar component isn't considered as an attendee
   * and hence he's not present among the attendees of in the component. If his participation has to
   * be explicit, then he should be added as a participant in the attendees in the calendar
   * component.
   * @return the attendees in this calendar component.
   */
  public AttendeeSet getAttendees() {
    return attendees.withCalendarComponent(this);
  }

  /**
   * Gets the revision sequence number of the calendar component within a sequence of revisions.
   * Any changes to some properties of a calendar component increment this sequence number. This
   * number is mainly dedicated with the synchronization or syndication mechanism of calendar
   * components with external calendars. Its meaning comes from the icalendar specification.
   *
   * The sequence number will be always incremented when a date property is modified (the period at
   * which the component occurs in the calendar, the recurrence rule for a recurrent component).
   * Nevertheless, it can also be incremented with the change of some other properties.
   *
   * Actually the sequence number has the same meaning than the version number
   * ({@link CalendarComponent#getVersion()}) minus the sequence number can to be not incremented
   * with the change of some properties. This means than the version number matches always the
   * number of updates of the calendar component whereas the sequence number matches only a
   * subset of update of the calendar component.
   * @return the revision number of this calendar component;
   */
  public long getSequence() {
    return this.sequence;
  }

  /**
   * Copies this calendar component. Only the state of this calendar component is cloned to a new
   * calendar component. The returned calendar component is not planned in any calendar.
   * @see CalendarComponent#copyTo(CalendarComponent)
   * @return an unplanned clone of this calendar component.
   */
  public CalendarComponent copy() {
    CalendarComponent copy = new CalendarComponent();
    copy.calendar = calendar;
    copy.sequence = sequence;
    copy.sequenceUpdated = false;
    return copyTo(copy);
  }

  /**
   * Copies the state of this calendar component to the specified other calendar component. The
   * unique identifier, the calendar to which this component is planned and the sequence number
   * aren't copied. Those are particular properties to each calendar component as they form both
   * the identify of a calendar component.
   * @param anotherComponent another calendar component.
   * @return the calendar component valued with the state of this component.
   */
  public CalendarComponent copyTo(final CalendarComponent anotherComponent) {
    anotherComponent.title = title;
    anotherComponent.description = description;
    anotherComponent.location = location;
    anotherComponent.period = period.copy();
    anotherComponent.priority = priority;
    anotherComponent.attributes = attributes.copy();
    if (OperationContext.statesOf(IMPORT)) {
      if (StringUtil.isNotDefined(anotherComponent.getId())) {
        // In case of import, the attendees are not modified
        AttendeeSet existingAttendees = anotherComponent.attendees;
        anotherComponent.attendees = new AttendeeSet(anotherComponent);
        existingAttendees.forEach(a -> a.copyFor(anotherComponent));
      }
    } else {
      anotherComponent.attendees = new AttendeeSet(anotherComponent);
      attendees.forEach(a -> a.copyFor(anotherComponent));
    }
    return anotherComponent;
  }

  /**
   * Sets explicitly the sequence number to this calendar component. This method is to be used
   * by the internal mechanisms of the Silverpeas Calendar Engine when working with components
   * detached from any persistence context or with copies of calendar components.
   *
   * @param sequence
   */
  void setSequence(final long sequence) {
    this.sequence = sequence;
    this.sequenceUpdated = true;
  }

  /**
   * Increments the current sequence number of this calendar component by one.
   * <p>In case of import, the sequence is not incremented. Please consult
   * {@link OperationContext#statesOf(OperationContext.State...)}</p>
   */
  void incrementSequence() {
    if (!sequenceUpdated) {
      setSequence(this.sequence + 1);
    }
  }

  @Override
  protected void performBeforeUpdate() {
    if (!OperationContext.statesOf(IMPORT)) {
      incrementSequence();
    }
    super.performBeforeUpdate();
  }

  /**
   * Is the properties of this calendar component was modified since its last specified state?
   * The attendees in this component aren't taken into account as they aren't considered as a
   * property of a calendar component.
   * @param previous a previous state of this calendar component.
   * @return true if the state of this calendar component is different with the specified one.
   */
  protected boolean isModifiedSince(final CalendarComponent previous) {
    if (!this.getTitle().equals(previous.getTitle())) {
      return true;
    }

    if (!this.getDescription().equals(previous.getDescription())) {
      return true;
    }

    if (!this.getLocation().equals(previous.getLocation())) {
      return true;
    }

    if (!this.getPeriod().equals(previous.getPeriod())) {
      return true;
    }

    if (this.getPriority() != previous.getPriority()) {
      return true;
    }

    return !this.getAttributes().equals(previous.getAttributes());
  }
}
