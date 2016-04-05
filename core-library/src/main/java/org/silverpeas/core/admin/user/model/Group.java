/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.admin.service.OrganizationController;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class Group implements Serializable, Comparable<Group> {

  private static final long serialVersionUID = 4430574302630237352L;
  private String id = null;
  private String specificId = null;
  private String domainId = null;
  private String superGroupId = null;
  private String name = "";
  private String description = "";
  private String rule = null;
  private String[] userIds = ArrayUtil.EMPTY_STRING_ARRAY;

  private int nbUsers = -1;
  private int nbTotalUsers = -1;

  /**
   * Gets the group with the specified unique identifier.
   * @param id the unique identifier of the group to get.
   * @return the group with the specified unique identifier or null if no such group exists.
   */
  public static Group getById(String id) {
    return getOrganisationController().getGroup(id);
  }

  /**
   * Gets all root groups available in Silverpeas, whatever their domain.
   * @return a list with all the groups in the Silverpeas portal.
   */
  public static List<Group> getAllRoots() {
    return Arrays.asList(getOrganisationController().getAllRootGroups());
  }

  /**
   * Gets all root groups available in the specified domain in Silverpeas.
   * @param domainId the unique identifier of the domain to which the root groups belong.
   * @return a list with all the root user groups in the specified domain.
   */
  public static List<Group> getAllRootsInDomain(String domainId) {
    return Arrays.asList(getOrganisationController().getAllRootGroupsInDomain(domainId));
  }

  /**
   * Constructor
   */
  public Group() {

  }

  public Group(Group toClone) {
    id = toClone.id;
    specificId = toClone.specificId;
    domainId = toClone.domainId;
    superGroupId = toClone.superGroupId;
    name = toClone.name;
    description = toClone.description;
    userIds = toClone.userIds;
    rule = toClone.rule;
  }

  /**
   * Get the group id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the group id
   */
  public void setId(String newId) {
    this.id = newId;
  }

  /**
   * Get the group specific id
   */
  public String getSpecificId() {
    return specificId;
  }

  /**
   * Set the group specific id
   */
  public void setSpecificId(String newSpecificId) {
    this.specificId = newSpecificId;
  }

  /**
   * Get the domain id where the group is stored
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Set the domain id where the group is stored
   */
  public void setDomainId(String newDomainId) {
    this.domainId = newDomainId;
  }

  /**
   * Get the father group id
   */
  public String getSuperGroupId() {
    return superGroupId;
  }

  /**
   * Set the father group id
   */
  public void setSuperGroupId(String newSuperGroupId) {
    this.superGroupId = newSuperGroupId;
  }

  /**
   * Get the group name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the group name
   */
  public void setName(String newName) {
    this.name = newName;
  }

  /**
   * Get the group description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the group description
   */
  public void setDescription(String newDescription) {
    if (newDescription != null) {
      this.description = newDescription;
    } else {
      this.description = "";
    }
  }

  /**
   * Set the list of users in the group
   */
  public void setUserIds(String[] sUserIds) {
    userIds = ArrayUtil.nullToEmpty(sUserIds);
  }

  /**
   * Get the list of users in the group
   */
  public String[] getUserIds() {
    return userIds;
  }

  /**
   * Trace the group's values
   */
  public void traceGroup() {
    int i;








    for (i = 0; i < userIds.length; i++) {

    }
  }

  @Override
  public int compareTo(Group o) {
    return (getName().toLowerCase()).compareTo(o.getName().toLowerCase());
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public boolean isSynchronized() {
    return (rule != null && rule.trim().length() > 0);
  }

  /**
   * Gets the number of direct users in this group; the users from its subgroups aren't counted. To
   * count also the users in its subgroups, please use the
   * {@code org.silverpeas.core.admin.user.model.Group#getTotalNbUsers} method instead.
   * @return the number of direct users.
   */
  public int getNbUsers() {
    if (nbUsers == -1) {
      return getUserIds().length;
    }
    return nbUsers;
  }

  /**
   * Gets the total number of users in this group and in its subgroups. Users that are in several
   * groups are counted only once.
   * </p>
   * Depending on the requester, the total number of users can omit some users by their state
   * (usually the users whose their account is deactivated). By default, all the users whose
   * the account is deleted aren't taken into account.
   * @return the total number of distinct users in its group and subgroups.
   */
  public int getTotalNbUsers() {
    if (nbTotalUsers < 0) {
      nbTotalUsers = getOrganisationController().getAllSubUsersNumber(getId());
    }
    return nbTotalUsers;
  }

  public void setTotalNbUsers(int nbUsers) {
    this.nbTotalUsers = nbUsers;
  }

  protected static OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  /**
   * Is this group is a root one?
   * A root group is a group that has no father group.
   * @return true if this group is a root one, false otherwise.
   */
  public boolean isRoot() {
    return !isDefined(this.superGroupId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Group other = (Group) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.specificId == null) ? (other.specificId != null)
            : !this.specificId.equals(other.specificId)) {
      return false;
    }
    if ((this.domainId == null) ? (other.domainId != null) : !this.domainId.equals(other.domainId)) {
      return false;
    }
    if ((this.superGroupId == null) ? (other.superGroupId != null)
            : !this.superGroupId.equals(other.superGroupId)) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null)
            : !this.description.equals(other.description)) {
      return false;
    }
    if ((this.rule == null) ? (other.rule != null) : !this.rule.equals(other.rule)) {
      return false;
    }
    if (this.getNbUsers() != other.getNbUsers()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 97 * hash + (this.specificId != null ? this.specificId.hashCode() : 0);
    hash = 97 * hash + (this.domainId != null ? this.domainId.hashCode() : 0);
    hash = 97 * hash + (this.superGroupId != null ? this.superGroupId.hashCode() : 0);
    hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 97 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 97 * hash + (this.rule != null ? this.rule.hashCode() : 0);
    hash = 97 * hash + this.nbUsers;
    return hash;
  }

  /**
   * Gets the direct subgroups of this user group.
   * @return a list with its direct subgroups. If this group hasn't children group, then the
   * returned list is empty.
   */
  public List<? extends Group> getSubGroups() {
    return Arrays.asList(getOrganisationController().getAllSubGroups(getId()));
  }

  /**
   * Gets the detail about all the users that are in this group (and in the subgroups of this group).
   * @return a list of all the user details in this group.
   */
  public List<? extends UserDetail> getAllUsers() {
    return Arrays.asList(getOrganisationController().getAllUsersOfGroup(getId()));
  }
}
