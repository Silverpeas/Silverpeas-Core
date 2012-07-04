/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.treeManager.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.treeManager.model.TreeNodeI18N;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * Class declaration
 * @author
 */
public class TreeI18NDAO {

  private static String treeI18NTable = "SB_Tree_TreeI18N";

  static final private String COLUMNS = "id,treeId,nodeId,lang,name,description";

  /**
   * Constructor declaration
   * @see
   */
  public TreeI18NDAO() {

  }

  public List<TreeNodeI18N> getTranslations(Connection con, String treeId, String nodeId)
      throws SQLException {
    String selectQuery = "select * from " + treeI18NTable
        + " where treeId = ? and nodeId = ?";
    Vector<TreeNodeI18N> allTranslations = new Vector<TreeNodeI18N>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(treeId));
      prepStmt.setInt(2, Integer.parseInt(nodeId));

      rs = prepStmt.executeQuery();

      TreeNodeI18N translation = null;
      while (rs.next()) {
        translation = new TreeNodeI18N();
        translation.setId(rs.getInt(1));
        translation.setObjectId(Integer.toString(rs.getInt(3)));
        translation.setLanguage(rs.getString(4));
        translation.setName(rs.getString(5));
        translation.setDescription(rs.getString(6));

        SilverTrace.info("Pdc", "TreeI18NDAO.getTranslations",
            "root.MSG_GEN_PARAM_VALUE", "translation " + translation.getId()
            + " for treeId = " + treeId + " and id =" + nodeId);
        allTranslations.add(translation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return allTranslations;
  }

  public void createTranslation(Connection con, TreeNodeI18N translation,
      String treeId) throws SQLException, UtilException {
    String selectQuery = "insert into " + treeI18NTable + "(" + COLUMNS
        + ") values  (?, ?, ?, ?, ?, ?)";
    PreparedStatement prepStmt = null;
    int id = -1;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      id = DBUtil.getNextId(treeI18NTable, "id");
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, Integer.parseInt(treeId));
      prepStmt.setInt(3, Integer.parseInt(translation.getObjectId()));
      prepStmt.setString(4, translation.getLanguage());
      prepStmt.setString(5, translation.getName());
      prepStmt.setString(6, translation.getDescription());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void updateTranslation(Connection con, TreeNodeI18N translation)
      throws SQLException, UtilException {
    String selectQuery = "update " + treeI18NTable
        + " set name = ?, description = ? where id = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setString(1, translation.getName());
      prepStmt.setString(2, translation.getDescription());
      prepStmt.setInt(3, translation.getId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public void updateTranslation(Connection con, String treeId,
      TreeNodeI18N translation) throws SQLException, UtilException {
    String selectQuery = "update "
        + treeI18NTable
        + " set name = ?, description = ? where treeId = ? and nodeId = ? and lang = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setString(1, translation.getName());
      prepStmt.setString(2, translation.getDescription());
      prepStmt.setInt(3, Integer.parseInt(treeId));
      prepStmt.setInt(4, Integer.parseInt(translation.getObjectId()));
      prepStmt.setString(5, translation.getLanguage());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public void deleteTranslation(Connection con, int translationId)
      throws SQLException, UtilException {
    String selectQuery = "delete from " + treeI18NTable + " where id = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, translationId);

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void deleteTranslation(Connection con, String treeId, String nodeId,
      String language) throws SQLException, UtilException {
    String selectQuery = "delete from " + treeI18NTable
        + " where treeId = ? and nodeId = ? and lang = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(treeId));
      prepStmt.setInt(2, Integer.parseInt(nodeId));
      prepStmt.setString(3, language);

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void deleteTreeTranslations(Connection con, String treeId)
      throws SQLException, UtilException {
    String selectQuery = "delete from " + treeI18NTable + " where treeId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(treeId));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void deleteNodeTranslations(Connection con, String treeId,
      String nodeId) throws SQLException, UtilException {
    String selectQuery = "delete from " + treeI18NTable
        + " where treeId = ? and nodeId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(treeId));
      prepStmt.setInt(2, Integer.parseInt(nodeId));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}