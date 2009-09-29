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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.QualifiedUsers;
import com.silverpeas.workflow.api.model.RelatedUser;
import com.silverpeas.workflow.api.model.UserInRole;

/**
 * Class implementing the representation of the &lt;allowedUsers&gt;,
 * &lt;workingUsers&gt;, &lt;notifiedUsers&gt; and &lt;interestedUsers&gt;
 * elements of a Process Model.
 **/
public class QualifiedUsersImpl implements QualifiedUsers, Serializable {
  private Vector userInRoleList;
  private Vector relatedUserList;
  private String role;
  private String message;
  private String senderId;

  /**
   * Constructor
   */
  public QualifiedUsersImpl() {
    userInRoleList = new Vector();
    relatedUserList = new Vector();
  }

  /**
   * Get the userInRoles
   * 
   * @return the userInRoles as a Vector
   */
  public UserInRole getUserInRole(String strRoleName) {
    UserInRole userInRole = new UserInRoleImpl();
    int idx;

    userInRole.setRoleName(strRoleName);
    idx = userInRoleList.indexOf(userInRole);

    if (idx >= 0)
      return (UserInRole) userInRoleList.get(idx);
    else
      return null;
  }

  /**
   * Get the userInRoles
   * 
   * @return the userInRoles as an array
   */
  public UserInRole[] getUserInRoles() {
    return (UserInRole[]) userInRoleList.toArray(new UserInRoleImpl[0]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.QualifiedUsers#addUserInRole(com.silverpeas
   * .workflow.api.model.UserInRole)
   */
  public void addUserInRole(UserInRole user) {
    userInRoleList.add(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.QualifiedUsers#createUserInRole()
   */
  public UserInRole createUserInRole() {
    return new UserInRoleImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.QualifiedUsers#iterateUserInRole()
   */
  public Iterator iterateUserInRole() {
    return userInRoleList.iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.QualifiedUsers#removeUserInRoles()
   */
  public void removeUserInRoles() {
    userInRoleList.clear();
  }

  /**
   * Get the participants and related users
   * 
   * @return the participants and related users as an array
   */
  public RelatedUser[] getRelatedUsers() {
    return (RelatedUser[]) relatedUserList.toArray(new RelatedUser[0]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.QualifiedUsers#getRelatedUser(com.silverpeas
   * .workflow.api.model.RelatedUser)
   */
  public RelatedUser getRelatedUser(RelatedUser relatedUser) {
    int idx = relatedUserList.indexOf(relatedUser);

    if (idx >= 0)
      return (RelatedUser) relatedUserList.get(idx);
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.QualifiedUsers#addRelatedUser(com.silverpeas
   * .workflow.api.model.RelatedUser)
   */
  public void addRelatedUser(RelatedUser user) {
    relatedUserList.add(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.QualifiedUsers#createRelatedUser()
   */
  public RelatedUser createRelatedUser() {
    return new RelatedUserImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.QualifiedUsers#iterateRelatedUser()
   */
  public Iterator iterateRelatedUser() {
    return relatedUserList.iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.QualifiedUsers#removeRelatedUser(RelatedUser
   * )
   */
  public void removeRelatedUser(RelatedUser reference) throws WorkflowException {
    if (!relatedUserList.remove(reference))
      throw new WorkflowException("QualifiedUsersImpl.removeRelatedUser()", //$NON-NLS-1$
          "workflowEngine.EX_RELATED_USER_NOT_FOUND", // $NON-NLS-1$
          reference == null ? "<null>" //$NON-NLS-1$
              : reference.getRelation() + ", " + reference.getRole());
  }

  /**
   * Get the role to which the related user will be affected
   */
  public String getRole() {
    return this.role;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.QualifiedUsers#setRole(java.lang.String)
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Get the message associated to the related users (only used for
   * notification)
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Set the message associated to the related users (only used for
   * notification)
   * 
   * @param message
   *          message as a String
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the user id used as sender for message.
   */
  public String getSenderId() {
    return senderId;
  }

  /**
   * Set the user id used as sender for message.
   * 
   * @param senderId
   *          the user id
   */
  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }
}