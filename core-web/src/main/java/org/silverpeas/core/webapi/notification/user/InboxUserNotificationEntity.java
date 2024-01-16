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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.notification.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

import static org.silverpeas.core.util.DateUtil.toLocalDate;

/**
 * It represents the state of a calendar in a calendar as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboxUserNotificationEntity implements WebEntity {
  private static final long serialVersionUID = 4872081532099078450L;

  private URI uri;
  private URI markAsReadUri;
  private long id;
  private String source;
  private String subject;
  private String senderName;
  private String date;
  private URI resourceViewUrl;
  private String content;
  private boolean read;

  protected InboxUserNotificationEntity() {
  }

  public static InboxUserNotificationEntity from(final SILVERMAILMessage notification) {
    return new InboxUserNotificationEntity().decorate(notification);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public InboxUserNotificationEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public InboxUserNotificationEntity withMarkAsReadURI(final URI uri) {
    this.markAsReadUri = uri;
    return this;
  }

  @XmlElement
  @Override
  public URI getURI() {
    return uri;
  }

  @XmlElement
  public URI getMarkAsReadURI() {
    return markAsReadUri;
  }

  @XmlElement
  public long getId() {
    return id;
  }

  @XmlElement
  public String getSource() {
    return source;
  }

  @XmlElement
  public String getSubject() {
    return subject;
  }

  @XmlElement
  public String getSenderName() {
    return senderName;
  }

  @XmlElement
  public String getDate() {
    return date;
  }

  @XmlElement
  public URI getResourceViewUrl() {
    return resourceViewUrl;
  }

  @XmlElement
  public String getContent() {
    return content;
  }

  @XmlElement
  public boolean isRead() {
    return read;
  }

  protected InboxUserNotificationEntity decorate(final SILVERMAILMessage notification) {
    this.id = notification.getId();
    this.source = notification.getSource();
    this.subject = notification.getSubject();
    this.senderName = notification.getSenderName();
    this.date = toLocalDate(notification.getDate()).toString();
    try {
      this.resourceViewUrl = UriBuilder.fromUri(notification.getUrl()).build();
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    this.content = notification.getBody();
    this.read = notification.getReaden() > 0;
    return this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("uri", getURI());
    builder.append("id", getId());
    builder.append("source", getSource());
    builder.append("subject", getSubject());
    builder.append("senderName", getSenderName());
    builder.append("date", getDate());
    builder.append("resourceViewUrl", getResourceViewUrl());
    builder.append("content", getContent());
    builder.append("read", isRead());
    return builder.toString();
  }
}
