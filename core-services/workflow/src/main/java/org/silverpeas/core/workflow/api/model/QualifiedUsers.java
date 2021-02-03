/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import org.silverpeas.core.workflow.api.WorkflowException;

import java.util.Iterator;

/**
 * Interface describing a representation of one of the following elements of a Process Model:
 * <ul>
 * <li>&lt;workingUsers&gt;</li>
 * <li>&lt;interestedUsers&gt;</li>
 * </ul>
 */
public interface QualifiedUsers {
  /**
   * Get the userInRoles
   * @return the userInRoles as an array
   */
  UserInRole[] getUserInRoles();

  /**
   * Iterate through the UserInRole objects
   * @return an iterator
   */
  Iterator<UserInRole> iterateUserInRole();

  /**
   * Create a new UserInRole
   * @return an object implementing UserInRole
   */
  UserInRole createUserInRole();

  /**
   * Add a UserInRole to the collection
   * @param user to be added
   */
  void addUserInRole(UserInRole user);

  /**
   * Get the userInRoles
   * @param strRoleName
   * @return the userInRoles as a Vector
   */
  UserInRole getUserInRole(String strRoleName);

  /**
   * Remove all UserInRole from the collection
   */
  void removeUserInRoles();

  /**
   * Get the participants and related users
   * @return the participants and related users as an array
   */
  RelatedUser[] getRelatedUsers();

  /**
   * Get the related user equivalent to the one specified
   * @param relatedUser the reference to look for
   * @return the related users as referenced or <code>null</code>
   */
  RelatedUser getRelatedUser(RelatedUser relatedUser);

  /**
   * Iterate through the RelatedUser objects
   * @return an iterator
   */
  Iterator<RelatedUser> iterateRelatedUser();

  /**
   * Add a RelatedUser to the collection
   * @param user to be added
   */
  void addRelatedUser(RelatedUser user);

  /**
   * Remove a RelatedUser from the collection
   * @param reference the reference of the RelatedUser to be removed
   * @throws WorkflowException when something goes wrong
   */
  void removeRelatedUser(RelatedUser reference) throws WorkflowException;

  /**
   * Get the related groups
   * @return the related groups as an array
   */
  RelatedGroup[] getRelatedGroups();

  /**
   * Get the role to which the related groups will be affected by default
   * @return the role name
   */
  String getRole();

  /**
   * Set the role to which the related user will be affected
   * @param role role as a String
   */
  void setRole(String role);

  /**
   * Get the message associated to the related users (only used for notification)
   * @return the message
   */
  String getMessage();

  /**
   * Set the message associated to the related users (only used for notification)
   * @param message message as a String
   */
  void setMessage(String message);

  /**
   * Get the user id used as sender for message.
   * @return
   */
  String getSenderId();

  /**
   * Get the linkDisabled status associated to the related users (only used for notification)
   * @return the status of linkDisabled
   */
  Boolean getLinkDisabled();

  /**
   * Set the linkDisabled status associated to the related users (only used for notification)
   * @param linkDisabled status as a boolean
   */
  void setLinkDisabled(Boolean linkDisabled);
}