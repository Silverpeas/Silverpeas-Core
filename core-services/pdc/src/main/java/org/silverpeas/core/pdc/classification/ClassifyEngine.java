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

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.JoinStatement;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the ClassifyEngine API It gives access to functions for classifying,
 * unclassifying and searching SilverObjetIds Assumption : The SilverObjetIds processed are int
 * values from 0 to n The axis processed are int values from 0 to n
 */
@Singleton
@Transactional
public class ClassifyEngine {
  // Maximum number of axis processed by the classifyEngine (from properties)
  private int nbMaxAxis = 0;
  // Helper object to build all the SQL statements
  private org.silverpeas.core.pdc.classification.SQLStatement SQLStatement = new SQLStatement();
  // Registered axis cache
  private int[] registeredAxis = null;
  // GetSinglePertinentAxis Cache
  private Map<String, PertinentAxis> m_hSinglePertinentAxis =
      new ConcurrentHashMap<String, PertinentAxis>(0);

  private static ClassifyEngine getInstance() {
    return ServiceProvider.getService(ClassifyEngine.class);
  }

  @PostConstruct
  protected void init() {
    // Get the maximum number of axis that the classifyEngine can handle
    SettingBundle res =
        ResourceLocator.getSettingBundle("org.silverpeas.classifyEngine.ClassifyEngine");
    String sMaxAxis = res.getString("MaxAxis");
    nbMaxAxis = Integer.parseInt(sMaxAxis);
    try {
      registeredAxis = loadRegisteredAxis();
    } catch (ClassifyEngineException e) {
      SilverTrace.error("classifyEngine", "ClassifyEngine.init()",
          "root.EX_CLASS_NOT_INITIALIZED",
          "registeredAxis initialization failed !", e);
    }
  }

  // Return the maximum number of supported axis
  static public int getMaxAxis() {
    return getInstance().nbMaxAxis;
  }

  /*
   * Constructor
   */
  protected ClassifyEngine() {
  }

  static public void clearCache() {
    getInstance().m_hSinglePertinentAxis.clear();
  }

  /*
   * Register an axis
   */
  public void registerAxis(Connection connection, int nLogicalAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;

    // check that thie given axis is not already registered
    if (this.AxisAlreadyRegistered(nLogicalAxisId)) {
      throw new ClassifyEngineException("ClassifyEngine.registerAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_AXIS_ALREADY_REGISTERED", "nLogicalAxisId: "
          + nLogicalAxisId);
    }

    try {
      synchronized (registeredAxis) {
        // Get the next unregistered axis
        int nNextAvailableAxis = this.getNextUnregisteredAxis();
        if (nNextAvailableAxis == -1) {
          throw new ClassifyEngineException("ClassifyEngine.registerAxis",
              SilverpeasException.ERROR,
              "classifyEngine.EX_NOMORE_AVAILABLE_AXIS", "nLogicalAxisId: "
              + nLogicalAxisId);
        }

        // build the statement to classify
        String sSQLStatement = SQLStatement.buildRegisterAxisStatement(
            nNextAvailableAxis, nLogicalAxisId);

        // Execute the insertion

        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        // Register the axis in memory
        registeredAxis[nNextAvailableAxis] = nLogicalAxisId;
      }

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.registerAxis",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_REGISTER_AXIS",
          "nLogicalAxisId= " + nLogicalAxisId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /*
   * Unregister an axis Return the list of the deleted positionsIds
   */
  public List<Integer> unregisterAxis(Connection connection, int nLogicalAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;
    List<Integer> alDeletedPositionIds = null;

    // Check the minimum required
    int nAxis = this.getPhysicalAxisId(nLogicalAxisId);
    this.checkAxisId(nAxis);

    try {
      // build the statement to classify
      String sSQLStatement = SQLStatement.buildUnregisterAxisStatement(nAxis);

      synchronized (registeredAxis) {
        // Execute the removal

        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        // unregister the axis in memory
        registeredAxis[nAxis] = -1;

        // Remove the positions of this axis
        this.removeAllPositionValuesOnAxis(connection, nAxis);

        // Remove all the positions with all the values at null
        alDeletedPositionIds = this.removeEmptyPositions(connection);
      }

      // Clear cache
      m_hSinglePertinentAxis.clear();

      return alDeletedPositionIds;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.unregisterAxis",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_UNREGISTER_AXIS",
          "nLogicalAxisId= " + nLogicalAxisId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  // Load the registered axis
  private int[] loadRegisteredAxis() throws ClassifyEngineException {
    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    int[] tempRegisteredAxis = new int[nbMaxAxis];
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      // build the statement to load
      String sSQLStatement = SQLStatement.buildLoadRegisteredAxisStatement();

      // Execute the insertion

      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      while (resSet.next()) {
        for (int nI = 0; nI < nbMaxAxis; nI++) {
          tempRegisteredAxis[nI] = Integer.parseInt(resSet.getString(3 + nI));
        }
      }

    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.loadRegisteredAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_LOAD_REGISTERED_AXIS", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
    return tempRegisteredAxis;
  }

  // Return the next unregistered axis
  private int getNextUnregisteredAxis() {
    int nNextUnregisteredAxis = -1;

    for (int nI = 0; nI < nbMaxAxis && nNextUnregisteredAxis == -1; nI++) {
      if (registeredAxis[nI] == -1) {
        nNextUnregisteredAxis = nI;
      }
    }

    return nNextUnregisteredAxis;
  }

  // Return the physicalAxisId given the LogicalAxisId
  private int getPhysicalAxisId(int nLogicalAxisId)
      throws ClassifyEngineException {
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      if (registeredAxis[nI] == nLogicalAxisId) {
        return nI;
      }
    }

    SilverTrace.error("classifyEngine", "ClassifyEngine.getPhysicalAxisId",
        "root.MSG_GEN_PARAM_VALUE",
        "Can't get physical axis Id, nLogicalAxisId: " + nLogicalAxisId
        + ", registeredAxis : " + printRegisteredAxis());
    throw new ClassifyEngineException("ClassifyEngine.getPhysicalAxisId",
        SilverpeasException.ERROR, "classifyEngine.EX_CANT_GET_PHYSICAL_AXIS",
        "nLogicalAxisId: " + nLogicalAxisId);
  }

  private String printRegisteredAxis() {
    StringBuilder sRegister = new StringBuilder(100);
    sRegister.append("[");
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      sRegister.append(registeredAxis[nI]).append(", ");
    }
    sRegister.append("]");
    return sRegister.toString();
  }

  // Return the LogicalAxisId given the physicalAxisId
  private int getLogicalAxisId(int nPhysicalAxisId)
      throws ClassifyEngineException {
    if (nPhysicalAxisId < 0 || nPhysicalAxisId > registeredAxis.length) {
      throw new ClassifyEngineException("ClassifyEngine.getLogicalAxisId",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_GET_LOGICAL_AXIS",
          "nPhysicalAxisId: " + nPhysicalAxisId);
    }

    return registeredAxis[nPhysicalAxisId];
  }

  // Return if the LogicalAxisId given is already registered
  private boolean AxisAlreadyRegistered(int nLogicalAxisId) {
    for (int nI = 0; nI < nbMaxAxis; nI++) {
      if (registeredAxis[nI] == nLogicalAxisId) {
        return true;
      }
    }

    return false;
  }

  public int isPositionAlreadyExists(int nSilverObjectId, Position position)
      throws ClassifyEngineException {
    // Convert the Axis Ids
    List<Value> alValues = position.getValues();
    for (Value value : alValues) {
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String sSQLStatement = null;
    Connection connection = null;
    try {
      // Open the connection if necessary
      connection = DBUtil.openConnection();

      // Check if the position already exists
      sSQLStatement = SQLStatement.buildVerifyStatement(nSilverObjectId, position);

      // Execute the verification

      prepStmt = connection.prepareStatement(sSQLStatement);
      rs = prepStmt.executeQuery();
      int newPositionId = -1;
      if (rs.next()) {
        newPositionId = rs.getInt(1);
      }

      return newPositionId;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.isPositionAlreadyExists",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_CLASSIFY_SILVEROBJECTID",
          "sSQLStatement= " + sSQLStatement, e);
    } finally {
      DBUtil.close(rs, prepStmt);
      DBUtil.close(connection);
    }
  }

  /*
   * Classify the given SilverObjectid within the classifyEngine If the given connection is null,
   * then we have to open a connection and close it Return the PositionId
   */
  public int classifySilverObject(Connection connection, int silverObjectId,
      Position position) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(silverObjectId, position);

    // Convert the Axis Ids
    List<Value> alValues = position.getValues();
    for (Value value : alValues) {
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // build the statement to classify
      int newPositionId = DBUtil.getNextId("SB_ClassifyEngine_Classify", "PositionId");
      String sSQLStatement = SQLStatement.buildClassifyStatement(silverObjectId, position,
          newPositionId);

      // Execute the insertion

      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();

      return newPositionId;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.classifySilverObject",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_CLASSIFY_SILVEROBJECTID",
          "silverObjectId= " + silverObjectId, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  /*
   * Remove the given SilverObjectId at th egiven position within the classifyEngine If the given
   * connection is null, then we have to open a connection and close it
   */
  public void unclassifySilverObjectByPosition(Connection connection,
      int nSilverObjectId, Position<Value> position) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(nSilverObjectId, position);

    // Convert the Axis Ids
    List<Value> alValues = position.getValues();
    for (Value value : alValues) {
      value.setAxisId(this.getPhysicalAxisId(value.getAxisId()));
    }
    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // build the statement to remove the position
      String sSQLStatement = SQLStatement.buildRemoveByPositionStatement(
          nSilverObjectId, position);

      // Execute the removal
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.unclassifySilverObjectByPosition",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_SILVEROBJECTID_POSITION",
          "silverObjectId= " + nSilverObjectId, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  /*
   * Remove the given SilverObjectId at all positions within the classifyEngine
   */
  public void unclassifySilverObject(Connection connection, int nSilverObjectId)
      throws ClassifyEngineException {
    // Check the minimum required
    this.checkSilverObjectId(nSilverObjectId);
    PreparedStatement prepStmt = null;
    try {
      // build the statement to remove the position
      String sSQLStatement = SQLStatement.buildRemoveSilverObjectStatement(nSilverObjectId);

      // Execute the removal
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.unclassifySilverObject", SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_SILVEROBJECTID_POSITION",
          "silverObjectId= " + nSilverObjectId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /*
   * Remove the SilverObject referenced with the given positionId within the classifyEngine If the
   * given connection is null, then we have to open a connection and close it
   */
  public void unclassifySilverObjectByPositionId(Connection connection,
      int nPositionId) throws ClassifyEngineException {
    boolean bCloseConnection = false;
    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // build the statement to remove the position
      String sSQLStatement = SQLStatement.buildRemoveByPositionIdStatement(nPositionId);

      // Execute the removal
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.unclassifySilverObjectByPositionId",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_SILVEROBJECTID_POSITION",
          "nPositionId= " + nPositionId, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  /*
   * update the given new position within the classifyEngine If the given connection is null, then
   * we have to open a connection and close it
   */
  public void updateSilverObjectPosition(Connection connection, Position newPosition)
      throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkPosition(newPosition);

    // Convert the Axis Ids
    List<Value> alValues = newPosition.getValues();
    for (Value value : alValues) {
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // build the statement to update the position
      String sSQLStatement = SQLStatement.buildUpdateByPositionIdStatement(newPosition);

      // Execute the update
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.updateSilverObjectPosition",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_UPDATE_SILVEROBJECTID_POSITION",
          "nPositionId= " + newPosition.getPositionId(), e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  /*
   * update several position with the given new position within the classifyEngine If the given
   * connection is null, then we have to open a connection and close it add by SAN
   */
  public void updateSilverObjectPositions(Connection connection,
      List<Value> classifyValues, int nSilverObjectId) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    Value value = null;
    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }
      for (Value classifyValue : classifyValues) {
        value = classifyValue;
        // faut utiliser l'instruction suivante on a deja fait toute cette
        // operation
        // dans la methode updateSilverObjectPosition.
        // value.setAxisId(this.getPhysicalAxisId(value.getAxisId()));

        // build the statement to update the position
        String sSQLStatement = SQLStatement.buildUpdateByObjectIdStatement(
            value, nSilverObjectId);

        // Execute the update
        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        // Clear cache
        m_hSinglePertinentAxis.clear();
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.updateSilverObjectPositions",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_UPDATE_SILVEROBJECTID_POSITION", "axisId= "
          + value.getAxisId(), e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  public List<Integer> findSilverOjectByCriterias(List<Criteria> alGivenCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String afterDate, String beforeDate) throws ClassifyEngineException {
    return findSilverOjectByCriterias(alGivenCriterias, joinStatementContainer,
        joinStatementContent, afterDate, beforeDate, true, true);
  }

  /*
   * Find all the SilverObjectId corresponding to the given criterias and the given Join Statement
   */
  public List<Integer> findSilverOjectByCriterias(List<Criteria> alGivenCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String afterDate, String beforeDate, boolean recursiveSearch,
      boolean visibilitySensitive) throws ClassifyEngineException {
    Connection connection = null;
    List<Integer> alObjectIds = new ArrayList<Integer>();

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    List<Criteria> alCriterias = new ArrayList<Criteria>();
    for (Criteria criteria : alGivenCriterias) {
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      String today = DateUtil.today2SQLDate();

      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement.buildFindByCriteriasStatementByJoin(
          alCriterias, joinStatementContainer, joinStatementContent, today,
          recursiveSearch, visibilitySensitive);

      // Execute the finding
      prepStmt = connection.prepareStatement(sSQLStatement);

      // works on dates
      if ((beforeDate != null && beforeDate.length() > 0)
          && (afterDate != null && afterDate.length() > 0)) {
        prepStmt.setDate(1, new Date(DateUtil.parseDate(beforeDate).getTime()));
        prepStmt.setDate(2, new Date(DateUtil.parseDate(afterDate).getTime()));
      } else if (beforeDate != null && beforeDate.length() > 0) {
        prepStmt.setDate(1, new Date(DateUtil.parseDate(beforeDate).getTime()));
      } else if (afterDate != null && afterDate.length() > 0) {
        prepStmt.setDate(1, new Date(DateUtil.parseDate(afterDate).getTime()));
      }
      resSet = prepStmt.executeQuery();
      // Fetch the results
      while (resSet.next()) {
        alObjectIds.add(resSet.getInt(1));
      }

      return alObjectIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.findSilverOjectByCriterias",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_FIND_SILVEROBJECTID", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }

  /*
   * get the SilverContentIds corresponding to the given PositionIds
   */
  public List<Integer> getSilverContentIdsByPositionIds(List<Integer> alPositionids)
      throws ClassifyEngineException {
    if (alPositionids == null || alPositionids.isEmpty()) {
      return new ArrayList<Integer>();
    }

    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement.buildSilverContentIdsByPositionIdsStatement(
          alPositionids);

      // Execute the finding
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      // Fetch the results and convert them in Positions
      ArrayList<Integer> alSilverContentIds = new ArrayList<Integer>();
      while (resSet.next()) {
        alSilverContentIds.add(resSet.getInt(1));
      }

      return alSilverContentIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getSilverContentIdsByPositionIds",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_SILVERCONTENTIDS_BYSILVEROBJECTIDS", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }

  /*
   * Find all the Positions corresponding to the given SilverObjectId
   */
  public List<Position> findPositionsBySilverOjectId(int nSilverObjectId)
      throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkSilverObjectId(nSilverObjectId);
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement.buildFindBySilverObjectIdStatement(nSilverObjectId);

      // Execute the finding

      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results and convert them in Positions
      List<Position> alResults = new ArrayList<Position>();
      while (resSet.next()) {
        Position<Value> position = new Position<Value>();
        position.setPositionId(resSet.getInt(1));

        List<Value> alValues = new ArrayList<Value>();
        for (int nI = 0; nI < nbMaxAxis; nI++) {
          Value value = new Value();
          value.setAxisId(this.getLogicalAxisId(nI));
          value.setValue(resSet.getString(3 + nI));
          alValues.add(value);
        }

        position.setValues(alValues);

        alResults.add(position);
      }

      return alResults;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.findPositionsBySilverOjectId",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_FIND_SILVEROBJECTID", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }

  // Remove all the Position values on the given axis
  private void removeAllPositionValuesOnAxis(Connection connection, int nAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;
    try {
      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement.buildRemoveAllPositionValuesStatement(nAxisId);

      // Execute the removal
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.removeAllPositionValuesOnAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_ALLPOSITIONVALUES", e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  // Remove all the positions with all the values at null
  // Return the deleted positionIds
  private List<Integer> removeEmptyPositions(Connection connection)
      throws ClassifyEngineException {
    // -----------------------------
    // Get the removed positionIds
    // -----------------------------

    // build the statement to get the empty positions
    String sSQLStatement = SQLStatement.buildGetEmptyPositionsStatement(nbMaxAxis);
    ArrayList<Integer> alDeletedPositionIds = new ArrayList<Integer>();
    // Execute the query

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      while (resSet.next()) {
        alDeletedPositionIds.add(resSet.getInt(1));
      }
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.removeEmptyPositions",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_EMPTYPOSITIONS", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
    }

    try {
      // -----------------------------
      // Remove the empty positions
      // -----------------------------

      // build the statement to remove the empty positions
      sSQLStatement = SQLStatement.buildRemoveEmptyPositionsStatement(nbMaxAxis);

      // Execute the removal

      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();

      return alDeletedPositionIds;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.removeEmptyPositions",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_EMPTYPOSITIONS", e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /*
   * Replace the old value with the new value for all positions
   */
  public void replaceValuesOnAxis(Connection connection, List<Value> oldValue,
      List<Value> newValue) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // For all the given values
    for (int nI = 0; nI < oldValue.size(); nI++) {
      Value oldV = oldValue.get(nI);
      Value newV = newValue.get(nI);

      // Convert the axis Ids
      oldV.setAxisId(this.getPhysicalAxisId(oldV.getAxisId()));
      newV.setAxisId(this.getPhysicalAxisId(newV.getAxisId()));

      // Check the minimum required
      this.checkAxisId(oldV.getAxisId());
      if (oldV.getAxisId() != newV.getAxisId()) {
        throw new ClassifyEngineException("ClassifyEngine.replaceValuesOnAxis",
            SilverpeasException.ERROR,
            "classifyEngine.EX_AXISVALUES_NOT_IDENTICAL");
      }
      if (oldV.getValue().equals(newV.getValue())) {
        throw new ClassifyEngineException("ClassifyEngine.replaceValuesOnAxis",
            SilverpeasException.ERROR, "classifyEngine.EX_VALUES_IDENTICAL");
      }
    }

    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        // Open the connection
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Update the value
      int nIndex = 0;
      while (nIndex < oldValue.size()) {
        // build the statement to get the SilverObjectIds
        String sSQLStatement = SQLStatement.buildReplaceValuesStatement(
            oldValue.get(nIndex), newValue.get(nIndex));

        // Execute the change
        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        nIndex++;
      }

      // Remove empty positions coming from the above change
      String sSQLStatement = SQLStatement.buildRemoveEmptyPositionsStatement(nbMaxAxis);
      // Execute the deletion of empty positions
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.replaceValuesOnAxis",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_REPLACE_VALUES", e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  private void checkSilverObjectId(int nSilverObjectId)
      throws ClassifyEngineException {
    if (nSilverObjectId < 0) {
      throw new ClassifyEngineException("ClassifyEngine.checkParameters",
          SilverpeasException.ERROR,
          "classifyEngine.EX_INCORRECT_SILVEROBJECTID");
    }
  }

  private void checkPosition(Position<Value> position) throws ClassifyEngineException {
    if (position != null) {
      position.checkPosition();
    } else {
      throw new ClassifyEngineException("ClassifyEngine.checkParameters",
          SilverpeasException.ERROR, "classifyEngine.EX_POSITION_NULL");
    }
  }

  private void checkParameters(int nSilverObjectId, Position<Value> position)
      throws ClassifyEngineException {
    this.checkSilverObjectId(nSilverObjectId);
    this.checkPosition(position);
  }

  private void checkCriterias(List<? extends Criteria> alCriterias) throws ClassifyEngineException {
    // Check if the given array of criterias is valid
    if (alCriterias == null) {
      throw new ClassifyEngineException("ClassifyEngine.checkCriterias",
          SilverpeasException.ERROR, "classifyEngine.EX_CRITERIAS_ARRAY_NULL");
    }

    // Check that each criteria is valid
    for (Criteria criteria : alCriterias) {
      criteria.checkCriteria();
    }
  }

  private void checkAxisId(int nAxisId) throws ClassifyEngineException {
    if (nAxisId < 0) {
      throw new ClassifyEngineException("ClassifyEngine.checkAxisId",
          SilverpeasException.ERROR,
          "classifyEngine.EX_INCORRECT_AXISID_VALUE", "nAxisId: " + nAxisId);
    }
  }

  /*
   * Return a List of PertinentAxis corresponding to the given criterias for the given AxisIds The
   * return list is ordered like the given one considering the AxisId
   */
  public List<PertinentAxis> getPertinentAxis(List<? extends Criteria> alGivenCriterias,
      List<Integer> alAxisIds)
      throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    List<Criteria> alCriterias = new ArrayList<Criteria>();
    for (Criteria criteria : alGivenCriterias) {
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    try {
      // Open the connection
      connection = DBUtil.openConnection();

      String today = DateUtil.today2SQLDate();

      // Call the search On axis one by one
      ArrayList<PertinentAxis> alPertinentAxis = new ArrayList<PertinentAxis>();
      for (Integer alAxisId : alAxisIds) {
        int nAxisId = this.getPhysicalAxisId(alAxisId);
        alPertinentAxis.add(this.getSinglePertinentAxis(connection, alCriterias, nAxisId, today));
      }

      return alPertinentAxis;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.getPertinentAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_AXIS", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  private PertinentAxis getSinglePertinentAxis(Connection connection,
      List<Criteria> alCriterias, int nAxisId, String todayFormatted)
      throws SQLException, ClassifyEngineException {
    // build the statements
    String sSQLStatement = SQLStatement.buildGetPertinentAxisStatement(
        alCriterias, nAxisId, todayFormatted);

    // Execute the finding

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    PertinentAxis pAxis = null;
    try {
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      // Fetch the results
      pAxis = new PertinentAxis();
      pAxis.setAxisId(this.getLogicalAxisId(nAxisId));
      int nDocs = 0;
      while (resSet.next()) {
        nDocs += resSet.getInt(1);
      }
      pAxis.setNbObjects(nDocs);
    } finally {
      DBUtil.close(resSet, prepStmt);
    }

    return pAxis;
  }

  /*
   * Return a List of PertinentAxis corresponding to the given criterias for the given AxisIds and
   * given Join Statement The return list is ordered like the given one considering the AxisId
   */
  public List<PertinentAxis> getPertinentAxisByJoin(List<? extends Criteria> alGivenCriterias,
      List<Integer> alAxisIds,
      JoinStatement joinStatementAllPositions) throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    List<Criteria> alCriterias = new ArrayList<Criteria>();
    for (Criteria criteria : alGivenCriterias) {
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    try {
      // Open the connection
      connection = DBUtil.openConnection();

      String today = DateUtil.today2SQLDate();

      // Call the search On axis one by one
      ArrayList<PertinentAxis> alPertinentAxis = new ArrayList<PertinentAxis>();
      for (Integer alAxisId : alAxisIds) {
        int nAxisId = this.getPhysicalAxisId(alAxisId);
        alPertinentAxis.add(this.getSinglePertinentAxisByJoin(connection,
            alCriterias, nAxisId, "", joinStatementAllPositions, today));
      }

      return alPertinentAxis;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getPertinentAxisByJoin", SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_AXIS", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  public PertinentAxis getSinglePertinentAxisByJoin(Connection connection,
      List<? extends Criteria> alCriterias, int nAxisId, String sRootValue,
      JoinStatement joinStatementAllPositions) throws SQLException,
      ClassifyEngineException {
    String today = DateUtil.today2SQLDate();
    return getSinglePertinentAxisByJoin(connection, alCriterias, nAxisId,
        sRootValue, joinStatementAllPositions, today);
  }

  /*
   * Return a PertinentAxis object corresponding to the given AxisId, rootValue and search Criterias
   */
  public PertinentAxis getSinglePertinentAxisByJoin(Connection connection,
      List<? extends Criteria> alCriterias, int nAxisId, String sRootValue,
      JoinStatement joinStatementAllPositions, String todayFormatted)
      throws SQLException, ClassifyEngineException {
    boolean bCloseConnection = false;

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      if (connection == null) {
        // Check the minimum required
        this.checkCriterias(alCriterias);

        // Convert the Axis Ids
        List<Criteria> alComputedCriterias = new ArrayList<Criteria>();
        for (Criteria criteria : alCriterias) {
          alComputedCriterias.add(
              new Criteria(this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
        }
        alCriterias = alComputedCriterias;

        // Convert the logicalAxisId
        nAxisId = this.getPhysicalAxisId(nAxisId);

        // Open the connection
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // build the statements
      String sSQLStatement = SQLStatement.buildGetPertinentAxisStatementByJoin(
          alCriterias, nAxisId, sRootValue, joinStatementAllPositions,
          todayFormatted);

      PertinentAxis pertinentAxis = m_hSinglePertinentAxis.get(sSQLStatement);
      if (pertinentAxis == null) {
        // Execute the finding
        prepStmt = connection.prepareStatement(sSQLStatement);
        resSet = prepStmt.executeQuery();

        // Fetch the results
        pertinentAxis = new PertinentAxis();
        pertinentAxis.setAxisId(this.getLogicalAxisId(nAxisId));
        int nDocs = 0;
        while (resSet.next()) {
          nDocs += resSet.getInt(1);
        }
        pertinentAxis.setNbObjects(nDocs);
        pertinentAxis.setRootValue(sRootValue);

        // Add in cache
        m_hSinglePertinentAxis.put(sSQLStatement, pertinentAxis);
      }

      return pertinentAxis;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getSinglePertinentAxisByJoin",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_AXIS", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      if (bCloseConnection) {
        DBUtil.close(connection);
      }
    }
  }

  /*
   * Return a List of PertinentValues corresponding to the givenAxisId The return list is ordered
   * like the given one considering the AxisId
   */
  public List<PertinentValue> getPertinentValues(List<? extends Criteria> alGivenCriterias,
      int nLogicalAxisId)
      throws ClassifyEngineException {


    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    ArrayList<Criteria> alCriterias = new ArrayList<Criteria>();
    for (Criteria criteria : alGivenCriterias) {
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      String today = DateUtil.today2SQLDate();

      // Build the statement
      String sSQLStatement = SQLStatement.buildGetPertinentValueStatement(
          alCriterias, this.getPhysicalAxisId(nLogicalAxisId), today);

      // Execute the finding

      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      ArrayList<PertinentValue> alPertinentValues = new ArrayList<PertinentValue>();
      while (resSet.next()) {
        PertinentValue pValue = new PertinentValue();
        pValue.setAxisId(nLogicalAxisId);
        pValue.setNbObjects(resSet.getInt(1));
        pValue.setValue(resSet.getString(2));

        alPertinentValues.add(pValue);
      }

      return alPertinentValues;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.getPertinentValues",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_VALUES", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }

  /*
   * Return a List of PertinentValues corresponding to the givenAxisId The return list is ordered
   * like the given one considering the AxisId
   */
  public List<PertinentValue> getPertinentValuesByJoin(List<? extends Criteria> alGivenCriterias,
      int nLogicalAxisId, JoinStatement joinStatementAllPositions)
      throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    List<Criteria> alCriterias = new ArrayList<Criteria>();
    for (Criteria criteria : alGivenCriterias) {
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      String today = DateUtil.today2SQLDate();

      // Build the statement
      String sSQLStatement = SQLStatement.buildGetPertinentValueByJoinStatement(alCriterias, this.
          getPhysicalAxisId(nLogicalAxisId), joinStatementAllPositions,
          today);

      // Execute the finding
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      ArrayList<PertinentValue> alPertinentValues = new ArrayList<PertinentValue>();
      while (resSet.next()) {
        PertinentValue pValue = new PertinentValue();
        pValue.setAxisId(nLogicalAxisId);
        pValue.setNbObjects(resSet.getInt(1));
        pValue.setValue(resSet.getString(2));

        alPertinentValues.add(pValue);
      }

      return alPertinentValues;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getPertinentValuesByJoin", SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_VALUES", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }

  /*
   * Return a List of ObjectValuePair corresponding to the givenAxisId The return list is ordered
   * like the given one considering the AxisId
   */
  public List<ObjectValuePair> getObjectValuePairsByJoin(List<? extends Criteria> alGivenCriterias,
      int nLogicalAxisId, JoinStatement joinStatementAllPositions)
      throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    List<Criteria> alCriterias = new ArrayList<Criteria>();
    for (Criteria criteria : alGivenCriterias) {
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      String today = DateUtil.today2SQLDate();

      // Build the statement
      String sSQLStatement =
          SQLStatement.buildGetObjectValuePairsByJoinStatement(alCriterias, this.
          getPhysicalAxisId(nLogicalAxisId), joinStatementAllPositions,
          today, true);

      // Execute the finding
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      // Fetch the results
      List<ObjectValuePair> objectValuePairs = new ArrayList<ObjectValuePair>();
      while (resSet.next()) {
        ObjectValuePair ovp = new ObjectValuePair(resSet.getString(1), resSet.getString(2), resSet.
            getString(3));

        objectValuePairs.add(ovp);
      }

      return objectValuePairs;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getObjectValuePairsByJoin",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_VALUES", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }

  /**
   * Get axis on which some informations are classified according to given list
   * @param instanceIds a List of component ids
   * @return a List of axis id on which at least one information is classified
   * @throws ClassifyEngineException
   */
  public List<Integer> getPertinentAxisByInstanceIds(List<String> instanceIds)
      throws ClassifyEngineException {


    if (instanceIds == null || instanceIds.isEmpty()) {
      return new ArrayList<Integer>();
    }

    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.openConnection();

      // Build the statement
      StringBuilder inClause = new StringBuilder(1000);
      boolean first = true;
      for (String instanceId : instanceIds) {
        if (!first) {
          inClause.append(",");
        }
        inClause.append("'").append(instanceId).append("'");
        first = false;
      }
      StringBuilder sSQLStatement = new StringBuilder(200);
      sSQLStatement.append("select * from sb_classifyengine_classify ");
      sSQLStatement.append("where objectid in ");
      sSQLStatement
          .append(
          "(select silvercontentid from sb_contentmanager_content, sb_contentmanager_instance where contentinstanceid in ");
      sSQLStatement.append(
          "(select instanceid from sb_contentmanager_instance where componentid IN (").append(
          inClause.toString()).append(")))");

      // Execute the finding
      prepStmt = connection.prepareStatement(sSQLStatement.toString());
      resSet = prepStmt.executeQuery();

      List<Integer> ids = new ArrayList<Integer>();

      // Fetch the results
      while (resSet.next()) {
        for (int nI = 0; nI < nbMaxAxis; nI++) {
          String value = resSet.getString(3 + nI);
          if (StringUtil.isDefined(value) && !ids.contains(nI)) {
            ids.add(nI);
          }
        }
      }

      List<Integer> axisIds = new ArrayList<Integer>();
      for (int id : ids) {
        axisIds.add(getLogicalAxisId(id));
      }

      return axisIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getPertinentAxisByInstanceIds",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_VALUES", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      DBUtil.close(connection);
    }
  }
}