/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
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
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;

import org.silverpeas.core.workflow.api.WorkflowException;

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
  public UserInRole[] getUserInRoles();

  /**
   * Iterate through the UserInRole objects
   * @return an iterator
   */
  public Iterator<UserInRole> iterateUserInRole();

  /**
   * Create a new UserInRole
   * @return an object implementing UserInRole
   */
  public UserInRole createUserInRole();

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
  public UserInRole getUserInRole(String strRoleName);

  /**
   * Remove all UserInRole from the collection
   */
  void removeUserInRoles();

  /**
   * Get the participants and related users
   * @return the participants and related users as an array
   */
  public RelatedUser[] getRelatedUsers();

  /**
   * Get the related user equivalent to the one specified
   * @param relatedUser the reference to look for
   * @return the related users as referenced or <code>null</code>
   */
  public RelatedUser getRelatedUser(RelatedUser relatedUser);

  /**
   * Iterate through the RelatedUser objects
   * @return an iterator
   */
  public Iterator<RelatedUser> iterateRelatedUser();

  /**
   * Create a new RelatedUser
   * @return an object implementing RelatedUser
   */
  public RelatedUser createRelatedUser();

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
  public RelatedGroup[] getRelatedGroups();

  /**
   * Get the related group equivalent to the one specified
   * @param relatedGroup the reference to look for
   * @return the related groups as referenced or <code>null</code>
   */
  public RelatedGroup getRelatedGroup(RelatedGroup relatedGroup);

  /**
   * Iterate through the RelatedGroup objects
   * @return an iterator
   */
  public Iterator<RelatedGroup> iterateRelatedGroup();

  /**
   * Create a new RelatedGroup
   * @return an object implementing RelatedGroup
   */
  public RelatedGroup createRelatedGroup();

  /**
   * Add a RelatedGroup to the collection
   * @param group to be added
   */
  void addRelatedGroup(RelatedGroup group);

  /**
   * Remove a RelatedGroup from the collection
   * @param reference the reference of the RelatedGroup to be removed
   * @throws WorkflowException when something goes wrong
   */
  void removeRelatedGroup(RelatedGroup reference) throws WorkflowException;

  /**
   * Get the role to which the related groups will be affected by default
   * @return the role name
   */
  public String getRole();

  /**
   * Set the role to which the related user will be affected
   * @param role role as a String
   */
  public void setRole(String role);

  /**
   * Get the message associated to the related users (only used for notification)
   * @return the message
   */
  public String getMessage();

  /**
   * Set the message associated to the related users (only used for notification)
   * @param message message as a String
   */
  public void setMessage(String message);

  /**
   * Get the user id used as sender for message.
   * @return
   */
  public String getSenderId();
}