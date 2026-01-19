/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.stub;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.personalization.UserPreferences;

import java.time.ZoneId;
import java.util.Date;

/**
 * User implementation for integration tests that don't require to dependent on the whole core admin
 * package.
 *
 * @author mmoquillon
 */
public class UserImpl implements User {

  public static final String SYSTEM_ID = "-1";

  public static Builder builder(String userId) {
    return new Builder(userId);
  }

  private final String id;
  private final UserState state;
  private UserAccessLevel accessLevel;
  private String firstName;
  private String lastName;
  private String login;
  private final Date today = new Date();
  private UserPreferences preferences;

  public UserImpl(String userId) {
    this.id = userId;
    this.firstName = "";
    this.lastName = "";
    this.login = "";
    this.state = UserState.VALID;
    this.accessLevel = UserAccessLevel.USER;
    preferences = new UserPreferences();
    preferences.setLanguage("fr");
    preferences.setZoneId(ZoneId.of("Europe/Paris"));
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDomainId() {
    return "0";
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
    return "";
  }

  @Override
  public Date getCreationDate() {
    return today;
  }

  @Override
  public Date getSaveDate() {
    return today;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public Date getStateSaveDate() {
    return today;
  }

  @Override
  public boolean isFullyDefined() {
    return false;
  }

  @Override
  public UserAccessLevel getAccessLevel() {
    return accessLevel;
  }

  @Override
  public boolean isSystem() {
    return id.equals(SYSTEM_ID);
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  public boolean isBlanked() {
    return false;
  }

  @Override
  public boolean isAccessAdmin() {
    return accessLevel == UserAccessLevel.ADMINISTRATOR;
  }

  @Override
  public boolean isAccessDomainManager() {
    return false;
  }

  @Override
  public boolean isAccessPdcManager() {
    return false;
  }

  @Override
  public boolean isAccessUser() {
    return accessLevel == UserAccessLevel.USER;
  }

  @Override
  public boolean isAccessGuest() {
    return false;
  }

  @Override
  public boolean isAccessUnknown() {
    return false;
  }

  @Override
  public boolean isPlayingAdminRole(String instanceId) {
    return false;
  }

  @Override
  public UserState getState() {
    return state;
  }

  @Override
  public boolean isActivatedState() {
    return true;
  }

  @Override
  public boolean isValidState() {
    return true;
  }

  @Override
  public boolean isDeletedState() {
    return false;
  }

  @Override
  public boolean isRemovedState() {
    return false;
  }

  @Override
  public boolean isBlockedState() {
    return false;
  }

  @Override
  public boolean isDeactivatedState() {
    return false;
  }

  @Override
  public boolean isExpiredState() {
    return false;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public UserPreferences getUserPreferences() {
    return preferences;
  }

  public void setUserPreferences(UserPreferences preferences) {
    this.preferences = preferences;
  }

  @Override
  public String getAvatar() {
    return "";
  }

  @Override
  public String getSmallAvatar() {
    return "";
  }

  @Override
  public String getStatus() {
    return "";
  }

  @Override
  public long getDurationOfCurrentSession() {
    return 0;
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
  public int compareTo(User o) {
    return id.compareTo(o.getId());
  }

  public static class Builder {

    private final UserImpl user;

    public Builder(String userId) {
      user = new UserImpl(userId);
    }

    public Builder setFirstName(String firstName) {
      user.setFirstName(firstName);
      return this;
    }

    public Builder setLastName(String lastName) {
      user.setLastName(lastName);
      return this;
    }

    public Builder setLogin(String login) {
      user.setLogin(login);
      return this;
    }

    public Builder setAccessLevel(UserAccessLevel level) {
      user.setAccessLevel(level);
      return this;
    }

    public Builder setPreferences(UserPreferences preferences) {
      user.setUserPreferences(preferences);
      return this;
    }

    public User build() {
      return user;
    }
  }

  private void setAccessLevel(UserAccessLevel level) {
    this.accessLevel = level;
  }

  private void setLogin(String login) {
    this.login = login;
  }

  private void setLastName(String lastName) {
    this.lastName = lastName;
  }

  private void setFirstName(String firstName) {
    this.firstName = firstName;
  }
}
  