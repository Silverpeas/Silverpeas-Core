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
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.event.CalendarEventOccurrenceReferenceData;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * It represents the state of a calendar event in a calendar as transmitted within the
 * body of an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventOccurrenceEntity implements WebEntity {

  private String occurrenceId;
  private String lastStartDate;
  private CalendarEventEntity event;
  private String startDate;
  private String endDate;
  private List<CalendarEventAttendeeEntity> attendees = new ArrayList<>();

  protected CalendarEventOccurrenceEntity() {
  }

  public static CalendarEventOccurrenceEntity fromOccurrence(
      final CalendarEventOccurrence occurrence) {
    return new CalendarEventOccurrenceEntity().decorate(occurrence);
  }

  public CalendarEventOccurrenceEntity withEventEntity(CalendarEventEntity eventEntity) {
    this.event = eventEntity;
    return this;
  }

  /**
   * Sets attendees to the occurrence entity.
   * @param attendees the attendees entity to set.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventOccurrenceEntity> T withAttendees(
      final List<CalendarEventAttendeeEntity> attendees) {
    this.attendees = attendees;
    return (T) this;
  }

  @Override
  @XmlElement(defaultValue = "")
  public URI getURI() {
    return URI.create("");
  }

  public String getId() {
    return occurrenceId;
  }

  protected void setId(String occurrenceId) {
    this.occurrenceId = occurrenceId;
  }

  public String getLastStartDate() {
    return lastStartDate;
  }

  public void setLastStartDate(final String lastStartDate) {
    this.lastStartDate = lastStartDate;
  }

  public CalendarEventEntity getEvent() {
    return event;
  }

  protected void setEvent(final CalendarEventEntity event) {
    this.event = event;
  }

  public String getStartDate() {
    return startDate;
  }

  protected void setStartDate(final String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  protected void setEndDate(final String endDate) {
    this.endDate = endDate;
  }

  public List<CalendarEventAttendeeEntity> getAttendees() {
    return attendees;
  }

  public void setAttendees(final List<CalendarEventAttendeeEntity> attendees) {
    withAttendees(attendees);
  }

  @XmlElement
  public String getOwnerName() {
    return event.getOwnerName();
  }

  @XmlElement
  public Date getCreateDate() {
    return event.getCreateDate();
  }

  @XmlElement
  public Date getLastUpdateDate() {
    return event.getLastUpdateDate();
  }

  /**
   * Gets the period of the occurrence.
   * @return a period instance.
   */
  @XmlTransient
  Period getPeriod() {
    final Period occurrencePeriod;
    if (getEvent().isOnAllDay()) {
      occurrencePeriod = Period.between(LocalDate.parse(startDate), LocalDate.parse(endDate));
    } else {
      occurrencePeriod =
          Period.between(OffsetDateTime.parse(startDate), OffsetDateTime.parse(endDate));
    }
    return occurrencePeriod;
  }

  /**
   * Gets the reference data of the occurrence, previous start date time and new period.
   * @return an instance of {@link CalendarEventOccurrenceReferenceData}.
   */
  @XmlTransient
  CalendarEventOccurrenceReferenceData getReferenceData() {
    return CalendarEventOccurrenceReferenceData.fromOccurrenceId(getId()).withPeriod(getPeriod());
  }

  /**
   * Gets the representation of an event with contains all the occurrence data.
   * The data of the entity are applied to the returned instance.
   * @return a {@link CalendarEvent} instance.
   */
  @XmlTransient
  CalendarEvent getMergedPersistentEventModel() {
    return getEvent().getMergedPersistentModel(getPeriod());
  }

  protected CalendarEventOccurrenceEntity decorate(
      final CalendarEventOccurrence calendarEventOccurrence) {
    this.occurrenceId = calendarEventOccurrence.getId();
    this.lastStartDate = calendarEventOccurrence.getLastStartDate().toString();
    this.startDate = calendarEventOccurrence.getStartDate().toString();
    this.endDate = calendarEventOccurrence.getEndDate().toString();
    return this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("occurrenceId", getId());
    builder.append("event", getEvent().toString());
    builder.append("lastStartDate", getLastStartDate());
    builder.append("startDate", getStartDate());
    builder.append("endDate", getEndDate());
    return builder.toString();
  }
}
