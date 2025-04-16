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
package org.silverpeas.core.webapi.profile;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.rs.WebEntity;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs.uriOfUser;

/**
 * The profile of a user that is web entity in the WEB. It is a web entity representing the profile
 * of a user that can be serialized into a given media type (JSON, XML). It is a decorator that
 * decorates a UserDetail object with additional properties concerning its exposition in the WEB.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserProfileEntity extends UserDetail implements WebEntity {

  private static final long serialVersionUID = -5011846708353591604L;

  private final transient ServletContext context = ServiceProvider.getService(ServletContext.class);
  private final UserDetail user;
  @XmlElement(required = true)
  private URI uri;
  @XmlElement
  private URI contactsUri;
  @XmlElement
  private String webPage;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String avatar;
  @XmlElement
  private String domainName;
  @XmlElement(required = true, defaultValue = "")
  @NotNull
  private String fullName = "";
  @XmlElement(defaultValue = "")
  private String language = "";
  @XmlElement(defaultValue = "false")
  private boolean connected = false;
  @XmlElement(defaultValue = "false")
  private boolean anonymous = false;
  @XmlElement(defaultValue = "false")
  private boolean system = false;

  protected UserProfileEntity() {
    user = new UserDetail();
  }

  protected UserProfileEntity(UserDetail user) {
    this.user = user;
    this.system = user.isSystem();
    UserPreferences prefs = getUserPreferences();
    if (prefs != null) {
      this.language = prefs.getLanguage();
    } else {
      this.language = DisplayI18NHelper.getDefaultLanguage();
    }
    if (user.getDomain() == null) {
      this.domainName = "";
    } else {
      this.domainName = user.getDomain().getName();
    }
    this.fullName = user.getDisplayedName();
    this.avatar = getAvatarURI();
    this.connected = this.user.isConnected();
    this.webPage = getUserProfileWebPageURI();
    this.anonymous = user.isAnonymous();
  }

  /**
   * Decorates the specified user details with the required WEB exposition features.
   *
   * @param user the user details to decorate.
   * @return a web entity representing the profile of a user.
   */
  public static UserProfileEntity fromUser(final User user) {
    return new UserProfileEntity(UserDetail.from(user));
  }

  /**
   * Decorates the specified user details with the required WEB exposition features.
   *
   * @param users a list of details on some users.
   * @param usersUri the URI at which the specified users are defined.
   * @return an array of web entities representing the profile of the specified users.
   */
  public static UserProfileEntity[] fromUsers(final List<? extends UserDetail> users, URI usersUri) {
    UserProfileEntity[] selectableUsers = new UserProfileEntity[users.size()];
    String fromUsersUri = usersUri.toString();
    int i = 0;
    for (UserDetail aUser : users) {
      selectableUsers[i++] = fromUser(aUser).withAsUri(uriOfUser(aUser, fromUsersUri));
    }
    return selectableUsers;
  }

  @Override
  @XmlElement(required = true)
  public String getId() {
    return user.getId();
  }

  @Override
  @XmlElement(required = true)
  public UserAccessLevel getAccessLevel() {
    return this.user.getAccessLevel();
  }

  @Override
  @XmlElement
  public String getDomainId() {
    return this.user.getDomainId();
  }

  @Override
  @XmlElement(required = true)
  public String getFirstName() {
    return this.user.getFirstName();
  }

  @Override
  @XmlElement(required = true)
  public String getLastName() {
    return this.user.getLastName();
  }

  @Override
  @XmlElement
  public String getEmailAddress() {
    return Encode.forHtml(this.user.getEmailAddress());
  }

  @Override
  @XmlElement
  public boolean isBlockedState() {
    return this.user.isBlockedState();
  }

  @Override
  @XmlElement
  public boolean isExpiredState() {
    return this.user.isExpiredState();
  }

  @Override
  @XmlElement
  public boolean isDeletedState() {
    return this.user.isDeletedState();
  }

  @Override
  @XmlElement
  public boolean isRemovedState() {
    return this.user.isRemovedState();
  }

  @Override
  @XmlElement
  public boolean isDeactivatedState() {
    return this.user.isDeactivatedState();
  }

  /**
   * Gets the language used by the user.
   *
   * @return the code of the language used by the user.
   */
  public String getLanguage() {
    return language;
  }

  @Override
  public void setAccessLevel(UserAccessLevel accessLevel) {
    this.user.setAccessLevel(accessLevel);
  }

  @Override
  public void setDomainId(String sDomainId) {
    this.user.setDomainId(sDomainId);
    this.domainName = user.getDomain().getId();
  }

  @Override
  public void setFirstName(String sFirstName) {
    this.user.setFirstName(sFirstName);
    this.fullName = this.user.getDisplayedName();
  }

  @Override
  public void setLastName(String sLastName) {
    this.user.setLastName(sLastName);
    this.fullName = this.user.getDisplayedName();
  }

  @Override
  public void setEmailAddress(String emailAddress) {
    this.user.setEmailAddress(emailAddress);
  }

  @Override
  public String getAvatar() {
    if (!isDefined(avatar)) {
      avatar = getAvatarURI();
    }
    return avatar;
  }

  /**
   * Gets the URL of the WEB page in which is presented the profile of this user.
   *
   * @return the URL of the user profile WEB page.
   */
  public String getWebPage() {
    if (!isDefined(webPage)) {
      webPage = getUserProfileWebPageURI();
    }
    return webPage;
  }

  /**
   * Gets the full name of the user. The full name is made up of its firstname and of its lastname.
   *
   * @return the user fullname.
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * Is this user connected to Silverpeas?
   *
   * @return true if the user is connected, false otherwise.
   */
  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public String getSpecificId() {
    return this.user.getSpecificId();
  }

  @Override
  public void setSpecificId(String sSpecificId) {
    this.user.setSpecificId(sSpecificId);
  }

  @Override
  public String getLogin() {
    return this.user.getLogin();
  }

  @Override
  public void setLogin(String sLogin) {
    this.user.setLogin(sLogin);
  }

  @Override
  @XmlElement(defaultValue = "")
  public String getStatus() {
    return this.user.getStatus();
  }

  public String getDomainName() {
    return this.domainName;
  }

  @Override
  public boolean isAnonymous() {
    return this.anonymous;
  }

  @Override
  public boolean isSystem() {
    return this.system;
  }

  public UserProfileEntity withAsUri(URI userUri) {
    this.uri = userUri;
    try {
      this.contactsUri = new URI(this.uri.toString() + "/contacts");
    } catch (URISyntaxException ex) {
      Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public void setId(String id) {
    this.user.setId(id);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserProfileEntity) {
      return this.user.equals(((UserProfileEntity) other).user);
    } else {
      return this.user.equals(other);
    }
  }

  @Override
  public int hashCode() {
    return this.user.hashCode();
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  public URI getContactsURI() {
    return this.contactsUri;
  }

  private String getAvatarURI() {
    String avatarURI = this.user.getSmallAvatar();
    if (context != null) {
      avatarURI = context.getContextPath() + avatarURI;
    }
    return avatarURI;
  }

  private String getUserProfileWebPageURI() {
    String pageUri = "/Rprofil/jsp/Main?userId=" + this.user.getId();
    if (context != null) {
      pageUri = context.getContextPath() + pageUri;
    } else {
      pageUri = URLUtil.getApplicationURL() + pageUri;
    }
    return pageUri;
  }
}
