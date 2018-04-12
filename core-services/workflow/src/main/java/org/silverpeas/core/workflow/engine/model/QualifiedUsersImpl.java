/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.RelatedGroup;
import org.silverpeas.core.workflow.api.model.RelatedUser;
import org.silverpeas.core.workflow.api.model.UserInRole;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Class implementing the representation of the &lt;allowedUsers&gt;, &lt;workingUsers&gt;,
 * &lt;notifiedUsers&gt; and &lt;interestedUsers&gt; elements of a Process Model.
 **/
@XmlAccessorType(XmlAccessType.NONE)
public class QualifiedUsersImpl implements QualifiedUsers, Serializable {

  private static final long serialVersionUID = -6137211965745730173L;
  @XmlElement(name = "userInRole", type = UserInRoleImpl.class)
  private List<UserInRole> userInRoleList;
  @XmlElement(name = "relatedUser", type = RelatedUserImpl.class)
  private List<RelatedUser> relatedUserList;
  @XmlElement(name = "relatedGroup", type = RelatedGroupImpl.class)
  private List<RelatedGroup> relatedGroupList;
  @XmlAttribute
  private String role;
  @XmlAttribute
  private String message;
  @XmlAttribute
  private String senderId;
  @XmlAttribute
  private boolean linkDisabled;

  /**
   * Constructor
   */
  public QualifiedUsersImpl() {
    userInRoleList = new Vector<>();
    relatedUserList = new Vector<>();
    relatedGroupList = new Vector<>();
  }

  /**
   * Get the userInRoles
   * @return the userInRoles as a Vector
   */
  public UserInRole getUserInRole(String strRoleName) {
    UserInRole userInRole = new UserInRoleImpl();
    userInRole.setRoleName(strRoleName);
    int idx = userInRoleList.indexOf(userInRole);

    if (idx >= 0) {
      return userInRoleList.get(idx);
    }
    return null;
  }

  /**
   * Get the userInRoles
   * @return the userInRoles as an array
   */
  @Override
  public UserInRole[] getUserInRoles() {
    return userInRoleList.toArray(new UserInRole[userInRoleList.size()]);
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#addUserInRole(com.silverpeas
   * .workflow.api.model.UserInRole)
   */
  @Override
  public void addUserInRole(UserInRole user) {
    userInRoleList.add(user);
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#createUserInRole()
   */
  @Override
  public UserInRole createUserInRole() {
    return new UserInRoleImpl();
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#iterateUserInRole()
   */
  @Override
  public Iterator<UserInRole> iterateUserInRole() {
    return userInRoleList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#removeUserInRoles()
   */
  @Override
  public void removeUserInRoles() {
    userInRoleList.clear();
  }

  /**
   * Get the participants and related users
   * @return the participants and related users as an array
   */
  @Override
  public RelatedUser[] getRelatedUsers() {
    return relatedUserList.toArray(new RelatedUser[relatedUserList.size()]);
  }

  /**
   * Get the related groups
   * @return the related groups as an array
   */
  @Override
  public RelatedGroup[] getRelatedGroups() {
    return relatedGroupList.toArray(new RelatedGroup[relatedUserList.size()]);
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#getRelatedUser(com.silverpeas
   * .workflow.api.model.RelatedUser)
   */
  @Override
  public RelatedUser getRelatedUser(RelatedUser relatedUser) {
    int idx = relatedUserList.indexOf(relatedUser);
    if (idx >= 0) {
      return relatedUserList.get(idx);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#addRelatedUser(com.silverpeas
   * .workflow.api.model.RelatedUser)
   */
  @Override
  public void addRelatedUser(RelatedUser user) {
    relatedUserList.add(user);
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#iterateRelatedUser()
   */
  @Override
  public Iterator<RelatedUser> iterateRelatedUser() {
    return relatedUserList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#removeRelatedUser(RelatedUser )
   */
  @Override
  public void removeRelatedUser(RelatedUser reference) throws WorkflowException {
    if (!relatedUserList.remove(reference)) {
      throw new WorkflowException("QualifiedUsersImpl.removeRelatedUser()",
          "workflowEngine.EX_RELATED_USER_NOT_FOUND", reference == null ? "<null>" : reference.
          getRelation() + ", " + reference.getRole());
    }
  }

  /**
   * Get the role to which the related user will be affected
   */
  @Override
  public String getRole() {
    return this.role;
  }

  /*
   * (non-Javadoc)
   * @see QualifiedUsers#setRole(java.lang.String)
   */
  @Override
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Get the message associated to the related users (only used for notification)
   */
  @Override
  public String getMessage() {
    return this.message;
  }

  /**
   * Set the message associated to the related users (only used for notification)
   * @param message message as a String
   */
  @Override
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the user id used as sender for message.
   */
  @Override
  public String getSenderId() {
    return senderId;
  }

  /**
   * Set the user id used as sender for message.
   * @param senderId the user id
   */
  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }
  

  /**
   * Get the linkDisabled status associated to the related users (only used for notification)
   */
  @Override
  public boolean getLinkDisabled() {
    return this.linkDisabled;
  }

  /**
   * Set the linkDisabled status associated to the related users (only used for notification)
   * @param linkDisabled status as a boolean
   */
  @Override
  public void setLinkDisabled(boolean linkDisabled) {
    this.linkDisabled = linkDisabled;
  }  
}