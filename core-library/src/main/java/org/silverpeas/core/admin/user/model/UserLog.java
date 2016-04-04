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

/*
 * @author Ludovic BERTIN
 * @version 1.0
 * date 10/08/2001
 */
package org.silverpeas.core.admin.user.model;

import java.util.Date;

public class UserLog {
  private String m_sSessionId = ""; // Client session id (http)
  private String m_sUserId = ""; // User Id (silverpeas)
  private String m_sUserLogin = ""; // User login (silverpeas)
  private Date m_LogDate = null; // Log date

  public UserLog() {
  }

  public void setSessionId(String sSessionId) {
    m_sSessionId = sSessionId;
  }

  public String getSessionId() {
    return m_sSessionId;
  }

  public void setUserId(String sUserId) {
    m_sUserId = sUserId;
  }

  public String getUserId() {
    return m_sUserId;
  }

  public void setUserLogin(String sUserLogin) {
    m_sUserLogin = sUserLogin;
  }

  public String getUserLogin() {
    return m_sUserLogin;
  }

  public void setLogDate(Date date) {
    m_LogDate = date;
  }

  public Date getLogDate() {
    return m_LogDate;
  }
}