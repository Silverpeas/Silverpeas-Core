/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.profile.web;

import com.silverpeas.personalization.UserPreferences;
import static com.silverpeas.profile.web.ProfileResourceBaseURIs.uriOfUser;
import com.silverpeas.ui.DisplayI18NHelper;
import static com.silverpeas.util.StringUtil.isDefined;
import com.silverpeas.web.Exposable;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * The profile of a user that is exposable in the WEB. It is a web entity representing the profile
 * of a user that can be serialized into a given media type (JSON, XML). It is a
 * decorator that decorates a UserDetail object with additional properties concerning its exposition
 * in the WEB.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserProfileEntity extends UserDetail implements Exposable {

  private static final long serialVersionUID = -5011846708353591604L;

  /**
   * Decorates the specified user details with the required WEB exposition features.
   *
   * @param user the user details to decorate.
   * @return a web entity representing the profile of a user.
   */
  public static UserProfileEntity fromUser(final UserDetail user) {
    return new UserProfileEntity(user);
  }

  /**
   * Decorates the specified user details with the required WEB exposition features.
   *
   * @param users a list of details on some users.
   * @param baseURI the URI at which the specified users are defined.
   * @return a list of web entities representing the profile of the specified users.
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
  private UserDetail user = null;
  @XmlElement(required=true)
  private URI uri;
  @XmlElement
  private String webPage;
  @XmlElement
  private String tchatPage;
  @XmlElement(required=true) @NotNull @Size(min=1)
  private String avatar;
  @XmlElement
  private String domainName;
  @XmlElement(required=true, defaultValue="") @NotNull
  private String fullName = "";
  @XmlElement(defaultValue="")
  private String language = "";
  @XmlElement(defaultValue="false")
  private boolean connected = false;
  @XmlElement(defaultValue="false")
  private boolean anonymous = false;

  private UserProfileEntity(UserDetail user) {
    this.user = user;
    UserPreferences prefs = getUserPreferences();
    if (prefs != null) {
      this.language = prefs.getLanguage();
    } else {
      this.language = DisplayI18NHelper.getDefaultLanguage();
    }
    try {
      this.domainName = UserDetail.getOrganizationController().getDomain(this.user.getDomainId()).
              getName();
    } catch (Exception e) {
      // Potential errors during getting domain should not break service  
      SilverTrace.warn("util", "UserProfileEntity.constructor", "root.EX_IGNORED", e);
    }
    this.fullName = user.getDisplayedName();
    this.avatar = getAvatarURI();
    this.connected = this.user.isConnected();
    this.webPage = getUserProfileWebPageURI();
    this.tchatPage = getTchatWebPageURI();
    this.anonymous = user.isAnonymous();
    
  }

  @Override
  @XmlElement(required=true)
  public String getId() {
    return user.getId();
  }

  @Override
  @XmlElement(required=true)
  public String getAccessLevel() {
    return this.user.getAccessLevel();
  }

  @Override
  @XmlElement
  public String getDomainId() {
    return this.user.getDomainId();
  }

  @Override
  @XmlElement(required=true)
  public String getFirstName() {
    return this.user.getFirstName();
  }

  @Override
  @XmlElement(required=true)
  public String getLastName() {
    return this.user.getLastName();
  }

  @Override
  @XmlElement
  public String geteMail() {
    return this.user.geteMail();
  }

  /**
   * Gets the language used by the user.
   * @return the code of the language used by the user.
   */
  public String getLanguage() {
    return language;
  }
  
  @Override
  public void setAccessLevel(String sAccessLevel) {
    this.user.setAccessLevel(sAccessLevel);
  }

  @Override
  public void setDomainId(String sDomainId) {
    this.user.setDomainId(sDomainId);
    this.domainName = UserDetail.getOrganizationController().getDomain(sDomainId).getName();
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
  public void seteMail(String seMail) {
    this.user.seteMail(seMail);
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
   * @return the URL of the user profile WEB page.
   */
  public String getWebPage() {
    if (!isDefined(webPage)) {
      webPage = getUserProfileWebPageURI();
    }
    return webPage;
  }
  
  /**
   * Gets the URL of the tchat WEB page opened to discuss with this user.
   * @return the URL of the user tchat page.
   */
  public String getTchatPage() {
    if (!isDefined(tchatPage)) {
      tchatPage = getUserProfileWebPageURI();
    }
    return tchatPage;
  }

  /**
   * Gets the full name of the user. The full name is made up of its firstname and of its lastname.
   * @return the user fullname.
   */
  public String getFullName() {
    return fullName;
  }
  
  /**
   * Is this user connected to Silverpeas?
   * @return true if the user is connected, false otherwise.
   */
  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  @XmlElement
  public String getSpecificId() {
    return this.user.getSpecificId();
  }

  @Override
  public void setSpecificId(String sSpecificId) {
    this.user.setSpecificId(sSpecificId);
  }

  @Override
  @XmlElement
  public String getLogin() {
    return this.user.getLogin();
  }

  @Override
  public void setLogin(String sLogin) {
    this.user.setLogin(sLogin);
  }

  @Override
  @XmlElement(defaultValue="")
  public String getStatus() {
    return user.getStatus();
  }
  
  public void setStatus(String newStatus) {
    
  }

  public String getDomainName() {
    return this.domainName;
  }
  
  @Override
  public boolean isAnonymous() {
    return this.anonymous;
  }

  public UserProfileEntity withAsUri(URI userUri) {
    this.uri = userUri;
    return this;
  }

  public UserDetail toUserDetail() {
    return this.user;
  }

  protected UserProfileEntity() {
    user = new UserDetail();
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
  
  private String getAvatarURI() {
    String avatarURI = this.user.getAvatar();
    WebApplicationContext context = ContextLoaderListener.getCurrentWebApplicationContext();
    if (context != null) {
      avatarURI =  context.getServletContext().getContextPath() + avatarURI;
    }
    return avatarURI;
  }
  
  private String getUserProfileWebPageURI() {
    String pageUri = "/Rprofil/jsp/Main?userId=" + this.user.getId();
    WebApplicationContext context = ContextLoaderListener.getCurrentWebApplicationContext();
    if (context != null) {
      pageUri =  context.getServletContext().getContextPath() + pageUri;
    } else {
      pageUri = URLManager.getApplicationURL() + pageUri;
    }
    return pageUri;
  }
  
  private String getTchatWebPageURI() {
    String pageUri = "/RcommunicationUser/jsp/OpenDiscussion?userId=" + this.user.getId();
    WebApplicationContext context = ContextLoaderListener.getCurrentWebApplicationContext();
    if (context != null) {
      pageUri =  context.getServletContext().getContextPath() + pageUri;
    } else {
      pageUri = URLManager.getApplicationURL() + pageUri;
    }
    return pageUri;
  }
}
