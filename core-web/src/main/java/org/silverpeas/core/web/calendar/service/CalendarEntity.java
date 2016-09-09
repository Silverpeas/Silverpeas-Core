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
package org.silverpeas.core.web.calendar.service;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.Date;

/**
 * It represents the state of a calendar in a calendar as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CalendarEntity implements WebEntity {

  public static CalendarEntity fromCalendar(final Calendar calendar) {
    return new CalendarEntity().decorate(calendar)
        .withURI(CalendarResourceURIs.buildCalendarURI(calendar));
  }

  private URI uri;
  private Calendar calendar;

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  private CalendarEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  @XmlElement(defaultValue = "")
  public URI getURI() {
    return uri;
  }

  @XmlElement
  public String getId() {
    return calendar.getId();
  }

  @XmlElement
  public String getTitle() {
    return calendar.getTitle();
  }

  @XmlElement
  public String getOwnerName() {
    return calendar.getCreator().getDisplayedName();
  }

  @XmlElement
  public Date getCreateDate() {
    return calendar.getCreateDate();
  }

  @XmlElement
  public Date getLastUpdateDate() {
    return calendar.getLastUpdateDate();
  }

  @XmlTransient
  public User getCreator() {
    return calendar.getCreator();
  }

  protected void setURI(final URI uri) {
    withURI(uri);
  }

  protected void setTitle(String title) {
    calendar.setTitle(title);
  }

  protected CalendarEntity decorate(final Calendar calendar) {
    this.calendar = calendar;
    return this;
  }

  protected CalendarEntity() {
    this.calendar = new Calendar("");
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("uri", getURI());
    builder.append("id", getId());
    builder.append("title", getTitle());
    return builder.toString();
  }
}
