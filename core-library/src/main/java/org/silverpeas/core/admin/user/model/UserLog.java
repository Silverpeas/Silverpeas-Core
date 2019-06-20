/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
/*
 * @author Ludovic BERTIN
 * @version 1.0
 * date 10/08/2001
 */
package org.silverpeas.core.admin.user.model;

import java.util.Date;

public class UserLog {
  private String sessionId = ""; // Client session id (http)
  private String userId = ""; // User Id (silverpeas)
  private String userLogin = ""; // User login (silverpeas)
  private Date logDate = null; // Log date

  public void setSessionId(String sSessionId) {
    sessionId = sSessionId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setUserId(String sUserId) {
    userId = sUserId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserLogin(String sUserLogin) {
    userLogin = sUserLogin;
  }

  public String getUserLogin() {
    return userLogin;
  }

  public void setLogDate(Date date) {
    logDate = date;
  }

  public Date getLogDate() {
    return logDate;
  }
}