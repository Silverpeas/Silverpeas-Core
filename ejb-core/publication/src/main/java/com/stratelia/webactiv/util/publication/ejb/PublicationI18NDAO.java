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

package com.stratelia.webactiv.util.publication.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.publication.model.PublicationI18N;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

/**
 * This is the Publication Data Access Object.
 * @author Nicolas Eysseric
 */
public class PublicationI18NDAO {
  private static String TABLENAME = "SB_Publication_PubliI18N";

  public static List<PublicationI18N> getTranslations(Connection con, PublicationPK pubPK)
      throws SQLException {
    StringBuffer selectStatement = new StringBuffer(128);
    selectStatement.append("select * from ").append(TABLENAME);
    selectStatement.append(" where pubId = ? ");
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      PublicationI18N pub = null;
      stmt = con.prepareStatement(selectStatement.toString());
      stmt.setInt(1, Integer.parseInt(pubPK.getId()));
      rs = stmt.executeQuery();
      ArrayList<PublicationI18N> list = new ArrayList<PublicationI18N>();
      while (rs.next()) {
        pub = new PublicationI18N();
        pub.setId(rs.getInt(1));
        pub.setObjectId(Integer.toString(rs.getInt(2)));
        pub.setLanguage(rs.getString(3));
        pub.setName(rs.getString(4));
        pub.setDescription(rs.getString(5));
        pub.setKeywords(rs.getString(6));
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static void addTranslation(Connection con, PublicationI18N translation)
      throws SQLException, UtilException {
    StringBuffer insertStatement = new StringBuffer(128);
    insertStatement.append("insert into ").append(TABLENAME).append(
        " values (?, ?, ?, ?, ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, DBUtil.getNextId(TABLENAME, "id"));
      prepStmt.setInt(2, Integer.parseInt(translation.getObjectId()));
      prepStmt.setString(3, translation.getLanguage());
      prepStmt.setString(4, translation.getName());
      prepStmt.setString(5, translation.getDescription());
      prepStmt.setString(6, translation.getKeywords());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateTranslation(Connection con,
      PublicationI18N translation) throws SQLException {
    int rowCount = 0;

    StringBuffer updateQuery = new StringBuffer(128);
    updateQuery.append("update ").append(TABLENAME);
    updateQuery.append(" set name = ? , description = ? , keywords = ? ");
    updateQuery.append(" where id = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery.toString());
      prepStmt.setString(1, translation.getName());
      prepStmt.setString(2, translation.getDescription());
      prepStmt.setString(3, translation.getKeywords());
      prepStmt.setInt(4, translation.getId());

      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new PublicationRuntimeException(
          "PublicationI18NDAO.updateTranslation()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "translationId = "
          + translation.getId());
    }
  }

  public static void removeTranslation(Connection con, String translationId)
      throws SQLException {
    removeTranslation(con, Integer.parseInt(translationId));
  }

  public static void removeTranslation(Connection con, int translationId)
      throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(TABLENAME).append(
        " where id = ? ");
    PreparedStatement stmt = null;

    try {
      stmt = con.prepareStatement(deleteStatement.toString());
      stmt.setInt(1, translationId);
      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  public static void removeTranslations(Connection con, PublicationPK pubPK)
      throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(TABLENAME).append(
        " where pubId = ? ");
    PreparedStatement stmt = null;

    try {
      stmt = con.prepareStatement(deleteStatement.toString());
      stmt.setInt(1, Integer.parseInt(pubPK.getId()));
      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }
}