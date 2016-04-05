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

package org.silverpeas.core.pdc.classification;

import org.silverpeas.core.util.JoinStatement;

import java.util.List;

import static org.silverpeas.core.util.StringUtil.isDefined;

class SQLStatement {

  private static final String CLASSIFICATION_TABLE = "SB_ClassifyEngine_Classify";
  private static final String POSITION_ID_COLUMN = "PositionId";
  private static final String SILVEROBJECT_ID_COLUMN = "ObjectId";
  private static final String AXIS_COLUMN = "Axis";
  private static final int axisMaxNumber = 49;

  // Load registered axis
  public String buildLoadRegisteredAxisStatement() {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("SELECT * FROM ").append(CLASSIFICATION_TABLE).append(
        " WHERE (").append(POSITION_ID_COLUMN).append(" = -1)");

    return sSQLStatement.toString();
  }

  // Register an axis
  public String buildRegisterAxisStatement(int nNextAvailableAxis,
      int nLogicalAxisId) {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("UPDATE ").append(CLASSIFICATION_TABLE).append(" SET ")
        .append(AXIS_COLUMN).
        append(nNextAvailableAxis).append(" = ").append(
        nLogicalAxisId);
    sSQLStatement.append(" WHERE (").append(POSITION_ID_COLUMN).append(
        " = -1)");

    return sSQLStatement.toString();
  }

  // Unregister an axis
  public String buildUnregisterAxisStatement(int nAxisId) {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("UPDATE ").append(CLASSIFICATION_TABLE).append(" SET ")
        .append(AXIS_COLUMN).
        append(nAxisId).append(" = -1 WHERE ").append(
        POSITION_ID_COLUMN).append(" = -1");
    return sSQLStatement.toString();
  }

  // Build the SQL statement to classify the object
  public String buildClassifyStatement(int nSilverObjectId, Position<Value> position,
      int nNextPositionId) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    List<Value> alValues = position.getValues();

    // Build the SQL statement to classify the object
    sSQLStatement.append("INSERT INTO ").append(CLASSIFICATION_TABLE).append(" (").append(
        POSITION_ID_COLUMN).append(", ").append(
        SILVEROBJECT_ID_COLUMN).append(", ");

    for (int nI = 0; nI < alValues.size(); nI++) {
      sSQLStatement.append(AXIS_COLUMN).append(alValues.get(nI).getPhysicalAxisId());
      if (nI < alValues.size() - 1) {
        sSQLStatement.append(", ");
      } else {
        sSQLStatement.append(") ");
      }
    }

    // Put the values
    sSQLStatement.append("VALUES(").append(nNextPositionId).append(", ").append(nSilverObjectId).
        append(", ");

    if (alValues.get(0) != null) {
      sSQLStatement.append("'");
    }
    for (int nI = 0; nI < alValues.size(); nI++) {
      sSQLStatement.append(alValues.get(nI).getValue());
      if (nI < alValues.size() - 1) {
        if (alValues.get(nI).getValue() == null) {
          if (alValues.get(nI + 1).getValue() != null) {
            sSQLStatement.append(", '");
          } else {
            sSQLStatement.append(", ");
          }
        } else if (alValues.get(nI + 1).getValue() != null) {
          sSQLStatement.append("', '");
        } else {
          sSQLStatement.append("', ");
        }
      } else if (alValues.get(nI).getValue() == null) {
        sSQLStatement.append(") ");
      } else {
        sSQLStatement.append("') ");
      }
    }

    return sSQLStatement.toString();
  }

  // Build the SQL statement to get a position
  public String buildVerifyStatement(int nSilverObjectId, Position<Value> position) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    List<Value> alValues = position.getValues();

    // Build the SQL statement to classify the object
    sSQLStatement.append("SELECT ").append(POSITION_ID_COLUMN).append(" FROM ").append(
        CLASSIFICATION_TABLE);

    // Put the values for the concerning axis, put null for the other non-valued axis
    sSQLStatement.append(" WHERE (");
    for (int axis = 0; axis <= axisMaxNumber; axis++) {
      Value foundValue = null;
      for (Value aValue : alValues) {
        if (aValue.getPhysicalAxisId() == axis) {
          foundValue = aValue;
          break;
        }
      }
      String axisValue = null;
      if (foundValue != null) {
        axisValue = "'" + foundValue.getValue() + "'";
      }
      sSQLStatement.append(AXIS_COLUMN).append(axis).append(" = ").append(axisValue);
      if (axis < axisMaxNumber) {
        sSQLStatement.append(" AND ");
      }

    }
    sSQLStatement.append(")");
    sSQLStatement.append(" AND ").append(SILVEROBJECT_ID_COLUMN).append(" = ").append(
        nSilverObjectId);

    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveByPositionStatement(int nSilverObjectId,
      Position<Value> position) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    List<Value> alValues = position.getValues();

    // Build the SQL statement to remove the object
    sSQLStatement.append("DELETE FROM ").append(CLASSIFICATION_TABLE).append(
        " WHERE (").append(SILVEROBJECT_ID_COLUMN).append("=").append(
        nSilverObjectId).append(" AND ");
    for (int nI = 0; nI < alValues.size(); nI++) {
      sSQLStatement.append(AXIS_COLUMN).append(
          alValues.get(nI).getAxisId()).append("='").append(
          alValues.get(nI).getValue()).append("'");
      if (nI < alValues.size() - 1) {
        sSQLStatement.append(" AND ");
      } else {
        sSQLStatement.append(")");
      }
    }
    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveSilverObjectStatement(int nSilverObjectId) {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("DELETE FROM ").append(CLASSIFICATION_TABLE).append(
        " WHERE (").append(SILVEROBJECT_ID_COLUMN).append("=").append(
        nSilverObjectId).append(")");

    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveByPositionIdStatement(int nPositionId) {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("DELETE FROM ").append(CLASSIFICATION_TABLE).append(
        " WHERE (").append(POSITION_ID_COLUMN).append("=").append(nPositionId).append(")");

    return sSQLStatement.toString();
  }

  // Update a Position with the given one for the given SilverObjectId
  public String buildUpdateByPositionIdStatement(Position<Value> newPosition) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("UPDATE ").append(CLASSIFICATION_TABLE).append(" SET ").append(
        POSITION_ID_COLUMN).append(" = ").append(
        newPosition.getPositionId()).append(", ");
    List<Value> allValues = newPosition.getValues();
    int valuesTakenInCharge = 0;
    for (int axisId = 0; axisId < axisMaxNumber; axisId++) {
      String value = null;
      if (valuesTakenInCharge < allValues.size()) {
        for (Value aValue : allValues) {
          if (aValue.getPhysicalAxisId() == axisId) {
            value = aValue.getValue();
            valuesTakenInCharge++;
            break;
          }
        }
      }
      sSQLStatement.append(AXIS_COLUMN).append(axisId);
      if (value == null || value.equals("-")) {
        sSQLStatement.append(" = ").append(value);
      } else {
        sSQLStatement.append(" = '").append(value).append("'");
      }

      if (axisId < axisMaxNumber - 1) {
        sSQLStatement.append(", ");
      }
    }

    sSQLStatement.append(" WHERE ").append(POSITION_ID_COLUMN).append(" = ").append(newPosition.
        getPositionId());

    return sSQLStatement.toString();
  }

  // Update several Positions for the given SilverObjectId
  // only if the value is invariant
  // add by SAN
  public String buildUpdateByObjectIdStatement(Value value, int nSilverObjectId) {

    String sSQLStatement = "UPDATE " + CLASSIFICATION_TABLE;
    sSQLStatement += " SET " + AXIS_COLUMN + value.getAxisId() + " = '"
        + value.getValue() + "' ";
    sSQLStatement += " WHERE " + SILVEROBJECT_ID_COLUMN + " =  "
        + nSilverObjectId;
    sSQLStatement += " AND " + AXIS_COLUMN + value.getAxisId()
        + " IS NOT NULL";

    return sSQLStatement;
  }

  public String buildSilverContentIdsByPositionIdsStatement(List<Integer> alPositionIds) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT ").append(SILVEROBJECT_ID_COLUMN).append(
        " FROM ").append(CLASSIFICATION_TABLE);
    if (!alPositionIds.isEmpty()) {
      sSQLStatement.append(" WHERE ");
    }
    for (int nI = 0; nI < alPositionIds.size(); nI++) {
      sSQLStatement.append("(").append(POSITION_ID_COLUMN).append(" = ").append(
          alPositionIds.get(0).
          intValue()).append(")");
      if (nI < alPositionIds.size() - 1) {
        sSQLStatement.append(" OR ");
      }
    }
    if (!alPositionIds.isEmpty()) {
      sSQLStatement.append(" GROUP BY ").append(SILVEROBJECT_ID_COLUMN);
    }

    return sSQLStatement.toString();
  }

  public String buildFindByCriteriasStatementByJoin(List<Criteria> alCriterias,
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
  public String buildFindByCriteriasStatementByJoin(List<Criteria> alCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String todayFormatted, boolean recursiveSearch) {
    return buildFindByCriteriasStatementByJoin(alCriterias,
        joinStatementContainer, joinStatementContent, todayFormatted,
        recursiveSearch, true);
  }

  public String buildFindByCriteriasStatementByJoin(List<Criteria> alCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String todayFormatted, boolean recursiveSearch,
      boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    String containerMgrLinks = joinStatementContainer.getTable(0);
    String containerMgrLinksKey = joinStatementContainer.getJoinKey(0);
    String contentMgr = joinStatementContent.getTable(0);
    String contentMgrKey = joinStatementContent.getJoinKey(0);

    String whereClause = "";
    sSQLStatement.append(" SELECT CEC.").append(SILVEROBJECT_ID_COLUMN).append(" FROM ").append(
        CLASSIFICATION_TABLE).append(" CEC,").append(
        containerMgrLinks).append(" CML,").append(contentMgr).append(" CMC");
    sSQLStatement.append(" WHERE ");
    sSQLStatement.append(" CEC.").append(POSITION_ID_COLUMN).append(" = CML.").append(
        containerMgrLinksKey);
    sSQLStatement.append(" AND CEC.").append(SILVEROBJECT_ID_COLUMN).append(
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
    for (Criteria alCriteria : alCriterias) {
      if (alCriteria.getValue() != null) {
        sSQLStatement.append(" AND (").append(AXIS_COLUMN).append(
            alCriteria.getAxisId());
        if (recursiveSearch) {
          sSQLStatement.append(" LIKE '");
        } else {
          sSQLStatement.append(" = '");
        }
        sSQLStatement.append(alCriteria.getValue());
        if (recursiveSearch) {
          sSQLStatement.append("%')");
        } else {
          sSQLStatement.append("')");
        }
      }
    }

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");

    if (visibilitySensitive) {
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");
    }

    sSQLStatement.append(" GROUP BY CEC.").append(SILVEROBJECT_ID_COLUMN);

    whereClause = null;

    return sSQLStatement.toString();
  }

  // Build the SQL statement to find the objects
  public String buildFindBySilverObjectIdStatement(int nSilverObjectId) {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("SELECT * FROM ").append(CLASSIFICATION_TABLE).append(
        " WHERE (").append(SILVEROBJECT_ID_COLUMN).append("=").append(
        nSilverObjectId).append(")");

    return sSQLStatement.toString();
  }

  // Remove the values on all the positions of the given axis
  public String buildRemoveAllPositionValuesStatement(int nAxisId) {
    StringBuilder sSQLStatement = new StringBuilder(100);
    sSQLStatement.append("UPDATE ").append(CLASSIFICATION_TABLE).append(" SET ")
        .append(AXIS_COLUMN).
        append(nAxisId).append(" = null WHERE NOT ").append(POSITION_ID_COLUMN).append(" = -1");

    return sSQLStatement.toString();
  }

  // Return the positionId of all the deleted empty positions
  public String buildGetEmptyPositionsStatement(int nbMaxAxis) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT ").append(POSITION_ID_COLUMN).append(" FROM ").append(
        CLASSIFICATION_TABLE).append(" WHERE ");
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      sSQLStatement.append("(").append(AXIS_COLUMN).append(nI).append(
          " IS NULL)");
      if (nI < nbMaxAxis - 1) {
        sSQLStatement.append(" AND ");
      }
    }

    return sSQLStatement.toString();
  }

  // Remove all the positions with all the values at null
  public String buildRemoveEmptyPositionsStatement(int nbMaxAxis) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("DELETE FROM ").append(CLASSIFICATION_TABLE).append(
        " WHERE ");
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      sSQLStatement.append("(").append(AXIS_COLUMN).append(nI).append(
          " IS NULL)");
      if (nI < nbMaxAxis - 1) {
        sSQLStatement.append(" AND ");
      }
    }

    return sSQLStatement.toString();
  }

  // Replace the oldValue with the newValue
  public String buildReplaceValuesStatement(Value oldValue, Value newValue) {
    StringBuilder sSQLStatement = new StringBuilder(1000);

    // Build the SQL statement to remove the values
    if (newValue.getValue() != null) {
      sSQLStatement.append("UPDATE ").append(CLASSIFICATION_TABLE).append(" SET ").append(
          AXIS_COLUMN).
          append(newValue.getAxisId()).append(" = '").append(newValue.getValue()).append("'");
    } else {
      sSQLStatement.append("UPDATE ").append(CLASSIFICATION_TABLE).append(" SET ").append(
          AXIS_COLUMN).
          append(newValue.getAxisId()).append(" = ").append(newValue.getValue());
    }

    if (oldValue.getValue() != null) {
      sSQLStatement.append(" WHERE (").append(AXIS_COLUMN).append(
          oldValue.getAxisId()).append(" = '").append(oldValue.getValue()).append("')");
    } else {
      sSQLStatement.append(" WHERE (").append(AXIS_COLUMN).append(
          oldValue.getAxisId()).append(" IS NULL)");
    }

    return sSQLStatement.toString();
  }

  // Get the pertinent Axis corresponding to the criterias
  public String buildGetPertinentAxisStatement(List<Criteria> alCriterias, int nAxisId,
      String todayFormatted) {
    return buildGetPertinentAxisStatement(alCriterias, nAxisId, todayFormatted,
        true);
  }

  public String buildGetPertinentAxisStatement(List<Criteria> alCriterias, int nAxisId,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);

    sSQLStatement.append("SELECT COUNT(*) FROM ").append(CLASSIFICATION_TABLE).append(
        " CEC, SB_ContentManager_Content CMC WHERE CEC.").append(
        SILVEROBJECT_ID_COLUMN).append(" = CMC.silverContentId AND (CEC.").append(
        POSITION_ID_COLUMN).append(" <> -1) ");
    if (alCriterias != null) {
      for (Criteria criteria : alCriterias) {
        if (criteria.getValue() != null) {
          sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(
              criteria.getAxisId()).append(" LIKE '").append(criteria.getValue()).append("%')");
        }
      }
    }

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive) {
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");
    }

    // Set the pertinent axiom
    sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(nAxisId).append(" IS NOT NULL)");

    return sSQLStatement.toString();
  }

  // Get the pertinent Axis corresponding to the criterias
  public String buildGetPertinentAxisStatementByJoin(List<? extends Criteria> alCriterias,
      int nAxisId, String sRootValue, JoinStatement joinStatementAllPositions,
      String todayFormatted) {
    return buildGetPertinentAxisStatementByJoin(alCriterias, nAxisId,
        sRootValue, joinStatementAllPositions, todayFormatted, true);
  }

  public String buildGetPertinentAxisStatementByJoin(List<? extends Criteria> alCriterias,
      int nAxisId, String sRootValue, JoinStatement joinStatementAllPositions,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    String containerMgrLinks = joinStatementAllPositions.getTable(0);
    String containerMgrLinksKey = joinStatementAllPositions.getJoinKey(0);

    sSQLStatement.append("SELECT COUNT(DISTINCT CEC.").append(
        SILVEROBJECT_ID_COLUMN).append(")");
    sSQLStatement.append(" FROM ").append(CLASSIFICATION_TABLE).append(" CEC, ").append(
        containerMgrLinks).append(
        " CML, SB_ContentManager_Content CMC ");
    sSQLStatement.append(" WHERE CEC.").append(POSITION_ID_COLUMN).append(
        " = CML.").append(containerMgrLinksKey);
    sSQLStatement.append(" AND CEC.").append(SILVEROBJECT_ID_COLUMN).append(
        " = CMC.silverContentId ");
    String whereClause = joinStatementAllPositions.getWhere();
    if (isDefined(whereClause)) {
      sSQLStatement.append(" AND ").append(whereClause);
    }
    if (alCriterias != null) {
      for (Criteria criteria : alCriterias) {
        if (criteria.getValue() != null) {
          sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(
              criteria.getAxisId()).append(" LIKE '").append(criteria.getValue()).append("%')");
        }
      }
    }

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive) {
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");
    }

    // Set the pertinent axiom
    if (sRootValue.length() == 0) {
      sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(nAxisId).append(
          " IS NOT NULL)");
    } else {
      sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(nAxisId).append(" LIKE '").
          append(sRootValue).append("%')");
    }
    return sSQLStatement.toString();
  }

  // Get the pertinent Value corresponding to the criterias
  public String buildGetPertinentValueStatement(List<Criteria> alCriterias, int nAxisId,
      String todayFormatted) {
    return buildGetPertinentValueStatement(alCriterias, nAxisId,
        todayFormatted, true);
  }

  public String buildGetPertinentValueStatement(List<Criteria> alCriterias, int nAxisId,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT COUNT(CEC.").append(SILVEROBJECT_ID_COLUMN).append("), ").append(
        AXIS_COLUMN).append(nAxisId).append(" FROM ").append(CLASSIFICATION_TABLE).append(
        "CEC, SB_ContentManager_Content CMC ");
    sSQLStatement.append("WHERE CEC.").append(SILVEROBJECT_ID_COLUMN).append(
        " = CMC.silverContentId ");

    for (int nI = 0; !alCriterias.isEmpty() && nI < alCriterias.size(); nI++) {
      if (alCriterias.get(nI).getValue() != null) {
        sSQLStatement.append("(CEC.").append(AXIS_COLUMN).append(
            alCriterias.get(nI).getAxisId()).append(" LIKE '").append(alCriterias.get(nI).
            getValue()).append("%')");
        if (nI < alCriterias.size() - 1) {
          sSQLStatement.append(" AND ");
        }
      }
    }

    // Set the pertinent axiom
    if (!alCriterias.isEmpty()) {
      sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(nAxisId).append(
          " IS NOT NULL)");
    } else {
      sSQLStatement.append("(CEC.").append(AXIS_COLUMN).append(nAxisId).append(" IS NOT NULL)");
    }

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive) {
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");
    }

    // Group By
    sSQLStatement.append(" GROUP BY CEC.").append(AXIS_COLUMN).append(nAxisId);

    return sSQLStatement.toString();
  }

  // Get the pertinent Value corresponding to the criterias
  public String buildGetPertinentValueByJoinStatement(List<Criteria> alCriterias,
      int nAxisId, JoinStatement joinStatementAllPositions,
      String todayFormatted) {
    return buildGetPertinentValueByJoinStatement(alCriterias, nAxisId,
        joinStatementAllPositions, todayFormatted, true);
  }

  public String buildGetPertinentValueByJoinStatement(List<Criteria> alCriterias,
      int nAxisId, JoinStatement joinStatementAllPositions,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT COUNT(DISTINCT CEC.").append(
        SILVEROBJECT_ID_COLUMN).append("), CEC.").append(AXIS_COLUMN).append(nAxisId);
    sSQLStatement.append(" FROM ").append(CLASSIFICATION_TABLE).append(" CEC, ").append(
        joinStatementAllPositions.
        getTable(0)).append(
        " CML, SB_ContentManager_Content CMC ");
    sSQLStatement.append(" WHERE CEC.").append(POSITION_ID_COLUMN).append(
        " = CML.").append(joinStatementAllPositions.getJoinKey(0));
    sSQLStatement.append(" AND CEC.").append(SILVEROBJECT_ID_COLUMN).append(
        " = CMC.silverContentId ");
    String whereClause = joinStatementAllPositions.getWhere();
    if ((whereClause != null) && (!whereClause.equals(""))) {
      sSQLStatement.append(" AND ").append(whereClause);
    }

    for (Criteria criteria : alCriterias) {
      if (criteria.getValue() != null) {
        sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(
            criteria.getAxisId()).append(" LIKE '").append(criteria.getValue()).append("%')");
      }
    }

    // Set the pertinent axiom
    sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(nAxisId).append(" IS NOT NULL)");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive) {
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");
    }

    // Group By
    sSQLStatement.append(" GROUP BY CEC.").append(AXIS_COLUMN).append(nAxisId);

    return sSQLStatement.toString();
  }

  public String buildGetObjectValuePairsByJoinStatement(List<Criteria> alCriterias,
      int nAxisId, JoinStatement joinStatementAllPositions,
      String todayFormatted, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT CMC.internalContentId").append(", CEC.").append(AXIS_COLUMN).
        append(nAxisId).append(", CMI.componentId");
    sSQLStatement.append(" FROM ").append(CLASSIFICATION_TABLE).append(" CEC, ").append(
        joinStatementAllPositions.
        getTable(0)).append(
        " CML, SB_ContentManager_Content CMC, SB_ContentManager_Instance CMI ");
    sSQLStatement.append(" WHERE CEC.").append(POSITION_ID_COLUMN).append(
        " = CML.").append(joinStatementAllPositions.getJoinKey(0));
    sSQLStatement.append(" AND CEC.").append(SILVEROBJECT_ID_COLUMN).append(
        " = CMC.silverContentId ");
    sSQLStatement.append(" AND CMC.contentInstanceId").append(
        " = CMI.instanceId ");
    String whereClause = joinStatementAllPositions.getWhere();
    if (isDefined(whereClause)) {
      sSQLStatement.append(" AND ").append(whereClause);
    }

    for (Criteria criteria : alCriterias) {
      if (criteria.getValue() != null) {
        sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(
            criteria.getAxisId()).append(" LIKE '").append(criteria.getValue()).append("%')");
      }
    }

    // Set the pertinent axiom
    sSQLStatement.append(" AND (CEC.").append(AXIS_COLUMN).append(nAxisId).append(" IS NOT NULL)");

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(" AND ('").append(todayFormatted).append(
        "' between CMC.beginDate AND CMC.endDate)");
    if (visibilitySensitive) {
      sSQLStatement.append(" AND (CMC.isVisible = 1 )");
    }

    return sSQLStatement.toString();
  }
}