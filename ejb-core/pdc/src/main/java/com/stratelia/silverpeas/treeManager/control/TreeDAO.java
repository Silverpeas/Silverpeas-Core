/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// TODO : reporter dans CVS (done)
package com.stratelia.silverpeas.treeManager.control;

import java.sql.*;
import java.util.*;
import com.stratelia.silverpeas.treeManager.model.*;
import com.stratelia.webactiv.util.*;

public class TreeDAO {

  private static String TreeTable = "SB_Tree_Tree";

  public TreeDAO() {
  }

  public static String createRoot(Connection con, TreeNode root)
      throws SQLException {
    int newTreeId = 0;

    try {
      newTreeId = DBUtil.getNextId(TreeTable, "treeId");
    } catch (Exception e) {
      throw new SQLException("DBUtil.getNextId() failed !");
    }

    String insertQuery = "insert into " + TreeTable
        + " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setInt(1, newTreeId);
      prepStmt.setInt(2, 0);
      prepStmt.setString(3, root.getName());
      prepStmt.setString(4, root.getDescription());
      prepStmt.setString(5, root.getCreationDate());
      prepStmt.setInt(6, new Integer(root.getCreatorId()).intValue());
      prepStmt.setString(7, "/");
      prepStmt.setInt(8, 0);
      prepStmt.setInt(9, -1);
      prepStmt.setInt(10, 0);
      prepStmt.setString(11, root.getLanguage());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    return new Integer(newTreeId).toString();
  }

  public static TreeNodePK createNode(Connection con, TreeNode node)
      throws SQLException {
    int newNodeId = 0;

    try {
      newNodeId = getNextValueIdToTree(con, node.getTreeId());
    } catch (Exception e) {
      throw new SQLException("DBUtil.getNextValueIdToTree() failed !");
    }

    String insertQuery = "insert into " + TreeTable
        + " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setInt(1, new Integer(node.getTreeId()).intValue());
      prepStmt.setInt(2, newNodeId);
      prepStmt.setString(3, node.getName());
      prepStmt.setString(4, node.getDescription());
      prepStmt.setString(5, node.getCreationDate());
      prepStmt.setInt(6, new Integer(node.getCreatorId()).intValue());
      prepStmt.setString(7, node.getPath());
      prepStmt.setInt(8, node.getLevelNumber());
      prepStmt.setInt(9, new Integer(node.getFatherId()).intValue());
      prepStmt.setInt(10, node.getOrderNumber());
      prepStmt.setString(11, node.getLanguage());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    return new TreeNodePK(new Integer(newNodeId).toString());
  }

  public static void updateNode(Connection con, TreeNode node)
      throws SQLException {
    String updateQuery = "update "
        + TreeTable
        + " set name = ? , description = ? , path = ? , levelNumber = ? , fatherId = ? , orderNumber = ? , lang = ? where treeId = ? and id = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, node.getName());
      prepStmt.setString(2, node.getDescription());
      prepStmt.setString(3, node.getPath());
      prepStmt.setInt(4, node.getLevelNumber());
      prepStmt.setInt(5, new Integer(node.getFatherId()).intValue());
      prepStmt.setInt(6, node.getOrderNumber());
      prepStmt.setString(7, node.getLanguage());
      prepStmt.setInt(8, new Integer(node.getTreeId()).intValue());
      prepStmt.setInt(9, new Integer(node.getPK().getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteNode(Connection con, TreeNodePK treeNodePK,
      String treeId) throws SQLException {

    String deleteQuery = "delete from " + TreeTable + " where treeId=" + treeId
        + " and id=" + treeNodePK.getId();

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
    } finally {
      DBUtil.close(stmt);
    }
  }

  public static void levelUp(Connection con, String path, String treeId)
      throws SQLException {
    String updateStatement = "update " + TreeTable
        + " set levelNumber = levelNumber - 1 where treeId = " + treeId
        + " and path like '" + path + "%' ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void levelDown(Connection con, String path, String treeId)
      throws SQLException {
    String updateStatement = "update " + TreeTable
        + " set levelNumber = levelNumber - 1 where treeId = " + treeId
        + " and path like '" + path + "%' ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void changeFatherAndPath(Connection con, int oldFather,
      int newFather, String path, String treeId) throws SQLException {
    String updateStatement = "update " + TreeTable
        + " set fatherId = ?, path = ? where treeId = " + treeId
        + " and fatherId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, newFather);
      prepStmt.setString(2, path);
      prepStmt.setInt(3, oldFather);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Update path of decendante nodes of the deleted node.
   * 
   * @param con
   *          - the connection to the database
   * @param oldFather
   *          - the deleted node
   */
  public static void updatePath(Connection con, String oldFather, String treeId)
      throws SQLException {
    // get in the first time all pathes of the son nodes of the oldFather
    ArrayList pathes = getPathes(con, oldFather, treeId);

    String oldPath = "";
    String newPath = "";
    String pattern = "/" + oldFather; // the pattern that we want remove
    int pattern_idx;
    int lenOfPattern = pattern.length(); // length of the pattern
    // we update pathes if old pathes exist
    while (!pathes.isEmpty()) {
      oldPath = (String) pathes.remove(0);
      pattern_idx = oldPath.indexOf(pattern); // != -1
      newPath = oldPath.substring(0, pattern_idx)
          + oldPath.substring(pattern_idx + lenOfPattern);
      // erase the pattern
      // update
      updatePath(con, oldPath, newPath, treeId);
    }
  }

  /**
   * Update the old path by the new path.
   * 
   * @param con
   *          - the connection to the database
   * @param oldPath
   *          - the old path that it must be updated
   * @param newPath
   *          - the new path
   */
  private static void updatePath(Connection con, String oldPath,
      String newPath, String treeId) throws SQLException {
    String updateQuery = " update " + TreeTable
        + " set path = ? where treeId = " + treeId + " and path = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, newPath);
      prepStmt.setString(2, oldPath);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Returns a list of path
   * 
   * @param con
   *          - the connection to the database
   * @param oldFather
   *          - the deleted node
   * @return a list of String
   */
  private static ArrayList getPathes(Connection con, String oldFather,
      String treeId) throws SQLException {
    String selectQuery = " select path from " + TreeTable + " where treeId = "
        + treeId + " and path like '%/" + oldFather + "/%'";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList pathes = new ArrayList();
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery);
      while (rs.next()) {
        pathes.add(rs.getString(1));
      }

    } finally {
      DBUtil.close(rs, stmt);
    }

    return pathes;
  }

  private static synchronized int getNextValueIdToTree(Connection con,
      String treeId) throws SQLException {
    String selectQuery = " select max(id) from " + TreeTable
        + " where treeId = " + treeId;
    Statement stmt = null;
    ResultSet rs = null;
    int nextValueId = 0;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery);
      if (rs.next()) {
        nextValueId = rs.getInt(1);
        nextValueId++;
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    return nextValueId;
  }

}