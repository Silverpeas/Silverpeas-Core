package com.stratelia.silverpeas.notificationserver.channel.server;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class ServerMessageBean extends SilverpeasBean
{

  public ServerMessageBean()
  {
  }

  private long userId = -1;
  public long getUserId()
  {
    return userId;
  }
  public void setUserId(long value)
  {
    userId = value;
  }

  private String body = "";
  public String getBody()
  {
    return body;
  }
  public void setBody(String value)
  {
    body = value;
  }

  private String sessionId = "";
  public String getSessionId()
  {
    return sessionId;
  }
  public void setSessionId(String value)
  {
    sessionId = value;
  }

/*****************************************************************************/
  /**
   *
   */
  public int _getConnectionType()
  {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  /**
   *
   */
  public String _getTableName()
  {
    return "ST_ServerMessage";
  }

}