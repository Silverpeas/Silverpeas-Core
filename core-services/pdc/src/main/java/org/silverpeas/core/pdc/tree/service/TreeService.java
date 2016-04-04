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

package org.silverpeas.core.pdc.tree.service;

import java.util.List;
import java.sql.Connection;

import org.silverpeas.core.pdc.tree.model.TreeManagerException;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.silverpeas.core.pdc.tree.model.TreeNodePK;
import org.silverpeas.core.index.search.model.AxisFilter;

public interface TreeService {

  public String createRoot(Connection con, TreeNode root)
      throws TreeManagerException;

  public void updateNode(Connection con, TreeNode node)
      throws TreeManagerException;

  public void updateRoot(Connection con, TreeNode root)
      throws TreeManagerException;

  public void deleteTree(Connection con, String treeId)
      throws TreeManagerException;

  public void deleteSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException;

  public List<TreeNode> getTree(Connection con, String treeId)
      throws TreeManagerException;

  public List<TreeNode> getTree(Connection con, String treeId, AxisFilter filter)
      throws TreeManagerException;

  public List<TreeNode> getSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException;

  public TreeNode getNode(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException;

  public List<TreeNode> getNodesByName(Connection con, String nodeName)
      throws TreeManagerException;

  public String insertFatherToNode(Connection con, TreeNode nodeToInsert,
      TreeNodePK refNode, String treeId) throws TreeManagerException;

  public void moveSubTreeToNewFather(Connection con, TreeNodePK nodeToMovePK,
      TreeNodePK newFatherPK, String treeId, int orderNumber)
      throws TreeManagerException;

  public String createSonToNode(Connection con, TreeNode nodeToInsert,
      TreeNodePK refNode, String treeId) throws TreeManagerException;

  public List<TreeNode> getSonsToNode(Connection con, TreeNodePK treeNodePK, String treeId)
      throws TreeManagerException;

  public void deleteNode(Connection con, TreeNodePK treeNodePK, String treeId)
      throws TreeManagerException;

  public List<TreeNode> getFullPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException;

  public String getPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException;

  public TreeNode getRoot(Connection con, String treeId)
      throws TreeManagerException;

  public void indexTree(Connection con, int treeId) throws TreeManagerException;
}