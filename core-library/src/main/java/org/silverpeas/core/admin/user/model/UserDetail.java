/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenRuntimeException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.socialnetwork.status.StatusService;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.*;

import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.getUserManualNotificationRecipientLimit;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.isUserManualNotificationRecipientLimitEnabled;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

public class UserDetail implements User {

  public static final String BLANK_NAME = "_Anonymous_";
  public static final String SYSTEM_USER_ID = "-1";
  /**
   * The identifier to use with some API when managing resources mapped with the users. This
   * identifier is mainly to be used by the Silverpeas internal mechanisms.
   */
  public static final String USER_COMPONENT = "users";
  private static final long serialVersionUID = -109886153681824159L;
  private static final String ANONYMOUS_ID_PROPERTY = "anonymousId";
  private static final String DEFAULT_AVATAR_PROPERTY = "login";
  private static final String AVATAR_PROPERTY =
      ResourceLocator.getGeneralSettingBundle()
          .getString("avatar.property", DEFAULT_AVATAR_PROPERTY);
  private static final String AVATAR_EXTENSION =
      ResourceLocator.getGeneralSettingBundle().getString("avatar.extension", "jpg");
  private static final String AVATAR_BASE_URI = "/display/avatar/";
  private static final SettingBundle generalSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
  private String id = null;
  private String specificId = null;
  private String domainId = null;
  private String login = null;
  private String firstName = "";
  private String lastName = "";
  private String eMail = "";
  private UserAccessLevel accessLevel = UserAccessLevel.from(null);
  private String loginQuestion = "";
  private String loginAnswer = "";
  private Date creationDate = null;
  private Date saveDate = null;
  private int version = 0;
  private Date tosAcceptanceDate = null;
  private Date lastLoginDate = null;
  private int nbSuccessfulLoginAttempts = 0;
  private Date lastLoginCredentialUpdateDate = null;
  private Date expirationDate = null;
  private UserState state = UserState.from(null);
  private Date stateSaveDate = null;
  private Integer notifManualReceiverLimit;
  private UserPreferences preferences = null;
  private boolean sensitiveData;

  /**
   * Gets a {@link UserDetail} form of the specified user.
   * @param user a user.
   * @return a user detail.
   */
  public static UserDetail from(final User user) {
    return (UserDetail) user;
  }

  /**
   * Gets the detail about the specified user.
   * @param userId the unique identifier of the user to get.
   * @return the detail about the user with the specified identifier or null if no such user exists.
   */
  public static UserDetail getById(String userId) {
    return (UserDetail) User.getById(userId);
  }

  /**
   * Gets the detail about the current user behind a request of treatment processing.
   * @return the detail about the user above described.
   */
  public static UserDetail getCurrentRequester() {
    return (UserDetail) User.getCurrentRequester();
  }

  /**
   * Gets the system user of Silverpeas. It is a virtual user (that is to say a user without any
   * account in Silverpeas) used in some processes that are triggered by no real users or executed
   * for one or more users but by the system itself (like batch processes).
   * @return the system user of Silverpeas.
   */
  public static UserDetail getSystemUser() {
    return (UserDetail) User.getSystemUser();
  }

  /**
   * Gets the detail about all the users in Silverpeas, whatever their domain.
   * @return a list with all the users in Silverpeas.
   */
  public static List<UserDetail> getAll() {
    return Arrays.asList(getOrganisationController().getAllUsers());
  }

  /**
   * Constructor
   */
  public UserDetail() {
  }

  public UserDetail(UserDetail otherUser) {
    id = otherUser.getId();
    specificId = otherUser.getSpecificId();
    domainId = otherUser.getDomainId();
    login = otherUser.getLogin();
    firstName = otherUser.getFirstName();
    lastName = otherUser.getLastName();
    eMail = otherUser.getEmailAddress();
    accessLevel = otherUser.getAccessLevel();
    loginQuestion = otherUser.getLoginQuestion();
    loginAnswer = otherUser.getLoginAnswer();
    creationDate = otherUser.getCreationDate();
    saveDate = otherUser.getSaveDate();
    version = otherUser.getVersion();
    tosAcceptanceDate = otherUser.getTosAcceptanceDate();
    lastLoginDate = otherUser.getLastLoginDate();
    nbSuccessfulLoginAttempts = otherUser.getNbSuccessfulLoginAttempts();
    lastLoginCredentialUpdateDate = otherUser.getLastLoginCredentialUpdateDate();
    expirationDate = otherUser.getExpirationDate();
    state = otherUser.getState();
    stateSaveDate = otherUser.getStateSaveDate();
    notifManualReceiverLimit = otherUser.getNotifManualReceiverLimit();
    sensitiveData = otherUser.hasSensitiveData();
    preferences = otherUser.preferences; // attribute to avoid the eager loading of preferences
  }

  /**
   * Does this user have sensitive data? User data that could be sensitive are defined in the
   * descriptor of the domain to which this user belongs.
   * @return true if some of the user data are sensitive. False otherwise
   */
  public boolean hasSensitiveData() {
    return sensitiveData;
  }

  /**
   * Sets some of the user data as sensitive or not. The potential data that can be sensitive are
   * defined in the descriptor of the domain to which this user belongs.
   * @param sensitive a boolean indicating whether the data declared as potentially sensitive in the
   * user domain descriptor have to be marked as such: true and the data are marked as sensitive,
   * false and the data are declared as not being actually sensitive.
   */
  public void setSensitiveData(boolean sensitive) {
    this.sensitiveData = sensitive;
  }

  /**
   * @return the login question String representation
   */
  public String getLoginQuestion() {
    return loginQuestion;
  }

  /**
   * Set the question to the user having lost his login.
   * @param loginQuestion the new login question
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
   * @param loginAnswer the new answer to the login question
   */
  public void setLoginAnswer(String loginAnswer) {
    this.loginAnswer = loginAnswer;
  }

  /**
   * @return the date of terms of service acceptance
   */
  public Date getTosAcceptanceDate() {
    return tosAcceptanceDate;
  }

  /**
   * @param tosAcceptanceDate the date of terms of service acceptance
   */
  public void setTosAcceptanceDate(final Date tosAcceptanceDate) {
    this.tosAcceptanceDate = tosAcceptanceDate;
  }

  /**
   * @return the date of the last user login
   */
  public Date getLastLoginDate() {
    return lastLoginDate;
  }

  /**
   * @param lastLoginDate the date of the last user login
   */
  public void setLastLoginDate(final Date lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
  }

  /**
   * @return number of successful login attempts
   */
  public int getNbSuccessfulLoginAttempts() {
    return nbSuccessfulLoginAttempts;
  }

  /**
   * @param nbSuccessfulLoginAttempts number of successful login attempts
   */
  public void setNbSuccessfulLoginAttempts(final int nbSuccessfulLoginAttempts) {
    this.nbSuccessfulLoginAttempts = nbSuccessfulLoginAttempts;
  }

  /**
   * @return the date of the last update of login credentials
   */
  public Date getLastLoginCredentialUpdateDate() {
    return lastLoginCredentialUpdateDate;
  }

  /**
   * @param lastLoginCredentialUpdateDate the date of the last update of login credentials
   */
  public void setLastLoginCredentialUpdateDate(final Date lastLoginCredentialUpdateDate) {
    this.lastLoginCredentialUpdateDate = lastLoginCredentialUpdateDate;
  }

  /**
   * @return the date of user expiration (account)
   */
  public Date getExpirationDate() {
    return expirationDate;
  }

  /**
   * @param expirationDate the date of user expiration (account)
   */
  public void setExpirationDate(final Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  @Override
  public UserState getState() {
    return state;
  }

  /**
   * The state of the user (account) is updated and the corresponding save date too.
   * @param state the state of the user (account)
   */
  public void setState(final UserState state) {
    this.state = state != null ? state : UserState.from(null);
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the date of the user creation
   */
  public void setCreationDate(final Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public Date getSaveDate() {
    return saveDate;
  }

  /**
   * @param saveDate the date of the last user save
   */
  public void setSaveDate(final Date saveDate) {
    this.saveDate = saveDate;
  }

  @Override
  public int getVersion() {
    return version;
  }

  /**
   * @param version the version of the last save
   */
  public void setVersion(final int version) {
    this.version = version;
  }

  @Override
  public Date getStateSaveDate() {
    return stateSaveDate;
  }

  /**
   * @param stateSaveDate the date of last user state save (when it changes)
   */
  public void setStateSaveDate(final Date stateSaveDate) {
    this.stateSaveDate = stateSaveDate;
  }

  @Override
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
   * @return the user identifier specific to the identity service into which his account is
   * registered.
   */
  public String getSpecificId() {
    return specificId;
  }

  /**
   * Set specific user id
   * @param specificId set the identifier of the user specific to the identity server into which
   * his account is registered.
   */
  public void setSpecificId(String specificId) {
    this.specificId = specificId;
  }

  @Override
  public String getDomainId() {
    return domainId;
  }

  /**
   * Set user domain id
   * @param domainId the unique identifier of the user domain he belongs to.
   */
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  @Override
  public String getLogin() {
    return this.login;
  }

  /**
   * Set user login
   * @param login the login to set
   */
  public void setLogin(String login) {
    this.login = Objects.requireNonNullElse(login, "");
  }

  @Override
  public String getFirstName() {
    String result = firstName;
    if (BLANK_NAME.equals(firstName)) {
      final User user = User.getCurrentRequester();
      final String language;
      if (user != null) {
        language = user.getUserPreferences().getLanguage();
      } else {
        language = DisplayI18NHelper.getDefaultLanguage();
      }
      final LocalizationBundle generalBundle =
          ResourceLocator.getGeneralLocalizationBundle(language);
      result = generalBundle.getString("GML.Anonymous");
    }
    return result;
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

  @Override
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
   * @param emailAddress the email address of the user.
   */
  public void setEmailAddress(String emailAddress) {
    this.eMail = Objects.requireNonNullElse(emailAddress, "");
  }

  @Override
  public String getEmailAddress() {
    return this.eMail;
  }

  @Override
  public UserAccessLevel getAccessLevel() {
    return accessLevel;
  }

  /**
   * Set user access level
   * @param accessLevel the access level of the user
   */
  public void setAccessLevel(UserAccessLevel accessLevel) {
    this.accessLevel = Objects.requireNonNullElse(accessLevel, UserAccessLevel.USER);
  }

  /**
   * Gets the domain to which this user belongs.
   * @return a user domain.
   */
  public Domain getDomain() {
    return getOrganisationController().getDomain(getDomainId());
  }

  @Override
  public boolean isDomainRestricted() {
    return (DomainProperties.areDomainsNonVisibleToOthers() ||
        (DomainProperties.areDomainsVisibleOnlyToDefaultOne() &&
            !DomainProperties.isDefaultDomain(getDomainId()))) && !isAccessAdmin();
  }

  @Override
  public boolean isDomainAdminRestricted() {
    return (!DomainProperties.areDomainsVisibleToAll() &&
        (!isAccessAdmin()) &&
        (!DomainProperties.areDomainsVisibleOnlyToDefaultOne() ||
            (!DomainProperties.isDefaultDomain(getDomainId()))));
  }

  public boolean isBackOfficeVisible() {
    return (isAccessPdcManager() || isAccessAdmin() || isAccessDomainManager());
  }

  @Override
  public boolean isAccessAdmin() {
    return UserAccessLevel.ADMINISTRATOR.equals(accessLevel);
  }

  @Override
  public boolean isAccessDomainManager() {
    return UserAccessLevel.DOMAIN_ADMINISTRATOR.equals(accessLevel);
  }

  @Override
  public boolean isAccessPdcManager() {
    return UserAccessLevel.PDC_MANAGER.equals(accessLevel);
  }

  @Override
  public boolean isAccessUser() {
    return UserAccessLevel.USER.equals(accessLevel);
  }

  @Override
  public boolean isAccessGuest() {
    return UserAccessLevel.GUEST.equals(accessLevel);
  }

  @Override
  public boolean isAccessUnknown() {
    return UserAccessLevel.UNKNOWN.equals(accessLevel);
  }

  @Override
  public boolean isPlayingAdminRole(final String instanceId) {
    if (isAccessAdmin()) {
      return true;
    }
    Set<SilverpeasRole> roles =
        ComponentAccessControl.get().getUserRoles(getId(), instanceId, AccessControlContext.init());
    return roles.contains(SilverpeasRole.ADMIN);
  }

  @Override
  public boolean isActivatedState() {
    return !isAnonymous() && !isDeletedState() && !isRemovedState() && !isDeactivatedState();
  }

  @Override
  public boolean isValidState() {
    return isAnonymous() ||
        (!UserState.UNKNOWN.equals(state) && !isDeletedState() && !isRemovedState() &&
            !isBlockedState() && !isDeactivatedState() && !isExpiredState());
  }

  @Override
  public boolean isDeletedState() {
    return UserState.DELETED.equals(state);
  }

  @Override
  public boolean isRemovedState() {
    return UserState.REMOVED.equals(state);
  }

  @Override
  public boolean isBlockedState() {
    return UserState.BLOCKED.equals(state);
  }

  @Override
  public boolean isDeactivatedState() {
    return UserState.DEACTIVATED.equals(state);
  }

  @Override
  public boolean isExpiredState() {
    return UserState.EXPIRED.equals(state) || (getExpirationDate() != null && getExpirationDate().
        compareTo(DateUtil.getDate()) < 0);
  }

  @Override
  public boolean isSystem() {
    return getId().equals(SYSTEM_USER_ID);
  }

  @Override
  public boolean isAnonymous() {
    return getId() != null && isAnonymousUser(getId());
  }

  @Override
  public boolean isBlanked() {
    return isDeletedState() && firstName.equals(BLANK_NAME);
  }

  /**
   * Gets the anonymous user or null if no such user exists.
   * @return the detail about the anonymous user or null if no such user exists.
   */
  public static UserDetail getAnonymousUser() {
    UserDetail anonymousUser = null;
    if (isAnonymousUserExist()) {
      anonymousUser = OrganizationControllerProvider.getOrganisationController()
          .getUserDetail(getAnonymousUserId());
    }
    return anonymousUser;
  }

  /**
   * Gets the API token of the user. Each user in Silverpeas has a token with which he can
   * access some services or web resources available out of Silverpeas; the token serves to
   * authenticate and to identify him among those services or web resources in order to use them.
   * @return the API token of the user.
   */
  public String getToken() {
    try {
      UserReference ref = UserReference.fromUser(this);
      return PersistentResourceToken.getOrCreateToken(ref).getValue();
    } catch (TokenException e) {
      throw new TokenRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserDetail) {
      UserDetail cmpUser = (UserDetail) other;
      return Objects.equals(id, cmpUser.getId()) &&
          Objects.equals(specificId, cmpUser.getSpecificId()) &&
          Objects.equals(domainId, cmpUser.getDomainId()) &&
          Objects.equals(login, cmpUser.getLogin()) &&
          Objects.equals(firstName, cmpUser.getFirstName()) &&
          Objects.equals(lastName, cmpUser.getLastName()) &&
          Objects.equals(eMail, cmpUser.getEmailAddress()) &&
          Objects.equals(accessLevel, cmpUser.getAccessLevel());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, specificId, domainId, login, firstName, lastName, eMail, accessLevel);
  }

  /**
   * Dump user values to the trace system
   */
  public void traceUser() {
    SilverLogger.getLogger(this)
        .info("User data:\nspecificId = {0}\ndomainId = {1}\nfirstName = {2}\nlastName = {3}",
            specificId, domainId, firstName, lastName);
  }

  @Override
  public int compareTo(User other) {
    Collator collator = Collator.getInstance();
    String myLastName = getLastName().toLowerCase(I18NHelper.defaultLocale);
    String otherLastName = other.getLastName().toLowerCase(I18NHelper.defaultLocale);
    int result = collator.compare(myLastName, otherLastName);
    if (result == 0) {
      String myFirstName = getFirstName().toLowerCase(I18NHelper.defaultLocale);
      String otherFirstName = other.getFirstName().toLowerCase(I18NHelper.defaultLocale);
      result = collator.compare(myFirstName, otherFirstName);
      if (result == 0) {
        String myId = getId();
        String otherId = other.getId();
        result = myId.compareTo(otherId);
      }
    }
    return result;
  }

  @Override
  public String getAvatar() {
    File avatar = getAvatarFile();
    if (avatar.exists() && avatar.isFile()) {
      return AVATAR_BASE_URI + avatar.getName();
    }
    return User.DEFAULT_AVATAR_PATH;
  }

  @Override
  public String getSmallAvatar() {
    String avatar = getAvatar();
    if (avatar.startsWith(AVATAR_BASE_URI)) {
      return FileServerUtils.getImageURL(avatar, "image.size.avatar.profil");
    }
    return avatar;
  }

  public boolean isAvatarDefined() {
    return getAvatarFile().exists();
  }

  public String getAvatarFileName() {
    String propertyValue = getLogin();
    if (!AVATAR_PROPERTY.equals(DEFAULT_AVATAR_PROPERTY)) {
      try {
        String getterName =
            "get" + Character.toUpperCase(AVATAR_PROPERTY.charAt(0)) + AVATAR_PROPERTY.substring(1);
        Method getter = getClass().getMethod(getterName);
        propertyValue = getter.invoke(this).toString();
        if (!StringUtil.isDefined(propertyValue)) {
          propertyValue = getLogin();
        }
      } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        SilverLogger.getLogger(this).silent(e);
      }
    }
    return propertyValue + "." + AVATAR_EXTENSION;
  }

  private File getAvatarFile() {
    return new File(FileRepositoryManager.getAvatarPath(), getAvatarFileName());
  }

  @Override
  public String getStatus() {
    StatusService statusService = ServiceProvider.getService(StatusService.class);
    String status = statusService.getLastStatus(Integer.parseInt(getId())).getDescription();
    if (isDefined(status)) {
      return status;
    }
    return "";
  }

  @Override
  public UserPreferences getUserPreferences() {
    if (preferences == null) {
      preferences =
          PersonalizationServiceProvider.getPersonalizationService().getUserSettings(getId());
    }
    return preferences;
  }

  /**
   * Indicates if the current user is in relation with a user represented by the given identifier.
   * @param userId the identifier of user which the current user is potentially in relation with.
   * @return true if the current user is in relation with the user represented by the given
   * identifier, false otherwise.
   */
  public boolean isInRelationWith(String userId) {
    RelationShipService relation = RelationShipService.get();
    try {
      return relation.isInRelationShip(Integer.parseInt(userId), Integer.parseInt(getId()));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return false;
  }

  public Invitation getInvitationSentTo(String userId) {
    return getInvitation(getId(), userId);
  }

  public Invitation getInvitationReceivedFrom(String userId) {
    return getInvitation(userId, getId());
  }

  private Invitation getInvitation(String fromUserId, String toUserId) {
    InvitationService invitationService = InvitationService.get();
    Invitation invitation = null;
    try {
      invitation =
          invitationService.getInvitation(Integer.parseInt(fromUserId), Integer.parseInt(toUserId));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return invitation;
  }

  @Override
  public boolean isConnected() {
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    return sessionManagement.isUserConnected(this);
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

  @Override
  public boolean isFullyDefined() {
    return StringUtil.isDefined(getId()) && StringUtil.isDefined(getLogin()) &&
        StringUtil.isDefined(getLastName());
  }

  @Override
  public String getDisplayedName() {
    // At a first read, this override is useless. In fact, JSTL is not able for now to find
    // default method implementations, so this override is just to get available the method from
    // JSP pages...
    return User.super.getDisplayedName();
  }

  @Override
  public String getDurationOfCurrentSession() {
    if (isConnected()) {
      return DateUtil.formatDuration(new Date().getTime() - getLastLoginDate().getTime());
    }
    return "";
  }

  @Override
  public boolean isUserManualNotificationUserReceiverLimit() {
    return getUserManualNotificationUserReceiverLimitValue() > 0;
  }

  @Override
  public int getUserManualNotificationUserReceiverLimitValue() {
    int limit = 0;
    if (isUserManualNotificationRecipientLimitEnabled() && (isAccessUser() || isAccessGuest() ||
        isAnonymous() || isAccessUnknown())) {
      if (!isAnonymous() && !isAccessUnknown() && getNotifManualReceiverLimit() != null) {
        limit = getNotifManualReceiverLimit();
      } else {
        limit = getUserManualNotificationRecipientLimit();
      }
    }
    return limit;
  }

  /**
   * Sets the maximum user receivers the user can notify manually. If the given value is:
   * <ul>
   * <li>null or less than/equal to -1 or equal to the default server limitation value, then it is
   * considered that the default server value will be taken into account</li>
   * <li>greater than or equal to 0, then the user has a specific limitation set</li>
   * </ul>
   * The value is persisted only if the user has USER or GUEST access right and if the limitation
   * is enabled at server level.
   * @param limit the maximum user receivers the user can notify manually.
   */
  public void setUserManualNotificationUserReceiverLimit(Integer limit) {
    if (isUserManualNotificationRecipientLimitEnabled() && (isAccessUser() || isAccessGuest())) {
      if (limit != null && (limit <= -1 || limit == getUserManualNotificationRecipientLimit())) {
        limit = null;
      }
      setNotifManualReceiverLimit(limit);
    }
  }

  /**
   * Sets the maximum user receivers the user can notify manually from the persistence context.<br>
   * This method must only be used be the administration persistence services.
   * @param notifManualReceiverLimit the maximum user receivers the user can notify manually from
   * the persistence context.
   */
  public void setNotifManualReceiverLimit(final Integer notifManualReceiverLimit) {
    this.notifManualReceiverLimit = notifManualReceiverLimit;
  }

  /*
   * Gets the maximum user receivers the user can notify manually from the persistence context.<br>
   * This method must only be used be the administration persistence services.
   * @return the maximum user receivers the user can notify manually from the persistence context.
   */
  public Integer getNotifManualReceiverLimit() {
    return notifManualReceiverLimit;
  }

  /**
   * Gets the unique identifier of the anonymous user as set in the general look properties.
   * @return the anonymous user identifier.
   */
  protected static String getAnonymousUserId() {
    return generalSettings.getString(ANONYMOUS_ID_PROPERTY, null);
  }

  protected static OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  /**
   * Centralized sort of {@link UserDetail} based on:
   * <ul>
   *   <li>firstly the last name</li>
   *   <li>then the first name</li>
   * </ul>
   */
  public static class OnFirstNameAndLastName extends AbstractComplexComparator<UserDetail> {
    @Override
    protected ValueBuffer getValuesToCompare(final UserDetail user) {
      return new ValueBuffer().append(user.getLastName()).append(user.getFirstName());
    }
  }

  /**
   * Centralized sort of {@link UserDetail} based on creation date
   */
  public static class OnCreationDate extends AbstractComplexComparator<User> {
    @Override
    protected ValueBuffer getValuesToCompare(final User user) {
      long creationDateTime = 0;
      if (user.getCreationDate() != null) {
        creationDateTime = user.getCreationDate().getTime();
      }
      return new ValueBuffer().append(creationDateTime);
    }
  }
}
