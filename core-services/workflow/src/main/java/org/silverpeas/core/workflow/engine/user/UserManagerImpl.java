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
package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;
import org.silverpeas.core.workflow.engine.exception.UnknownUserException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.SUPERVISOR;

/**
 * A UserManager implementation built upon the silverpeas user management system.
 */
@Service
public class UserManagerImpl implements UserManager {

  /**
   * Returns the user with the given userId
   * @param userId
   * @return the user with the given userId.
   * @throws WorkflowException if the userId is unknown.
   */
  @Override
  public User getUser(String userId) throws WorkflowException {
    return new UserImpl(getUserDetail(userId));
  }

  /**
   * Make a User[] from a userIds' String[].
   * @param userIds
   * @return
   * @throws WorkflowException
   * @throw WorkflowException if a userId is unknown.
   */
  @Override
  public User[] getUsers(String[] userIds) throws WorkflowException {
    final User[] users = new User[userIds.length];
    for (int i = 0; i < userIds.length; i++) {
      users[i] = getUser(userIds[i]);
    }
    return users;
  }

  @Override
  public User[] getUsersInRole(String roleName, String modelId) {
    // the modelId is the peasId.
    final boolean includeRemovedUsers = !SUPERVISOR.getName().equals(roleName) &&
        existsAtLeastOneValidReplacementAtNow(roleName, modelId);
    return Stream
        .of(getOrganisationController()
            .getUsersIdsByRoleNames(modelId, List.of(roleName), includeRemovedUsers))
        .map(UserDetail::getById)
        .map(UserImpl::new)
        .toArray(User[]::new);
  }

  @Override
  public User[] getUsersInGroup(String groupId, final String modelId) {
    final UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria().onGroupIds(groupId);
    if (existsAtLeastOneValidReplacementAtNow(modelId)) {
      criteria.includeRemovedUsers();
    } else {
      criteria.onUserStatesToExclude(UserState.REMOVED);
    }
    return getOrganisationController().searchUsers(criteria)
        .stream()
        .map(UserDetail.class::cast)
        .map(UserImpl::new)
        .toArray(User[]::new);
  }

  /**
   * returns the userDetail of a userId.
   */
  private UserDetail getUserDetail(String userId) throws WorkflowException {
    final UserDetail userDetail =  UserDetail.getById(userId);
    if (userDetail == null) {
      throw new UnknownUserException("UserManagerImpl.getUserDetail", userId);
    }
    return userDetail;
  }

  /**
   * Get a user from a given user and relation
   * @param user reference user
   * @param relation relation between given user and searched user
   * @param peasId the id of workflow peas associated to that information
   * @return the user that has the given relation with given user
   * @throws WorkflowException
   */
  @Override
  public User getRelatedUser(User user, String relation, String peasId)
      throws WorkflowException {
    final UserSettings settings = UserSettingsService.get().get(user.getUserId(), peasId);
    if (settings == null) {
      throw new WorkflowException("UserManagerImpl.getRelatedUser",
          "workflowEngine.EXP_NO_USER_SETTING", "user id : " + user.getUserId());
    }
    final UserInfo info = settings.getUserInfo(relation);
    if (info == null) {
      throw new WorkflowException("UserManagerImpl.getRelatedUser",
          "workflowEngine.EXP_USERINFO_NOT_FOUND", "user id : "
          + user.getUserId() + ", info name : " + relation);
    }
    return getUser(info.getValue());
  }

  private static boolean existsAtLeastOneValidReplacementAtNow(final String roleName,
      final String modelId) {
    return Replacement.getAll(modelId)
        .stream()
        .filterCurrentAt(LocalDate.now())
        .filterOnAtLeastOneRole(roleName)
        .findFirst()
        .isPresent();
  }

  private static boolean existsAtLeastOneValidReplacementAtNow(final String modelId) {
    return Replacement.getAll(modelId)
        .stream()
        .filterCurrentAt(LocalDate.now())
        .findFirst()
        .isPresent();
  }
}