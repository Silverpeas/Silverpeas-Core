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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.ArrayUtil;

import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class GroupDetail implements Group {

  private static final long serialVersionUID = 4430574302630237352L;
  private String id = null;
  private String specificId = null;
  private String domainId = null;
  private String superGroupId = null;
  private String name = "";
  private String description = "";
  private String rule = null;
  private String[] userIds = ArrayUtil.emptyStringArray();

  private int nbUsers = -1;
  private int nbTotalUsers = -1;

  /**
   * Constructs an empty group detail.
   */
  public GroupDetail() {
   // an empty group detail
  }

  /**
   * Constructs a group detail from the specified one. The given group detail is cloned to the
   * new group detail.
   * @param toClone a group to clone.
   */
  public GroupDetail(GroupDetail toClone) {
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
  @Override
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

  @Override
  public String getDomainId() {
    return domainId;
  }

  /**
   * Set the domain id where the group is stored
   */
  public void setDomainId(String newDomainId) {
    this.domainId = newDomainId;
  }

  @Override
  public String getSuperGroupId() {
    return superGroupId;
  }

  /**
   * Set the father group id
   */
  public void setSuperGroupId(String newSuperGroupId) {
    this.superGroupId = newSuperGroupId;
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Set the group name
   */
  public void setName(String newName) {
    this.name = newName;
  }

  @Override
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

  @Override
  public String[] getUserIds() {
    return userIds;
  }

  @Override
  public int compareTo(Group o) {
    return Collator.getInstance()
        .compare(getName().toLowerCase(), o.getName().toLowerCase());
  }

  @Override
  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  @Override
  public boolean isSynchronized() {
    return (rule != null && rule.trim().length() > 0);
  }

  @Override
  public int getNbUsers() {
    if (nbUsers == -1) {
      return getUserIds().length;
    }
    return nbUsers;
  }

  @Override
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

  @Override
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
    final GroupDetail other = (GroupDetail) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.specificId, other.specificId)) {
      return false;
    }
    if (!Objects.equals(this.domainId, other.domainId)) {
      return false;
    }
    if (!Objects.equals(this.superGroupId, other.superGroupId)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.description, other.description)) {
      return false;
    }
    if (!Objects.equals(this.rule, other.rule)) {
      return false;
    }

    return this.getNbUsers() == other.getNbUsers();
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

  @Override
  public List<Group> getSubGroups() {
    return Arrays.asList(getOrganisationController().getAllSubGroups(getId()));
  }

  @Override
  public List<User> getAllUsers() {
    return Arrays.asList(getOrganisationController().getAllUsersOfGroup(getId()));
  }
}
