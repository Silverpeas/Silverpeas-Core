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

package org.silverpeas.core.contribution.publication.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.ValidationStep;

public class ValidationStepsDAO {

  private static String publicationValidationTableName = "SB_Publication_Validation";

  /**
   * Deletes all validation data of publications linked to the component instance represented by
   * the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(publicationValidationTableName)
        .where("instanceId = ?", componentInstanceId).execute();
  }

  /*
   * id int NOT NULL, pubId int NOT NULL, instanceId varchar(50) NOT NULL, userId int NOT NULL,
   * decisionDate varchar(20) NOT NULL, decision varchar(50) NOT NULL
   */

  public static void addStep(Connection con, ValidationStep step)
      throws SQLException {
    StringBuffer insertStatement = new StringBuffer(128);
    insertStatement.append("insert into ").append(
        publicationValidationTableName).append(" values (?, ?, ?, ?, ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement.toString());

      int id = DBUtil.getNextId(publicationValidationTableName, "id");

      prepStmt.setInt(1, id);
      prepStmt.setInt(2, Integer.parseInt(step.getPubPK().getId()));
      prepStmt.setString(3, step.getPubPK().getInstanceId());
      prepStmt.setInt(4, Integer.parseInt(step.getUserId()));
      prepStmt.setString(5, Long.toString(new Date().getTime()));
      prepStmt.setString(6, step.getDecision());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeSteps(Connection con, PublicationPK pubPK)
      throws SQLException {
    StringBuffer statement = new StringBuffer(128);
    statement.append("delete from ").append(publicationValidationTableName)
        .append(" where pubId = ? and instanceId = ?");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(statement.toString());

      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<ValidationStep> getSteps(Connection con, PublicationPK pubPK)
      throws SQLException {
    List<ValidationStep> steps = new ArrayList<ValidationStep>();

    StringBuffer statement = new StringBuffer(128);
    statement.append("select * from ").append(publicationValidationTableName)
        .append(
        " where pubId = ? and instanceId = ? order by decisionDate desc");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(statement.toString());

      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());
      ResultSet rs = prepStmt.executeQuery();

      while (rs.next()) {
        ValidationStep step = new ValidationStep();

        step.setId(rs.getInt(1));
        step.setPubPK(pubPK);
        step.setUserId(String.valueOf(rs.getInt(4)));
        step.setValidationDate(new Date(Long.parseLong(rs.getString(5))));
        step.setDecision(rs.getString(6));

        steps.add(step);
      }
    } finally {
      DBUtil.close(prepStmt);
    }

    return steps;
  }

  public static ValidationStep getStepByUser(Connection con,
      PublicationPK pubPK, String userId) throws SQLException {
    StringBuffer statement = new StringBuffer(128);
    statement.append("select * from ").append(publicationValidationTableName)
        .append(" where pubId = ? and instanceId = ?");
    statement.append(" and userId = ? order by decisionDate desc");

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(statement.toString());

      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());
      prepStmt.setInt(3, Integer.parseInt(userId));

      ResultSet rs = prepStmt.executeQuery();
      if (rs.next()) {
        ValidationStep step = new ValidationStep();

        step.setId(rs.getInt(1));
        step.setPubPK(pubPK);
        step.setUserId(String.valueOf(rs.getInt(4)));
        step.setValidationDate(new Date(Long.parseLong(rs.getString(5))));
        step.setDecision(rs.getString(6));

        return step;
      }
    } finally {
      DBUtil.close(prepStmt);
    }

    return null;
  }

}