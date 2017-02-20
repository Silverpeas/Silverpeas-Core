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
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * A calendar component is a set of properties that express a common semantic for all objects in a
 * calendar. Those objects can be an event, a to-do, and so one and they share a common set of
 * properties that are defined in this class. This class is dedicated to be used in a transparent
 * way (by inheritance or by composition) by the more concrete representations of a calendar
 * component (like for example {@link CalendarEvent}).
 * @author mmoquillon
 */
@Entity
@Table(name = "sb_cal_components")
public class CalendarComponent extends SilverpeasJpaEntity<CalendarComponent, UuidIdentifier> {

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false)
  private Calendar calendar;

  @Embedded
  private Period period;

  @Column(name = "title", nullable = false)
  @Size(min = 1)
  @NotNull
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "location")
  private String location;

  @Column(name = "priority", nullable = false)
  @NotNull
  private Priority priority = Priority.NORMAL;

  @Embedded
  private Attributes attributes = new Attributes();

  @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true, fetch =
      FetchType.EAGER)
  private Set<Attendee> attendees = new HashSet<>();

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
  public CalendarComponent(final Period period) {
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
    return (this.location == null ? "" : this.location);
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
   * directly managed through the returned {@link Attributes} instance.
   * @return the additional attributes of this calendar component.
   */
  public Attributes getAttributes() {
    return this.attributes;
  }

  /**
   * Gets the attendees in this calendar component. An attendee can be either an internal user in
   * Silverpeas ({@link InternalAttendee}) or a user external from Silverpeas.
   * ({@link ExternalAttendee}). The creator of a calendar component isn't considered as an attendee
   * and hence he's not present among the attendees of in the component. If his participation has to
   * be explicit, then he should be added as a participant in the attendees in the calendar
   * component.
   * @return a set with the attendees in this calendar component.
   */
  public Set<Attendee> getAttendees() {
    return this.attendees;
  }

  @Override
  public CalendarComponent clone() {
    CalendarComponent clone = super.clone();
    clone.period = period.clone();
    clone.priority = priority;
    clone.attributes = attributes.clone();
    clone.location = this.location;
    clone.attendees = new HashSet<>();
    attendees.forEach(a -> a.cloneFor(clone));
    return clone;
  }

}
