/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.Attendee.ParticipationStatus;
import org.silverpeas.core.calendar.Attendee.PresenceStatus;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;

/**
 * It represents the state of a recurrence in a calendar event as transmitted within the
 * body of an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventAttendeeEntity implements WebEntity {

  private URI uri;

  private String id;
  private String fullName;
  private ParticipationStatus participationStatus;
  private PresenceStatus presenceStatus;

  protected CalendarEventAttendeeEntity() {
  }

  public static CalendarEventAttendeeEntity from(final Attendee attendee) {
    return new CalendarEventAttendeeEntity().decorate(attendee);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventAttendeeEntity> T withURI(final URI uri) {
    this.uri = uri;
    return (T) this;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public void setUri(final URI uri) {
    withURI(uri);
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(final String fullName) {
    this.fullName = fullName;
  }

  public ParticipationStatus getParticipationStatus() {
    return participationStatus;
  }

  public void setParticipationStatus(final ParticipationStatus participationStatus) {
    this.participationStatus = participationStatus;
  }

  public PresenceStatus getPresenceStatus() {
    return presenceStatus;
  }

  public void setPresenceStatus(final PresenceStatus presenceStatus) {
    this.presenceStatus = presenceStatus;
  }

  /**
   * Adds to the given event the current attendee if it is not already existing.<br>
   * If the attendee is already referenced by the event, only the presence status is updated.
   * @return a {@link CalendarEvent} instance.
   */
  @XmlTransient
  Attendee addTo(CalendarComponent component) {
    Attendee attendee = null;
    for (Attendee anAttendee : component.getAttendees()) {
      if (getId().equals(anAttendee.getId())) {
        attendee = anAttendee;
        break;
      }
    }
    if (attendee == null) {
      // Attendee has not been found from the existing set
      if (StringUtil.isLong(getId())) {
        attendee = component.getAttendees().add(User.getById(getId()));
      } else {
        attendee = component.getAttendees().add(getId());
      }
    }
    attendee.withPresenceStatus(getPresenceStatus());
    return attendee;
  }

  protected CalendarEventAttendeeEntity decorate(final Attendee attendee) {
    id = attendee.getId();
    fullName = attendee.getFullName();
    participationStatus = attendee.getParticipationStatus();
    presenceStatus = attendee.getPresenceStatus();
    return this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("uri", getURI());
    builder.append("attendeeId", getId());
    builder.append("fullName", getFullName());
    builder.append("participationStatus", getParticipationStatus());
    builder.append("presenceStatus", getPresenceStatus());
    return builder.toString();
  }
}
