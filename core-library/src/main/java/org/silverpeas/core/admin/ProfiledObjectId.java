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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.node.model.NodePK;

import java.io.Serializable;
import java.util.Objects;

/**
 * The unique identifier of an object that is covered by a right profile.
 * @author mmoquillon
 */
public class ProfiledObjectId implements ResourceIdentifier, Serializable {

  private static final long serialVersionUID = -6252481085153576106L;

  private final String id;
  private final ProfiledObjectType type;

  public static final ProfiledObjectId NOTHING = new ProfiledObjectId();
  public static final String ROOT_ID = NodePK.ROOT_NODE_ID;

  private ProfiledObjectId() {
    id = "-1";
    type = ProfiledObjectType.NONE;
  }

  /**
   * Constructs the identifier of an object that is related to a right profile, that is of the
   * specified type and that have the given identifier.
   * @param type the type of the profiled object.
   * @param id the local identifier of the object.
   */
  public ProfiledObjectId(final ProfiledObjectType type, final String id) {
    this.id = id;
    this.type = type;
  }

  /**
   * Constructs from the specified node identifier a {@link ProfiledObjectId} instance that
   * refers the node as an access right profiled object.
   * @param nodeId the unique identifier of a node.
   * @return the {@link ProfiledObjectId} instance representation of the node identifier.
   */
  public static ProfiledObjectId fromNode(final String nodeId) {
    return new ProfiledObjectId(ProfiledObjectType.NODE, nodeId);
  }

  /**
   * Constructs from the specified node identifier a {@link ProfiledObjectId} instance that
   * refers the node as an access right profiled object.
   * @param nodeId the unique identifier of a node.
   * @return the {@link ProfiledObjectId} instance representation of the node identifier.
   */
  public static ProfiledObjectId fromNode(final int nodeId) {
    return fromNode(String.valueOf(nodeId));
  }

  /**
   * Constructs from its specified serialized expression a {@link ProfiledObjectId}  instance.
   * The expression must be formed with a code defining its type and of its local identifier.
   * For knowing possible types, please see {@link ProfiledObjectType} enumeration.
   * @param expressionId a textual expression of a profiled object identifier.
   * @return the {@link ProfiledObjectId} instance represented by the specified expression.
   */
  public static ProfiledObjectId from(final String expressionId) {
    final ProfiledObjectType type = ProfiledObjectType.valueOf(expressionId.substring(0, 1));
    final String id = expressionId.substring(2);
    return new ProfiledObjectId(type, id);
  }

  /**
   * Gets the local identifier of the object referred by this identifier.
   * @return an identifier.
   */
  public String getId() {
    return id;
  }

  private int getIdAsInt() {
    return Integer.parseInt(id);
  }

  /**
   * Gets the type of the object referred by this identifier.
   * @return the type of the object as a {@link ProfiledObjectType} enumeration value.
   */
  public ProfiledObjectType getType() {
    return type;
  }

  /**
   * Is this identifier defined? If the identifier is defined, then the referred object must have
   * a type and an identifier positive.
   * @return true if the identifier is defined.
   */
  public boolean isDefined() {
    return getType() != null && getIdAsInt() > -1;
  }

  /**
   * Is this identifier undefined? The identifier isn't defined when no object is referred by it.
   * As such, the type isn't defined and the identifier is strictly negative.
   * @return
   */
  public boolean isNotDefined() {
    return getType() == null || getIdAsInt() == -1;
  }

  /**
   * Is the identifier referring a root node object?
   * @return true if the referred object is a root node.
   */
  public boolean isRootNode() {
    return isDefined() && type == ProfiledObjectType.NODE && ROOT_ID.equals(getId());
  }

  @Override
  public String asString() {
    return type.getCode() + id;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProfiledObjectId)) {
      return false;
    }
    final ProfiledObjectId objectId = (ProfiledObjectId) o;
    return id.equals(objectId.id) && type == objectId.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type);
  }
}
  