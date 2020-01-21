/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.cmis;

import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Node in a tree of {@link Identifiable} objects. A node is linked to a parent node, otherwise it
 * is the root of the tree. A node can have one or more children nodes and is qualified by a path
 * from a root node.
 * @author mmoquillon
 */
public class TreeNode {
  final Map<String, TreeNode> cache;
  final Identifiable object;
  final Set<TreeNode> children = new HashSet<>();
  final TreeNode parent;

  /**
   * Constructs a new node of a tree with the specified object as node value. The node has no parent
   * and hence is a root node of the tree. This method is for the {@link SilverpeasObjectsTree}.
   * @param cache the cache of nodes backed by the tree.
   * @param object the value of the node.
   */
  TreeNode(Map<String, TreeNode> cache, final Identifiable object) {
    this(cache, object, null);
  }

  /**
   * Constructs a new node of a tree with the specified object as node value and that has as parent
   * the given tree node. This method is for the {@link SilverpeasObjectsTree}.
   * @param cache the cache of nodes backed by the tree.
   * @param object the value of the node.
   * @param parent the parent of the node. If null, then the node is a root one.
   */
  TreeNode(Map<String, TreeNode> cache, final Identifiable object, final TreeNode parent) {
    this.cache = cache;
    this.object = object;
    this.parent = parent;
  }

  /**
   * Gets the unique identifier of the node. The identifier of the node is the identifier of the
   * object wrapped by the node.
   * @return the unique identifier of the wrapped object (node value).
   */
  public String getId() {
    return object.getId();
  }

  /**
   * Gets the value of the node, id est the object wrapped by the node.
   * @return the wrapped object.
   */
  public Identifiable getObject() {
    return object;
  }

  /**
   * Gets the parent of this node. Null if this node is a root one and hence has no parent.
   * @return either the parent node or null if this node is a root one.
   */
  public TreeNode getParent() {
    return parent;
  }

  /**
   * Gets all the nodes that are the direct children of this node.
   * @return a list of the nodes that are children of this node, or an empty list if this node has
   * no children.
   */
  public Set<TreeNode> getChildren() {
    return children;
  }

  /**
   * Gets the path of this node from the root node of the tree.
   * @return a list of nodes from a root one to this one in the tree. The list is ordered by their
   * position in the tree up to this node.
   */
  public List<TreeNode> getPath() {
    final List<TreeNode> path = new ArrayList<>();
    TreeNode current = this;
    do {
      path.add(0, current);
      current = current.getParent();
    } while (current != null);
    return path;
  }

  /**
   * Is this node a root one? A node is a root of a tree if it has no parent.
   * @return true if this node is a root one, false otherwise.
   */
  public boolean isRoot() {
    return getParent() == null;
  }

  /**
   * Adds a new node child wrapping the specified space instance.
   * @param spaceInstLight an instance of a space in Silverpeas.
   * @return itself.
   */
  public TreeNode addChildren(final SpaceInstLight spaceInstLight) {
    if (object instanceof SpaceInstLight) {
      spaceInstLight.setFatherId(((SpaceInstLight) object).getLocalId());
      return addChildrenNode(spaceInstLight);
    } else {
      throw new IllegalArgumentException("A space can be a child only to another space");
    }
  }

  /**
   * Adds a new node child wrapping the specified application instance.
   * @param componentInstLight an instance of an application in Silverpeas.
   * @return itself.
   */
  public TreeNode addChildren(final ComponentInstLight componentInstLight) {
    if (object instanceof SpaceInstLight) {
      componentInstLight.setDomainFatherId(object.getId());
      return addChildrenNode(componentInstLight);
    } else {
      throw new IllegalArgumentException("A component instance can be a child only to another space");
    }
  }

  private TreeNode addChildrenNode(final Identifiable child) {
    TreeNode node = new TreeNode(cache, child, this);
    getChildren().add(node);
    cache.put(node.getId(), node);
    return node;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TreeNode)) {
      return false;
    }
    final TreeNode treeNode = (TreeNode) o;
    return object.getId().equals(treeNode.object.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(object.getId());
  }
}
  