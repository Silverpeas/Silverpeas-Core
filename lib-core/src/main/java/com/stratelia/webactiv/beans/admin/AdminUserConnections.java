/*
 * @author Ludovic BERTIN	
 * @version 1.0
 * date 20/08/2001
 */

package com.stratelia.webactiv.beans.admin;

public class AdminUserConnections implements java.io.Serializable {
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