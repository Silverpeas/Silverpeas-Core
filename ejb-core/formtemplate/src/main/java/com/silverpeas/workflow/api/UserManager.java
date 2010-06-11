/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.user.UserSettings;

/**
 * The workflow engine services relate to user management.
 */
public interface UserManager {
  /**
   * Returns the user with the given userId
   * @return the user with the given userId.
   * @throw WorkflowException if the userId is unknown.
   */
  public User getUser(String userId) throws WorkflowException;

  /**
   * Make a User[] from a userIds' String[].
   * @throw WorkflowException if a userId is unknown.
   */
  public User[] getUsers(String[] userIds) throws WorkflowException;

  /**
   * Returns all the roles of a given user relative to a processModel.
   */
  public String[] getRoleNames(User user, String processModelId)
      throws WorkflowException;

  /**
   * Returns all the users having a given role relative to a processModel.
   */
  public User[] getUsersInRole(String roleName, String processModelId)
      throws WorkflowException;
  
  public User[] getUsersInGroup(String groupId);

  /**
   * returns all the known info for an user; Each returned value can be used as a parameter to the
   * User method getInfo().
   */
  public String[] getUserInfoNames();

  /**
   * Get a user from a given user and relation
   * @param user reference user
   * @param relation relation between given user and searched user
   * @param peasId the id of workflow peas associated to that information
   * @return the user that has the given relation with given user
   */
  public User getRelatedUser(User user, String relation, String peasId)
      throws WorkflowException;

  /**
   * Get the user settings in database The full list of information is described in the process
   * model
   * @param userId the user Id
   * @param peasId the id of workflow peas associated to that information
   * @return UserSettings
   * @see ProcessModel
   */
  public UserSettings getUserSettings(String userId, String peasId)
      throws WorkflowException;

  public void resetUserSettings(String userId, String peasId)
      throws WorkflowException;

  /**
   * Get an empty user settings in database The full list of information is described in the process
   * model
   * @param userId the user Id
   * @param peasId the id of workflow peas associated to that information
   * @return UserSettings
   * @see ProcessModel
   */
  public UserSettings getEmptyUserSettings(String userId, String peasId);
}
