/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;
import org.silverpeas.core.workflow.engine.exception.UnknownUserException;

import javax.inject.Singleton;
import java.util.stream.Stream;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;

/**
 * A UserManager implementation built upon the silverpeas user management system.
 */
@Singleton
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

  /**
   * Returns all the users having a given role relative to a processModel.
   * @param roleName
   * @param modelId
   * @return
   * @throws WorkflowException
   */
  @Override
  public User[] getUsersInRole(String roleName, String modelId) throws WorkflowException {
    UserDetail[] userDetails;
    try {
      // the modelId is the peasId.
      final ComponentInst peas = getAdminService().getComponentInst(modelId);
      userDetails = getOrganisationController().getUsers(peas.getDomainFatherId(), modelId, roleName);
    } catch (AdminException e) {
      throw new WorkflowException("UserManagerImpl.getUserInRole",
          "workflowEngine.EXP_UNKNOWN_ROLE", e);
    }
    if (userDetails == null) {
      userDetails = new UserDetail[0];
    }
    return asUsers(userDetails);
  }

  @Override
  public User[] getUsersInGroup(String groupId) {
    final UserDetail[] userDetails = getOrganisationController().getAllUsersOfGroup(groupId);
    return asUsers(userDetails);
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
   * Make a User[] from a UserDetail[].
   */
  private User[] asUsers(UserDetail[] userDetails) {
    return Stream.of(userDetails).map(UserImpl::new).toArray(User[]::new);
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
}