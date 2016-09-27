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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.silverpeas.core.calendar.VisibilityLevel.PUBLIC;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * It represents the state of a calendar event in a calendar as transmitted within the
 * body of an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventEntity implements WebEntity {

  private URI uri;
  private URI calendarUri;

  private String id;
  private String title;
  private String description;
  private String location;
  private boolean onAllDay;
  private OffsetDateTime startDateTime;
  private OffsetDateTime endDateTime;
  private VisibilityLevel visibility;
  private Priority priority;
  private CalendarEventRecurrenceEntity recurrence;
  private List<CalendarEventAttendeeEntity> attendees = new ArrayList<>();
  private String ownerName;
  private Date createDate;
  private Date lastUpdateDate;
  private boolean canBeAccessed;
  private boolean canBeModified;
  private boolean canBeDeleted;

  protected CalendarEventEntity() {
  }

  public static CalendarEventEntity fromEvent(final CalendarEvent event,
      final String componentInstanceId) {
    return new CalendarEventEntity().decorate(event, componentInstanceId);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventEntity> T withURI(final URI uri) {
    this.uri = uri;
    return (T) this;
  }

  /**
   * Sets a URI to linked calendar entity. With this URI, it can then be accessed through the Web.
   * @param calendarUri the linked calendar web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventEntity> T withCalendarURI(final URI calendarUri) {
    this.calendarUri = calendarUri;
    return (T) this;
  }

  /**
   * Sets attendees to linked calendar entity.
   * @param attendees the linked calendar event attendees web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventEntity> T withAttendees(
      final List<CalendarEventAttendeeEntity> attendees) {
    this.attendees = attendees;
    return (T) this;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  protected void setURI(final URI uri) {
    withURI(uri);
  }

  public URI getCalendarUri() {
    return calendarUri;
  }

  protected void setCalendarUri(final URI calendarUri) {
    this.calendarUri = calendarUri;
  }

  public String getId() {
    return id;
  }

  protected void setId(final String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  protected void setTitle(final String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  protected void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  protected void setLocation(final String location) {
    this.location = location;
  }

  public boolean isOnAllDay() {
    return onAllDay;
  }

  protected void setOnAllDay(final boolean onAllDay) {
    this.onAllDay = onAllDay;
  }

  public String getStartDateTime() {
    return startDateTime.toString();
  }

  protected void setStartDateTime(final String startDateTime) {
    this.startDateTime = isDefined(startDateTime) ? OffsetDateTime.parse(startDateTime) : null;
  }

  public String getEndDateTime() {
    return endDateTime.toString();
  }

  protected void setEndDateTime(final String endDateTime) {
    this.endDateTime = isDefined(endDateTime) ? OffsetDateTime.parse(endDateTime) : null;
  }

  public VisibilityLevel getVisibility() {
    return visibility;
  }

  protected void setVisibility(final VisibilityLevel visibility) {
    this.visibility = visibility;
  }

  public Priority getPriority() {
    return priority;
  }

  protected void setPriority(final Priority priority) {
    this.priority = priority;
  }

  public CalendarEventRecurrenceEntity getRecurrence() {
    return recurrence;
  }

  public void setRecurrence(final CalendarEventRecurrenceEntity recurrence) {
    this.recurrence = recurrence;
  }

  public List<CalendarEventAttendeeEntity> getAttendees() {
    return attendees;
  }

  public void setAttendees(final List<CalendarEventAttendeeEntity> attendees) {
    withAttendees(attendees);
  }

  @XmlElement
  public String getOwnerName() {
    return ownerName;
  }

  @XmlElement
  public Date getCreateDate() {
    return createDate;
  }

  @XmlElement
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  public boolean isCanBeAccessed() {
    return canBeAccessed;
  }

  @XmlElement
  public boolean canBeModified() {
    return canBeModified;
  }

  @XmlElement
  public boolean canBeDeleted() {
    return canBeDeleted;
  }

  /**
   * Get the persistent data representation of an event merged with the entity data.
   * The data of the entity are applied to the returned instance.
   * @return a {@link CalendarEvent} instance.
   */
  @XmlTransient
  public CalendarEvent getMergedPersistentModel() {
    final CalendarEvent event;
    if (isDefined(getId())) {
      event = CalendarEvent.getById(getId());
    } else {
      final Period eventPeriod;
      if (isOnAllDay()) {
        eventPeriod = Period.allDaysBetween(startDateTime, endDateTime);
      } else {
        eventPeriod = Period.between(startDateTime, endDateTime);
      }
      event = CalendarEvent.on(eventPeriod);
    }
    event.withTitle(getTitle());
    event.withDescription(getDescription());
    event.setLocation(getLocation());
    event.withVisibilityLevel(getVisibility());
    event.withPriority(getPriority());
    if (getRecurrence() != null) {
      getRecurrence().applyOn(event);
    } else {
      event.unsetRecurrence();
    }
    List<String> newAttendeeIds = new ArrayList<>(getAttendees().size());
    for (CalendarEventAttendeeEntity attendeeEntity : getAttendees()) {
      attendeeEntity.addTo(event);
      newAttendeeIds.add(attendeeEntity.getId());
    }
    Iterator<Attendee> existingAttendees = event.getAttendees().iterator();
    while(existingAttendees.hasNext()) {
      Attendee attendee = existingAttendees.next();
      if (!newAttendeeIds.contains(attendee.getId())) {
        existingAttendees.remove();
      }
    }
    return event;
  }

  protected CalendarEventEntity decorate(final CalendarEvent calendarEvent,
      final String componentInstanceId) {
    boolean hasPrivateBehaviorToBeApplied =
        !componentInstanceId.equals(calendarEvent.getCalendar().getComponentInstanceId()) &&
            PUBLIC != calendarEvent.getVisibilityLevel();
    User currentUser = User.getCurrentRequester();
    id = calendarEvent.getId();
    onAllDay = calendarEvent.isOnAllDay();
    startDateTime = calendarEvent.getStartDateTime();
    endDateTime = calendarEvent.getEndDateTime();
    createDate = calendarEvent.getCreateDate();
    lastUpdateDate = calendarEvent.getLastUpdateDate();
    ownerName = calendarEvent.getCreator().getDisplayedName();
    canBeAccessed = calendarEvent.canBeAccessedBy(currentUser);
    if (canBeAccessed || !hasPrivateBehaviorToBeApplied) {
      title = calendarEvent.getTitle();
      description = calendarEvent.getDescription();
      location = calendarEvent.getLocation();
      visibility = calendarEvent.getVisibilityLevel();
      priority = calendarEvent.getPriority();
      recurrence = CalendarEventRecurrenceEntity.from(calendarEvent.getRecurrence());
      canBeModified = calendarEvent.canBeModifiedBy(currentUser);
      canBeDeleted = calendarEvent.canBeDeletedBy(currentUser);
    } else {
      canBeModified = false;
      canBeDeleted = false;
    }
    return this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("uri", getURI());
    builder.append("id", getId());
    builder.append("title", getTitle());
    builder.append("description", getDescription());
    builder.append("startDateTime", getStartDateTime());
    builder.append("endDateTime", getEndDateTime());
    builder.append("onAllDay", isOnAllDay());
    builder.append("visibility", getVisibility());
    builder.append("priority", getPriority());
    builder.append("recurrence", getRecurrence());
    builder.append("ownerName", getOwnerName());
    builder.append("createDate", getCreateDate());
    builder.append("lastUpdateDate", getLastUpdateDate());
    return builder.toString();
  }
}
