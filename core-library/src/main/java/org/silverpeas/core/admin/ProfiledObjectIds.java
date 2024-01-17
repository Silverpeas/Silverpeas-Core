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

package org.silverpeas.core.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * The unique identifiers of object that is covered by right profiles.
 * @author silveryocha
 */
public class ProfiledObjectIds extends ArrayList<String> {
  private static final long serialVersionUID = 1105782427397730734L;

  private final ProfiledObjectType type;
  private boolean typeFilledOnly = false;

  private ProfiledObjectIds(final ProfiledObjectType type) {
    this.type = type;
  }

  private ProfiledObjectIds(final ProfiledObjectType type,Collection<String> objectIds) {
    this.type = type;
    this.addAll(objectIds);
  }

  private ProfiledObjectIds(Collection<String> objectIds, final ProfiledObjectType type) {
    this.type = type;
    this.addAll(objectIds);
  }

  public static ProfiledObjectIds none() {
    return new ProfiledObjectIds(ProfiledObjectType.NONE);
  }

  public static ProfiledObjectIds ofType(final ProfiledObjectType profiledObjectType) {
    final ProfiledObjectIds type = new ProfiledObjectIds(emptyList(), profiledObjectType);
    type.typeFilledOnly = true;
    return type;
  }

  /**
   * Constructs from the specified node identifiers a typed list.
   * @param profiledObjectId a profile object id.
   * @return the {@link ProfiledObjectId} instance representation of the node identifier.
   */
  public static ProfiledObjectIds fromProfileObjectId(final ProfiledObjectId profiledObjectId) {
    return new ProfiledObjectIds(profiledObjectId.getType(), Collections.singleton(profiledObjectId.getId()));
  }

  /**
   * Constructs from the specified node identifiers a typed list.
   * @param nodeIds a collection of unique node identifier.
   * @return the {@link ProfiledObjectId} instance representation of the node identifier.
   */
  public static ProfiledObjectIds fromNodeIds(final Collection<String> nodeIds) {
    return new ProfiledObjectIds(nodeIds, ProfiledObjectType.NODE);
  }

  /**
   * Gets the type of the objects referred into this list.
   * @return the type of the object as a {@link ProfiledObjectType} enumeration value.
   */
  public ProfiledObjectType getType() {
    return type;
  }

  /**
   * Indicates only the type has been filled.
   * @return true if only the type has been filled.
   */
  public boolean typeFilledOnly() {
    return isEmpty() && typeFilledOnly;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final ProfiledObjectIds strings = (ProfiledObjectIds) o;
    return type == strings.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type);
  }
}
