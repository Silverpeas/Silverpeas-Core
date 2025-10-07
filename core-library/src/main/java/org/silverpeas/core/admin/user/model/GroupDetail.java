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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.util.ArrayUtil;

import java.text.Collator;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

public class GroupDetail implements Group {

  private static final long serialVersionUID = 4430574302630237352L;
  private String instanceId = null;
  private String id = null;
  private String specificId = null;
  private String domainId = null;
  private String superGroupId = null;
  private String name = "";
  private String description = "";
  private String rule = null;
  private String[] userIds = ArrayUtil.emptyStringArray();
  private Date creationDate = null;
  private Date saveDate = null;
  private GroupState state = GroupState.from(null);
  private Date stateSaveDate = null;

  private int nbTotalUsers = -1;

  /**
   * Constructs an empty group detail.
   */
  public GroupDetail() {
    // an empty group detail
  }

  /**
   * Constructs a group detail from the specified one. The given group detail is cloned to the new
   * group detail.
   * <p>
   * BE CAREFULL: {@link #nbTotalUsers} fields are not copied.
   * </p>
   *
   * @param toClone a group to clone.
   */
  public GroupDetail(GroupDetail toClone) {
    id = toClone.id;
    specificId = toClone.specificId;
    domainId = toClone.domainId;
    instanceId = toClone.instanceId;
    superGroupId = toClone.superGroupId;
    name = toClone.name;
    description = toClone.description;
    userIds = toClone.userIds;
    rule = toClone.rule;
    creationDate = toClone.getCreationDate();
    saveDate = toClone.getSaveDate();
    state = toClone.getState();
    stateSaveDate = toClone.getStateSaveDate();
    setTotalNbUsers(-1);
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
    this.description = Objects.requireNonNullElse(newDescription, "");
  }

  protected void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Set the list of the direct users in the group
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
    return (rule != null && !rule.trim().isEmpty());
  }

  @Override
  public int getDirectUsersCount() {
    return getUserIds().length;
  }

  @Override
  public int getTotalUsersCount() {
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
    return Objects.equals(this.id, other.id)
        && Objects.equals(this.specificId, other.specificId)
        && Objects.equals(this.domainId, other.domainId)
        && Objects.equals(this.superGroupId, other.superGroupId)
        && Objects.equals(this.name, other.name)
        && Objects.equals(this.description, other.description)
        && Objects.equals(this.rule, other.rule)
        && Objects.equals(this.getDirectUsersCount(), other.getDirectUsersCount());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id,
        this.specificId,
        this.domainId,
        this.superGroupId,
        this.name,
        this.description,
        this.rule,
        this.getDirectUsersCount());
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<GroupDetail> getSubGroups() {
    return Arrays.asList(getOrganisationController().getAllSubGroups(getId()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<UserDetail> getAllUsers() {
    return Arrays.asList(getOrganisationController().getAllUsersOfGroup(getId()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<UserDetail> getUsers() {
    return Stream.of(getUserIds())
        .map(getOrganisationController()::getUserDetail)
        .map(UserDetail.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public boolean isValidState() {
    return !GroupState.UNKNOWN.equals(state) && !isRemovedState();
  }

  @Override
  public boolean isRemovedState() {
    return GroupState.REMOVED.equals(state);
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the date of the user creation
   */
  public void setCreationDate(final Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public Date getSaveDate() {
    return saveDate;
  }

  /**
   * @param saveDate the date of the last group save
   */
  public void setSaveDate(final Date saveDate) {
    this.saveDate = saveDate;
  }

  @Override
  public GroupState getState() {
    return state;
  }

  /**
   * The state of the group is updated and the according save date too.
   *
   * @param state the state of the group.
   */
  public void setState(final GroupState state) {
    this.state = state != null ? state : GroupState.from(null);
  }

  @Override
  public Date getStateSaveDate() {
    return stateSaveDate;
  }

  @Override
  public boolean isApplicationManaged() {
    return instanceId != null;
  }

  /**
   * @param stateSaveDate the date of last user state save (when it changes)
   */
  public void setStateSaveDate(final Date stateSaveDate) {
    this.stateSaveDate = stateSaveDate;
  }
}
