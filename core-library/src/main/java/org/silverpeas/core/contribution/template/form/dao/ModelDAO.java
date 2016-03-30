/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.template.form.dao;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModelDAO {

  public static void addModel(Connection con, String instanceId, String modelId)
      throws SQLException {
    addModel(con, instanceId, modelId, "0");
  }

  public static void addModel(Connection con, String instanceId, String modelId, String objectId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "insert into st_instance_ModelUsed values (?,?,?)";

      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, modelId);
      prepStmt.setString(3, objectId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteModel(Connection con, String instanceId)
      throws SQLException, UtilException {
    deleteModel(con, instanceId, "0");
  }

  public static void deleteModel(Connection con, String instanceId, String objectId)
      throws SQLException, UtilException {
    // deleting all models
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from st_instance_ModelUsed where instanceId = ? ";
      if (StringUtil.isDefined(objectId)) {
        query += " and objectId = ? ";
      }

      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      if (StringUtil.isDefined(objectId)) {
        prepStmt.setString(2, objectId);
      }
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static Collection<String> getModelUsed(Connection con, String instanceId)
      throws SQLException, UtilException {
    return getModelUsed(con, instanceId, "0");
  }

  public static Collection<String> getModelUsed(Connection con, String instanceId, String objectId)
      throws SQLException, UtilException {
    List<String> listModel = new ArrayList<>();
    String query = "select modelId from st_instance_ModelUsed where instanceId = ?";
    if (StringUtil.isDefined(objectId)) {
      query += " and objectId = ? ";
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      if (StringUtil.isDefined(objectId)) {
        prepStmt.setString(2, objectId);
      }
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String modelId = rs.getString(1);
        listModel.add(modelId);
      }
    } finally {
      // Closing
      DBUtil.close(rs, prepStmt);
    }
    return listModel;
  }

}
