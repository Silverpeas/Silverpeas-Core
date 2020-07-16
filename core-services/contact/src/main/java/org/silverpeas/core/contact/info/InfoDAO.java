/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contact.info;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.contact.info.model.InfoPK;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InfoDAO {

  private static final String UNKNOWN = "unknown";

  /**
   * @param con the database connection
   * @param contactPK the contact primary key
   * @param modelId the model identifier
   * @return true if an info already exists
   * @throws SQLException if an error occurs
   */
  private boolean hasInfo(Connection con, ContactPK contactPK, String modelId) throws SQLException {
    final boolean result;
    final InfoPK infoPK = new InfoPK(UNKNOWN, contactPK);
    final String tableName = infoPK.getTableName();
    final String selectStatement =
        "select infoId FROM " + tableName + " WHERE contactId = ? and instanceId = ? " +
            "and modelId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(contactPK.getId()));
      prepStmt.setString(2, contactPK.getComponentName());
      prepStmt.setString(3, modelId);
      try (final ResultSet rs = prepStmt.executeQuery()) {
        result = rs.next();
      }
    }
    return result;
  }

  /**
   * create the info reference
   * match the info with a model
   * @param con the database connection
   * @param modelId the model identifier
   * @param contactPK the contact primary key
   * @return an Info primary key
   * @throws SQLException if an error occurs
   */
  public InfoPK createInfo(Connection con, String modelId, ContactPK contactPK) throws SQLException {
    final InfoPK infoPK = new InfoPK(UNKNOWN, contactPK);
    final String tableName = infoPK.getTableName();
    int newId = DBUtil.getNextId(tableName, "infoId");
    infoPK.setId(Integer.toString(newId));
    if (!hasInfo(con, contactPK, modelId)) {
      String insertStatement = "INSERT INTO " + tableName + " values ( ? , ? , ? , ? )";
      try (final PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
        prepStmt.setInt(1, Integer.parseInt(infoPK.getId()));
        prepStmt.setInt(2, Integer.parseInt(contactPK.getId()));
        prepStmt.setString(3, modelId);
        prepStmt.setString(4, contactPK.getComponentName());
        prepStmt.executeUpdate();
      }
    }
    return infoPK;
  }

  private void deleteInfo(Connection con, InfoPK infoPK) throws SQLException {
    final String deleteStatement = "delete from " + infoPK.getTableName() + " where infoId=?";
    try (final PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.setInt(1, Integer.parseInt(infoPK.getId()));
    }
  }

  public void deleteInfoDetailByContactPK(Connection con, ContactPK contactPK) throws SQLException {
    InfoPK infoPK = new InfoPK(UNKNOWN, contactPK);
    final String tableName = infoPK.getTableName();
    final String selectStatement =
        "select * from " + tableName + " where contactId = ? and instanceId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(contactPK.getId()));
      prepStmt.setString(2, infoPK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        String id;
        while (rs.next()) {
          id = Integer.toString(rs.getInt(1));
          infoPK = new InfoPK(id, contactPK);
          deleteInfo(con, infoPK);
        }
      }
    }
  }

  public void deleteAllInfoByInstanceId(Connection con, String instanceId) throws SQLException {
    final String sql = "DELETE FROM SB_Contact_Info WHERE instanceId = ?";
    try (PreparedStatement deletion = con.prepareStatement(sql)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  public List<String> getInfo(Connection con, ContactPK pubPK) throws SQLException {
    final List<String> modelIds = new ArrayList<>();
    final InfoPK infoPK = new InfoPK(UNKNOWN, pubPK);
    final String tableName = infoPK.getTableName();
    final String selectStatement =
        "select modelId FROM " + tableName + " WHERE contactId = ? and instanceId = ? ";
    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          modelIds.add(rs.getString("modelId"));
        }
      }
    }
    return modelIds;
  }
}