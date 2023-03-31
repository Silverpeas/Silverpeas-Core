/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.util.SilverpeasProperty;
import org.silverpeas.core.util.StringUtil;

import javax.jcr.PropertyType;
import java.util.stream.Stream;

import static org.apache.jackrabbit.JcrConstants.NT_FILE;
import static org.apache.jackrabbit.JcrConstants.NT_FOLDER;

/**
 * A Node in the JCR. The JCR is structured like a tree in which content, and administrative and
 * management data are both stored. Each node in the JCR is represented by a {@link javax.jcr.Node}
 * object but some implementations of the JCR can provide another type to represent such a node in
 * order to ease the navigation over the tree or to facilitate the access the administrative and
 * management data (users, groups of users, ACL, access policies, ...). This abstract class is to
 * provide to the {@link JCRAccessController} instances unified access to the node in the JCR
 * whatever the concrete type of the nodes defined in the implementation of the JCR.
 * @param <T> the type of the node in the implementation of the JCR. Usually, it should be a
 * {@link javax.jcr.Node} but some implementations can provide another type to represent a node.
 * @author mmoquillon
 */
public abstract class JCRNode<T> {

  private final T node;

  /**
   * Constructs a new wrapper of the specified node.
   * @param node the node to wrap.
   */
  protected JCRNode(T node) {
    this.node = node;
  }

  /**
   * Is this node represents a folder in Silverpeas?
   * @return true if this node is a Silverpeas folder (a component instance for example).
   */
  public boolean isFolder() {
    return NT_FOLDER.equals(getPrimaryType());
  }

  /**
   * Is this node represents a file in Silverpeas?
   * @return true if this node is mapped to a document in Silverpeas (an attachment for example).
   */
  public boolean isFile() {
    return NT_FILE.equals(getPrimaryType());
  }

  /**
   * Is this node is locked by a user in Silverpeas?
   * @return true if this node is locked. Usually a node is locked to ensure only one user is
   * handling it.
   */
  public boolean isLocked() {
    return getMixinTypes().anyMatch(SilverpeasProperty.SLV_OWNABLE_MIXIN::equals);
  }

  /**
   * Is the specified user owns this node. An owner is a user that has authored the Silverpeas
   * resource mapped by this node.
   * @param user a user in Silverpeas.
   * @return true if this node represents a resource in Silverpeas that has been authored by the
   * given user.
   */
  public boolean isOwnedByUser(final User user) {
    String owner = getProperty(SilverpeasProperty.SLV_PROPERTY_OWNER, PropertyType.LONG);
    if (StringUtil.isDefined(owner)) {
      return user != null && user.getId().equals(owner);
    }
    return false;
  }

  /**
   * Gets the absolute path of this node in the JCR from the root of the tree.
   * @return the absolute path of this node in the JCR, each item separated by a slash (/)
   * character.
   */
  public abstract String getPath();

  /**
   * Gets the fully qualified name of the primary type of this node.
   * @return the name of the node's primary type.
   */
  public abstract String getPrimaryType();

  /**
   * Gets the fully qualified name of each mixins of this node.
   * @return a stream on the names of the node's mixin types.
   */
  public abstract Stream<String> getMixinTypes();

  /**
   * Gets the value of the specified property.
   * @param name the name of the property.
   * @param type the type of the property value as defined in {@link PropertyType}.
   * @return the textual representation of the value of the property  or null the node doesn't have
   * such property with the given type.
   */
  public abstract String getProperty(String name, int type);

  protected T getNode() {
    return node;
  }
}
