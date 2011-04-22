/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

// TODO : reporter dans CVS (done)
package com.stratelia.silverpeas.containerManager;

import java.util.*;
import java.sql.*;

import com.stratelia.webactiv.util.exception.*;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.silverpeas.silvertrace.*;
import com.stratelia.silverpeas.util.JoinStatement;

/**
 * This class represents the ContainerManager API It is the gateway to all the silverpeas containers
 * (PDC, ....)
 */
public class ContainerManager extends Object implements java.io.Serializable {

  private static final long serialVersionUID = 3059920239753917851L;
  // Container peas
  private static boolean s_bDescriptorsRead = false;
  private static List<ContainerPeas> s_acContainerPeas = null;

  // Association componentId instanceId association (cache)
  // private static List s_asAssoInstanceId = null;
  // private static List s_asAssoComponentId = null;
  private static Hashtable<String, String> assoComponentIdInstanceId = null;

  // Datebase properties
  private static String m_dbName = JNDINames.CONTAINERMANAGER_DATASOURCE;
  private static String m_sInstanceTable = "SB_ContainerManager_Instance";
  String m_sLinksTable = "SB_ContainerManager_Links";

  static {
    try {
      assoComponentIdInstanceId = new Hashtable<String, String>(loadAsso(null));
    } catch (ContainerManagerException e) {
      SilverTrace.error("containerManager", "ContainerManager.initStatic",
          "root.EX_CLASS_NOT_INITIALIZED",
          "assoComponentIdInstanceId initialization failed !", e);
    }
  }

  /**
   * Constructor declaration
   * @see
   */
  public ContainerManager() throws ContainerManagerException {
    // If the container descriptors (.xml) have not been read, do it
    if (!s_bDescriptorsRead) {
      // -------------------------------------------------
      // We don't have enough time to do the parsing !!!
      // We hard coded for this time !!!!
      // -------------------------------------------------
      ContainerPeas containerPDC = new ContainerPeas("containerPDC");

      // Put the PDC container in the array of containers
      s_acContainerPeas = new ArrayList<ContainerPeas>(); // Only PDC
      s_acContainerPeas.add(containerPDC);

      // Set the read flag to true
      s_bDescriptorsRead = true;
    }

    // Load the instanceId-componentId cache
    /*
     * if (s_asAssoInstanceId == null && s_asAssoComponentId == null) { s_asAssoInstanceId = new
     * ArrayList(); s_asAssoComponentId = new ArrayList(); this.loadAsso(null); }
     */
  }

  /**
   * When a generic component is instanciate, this function is called to register the association
   * between container and content
   */
  public int registerNewContainerInstance(Connection connection,
      String sComponentId, String sContainerType, String sContentType)
      throws ContainerManagerException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(sComponentId, sContainerType, sContentType);

    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        // Open connection
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // Compute the next instanceId
      int newInstanceId = DBUtil.getNextId(m_sInstanceTable, new String(
          "instanceId"));

      // Insert the association container - content
      String sSQLStatement = "INSERT INTO " + m_sInstanceTable
          + "(instanceId, componentId, containerType, contentType) ";

      sSQLStatement += "VALUES (" + newInstanceId + ",'" + sComponentId + "','"
          + sContainerType + "','" + sContentType + "')";

      // Execute the insertion
      SilverTrace.info("containerManager",
          "ContainerManager.registerNewContainerInstance",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);

      prepStmt.executeUpdate();

      // Reload the cache
      // this.loadAsso(connection);

      addAsso(sComponentId, newInstanceId);

      return newInstanceId;
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.registerNewContainerInstance",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_REGISTER_CONTAINER_INSTANCE",
          "sComponentId: " + sComponentId + "    sContainerType: "
          + sContainerType, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection)
        closeConnection(connection);
    }
  }

  /**
   * When a generic component is uninstanciate, this function is called to unregister the
   * association between container and content
   */
  public void unregisterNewContainerInstance(Connection connection,
      String sComponentId, String sContainerType, String sContentType)
      throws ContainerManagerException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(sComponentId, sContainerType, sContentType);
    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        // Open connection
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // Remove the association container - content
      String sSQLStatement = "DELETE FROM " + m_sInstanceTable + " WHERE (";

      sSQLStatement += "componentId = '" + sComponentId
          + "') AND (containerType = '" + sContainerType
          + "') AND (contentType = '" + sContentType + "')";

      // Execute the insertion
      SilverTrace.info("containerManager",
          "ContainerManager.unregisterNewContainerInstance",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);

      prepStmt.executeUpdate();

      // Reload the cache
      // this.loadAsso(connection);

      removeAsso(sComponentId);
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.unregisterNewContainerInstance",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_UNREGISTER_CONTAINER_INSTANCE",
          "sComponentId: " + sComponentId + "    sContainerType: "
          + sContainerType, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection)
        closeConnection(connection);
    }
  }

  /**
   * Return the containerPeas corresponding to the given componentId
   */
  public ContainerPeas getContainerPeas(String sComponentId)
      throws ContainerManagerException {
    // Get the containerType
    String sContainerType = this.getContainerType(sComponentId);

    // Get the containerPeas from the containerType
    for (int nI = 0; nI < s_acContainerPeas.size(); nI++) {
      if (((ContainerPeas) s_acContainerPeas.get(nI)).getType().equals(
          sContainerType)) {
        return (ContainerPeas) s_acContainerPeas.get(nI);

      }
    }
    return null;
  }

  // Return the container type corresponding to the given componentId

  /**
   * Method declaration
   * @param componentId
   * @return
   * @throws ContainerManagerException
   * @see
   */
  private String getContainerType(String componentId)
      throws ContainerManagerException {
    // Build the SQL statement
    String sSQLStatement = "SELECT containerType FROM " + m_sInstanceTable
        + " WHERE (componentId = '" + componentId + "')";

    // Get the contentType from the DB Query
    String sContainerType = this.getFirstStringValue(sSQLStatement);

    return sContainerType;
  }

  // Return the containerPeas corresponding to the given ContainerInstanceId

  /**
   * Method declaration
   * @param connection
   * @param nContainerInstanceId
   * @return
   * @throws Exception
   * @see
   */
  private ContainerPeas getContainerPeas(Connection connection,
      int nContainerInstanceId) throws Exception {
    // Build the statement
    String sSQLStatement = "SELECT * FROM " + m_sInstanceTable
        + " WHERE( instanceId = " + nContainerInstanceId + ")";

    // Execute the query
    SilverTrace.info("containerManager", "ContainerManager.getContainerPeas",
        "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);

    // Get the containerType
    String sContainerType = null;
    PreparedStatement prepStmt = null;

    try {
      prepStmt = connection.prepareStatement(sSQLStatement);
      ResultSet resSet = prepStmt.executeQuery();

      while (resSet.next() && sContainerType == null) {
        sContainerType = resSet.getString(3);
      }
    } finally {
      DBUtil.close(prepStmt);
    }

    // return the containerPeas
    return this.getContainerPeasByType(sContainerType);
  }

  // Return the containerPeas corresponding to the given containerType

  /**
   * Method declaration
   * @param sContainerType
   * @return
   * @throws ContainerManagerException
   * @see
   */
  public ContainerPeas getContainerPeasByType(String sContainerType)
      throws ContainerManagerException {
    for (int nI = 0; s_acContainerPeas != null && nI < s_acContainerPeas.size(); nI++) {
      if (((ContainerPeas) s_acContainerPeas.get(nI)).getType().equals(
          sContainerType)) {
        return (ContainerPeas) s_acContainerPeas.get(nI);
      }
    }
    throw new ContainerManagerException("ContainerManager.getContainerPeas",
        SilverpeasException.ERROR,
        "containerManager.EX_CANT_FIND_CONTAINERPEAS");
  }

  /**
   * Return the container instance Id corresponding to the componentId
   */
  public int getContainerInstanceId(String sComponentId)
      throws ContainerManagerException {
    // Check the parameter
    this.checkComponentId(sComponentId);

    // Find the index of the component in the cache
    // int nIndex = 0;
    int containerInstanceId = -1; // -1 the input component is not a component
    // of a container

    /*
     * for (nIndex = 0; nIndex < s_asAssoComponentId.size(); nIndex++) { if (((String)
     * s_asAssoComponentId.get(nIndex)).equals(sComponentId)) { containerInstanceId = ((Integer)
     * s_asAssoInstanceId.get(nIndex)).intValue(); break; } }
     */

    String sContainerInstanceId = getInstanceId(sComponentId);

    // if (nIndex < 0 || s_asAssoComponentId.size() == 0 || nIndex >
    // s_asAssoComponentId.size() - 1)
    if (sContainerInstanceId != null) {
      containerInstanceId = Integer.parseInt(sContainerInstanceId);
    } else {
      // the given instance is not registered. This code is used to maintains
      // compatibility with previous versions.
      String componentName = extractComponentNameFromInstanceId(sComponentId);
      containerInstanceId = registerNewContainerInstance(null, sComponentId,
          "containerPDC", componentName);
      // loadAsso(null);
    }

    /*
     * SilverTrace.info("containerManager", "ContainerManager.getContainerInstanceId",
     * "root.MSG_GEN_PARAM_VALUE", "s_asAssoComponentId.size() = " + s_asAssoComponentId.size());
     * SilverTrace.info("containerManager", "ContainerManager.getContainerInstanceId",
     * "root.MSG_GEN_PARAM_VALUE", "s_asAssoInstanceId.size() = " + s_asAssoInstanceId.size());
     */
    SilverTrace.info("containerManager",
        "ContainerManager.getContainerInstanceId", "root.MSG_GEN_PARAM_VALUE",
        "getAsso().size() = " + getAsso().size());
    return containerInstanceId;
  }

  private String extractComponentNameFromInstanceId(String instanceId) {
    StringBuffer componentName = new StringBuffer();
    char character;
    for (int i = 0; i < instanceId.length(); i++) {
      character = instanceId.charAt(i);
      if (character == '0' || character == '1' || character == '2'
          || character == '3' || character == '4' || character == '5'
          || character == '6' || character == '7' || character == '8'
          || character == '9') {
        SilverTrace.info("containerManager",
            "ContainerManager.extractComponentNameFromInstanceId",
            "root.MSG_GEN_PARAM_VALUE", "componentName = " + componentName);
        return instanceId.substring(0, i);
      }
    }
    return instanceId;
  }

  private Hashtable<String, String> getAsso() throws ContainerManagerException {
    /*
     * synchronized (assoComponentIdInstanceId) { if (assoComponentIdInstanceId.isEmpty())
     * assoComponentIdInstanceId.putAll(loadAsso(null)); }
     */

    return assoComponentIdInstanceId;
  }

  private String getInstanceId(String componentId)
      throws ContainerManagerException {
    return (String) getAsso().get(componentId);
  }

  private void addAsso(String componentId, int instanceId)
      throws ContainerManagerException {
    getAsso().put(componentId, Integer.toString(instanceId));
  }

  private void removeAsso(String componentId) throws ContainerManagerException {
    getAsso().remove(componentId);
  }

  /**
   * Load the cache instanceId-componentId
   * @param connection
   * @throws ContainerManagerException
   */
  private static Hashtable<String, String> loadAsso(Connection connection)
      throws ContainerManagerException {
    boolean bCloseConnection = false;

    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    Hashtable<String, String> tempAsso = new Hashtable<String, String>();
    try {
      if (connection == null) {
        // Open connection
        connection = DBUtil.makeConnection(m_dbName);
        bCloseConnection = true;
      }

      // Get the instanceId
      String sSQLStatement = "SELECT instanceId, componentId FROM "
          + m_sInstanceTable;

      // Execute the insertion
      SilverTrace.info("containerManager", "ContainerManager.loadAsso",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      while (resSet.next()) {
        tempAsso.put(resSet.getString(2), Integer.toString(resSet.getInt(1)));
      }
    } catch (Exception e) {
      throw new ContainerManagerException("ContainerManager.loadAsso",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_LOAD_ASSO_CACHE", "", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      try {
        if (bCloseConnection && connection != null)
          connection.close();
      } catch (Exception e) {
        SilverTrace.error("containerManager", "ContainerManager.loadAsso",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
    return tempAsso;
  }

  /**
   * A SilverContent is removed, the containerManager is called to do all the necessary actions
   * Assumption : the nSilverContentId is unique among all the SilverContent
   */
  public void silverContentIsRemoved(Connection connection,
      int nSilverContentId, int nContainerInstanceId)
      throws ContainerManagerException {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    boolean bCloseConnection = false;

    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        // Open connection
        connection = DBUtil.makeConnection(m_dbName);
        connection.setAutoCommit(false);
        bCloseConnection = true;
      }

      // Search for the corresponding containerPeas
      ContainerPeas containerPeas = this.getContainerPeas(connection,
          nContainerInstanceId);

      // Call the remove on the ContainerInterface
      ContainerInterface containerInterface = containerPeas
          .getContainerInterface();
      List<Integer> alPositions = containerInterface.removePosition(connection,
          nSilverContentId);

      // Remove the links Positions-ContainerInstanceId
      if (alPositions.size() > 0) {
        sSQLStatement.append("DELETE FROM " + m_sLinksTable + " WHERE ");
        for (int nI = 0; nI < alPositions.size(); nI++) {
          sSQLStatement.append("(positionId = " + alPositions.get(nI).toString() + ")");
          if (nI < alPositions.size() - 1) {
            sSQLStatement.append(" OR ");
          }
        }
        SilverTrace.info("containerManager",
            "ContainerManager.silverContentIsRemoved",
            "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
        prepStmt = connection.prepareStatement(sSQLStatement.toString());

        prepStmt.executeUpdate();
      }

      // Commit the changes
      if (bCloseConnection)
        connection.commit();
    } catch (Exception e) {
      if (bCloseConnection)
        this.rollbackConnection(connection);
      throw new ContainerManagerException(
          "ContainerManager.silverContentIsRemoved", SilverpeasException.ERROR,
          "containerManager.EX_CANT_REMOVE_SILVERCONTENT", "nSilverContentId: "
          + nSilverContentId + "   nContainerInstanceId: "
          + nContainerInstanceId, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection)
        closeConnection(connection);
    }
  }

  /**
   * Method declaration
   * @param sComponentId
   * @param sContainerType
   * @param sContentType
   * @throws ContainerManagerException
   * @see
   */
  private void checkParameters(String sComponentId, String sContainerType,
      String sContentType) throws ContainerManagerException {
    // Check the componentId
    this.checkComponentId(sComponentId);

    // Check if the given containerType is not null
    if (sContainerType == null) {
      throw new ContainerManagerException("ContainerManager.checkParameters",
          SilverpeasException.ERROR, "containerManager.EX_CONTAINERTYPE_NULL");

      // Check if the given containerType is not empty
    }
    if (sContainerType.length() == 0) {
      throw new ContainerManagerException("ContainerManager.checkParameters",
          SilverpeasException.ERROR, "containerManager.EX_CONTAINERTYPE_EMPTY");

      // Check if the given contentType is not null
    }
    if (sContentType == null) {
      throw new ContainerManagerException("ContainerManager.checkParameters",
          SilverpeasException.ERROR, "containerManager.EX_CONTENTTYPE_NULL");

      // Check if the given contentType is not empty
    }
    if (sContentType.length() == 0) {
      throw new ContainerManagerException("ContainerManager.checkParameters",
          SilverpeasException.ERROR, "containerManager.EX_CONTENTTYPE_EMPTY");
    }
  }

  /**
   * Method declaration
   * @param sComponentId
   * @throws ContainerManagerException
   * @see
   */
  private void checkComponentId(String sComponentId)
      throws ContainerManagerException {
    // Check if the given componentId is not null
    if (sComponentId == null) {
      throw new ContainerManagerException("ContainerManager.checkParameters",
          SilverpeasException.ERROR, "containerManager.EX_COMPONENTID_NULL");

      // Check if the given componentId is not empty
    }
    if (sComponentId.length() == 0) {
      throw new ContainerManagerException("ContainerManager.checkParameters",
          SilverpeasException.ERROR, "containerManager.EX_COMPONENTID_EMPTY");
    }
  }

  /**
   * Method declaration
   * @param sSQLStatement
   * @return
   * @throws ContainerManagerException
   * @see
   */
  private String getFirstStringValue(String sSQLStatement)
      throws ContainerManagerException {
    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      String sValue = null;

      // Open connection
      connection = DBUtil.makeConnection(m_dbName);

      // Execute the query
      SilverTrace.info("containerManager",
          "ContainerManager.getFirstStringValue", "root.MSG_GEN_PARAM_VALUE",
          "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the result
      while (resSet.next() && sValue == null) {
        sValue = resSet.getString(1);
      }

      return sValue;
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.getFirstStringValue", SilverpeasException.ERROR,
          "containerManager.EX_CANT_QUERY_DATABASE", "sSQLStatement: "
          + sSQLStatement, e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      closeConnection(connection);
    }
  }

  /**
   * Add a link between a position (in the classify engine) and a container intance Called when a
   * content is referenced in a container
   */
  public void addContainerContentInstanceLink(Connection connection,
      int nPositionId, String sComponentId) throws ContainerManagerException {
    PreparedStatement prepStmt = null;
    try {
      // Get the containerInstanceId corresponding to the given componentId
      int nContainerInstanceId = this.getContainerInstanceId(sComponentId);

      // Set the insertion statement
      String sSQLStatement = "INSERT INTO " + m_sLinksTable
          + " (positionId, containerInstanceId) VALUES(" + nPositionId + ", "
          + nContainerInstanceId + ")";

      // Execute the insertion
      SilverTrace.info("containerManager",
          "ContainerManager.addContainerContentInstanceLink",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);

      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.addContainerContentInstanceLink",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_ADDLINK_CONTAINER_POSITION",
          "nPositionId: " + nPositionId + "sComponentId: " + sComponentId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Remove a link between a position (in the classify engine) and a container intance Called when a
   * content is unreferenced in a container
   */
  public void removeContainerContentInstanceLink(Connection connection,
      int nPositionId, String sComponentId) throws ContainerManagerException {
    PreparedStatement prepStmt = null;
    try {
      // Get the containerInstanceId corresponding to the given componentId
      int nContainerInstanceId = this.getContainerInstanceId(sComponentId);

      // Set the delete statement
      String sSQLStatement = "DELETE FROM " + m_sLinksTable
          + " WHERE (positionId = " + nPositionId
          + ") AND (containerInstanceId = " + nContainerInstanceId + ")";

      // Execute the delete
      SilverTrace.info("containerManager",
          "ContainerManager.removeContainerContentInstanceLink",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);

      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.removeContainerContentInstanceLink",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_REMOVELINK_CONTAINER_POSITION",
          "nPositionId: " + nPositionId + "sComponentId: " + sComponentId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Remove all the links for the given positionIds Ex: Called when empty positions are removed from
   * the ClassifyEngine
   */
  public void removeAllPositionIdsLink(Connection connection, List<Integer> alPositionIds)
      throws ContainerManagerException {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    PreparedStatement prepStmt = null;
    try {
      if (alPositionIds != null && alPositionIds.size() > 0) {
        // Set the delete statement
        sSQLStatement.append("DELETE FROM " + m_sLinksTable + " WHERE ");
        for (int nI = 0; nI < alPositionIds.size(); nI++) {
          sSQLStatement.append("(positionId = " + alPositionIds.get(nI).intValue() + ")");
          if (nI < alPositionIds.size() - 1) {
            sSQLStatement.append(" OR ");
          }
        }

        // Execute the delete
        SilverTrace.info("containerManager",
            "ContainerManager.removeAllPositionIdsLink",
            "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
        prepStmt = connection.prepareStatement(sSQLStatement.toString());

        prepStmt.executeUpdate();
      }
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.removeAllPositionIdsLink",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_REMOVEALLLINK_POSITIONIDS", "", e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Return the statement to get only the positions that belongs to the given componentId
   */
  public JoinStatement getFilterPositionsByComponentIdStatement(
      List<Integer> alPositions, List<String> alComponentId) throws ContainerManagerException {
    StringBuffer sSQLStatement = new StringBuffer(1000);

    JoinStatement joinStatement = new JoinStatement();

    List<String> alGivenTables = new ArrayList<String>();
    List<String> alGivenKeys = new ArrayList<String>();

    alGivenTables.add(m_sLinksTable);
    alGivenKeys.add("positionId");

    alGivenTables.add(m_sInstanceTable);
    alGivenKeys.add("instanceId");

    joinStatement.setTables(alGivenTables);

    joinStatement.setJoinKeys(alGivenKeys);

    // works on the componentId List
    if (alComponentId != null && alComponentId.size() > 0) {
      sSQLStatement.append(" CML.containerInstanceId IN (");
      boolean first = true;
      for (String component : alComponentId) {
        // Get the containerInstanceId corresponding to the given componentId
        int nContainerInstanceId = this.getContainerInstanceId(component);
        // We need only components in a container
        if (nContainerInstanceId != -1) {
          if (!first) {
            sSQLStatement.append(", ");
          } else {
            first = false;
          }
          
          sSQLStatement.append(nContainerInstanceId);
        }
      }
      sSQLStatement.append(") ");
    }

    if (alPositions != null && alPositions.size() > 0) {
      sSQLStatement.append(" AND (");
    }
    for (int nI = 0; alPositions != null && nI < alPositions.size(); nI++) {
      sSQLStatement.append("CML.positionId = " + alPositions.get(nI).toString());
      if (nI < alPositions.size() - 1) {
        sSQLStatement.append(" OR ");
      } else {
        sSQLStatement.append(")");
      }
    }

    joinStatement.setWhere(sSQLStatement.toString());
    SilverTrace.info("containerManager",
        "ContainerManager.getFilterPositionsByComponentIdStatement",
        "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);

    return joinStatement;
  }

  /**
   * Return only the positions that belongs to the given componentId ATTENTION: this function is
   * slow, use it only for a few positions (for one SilverContentId)
   */
  public List<Integer> filterPositionsByComponentId(List<Integer> alPositions, String sComponentId)
      throws ContainerManagerException {
    StringBuffer sSQLStatement = new StringBuffer(1000);
    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      // Open connection
      connection = DBUtil.makeConnection(m_dbName);

      // Get the containerInstanceId corresponding to the given componentId
      int nContainerInstanceId = this.getContainerInstanceId(sComponentId);

      // Set the query statement to get the positions correponding to this
      // componentId
      sSQLStatement.append("SELECT positionId FROM " + m_sLinksTable
          + " WHERE (containerInstanceId = " + nContainerInstanceId + ")");
      if (alPositions != null && alPositions.size() > 0) {
        sSQLStatement.append(" AND (");
      }
      for (int nI = 0; alPositions != null && nI < alPositions.size(); nI++) {
        sSQLStatement.append("positionId = " + alPositions.get(nI).toString());
        if (nI < alPositions.size() - 1) {
          sSQLStatement.append(" OR ");
        } else {
          sSQLStatement.append(")");
        }
      }

      // Execute the query
      SilverTrace.info("containerManager",
          "ContainerManager.filterPositionsByComponentId",
          "root.MSG_GEN_PARAM_VALUE", "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement.toString());
      resSet = prepStmt.executeQuery();

      // Fetch the results
      List<Integer> alFilteredPositions = new ArrayList<Integer>();

      while (resSet.next()) {
        alFilteredPositions.add(Integer.valueOf(resSet.getInt(1)));
      }

      return alFilteredPositions;
    } catch (Exception e) {
      throw new ContainerManagerException(
          "ContainerManager.filterPositionsByComponentId",
          SilverpeasException.ERROR,
          "containerManager.EX_CANT_FILTER_POSITIONS", "sComponentId: "
          + sComponentId, e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      closeConnection(connection);
    }
  }

  /**
   * Return only the SilverContentIds that belongs to the given componentId
   */

  /*
   * public List filterSilverContentIdsByComponentId(List alDoubleIds, String sComponentId) throws
   * ContainerManagerException { try { List alPositionIds = (List) alDoubleIds.get(0); List
   * alObjectIds = (List) alDoubleIds.get(1); // Filter by positions List alFilteredPositionIds =
   * this.filterPositionsByComponentId(alPositionIds, sComponentId); // Build the return list of
   * FilteredSilverContentIds (Group By objectId) int nObjectsDone = 0; int[] anObjectsDone = new
   * int[alFilteredPositionIds.size()]; List alFilteredObjectIds = new ArrayList(); for(int nI=0;
   * alFilteredPositionIds!= null && nI < alFilteredPositionIds.size(); nI++) for(int nJ=0;
   * alPositionIds != null && nJ < alPositionIds.size(); nJ++)
   * if(((Integer)alFilteredPositionIds.get(nI)).intValue() ==
   * ((Integer)alPositionIds.get(nJ)).intValue())
   * if(this.notIn(((Integer)alObjectIds.get(nJ)).intValue(), anObjectsDone, nObjectsDone)) {
   * alFilteredObjectIds.add(alObjectIds.get(nJ)); anObjectsDone[nObjectsDone++] =
   * ((Integer)alObjectIds.get(nJ)).intValue(); } return alFilteredObjectIds; } catch(Exception e) {
   * throw new ContainerManagerException ("ContainerManager.filterSilverContentIdsByComponentId"
   * ,SilverpeasException. ERROR,"containerManager.EX_CANT_FILTER_SILVERCONTENTIDS",
   * "sComponentId: " + sComponentId , e); } }
   */

  /**
   * Method declaration
   * @param connection
   * @see
   */
  private void rollbackConnection(Connection connection) {
    try {
      if (connection != null) {
        connection.rollback();
      }
    } catch (Exception e) {
      SilverTrace.error("containerManager",
          "ContainerManager.rollbackConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
  }

  private void closeConnection(Connection connection) {
    try {
      if (connection != null)
        connection.close();
    } catch (Exception e) {
      SilverTrace.error("containerManager", "ContainerManager.closeConnection",
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

}
