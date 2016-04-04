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

package org.silverpeas.core.notification.user.client.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class SentNotificationDetail implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -4746255546630667116L;
  private int notifId;
  private int userId;
  private int messageType;
  private Date notifDate;
  private String title;
  private String source;
  private String link;
  private String sessionId;
  private String componentId;
  private String body;
  private List<String> users;

  public SentNotificationDetail() {
  }

  public SentNotificationDetail(int userId, int messageType, Date notifDate, String title,
      String source, String link, String sessionId, String componentId, String body) {
    this.userId = userId;
    this.messageType = messageType;
    this.notifDate = notifDate;
    this.title = title;
    this.source = source;
    this.link = link;
    this.sessionId = sessionId;
    this.componentId = componentId;
    this.body = body;
  }

  public int getNotifId() {
    return notifId;
  }

  public void setNotifId(int notifId) {
    this.notifId = notifId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

  public Date getNotifDate() {
    return notifDate;
  }

  public void setNotifDate(Date notifDate) {
    this.notifDate = notifDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }

}