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

package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.node.model.NodePK;
import java.util.ArrayList;
import java.util.List;

/**
 * Structure to hold the node hierarchy with the number of publications.
 * @author ehugonnet
 */
public class NodeTree {

  private int nbPublications;
  private NodePK currentNode;
  private NodeTree parent;
  private ArrayList<NodeTree> children = new ArrayList<NodeTree>();

  public NodeTree(NodePK pk) {
    currentNode = pk;
  }

  public NodePK getKey() {
    return currentNode;
  }

  @SuppressWarnings("unchecked")
  public List<NodeTree> getChildren() {
    return (List<NodeTree>) children.clone();
  }

  public void addChild(NodeTree child) {
    children.add(child);
  }

  public void setParent(NodeTree parent) {
    this.parent = parent;
  }

  public NodeTree getParent() {
    return parent;
  }

  public int getNbPublications() {
    return nbPublications;
  }

  public void setNbPublications(int nbPublications) {
    this.nbPublications = nbPublications;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NodeTree other = (NodeTree) obj;
    if (this.currentNode != other.currentNode && (this.currentNode == null || !this.currentNode.
        equals(other.currentNode))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 17 * hash + (this.currentNode != null ? this.currentNode.hashCode() : 0);
    return hash;
  }
}