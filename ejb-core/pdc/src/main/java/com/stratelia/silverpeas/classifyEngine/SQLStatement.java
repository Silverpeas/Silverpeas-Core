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
package com.stratelia.silverpeas.classifyEngine;

import java.util.*;

import com.stratelia.silverpeas.util.JoinStatement;

class SQLStatement extends Object {
  String m_sClassifyTable = "SB_ClassifyEngine_Classify";
  String m_sPositionIdColumn = "PositionId";
  String m_sSilverObjectIdColumn = "ObjectId";
  String m_sAxisColumn = "Axis";
  String m_sBeginDateColumn = "BeginDate";
  String m_sEndDateColumn = "EndDate";
  String m_sIsVisibleColumn = "IsVisible";

  // Load registered axis
  public String buildLoadRegisteredAxisStatement() {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("SELECT * FROM ").append(m_sClassifyTable).append(
        " WHERE (").append(m_sPositionIdColumn).append(" = -1)");

    return sSQLStatement.toString();
  }

  // Register an axis
  public String buildRegisterAxisStatement(int nNextAvailableAxis,
      int nLogicalAxisId) {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("UPDATE ").append(m_sClassifyTable).append(" SET ")
        .append(m_sAxisColumn).append(nNextAvailableAxis).append(" = ").append(
        nLogicalAxisId);
    sSQLStatement.append(" WHERE (").append(m_sPositionIdColumn).append(
        " = -1)");

    return sSQLStatement.toString();
  }

  // Unregister an axis
  public String buildUnregisterAxisStatement(int nAxisId) {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("UPDATE ").append(m_sClassifyTable).append(" SET ")
        .append(m_sAxisColumn).append(nAxisId).append(" = -1 WHERE ").append(
        m_sPositionIdColumn).append(" = -1");
    return sSQLStatement.toString();
  }

  // Build the SQL statement to classify the object
  public String buildClassifyStatement(int nSilverObjectId, Position position,
      int nNextPositionId) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    List alValues = position.getValues();

    // Build the SQL statement to classify the object
    sSQLStatement.append("INSERT INTO ").append(m_sClassifyTable).append(" (")
        .append(m_sPositionIdColumn).append(", ").append(
        m_sSilverObjectIdColumn).append(", ");

    // visibility attributes
    // sSQLStatement.append(m_sBeginDateColumn + ", " + m_sEndDateColumn + ", "
    // + m_sIsVisibleColumn + ", ");

    for (int nI = 0; nI < alValues.size(); nI++) {
      sSQLStatement.append(m_sAxisColumn).append(
          ((Value) alValues.get(nI)).getPhysicalAxisId());
      if (nI < alValues.size() - 1)
        sSQLStatement.append(", ");
      else
        sSQLStatement.append(") ");
    }

    // Put the values
    sSQLStatement.append("VALUES(").append(nNextPositionId).append(", ")
        .append(nSilverObjectId).append(", ");

    // visibility attributes
    // sSQLStatement.append("'0000/00/00', '9999/99/99', 1, ");

    if (alValues.get(0) != null)
      sSQLStatement.append("'");
    for (int nI = 0; nI < alValues.size(); nI++) {
      sSQLStatement.append(((Value) alValues.get(nI)).getValue());
      if (nI < alValues.size() - 1)
        if (((Value) alValues.get(nI)).getValue() == null)
          if (((Value) alValues.get(nI + 1)).getValue() != null)
            sSQLStatement.append(", '");
          else
            sSQLStatement.append(", ");
        else if (((Value) alValues.get(nI + 1)).getValue() != null)
          sSQLStatement.append("', '");
        else
          sSQLStatement.append("', ");

      else if (((Value) alValues.get(nI)).getValue() == null)
        sSQLStatement.append(") ");
      else
        sSQLStatement.append("') ");
    }

    return sSQLStatement.toString();
  }

  // Build the SQL statement to get a position
  public String buildVerifyStatement(int nSilverObjectId, Position position) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    List alValues = position.getValues();

    // Build the SQL statement to classify the object
    sSQLStatement.append("SELECT ").append(m_sPositionIdColumn)
        .append(" FROM ").append(m_sClassifyTable);

    // Put the values
    sSQLStatement.append(" WHERE (");
    Value value = null;
    for (int nI = 0; nI < alValues.size(); nI++) {
      value = (Value) alValues.get(nI);
      sSQLStatement.append(m_sAxisColumn).append(value.getPhysicalAxisId());
      sSQLStatement.append(" = '").append(value.getValue()).append("'");
      if (nI < alValues.size() - 1)
        sSQLStatement.append(" AND ");
    }
    sSQLStatement.append(")");

    sSQLStatement.append(" AND ").append(m_sSilverObjectIdColumn).append(" = ")
        .append(nSilverObjectId);

    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveByPositionStatement(int nSilverObjectId,
      Position position) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    List alValues = position.getValues();

    // Build the SQL statement to remove the object
    sSQLStatement.append("DELETE FROM ").append(m_sClassifyTable).append(
        " WHERE (").append(m_sSilverObjectIdColumn).append("=").append(
        nSilverObjectId).append(" AND ");
    for (int nI = 0; nI < alValues.size(); nI++) {
      sSQLStatement.append(m_sAxisColumn).append(
          ((Value) alValues.get(nI)).getAxisId()).append("='").append(
          ((Value) alValues.get(nI)).getValue()).append("'");
      if (nI < alValues.size() - 1)
        sSQLStatement.append(" AND ");
      else
        sSQLStatement.append(")");
    }
    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveSilverObjectStatement(int nSilverObjectId) {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("DELETE FROM ").append(m_sClassifyTable).append(
        " WHERE (").append(m_sSilverObjectIdColumn).append("=").append(
        nSilverObjectId).append(")");

    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveByPositionIdStatement(int nPositionId) {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("DELETE FROM ").append(m_sClassifyTable).append(
        " WHERE (").append(m_sPositionIdColumn).append("=").append(nPositionId)
        .append(")");

    return sSQLStatement.toString();
  }

  // Update a Position with the given one for the given SilverObjectId
  public String buildUpdateByPositionIdStatement(Position newPosition) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("UPDATE ").append(m_sClassifyTable).append(" SET ")
        .append(m_sPositionIdColumn).append(" = ").append(
        newPosition.getPositionId()).append(", ");
    List alValues = newPosition.getValues();
    for (int nI = 0; nI < alValues.size(); nI++) {
      Value oneValue = (Value) alValues.get(nI);
      sSQLStatement.append(m_sAxisColumn).append(oneValue.getPhysicalAxisId());
      String value = oneValue.getValue();
      if (value.equals("-")) {
        value = null;
        sSQLStatement.append(" = ").append(value);
      } else {
        sSQLStatement.append(" = '").append(value).append("'");
      }

      if (nI < alValues.size() - 1)
        sSQLStatement.append(", ");
    }

    sSQLStatement.append(" WHERE ").append(m_sPositionIdColumn).append(" = ")
        .append(newPosition.getPositionId());

    return sSQLStatement.toString();
  }

  // Update several Positions for the given SilverObjectId
  // only if the value is invariant
  // add by SAN
  public String buildUpdateByObjectIdStatement(Value value, int nSilverObjectId) {

    String sSQLStatement = "UPDATE " + m_sClassifyTable;
    sSQLStatement += " SET " + m_sAxisColumn + value.getAxisId() + " = '"
        + value.getValue() + "' ";
    sSQLStatement += " WHERE " + m_sSilverObjectIdColumn + " =  "
        + nSilverObjectId;
    sSQLStatement += " AND " + m_sAxisColumn + value.getAxisId()
        + " IS NOT NULL";

    return sSQLStatement;
  }

  public String buildSilverContentIdsByPositionIdsStatement(List alPositionIds) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("SELECT ").append(m_sSilverObjectIdColumn).append(
        " FROM ").append(m_sClassifyTable);
    if (alPositionIds.size() != 0)
      sSQLStatement.append(" WHERE ");
    for (int nI = 0; nI < alPositionIds.size(); nI++) {
      sSQLStatement.append("(").append(m_sPositionIdColumn).append(" = ")
          .append(((Integer) alPositionIds.get(0)).intValue()).append(")");
      if (nI < alPositionIds.size() - 1)
        sSQLStatement.append(" OR ");
    }
    if (alPositionIds.size() != 0)
      sSQLStatement.append(" GROUP BY ").append(m_sSilverObjectIdColumn);

    return sSQLStatement.toString();
  }

  public String buildFindByCriteriasStatementByJoin(List alCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String todayFormatted) {
    return buildFindByCriteriasStatementByJoin(alCriterias,
        joinStatementContainer, joinStatementContent, todayFormatted, true);
  }

  /*
   * Build the SQL statement to find the objects.
   * @param recursiveSearch if true, the search will be made on value and all subvalues (first and
   * classic Silverpeas implementation). If set to false, the search will be made only one value
   * (useful for taglib functionality)
   * @return the Sql query string
   */
  public String buildFindByCriteriasStatementByJoin(List alCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String todayFormatted, boolean recursiveSearch) {
    return buildFindByCriteriasStatementByJoin(alCriterias,
        joinStatementContainer, joinStatementContent, todayFormatted,
        recursiveSearch, true);
  }

  public String buildFindByCriteriasStatementByJoin(List alCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String todayFormatted, boolean recursiveSearch,
      boolean visibilitySensitive) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    String containerMgrLinks = joinStatementContainer.getTable(0);
    String containerMgrLinksKey = joinStatementContainer.getJoinKey(0);
    String contentMgr = joinStatementContent.getTable(0);
    String contentMgrKey = joinStatementContent.getJoinKey(0);

    String whereClause = "";
    sSQLStatement.append(" SELECT CEC.").append(m_sSilverObjectIdColumn)
        .append(" FROM ").append(m_sClassifyTable).append(" CEC,").append(
        containerMgrLinks).append(" CML,").append(contentMgr)
        .append(" CMC");
    sSQLStatement.append(" WHERE ");
    sSQLStatement.append(" CEC.").append(m_sPositionIdColumn).append(" = CML.")
        .append(containerMgrLinksKey);
    sSQLStatement.append(" AND CEC.").append(m_sSilverObjectIdColumn).append(
        " = CMC.").append(contentMgrKey);
    // works on the container statement
    whereClause = joinStatementContainer.getWhere();
    if (!whereClause.equals("")) {
      sSQLStatement.append(" AND ").append(whereClause);
    }

    // works on the content statement
    whereClause = joinStatementContent.getWhere();
    if (!whereClause.equals("")) {
      sSQLStatement.append(" AND ").append(whereClause);
    }

    // criteres
    for (int nI = 0; nI < alCriterias.size(); nI++) {
      if (((Criteria) alCriterias.get(nI)).getValue() != null) {
        sSQLStatement.append(" AND (").append(m_sAxisColumn).append(
            ((Criteria) alCriterias.get(nI)).getAxisId());
        if (recursiveSearch)
          sSQLStatement.append(" LIKE '");
        else
          sSQLStatement.append(" = '");
        sSQLStatement.append(((Criteria) alCriterias.get(nI)).getValue());
        if (recursiveSearch)
          sSQLStatement.append("%')");
        else
          sSQLStatement.append("')");
      }
    }

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");

    if (visibilitySensitive)
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");

    sSQLStatement.append(" GROUP BY CEC.").append(m_sSilverObjectIdColumn);

    whereClause = null;

    return sSQLStatement.toString();
  }

  // Build the SQL statement to find the objects
  public String buildFindBySilverObjectIdStatement(int nSilverObjectId) {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("SELECT * FROM ").append(m_sClassifyTable).append(
        " WHERE (").append(m_sSilverObjectIdColumn).append("=").append(
        nSilverObjectId).append(")");

    return sSQLStatement.toString();
  }

  // Remove the values on all the positions of the given axis
  public String buildRemoveAllPositionValuesStatement(int nAxisId) {
    StringBuffer sSQLStatement = new StringBuffer(100);
    sSQLStatement.append("UPDATE ").append(m_sClassifyTable).append(" SET ")
        .append(m_sAxisColumn).append(nAxisId).append(" = null WHERE NOT ")
        .append(m_sPositionIdColumn).append(" = -1");

    return sSQLStatement.toString();
  }

  // Return the positionId of all the deleted empty positions
  public String buildGetEmptyPositionsStatement(int nbMaxAxis) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("SELECT ").append(m_sPositionIdColumn)
        .append(" FROM ").append(m_sClassifyTable).append(" WHERE ");
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      sSQLStatement.append("(").append(m_sAxisColumn).append(nI).append(
          " IS NULL)");
      if (nI < nbMaxAxis - 1)
        sSQLStatement.append(" AND ");
    }

    return sSQLStatement.toString();
  }

  // Remove all the positions with all the values at null
  public String buildRemoveEmptyPositionsStatement(int nbMaxAxis) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("DELETE FROM ").append(m_sClassifyTable).append(
        " WHERE ");
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      sSQLStatement.append("(").append(m_sAxisColumn).append(nI).append(
          " IS NULL)");
      if (nI < nbMaxAxis - 1)
        sSQLStatement.append(" AND ");
    }

    return sSQLStatement.toString();
  }

  // Replace the oldValue with the newValue
  public String buildReplaceValuesStatement(Value oldValue, Value newValue) {
    StringBuffer sSQLStatement = new StringBuffer(1000);

    // Build the SQL statement to remove the values
    if (newValue.getValue() != null)
      sSQLStatement.append("UPDATE ").append(m_sClassifyTable).append(" SET ")
          .append(m_sAxisColumn).append(newValue.getAxisId()).append(" = '")
          .append(newValue.getValue()).append("'");
    else
      sSQLStatement.append("UPDATE ").append(m_sClassifyTable).append(" SET ")
          .append(m_sAxisColumn).append(newValue.getAxisId()).append(" = ")
          .append(newValue.getValue());

    if (oldValue.getValue() != null)
      sSQLStatement.append(" WHERE (").append(m_sAxisColumn).append(
          oldValue.getAxisId()).append(" = '").append(oldValue.getValue())
          .append("')");
    else
      sSQLStatement.append(" WHERE (").append(m_sAxisColumn).append(
          oldValue.getAxisId()).append(" IS NULL)");

    return sSQLStatement.toString();
  }

  // Get the pertinent Axis corresponding to the criterias
  public String buildGetPertinentAxisStatement(List alCriterias, int nAxisId,
      String todayFormatted) {
    return buildGetPertinentAxisStatement(alCriterias, nAxisId, todayFormatted,
        true);
  }

  public String buildGetPertinentAxisStatement(List alCriterias, int nAxisId,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuffer sSQLStatement = new StringBuffer(1000);

    sSQLStatement.append("SELECT COUNT(*) FROM ").append(m_sClassifyTable)
        .append(" CEC, SB_ContentManager_Content CMC WHERE CEC.").append(
        m_sSilverObjectIdColumn).append(" = CMC.silverContentId AND (CEC.")
        .append(m_sPositionIdColumn).append(" <> -1) ");
    for (int nI = 0; alCriterias != null && nI < alCriterias.size(); nI++)

      if (((Criteria) alCriterias.get(nI)).getValue() != null)
        sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(
            ((Criteria) alCriterias.get(nI)).getAxisId()).append(" LIKE '")
            .append(((Criteria) alCriterias.get(nI)).getValue()).append("%')");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive)
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");

    // Set the pertinent axiom
    sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(nAxisId)
        .append(" IS NOT NULL)");

    return sSQLStatement.toString();
  }

  // Get the pertinent Axis corresponding to the criterias
  public String buildGetPertinentAxisStatementByJoin(List alCriterias,
      int nAxisId, String sRootValue, JoinStatement joinStatementAllPositions,
      String todayFormatted) {
    return buildGetPertinentAxisStatementByJoin(alCriterias, nAxisId,
        sRootValue, joinStatementAllPositions, todayFormatted, true);
  }

  public String buildGetPertinentAxisStatementByJoin(List alCriterias,
      int nAxisId, String sRootValue, JoinStatement joinStatementAllPositions,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    String containerMgrLinks = joinStatementAllPositions.getTable(0);
    String containerMgrLinksKey = joinStatementAllPositions.getJoinKey(0);

    sSQLStatement.append("SELECT COUNT(DISTINCT CEC.").append(
        m_sSilverObjectIdColumn).append(")");
    sSQLStatement.append(" FROM ").append(m_sClassifyTable).append(" CEC, ")
        .append(containerMgrLinks).append(
        " CML, SB_ContentManager_Content CMC ");
    sSQLStatement.append(" WHERE CEC.").append(m_sPositionIdColumn).append(
        " = CML.").append(containerMgrLinksKey);
    sSQLStatement.append(" AND CEC.").append(m_sSilverObjectIdColumn).append(
        " = CMC.silverContentId ");
    String whereClause = joinStatementAllPositions.getWhere();
    if ((whereClause != null) && (!whereClause.equals(""))) {
      sSQLStatement.append(" AND ").append(whereClause);
    }
    for (int nI = 0; alCriterias != null && nI < alCriterias.size(); nI++)
      if (((Criteria) alCriterias.get(nI)).getValue() != null)
        sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(
            ((Criteria) alCriterias.get(nI)).getAxisId()).append(" LIKE '")
            .append(((Criteria) alCriterias.get(nI)).getValue()).append("%')");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive)
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");

    // Set the pertinent axiom
    if (sRootValue.length() == 0)
      sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(nAxisId)
          .append(" IS NOT NULL)");
    else
      sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(nAxisId)
          .append(" LIKE '").append(sRootValue).append("%')");

    return sSQLStatement.toString();
  }

  // Get the pertinent Value corresponding to the criterias
  public String buildGetPertinentValueStatement(List alCriterias, int nAxisId,
      String todayFormatted) {
    return buildGetPertinentValueStatement(alCriterias, nAxisId,
        todayFormatted, true);
  }

  public String buildGetPertinentValueStatement(List alCriterias, int nAxisId,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("SELECT COUNT(CEC.").append(m_sSilverObjectIdColumn)
        .append("), ").append(m_sAxisColumn).append(nAxisId).append(" FROM ")
        .append(m_sClassifyTable).append("CEC, SB_ContentManager_Content CMC ");
    sSQLStatement.append("WHERE CEC.").append(m_sSilverObjectIdColumn).append(
        " = CMC.silverContentId ");
    for (int nI = 0; alCriterias.size() != 0 && nI < alCriterias.size(); nI++)
      if (((Criteria) alCriterias.get(nI)).getValue() != null) {
        sSQLStatement.append("(CEC.").append(m_sAxisColumn).append(
            ((Criteria) alCriterias.get(nI)).getAxisId()).append(" LIKE '")
            .append(((Criteria) alCriterias.get(nI)).getValue()).append("%')");
        if (nI < alCriterias.size() - 1)
          sSQLStatement.append(" AND ");
      }

    // Set the pertinent axiom
    if (alCriterias.size() != 0)
      sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(nAxisId)
          .append(" IS NOT NULL)");
    else
      sSQLStatement.append("(CEC.").append(m_sAxisColumn).append(nAxisId)
          .append(" IS NOT NULL)");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive)
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");

    // Group By
    sSQLStatement.append(" GROUP BY CEC.").append(m_sAxisColumn)
        .append(nAxisId);

    return sSQLStatement.toString();
  }

  // Get the pertinent Value corresponding to the criterias
  public String buildGetPertinentValueByJoinStatement(List alCriterias,
      int nAxisId, JoinStatement joinStatementAllPositions,
      String todayFormatted) {
    return buildGetPertinentValueByJoinStatement(alCriterias, nAxisId,
        joinStatementAllPositions, todayFormatted, true);
  }

  public String buildGetPertinentValueByJoinStatement(List alCriterias,
      int nAxisId, JoinStatement joinStatementAllPositions,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("SELECT COUNT(DISTINCT CEC.").append(
        m_sSilverObjectIdColumn).append("), CEC.").append(m_sAxisColumn)
        .append(nAxisId);
    sSQLStatement.append(" FROM ").append(m_sClassifyTable).append(" CEC, ")
        .append(joinStatementAllPositions.getTable(0)).append(
        " CML, SB_ContentManager_Content CMC ");
    sSQLStatement.append(" WHERE CEC.").append(m_sPositionIdColumn).append(
        " = CML.").append(joinStatementAllPositions.getJoinKey(0));
    sSQLStatement.append(" AND CEC.").append(m_sSilverObjectIdColumn).append(
        " = CMC.silverContentId ");
    String whereClause = joinStatementAllPositions.getWhere();
    if ((whereClause != null) && (!whereClause.equals(""))) {
      sSQLStatement.append(" AND ").append(whereClause);
    }

    for (int nI = 0; alCriterias.size() != 0 && nI < alCriterias.size(); nI++)
      if (((Criteria) alCriterias.get(nI)).getValue() != null)
        sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(
            ((Criteria) alCriterias.get(nI)).getAxisId()).append(" LIKE '")
            .append(((Criteria) alCriterias.get(nI)).getValue()).append("%')");

    // Set the pertinent axiom
    sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(nAxisId)
        .append(" IS NOT NULL)");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive)
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");

    // Group By
    sSQLStatement.append(" GROUP BY CEC.").append(m_sAxisColumn)
        .append(nAxisId);

    return sSQLStatement.toString();
  }

  public String buildGetObjectValuePairsByJoinStatement(List alCriterias,
      int nAxisId, JoinStatement joinStatementAllPositions,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    sSQLStatement.append("SELECT CMC.internalContentId").append(", CEC.")
        .append(m_sAxisColumn).append(nAxisId).append(", CMI.componentId");
    sSQLStatement
        .append(" FROM ")
        .append(m_sClassifyTable)
        .append(" CEC, ")
        .append(joinStatementAllPositions.getTable(0))
        .append(
        " CML, SB_ContentManager_Content CMC, SB_ContentManager_Instance CMI ");
    sSQLStatement.append(" WHERE CEC.").append(m_sPositionIdColumn).append(
        " = CML.").append(joinStatementAllPositions.getJoinKey(0));
    sSQLStatement.append(" AND CEC.").append(m_sSilverObjectIdColumn).append(
        " = CMC.silverContentId ");
    sSQLStatement.append(" AND CMC.contentInstanceId").append(
        " = CMI.instanceId ");
    String whereClause = joinStatementAllPositions.getWhere();
    if ((whereClause != null) && (!whereClause.equals(""))) {
      sSQLStatement.append(" AND ").append(whereClause);
    }

    for (int nI = 0; alCriterias.size() != 0 && nI < alCriterias.size(); nI++)
      if (((Criteria) alCriterias.get(nI)).getValue() != null)
        sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(
            ((Criteria) alCriterias.get(nI)).getAxisId()).append(" LIKE '")
            .append(((Criteria) alCriterias.get(nI)).getValue()).append("%')");

    // Set the pertinent axiom
    sSQLStatement.append(" AND (CEC.").append(m_sAxisColumn).append(nAxisId)
        .append(" IS NOT NULL)");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive)
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");

    // Group By
    // sSQLStatement.append(" GROUP BY CEC.").append(m_sAxisColumn).append(nAxisId);

    return sSQLStatement.toString();
  }
}