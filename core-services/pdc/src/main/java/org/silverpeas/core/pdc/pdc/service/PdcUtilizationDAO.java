/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.index.search.model.AxisFilter;
import org.silverpeas.core.index.search.model.AxisFilterNode;
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

  private static String PdcUtilizationTable = "SB_Pdc_Utilization";
  private static String TreeTable = "SB_Tree_Tree";

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
    String updateQuery = " update " + PdcUtilizationTable +
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
    String selectQuery =
        " select path from " + TreeTable + " where treeId = " + treeId + " and id = " + baseValue;
    Statement stmt = null;
    ResultSet rs = null;
    String motherPath = "";
    String valuePath = "";
    try {
      stmt = con.createStatement();

      rs = stmt.executeQuery(selectQuery);

      if (rs.next()) {
        motherPath = rs.getString(1);
      }

      valuePath = motherPath + baseValue + "/";
    } finally {
      DBUtil.close(stmt);
    }

    // recherche des valeurs de base utilisées qui sont en fait soit des soeurs soit des nieces
    // de la valeur que l'on veut effacer
    selectQuery =
        " select baseValue from " + PdcUtilizationTable + " where instanceId = '" + instanceId +
            "' and axisId = " + axisId + " and baseValue in " + " ( " + "	select id from " +
            TreeTable + " where treeId = " + treeId + " and path like '" + motherPath +
            "%' and path not like '" + valuePath + "%' and id <> " + baseValue + " ) " +
            " and baseValue <> " + baseValue;

    try {
      stmt = con.createStatement();

      rs = stmt.executeQuery(selectQuery);

      if (rs.next()) {
        // on a donc une soeur ou une niece, on ne peut donc pas remplacer la basevalue par sa mere
        canUpdate = false;
      }

    } finally {
      DBUtil.close(stmt);
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
    List<String> forbiddenValues = new ArrayList<String>();
    boolean isAdded = false;

    // Récupération dans un 1er temps de toutes les valeurs de base
    // qui sont contenues dans l'axe -axisId- du composant -instanceId-
    List<Integer> allBaseValues = getAllBaseValues(con, usedAxisId, instanceId, axisId);
    isAdded = allBaseValues.contains(new Integer(baseValue));

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
          " select path, id from " + TreeTable + " " + whereClause.toString() + ")";



      whereClause = new StringBuilder("where treeId = " + treeId + " and (1=0 "); // on prépare la
      // prochaine clause Where
      String allCompletPathes = "";
      // String qui va nous permettre par la suite de récupérer les valeurs ascendantes
      Statement stmt = null;
      ResultSet rs = null;
      try {
        stmt = con.createStatement();
        rs = stmt.executeQuery(selectQuery);
        while (rs.next()) {
          String path = rs.getString(1);
          String node = Integer.toString(rs.getInt(2));
          allCompletPathes += path;
          whereClause.append(" or path like '").append(path).append(node).append("/%'");
          // on construit la clause Where pour le prochain accès en base
        }
        whereClause.append(")");
      } finally {
        DBUtil.close(rs, stmt);
      }
      // ici, on ajoute dans le vecteur devant contenir toutes les valeurs interdites
      // les valeurs qui sont stockées dans le String allCompletPathes
      StringTokenizer st = new StringTokenizer(allCompletPathes, "/");
      String forbiddenValue = "";
      while (st.hasMoreTokens()) {
        forbiddenValue = st.nextToken();

        forbiddenValues.add(forbiddenValue);
      }

      // maintenant on prepare la requete SQL qui va récupérer toutes les valeurs filles
      // et on va les mettre dans le vecteur de valeurs interdites
      forbiddenValues = getAllDaughterValues(con, forbiddenValues, whereClause.toString());

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
    String updateQuery = " update " + PdcUtilizationTable +
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

  public List<AxisHeader> getAxisUsedByInstanceId(Connection con, List<String> instanceIds)
      throws SQLException {
    return getAxisUsedByInstanceId(con, instanceIds, new AxisFilter());
  }

  public List<AxisHeader> getAxisUsedByInstanceId(Connection con, List<String> instanceIds,
      AxisFilter filter) throws SQLException {


    List<AxisHeader> axisUsed = new ArrayList<>();

    if (instanceIds == null || instanceIds.isEmpty()) {
      return axisUsed;
    }

    String selectStatement =
        "select distinct(A.id), A.RootId, A.Name, A.AxisType, A.AxisOrder, A.description " +
            "from SB_Pdc_Utilization U, SB_Pdc_Axis A " + "where U.axisId = A.id ";

    // la liste instanceIds n'est jamais nulle
    selectStatement += "  and U.instanceId IN (";
    boolean first = true;
    for (String instanceId : instanceIds) {
      if (!first) {
        selectStatement += ",";
      }
      selectStatement += "'" + instanceId + "'";
      first = false;
    }
    selectStatement += " ) ";

    AxisFilterNode condition;
    String property;

    boolean first_condition = true;
    for (int i = 0; i < filter.size(); i++) {
      if (i == 0) {
        condition = filter.getFirstCondition();
      } else {
        condition = filter.getNextCondition();
      }
      property = condition.getPriperty();

      if (AxisFilter.NAME.equals(property)) {
        if (first_condition) {
          selectStatement += " and (A.Name like ? ";
          first_condition = false;
        } else {
          selectStatement += " or A.Name like ? ";
        }
      } else if (AxisFilter.DESCRIPTION.equals(property)) {
        if (first_condition) {
          selectStatement += " and (A.description like ? ";
          first_condition = false;
        } else {
          selectStatement += " or A.description like ? ";
        }
      }
    }

    if (!first_condition) {
      selectStatement += ") ";
    }
    selectStatement += " order by A.AxisType Asc, A.AxisOrder ASC";



    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      int index = 1;
      for (int i = 0; i < filter.size(); i++) {
        if (i == 0) {
          condition = filter.getFirstCondition();
        } else {
          condition = filter.getNextCondition();
        }
        property = condition.getPriperty();
        if (AxisFilter.NAME.equals(property) || AxisFilter.DESCRIPTION.equals(property)) {
          prepStmt.setString(index++, condition.getValue());
        }
      }
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        int axisId = rs.getInt(1);
        int axisRootId = rs.getInt(2);
        String axisName = rs.getString(3);
        String axisType = rs.getString(4);
        int axisOrder = rs.getInt(5);
        String axisDescription = rs.getString(6);

        AxisHeader axisHeader =
            new AxisHeader(Integer.toString(axisId), axisName, axisType, axisOrder, axisRootId,
                axisDescription);
        axisUsed.add(axisHeader);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return axisUsed;
  }

  public void deleteAllAxisUsedByInstanceId(Connection con, String instanceId) throws SQLException {
    final String sqlDeletion = "DELETE FROM " + PdcUtilizationTable + " WHERE instanceId = ?";
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

    String selectQuery = "select baseValue from " + PdcUtilizationTable +
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
   * Returns all daughter values of the selected values
   * @param con - the connection to the database
   * @param forbiddenValues - The vector which contains the values that can't be used
   * @param whereClause - the string of the SQL WHERE clause
   * @return the forbiddenValues updated
   */
  private List<String> getAllDaughterValues(Connection con, List<String> forbiddenValues,
      String whereClause) throws SQLException {
    String selectQuery = "select id from " + TreeTable + " " + whereClause;

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

    return forbiddenValues;
  }
}