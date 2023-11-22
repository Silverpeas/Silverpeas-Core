/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.test;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.personalization.UserPreferences;

import java.util.Date;
import java.util.Objects;

/**
 * Implementation of a user in Silverpeas dedicated to the tests.
 * @author mmoquillon
 */
public class TestUser implements User {

  private String id;
  private String domainId;
  private String firstName;
  private String lastName;
  private String login;
  private String token;

  public static class Builder {

    private final TestUser user = new TestUser();

    public Builder setId(final String id) {
      this.user.id = id;
      return this;
    }

    public Builder setDomainId(final String domainId) {
      this.user.domainId = domainId;
      return this;
    }

    public Builder setFirstName(final String firstName) {
      this.user.firstName = firstName;
      return this;
    }

    public Builder setLastName(final String lastName) {
      this.user.lastName = lastName;
      return this;
    }

    public Builder setLogin(final String login) {
      this.user.login = login;
      return this;
    }

    public Builder setToken(final String token) {
      this.user.token = token;
      return this;
    }

    public User build() {
      return this.user;
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDomainId() {
    return domainId;
  }

  @Override
  public boolean isDomainRestricted() {
    return false;
  }

  @Override
  public boolean isDomainAdminRestricted() {
    return false;
  }

  @Override
  public String getLogin() {
    return login;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  @Override
  public String getEmailAddress() {
    return null;
  }

  @Override
  public Date getCreationDate() {
    return new Date();
  }

  @Override
  public Date getSaveDate() {
    return new Date();
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public Date getStateSaveDate() {
    return new Date();
  }

  @Override
  public boolean isFullyDefined() {
    return false;
  }

  @Override
  public UserAccessLevel getAccessLevel() {
    return id.equals("0") ? UserAccessLevel.ADMINISTRATOR : UserAccessLevel.USER;
  }

  @Override
  public boolean isSystem() {
    return id.equals("-1");
  }

  @Override
  public boolean isAnonymous() {
    return getAccessLevel() == UserAccessLevel.GUEST;
  }

  @Override
  public boolean isBlanked() {
    return isDeletedState() && firstName.equals("_Anonymous_");
  }

  @Override
  public boolean isAccessAdmin() {
    return getAccessLevel() == UserAccessLevel.ADMINISTRATOR;
  }

  @Override
  public boolean isAccessDomainManager() {
    return isAccessAdmin() || getAccessLevel() == UserAccessLevel.DOMAIN_ADMINISTRATOR;
  }

  @Override
  public boolean isAccessPdcManager() {
    return isAccessAdmin() || getAccessLevel() == UserAccessLevel.PDC_MANAGER;
  }

  @Override
  public boolean isAccessUser() {
    return getAccessLevel() == UserAccessLevel.USER;
  }

  @Override
  public boolean isAccessGuest() {
    return getAccessLevel() == UserAccessLevel.GUEST;
  }

  @Override
  public boolean isAccessUnknown() {
    return getAccessLevel() == UserAccessLevel.UNKNOWN;
  }

  @Override
  public boolean isPlayingAdminRole(final String s) {
    return false;
  }

  @Override
  public UserState getState() {
    return UserState.VALID;
  }

  @Override
  public boolean isActivatedState() {
    return !isAnonymous() && !isDeletedState() && !isRemovedState() && !isDeactivatedState();
  }

  @Override
  public boolean isValidState() {
    return isAnonymous() ||
        (!UserState.UNKNOWN.equals(getState()) && !isDeletedState() && !isRemovedState() &&
            !isBlockedState() && !isDeactivatedState() && !isExpiredState());
  }

  @Override
  public boolean isDeletedState() {
    return UserState.DELETED == getState();
  }

  @Override
  public boolean isRemovedState() {
    return UserState.REMOVED == getState();
  }

  @Override
  public boolean isBlockedState() {
    return UserState.BLOCKED == getState();
  }

  @Override
  public boolean isDeactivatedState() {
    return UserState.DEACTIVATED == getState();
  }

  @Override
  public boolean isExpiredState() {
    return UserState.EXPIRED.equals(getState());
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public UserPreferences getUserPreferences() {
    return null;
  }

  @Override
  public String getAvatar() {
    return User.DEFAULT_AVATAR_PATH;
  }

  @Override
  public String getSmallAvatar() {
    return User.DEFAULT_AVATAR_PATH;
  }

  @Override
  public String getStatus() {
    return getState().getName();
  }

  @Override
  public String getDurationOfCurrentSession() {
    return "0";
  }

  @Override
  public boolean isUserManualNotificationUserReceiverLimit() {
    return false;
  }

  @Override
  public int getUserManualNotificationUserReceiverLimitValue() {
    return 0;
  }

  @Override
  public int compareTo(final User user) {
    return this.getId().compareTo(user.getId());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TestUser testUser = (TestUser) o;
    return id.equals(testUser.id) && domainId.equals(testUser.domainId) &&
        Objects.equals(firstName, testUser.firstName) &&
        Objects.equals(lastName, testUser.lastName) &&
        Objects.equals(login, testUser.login) &&
        Objects.equals(token, testUser.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, domainId, firstName, lastName, login, token);
  }
}
