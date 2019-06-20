/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author
 */
@Singleton
public class PdcUtilizationDAO {

  private static final String PDC_UTILIZATION_TABLE = "SB_Pdc_Utilization";
  private static final String TREE_TABLE = "SB_Tree_Tree";

  /**
   * Constructor
   */
  protected PdcUtilizationDAO() {
  }

  /**
   * Gets the PdC axis that are used in a content classification in the specified Silverpeas
   * component instance.
   * @param con a connection to the data source.
   * @param instanceId the unique identifier of the component instance.
   * @return a list of axis that can be used in a content classification.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public List<UsedAxis> getUsedAxisByInstanceId(Connection con, String instanceId)
      throws SQLException {
    String selectStatement =
        "select U.id, U.instanceId, U.axisId, U.baseValue, U.mandatory, U.variant, A.Name, A" +
            ".AxisType, A.RootId, T.name " +
            "from SB_Pdc_Utilization U, SB_Pdc_Axis A, SB_Tree_Tree T " + "where U.axisId = A.id " +
            "and A.RootId = T.treeId " + "and U.baseValue = T.id " + "and U.instanceId = ? " +
            "order by A.AxisType Asc, A.AxisOrder ASC";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<UsedAxis> usedAxis = new ArrayList<>();

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        int id = rs.getInt(1);
        instanceId = rs.getString(2);
        int axisId = rs.getInt(3);
        int baseValue = rs.getInt(4);
        int mandatory = rs.getInt(5);
        int variant = rs.getInt(6);
        String axisName = rs.getString(7);
        String axisType = rs.getString(8);
        int axisRootId = rs.getInt(9);
        String valueName = rs.getString(10);
        UsedAxis axis = new UsedAxis(id, instanceId, axisId, baseValue, mandatory, variant);
        axis._setAxisName(axisName);
        axis._setAxisType(axisType);
        axis._setBaseValueName(valueName);
        axis._setAxisRootId(axisRootId);
        usedAxis.add(axis);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return usedAxis;
  }

  /**
   * Updates the base value of the specified hierarchical tree of the given axis.
   * @param con the connection to the database.
   * @param oldBaseValue the old base value of the tree.
   * @param newBaseValue the base value with which the old is replaced.
   * @param axisId the identifier of the axis.
   * @param treeId the identifier of the axis tree.
   * @param instanceId the unique identifier of the Silverpeas component instance that uses the
   * axis.
   * @return the number of rows affected by the update.
   */
  public int updateBaseValue(Connection con, int oldBaseValue, int newBaseValue, int axisId,
      String treeId, String instanceId) throws SQLException {
    String updateQuery = " update " + PDC_UTILIZATION_TABLE +
        " set baseValue = ? where instanceId = ? and axisId = ? and baseValue = ? ";

    PreparedStatement prepStmt = null;
    int nbAffectedRows = 0;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setInt(1, newBaseValue);
      prepStmt.setString(2, instanceId);
      prepStmt.setInt(3, axisId);
      prepStmt.setInt(4, oldBaseValue);

      nbAffectedRows = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    return nbAffectedRows;
  }

  /**
   * Tests if the base value can be updated. No new sisters ou new "niece"s are used in the use of
   * the axis.
   * @param con the connection to the database.
   * @param baseValue the base value to test.
   * @param axisId the identifier of the axis.
   * @param treeId the identifier of the axis tree.
   * @param instanceId the unique identifier of the Silverpeas component instance that uses the
   * axis.
   * @return true if this base value has no new sisters ... not used otherwise false
   */
  public boolean canUpdateBaseValue(Connection con, int baseValue, String axisId, String treeId,
      String instanceId) throws SQLException {

    boolean canUpdate = true;
    // recherche des valeurs soeurs ou nieces qui sont utilisées
    // recherche du chemin de la mere de cette valeur de base et construction de son chemin complet
    String motherPath = "";
    String valuePath = "";
    final String findSiblingValues = "select path from SB_Tree_Tree where treeId = ? and id = ?";
    try (PreparedStatement stmt = con.prepareStatement(findSiblingValues)) {
      stmt.setInt(1, Integer.valueOf(treeId));
      stmt.setInt(2, baseValue);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          motherPath = rs.getString(1);
        }
        valuePath = motherPath + baseValue + "/";
      }
    }

    // recherche des valeurs de base utilisées qui sont en fait soit des soeurs soit des nieces
    // de la valeur que l'on veut effacer
    final String findBaseValues =
        "select baseValue from SB_Pdc_Utilization where instanceId = ? and axisId = ? and " +
            "baseValue in (select id from  SB_Tree_Tree where treeId = ? and path like ? and path" +
            " not like ? and id <> ?) and baseValue <> ?";
    try (PreparedStatement stmt = con.prepareStatement(findBaseValues)) {
      stmt.setString(1, instanceId);
      stmt.setInt(2, Integer.valueOf(axisId));
      stmt.setInt(3, Integer.valueOf(treeId));
      stmt.setString(4, motherPath + "%");
      stmt.setString(5, valuePath + "%");
      stmt.setInt(6, baseValue);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          // on a donc une soeur ou une niece, on ne peut donc pas remplacer la basevalue par sa
          // mere

          canUpdate = false;
        }
      }
    }

    return canUpdate;
  }

  /**
   * Method declaration
   * @param con
   * @param instanceId
   * @param axisId
   * @param baseValue
   * @return
   * @throws SQLException
   */
  public boolean isAlreadyAdded(Connection con, String instanceId, int usedAxisId, int axisId,
      int baseValue, String treeId) throws SQLException {
    List<String> forbiddenValues = new ArrayList<>();
    boolean isAdded = false;

    // Récupération dans un 1er temps de toutes les valeurs de base
    // qui sont contenues dans l'axe -axisId- du composant -instanceId-
    List<Integer> allBaseValues = getAllBaseValues(con, usedAxisId, instanceId, axisId);
    isAdded = allBaseValues.contains(baseValue);

    // ensuite, pour chaque valeur de base récupérée, on cherche toute la filiation
    // de celle-ci. Si le vecteur est vide ou si la valeur que l'on reçoit est
    // déja utilisée alors on peut retourner faux
    if ((!allBaseValues.isEmpty()) && (!isAdded)) {

      // On cherche d'abord le chemin complet de chaque valeur de base
      StringBuilder whereClause = new StringBuilder(" where treeId = " + treeId + " and (1=0 ");
      for (Integer value : allBaseValues) {
        whereClause.append(" or id = ").append(value.toString());
      }
      String selectQuery =
          " select path, id from " + TREE_TABLE + " " + whereClause.toString() + ")";



      whereClause = new StringBuilder("where treeId = " + treeId + " and (1=0 "); // on prépare la
      // prochaine clause Where
      StringBuilder allCompletPathes = new StringBuilder();
      // String qui va nous permettre par la suite de récupérer les valeurs ascendantes
      Statement stmt = null;
      ResultSet rs = null;
      try {
        stmt = con.createStatement();
        rs = stmt.executeQuery(selectQuery);
        while (rs.next()) {
          String path = rs.getString(1);
          String node = Integer.toString(rs.getInt(2));
          allCompletPathes.append(path);
          whereClause.append(" or path like '").append(path).append(node).append("/%'");
          // on construit la clause Where pour le prochain accès en base
        }
        whereClause.append(")");
      } finally {
        DBUtil.close(rs, stmt);
      }
      // ici, on ajoute dans le vecteur devant contenir toutes les valeurs interdites
      // les valeurs qui sont stockées dans le String allCompletPathes
      StringTokenizer st = new StringTokenizer(allCompletPathes.toString(), "/");
      String forbiddenValue = "";
      while (st.hasMoreTokens()) {
        forbiddenValue = st.nextToken();

        forbiddenValues.add(forbiddenValue);
      }

      // maintenant on prepare la requete SQL qui va récupérer toutes les valeurs filles
      // et on va les mettre dans le vecteur de valeurs interdites
      setAllForbiddenValues(con, forbiddenValues, whereClause.toString());

      // on détermine si la valeur que l'on reçoit appartient au vecteur
      isAdded = forbiddenValues.contains(Integer.toString(baseValue));
    }
    return isAdded;
  }

  /**
   * Updates the specified used axis.
   * @param con the connection to the database
   * @param usedAxis the new or modified used axis.
   */
  public void updateAllUsedAxis(Connection con, UsedAxis usedAxis) throws SQLException {
    String updateQuery = " update " + PDC_UTILIZATION_TABLE +
        " set mandatory = ?, variant = ? where instanceId = ? and axisId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setInt(1, usedAxis.getMandatory());
      prepStmt.setInt(2, usedAxis.getVariant());
      prepStmt.setString(3, usedAxis.getInstanceId());
      prepStmt.setInt(4, usedAxis.getAxisId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void deleteAllAxisUsedByInstanceId(Connection con, String instanceId) throws SQLException {
    final String sqlDeletion = "DELETE FROM " + PDC_UTILIZATION_TABLE + " WHERE instanceId = ?";
    try (PreparedStatement deletion = con.prepareStatement(sqlDeletion)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  /**
   * Returns all base values for an axis and an instance of a Silverpeas component.
   * @param con the connection to the database
   * @param usedAxisId the identifier of the axis used by the component instance.
   * @param instanceId the identifier of the instance of the component
   * @param axisId the identifier of the axis.
   * @return a list containing all base values.
   */
  private List<Integer> getAllBaseValues(Connection con, int usedAxisId, String instanceId,
      int axisId) throws SQLException {

    String selectQuery = "select baseValue from " + PDC_UTILIZATION_TABLE +
        " where instanceId = ? and axisId = ? and id <> ? ";

    List<Integer> allBaseValues = new ArrayList<>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, axisId);
      prepStmt.setInt(3, usedAxisId);
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        int baseValue = rs.getInt(1);
        allBaseValues.add(baseValue); // get and stock the result
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return allBaseValues;
  }

  /**
   * Set all forbidden values that matches the specified clause.
   * @param con - the connection to the database
   * @param forbiddenValues - The list which contains the values that can't be used
   * @param whereClause - the string of the SQL WHERE clause
   */
  private void setAllForbiddenValues(Connection con, List<String> forbiddenValues,
      String whereClause) throws SQLException {
    final String selectQuery = "select id from SB_Tree_Tree " + whereClause;

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery);
      while (rs.next()) {
        // Add this value to list
        forbiddenValues.add(Integer.toString(rs.getInt(1)));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}