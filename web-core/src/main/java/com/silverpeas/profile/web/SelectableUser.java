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

import static com.silverpeas.profile.web.ProfileResourceBaseURIs.uriOfUser;
import static com.silverpeas.util.StringUtil.isDefined;
import com.silverpeas.web.Selectable;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A user that is selectable. It is a web entity representing the profile of a user that can be
 * selected among others in order to participate to a given action in the Silverpeas portal. It is a
 * decorator that decorates a UserDetail object with additional properties concerning the selection.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SelectableUser extends UserDetail implements Selectable {

  private static final long serialVersionUID = -5011846708353591604L;

  /**
   * Decorates the specified user with selectable features. By default, the decorated user isn't
   * selected.
   *
   * @param user the user to decorate.
   * @return a selectable user, not selected by default.
   */
  public static SelectableUser fromUser(final UserDetail user) {
    return new SelectableUser(user);
  }

  /**
   * Decorates the specified users with selectable features. By default, the decorated users aren't
   * selected.
   *
   * @param users a list of users to decorate.
   * @param baseURI the URI at which the specified users are defined.
   * @return a list of selectable users, not selected by default.
   */
  public static SelectableUser[] fromUsers(final List<? extends UserDetail> users, URI usersUri) {
    SelectableUser[] selectableUsers = new SelectableUser[users.size()];
    String fromUsersUri = usersUri.toString();
    int i = 0;
    for (UserDetail aUser : users) {
      selectableUsers[i++] = fromUser(aUser).withAsUri(uriOfUser(aUser, fromUsersUri));
    }
    return selectableUsers;
  }
  private UserDetail user = null;
  @XmlElement
  private boolean selected = false;
  @XmlElement
  private URI uri;
  @XmlElement
  private String avatar;
  @XmlElement
  private String domainName;
  @XmlElement
  private String fullName = "";

  private SelectableUser(UserDetail user) {
    this.user = user;
    this.domainName = UserDetail.getOrganizationController().getDomain(this.user.getDomainId()).
            getName();
    this.fullName = user.getDisplayedName();
    this.avatar = this.user.getAvatar();
  }

  @Override
  @XmlElement
  public String getId() {
    return user.getId();
  }

  @Override
  public boolean isSelected() {
    return this.selected;
  }

  public URI getUri() {
    return uri;
  }

  @Override
  @XmlElement
  public String getAccessLevel() {
    return this.user.getAccessLevel();
  }

  @Override
  @XmlElement
  public String getDomainId() {
    return this.user.getDomainId();
  }

  @Override
  @XmlElement
  public String getFirstName() {
    return this.user.getFirstName();
  }

  @Override
  @XmlElement
  public String getLastName() {
    return this.user.getLastName();
  }

  @Override
  @XmlElement
  public String geteMail() {
    return this.user.geteMail();
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
      avatar = this.user.getAvatar();
    }
    return avatar;
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

  public String getDomainName() {
    return this.domainName;
  }

  @Override
  public void select() {
    this.selected = true;
  }

  @Override
  public void unselect() {
    this.selected = false;
  }

  public SelectableUser withAsUri(URI userUri) {
    this.uri = userUri;
    return this;
  }

  public UserDetail toUserDetail() {
    return this.user;
  }

  protected SelectableUser() {
    user = new UserDetail();
  }

  @Override
  public void setId(String id) {
    this.user.setId(id);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof SelectableUser) {
      return this.user.equals(((SelectableUser) other).user);
    } else {
      return this.user.equals(other);
    }
  }

  @Override
  public int hashCode() {
    return this.user.hashCode();
  }
}
