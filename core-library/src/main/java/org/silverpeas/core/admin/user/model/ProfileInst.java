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

import org.silverpeas.core.admin.BaseRightProfile;
import org.silverpeas.core.admin.ProfiledObjectId;

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
public class ProfileInst extends BaseRightProfile {

  private String componentFatherId = "";
  private ProfiledObjectId objectId = ProfiledObjectId.NOTHING;
  private ProfiledObjectId parentObjectId = ProfiledObjectId.NOTHING;

  /**
   * Copy this profile to get another profile with the same value.
   * @return a copy of this profile.
   */
  @SuppressWarnings("unchecked")
  public ProfileInst copy() {
    ProfileInst pi = new ProfileInst();
    pi.setName(getName());
    pi.setLabel(getLabel());
    pi.setDescription(getDescription());
    pi.componentFatherId = componentFatherId;
    pi.setInherited(isInherited());
    pi.parentObjectId = parentObjectId;
    pi.objectId = objectId;
    pi.setGroups(getAllGroups());
    pi.setUsers(getAllUsers());
    return pi;
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
   * Is the right profile on a component instance?
   * @return true if the profile defines right access of a component instance or false if it defines
   * right access of an object managed by that component instance.
   */
  public boolean isOnComponentInstance() {
    return this.objectId == ProfiledObjectId.NOTHING;
  }
}