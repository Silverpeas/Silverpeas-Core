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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.socialNetwork.status.StatusService;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import static com.silverpeas.util.StringUtil.*;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class UserDetail implements Serializable, Comparable<UserDetail> {

  private static final String ANONYMOUS_ID_PROPERTY = "anonymousId";
  public static final String ADMIN_ACCESS = "A";
  public static final String USER_ACCESS = "U";
  public static final String REMOVED_ACCESS = "R";
  public static final String GUEST_ACCESS = "G";
  public static final String KM_ACCESS = "K";
  public static final String DOMAIN_ACCESS = "D";
  private static final String AVATAR_PROPERTY = GeneralPropertiesManager.getString("avatar.property", "login");
  private static final String AVATAR_EXTENSION = GeneralPropertiesManager.getString("avatar.extension", "jpg");
  private static final ResourceLocator generalSettings = new ResourceLocator(
      "com.stratelia.silverpeas.lookAndFeel.generalLook", "");
  private static final long serialVersionUID = -109886153681824159L;
  private String m_sId = null;
  private String m_sSpecificId = null;
  private String m_sDomainId = null;
  private String m_sLogin = null;
  private String m_sFirstName = "";
  private String m_sLastName = "";
  private String m_seMail = "";
  private String m_sAccessLevel = "";
  private String m_sLoginQuestion = "";
  private String m_sLoginAnswer = "";

  /**
   * Constructor
   */
  public UserDetail() {
  }

  public UserDetail(UserDetail toClone) {
    m_sId = toClone.getId();
    m_sSpecificId = toClone.getSpecificId();
    m_sDomainId = toClone.getDomainId();
    m_sLogin = toClone.getLogin();
    m_sFirstName = toClone.getFirstName();
    m_sLastName = toClone.getLastName();
    m_seMail = toClone.geteMail();
    m_sAccessLevel = toClone.getAccessLevel();
    m_sLoginQuestion = toClone.getLoginQuestion();
    m_sLoginAnswer = toClone.getLoginAnswer();
  }

  public String getLoginQuestion() {
    return m_sLoginQuestion;
  }

  public void setLoginQuestion(String mSLoginQuestion) {
    m_sLoginQuestion = mSLoginQuestion;
  }

  public String getLoginAnswer() {
    return m_sLoginAnswer;
  }

  public void setLoginAnswer(String mSLoginAnswer) {
    m_sLoginAnswer = mSLoginAnswer;
  }

  /**
   * Get user id as stored in database
   * @return
   */
  public String getId() {
    return m_sId;
  }

  /**
   * Set user id
   * @param sId
   */
  public void setId(String sId) {
    m_sId = sId;
  }

  /**
   * Get specific user id
   * @return
   */
  public String getSpecificId() {
    return m_sSpecificId;
  }

  /**
   * Set specific user id
   * @param sSpecificId
   */
  public void setSpecificId(String sSpecificId) {
    m_sSpecificId = sSpecificId;
  }

  /**
   * Get user's domain id
   * @return user's domain id
   */
  public String getDomainId() {
    return m_sDomainId;
  }

  /**
   * Set user domain id
   * @param sDomainId
   */
  public void setDomainId(String sDomainId) {
    m_sDomainId = sDomainId;
  }

  /**
   * Get user's login
   * @return user's login
   */
  public String getLogin() {
    return m_sLogin;
  }

  /**
   * Set user login
   * @param sLogin
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
   * @return user's first name
   */
  public String getFirstName() {
    return m_sFirstName;
  }

  /**
   * Set user first name
   * @param sFirstName user first name
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
   * @return user's last name
   */
  public String getLastName() {
    return m_sLastName;
  }

  /**
   * Set user last name
   * @param sLastName user last name
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
   * @param seMail
   */
  public void seteMail(String seMail) {
    if (seMail != null) {
      m_seMail = seMail;
    } else {
      m_seMail = "";
    }
  }

  /**
   * Get user's email
   * @return
   */
  public String geteMail() {
    return m_seMail;
  }

  /**
   * Get user's access level
   * @return
   */
  public String getAccessLevel() {
    return m_sAccessLevel;
  }

  /**
   * Set user access level
   * @param sAccessLevel
   */
  public void setAccessLevel(String sAccessLevel) {
    if (sAccessLevel != null) {
      m_sAccessLevel = sAccessLevel.trim();
    } else {
      m_sAccessLevel = USER_ACCESS;
    }

  }

  public boolean isDomainAdminRestricted() {
    return ((GeneralPropertiesManager.getDomainVisibility() != GeneralPropertiesManager.DVIS_ALL)
        && (!isAccessAdmin()) && ((GeneralPropertiesManager.getDomainVisibility() != GeneralPropertiesManager.DVIS_ONE) || (!"0"
        .equals(getDomainId()))));
  }

  public boolean isBackOfficeVisible() {
    return (isAccessKMManager() || isAccessAdmin() || isAccessDomainManager());
  }

  public boolean isAccessAdmin() {
    return ADMIN_ACCESS.equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessDomainManager() {
    return DOMAIN_ACCESS.equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessUser() {
    return USER_ACCESS.equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessRemoved() {
    return REMOVED_ACCESS.equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessGuest() {
    return GUEST_ACCESS.equalsIgnoreCase(m_sAccessLevel);
  }

  public boolean isAccessKMManager() {
    return KM_ACCESS.equalsIgnoreCase(m_sAccessLevel);
  }

  /**
   * Is the user is the anonymous one?
   * @return true if he's the anonymous user.
   */
  public boolean isAnonymous() {
    return getId().equals(getAnonymousUserId());
  }

  /**
   * Gets the anonymous user or null if no such user exists.
   * @return the detail about the anonymous user or null if no such user exists.
   */
  public static UserDetail getAnonymousUser() {
    UserDetail anonymousUser = null;
    if(isAnonymousUserExist()) {
      OrganizationController organizationController = new OrganizationController();
      anonymousUser = organizationController.getUserDetail(getAnonymousUserId());
    }
    return anonymousUser;
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

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserDetail) {
      UserDetail cmpUser = (UserDetail)other;
      return areStringEquals(m_sId, cmpUser.getId())
        && areStringEquals(m_sSpecificId, cmpUser.getSpecificId())
        && areStringEquals(m_sDomainId, cmpUser.getDomainId())
        && areStringEquals(m_sLogin, cmpUser.getLogin())
        && areStringEquals(m_sFirstName, cmpUser.getFirstName())
        && areStringEquals(m_sLastName, cmpUser.getLastName())
        && areStringEquals(m_seMail, cmpUser.geteMail())
        && areStringEquals(m_sAccessLevel, cmpUser.getAccessLevel());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + (this.m_sId != null ? this.m_sId.hashCode() : 0);
    hash = 41 * hash + (this.m_sSpecificId != null ? this.m_sSpecificId.hashCode() : 0);
    hash = 41 * hash + (this.m_sDomainId != null ? this.m_sDomainId.hashCode() : 0);
    hash = 41 * hash + (this.m_sLogin != null ? this.m_sLogin.hashCode() : 0);
    hash = 41 * hash + (this.m_sFirstName != null ? this.m_sFirstName.hashCode() : 0);
    hash = 41 * hash + (this.m_sLastName != null ? this.m_sLastName.hashCode() : 0);
    hash = 41 * hash + (this.m_seMail != null ? this.m_seMail.hashCode() : 0);
    hash = 41 * hash + (this.m_sAccessLevel != null ? this.m_sAccessLevel.hashCode() : 0);
    return hash;
  }


  /**
   * Dump user values to the trace system
   */
  public void traceUser() {
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "Id : " + m_sId);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "SpecificId : " + m_sSpecificId);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "DomainId : " + m_sDomainId);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "Login : " + m_sLogin);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "FirstName : " + m_sFirstName);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "LastName : " + m_sLastName);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "eMail : " + m_seMail);
    SilverTrace.info("admin", "UserDetail.traceUser",
        "admin.MSG_DUMP_USER", "AccessLevel : " + m_sAccessLevel);
  }

  @Override
  public int compareTo(UserDetail o) {
    UserDetail other = o;
    return ((getLastName() + getFirstName()).toLowerCase()).compareTo((other.getLastName() + other.
        getFirstName()).toLowerCase());
  }

  public String getAvatar() {
    String avatar = getAvatarFileName();
    File image = new File(FileRepositoryManager.getAbsolutePath("avatar")
        + File.separatorChar + avatar);
    if (image.exists()) {
      return "/display/avatar/" + avatar;
    }
    return "/directory/jsp/icons/avatar.png";
  }

  public String getAvatarFileName() {
    String propertyValue = getLogin();
    try {
      propertyValue = BeanUtils.getSimpleProperty(this, AVATAR_PROPERTY);
    } catch (Exception e) {
      SilverTrace.debug("admin", "UserDetail.getAvatarFileName", "admin.MSG_GET_PROPERTY", e);
    }
    return propertyValue + "." + AVATAR_EXTENSION;
  }

  public String getStatus() {
    String status = new StatusService().getLastStatusService(Integer.parseInt(getId())).getDescription();
    if (isDefined(status)) {
      return status;
    }
    return "";
  }

  /**
   * Is the anonymous user exist in this running Silverpeas application?
   * @return true if the anonymous user exist, false otherwise.
   */
  public static boolean isAnonymousUserExist() {
    return isDefined(getAnonymousUserId());
  }

  /**
   * Is the specified user is the anonymous one?
   * @param userId the identifier of the user.
   * @return true if the specified user is the anonymous one, false otherwise.
   */
  public static boolean isAnonymousUser(String userId) {
    return isAnonymousUserExist() && getAnonymousUserId().equals(userId);
  }

  /**
   * Gets the unique identifier of the anonymous user as set in the general look properties.
   * @return the anonymous user identifier.
   */
  protected static String getAnonymousUserId() {
    return generalSettings.getString(ANONYMOUS_ID_PROPERTY, null);
  }
}