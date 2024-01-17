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
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.rs.WebEntity;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.Date;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * It represents the state of a calendar in a calendar as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEntity implements WebEntity {

  private URI uri;
  @XmlElement
  private URI icalPublicUri;
  @XmlElement
  private URI icalPrivateUri;
  private String id;
  private String title;
  private String zoneId;
  private URI externalUrl;
  private boolean main;
  private boolean userMainPersonal;
  private boolean userPersonal;
  private String ownerName;
  private Date createDate;
  private Date lastUpdateDate;
  private boolean canBeModified;
  private boolean canBeDeleted;

  protected CalendarEntity() {
  }

  public static CalendarEntity fromCalendar(final Calendar calendar) {
    return new CalendarEntity().decorate(calendar);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public CalendarEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets a ical public URI to this entity.
   * @param uri the web entity URI.
   * @return itself.
   */
  public CalendarEntity withICalPublicURI(final URI uri) {
    this.icalPublicUri = uri;
    return this;
  }

  /**
   * Sets a ical public URI to this entity.
   * @param uri the web entity URI.
   * @return itself.
   */
  public CalendarEntity withICalPrivateURI(final URI uri) {
    this.icalPrivateUri = uri;
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

  public String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }

  public String getZoneId() {
    return zoneId;
  }

  public void setZoneId(final String zoneId) {
    this.zoneId = zoneId;
  }

  public URI getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(final URI externalUrl) {
    this.externalUrl = externalUrl;
  }

  @XmlElement
  public boolean isMain() {
    return main;
  }

  @XmlElement
  public boolean isUserMainPersonal() {
    return userMainPersonal;
  }

  @XmlElement
  public boolean isUserPersonal() {
    return userPersonal;
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

  @XmlElement
  public boolean canBeModified() {
    return canBeModified;
  }

  @XmlElement
  public boolean canBeDeleted() {
    return canBeDeleted;
  }

  @SuppressWarnings("unchecked")
  protected <T extends CalendarEntity> T decorate(final Calendar calendar) {
    User currentUser = User.getCurrentRequester();
    this.id = calendar.getId();
    this.title = calendar.getTitle();
    this.zoneId = calendar.getZoneId().toString();
    try {
      this.externalUrl =
          calendar.getExternalCalendarUrl() != null ? calendar.getExternalCalendarUrl().toURI() :
              null;
    } catch (URISyntaxException e) {
      SilverLogger.getLogger(this).warn(e);
    }
    this.main = calendar.isMain();
    this.userMainPersonal = calendar.isMainPersonalOf(currentUser);
    this.userPersonal = calendar.isPersonalOf(currentUser);
    this.ownerName = calendar.getCreator().getDisplayedName();
    this.createDate = calendar.getCreationDate();
    this.lastUpdateDate = calendar.getLastUpdateDate();
    this.canBeModified = calendar.canBeModifiedBy(currentUser);
    this.canBeDeleted= calendar.canBeDeletedBy(currentUser);
    return (T) this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("uri", getURI());
    builder.append("id", getId());
    builder.append("title", getTitle());
    builder.append("zoneId", getZoneId());
    builder.append("externalUrl", getExternalUrl());
    return builder.toString();
  }

  /**
   * Merges into given calendar instance the data from the entity.<br>
   * System data are not merged (id, creation date, update date, ...)
   * @param calendar the calendar which will get the new data.
   * @return the given calendar instance with merged data.
   * @throw javax.ws.javax.ws.rs.WebApplicationException if given calendar does not exist.
   */
  public Calendar merge(Calendar calendar) {
    calendar.setTitle(getTitle());
    if (getExternalUrl() != null) {
      try {
        calendar.setExternalCalendarUrl(getExternalUrl().toURL());
      } catch (MalformedURLException e) {
        throw new WebApplicationException(e);
      }
    }
    if (isDefined(getZoneId())) {
      calendar.setZoneId(ZoneId.of(getZoneId()));
    } else {
      throw new WebApplicationException("zoneId must exist into calendar attributes",
          Response.Status.NOT_ACCEPTABLE);
    }
    return calendar;
  }
}
