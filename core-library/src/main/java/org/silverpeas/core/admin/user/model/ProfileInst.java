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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.RightProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * A right profile on a component instance or in an object managed by that component instance.
 * It defines all the users and groups that can access a component instance with some well
 * defined privileges. The privileges in Silverpeas are defined by a set of predefined roles and
 * the given role a profile is related to is indicated by the {@link ProfileInst#getName()} method.
 * When the profile is about a given resource of a component instance,
 * the right accesses are defined for that resource whose the identifier can be get with the
 * {@link ProfileInst#getObjectId()} method; by default this method returns
 * {@link ProfiledObjectId#NOTHING} if the right profile is on the component instance itself.
 */
public class ProfileInst implements RightProfile {

  private static final long serialVersionUID = 3092416162986110340L;
  private String id;
  private String name;
  private String label;
  private String description;
  private String componentFatherId;
  private List<String> groups;
  private List<String> users;

  private boolean isInherited = false;

  private ProfiledObjectId objectId = ProfiledObjectId.NOTHING;
  private ProfiledObjectId parentObjectId = ProfiledObjectId.NOTHING;

  /**
   * Constructs an empty right profile instance.
   */
  public ProfileInst() {
    id = "";
    name = "";
    label = "";
    description = "";
    componentFatherId = "";
    groups = new ArrayList<>();
    users = new ArrayList<>();
  }

  /**
   * Copy this profile to get another profile with the same value.
   * @return a copy of this profile.
   */
  @SuppressWarnings("unchecked")
  public ProfileInst copy() {
    ProfileInst pi = new ProfileInst();
    pi.name = name;
    pi.label = label;
    pi.description = description;
    pi.componentFatherId = componentFatherId;
    pi.isInherited = isInherited;
    pi.parentObjectId = parentObjectId;
    pi.objectId = objectId;
    pi.groups = (List<String>) ((ArrayList<String>) groups).clone();
    pi.users = (List<String>) ((ArrayList<String>) users).clone();
    return pi;
  }

  /**
   * Sets a unique identifier to this profile. Shouldn't be used. Reserved to the persistence
   * layer.
   * @param sId the unique identifier of the profile.
   */
  public void setId(String sId) {
    id = sId;
  }

  /**
   * Gets a unique identifier of this profile.
   * @return the profile identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets a name to this profile. The name of the profile defines by convention the privileges
   * that are granted to the users and groups defined in this profile. In Silverpeas, the privileges
   * are defined by convention in different predefined roles and it is the name of this role that
   * should be set here.
   * @param sName the name of the role referred by this profile.
   */
  public void setName(String sName) {
    name = sName;
  }

  /**
   * Gets the name of this profile. The name of the profile defines by convention the privileges
   * that are granted to the users and groups defined in this profile. In Silverpeas, the privileges
   * are defined by convention in different predefined roles and it is the name of this role that
   * is returned here.
   * @return the name of the role referred by this profile.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets a label to this profile. A label is a user-friendly name given to this profile.
   * @param sLabel a label to give to this profile.
   */
  public void setLabel(String sLabel) {
    label = sLabel;
  }

  /**
   * Gets a label of this profile. A label is a user-friendly name given to this profile.
   * @return the profile label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a short description to this profile
   * @param sDescription a textual description.
   */
  public void setDescription(String sDescription) {
    description = sDescription;
  }

  /**
   * Gets a short description of this profile.
   * @return a profile description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the component instance that is related by this profile. By default, this profile is
   * on this component instance until an object identifier is set with the
   * {@link ProfileInst#setObjectId(ProfiledObjectId)} method with a value other than
   * {@link ProfiledObjectId#NOTHING}.
   * @param sComponentFatherId the unique identifier of a component instance
   */
  public void setComponentFatherId(String sComponentFatherId) {
    componentFatherId = sComponentFatherId;
  }

  /**
   * Gets the component instance on which is related this profile. The profile is about the
   * access granted to this component instance with some well defined privileges unless the
   * {@link ProfileInst#getObjectId()} returns other than {@link ProfiledObjectId#NOTHING}.
   * @return the unique identifier of a component instance.
   */
  public String getComponentFatherId() {
    return componentFatherId;
  }

  /**
   * Gets the number of groups that are concerned by this profile
   * @return
   */
  public int getNumGroup() {
    return groups.size();
  }

  /**
   * Adds among the groups covered by this profile the specified group.
   * @param sGroupId the unique identifier of a group.
   */
  @Override
  public void addGroup(String sGroupId) {
    if (!groups.contains(sGroupId)) {
      groups.add(sGroupId);
    }
  }

  /**
   * Removes from the groups concerned by this profile the specified group.
   * @param sGroupId the unique identifier of a group.
   */
  @Override
  public void removeGroup(String sGroupId) {
    groups.remove(sGroupId);
  }

  /**
   * Gets all the groups that are concerned by this profile.
   * @return a list of group identifiers.
   */
  public List<String> getAllGroups() {
    return groups;
  }

  /**
   * Removes all the groups concerned by this profile.
   */
  public void removeAllGroups() {
    groups = new ArrayList<>();
  }

  /**
   * Gets the number of users covered by this profile.
   * @return the number of users in this profile.
   */
  public int getNumUser() {
    return users.size();
  }

  /**
   * Adds a user as being covered by this profile.
   * @param sUserId a unique identifier of a user.
   */
  @Override
  public void addUser(String sUserId) {
    if (!users.contains(sUserId)) {
      users.add(sUserId);
    }
  }

  /**
   * Removes the specified user among those covered by this profile.
   * @param sUserId a unique identifier of a user.
   */
  @Override
  public void removeUser(String sUserId) {
    users.remove(sUserId);
  }

  /**
   * Gets all the users concerned by this profile.
   * @return a list of user identifiers.
   */
  public List<String> getAllUsers() {
    return users;
  }

  /**
   * Removes all the users concerned by this profile.
   */
  public void removeAllUsers() {
    users = new ArrayList<>();
  }

  /**
   * Adds all the specified users in this profile.
   * @param users a list of user identifiers.
   */
  public void addUsers(List<String> users) {
    ArrayList<String> a = new ArrayList<>(users);
    a.removeAll(this.users);
    this.users.addAll(a);
  }

  /**
   * Adds all the specified groups in this profile.
   * @param groups a list of group identifiers.
   */
  public void addGroups(List<String> groups) {
    ArrayList<String> a = new ArrayList<>(groups);
    a.removeAll(this.groups);
    this.groups.addAll(a);
  }

  /**
   * Is the right accesses defined by this profile are inherited by another right profile, a
   * parent right profile?
   * @return true if the right accesses are inherited. False otherwise.
   */
  public boolean isInherited() {
    return isInherited;
  }

  /**
   * Sets the inheritance in right accesses to this profile.
   * @param isInherited a boolean indicating if the right accesses are inherited from another
   * right profile.
   */
  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  /**
   * Gets the identifier of the object covered by this profile. In the case the profile only about
   * the component instance referred by the {@link ProfileInst#getComponentFatherId()} method, then
   * {@link ProfiledObjectId#NOTHING} is returned. Such an object can be for example a node.
   * @return the identifier of the object referred by this profile. {@link ProfiledObjectId#NOTHING}
   * if none object is covered explicitly by this profile.
   */
  public ProfiledObjectId getObjectId() {
    return objectId;
  }

  /**
   * This profile is about the specified object and not on the component instance referred by the
   * {@link ProfileInst#getComponentFatherId()} method. Such an object can be for example a node.
   * @param objectId the unique identifier of the object covered by this profile.
   */
  public void setObjectId(final ProfiledObjectId objectId) {
    this.objectId = objectId;
  }

  /**
   * This profile isn't on the actual object but on its one of its parent (in the case the objects
   * are related) whose identifier is returned here.
   * @return the identifier of the object that is really covered by this profile. The
   * actual object get by {@link ProfileInst#getObjectId()} method inherits the right access
   * defines by this profile.
   */
  public ProfiledObjectId getParentObjectId() {
    return this.parentObjectId;
  }

  /**
   * This profile isn't on the actual object but on its one of its parent (in the case the objects
   * are related) whose identifier is set here.
   * @param parentObjectId the identifier of the object that is really covered by this profile. The
   * actual object get by {@link ProfileInst#getObjectId()} method inherits the right access
   * defines by this profile.
   */
  public void setParentObjectId(final ProfiledObjectId parentObjectId) {
    this.parentObjectId = parentObjectId;
  }

  /**
   * Sets the users and the groups that have to be concerned by this profile.
   * @param groupIds an array with some group identifiers.
   * @param userIds an array with some user identifiers.
   */
  public void setGroupsAndUsers(String[] groupIds, String[] userIds) {
    // groups
    for (int i = 0; groupIds != null && i < groupIds.length; i++) {
      if (groupIds[i] != null && groupIds[i].length() > 0) {
        addGroup(groupIds[i]);
      }
    }

    // users
    for (int i = 0; userIds != null && i < userIds.length; i++) {
      if (userIds[i] != null && userIds[i].length() > 0) {
        addUser(userIds[i]);
      }
    }
  }

  /**
   * Is the right profile on a component instance?
   * @return true if the profile defines right access of a component instance or false if it defines
   * right access of an object managed by that component instance.
   */
  public boolean isOnComponentInstance() {
    return this.objectId == ProfiledObjectId.NOTHING;
  }

  /**
   * Is this right profile empty?
   * @return true if no users or groups are concerned by this right profile. False otherwise.
   */
  public boolean isEmpty() {
    return getAllGroups().isEmpty() && getAllUsers().isEmpty();
  }
}