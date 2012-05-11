/**
* Copyright (C) 2000 - 2011 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection with Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.socialnetwork.status.StatusService;
import static com.silverpeas.util.StringUtil.areStringEquals;
import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;

public class UserDetail implements Serializable, Comparable<UserDetail> {
  private static final long serialVersionUID = -109886153681824159L;

  private static final String ANONYMOUS_ID_PROPERTY = "anonymousId";
  public static final String ADMIN_ACCESS = "A";
  public static final String USER_ACCESS = "U";
  public static final String REMOVED_ACCESS = "R";
  public static final String GUEST_ACCESS = "G";
  public static final String KM_ACCESS = "K";
  public static final String DOMAIN_ACCESS = "D";
  private static final String AVATAR_PROPERTY =
      GeneralPropertiesManager.getString("avatar.property", "login");
  private static final String AVATAR_EXTENSION =
      GeneralPropertiesManager.getString("avatar.extension", "jpg");
  private static final ResourceLocator generalSettings = new ResourceLocator(
      "com.stratelia.silverpeas.lookAndFeel.generalLook", "");

  private String id = null;
  private String specificId = null;
  private String domainId = null;
  private String login = null;
  private String firstName = "";
  private String lastName = "";
  private String eMail = "";
  private String accessLevel = "";
  private String loginQuestion = "";
  private String loginAnswer = "";

  /**
* Gets the detail about the specified user.
* @param userId the unique identifier of the user to get.
* @return the detail about the user with the specified identifier or null if no such user exists.
*/
  public static UserDetail getById(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  /**
* Gets the detail about all the users in Silverpeas, whatever their domain.
* @return a list with all the users in Silverpeas.
*/
  public static List<UserDetail> getAll() {
    return Arrays.asList(getOrganizationController().getAllUsers());
  }

  /**
* Gets the detail about all the users belonging in the specified domain.
* @param domainId the unique identifier of the domain.
* @return a list with all the users that defined in the specified domain or null if no such
* domain exists.
*/
  public static List<UserDetail> getAllInDomain(String domainId) {
    return Arrays.asList(getOrganizationController().getAllUsersInDomain(domainId));
  }

  /**
* Constructor
*/
  public UserDetail() {
  }

  public UserDetail(UserDetail toClone) {
    id = toClone.getId();
    specificId = toClone.getSpecificId();
    domainId = toClone.getDomainId();
    login = toClone.getLogin();
    firstName = toClone.getFirstName();
    lastName = toClone.getLastName();
    eMail = toClone.geteMail();
    accessLevel = toClone.getAccessLevel();
    loginQuestion = toClone.getLoginQuestion();
    loginAnswer = toClone.getLoginAnswer();
  }

  /**
* @return the login question String representation
*/
  public String getLoginQuestion() {
    return loginQuestion;
  }

  /**
* Set the login question
* @param loginQuestion
*/
  public void setLoginQuestion(String loginQuestion) {
    this.loginQuestion = loginQuestion;
  }

  /**
* @return the login answer
*/
  public String getLoginAnswer() {
    return loginAnswer;
  }

  /**
* Set the login answer
* @param loginAnswer
*/
  public void setLoginAnswer(String loginAnswer) {
    this.loginAnswer = loginAnswer;
  }

  /**
* Get user id as stored in database
* @return
*/
  public String getId() {
    return this.id;
  }

  /**
* Set user identifier
* @param id the user identifier to set
*/
  public void setId(String id) {
    this.id = id;
  }

  /**
* Get specific user id
* @return
*/
  public String getSpecificId() {
    return specificId;
  }

  /**
* Set specific user id
* @param specificId
*/
  public void setSpecificId(String specificId) {
    this.specificId = specificId;
  }

  /**
* Get user's domain id
* @return user's domain id
*/
  public String getDomainId() {
    return domainId;
  }

  /**
* Set user domain id
* @param domainId
*/
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /**
* Get user's login
* @return user's login
*/
  public String getLogin() {
    return this.login;
  }

  /**
* Set user login
* @param login the login to set
*/
  public void setLogin(String login) {
    if (login != null) {
      this.login = login;
    } else {
      this.login = "";
    }
  }

  /**
* Get user's first name
* @return user's first name
*/
  public String getFirstName() {
    return firstName;
  }

  /**
* Set user first name
* @param firstName user first name
*/
  public void setFirstName(String firstName) {
    if (firstName != null) {
      this.firstName = firstName.trim();
    } else {
      this.firstName = "";
    }
  }

  /**
* Get user's last name
* @return user's last name
*/
  public String getLastName() {
    return lastName;
  }

  /**
* Set user last name
* @param sLastName user last name
*/
  public void setLastName(String sLastName) {
    if (sLastName != null) {
      this.lastName = sLastName.trim();
    } else {
      this.lastName = "";
    }
  }

  /**
* Set user's email
* @param seMail
*/
  public void seteMail(String seMail) {
    if (seMail != null) {
      this.eMail = seMail;
    } else {
      this.eMail = "";
    }
  }

  /**
* Get user's email
* @return
*/
  public String geteMail() {
    return this.eMail;
  }

  /**
* Get user's access level
* @return
*/
  public String getAccessLevel() {
    return accessLevel;
  }

  /**
* Set user access level
* @param sAccessLevel
*/
  public void setAccessLevel(String sAccessLevel) {
    if (sAccessLevel != null) {
      this.accessLevel = sAccessLevel.trim();
    } else {
      this.accessLevel = USER_ACCESS;
    }

  }

  /**
* Is the specified user is restricted to access the resource in its own domain?
* @return true if he's restricted in its own domain, false otherwise.
*/
  public boolean isDomainRestricted() {
    return (GeneralPropertiesManager.getDomainVisibility() == GeneralPropertiesManager.DVIS_ONE ||
            (GeneralPropertiesManager.getDomainVisibility() == GeneralPropertiesManager.DVIS_EACH &&
            ! "0".equals(getDomainId()))) && !isAccessAdmin();
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
    return ADMIN_ACCESS.equalsIgnoreCase(accessLevel);
  }

  public boolean isAccessDomainManager() {
    return DOMAIN_ACCESS.equalsIgnoreCase(accessLevel);
  }

  public boolean isAccessUser() {
    return USER_ACCESS.equalsIgnoreCase(accessLevel);
  }

  public boolean isAccessRemoved() {
    return REMOVED_ACCESS.equalsIgnoreCase(accessLevel);
  }

  public boolean isAccessGuest() {
    return GUEST_ACCESS.equalsIgnoreCase(accessLevel);
  }

  public boolean isAccessKMManager() {
    return KM_ACCESS.equalsIgnoreCase(accessLevel);
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
    if (isAnonymousUserExist()) {
      OrganizationController organizationController = new OrganizationController();
      anonymousUser = organizationController.getUserDetail(getAnonymousUserId());
    }
    return anonymousUser;
  }

  public String getDisplayedName() {
    return (getFirstName() + " " + getLastName()).trim();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserDetail) {
      UserDetail cmpUser = (UserDetail) other;
      return areStringEquals(id, cmpUser.getId())
          && areStringEquals(specificId, cmpUser.getSpecificId())
          && areStringEquals(domainId, cmpUser.getDomainId())
          && areStringEquals(login, cmpUser.getLogin())
          && areStringEquals(firstName, cmpUser.getFirstName())
          && areStringEquals(lastName, cmpUser.getLastName())
          && areStringEquals(eMail, cmpUser.geteMail())
          && areStringEquals(accessLevel, cmpUser.getAccessLevel());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 41 * hash + (this.specificId != null ? this.specificId.hashCode() : 0);
    hash = 41 * hash + (this.domainId != null ? this.domainId.hashCode() : 0);
    hash = 41 * hash + (this.login != null ? this.login.hashCode() : 0);
    hash = 41 * hash + (this.firstName != null ? this.firstName.hashCode() : 0);
    hash = 41 * hash + (this.lastName != null ? this.lastName.hashCode() : 0);
    hash = 41 * hash + (this.eMail != null ? this.eMail.hashCode() : 0);
    hash = 41 * hash + (this.accessLevel != null ? this.accessLevel.hashCode() : 0);
    return hash;
  }

  /**
* Dump user values to the trace system
*/
  public void traceUser() {
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "Id : " + id);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "SpecificId : " +
        specificId);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "DomainId : " +
        domainId);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "Login : " + login);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "FirstName : " +
        firstName);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "LastName : " +
        lastName);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "eMail : " + eMail);
    SilverTrace.info("admin", "UserDetail.traceUser", "admin.MSG_DUMP_USER", "AccessLevel : " +
        accessLevel);
  }

  @Override
  public int compareTo(UserDetail o) {
    UserDetail other = o;
    return ((getLastName() + getFirstName()).toLowerCase()).compareTo((other.getLastName() + other.
        getFirstName()).toLowerCase());
  }

  public String getAvatar() {
    String avatar = getAvatarFileName();
    File image = new File(FileRepositoryManager.getAvatarPath() + File.separatorChar + avatar);
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
    String status =
        new StatusService().getLastStatusService(Integer.parseInt(getId())).getDescription();
    if (isDefined(status)) {
      return status;
    }
    return "";
  }

  /**
* Gets the preferences of this user.
* @return the user preferences.
*/
  public final UserPreferences getUserPreferences() {
    return SilverpeasServiceProvider.getPersonalizationService().getUserSettings(getId());
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

  protected static OrganizationController getOrganizationController() {
    return OrganizationControllerFactory.getFactory().getOrganizationController();
  }
}