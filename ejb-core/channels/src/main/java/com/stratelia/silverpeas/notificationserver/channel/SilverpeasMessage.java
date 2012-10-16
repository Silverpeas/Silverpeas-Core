/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.notificationserver.channel;

/**
 * @author ehugonnet
 */
public class SilverpeasMessage {
  private long m_Id;

  public void setId(long value) {
    m_Id = value;
  }

  public long getId() {
    return m_Id;
  }

  private long userId = -1;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  private String m_UserLogin;

  public void setUserLogin(String value) {
    m_UserLogin = value;
  }

  public String getUserLogin() {
    return m_UserLogin;
  }

  private String m_SenderName;

  public void setSenderName(String value) {
    m_SenderName = value;
  }

  public String getSenderName() {
    return m_SenderName;
  }

  private String m_Subject;

  public void setSubject(String value) {
    m_Subject = value;
  }

  public String getSubject() {
    return m_Subject;
  }

  private String m_Body;

  public void setBody(String value) {
    m_Body = value;
  }

  public String getBody() {
    return m_Body;
  }
}
