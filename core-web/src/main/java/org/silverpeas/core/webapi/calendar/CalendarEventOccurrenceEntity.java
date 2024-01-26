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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.kernel.util.StringUtil;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.time.ZoneId;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.calendar.CalendarEventUtil.formatDateWithOffset;
import static org.silverpeas.core.calendar.CalendarEventUtil.formatTitle;

/**
 * It represents the state of a calendar event in a calendar as transmitted within the
 * body of an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventOccurrenceEntity extends CalendarEventEntity {

  private URI occurrenceUri;
  private URI occurrenceViewUrl;
  private URI occurrenceEditionUrl;
  private URI occurrencePermalinkUrl;

  private String occurrenceId;
  private String originalStartDate;
  private boolean firstEventOccurrence;

  protected CalendarEventOccurrenceEntity() {
  }

  public static CalendarEventOccurrenceEntity fromOccurrence(
      final CalendarEventOccurrence occurrence, final String componentInstanceId,
      final ZoneId zoneId, final boolean isEditionMode) {
    return new CalendarEventOccurrenceEntity()
        .decorate(occurrence, componentInstanceId, zoneId, isEditionMode);
  }

  public static String decodeId(String occurrenceId) {
    return new String(StringUtil.fromBase64(occurrenceId));
  }

  /**
   * Sets a URI of the view page of the occurrence.
   * @param occurrenceViewUrl the occurrence web entity URI.
   * @return itself.
   */
  public CalendarEventOccurrenceEntity withOccurrenceViewURL(final URI occurrenceViewUrl) {
    setOccurrenceViewUrl(occurrenceViewUrl);
    return this;
  }

  /**
   * Sets a URI of the edition page of the occurrence.
   * @param occurrenceEditionUrl the occurrence web entity URI.
   * @return itself.
   */
  public CalendarEventOccurrenceEntity withOccurrenceEditionURL(final URI occurrenceEditionUrl) {
    setOccurrenceEditionUrl(occurrenceEditionUrl);
    return this;
  }

  /**
   * Sets a permalink URI to this entity. With this URI, it can then be accessed through the Web.
   * @param permalinkUrl the web entity URI.
   * @return itself.
   */
  public CalendarEventOccurrenceEntity withOccurrencePermalinkURL(final URI permalinkUrl) {
    this.occurrencePermalinkUrl = permalinkUrl;
    return this;
  }

  /**
   * Sets a URI of occurrence entity. With this URI, it can then be accessed through the Web.
   * @param occurrenceUri the occurrence web entity URI.
   * @return itself.
   */
  public CalendarEventOccurrenceEntity withOccurrenceURI(final URI occurrenceUri) {
    this.occurrenceUri = occurrenceUri;
    return this;
  }

  @Override
  public CalendarEventOccurrenceEntity withEventURI(final URI eventUri) {
    super.withEventURI(eventUri);
    return this;
  }

  @Override
  public CalendarEventOccurrenceEntity withCalendarURI(final URI calendarUri) {
    super.withCalendarURI(calendarUri);
    return this;
  }

  @Override
  public CalendarEventOccurrenceEntity withEventPermalinkURL(final URI permalinkUrl) {
    super.withEventPermalinkURL(permalinkUrl);
    return this;
  }

  /**
   * Sets attendees to the occurrence entity.
   * @param attendees the attendees to set.
   * @return itself.
   */
  @Override
  public CalendarEventOccurrenceEntity withAttendees(
      final List<CalendarEventAttendeeEntity> attendees) {
    super.withAttendees(attendees);
    return this;
  }

  /**
   * Sets attributes to the occurrence entity.
   * @param attributes the attributes to set.
   * @return itself.
   */
  @Override
  public CalendarEventOccurrenceEntity withAttributes(
      final List<CalendarEventAttributeEntity> attributes) {
    super.withAttributes(attributes);
    return this;
  }

  @Override
  @XmlElement(defaultValue = "")
  public URI getURI() {
    return getOccurrenceUri();
  }

  public URI getOccurrenceUri() {
    return occurrenceUri;
  }

  protected void setOccurrenceUri(final URI occurrenceUri) {
    this.occurrenceUri = occurrenceUri;
  }

  public URI getOccurrenceViewUrl() {
    return occurrenceViewUrl;
  }

  public void setOccurrenceViewUrl(final URI occurrenceViewUrl) {
    this.occurrenceViewUrl = occurrenceViewUrl;
  }

  public URI getOccurrenceEditionUrl() {
    return occurrenceEditionUrl;
  }

  public void setOccurrenceEditionUrl(final URI occurrenceEditionUrl) {
    this.occurrenceEditionUrl = occurrenceEditionUrl;
  }

  public URI getOccurrencePermalinkUrl() {
    return occurrencePermalinkUrl;
  }

  public void setOccurrencePermalinkUrl(final URI occurrencePermalinkUrl) {
    this.occurrencePermalinkUrl = occurrencePermalinkUrl;
  }

  public String getOccurrenceId() {
    return occurrenceId;
  }

  public void setOccurrenceId(final String occurrenceId) {
    this.occurrenceId = occurrenceId;
  }

  public String getOriginalStartDate() {
    return originalStartDate;
  }

  public void setOriginalStartDate(final String originalStartDate) {
    this.originalStartDate = originalStartDate;
  }

  public boolean isFirstEventOccurrence() {
    return firstEventOccurrence;
  }

  protected void setFirstEventOccurrence(final boolean firstEventOccurrence) {
    this.firstEventOccurrence = firstEventOccurrence;
  }

  /**
   * Gets the representation of an event with contains all the occurrence data.
   * The data of the entity are applied to the returned instance.
   * @return a {@link CalendarEvent} instance.
   */
  @XmlTransient
  CalendarEventOccurrence getMergedOccurrence() {
    final CalendarEventOccurrence occurrence =
        CalendarEventOccurrence.getById(decodeId(getOccurrenceId())).orElse(null);
    if (occurrence == null) {
      throw new WebApplicationException(NOT_FOUND);
    }
    applyOn(occurrence.getCalendarEvent());
    applyOn(occurrence.asCalendarComponent());
    return occurrence;
  }

  protected CalendarEventOccurrenceEntity decorate(
      final CalendarEventOccurrence calendarEventOccurrence, final String componentInstanceId,
      final ZoneId zoneId, final boolean isEditionMode) {
    final CalendarEvent calEvent = calendarEventOccurrence.getCalendarEvent();
    decorate(calEvent, calEvent.getCalendar().getComponentInstanceId(), zoneId, isEditionMode);
    setId(calendarEventOccurrence.getIdentifier().asString());
    setContributionType(calendarEventOccurrence.getContributionType());
    this.occurrenceId = StringUtil.asBase64(calendarEventOccurrence.getId().getBytes());
    this.originalStartDate = calendarEventOccurrence.getOriginalStartDate().toString();
    setFirstEventOccurrence(calendarEventOccurrence.getOriginalStartDate()
        .equals(calendarEventOccurrence.getCalendarEvent().getStartDate()));
    final CalendarComponent component = calendarEventOccurrence.asCalendarComponent();
    setOnAllDay(calendarEventOccurrence.isOnAllDay());
    setStartDate(formatDateWithOffset(component, calendarEventOccurrence.getStartDate(), zoneId));
    setEndDate(formatDateWithOffset(component, calendarEventOccurrence.getEndDate(), zoneId));
    if (component.getLastUpdateDate() != null) {
      setLastUpdateDate(component.getLastUpdateDate());
      setLastUpdatedById(component.getLastUpdaterId());
    }
    setTitle(formatTitle(component, componentInstanceId, canBeAccessed()));
    if (canBeAccessed()) {
      setDescription(component.getDescription());
      setLocation(component.getLocation());
      setPriority(component.getPriority());
    }
    return this;
  }

  @Override
  protected ToStringBuilder toStringBuilder() {
    ToStringBuilder builder = super.toStringBuilder();
    builder.append("occurrenceId", getOccurrenceId());
    builder.append("calendarZoneId", getCalendarZoneId());
    builder.append("originalStartDate", getOriginalStartDate());
    builder.append("firstEventOccurrence", isFirstEventOccurrence());
    return builder;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final CalendarEventOccurrenceEntity that = (CalendarEventOccurrenceEntity) o;

    return new EqualsBuilder().append(occurrenceId, that.occurrenceId).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(occurrenceId).toHashCode();
  }
}
