/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.personalization.UserPreferences;

import java.io.Serializable;
import java.util.Date;

/**
 * This interface represents simple data of a user.
 * @author Yohann Chastagnier
 */
public interface User extends Serializable, Comparable<User> {

  /**
   * Gets the detail about the specified user.
   * @param userId the unique identifier of the user to get.
   * @return the detail about the user with the specified identifier or null if no such user exists.
   */
  static User getById(String userId) {
    return UserProvider.get().getUser(userId);
  }

  /**
   * @see #isActivatedState()
   */
  static boolean isActivatedStateFor(String userId) {
    User user = getById(userId);
    return user != null && user.isActivatedState();
  }

  /**
   * Gets the current user behind a request of treatment processing.
   * @return the {@link User} instance of current requester.
   */
  static User getCurrentRequester() {
    return UserProvider.get().getCurrentRequester();
  }

  /**
   * The unique identifier of the user into Silverpeas.
   * @return identifier as string.
   */
  String getId();

  /**
   * Gets the user domain id.
   * @return user domain id as string.
   */
  String getDomainId();

  /**
   * Indicates if the user is restricted to access the resource in its own domain only.
   * @return true if it's restricted, false otherwise.
   */
  boolean isDomainRestricted();

  /**
   * Indicates if the user has admin account and is restricted to access the resource in its own
   * domain only.
   * @return true if it's restricted, false otherwise.
   */
  boolean isDomainAdminRestricted();

  /**
   * Gets the user login.
   * @return user login as string.
   */
  String getLogin();

  /**
   * Gets the last name of the user.
   * @return last name as string.
   */
  String getLastName();

  /**
   * Gets the first name of the user.
   * @return first name as string.
   */
  String getFirstName();

  /**
   * Gets the user email.
   * @return user email as string.
   */
  String geteMail();

  /**
   * Gets the date of the user creation.
   * @return creation date of the user as {@link Date}.
   */
  Date getCreationDate();

  /**
   * Gets the date of the last user save.
   * @return the date of the last user save as {@link Date}.
   */
  Date getSaveDate();

  /**
   * Gets the version of the user data. The number of time the entity has been saved in other
   * words.
   * @return the version of the last save as integer.
   */
  int getVersion();

  /**
   * Gets the last date of the last user save.
   * @return the date of last user state save (when it changes) as {@link Date}.
   */
  Date getStateSaveDate();

  /**
   * Indicates if the mandatory user data are defined.
   * @return true if all mandatory are defined, false otherwise.
   */
  boolean isFullyDefined();

  /**
   * Default first name and last name concatenation.
   * @return concatenation of first name and last name as string (without outer space).
   */
  default String getDisplayedName() {
    return (getFirstName() + " " + getLastName()).trim();
  }

  /**
   * Gets the user access level.
   * @return the access level as {@link UserAccessLevel}.
   */
  UserAccessLevel getAccessLevel();

  /**
   * Indicates if the user is an anonymous one.
   * @return true if anonymous, false otherwise.
   */
  boolean isAnonymous();

  /**
   * Indicates if the user has admin access on the platform.
   * @return true if admin access, false otherwise.
   */
  boolean isAccessAdmin();

  /**
   * Indicates if the user has domain manager access on the platform.
   * @return true if domain manager access, false otherwise.
   */
  boolean isAccessDomainManager();

  /**
   * Indicates if the user has space manager access on the platform.
   * @return true if space manager access, false otherwise.
   */
  boolean isAccessSpaceManager();

  /**
   * Indicates if the user has PDC manager access on the platform.
   * @return true if PDC manager access, false otherwise.
   */
  boolean isAccessPdcManager();

  /**
   * Indicates if the user has user access on the platform.
   * @return true if user access, false otherwise.
   */
  boolean isAccessUser();

  /**
   * Indicates if the user has guest access on the platform.
   * @return true if guest access, false otherwise.
   */
  boolean isAccessGuest();

  /**
   * Indicates if the user has unknown access on the platform.
   * @return true if unknown access, false otherwise.
   */
  boolean isAccessUnknown();

  /**
   * Please use {@link User#isValidState()} to retrieve user validity information.
   * Please use {@link User#isDeletedState()} to retrieve user deletion information.
   * Please use {@link User#isBlockedState()} to retrieve user blocked information.
   * Please use {@link User#isDeactivatedState()} to retrieve user deactivated information.
   * Please use {@link User#isExpiredState()} to retrieve user expiration information.
   * This method returns the stored state information but not the functional information.
   * @return the state of the user (account)
   */
  UserState getState();

  /**
   * This method indicates if the user is activated. The returned value is a combination of
   * following method call result :
   * <ul>
   * <li>not {@link #isAnonymous()}</li>
   * <li>and not {@link #isDeletedState()}</li>
   * <li>and not {@link #isDeactivatedState()}</li>
   * </ul>
   * @return true if activated state, false otherwise.
   */
  boolean isActivatedState();

  /**
   * This method is the only one able to indicate the user validity state. Please do not use
   * {@link User#getState()} to retrieve user validity information.
   * @return true if valid state, false otherwise.
   */
  boolean isValidState();

  /**
   * This method is the only one able to indicate the user deletion state. Please do not use
   * {@link User#getState()} to retrieve user deletion information.
   * @return true if deleted state, false otherwise.
   */
  boolean isDeletedState();

  /**
   * This method is the only one able to indicate the user blocked state. Please do not use
   * {@link User#getState()} to retrieve user blocked information.
   * @return true if blocked state, false otherwise.
   */
  boolean isBlockedState();

  /**
   * This method is the only one able to indicate the user deactivated state. Please do not use
   * {@link User#getState()} to retrieve user deactivated information.
   * @return true if deactivated state, false otherwise.
   */
  boolean isDeactivatedState();

  /**
   * This method is the only one able to indicate the user expiration state. Please do not use
   * {@link User#getState()} to retrieve user expiration information.
   * @return true if user is expired.
   */
  boolean isExpiredState();

  /**
   * Indicates if the user is currently connected to Silverpeas.
   * @return true if the user is currently connected to Silverpeas, false otherwise.
   */
  boolean isConnected();

  /**
   * Gets the preferences of this user.
   * @return the user preferences.
   */
  UserPreferences getUserPreferences();

  /**
   * Gets the avatar URL of the user.
   * @return the avatar URL as string.
   */
  String getAvatar();

  /**
   * Gets the URL of a smaller version of avatar than the one provided by {@link #getAvatar()}
   * method.
   * @return the small avatar URL as string.
   */
  String getSmallAvatar();

  /**
   * Gets the current status filled by the user itself.
   * @return current status as string.
   */
  String getStatus();

  /**
   * Gets the duration of the current user session since its last registered login date.
   * @return the formatted duration of the current user session, or empty string if the user is not
   * connected.
   */
  String getDurationOfCurrentSession();
}
