package com.stratelia.silverpeas.classifyEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import com.stratelia.webactiv.util.exception.*;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.silverpeas.silvertrace.*;
import com.stratelia.silverpeas.util.JoinStatement;

/**
 * This class represents the ClassifyEngine API It gives access to functions for
 * classifying, unclassifying and searching SilverObjetIds
 * 
 * Assumption : The SilverObjetIds processed are int values from 0 to n The axis
 * processed are int values from 0 to n
 */

public class ClassifyEngine extends Object {
  // Maximum number of axis processed by the classifyEngine (from properties)
  static private int nbMaxAxis = 0;

  // Helper object to build all the SQL statements
  private static SQLStatement SQLStatement = new SQLStatement();

  // Database
  private static String m_dbName = JNDINames.CLASSIFYENGINE_DATASOURCE;
  private String m_sClassifyTable = "SB_ClassifyEngine_Classify";
  private String m_sPositionIdColumn = "PositionId";

  // Registered axis cache
  static private int[] m_anRegisteredAxis = null;

  // GetSinglePertinentAxis Cache
  static private Hashtable m_hSinglePertinentAxis = new Hashtable(0);

  // the date format used in database to represent a date
  static private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  // Init Function
  static {
    // Get the maximum number of axis that the classifyEngine can handle
    ResourceLocator res = new ResourceLocator(
        "com.stratelia.silverpeas.classifyEngine.ClassifyEngine", "");
    String sMaxAxis = res.getString("MaxAxis");
    nbMaxAxis = new Integer(sMaxAxis).intValue();
    try {
      m_anRegisteredAxis = loadRegisteredAxis();
    } catch (ClassifyEngineException e) {
      SilverTrace.error("classifyEngine", "ClassifyEngine.initStatic",
          "root.EX_CLASS_NOT_INITIALIZED",
          "m_anRegisteredAxis initialization failed !", e);
    }
  }

  // Return the maximum number of supported axis
  static public int getMaxAxis() {
    return nbMaxAxis;
  }

  /*
   * Constructor
   */
  public ClassifyEngine() throws ClassifyEngineException {
  }

  static public void clearCache() {
    m_hSinglePertinentAxis.clear();
  }

  /*
   * Register an axis
   */
  public void registerAxis(Connection connection, int nLogicalAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;

    // check that thie given axis is not already registered
    if (this.AxisAlreadyRegistered(nLogicalAxisId))
      throw new ClassifyEngineException("ClassifyEngine.registerAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_AXIS_ALREADY_REGISTERED", "nLogicalAxisId: "
              + nLogicalAxisId);

    try {
      synchronized (m_anRegisteredAxis) {
        // Get the next unregistered axis
        int nNextAvailableAxis = this.getNextUnregisteredAxis();
        if (nNextAvailableAxis == -1)
          throw new ClassifyEngineException("ClassifyEngine.registerAxis",
              SilverpeasException.ERROR,
              "classifyEngine.EX_NOMORE_AVAILABLE_AXIS", "nLogicalAxisId: "
                  + nLogicalAxisId);

        // build the statement to classify
        String sSQLStatement = SQLStatement.buildRegisterAxisStatement(
            nNextAvailableAxis, nLogicalAxisId);

        // Execute the insertion
        SilverTrace.info("classifyEngine", "ClassifyEngine.registerAxis",
            "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        // Register the axis in memory
        m_anRegisteredAxis[nNextAvailableAxis] = nLogicalAxisId;
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
  public List unregisterAxis(Connection connection, int nLogicalAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;
    List alDeletedPositionIds = null;

    // Check the minimum required
    int nAxis = this.getPhysicalAxisId(nLogicalAxisId);
    this.checkAxisId(nAxis);

    try {
      // build the statement to classify
      String sSQLStatement = SQLStatement.buildUnregisterAxisStatement(nAxis);

      synchronized (m_anRegisteredAxis) {
        // Execute the removal
        SilverTrace.info("classifyEngine", "ClassifyEngine.unregisterAxis",
            "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        // unregister the axis in memory
        m_anRegisteredAxis[nAxis] = -1;

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
  private static int[] loadRegisteredAxis() throws ClassifyEngineException {
    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    int[] tempRegisteredAxis = new int[nbMaxAxis];
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      // build the statement to load
      String sSQLStatement = SQLStatement.buildLoadRegisteredAxisStatement();

      // Execute the insertion
      SilverTrace.info("classifyEngine", "ClassifyEngine.loadRegisteredAxis",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // m_anRegisteredAxis = new int[nbMaxAxis];
      while (resSet.next())
        for (int nI = 0; nI < nbMaxAxis; nI++)
          tempRegisteredAxis[nI] = new Integer(resSet.getString(3 + nI))
              .intValue();

    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.loadRegisteredAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_LOAD_REGISTERED_AXIS", e);
    } finally {
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed()) {
          connection.close();
        }
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.loadRegisteredAxis",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
    return tempRegisteredAxis;
  }

  // Return the next unregistered axis
  private int getNextUnregisteredAxis() {
    int nNextUnregisteredAxis = -1;

    for (int nI = 0; nI < nbMaxAxis && nNextUnregisteredAxis == -1; nI++)
      if (m_anRegisteredAxis[nI] == -1)
        nNextUnregisteredAxis = nI;

    return nNextUnregisteredAxis;
  }

  // Return the physicalAxisId given the LogicalAxisId
  private int getPhysicalAxisId(int nLogicalAxisId)
      throws ClassifyEngineException {
    for (int nI = 0; nI < nbMaxAxis; nI++)
      if (m_anRegisteredAxis[nI] == nLogicalAxisId)
        return nI;

    SilverTrace.error("classifyEngine", "ClassifyEngine.getPhysicalAxisId",
        "root.MSG_GEN_PARAM_VALUE",
        "Can't get physical axis Id, nLogicalAxisId: " + nLogicalAxisId
            + ", m_anRegisteredAxis : " + printRegisteredAxis());
    throw new ClassifyEngineException("ClassifyEngine.getPhysicalAxisId",
        SilverpeasException.ERROR, "classifyEngine.EX_CANT_GET_PHYSICAL_AXIS",
        "nLogicalAxisId: " + nLogicalAxisId);
  }

  private String printRegisteredAxis() {
    StringBuffer sRegister = new StringBuffer(100);
    sRegister.append("[");
    for (int nI = 0; nI < nbMaxAxis; nI++)
      sRegister.append(m_anRegisteredAxis[nI]).append(", ");
    sRegister.append("]");
    return sRegister.toString();
  }

  // Return the LogicalAxisId given the physicalAxisId
  private int getLogicalAxisId(int nPhysicalAxisId)
      throws ClassifyEngineException {
    if (nPhysicalAxisId < 0 || nPhysicalAxisId > m_anRegisteredAxis.length)
      throw new ClassifyEngineException("ClassifyEngine.getLogicalAxisId",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_GET_LOGICAL_AXIS",
          "nPhysicalAxisId: " + nPhysicalAxisId);

    return m_anRegisteredAxis[nPhysicalAxisId];
  }

  // Return if the LogicalAxisId given is already registered
  private boolean AxisAlreadyRegistered(int nLogicalAxisId)
      throws ClassifyEngineException {
    for (int nI = 0; nI < nbMaxAxis; nI++)
      if (m_anRegisteredAxis[nI] == nLogicalAxisId)
        return true;

    return false;
  }

  public int isPositionAlreadyExists(int nSilverObjectId, Position position)
      throws ClassifyEngineException {
    // Convert the Axis Ids
    List alValues = position.getValues();
    for (int nI = 0; nI < alValues.size(); nI++) {
      Value value = (Value) alValues.get(nI);
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String sSQLStatement = null;
    Connection connection = null;
    try {
      // Open the connection if necessary
      connection = DBUtil.makeConnection(m_dbName);

      // Check if the position already exists
      sSQLStatement = SQLStatement.buildVerifyStatement(nSilverObjectId,
          position);

      // Execute the verification
      SilverTrace.info("classifyEngine", "ClassifyEngine.classifySilverObject",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      rs = prepStmt.executeQuery();
      int newPositionId = -1;
      if (rs.next()) {
        newPositionId = rs.getInt(1);
      }

      return newPositionId;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.isPositionAlreadyExists", SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_CLASSIFY_SILVEROBJECTID", "sSQLStatement= "
              + sSQLStatement, e);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.isPositionAlreadyExists",
            "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /*
   * Classify the given SilverObjectid within the classifyEngine If the given
   * connection is null, then we have to open a connection and close it Return
   * the PositionId
   */
  public int classifySilverObject(Connection connection, int nSilverObjectId,
      Position position) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(nSilverObjectId, position);

    // Convert the Axis Ids
    List alValues = position.getValues();
    for (int nI = 0; nI < alValues.size(); nI++) {
      Value value = (Value) alValues.get(nI);
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // build the statement to classify
      int newPositionId = DBUtil.getNextId(m_sClassifyTable,
          m_sPositionIdColumn);
      String sSQLStatement = SQLStatement.buildClassifyStatement(
          nSilverObjectId, position, newPositionId);

      // Execute the insertion
      SilverTrace.info("classifyEngine", "ClassifyEngine.classifySilverObject",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();

      return newPositionId;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.classifySilverObject",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_CLASSIFY_SILVEROBJECTID", "nSilverObjectId= "
              + nSilverObjectId, e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.classifySilverObject",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * Remove the given SilverObjectId at th egiven position within the
   * classifyEngine If the given connection is null, then we have to open a
   * connection and close it
   */
  public void unclassifySilverObjectByPosition(Connection connection,
      int nSilverObjectId, Position position) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(nSilverObjectId, position);

    // Convert the Axis Ids
    List alValues = position.getValues();
    for (int nI = 0; nI < alValues.size(); nI++) {
      Value value = (Value) alValues.get(nI);
      value.setAxisId(this.getPhysicalAxisId(value.getAxisId()));
    }
    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // build the statement to remove the position
      String sSQLStatement = SQLStatement.buildRemoveByPositionStatement(
          nSilverObjectId, position);

      // Execute the removal
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.unclassifySilverObjectByPosition",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.unclassifySilverObjectByPosition",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_SILVEROBJECTID_POSITION",
          "nSilverObjectId= " + nSilverObjectId, e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.unclassifySilverObjectByPosition",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
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
      String sSQLStatement = SQLStatement
          .buildRemoveSilverObjectStatement(nSilverObjectId);

      // Execute the removal
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.unclassifySilverObject", "root.MSG_GEN_PARAM_VALUE",
          "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.unclassifySilverObject", SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_REMOVE_SILVEROBJECTID_POSITION",
          "nSilverObjectId= " + nSilverObjectId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /*
   * Remove the SilverObject referenced with the given positionId within the
   * classifyEngine If the given connection is null, then we have to open a
   * connection and close it
   */
  public void unclassifySilverObjectByPositionId(Connection connection,
      int nPositionId) throws ClassifyEngineException {
    boolean bCloseConnection = false;
    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // build the statement to remove the position
      String sSQLStatement = SQLStatement
          .buildRemoveByPositionIdStatement(nPositionId);

      // Execute the removal
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.unclassifySilverObjectByPositionId",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
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
      try {
        DBUtil.close(prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.unclassifySilverObjectByPositionId",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * update the given new position within the classifyEngine If the given
   * connection is null, then we have to open a connection and close it
   */
  public void updateSilverObjectPosition(Connection connection,
      Position newPosition) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkPosition(newPosition);

    // Convert the Axis Ids
    List alValues = newPosition.getValues();
    for (int nI = 0; nI < alValues.size(); nI++) {
      Value value = (Value) alValues.get(nI);
      value.setPhysicalAxisId(getPhysicalAxisId(value.getAxisId()));
    }

    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // build the statement to update the position
      String sSQLStatement = SQLStatement
          .buildUpdateByPositionIdStatement(newPosition);

      // Execute the update
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.updateSilverObjectPosition",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
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
      try {
        DBUtil.close(prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.updateSilverObjectPosition",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * update several position with the given new position within the
   * classifyEngine If the given connection is null, then we have to open a
   * connection and close it add by SAN
   */
  public void updateSilverObjectPositions(Connection connection,
      List classifyValues, int nSilverObjectId) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    Value value = null;
    PreparedStatement prepStmt = null;
    try {
      // Open the connection if necessary
      if (connection == null) {
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }
      for (int i = 0; i < classifyValues.size(); i++) {
        value = (Value) classifyValues.get(i);
        // faut utiliser l'instruction suivante on a deja fait toute cette
        // operation
        // dans la méthode updateSilverObjectPosition.
        // value.setAxisId(this.getPhysicalAxisId(value.getAxisId()));

        // build the statement to update the position
        String sSQLStatement = SQLStatement.buildUpdateByObjectIdStatement(
            value, nSilverObjectId);

        // Execute the update
        SilverTrace.info("classifyEngine",
            "ClassifyEngine.updateSilverObjectPositions__SEA",
            "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
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
      try {
        DBUtil.close(prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.updateSilverObjectPosition",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public List findSilverOjectByCriterias(List alGivenCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String afterDate, String beforeDate) throws ClassifyEngineException {
    return findSilverOjectByCriterias(alGivenCriterias, joinStatementContainer,
        joinStatementContent, afterDate, beforeDate, true, true);
  }

  /*
   * Find all the SilverObjectId corresponding to the given criterias and the
   * given Join Statement
   */
  public List findSilverOjectByCriterias(List alGivenCriterias,
      JoinStatement joinStatementContainer, JoinStatement joinStatementContent,
      String afterDate, String beforeDate, boolean recursiveSearch,
      boolean visibilitySensitive) throws ClassifyEngineException {
    Connection connection = null;
    ArrayList alObjectIds = new ArrayList();

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    ArrayList alCriterias = new ArrayList();
    for (int nI = 0; nI < alGivenCriterias.size(); nI++) {
      Criteria criteria = (Criteria) alGivenCriterias.get(nI);
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      String today = formatter.format(new java.util.Date());

      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement.buildFindByCriteriasStatementByJoin(
          alCriterias, joinStatementContainer, joinStatementContent, today,
          recursiveSearch, visibilitySensitive);

      // Execute the finding
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.findSilverOjectByCriterias",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);

      // works on dates
      if ((beforeDate != null && beforeDate.length() > 0)
          && (afterDate != null && afterDate.length() > 0)) {
        prepStmt.setDate(1, new Date(formatter.parse(beforeDate).getTime()));
        prepStmt.setDate(2, new Date(formatter.parse(afterDate).getTime()));
      } else if (beforeDate != null && beforeDate.length() > 0) {
        prepStmt.setDate(1, new Date(formatter.parse(beforeDate).getTime()));
      } else if (afterDate != null && afterDate.length() > 0) {
        prepStmt.setDate(1, new Date(formatter.parse(afterDate).getTime()));
      }
      SilverTrace.debug("classifyEngine",
          "ClassifyEngine.findSilverOjectByCriterias",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      resSet = prepStmt.executeQuery();
      SilverTrace.debug("classifyEngine",
          "ClassifyEngine.findSilverOjectByCriterias",
          "root.MSG_GEN_PARAM_VALUE", "query executed !");

      // Fetch the results
      while (resSet.next())
        alObjectIds.add(new Integer(resSet.getInt(1)));

      return alObjectIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.findSilverOjectByCriterias",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_FIND_SILVEROBJECTID", e);
    } finally {
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.findSilverOjectByCriterias",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * get the SilverContentIds corresponding to the given PositionIds
   */
  public List getSilverContentIdsByPositionIds(List alPositionids)
      throws ClassifyEngineException {
    if (alPositionids == null || alPositionids.size() == 0)
      return new ArrayList();

    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement
          .buildSilverContentIdsByPositionIdsStatement(alPositionids);

      // Execute the finding
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getSilverContentIdsByPositionIds",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      // Fetch the results and convert them in Positions
      ArrayList alSilverContentIds = new ArrayList();
      while (resSet.next())
        alSilverContentIds.add(new Integer(resSet.getInt(1)));

      return alSilverContentIds;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getSilverContentIdsByPositionIds",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_SILVERCONTENTIDS_BYSILVEROBJECTIDS", e);
    } finally {
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.getSilverContentIdsByPositionIds",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * Find all the Positions corresponding to the given SilverObjectId
   */
  public List findPositionsBySilverOjectId(int nSilverObjectId)
      throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkSilverObjectId(nSilverObjectId);
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement
          .buildFindBySilverObjectIdStatement(nSilverObjectId);

      // Execute the finding
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.findPositionsBySilverOjectId",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results and convert them in Positions
      ArrayList alResults = new ArrayList();
      while (resSet.next()) {
        Position position = new Position();
        position.setPositionId(resSet.getInt(1));

        ArrayList alValues = new ArrayList();
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
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.findPositionsBySilverOjectId",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  // Remove all the Position values on the given axis
  private void removeAllPositionValuesOnAxis(Connection connection, int nAxisId)
      throws ClassifyEngineException {
    PreparedStatement prepStmt = null;
    try {
      // build the statement to get the SilverObjectIds
      String sSQLStatement = SQLStatement
          .buildRemoveAllPositionValuesStatement(nAxisId);

      // Execute the removal
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.removeAllPositionValuesOnAxis",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
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
  private List removeEmptyPositions(Connection connection)
      throws ClassifyEngineException {
    // -----------------------------
    // Get the removed positionIds
    // -----------------------------

    // build the statement to get the empty positions
    String sSQLStatement = SQLStatement
        .buildGetEmptyPositionsStatement(nbMaxAxis);
    ArrayList alDeletedPositionIds = new ArrayList();
    // Execute the query
    SilverTrace.info("classifyEngine", "ClassifyEngine.removeEmptyPositions",
        "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      while (resSet.next())
        alDeletedPositionIds.add(new Integer(resSet.getInt(1)));
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
      sSQLStatement = SQLStatement
          .buildRemoveEmptyPositionsStatement(nbMaxAxis);

      // Execute the removal
      SilverTrace.info("classifyEngine", "ClassifyEngine.removeEmptyPositions",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
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
  public void replaceValuesOnAxis(Connection connection, List oldValue,
      List newValue) throws ClassifyEngineException {
    boolean bCloseConnection = false;

    // For all the given values
    for (int nI = 0; nI < oldValue.size(); nI++) {
      Value oldV = (Value) oldValue.get(nI);
      Value newV = (Value) newValue.get(nI);

      // Convert the axis Ids
      oldV.setAxisId(this.getPhysicalAxisId(oldV.getAxisId()));
      newV.setAxisId(this.getPhysicalAxisId(newV.getAxisId()));

      // Check the minimum required
      this.checkAxisId(oldV.getAxisId());
      if (oldV.getAxisId() != newV.getAxisId())
        throw new ClassifyEngineException("ClassifyEngine.replaceValuesOnAxis",
            SilverpeasException.ERROR,
            "classifyEngine.EX_AXISVALUES_NOT_IDENTICAL");
      if (oldV.getValue().equals(newV.getValue()))
        throw new ClassifyEngineException("ClassifyEngine.replaceValuesOnAxis",
            SilverpeasException.ERROR, "classifyEngine.EX_VALUES_IDENTICAL");
    }

    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        // Open the connection
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // Update the value
      int nIndex = 0;
      while (nIndex < oldValue.size()) {
        // build the statement to get the SilverObjectIds
        String sSQLStatement = SQLStatement.buildReplaceValuesStatement(
            (Value) oldValue.get(nIndex), (Value) newValue.get(nIndex));

        // Execute the change
        SilverTrace.info("classifyEngine",
            "ClassifyEngine.replaceValuesOnAxis", "root.MSG_GEN_PARAM_VALUE",
            "sSQLStatement= " + sSQLStatement);
        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.executeUpdate();

        nIndex++;
      }

      // Clear cache
      m_hSinglePertinentAxis.clear();
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.replaceValuesOnAxis",
          SilverpeasException.ERROR, "classifyEngine.EX_CANT_REPLACE_VALUES", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.replaceValuesOnAxis",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private void checkSilverObjectId(int nSilverObjectId)
      throws ClassifyEngineException {
    if (nSilverObjectId < 0)
      throw new ClassifyEngineException("ClassifyEngine.checkParameters",
          SilverpeasException.ERROR,
          "classifyEngine.EX_INCORRECT_SILVEROBJECTID");
  }

  private void checkPosition(Position position) throws ClassifyEngineException {
    if (position != null)
      position.checkPosition();
    else
      throw new ClassifyEngineException("ClassifyEngine.checkParameters",
          SilverpeasException.ERROR, "classifyEngine.EX_POSITION_NULL");
  }

  private void checkParameters(int nSilverObjectId, Position position)
      throws ClassifyEngineException {
    this.checkSilverObjectId(nSilverObjectId);
    this.checkPosition(position);
  }

  private void checkCriterias(List alCriterias) throws ClassifyEngineException {
    // Check if the given array of criterias is valid
    if (alCriterias == null)
      throw new ClassifyEngineException("ClassifyEngine.checkCriterias",
          SilverpeasException.ERROR, "classifyEngine.EX_CRITERIAS_ARRAY_NULL");

    // Check that each criteria is valid
    for (int nI = 0; nI < alCriterias.size(); nI++)
      ((Criteria) alCriterias.get(nI)).checkCriteria();
  }

  private void checkAxisId(int nAxisId) throws ClassifyEngineException {
    if (nAxisId < 0)
      throw new ClassifyEngineException("ClassifyEngine.checkAxisId",
          SilverpeasException.ERROR,
          "classifyEngine.EX_INCORRECT_AXISID_VALUE", "nAxisId: " + nAxisId);
  }

  /*
   * Return a List of PertinentAxis corresponding to the given criterias for the
   * given AxisIds The return list is ordered like the given one considering the
   * AxisId
   */
  public List getPertinentAxis(List alGivenCriterias, List alAxisIds)
      throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    ArrayList alCriterias = new ArrayList();
    for (int nI = 0; nI < alGivenCriterias.size(); nI++) {
      Criteria criteria = (Criteria) alGivenCriterias.get(nI);
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      String today = formatter.format(new java.util.Date());

      // Call the search On axis one by one
      ArrayList alPertinentAxis = new ArrayList();
      for (int nI = 0; nI < alAxisIds.size(); nI++) {
        int nAxisId = this.getPhysicalAxisId(((Integer) alAxisIds.get(nI))
            .intValue());
        alPertinentAxis.add(this.getSinglePertinentAxis(connection,
            alCriterias, nAxisId, today));
      }

      return alPertinentAxis;
    } catch (Exception e) {
      throw new ClassifyEngineException("ClassifyEngine.getPertinentAxis",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_AXIS", e);
    } finally {
      try {
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine", "ClassifyEngine.getPertinentAxis",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private PertinentAxis getSinglePertinentAxis(Connection connection,
      List alCriterias, int nAxisId, String todayFormatted)
      throws SQLException, ClassifyEngineException {
    // build the statements
    String sSQLStatement = SQLStatement.buildGetPertinentAxisStatement(
        alCriterias, nAxisId, todayFormatted);

    // Execute the finding
    SilverTrace.info("classifyEngine", "ClassifyEngine.getSinglePertinentAxis",
        "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    PertinentAxis pAxis = null;
    try {
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getSinglePertinentAxis", "root.MSG_GEN_PARAM_VALUE",
          "Query executed !");
      // Fetch the results
      pAxis = new PertinentAxis();
      pAxis.setAxisId(this.getLogicalAxisId(nAxisId));
      int nDocs = 0;
      while (resSet.next())
        nDocs += resSet.getInt(1);
      pAxis.setNbObjects(nDocs);
    } finally {
      DBUtil.close(resSet, prepStmt);
    }

    return pAxis;
  }

  /*
   * Return a List of PertinentAxis corresponding to the given criterias for the
   * given AxisIds and given Join Statement The return list is ordered like the
   * given one considering the AxisId
   */
  public List getPertinentAxisByJoin(List alGivenCriterias, List alAxisIds,
      JoinStatement joinStatementAllPositions) throws ClassifyEngineException {
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    ArrayList alCriterias = new ArrayList();
    for (int nI = 0; nI < alGivenCriterias.size(); nI++) {
      Criteria criteria = (Criteria) alGivenCriterias.get(nI);
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      String today = formatter.format(new java.util.Date());

      // Call the search On axis one by one
      ArrayList alPertinentAxis = new ArrayList();
      for (int nI = 0; nI < alAxisIds.size(); nI++) {
        int nAxisId = this.getPhysicalAxisId(((Integer) alAxisIds.get(nI))
            .intValue());
        alPertinentAxis.add(this.getSinglePertinentAxisByJoin(connection,
            alCriterias, nAxisId, "", joinStatementAllPositions, today));
      }

      return alPertinentAxis;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getPertinentAxisByJoin", SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_AXIS", e);
    } finally {
      try {
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.getPertinentAxisByJoin",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public PertinentAxis getSinglePertinentAxisByJoin(Connection connection,
      List alCriterias, int nAxisId, String sRootValue,
      JoinStatement joinStatementAllPositions) throws SQLException,
      ClassifyEngineException {
    String today = formatter.format(new java.util.Date());
    return getSinglePertinentAxisByJoin(connection, alCriterias, nAxisId,
        sRootValue, joinStatementAllPositions, today);
  }

  /*
   * Return a PertinentAxis object corresponding to the given AxisId, rootValue
   * and search Criterias
   */
  public PertinentAxis getSinglePertinentAxisByJoin(Connection connection,
      List alCriterias, int nAxisId, String sRootValue,
      JoinStatement joinStatementAllPositions, String todayFormatted)
      throws SQLException, ClassifyEngineException {
    SilverTrace.info("classifyEngine",
        "ClassifyEngine.getSinglePertinentAxisByJoin",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + nAxisId);
    boolean bCloseConnection = false;

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      if (connection == null) {
        // Check the minimum required
        this.checkCriterias(alCriterias);

        // Convert the Axis Ids
        ArrayList alComputedCriterias = new ArrayList();
        for (int nI = 0; nI < alCriterias.size(); nI++) {
          Criteria criteria = (Criteria) alCriterias.get(nI);
          alComputedCriterias.add(new Criteria(this.getPhysicalAxisId(criteria
              .getAxisId()), criteria.getValue()));
        }
        alCriterias = alComputedCriterias;

        // Convert the logicalAxisId
        nAxisId = this.getPhysicalAxisId(nAxisId);

        // Open the connection
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // build the statements
      String sSQLStatement = SQLStatement.buildGetPertinentAxisStatementByJoin(
          alCriterias, nAxisId, sRootValue, joinStatementAllPositions,
          todayFormatted);

      PertinentAxis pertinentAxis = (PertinentAxis) m_hSinglePertinentAxis
          .get(sSQLStatement);
      if (pertinentAxis == null) {
        // Execute the finding
        SilverTrace.info("classifyEngine",
            "ClassifyEngine.getSinglePertinentAxisByJoin",
            "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
        prepStmt = connection.prepareStatement(sSQLStatement);
        resSet = prepStmt.executeQuery();
        SilverTrace.info("classifyEngine",
            "ClassifyEngine.getSinglePertinentAxisByJoin",
            "root.MSG_GEN_PARAM_VALUE", "Query executed !");

        // Fetch the results
        pertinentAxis = new PertinentAxis();
        pertinentAxis.setAxisId(this.getLogicalAxisId(nAxisId));
        int nDocs = 0;
        while (resSet.next())
          nDocs += resSet.getInt(1);
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
      try {
        DBUtil.close(resSet, prepStmt);
        if (bCloseConnection && connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.getSinglePertinentAxisByJoin",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getSinglePertinentAxisByJoin",
          "root.MSG_GEN_EXIT_METHOD");
    }
  }

  /*
   * Return a List of PertinentValues corresponding to the givenAxisId The
   * return list is ordered like the given one considering the AxisId
   */
  public List getPertinentValues(List alGivenCriterias, int nLogicalAxisId)
      throws ClassifyEngineException {
    SilverTrace.info("classifyEngine", "ClassifyEngine.getPertinentValues",
        "root.MSG_GEN_ENTER_METHOD", "nLogicalAxisId = " + nLogicalAxisId);

    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    ArrayList alCriterias = new ArrayList();
    for (int nI = 0; nI < alGivenCriterias.size(); nI++) {
      Criteria criteria = (Criteria) alGivenCriterias.get(nI);
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      String today = formatter.format(new java.util.Date());

      // Build the statement
      String sSQLStatement = SQLStatement.buildGetPertinentValueStatement(
          alCriterias, this.getPhysicalAxisId(nLogicalAxisId), today);

      // Execute the finding
      SilverTrace.info("classifyEngine", "ClassifyEngine.getPertinentValues",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      ArrayList alPertinentValues = new ArrayList();
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
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.getPertinentValues",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * Return a List of PertinentValues corresponding to the givenAxisId The
   * return list is ordered like the given one considering the AxisId
   */
  public List getPertinentValuesByJoin(List alGivenCriterias,
      int nLogicalAxisId, JoinStatement joinStatementAllPositions)
      throws ClassifyEngineException {
    SilverTrace.info("classifyEngine",
        "ClassifyEngine.getPertinentValuesByJoin", "root.MSG_GEN_ENTER_METHOD",
        "nLogicalAxisId = " + nLogicalAxisId);
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    ArrayList alCriterias = new ArrayList();
    for (int nI = 0; nI < alGivenCriterias.size(); nI++) {
      Criteria criteria = (Criteria) alGivenCriterias.get(nI);
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      String today = formatter.format(new java.util.Date());

      // Build the statement
      String sSQLStatement = SQLStatement
          .buildGetPertinentValueByJoinStatement(alCriterias, this
              .getPhysicalAxisId(nLogicalAxisId), joinStatementAllPositions,
              today);

      // Execute the finding
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getPertinentValuesByJoin",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getPertinentValuesByJoin",
          "root.MSG_GEN_PARAM_VALUE", "Query executed !");

      // Fetch the results
      ArrayList alPertinentValues = new ArrayList();
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
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.getPertinentValuesByJoin",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /*
   * Return a List of ObjectValuePair corresponding to the givenAxisId The
   * return list is ordered like the given one considering the AxisId
   */
  public List getObjectValuePairsByJoin(List alGivenCriterias,
      int nLogicalAxisId, JoinStatement joinStatementAllPositions)
      throws ClassifyEngineException {
    SilverTrace.info("classifyEngine",
        "ClassifyEngine.getObjectValuePairsByJoin",
        "root.MSG_GEN_ENTER_METHOD", "nLogicalAxisId = " + nLogicalAxisId);
    Connection connection = null;

    // Check the minimum required
    this.checkCriterias(alGivenCriterias);

    // Convert the Axis Ids
    List alCriterias = new ArrayList();
    for (int nI = 0; nI < alGivenCriterias.size(); nI++) {
      Criteria criteria = (Criteria) alGivenCriterias.get(nI);
      alCriterias.add(new Criteria(
          this.getPhysicalAxisId(criteria.getAxisId()), criteria.getValue()));
    }

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);

      String today = formatter.format(new java.util.Date());

      // Build the statement
      String sSQLStatement = SQLStatement
          .buildGetObjectValuePairsByJoinStatement(alCriterias, this
              .getPhysicalAxisId(nLogicalAxisId), joinStatementAllPositions,
              today, true);

      // Execute the finding
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getObjectValuePairsByJoin",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();
      SilverTrace.info("classifyEngine",
          "ClassifyEngine.getObjectValuePairsByJoin",
          "root.MSG_GEN_PARAM_VALUE", "Query executed !");

      // Fetch the results
      List objectValuePairs = new ArrayList();
      while (resSet.next()) {
        ObjectValuePair ovp = new ObjectValuePair(resSet.getInt(1), resSet
            .getString(2), resSet.getString(3));

        objectValuePairs.add(ovp);
      }

      return objectValuePairs;
    } catch (Exception e) {
      throw new ClassifyEngineException(
          "ClassifyEngine.getObjectValuePairsByJoin",
          SilverpeasException.ERROR,
          "classifyEngine.EX_CANT_GET_PERTINENT_VALUES", e);
    } finally {
      try {
        DBUtil.close(resSet, prepStmt);
        if (connection != null && !connection.isClosed())
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("classifyEngine",
            "ClassifyEngine.getObjectValuePairsByJoin",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

}