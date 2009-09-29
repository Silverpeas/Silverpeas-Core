/*
 * @author Norbert CHAIX
 * @version 1.0
  date 01/09/2000
 */

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class UserDetail extends Object implements Serializable, Comparable {
  private String m_sId = null;
  private String m_sSpecificId = null;
  private String m_sDomainId = null;
  private String m_sLogin = null;
  private String m_sFirstName = "";
  private String m_sLastName = "";
  private String m_seMail = "";
  private String m_sAccessLevel = "";

  /**
   * Constructor
   */
  public UserDetail() {
  }

  public UserDetail(UserDetail toClone) {
    m_sId = toClone.m_sId;
    m_sSpecificId = toClone.m_sSpecificId;
    m_sDomainId = toClone.m_sDomainId;
    m_sLogin = toClone.m_sLogin;
    m_sFirstName = toClone.m_sFirstName;
    m_sLastName = toClone.m_sLastName;
    m_seMail = toClone.m_seMail;
    m_sAccessLevel = toClone.m_sAccessLevel;
  }

  /**
   * Get user id as stored in database
   */
  public String getId() {
    return m_sId;
  }

  /**
   * Set user id
   */
  public void setId(String sId) {
    m_sId = sId;
  }

  /**
   * Get specific user id
   */
  public String getSpecificId() {
    return m_sSpecificId;
  }

  /**
   * Set specific user id
   */
  public void setSpecificId(String sSpecificId) {
    m_sSpecificId = sSpecificId;
  }

  /**
   * Get user domain id
   */
  public String getDomainId() {
    return m_sDomainId;
  }

  /**
   * Set user domain id
   */
  public void setDomainId(String sDomainId) {
    m_sDomainId = sDomainId;
  }

  /**
   * Get user login
   */
  public String getLogin() {
    return m_sLogin;
  }

  /**
   * Set user login
   */
  public void setLogin(String sLogin) {
    if (sLogin != null) {
      m_sLogin = sLogin;
    } else {
      m_sLogin = "";
    }
  }

  /**
   * Get user's first name
   */
  public String getFirstName() {
    return m_sFirstName;
  }

  /**
   * Set user first name
   */
  public void setFirstName(String sFirstName) {
    if (sFirstName != null) {
      m_sFirstName = sFirstName;
    } else {
      m_sFirstName = "";
    }
  }

  /**
   * Get user's last name
   */
  public String getLastName() {
    return m_sLastName;
  }

  /**
   * Set user last name
   */
  public void setLastName(String sLastName) {
    if (sLastName != null) {
      m_sLastName = sLastName;
    } else {
      m_sLastName = "";
    }
  }

  /**
   * Set user's email
   */
  public void seteMail(String seMail) {
    if (seMail != null)
      m_seMail = seMail;
    else
      m_seMail = "";
  }

  /**
   * Get user's email
   */
  public String geteMail() {
    return m_seMail;
  }

  /**
   * Get user's access level
   */
  public String getAccessLevel() {
    return m_sAccessLevel;
  }

  /**
   * Set user access level
   */
  public void setAccessLevel(String sAccessLevel) {
    if (sAccessLevel != null) {
      m_sAccessLevel = sAccessLevel.trim();
    } else {
      m_sAccessLevel = "U";
    }

  }

  public boolean isDomainAdminRestricted() {
    return ((GeneralPropertiesManager.getDomainVisibility() != GeneralPropertiesManager.DVIS_ALL)
        && (!isAccessAdmin()) && ((GeneralPropertiesManager
        .getDomainVisibility() != GeneralPropertiesManager.DVIS_ONE) || (!getDomainId()
        .equals("0"))));
  }

  public boolean isBackOfficeVisible() {
    return (isAccessKMManager() || isAccessAdmin() || isAccessDomainManager());
  }

  public boolean isAccessAdmin() {
    return "A".equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessDomainManager() {
    return "D".equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessUser() {
    return "U".equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessRemoved() {
    return "R".equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessGuest() {
    return "G".equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessKMManager() {
    return "K".equalsIgnoreCase(m_sAccessLevel);
  }

  public String getDisplayedName() {
    String valret = "";

    if (getFirstName() != null) {
      valret = getFirstName() + " ";
    }
    if (getLastName() != null) {
      valret += getLastName();
    }
    return valret;
  }

  public boolean equals(UserDetail cmpUser) {
    if (isEqualStrings(m_sId, cmpUser.m_sId) == false)
      return false;
    if (isEqualStrings(m_sSpecificId, cmpUser.m_sSpecificId) == false)
      return false;
    if (isEqualStrings(m_sDomainId, cmpUser.m_sDomainId) == false)
      return false;
    if (isEqualStrings(m_sLogin, cmpUser.m_sLogin) == false)
      return false;
    if (isEqualStrings(m_sFirstName, cmpUser.m_sFirstName) == false)
      return false;
    if (isEqualStrings(m_sLastName, cmpUser.m_sLastName) == false)
      return false;
    if (isEqualStrings(m_seMail, cmpUser.m_seMail) == false)
      return false;
    if (isEqualStrings(m_sAccessLevel, cmpUser.m_sAccessLevel) == false)
      return false;
    return true;
  }

  protected boolean isEqualStrings(String s1, String s2) {
    if ((s1 == null) && (s2 == null)) {
      return true;
    }
    if ((s1 == null) || (s2 == null)) {
      return false;
    }
    return s1.equals(s2);
  }

  /**
   * Dump user values to the trace system
   */
  public void traceUser() {
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "Id : " + m_sId);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "SpecificId : " + m_sSpecificId);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "DomainId : " + m_sDomainId);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "Login : " + m_sLogin);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "FirstName : " + m_sFirstName);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "LastName : " + m_sLastName);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "eMail : " + m_seMail);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER",
        "AccessLevel : " + m_sAccessLevel);
  }

  public int compareTo(Object o) {
    UserDetail other = (UserDetail) o;
    return ((getLastName() + getFirstName()).toLowerCase()).compareTo((other
        .getLastName() + other.getFirstName()).toLowerCase());
  }

}