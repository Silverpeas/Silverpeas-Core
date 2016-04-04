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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.pdc.tree.model;

import java.util.List;

/**
 * Class declaration
 * @author
 */
public class Tree implements java.io.Serializable {

  private static final long serialVersionUID = 2045507298400203693L;
  private List<TreeNode> tree = null;

  /**
   * Constructor declaration
   * @param treeNodes
   * @see
   */
  public Tree(List<TreeNode> treeNodes) {
    tree = treeNodes;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public List<TreeNode> getTree() {
    return tree;
  }

  /**
   * Method declaration
   * @param treeNodes
   * @see
   */
  public void setTree(List<TreeNode> treeNodes) {
    this.tree = treeNodes;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getDepth() {
    if (getTree() != null && getTree().size() > 0) {
      List<TreeNode> tree = getTree();
      int maxLevel = 0;
      int rootLevel = 0;
      int level = 0;
      TreeNode node = null;

      for (int i = 0; i < tree.size(); i++) {
        node = tree.get(0);
        level = node.getLevelNumber();
        if (i == 0) {
          rootLevel = level;
        }
        if (level > maxLevel) {
          maxLevel = level;
        }
      }
      return maxLevel - rootLevel;
    }
    return 0;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public TreeNode getRoot() {
    if (getTree() != null && getTree().size() > 0) {
      return getTree().get(0);
    }
    return null;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(tree = " + getTree() + ")";
  }

}
