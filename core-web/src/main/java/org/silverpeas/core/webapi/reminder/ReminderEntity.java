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

package org.silverpeas.core.webapi.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.reminder.DateTimeReminder;
import org.silverpeas.core.reminder.DurationReminder;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.time.OffsetDateTime;

/**
 * It represents a reminder as transmitted within the body of an HTTP response or an HTTP request.
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderEntity implements WebEntity {
  private static final long serialVersionUID = -6481988671227188535L;

  private URI uri;
  private String id;
  private String cId;
  private String cProperty;
  private String userId;
  private String processName;
  private String dateTime;
  private Integer duration;
  private TimeUnit timeUnit;
  private String text;
  private boolean canBeModified;
  private boolean canBeDeleted;

  protected ReminderEntity() {
  }

  public static ReminderEntity fromReminder(final Reminder reminder) {
    return new ReminderEntity().decorate(reminder);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public ReminderEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  protected void setUri(final URI uri) {
    withURI(uri);
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getcId() {
    return cId;
  }

  public void setcId(final String cId) {
    this.cId = cId;
  }

  public String getcProperty() {
    return cProperty;
  }

  public void setcProperty(final String cProperty) {
    this.cProperty = cProperty;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(final String processName) {
    this.processName = processName;
  }

  public String getDateTime() {
    return dateTime;
  }

  public void setDateTime(final String dateTime) {
    this.dateTime = dateTime;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(final Integer duration) {
    this.duration = duration;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public void setTimeUnit(final TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  public String getText() {
    return text;
  }

  public void setText(final String text) {
    this.text = text;
  }

  @XmlElement
  public boolean canBeModified() {
    return canBeModified;
  }

  @XmlElement
  public boolean canBeDeleted() {
    return canBeDeleted;
  }

  @SuppressWarnings("unchecked")
  protected <T extends ReminderEntity> T decorate(final Reminder reminder) {
    this.id = reminder.getId();
    this.cId = reminder.getContributionId().asString();
    this.userId = reminder.getUserId();
    this.processName = reminder.getProcessName();
    if (reminder instanceof DateTimeReminder) {
      final DateTimeReminder dateTimeReminder = (DateTimeReminder) reminder;
      this.dateTime = dateTimeReminder.getDateTime().toString();
    } else {
      final DurationReminder durationReminder = (DurationReminder) reminder;
      this.cProperty = durationReminder.getContributionProperty();
      this.duration = durationReminder.getDuration();
      this.timeUnit = durationReminder.getTimeUnit();
    }
    this.text = reminder.getText();
    this.canBeModified = true;
    this.canBeDeleted = true;
    return (T) this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("uri", getURI());
    builder.append("id", getId());
    builder.append("contributionId", getcId());
    builder.append("contributionProperty", getcProperty());
    builder.append("userId", getUserId());
    builder.append("processName", getProcessName());
    builder.append("dateTime", getDateTime());
    builder.append("duration", getDuration());
    builder.append("timeUnit", getTimeUnit());
    builder.append("text", getText());
    return builder.toString();
  }

  /**
   * Merges into given reminder instance the data from the entity.<br>
   * System data are not merged (id, creation date, update date, ...)
   * @param reminder the reminder which will get the new data.
   * @return the given reminder instance with merged data.
   * @throw javax.ws.javax.ws.rs.WebApplicationException if given reminder does not exist.
   */
  public Reminder mergeInto(Reminder reminder) {
    reminder.withText(getText());
    if (reminder instanceof DateTimeReminder) {
      final DateTimeReminder dateTimeReminder = (DateTimeReminder) reminder;
      dateTimeReminder.triggerAt(OffsetDateTime.parse(getDateTime()));
    } else {
      final DurationReminder durationReminder = (DurationReminder) reminder;
      durationReminder.triggerBefore(getDuration(), getTimeUnit(), getcProperty());
    }
    return reminder;
  }
}
