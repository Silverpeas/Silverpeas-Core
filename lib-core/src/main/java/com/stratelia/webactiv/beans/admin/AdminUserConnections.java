/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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
 * date 20/08/2001
 */

package com.stratelia.webactiv.beans.admin;

public class AdminUserConnections implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private String m_sUserId = ""; // User Id (silverpeas)
  private String m_sSessionId = ""; // Session id

  public AdminUserConnections() {
  }

  public AdminUserConnections(String sUserId, String sSessionId) {
    m_sUserId = sUserId;
    m_sSessionId = sSessionId;
  }

  public void setUserId(String sUserId) {
    m_sUserId = sUserId;
  }

  public String getUserId() {
    return m_sUserId;
  }

  public void setSessionId(String sSessionId) {
    m_sSessionId = sSessionId;
  }

  public String getSessionId() {
    return m_sSessionId;
  }
}