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
package com.silverpeas.formTemplate.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class ModelDAO {
  public static void addModel(Connection con, String instanceId, String modelId)
      throws SQLException, UtilException {
    // ajout d'un modèle
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "insert into st_instance_ModelUsed values (?,?)";
      // initialisation des paramètres

      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, modelId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteModel(Connection con, String instanceId)
      throws SQLException, UtilException {
    // suppression de tous les modèles
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "delete from st_instance_ModelUsed where instanceId = ? ";
      // initialisation des paramètres

      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static Collection getModelUsed(Connection con, String instanceId)
      throws SQLException, UtilException {
    ArrayList listModel = new ArrayList();
    String query = "select modelId from st_instance_ModelUsed where instanceId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String modelId = rs.getString(1);
        listModel.add(modelId);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listModel;
  }

}
