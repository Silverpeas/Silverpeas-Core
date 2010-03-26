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
package com.stratelia.silverpeas.notificationserver.channel.server;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class ServerMessageBean extends SilverpeasBean {

  public ServerMessageBean() {
  }

  private long userId = -1;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  private String body = "";

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    body = value;
  }

  private String sessionId = "";

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String value) {
    sessionId = value;
  }

  /*****************************************************************************/
  /**
   *
   */
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  /**
   *
   */
  public String _getTableName() {
    return "ST_ServerMessage";
  }

}