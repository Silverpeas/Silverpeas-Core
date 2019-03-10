/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.pdc.classification;

import org.silverpeas.core.contribution.contentcontainer.content.SilverContentPostUpdate;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.JoinStatement;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the ClassifyEngine API It gives access to functions for classifying,
 * unclassifying and searching SilverObjetIds Assumption : The SilverObjetIds processed are int
 * values from 0 to n The axis processed are int values from 0 to n
 */
@Singleton
@Transactional
public class ClassifyEngine implements SilverContentPostUpdate {
  // Maximum number of axis processed by the classifyEngine (from properties)
  private int nbMaxAxis = 0;
  // Helper object to build all the SQL statements
  private org.silverpeas.core.pdc.classification.SQLStatement sqlStatement = new SQLStatement();
  // Registered axis cache
  private int[] registeredAxis = null;
  // GetSinglePertinentAxis Cache
  private Map<String, PertinentAxis> singlePertinentAxis = new ConcurrentHashMap<>(0);

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
      SilverLogger.getLogger(this).error(e);
    }
  }

  // Return the maximum number of supported axis
  public static int getMaxAxis() {
    return getInstance().nbMaxAxis;
  }

  /*
   * Constructor
   */
  protected ClassifyEngine() {
    // Nothing to do.
  }

  @Override
  public void postSilverpeasContentUpdate(final int silverContentId) {
    clearCache();
  }

  private static void clearCache() {
    getInstance().singlePertinentAxis.clear();
  }

  /*
   * Register an axis
   */
  public void registerAxis(Connection connection, int nLogicalAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;

    // check that thie given axis is not already registered
    if (this.isAxisAlreadyRegistered(nLogicalAxisId)) {
      throw new ClassifyEngineException("Axis already registered. nLogicalAxisId: "
          + nLogicalAxisId);
    }

    try {
      synchronized (registeredAxis) {
        // Get the next unregistered axis
        int nNextAvailableAxis = this.getNextUnregisteredAxis();
        if (nNextAvailableAxis == -1) {
          throw new ClassifyEngineException("No more available axis. nLogicalAxisId: "
              + nLogicalAxisId);
        }

        // build the statement to classify
        String sSQLStatement = sqlStatement.buildRegisterAxisStatement(
            nNextAvailableAxis, nLogicalAxisId);

        // Execute the insertion

        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        // Register the axis in memory
        registeredAxis[nNextAvailableAxis] = nLogicalAxisId;
      }

      // Clear cache
      singlePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
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
      String sSQLStatement = sqlStatement.buildUnregisterAxisStatement(nAxis);

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
      singlePertinentAxis.clear();

      return alDeletedPositionIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
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
      String sSQLStatement = sqlStatement.buildLoadRegisteredAxisStatement();

      // Execute the insertion

      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      while (resSet.next()) {
        for (int nI = 0; nI < nbMaxAxis; nI++) {
          tempRegisteredAxis[nI] = Integer.parseInt(resSet.getString(3 + nI));
        }
      }

    } catch (Exception e) {
      throw new ClassifyEngineException(e);
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
    throw new ClassifyEngineException(
        "Cannot get physical axis. nLogicalAxisId: " + nLogicalAxisId);
  }

  // Return the LogicalAxisId given the physicalAxisId
  private int getLogicalAxisId(int nPhysicalAxisId)
      throws ClassifyEngineException {
    if (nPhysicalAxisId < 0 || nPhysicalAxisId > registeredAxis.length) {
      throw new ClassifyEngineException(
          "Cannot get logical axis. nPhysicalAxisId: " + nPhysicalAxisId);
    }

    return registeredAxis[nPhysicalAxisId];
  }

  // Return if the LogicalAxisId given is already registered
  private boolean isAxisAlreadyRegistered(int nLogicalAxisId) {
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

    try (Connection connection = DBUtil.openConnection()) {
      // Check if the position already exists
      final String sSQLStatement = sqlStatement.buildVerifyStatement(nSilverObjectId, position);

      // Execute the verification
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement);
           final ResultSet rs = prepStmt.executeQuery()) {
        int newPositionId = -1;
        if (rs.next()) {
          newPositionId = rs.getInt(1);
        }
        return newPositionId;
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * Classify the given SilverObjectid within the classifyEngine If the given connection is null,
   * then we have to open a connection and close it Return the PositionId
   */
  public int classifySilverObject(Connection connection, int silverObjectId,
      Position position) throws ClassifyEngineException {
    Objects.requireNonNull(connection);

    // Check the minimum required
    this.checkParameters(silverObjectId, position);

    // Convert the Axis Ids
    List<Value> alValues = position.getValues();
    for (Value value : alValues) {
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    try {
      // build the statement to classify
      int newPositionId = DBUtil.getNextId("SB_ClassifyEngine_Classify", "PositionId");
      String sSQLStatement = sqlStatement.buildClassifyStatement(silverObjectId, position,
          newPositionId);

      // Execute the insertion
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
        prepStmt.executeUpdate();
      }

      // Clear cache
      singlePertinentAxis.clear();

      return newPositionId;
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * Remove the given SilverObjectId at all positions within the classifyEngine
   */
  public void unclassifySilverObject(Connection connection, int nSilverObjectId)
      throws ClassifyEngineException {
    Objects.requireNonNull(connection);
    // Check the minimum required
    this.checkSilverObjectId(nSilverObjectId);
      // build the statement to remove the position
    String sSQLStatement = sqlStatement.buildRemoveSilverObjectStatement(nSilverObjectId);

      // Execute the removal
    try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
      prepStmt.executeUpdate();

      // Clear cache
      singlePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * Remove the SilverObject referenced with the given positionId within the classifyEngine If the
   * given connection is null, then we have to open a connection and close it
   */
  public void unclassifySilverObjectByPositionId(Connection connection,
      int nPositionId) throws ClassifyEngineException {
    Objects.requireNonNull(connection);

    // build the statement to remove the position
    String sSQLStatement = sqlStatement.buildRemoveByPositionIdStatement(nPositionId);

    // Execute the removal
    try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
      prepStmt.executeUpdate();

      // Clear cache
      singlePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * update the given new position within the classifyEngine If the given connection is null, then
   * we have to open a connection and close it
   */
  public void updateSilverObjectPosition(final Position newPosition)
      throws ClassifyEngineException {
    // Check the minimum required
    this.checkPosition(newPosition);

    // Convert the Axis Ids
    List<Value> alValues = newPosition.getValues();
    for (Value value : alValues) {
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    try (final Connection connection = DBUtil.openConnection()) {
      // build the statement to update the position
      String sSQLStatement = sqlStatement.buildUpdateByPositionIdStatement(newPosition);

      // Execute the update
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
        prepStmt.executeUpdate();
      }

      // Clear cache
      singlePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * update several position with the given new position within the classifyEngine If the given
   * connection is null, then we have to open a connection and close it add by SAN
   */
  public void updateSilverObjectPositions(List<Value> classifyValues, int nSilverObjectId)
      throws ClassifyEngineException {
    try (final Connection connection = DBUtil.openConnection()) {
      for (Value classifyValue : classifyValues) {
        // build the statement to update the position
        final String sql =
            sqlStatement.buildUpdateByObjectIdStatement(classifyValue, nSilverObjectId);

        // Execute the update
        try (final PreparedStatement prepStmt = connection.prepareStatement(sql)) {
          prepStmt.executeUpdate();
        }

        // Clear cache
        singlePertinentAxis.clear();
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * Find all the SilverObjectId corresponding to the given criterias and the given Join Statement
   */
  public List<Integer> findSilverOjectByCriterias(List<Criteria> alGivenCriterias,
      List<String> instanceIds, JoinStatement joinStatementContent,
      String afterDate, String beforeDate, boolean recursiveSearch,
      boolean visibilitySensitive) throws ClassifyEngineException {
    final List<Integer> allObjectIds = new ArrayList<>();
    final List<Criteria> allCriteria = buildCriteriaOnAxis(alGivenCriterias);
    try (final Connection connection = DBUtil.openConnection()) {
      String today = DateUtil.today2SQLDate();

      // build the statement to get the SilverObjectIds
      String sSQLStatement =
          sqlStatement.buildFindByCriteriasStatementByJoin(allCriteria, instanceIds,
              joinStatementContent, today,
          recursiveSearch, visibilitySensitive);

      // Execute the finding
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {

        // works on dates
        if ((beforeDate != null && beforeDate.length() > 0) &&
            (afterDate != null && afterDate.length() > 0)) {
          prepStmt.setDate(1, new Date(DateUtil.parseDate(beforeDate).getTime()));
          prepStmt.setDate(2, new Date(DateUtil.parseDate(afterDate).getTime()));
        } else if (beforeDate != null && beforeDate.length() > 0) {
          prepStmt.setDate(1, new Date(DateUtil.parseDate(beforeDate).getTime()));
        } else if (afterDate != null && afterDate.length() > 0) {
          prepStmt.setDate(1, new Date(DateUtil.parseDate(afterDate).getTime()));
        }

        try (final ResultSet resSet = prepStmt.executeQuery()) {
          // Fetch the results
          while (resSet.next()) {
            allObjectIds.add(resSet.getInt(1));
          }
        }
      }
      return allObjectIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  private List<Criteria> buildCriteriaOnAxis(final List<? extends Criteria> allGivenCriteria)
      throws ClassifyEngineException {
    // Convert the Axis Ids
    this.checkCriterias(allGivenCriteria);
    final List<Criteria> allCriteria = new ArrayList<>();
    for (Criteria criteria : allGivenCriteria) {
      allCriteria.add(
          new Criteria(this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }
    return allCriteria;
  }

  /*
   * Find all the Positions corresponding to the given SilverObjectId
   */
  public List<Position> findPositionsBySilverOjectId(int nSilverObjectId)
      throws ClassifyEngineException {
    // Check the minimum required
    this.checkSilverObjectId(nSilverObjectId);
    try (final Connection connection = DBUtil.openConnection()) {

      // build the statement to get the SilverObjectIds
      String sSQLStatement = sqlStatement.buildFindBySilverObjectIdStatement(nSilverObjectId);

      // Execute the finding
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement);
           final ResultSet resSet = prepStmt.executeQuery()) {

        // Fetch the results and convert them in Positions
        final List<Position> allResults = new ArrayList<>();
        while (resSet.next()) {
          Position<Value> position = new Position<>();
          position.setPositionId(resSet.getInt(1));

          List<Value> alValues = new ArrayList<>();
          for (int nI = 0; nI < nbMaxAxis; nI++) {
            Value value = new Value();
            value.setAxisId(this.getLogicalAxisId(nI));
            value.setValue(resSet.getString(3 + nI));
            alValues.add(value);
          }

          position.setValues(alValues);

          allResults.add(position);
        }

        return allResults;
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  // Remove all the Position values on the given axis
  private void removeAllPositionValuesOnAxis(Connection connection, int nAxisId)
      throws ClassifyEngineException {

    // build the statement to get the SilverObjectIds
    String sSQLStatement = sqlStatement.buildRemoveAllPositionValuesStatement(nAxisId);

      // Execute the removal
    try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
      prepStmt.executeUpdate();

      // Clear cache
      singlePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  // Remove all the positions with all the values at null
  // Return the deleted positionIds
  private List<Integer> removeEmptyPositions(Connection connection)
      throws ClassifyEngineException {
    // -----------------------------
    // Get the removed positionIds
    // -----------------------------
    String sSQLStatement = sqlStatement.buildGetEmptyPositionsStatement(nbMaxAxis);
    final List<Integer> allDeletedPositionIds = new ArrayList<>();
    try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement);
         final ResultSet resSet = prepStmt.executeQuery()) {
      while (resSet.next()) {
        allDeletedPositionIds.add(resSet.getInt(1));
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }

    // -----------------------------
    // Remove the empty positions
    // -----------------------------
    sSQLStatement = sqlStatement.buildRemoveEmptyPositionsStatement(nbMaxAxis);
    try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
      prepStmt.executeUpdate();

      // Clear cache
      singlePertinentAxis.clear();

    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }

    return allDeletedPositionIds;
  }

  /*
   * Replace the old value with the new value for all positions
   */
  public void replaceValuesOnAxis(Connection connection, List<Value> oldValue,
      List<Value> newValue) throws ClassifyEngineException {
    Objects.requireNonNull(connection);

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
        throw new ClassifyEngineException("Axis ids not identical");
      }
      if (oldV.getValue().equals(newV.getValue())) {
        throw new ClassifyEngineException("Axis values identical");
      }
    }

    try {
      // Update the value
      int nIndex = 0;
      while (nIndex < oldValue.size()) {
        // build the statement to get the SilverObjectIds
        String sSQLStatement = sqlStatement.buildReplaceValuesStatement(
            oldValue.get(nIndex), newValue.get(nIndex));

        // Execute the change
        try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
          prepStmt.executeUpdate();
        }

        nIndex++;
      }

      // Remove empty positions coming from the above change
      String sSQLStatement = sqlStatement.buildRemoveEmptyPositionsStatement(nbMaxAxis);
      // Execute the deletion of empty positions
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
        prepStmt.executeUpdate();
      }

      // Clear cache
      singlePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  private void checkSilverObjectId(int nSilverObjectId)
      throws ClassifyEngineException {
    if (nSilverObjectId < 0) {
      throw new ClassifyEngineException("Incorrect SilverObject id");
    }
  }

  private void checkPosition(Position<Value> position) throws ClassifyEngineException {
    if (position != null) {
      position.checkPosition();
    } else {
      throw new ClassifyEngineException("Null position");
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
      throw new ClassifyEngineException("Null criteria");
    }

    // Check that each criteria is valid
    for (Criteria criteria : alCriterias) {
      criteria.checkCriteria();
    }
  }

  private void checkAxisId(int nAxisId) throws ClassifyEngineException {
    if (nAxisId < 0) {
      throw new ClassifyEngineException("Incorrect axis id. nAxisId: " + nAxisId);
    }
  }

  /*
   * Return a List of PertinentAxis corresponding to the given criterias for the given AxisIds and
   * given Join Statement The return list is ordered like the given one considering the AxisId
   */
  public List<PertinentAxis> getPertinentAxisByJoin(List<? extends Criteria> alGivenCriterias,
      List<Integer> alAxisIds, List<String> instanceIds) throws ClassifyEngineException {
    // Convert the Axis Ids
    List<Criteria> alCriterias = buildCriteriaOnAxis(alGivenCriterias);

    try (final Connection connection = DBUtil.openConnection()) {

      String today = DateUtil.today2SQLDate();

      // Call the search On axis one by one
      ArrayList<PertinentAxis> alPertinentAxis = new ArrayList<>();
      for (Integer alAxisId : alAxisIds) {
        int nAxisId = this.getPhysicalAxisId(alAxisId);
        alPertinentAxis.add(getSinglePertinentAxisByJoin(connection,
            alCriterias, nAxisId, "", instanceIds, today));
      }

      return alPertinentAxis;
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * Return a PertinentAxis object corresponding to the given AxisId, rootValue and search Criterias
   */
  private PertinentAxis getSinglePertinentAxisByJoin(final Connection connection,
      final List<? extends Criteria> alCriterias, final int nAxisId, final String sRootValue,
      final List<String> instanceIds, final String todayFormatted) throws ClassifyEngineException {
    // Convert the Axis Ids
    final List<Criteria> allComputedCriteria = buildCriteriaOnAxis(alCriterias);

    // build the statements
    final String sSQLStatement =
        sqlStatement.buildGetPertinentAxisStatementByJoin(allComputedCriteria, nAxisId,
            sRootValue, instanceIds, todayFormatted);

    PertinentAxis pertinentAxis = singlePertinentAxis.get(sSQLStatement);
    if (pertinentAxis == null) {
      // Execute the finding
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement);
           final ResultSet resSet = prepStmt.executeQuery()) {

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
        singlePertinentAxis.put(sSQLStatement, pertinentAxis);


      } catch (Exception e) {
        throw new ClassifyEngineException(e);
      }
    }
    return pertinentAxis;
  }

  /*
   * Return a List of PertinentValues corresponding to the givenAxisId The return list is ordered
   * like the given one considering the AxisId
   */
  public List<PertinentValue> getPertinentValuesByJoin(List<? extends Criteria> alGivenCriterias,
      int nLogicalAxisId, List<String> instanceIds) throws ClassifyEngineException {

    // Convert the Axis Ids
    final List<Criteria> alCriterias = buildCriteriaOnAxis(alGivenCriterias);
    try (Connection connection = DBUtil.openConnection()) {

      String today = DateUtil.today2SQLDate();

      // Build the statement
      String sSQLStatement = sqlStatement.buildGetPertinentValueByJoinStatement(alCriterias, this.
          getPhysicalAxisId(nLogicalAxisId), instanceIds, today);

      // Execute the finding
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement);
           final ResultSet resSet = prepStmt.executeQuery()) {

        // Fetch the results
        ArrayList<PertinentValue> alPertinentValues = new ArrayList<>();
        while (resSet.next()) {
          PertinentValue pValue = new PertinentValue();
          pValue.setAxisId(nLogicalAxisId);
          pValue.setNbObjects(resSet.getInt(1));
          pValue.setValue(resSet.getString(2));

          alPertinentValues.add(pValue);
        }

        return alPertinentValues;
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }

  /*
   * Return a List of ObjectValuePair corresponding to the givenAxisId The return list is ordered
   * like the given one considering the AxisId
   */
  public List<ObjectValuePair> getObjectValuePairsByJoin(List<? extends Criteria> alGivenCriterias,
      int nLogicalAxisId, List<String> instanceIds) throws ClassifyEngineException {
    // Convert the Axis Ids
    List<Criteria> alCriterias = buildCriteriaOnAxis(alGivenCriterias);

    try (final Connection connection = DBUtil.openConnection()) {
      String today = DateUtil.today2SQLDate();

      // Build the statement
      String sSQLStatement = sqlStatement.buildGetObjectValuePairsByJoinStatement(alCriterias, this.
          getPhysicalAxisId(nLogicalAxisId), instanceIds, today, true);

      // Execute the finding
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement);
           final ResultSet resSet = prepStmt.executeQuery()) {
        // Fetch the results
        List<ObjectValuePair> objectValuePairs = new ArrayList<>();
        while (resSet.next()) {
          ObjectValuePair ovp =
              new ObjectValuePair(resSet.getString(1), resSet.getString(2), resSet.
                  getString(3));

          objectValuePairs.add(ovp);
        }

        return objectValuePairs;
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
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
      return new ArrayList<>();
    }

    try (final Connection connection = DBUtil.openConnection()) {
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
      final StringBuilder sSQLStatement = new StringBuilder(200);
      sSQLStatement.append("select * from sb_classifyengine_classify ");
      sSQLStatement.append("where objectid in ");
      sSQLStatement
          .append(
          "(select silvercontentid from sb_contentmanager_content, sb_contentmanager_instance where contentinstanceid in ");
      sSQLStatement.append(
          "(select instanceid from sb_contentmanager_instance where componentid IN (").append(
          inClause.toString()).append(")))");

      // Execute the finding
      try (final PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement.toString());
           final ResultSet resSet = prepStmt.executeQuery()) {
        final List<Integer> axisIds = new ArrayList<>();
        // Fetch the results
        while (resSet.next()) {
          for (int nI = 0; nI < nbMaxAxis; nI++) {
            String value = resSet.getString(3 + nI);
            int axisId = getLogicalAxisId(nI);
            if (StringUtil.isDefined(value) && !axisIds.contains(axisId)) {
              axisIds.add(axisId);
            }
          }
        }
        return axisIds;
      }
    } catch (Exception e) {
      throw new ClassifyEngineException(e);
    }
  }
}