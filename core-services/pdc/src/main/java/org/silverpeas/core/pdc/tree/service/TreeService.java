/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.tree.service;

import org.silverpeas.core.pdc.tree.model.TreeManagerException;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.silverpeas.core.pdc.tree.model.TreeNodePK;

import java.sql.Connection;
import java.util.List;

public interface TreeService {

  String createRoot(Connection con, TreeNode root)
      throws TreeManagerException;

  void updateNode(Connection con, TreeNode node)
      throws TreeManagerException;

  void updateRoot(Connection con, TreeNode root)
      throws TreeManagerException;

  void deleteTree(Connection con, String treeId)
      throws TreeManagerException;

  void deleteSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException;

  List<TreeNode> getTree(Connection con, String treeId)
      throws TreeManagerException;

  List<TreeNode> getSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException;

  TreeNode getNode(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException;

  List<TreeNode> getNodesByName(Connection con, String nodeName)
      throws TreeManagerException;

  String insertFatherToNode(Connection con, TreeNode nodeToInsert,
      TreeNodePK refNode, String treeId) throws TreeManagerException;

  void moveSubTreeToNewFather(Connection con, TreeNodePK nodeToMovePK,
      TreeNodePK newFatherPK, String treeId, int orderNumber)
      throws TreeManagerException;

  String createSonToNode(Connection con, TreeNode nodeToInsert,
      TreeNodePK refNode, String treeId) throws TreeManagerException;

  List<TreeNode> getSonsToNode(Connection con, TreeNodePK treeNodePK, String treeId)
      throws TreeManagerException;

  void deleteNode(Connection con, TreeNodePK treeNodePK, String treeId)
      throws TreeManagerException;

  List<TreeNode> getFullPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException;

  String getPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException;

  TreeNode getRoot(Connection con, String treeId)
      throws TreeManagerException;

  void indexTree(Connection con, int treeId) throws TreeManagerException;
}