package com.stratelia.webactiv.util.publication.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.ValidationStep;

public class ValidationStepsDAO {

  private static String publicationValidationTableName = "SB_Publication_Validation";

  /*
   * id int NOT NULL, pubId int NOT NULL, instanceId varchar(50) NOT NULL,
   * userId int NOT NULL, decisionDate varchar(20) NOT NULL, decision
   * varchar(50) NOT NULL
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
    } catch (UtilException e) {
      throw new SQLException(e.getMessage());
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

  public static List getSteps(Connection con, PublicationPK pubPK)
      throws SQLException {
    List steps = new ArrayList();

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