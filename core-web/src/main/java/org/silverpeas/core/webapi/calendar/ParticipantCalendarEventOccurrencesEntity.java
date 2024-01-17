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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * It represents the occurrences of a participant.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParticipantCalendarEventOccurrencesEntity {

  private String id;
  private String userId;
  private String title;
  private List<CalendarEventOccurrenceEntity> occurrences;
  private boolean canBeModified;
  private boolean canBeDeleted;

  public static ParticipantCalendarEventOccurrencesEntity from(final User user) {
    return new ParticipantCalendarEventOccurrencesEntity().decorate(user);
  }

  @SuppressWarnings("unchecked")
  public <T extends ParticipantCalendarEventOccurrencesEntity> T withOccurrences(
      final List<CalendarEventOccurrenceEntity> occurrences) {
    this.occurrences = occurrences;
    return (T) this;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }

  public List<CalendarEventOccurrenceEntity> getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(final List<CalendarEventOccurrenceEntity> occurrences) {
    this.occurrences = occurrences;
  }

  @XmlElement
  public boolean canBeModified() {
    return canBeModified;
  }

  @XmlElement
  public boolean canBeDeleted() {
    return canBeDeleted;
  }

  protected ParticipantCalendarEventOccurrencesEntity() {
  }

  @SuppressWarnings("unchecked")
  protected <T extends ParticipantCalendarEventOccurrencesEntity> T decorate(final User user) {
    this.userId = user.getId();
    this.id = "participation-calendar-user-" + user.getId();
    this.title = user.getDisplayedName();
    this.canBeModified = false;
    this.canBeDeleted = false;
    return (T) this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("id", getId());
    builder.append("title", getTitle());
    builder.append("occurrences", getOccurrences());
    return builder.toString();
  }
}
