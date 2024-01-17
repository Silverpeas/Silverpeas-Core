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
package org.silverpeas.core.workflow.api;

import org.silverpeas.core.workflow.api.user.User;

/**
 * The workflow engine services relate to user management.
 */
public interface UserManager {
  /**
   * Returns the user with the given userId
   * @return the user with the given userId.
   * @throws WorkflowException if the userId is unknown.
   */
  User getUser(String userId) throws WorkflowException;

  /**
   * Make a User[] from a userIds' String[].
   * @throws WorkflowException if a userId is unknown.
   */
  User[] getUsers(String[] userIds) throws WorkflowException;

  /**
   * Returns all the users having a given role relative to a processModel.
   * <p>
   *   Users in removed states are taken into account if it exists a valid replacement at the
   *   instant of method call for the given role name on the process represented by modelId
   *   parameter.
   * </p>
   * @param roleName the role name to check.
   * @param modelId the identifier of the model which is the identifier of the component instance
   * id.
   * @return an array of {@link User} instances corresponding to given parameters.
   */
  User[] getUsersInRole(String roleName, String modelId);

  /**
   * Returns all the users into given group and its sub groups.
   * <p>
   *   Users in removed states are taken into account if it exists a valid replacement at the
   *   instant of method call on the process represented by modelId parameter.
   * </p>
   * @param modelId the identifier of the model which is the identifier of the component instance
   * id.
   * @return an array of {@link User} instances corresponding to given parameters.
   */
  User[] getUsersInGroup(String groupId, final String modelId);

  /**
   * Get a user from a given user and relation
   * @param user reference user
   * @param relation relation between given user and searched user
   * @param peasId the id of workflow peas associated to that information
   * @return the user that has the given relation with given user
   */
  User getRelatedUser(User user, String relation, String peasId) throws WorkflowException;
}
