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
package org.silverpeas.core.admin.component.model;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A user profile defined in an application component. A user profile is a mix between a role and
 * some privileges. The name of the profile denotes the name of the role. If the application can
 * describe any role it supports, even new ones, Silverpeas does support only a set of predefined
 * roles, all of them represented in the {@link org.silverpeas.core.admin.user.model.SilverpeasRole}
 * enum. To these roles are automatically mapped some privileges which are all hard-coded and based
 * upon access rights conventions in Silverpeas (for example the
 * {@link org.silverpeas.core.admin.user.model.SilverpeasRole#READER} role has read-only access to
 * the contributions). So, when a user profile is defined for an application, a mapping between this
 * profile with one of the predefined user profile in Silverpeas is required; if no mapping is
 * provided, then by default the name of the profile should match the name of a predefined profile.
 * The mapping of profiles are applied between those of the parent space and the instances of the
 * application component; so only a subset of predefined roles in Silverpeas is concerned by the
 * mapping of profiles: see {@link InheritableSpaceRoles} to have a look of them.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProfileType")
public class Profile extends ProfileDescription {

  @XmlAttribute
  protected String name;

  /**
   * Gets the value of the name property.
   * @return possible object is {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   * @param value allowed object is {@link String }
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the mapping between this user profile with those of the parent space of the component. By
   * convention, if no mapping is explicitly defined, then the default mapping applied is on the
   * profile name, even this name doesn't match any predefined space profile; in this case, the
   * mapping should be ignored and no role inheritance should be applied for this profile.
   * @return the mapping of this profile with at least one of the parent space. By default, if no
   * mapping has been defined in the descriptor of the application, the mapping is on the user
   * profile in the parent space having the same name that this profile.
   */
  @Override
  @Nonnull
  public ComponentSpaceProfileMapping getSpaceProfileMapping() {
    if (spaceProfileMapping == null) {
      spaceProfileMapping = new ComponentSpaceProfileMapping();
      spaceProfileMapping.getProfiles().add(new SpaceProfile(getName()));
    }
    return spaceProfileMapping;
  }
}
