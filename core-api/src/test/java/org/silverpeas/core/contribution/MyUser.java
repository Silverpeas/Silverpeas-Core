/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;

import java.time.ZoneId;
import java.util.Date;

/**
 * A user dedicated to unit tests.
 * @author mmoquillon
 */
public class MyUser implements User {

  private final String id;
  private Date creationDate;
  private String domainId;
  private final String firstName;
  private final String lastName;

  public MyUser(final String id, final String firstName, final String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public MyUser inDomainById(final String domainId) {
    this.domainId = domainId;
    this.creationDate = new Date();
    return this;
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
    return firstName + "." + lastName;
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
  public String geteMail() {
    return firstName + "." + lastName + "@silverpeas.io";
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Date getSaveDate() {
    return creationDate;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public Date getStateSaveDate() {
    return creationDate;
  }

  @Override
  public boolean isFullyDefined() {
    return false;
  }

  @Override
  public UserAccessLevel getAccessLevel() {
    return UserAccessLevel.USER;
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
    return getAccessLevel() == UserAccessLevel.ADMINISTRATOR;
  }

  @Override
  public boolean isAccessDomainManager() {
    return getAccessLevel() == UserAccessLevel.DOMAIN_ADMINISTRATOR;
  }

  @Override
  public boolean isAccessSpaceManager() {
    return getAccessLevel() == UserAccessLevel.SPACE_ADMINISTRATOR;
  }

  @Override
  public boolean isAccessPdcManager() {
    return getAccessLevel() == UserAccessLevel.PDC_MANAGER;
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
  public UserState getState() {
    return UserState.VALID;
  }

  @Override
  public boolean isActivatedState() {
    return getState() != UserState.DEACTIVATED;
  }

  @Override
  public boolean isValidState() {
    return getState() == UserState.VALID;
  }

  @Override
  public boolean isDeletedState() {
    return getState() == UserState.DELETED;
  }

  @Override
  public boolean isRemovedState() {
    return getState() == UserState.REMOVED;
  }

  @Override
  public boolean isBlockedState() {
    return getState() == UserState.BLOCKED;
  }

  @Override
  public boolean isDeactivatedState() {
    return getState() == UserState.DEACTIVATED;
  }

  @Override
  public boolean isExpiredState() {
    return getState() == UserState.EXPIRED;
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public UserPreferences getUserPreferences() {
    return new UserPreferences(getId(), "fr", ZoneId.of("Europe/Paris"), "Initial", "", false, true,
        true, UserMenuDisplay.DEFAULT);
  }

  @Override
  public String getAvatar() {
    return "avatar-" + id + ".jpg";
  }

  @Override
  public String getSmallAvatar() {
    return "avatar-small-" + id + ".jpg";
  }

  @Override
  public String getStatus() {
    return "Gogol the First";
  }

  @Override
  public String getDurationOfCurrentSession() {
    return "unlimited!";
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
  public int compareTo(final User o) {
    return 0;
  }
}
  