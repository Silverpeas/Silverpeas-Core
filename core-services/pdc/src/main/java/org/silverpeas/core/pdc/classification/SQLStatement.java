/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.pdc.classification;

import org.silverpeas.core.util.JoinStatement;
import org.silverpeas.kernel.util.StringUtil;

import java.util.List;

class SQLStatement {

  private static final String INSTANCES_TABLE = "SB_ContentManager_Instance";
  private static final String CLASSIFICATION_TABLE = "SB_ClassifyEngine_Classify";
  private static final String POSITION_ID_COLUMN = "PositionId";
  private static final String SILVEROBJECT_ID_COLUMN = "ObjectId";
  private static final String AXIS_COLUMN = "Axis";
  private static final int AXIS_MAX_NUMBER = 49;
  private static final String SELECT = "SELECT ";
  private static final String FROM = " FROM ";
  private static final String AND = " AND ";
  private static final String WHERE = " WHERE ";
  private static final String UPDATE = "UPDATE ";
  private static final String SET = " SET ";
  private static final String DELETE_FROM = "DELETE FROM ";
  private static final String AND_CEC = " AND CEC.";
  private static final String CMI_COMPONENT_ID_IN = "CMI.componentId IN ('";
  private static final String LIKE = " LIKE '";
  private static final String AND_BLOCK_BEGIN = " AND ('";
  private static final String BETWEEN_CMC_BEGIN_DATE_AND_CMC_END_DATE = "' between CMC.beginDate AND CMC.endDate)";
  private static final String AND_CMC_IS_VISIBLE = " AND (CMC.isVisible = 1 )";
  private static final String GROUP_BY_CEC = " GROUP BY CEC.";
  private static final String IS_NULL = " IS NULL)";
  private static final String AND_BLOCK_CEC = " AND (CEC.";
  private static final String IS_NOT_NULL = " IS NOT NULL)";
  private static final String CEC = " CEC, ";
  private static final String EQUAL_CMC_SILVER_CONTENT_ID = " = CMC.silverContentId ";

  // Load registered axis
  public String buildLoadRegisteredAxisStatement() {
    return "SELECT * FROM " + CLASSIFICATION_TABLE +
        WHERE + "(" + POSITION_ID_COLUMN + " = -1)";
  }

  // Register an axis
  public String buildRegisterAxisStatement(int nNextAvailableAxis,
      int nLogicalAxisId) {
    return UPDATE + CLASSIFICATION_TABLE + SET +
        AXIS_COLUMN + nNextAvailableAxis + " = " + nLogicalAxisId +
        WHERE + "(" + POSITION_ID_COLUMN + " = -1)";
  }

  // Unregister an axis
  public String buildUnregisterAxisStatement(int nAxisId) {
    return UPDATE + CLASSIFICATION_TABLE + SET +
        AXIS_COLUMN + nAxisId + " = -1 WHERE " + POSITION_ID_COLUMN + " = -1";
  }

  // Build the SQL statement to classify the object
  public <T extends Value> String buildClassifyStatement(int nSilverObjectId, Position<T> position,
      int nNextPositionId) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    List<T> alValues = position.getValues();

    // Build the SQL statement to classify the object
    sSQLStatement.append("INSERT INTO ").append(CLASSIFICATION_TABLE).append(" (").append(
        POSITION_ID_COLUMN).append(", ").append(
        SILVEROBJECT_ID_COLUMN).append(", ");

    setClassificationPositions(nSilverObjectId, nNextPositionId, sSQLStatement, alValues);

    return sSQLStatement.toString();
  }

  private static <T extends Value> void setClassificationPositions(int nSilverObjectId, int nNextPositionId, StringBuilder sSQLStatement, List<T> alValues) {
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
    setPositionValues(sSQLStatement, alValues);
  }

  private static <T extends Value> void setPositionValues(StringBuilder sSQLStatement, List<T> alValues) {
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
  }

  // Build the SQL statement to get a position
  public <T extends Value> String buildVerifyStatement(int nSilverObjectId, Position<T> position) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    List<T> alValues = position.getValues();

    // Build the SQL statement to classify the object
    sSQLStatement.append(SELECT).append(POSITION_ID_COLUMN).append(FROM).append(
        CLASSIFICATION_TABLE);

    // Put the values for the concerning axis, put null for the other non-valued axis
    sSQLStatement.append(WHERE).append("(");
    for (int axis = 0; axis <= AXIS_MAX_NUMBER; axis++) {
      Value foundValue = null;
      for (T aValue : alValues) {
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
      if (axis < AXIS_MAX_NUMBER) {
        sSQLStatement.append(AND);
      }

    }
    sSQLStatement.append(")");
    sSQLStatement.append(AND).append(SILVEROBJECT_ID_COLUMN).append(" = ").append(
        nSilverObjectId);

    return sSQLStatement.toString();
  }

  // Build the SQL statement to remove the object
  public String buildRemoveSilverObjectStatement(int nSilverObjectId) {
    return DELETE_FROM + CLASSIFICATION_TABLE +
        WHERE + "(" + SILVEROBJECT_ID_COLUMN + "=" + nSilverObjectId + ")";
  }

  // Build the SQL statement to remove the object
  public String buildRemoveByPositionIdStatement(int nPositionId) {
    return DELETE_FROM + CLASSIFICATION_TABLE +
        WHERE + "(" + POSITION_ID_COLUMN + "=" + nPositionId + ")";
  }

  // Update a Position with the given one for the given SilverObjectId
  public <T extends Value> String buildUpdateByPositionIdStatement(Position<T> newPosition) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append(UPDATE).append(CLASSIFICATION_TABLE).append(SET).append(
        POSITION_ID_COLUMN).append(" = ").append(
        newPosition.getPositionId()).append(", ");
    List<T> allValues = newPosition.getValues();
    int valuesTakenInCharge = 0;
    setNewAxisPositionValues(sSQLStatement, allValues, valuesTakenInCharge);

    sSQLStatement.append(WHERE).append(POSITION_ID_COLUMN).append(" = ").append(newPosition.
        getPositionId());

    return sSQLStatement.toString();
  }

  private static <T extends Value> void setNewAxisPositionValues(StringBuilder sSQLStatement,
      List<T> allValues, int valuesTakenInCharge) {
    for (int axisId = 0; axisId < AXIS_MAX_NUMBER; axisId++) {
      String value = null;
      if (valuesTakenInCharge >= allValues.size()) {
        continue;
      }
      for (T aValue : allValues) {
        if (aValue.getPhysicalAxisId() == axisId) {
          value = aValue.getValue();
          valuesTakenInCharge++;
          break;
        }
      }
      sSQLStatement.append(AXIS_COLUMN).append(axisId);
      if (value == null || value.equals("-")) {
        sSQLStatement.append(" = ").append(value);
      } else {
        sSQLStatement.append(" = '").append(value).append("'");
      }

      if (axisId < AXIS_MAX_NUMBER - 1) {
        sSQLStatement.append(", ");
      }
    }
  }

  // Update several Positions for the given SilverObjectId
  // only if the value is invariant
  // add by SAN
  public String buildUpdateByObjectIdStatement(Value value, int nSilverObjectId) {

    String sSQLStatement = UPDATE + CLASSIFICATION_TABLE;
    sSQLStatement += SET + AXIS_COLUMN + value.getAxisId() + " = '"
        + value.getValue() + "' ";
    sSQLStatement += WHERE + SILVEROBJECT_ID_COLUMN + " =  "
        + nSilverObjectId;
    sSQLStatement += AND + AXIS_COLUMN + value.getAxisId()
        + " IS NOT NULL";

    return sSQLStatement;
  }

  public String buildFindByCriteriasStatementByJoin(List<Criteria> alCriterias,
      List<String> instanceIds, JoinStatement joinStatementContent,
      String todayFormatted, boolean recursiveSearch, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    String contentMgr = joinStatementContent.getTable(0);
    String contentMgrKey = joinStatementContent.getJoinKey(0);

    sSQLStatement.append(" SELECT CEC.").append(SILVEROBJECT_ID_COLUMN).append(FROM).append(
        CLASSIFICATION_TABLE).append(" CEC,").append(
        INSTANCES_TABLE).append(" CMI,").append(contentMgr).append(" CMC");
    sSQLStatement.append(WHERE);
    sSQLStatement.append(" CMC.contentInstanceId = CMI.instanceId");
    sSQLStatement.append(AND_CEC).append(SILVEROBJECT_ID_COLUMN).append(
        " = CMC.").append(contentMgrKey);
    if (!instanceIds.isEmpty()) {
      sSQLStatement.append(AND).append(CMI_COMPONENT_ID_IN);
      sSQLStatement.append(String.join("','", instanceIds));
      sSQLStatement.append("')");
    }

    // works on the content statement
    String whereClause = joinStatementContent.getWhere();
    if (StringUtil.isDefined(whereClause)) {
      sSQLStatement.append(AND).append(whereClause);
    }

    // criteres
    for (Criteria alCriteria : alCriterias) {
      if (alCriteria.getValue() != null) {
        sSQLStatement.append(" AND (").append(AXIS_COLUMN).append(
            alCriteria.getAxisId());
        if (recursiveSearch) {
          sSQLStatement.append(LIKE);
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
    sSQLStatement.append(AND_BLOCK_BEGIN).append(todayFormatted).append(
        BETWEEN_CMC_BEGIN_DATE_AND_CMC_END_DATE);

    if (visibilitySensitive) {
      sSQLStatement.append(AND_CMC_IS_VISIBLE);
    }

    sSQLStatement.append(GROUP_BY_CEC).append(SILVEROBJECT_ID_COLUMN);

    return sSQLStatement.toString();
  }

  // Build the SQL statement to find the objects
  public String buildFindBySilverObjectIdStatement(int nSilverObjectId) {
    return "SELECT * FROM " + CLASSIFICATION_TABLE +
        WHERE + "(" + SILVEROBJECT_ID_COLUMN + "=" +
        nSilverObjectId + ")";
  }

  // Remove the values on all the positions of the given axis
  public String buildRemoveAllPositionValuesStatement(int nAxisId) {
    return UPDATE + CLASSIFICATION_TABLE + SET +
        AXIS_COLUMN +
        nAxisId + " = null WHERE NOT " + POSITION_ID_COLUMN + " = -1";
  }

  // Return the positionId of all the deleted empty positions
  public String buildGetEmptyPositionsStatement(int nbMaxAxis) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append(SELECT).append(POSITION_ID_COLUMN).append(FROM).append(
        CLASSIFICATION_TABLE).append(WHERE);
    return setAxisPositionAsNull(sSQLStatement, nbMaxAxis);
  }

  private String setAxisPositionAsNull(StringBuilder sSQLStatement, int nbMaxAxis) {
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      sSQLStatement.append("(").append(AXIS_COLUMN).append(nI).append(
          IS_NULL);
      if (nI < nbMaxAxis - 1) {
        sSQLStatement.append(AND);
      }
    }

    return sSQLStatement.toString();
  }

  // Remove all the positions with all the values at null
  public String buildRemoveEmptyPositionsStatement(int nbMaxAxis) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append(DELETE_FROM).append(CLASSIFICATION_TABLE).append(
        WHERE);
    return setAxisPositionAsNull(sSQLStatement, nbMaxAxis);
  }

  // Replace the oldValue with the newValue
  public String buildReplaceValuesStatement(Value oldValue, Value newValue) {
    StringBuilder sSQLStatement = new StringBuilder(1000);

    // Build the SQL statement to remove the values
    if (newValue.getValue() != null) {
      sSQLStatement.append(UPDATE).append(CLASSIFICATION_TABLE).append(SET).append(
              AXIS_COLUMN).
          append(newValue.getAxisId()).append(" = '").append(newValue.getValue()).append("'");
    } else {
      sSQLStatement.append(UPDATE).append(CLASSIFICATION_TABLE).append(SET).append(
              AXIS_COLUMN).
          append(newValue.getAxisId()).append(" = ").append(newValue.getValue());
    }

    if (oldValue.getValue() != null) {
      sSQLStatement.append(WHERE).append("(").append(AXIS_COLUMN).append(
          oldValue.getAxisId()).append(" = '").append(oldValue.getValue()).append("')");
    } else {
      sSQLStatement.append(WHERE).append("(").append(AXIS_COLUMN).append(
          oldValue.getAxisId()).append(IS_NULL);
    }

    return sSQLStatement.toString();
  }

  // Get the pertinent Axis corresponding to the criterias
  public String buildGetPertinentAxisStatementByJoin(List<? extends Criteria> alCriterias,
      int nAxisId, String sRootValue, List<String> instanceIds,
      String todayFormatted) {
    StringBuilder sSQLStatement = new StringBuilder(1000);

    sSQLStatement.append("SELECT COUNT(DISTINCT CEC.").append(
        SILVEROBJECT_ID_COLUMN).append(")");
    sSQLStatement.append(FROM).append(CLASSIFICATION_TABLE).append(CEC).append(
        INSTANCES_TABLE).append(" CMI, SB_ContentManager_Content CMC ");
    sSQLStatement.append(" WHERE CMC.contentInstanceId").append(" = CMI.instanceId");
    sSQLStatement.append(AND_CEC).append(SILVEROBJECT_ID_COLUMN).append(
        EQUAL_CMC_SILVER_CONTENT_ID);
    if (!instanceIds.isEmpty()) {
      sSQLStatement.append(AND).append(CMI_COMPONENT_ID_IN);
      sSQLStatement.append(String.join("','", instanceIds));
      sSQLStatement.append("')");
    }
    if (alCriterias != null) {
      for (Criteria criteria : alCriterias) {
        if (criteria.getValue() != null) {
          sSQLStatement.append(AND_BLOCK_CEC).append(AXIS_COLUMN).append(
              criteria.getAxisId()).append(LIKE).append(criteria.getValue()).append("%')");
        }
      }
    }

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(AND_BLOCK_BEGIN).append(todayFormatted).append(
        BETWEEN_CMC_BEGIN_DATE_AND_CMC_END_DATE).append(AND_CMC_IS_VISIBLE);

    // Set the pertinent axiom
    if (sRootValue.length() == 0) {
      sSQLStatement.append(AND_BLOCK_CEC).append(AXIS_COLUMN).append(nAxisId).append(
          IS_NOT_NULL);
    } else {
      sSQLStatement.append(AND_BLOCK_CEC).append(AXIS_COLUMN).append(nAxisId).append(LIKE).
          append(sRootValue).append("%')");
    }
    return sSQLStatement.toString();
  }

  // Get the pertinent Value corresponding to the criterias
  public String buildGetPertinentValueByJoinStatement(List<Criteria> alCriterias,
      int nAxisId, List<String> instanceIds, String todayFormatted) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT COUNT(DISTINCT CEC.").append(
        SILVEROBJECT_ID_COLUMN).append("), CEC.").append(AXIS_COLUMN).append(nAxisId);
    sSQLStatement.append(FROM).append(CLASSIFICATION_TABLE).append(CEC).append(
        INSTANCES_TABLE).append(" CMI, SB_ContentManager_Content CMC ");
    sSQLStatement.append(" WHERE CMC.contentInstanceId").append(" = CMI.instanceId");
    sSQLStatement.append(AND_CEC).append(SILVEROBJECT_ID_COLUMN).append(
        EQUAL_CMC_SILVER_CONTENT_ID);
    completeStatement(sSQLStatement, instanceIds, alCriterias, nAxisId, todayFormatted, true);

    // Group By
    sSQLStatement.append(GROUP_BY_CEC).append(AXIS_COLUMN).append(nAxisId);

    return sSQLStatement.toString();
  }

  public String buildGetObjectValuePairsByJoinStatement(List<Criteria> alCriterias,
      int nAxisId, List<String> instanceIds, String todayFormatted, boolean visibilitySensitive) {
    StringBuilder sSQLStatement = new StringBuilder(1000);
    sSQLStatement.append("SELECT CMC.internalContentId").append(", CEC.").append(AXIS_COLUMN).
        append(nAxisId).append(", CMI.componentId");
    sSQLStatement.append(FROM).append(CLASSIFICATION_TABLE).append(CEC).append(
        " SB_ContentManager_Content CMC, SB_ContentManager_Instance CMI ");
    sSQLStatement.append(" WHERE CEC.").append(SILVEROBJECT_ID_COLUMN).append(
        EQUAL_CMC_SILVER_CONTENT_ID);
    sSQLStatement.append(" AND CMC.contentInstanceId").append(" = CMI.instanceId ");
    completeStatement(sSQLStatement, instanceIds, alCriterias, nAxisId, todayFormatted, visibilitySensitive);

    return sSQLStatement.toString();
  }

  private static void completeStatement(StringBuilder sSQLStatement, List<String> instanceIds, List<Criteria> alCriterias, int nAxisId, String todayFormatted, boolean visibilitySensitive) {
    if (!instanceIds.isEmpty()) {
      sSQLStatement.append(AND).append(CMI_COMPONENT_ID_IN);
      sSQLStatement.append(String.join("','", instanceIds));
      sSQLStatement.append("')");
    }

    for (Criteria criteria : alCriterias) {
      if (criteria.getValue() != null) {
        sSQLStatement.append(AND_BLOCK_CEC).append(AXIS_COLUMN).append(
            criteria.getAxisId()).append(LIKE).append(criteria.getValue()).append("%')");
      }
    }

    // Set the pertinent axiom
    sSQLStatement.append(AND_BLOCK_CEC).append(AXIS_COLUMN).append(nAxisId).append(IS_NOT_NULL);

    // Set the visibility constraints --> en faire une fonction
    sSQLStatement.append(AND_BLOCK_BEGIN).append(todayFormatted).append(
        BETWEEN_CMC_BEGIN_DATE_AND_CMC_END_DATE);
    if (visibilitySensitive) {
      sSQLStatement.append(AND_CMC_IS_VISIBLE);
    }
  }
}