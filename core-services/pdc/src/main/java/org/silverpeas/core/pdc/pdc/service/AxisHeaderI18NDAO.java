/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderI18N;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class declaration
 * @author
 */
@Repository
public class AxisHeaderI18NDAO {

  private static String pdcAxisI18NTable = "SB_Pdc_AxisI18N";

  private static final String COLUMNS = "id,AxisId,Lang,Name,Description";

  /**
   * Constructor declaration
   *
   */
  protected AxisHeaderI18NDAO() {

  }

  public List<AxisHeaderI18N> getTranslations(Connection con, String axisId)
      throws SQLException {
    String selectQuery = "select * from " + pdcAxisI18NTable
        + " where AxisId = ?";
    List<AxisHeaderI18N> allTranslations = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(axisId));

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        AxisHeaderI18N translation;
        translation = new AxisHeaderI18N();
        translation.setId(String.valueOf(rs.getInt(1)));
        translation.setObjectId(java.lang.Integer.toString(rs.getInt(2)));
        translation.setLanguage(rs.getString(3));
        translation.setName(rs.getString(4));
        translation.setDescription(rs.getString(5));

        allTranslations.add(translation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return allTranslations;
  }

  public void createTranslation(Connection con, AxisHeaderI18N translation)
      throws SQLException {
    String selectQuery = "insert into " + pdcAxisI18NTable + "(" + COLUMNS
        + ") values  (?, ?, ?, ?, ?)";
    PreparedStatement prepStmt = null;
    int id = -1;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      id = DBUtil.getNextId(pdcAxisI18NTable, "id");
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, Integer.parseInt(translation.getObjectId()));
      prepStmt.setString(3, translation.getLanguage());
      prepStmt.setString(4, translation.getName());
      prepStmt.setString(5, translation.getDescription());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void updateTranslation(Connection con, AxisHeaderI18N translation)
      throws SQLException {
    String selectQuery = "update " + pdcAxisI18NTable
        + " set name = ?, description = ? where id = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setString(1, translation.getName());
      prepStmt.setString(2, translation.getDescription());
      prepStmt.setInt(3, Integer.parseInt(translation.getId()));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public void deleteTranslation(Connection con, String translationId)
      throws SQLException {
    String selectQuery = "delete from " + pdcAxisI18NTable + " where id = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(translationId));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void deleteTranslations(Connection con, String axisId)
      throws SQLException {
    String selectQuery = "delete from " + pdcAxisI18NTable
        + " where axisId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(axisId));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}