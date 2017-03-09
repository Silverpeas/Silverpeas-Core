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

package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.JoinStatement;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents the ContentManager API It is the gateway to all the silverpeas contents
 * (documentation, ....)
 */
@Singleton
@Named("contentManager")
public class ContentManager implements Serializable {

  private final static List<ContentPeas> s_acContentPeas = new ArrayList<>();

  static {
    // -------------------------------------------------
    // We don't have enough time to do the parsing !!!
    // We hard coded for this time !!!!
    // -------------------------------------------------

    // Put all the existing contents in the array of contents
    s_acContentPeas.add(new ContentPeas("whitePages"));
    s_acContentPeas.add(new ContentPeas("questionReply"));
    s_acContentPeas.add(new ContentPeas("kmelia"));
    s_acContentPeas.add(new ContentPeas("survey"));
    s_acContentPeas.add(new ContentPeas("toolbox"));
    s_acContentPeas.add(new ContentPeas("quickinfo"));
    s_acContentPeas.add(new ContentPeas("almanach"));
    s_acContentPeas.add(new ContentPeas("quizz"));
    s_acContentPeas.add(new ContentPeas("forums"));
    s_acContentPeas.add(new ContentPeas("pollingStation"));
    s_acContentPeas.add(new ContentPeas("bookmark"));
    s_acContentPeas.add(new ContentPeas("infoLetter"));
    s_acContentPeas.add(new ContentPeas("webSites"));
    s_acContentPeas.add(new ContentPeas("gallery"));
    s_acContentPeas.add(new ContentPeas("blog"));
  }

  // Container peas
  private static Map<String, String> assoComponentIdInstanceId = null;
  // Association SilverContentId (the key) internalContentId (the value) (cache)
  private static HashMap<String, String> assoSilverContentIdInternalComponentId =
      new HashMap<>(1000);
  private static String m_sInstanceTable = "SB_ContentManager_Instance";
  private static final long serialVersionUID = 7069917496138130066L;
  private static String m_sSilverContentTable = "SB_ContentManager_Content";

  static {
    try {
      assoComponentIdInstanceId = new HashMap<>(loadAsso(null));
    } catch (ContentManagerException e) {
      SilverTrace
          .error("contentManager", "ContentManager.initStatic", "root.EX_CLASS_NOT_INITIALIZED",
              "assoComponentIdInstanceId initialization failed !", e);
    }
  }

  /**
   * return a list of identifiers of the resources matching the specified identifiers of
   * {@link SilverContent} objects.
   * @param contentIds a list of identifiers of
   * {@link org.silverpeas.core.contribution.contentcontainer.content.SilverContent} objects.
   * @return a list of resource identifiers.
   */
  public List<String> getResourcesMatchingContents(final List<Integer> contentIds) {
    List<String> pks = new ArrayList<>();
    // for each id of SilverContent, we get the identifier of the matching resource.
    for (Integer contentId : contentIds) {
      try {
        String id = ContentManagerProvider.getContentManager().getInternalContentId(contentId);
        pks.add(id);
      } catch (ClassCastException | ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return pks;
  }

  /**
   * When a generic component is instanciate, this function is called to register the association
   * between container and content
   * @param connection
   * @param sComponentId
   * @param sContainerType
   * @param sContentType
   * @return
   * @throws ContentManagerException
   */
  public int registerNewContentInstance(Connection connection, String sComponentId,
      String sContainerType, String sContentType) throws ContentManagerException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(sComponentId, sContainerType, sContentType);
    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Compute the next instanceId
      int newInstanceId = DBUtil.getNextId(m_sInstanceTable, "instanceId");
      // Insert the association container - content
      String sSQLStatement = "INSERT INTO " + m_sInstanceTable +
          "(instanceId, componentId, containerType, contentType) ";
      sSQLStatement +=
          "VALUES (" + newInstanceId + ",'" + sComponentId + "','" + sContainerType + "','" +
              sContentType + "')";
      // Execute the insertion

      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();
      addAsso(sComponentId, newInstanceId);
      return newInstanceId;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.registerNewContentInstance",
          SilverpeasException.ERROR, "contentManager.EX_CANT_REGISTER_CONTENT_INSTANCE",
          "sComponentId: " + sComponentId + "    sContentType: " + sContentType, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        closeConnection(connection);
      }
    }
  }

  private void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      SilverTrace.error("contentManager", "ContentManager.closeConnection",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
  }

  /**
   * When a generic component is uninstanciate, this function is called to unregister the
   * association between container and content
   * @param connection
   * @param sComponentId
   * @param sContainerType
   * @param sContentType
   * @throws ContentManagerException
   */
  public void unregisterNewContentInstance(Connection connection, String sComponentId,
      String sContainerType, String sContentType) throws ContentManagerException {
    boolean bCloseConnection = false;
    this.checkParameters(sComponentId, sContainerType, sContentType);
    try {
      if (connection == null) {
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      final String contentDeletion = "DELETE FROM " + m_sSilverContentTable + " WHERE " +
          "contentInstanceId IN (SELECT instanceId from " + m_sInstanceTable + " WHERE componentId = ? AND " +
          "containerType = ? AND contentType = ?)";

      final String instanceDeletion = "DELETE FROM " + m_sInstanceTable + " WHERE componentId = ?" +
          " AND containerType = ? AND contentType = ?";

      try(PreparedStatement deletion = connection.prepareStatement(contentDeletion)) {
        deletion.setString(1, sComponentId);
        deletion.setString(2, sContainerType);
        deletion.setString(3, sContentType);
        deletion.execute();
      }
      try(PreparedStatement deletion = connection.prepareStatement(instanceDeletion)) {
        deletion.setString(1, sComponentId);
        deletion.setString(2, sContainerType);
        deletion.setString(3, sContentType);
        deletion.execute();
      }

      removeAsso(sComponentId);
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.unregisterNewContentInstance",
          SilverpeasException.ERROR, "contentManager.EX_CANT_UNREGISTER_CONTENT_INSTANCE",
          "sComponentId: " + sComponentId + "    sContentType: " + sContentType, e);
    } finally {
      if (bCloseConnection) {
        closeConnection(connection);
      }
    }
  }

  /**
   * Return the ContentPeas corresponding to the given componentId
   * @param sComponentId
   * @return
   * @throws ContentManagerException
   */
  public ContentPeas getContentPeas(String sComponentId) throws ContentManagerException {
    // Get the ContentType
    String sContentType = this.getContentType(sComponentId);


    // Get the ContentPeas from the ContentType
    for (ContentPeas s_acContentPea : s_acContentPeas) {

      if (s_acContentPea.getType().equals(sContentType)) {

        return s_acContentPea;
      }
    }

    return null;
  }

  // Return the Content type corresponding to the given componentId
  private String getContentType(String componentId) throws ContentManagerException {
    // Build the SQL statement
    String sSQLStatement =
        "SELECT contentType FROM " + m_sInstanceTable + " WHERE (componentId = '" + componentId +
            "')";
    // Get the contentType from the DB Query
    String sContentType = this.getFirstStringValue(sSQLStatement);
    return sContentType;
  }

  private void checkParameters(String sComponentId, String sContainerType, String sContentType)
      throws ContentManagerException {
    // Check if the given componentId is not null
    if (sComponentId == null) {
      throw new ContentManagerException("ContentManager.checkParameters", SilverpeasException.ERROR,
          "contentManager.EX_COMPONENTID_NULL");
    }

    // Check if the given componentId is not empty
    if (sComponentId.length() == 0) {
      throw new ContentManagerException("ContentManager.checkParameters", SilverpeasException.ERROR,
          "contentManager.EX_COMPONENTID_EMPTY");
    }

    // Check if the given containerType is not null
    if (sContainerType == null) {
      throw new ContentManagerException("ContentManager.checkParameters", SilverpeasException.ERROR,
          "contentManager.EX_CONTAINERTYPE_NULL");
    }

    // Check if the given containerType is not empty
    if (sContainerType.length() == 0) {
      throw new ContentManagerException("ContentManager.checkParameters", SilverpeasException.ERROR,
          "contentManager.EX_CONTAINERTYPE_EMPTY");
    }

    // Check if the given contentType is not null
    if (sContentType == null) {
      throw new ContentManagerException("ContentManager.checkParameters", SilverpeasException.ERROR,
          "contentManager.EX_CONTENTTYPE_NULL");
    }

    // Check if the given contentType is not empty
    if (sContentType.length() == 0) {
      throw new ContentManagerException("ContentManager.checkParameters", SilverpeasException.ERROR,
          "contentManager.EX_CONTENTTYPE_EMPTY");
    }
  }

  private String getFirstStringValue(String sSQLStatement) throws ContentManagerException {
    Connection connection = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    try {
      String sValue = null;

      // Open connection
      connection = DBUtil.openConnection();

      // Execute the query
      SilverTrace
          .info("contentManager", "ContentManager.getFirstStringValue", "root.MSG_GEN_PARAM_VALUE",
              "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the result
      while (resSet.next() && sValue == null) {
        sValue = resSet.getString(1);
      }

      return sValue;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.getFirstStringValue",
          SilverpeasException.ERROR, "contentManager.EX_CANT_QUERY_DATABASE",
          "sSQLStatement: " + sSQLStatement, e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      closeConnection(connection);
    }
  }

  /**
   * Add a silver content Called when a content add a document and register it to get its
   * SilverContentId in return
   */
  public int addSilverContent(Connection connection, String sInternalContentId, String sComponentId,
      String sAuthorId) throws ContentManagerException {
    return addSilverContent(connection, sInternalContentId, sComponentId, sAuthorId, null);
  }

  /**
   * Add a silver content Called when a content add a document and register it to get its
   * SilverContentId in return
   */
  public int addSilverContent(Connection connection, String sInternalContentId, String sComponentId,
      String sAuthorId, SilverContentVisibility scv) throws ContentManagerException {
    //
    // creation d'un objet java.sql.Date qui represente la date systeme.
    //
    Date date = new Date(); // recupere la date de ce jour
    long time = date.getTime(); // recupere les millisecondes de la date de ce jour
    java.sql.Date systemDate = new java.sql.Date(time);

    boolean bCloseConnection = false;

    if (scv == null) {
      scv = new SilverContentVisibility();
    }

    PreparedStatement prepStmt = null;
    try {
      if (connection == null) {
        // Open connection
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Get the contentInstanceId corresponding to the given componentId
      int nContentInstanceId = this.getContentInstanceId(sComponentId);

      SilverTrace
          .info("contentManager", "ContentManager.addSilverContent", "root.MSG_GEN_PARAM_VALUE",
              "nContentInstanceId= " + nContentInstanceId);

      // Compute the next silverContentId
      int newSilverContentId = DBUtil.getNextId(m_sSilverContentTable, "silverContentId");

      SilverTrace
          .info("contentManager", "ContentManager.addSilverContent", "root.MSG_GEN_PARAM_VALUE",
              "newSilverContentId= " + newSilverContentId);

      // Insert the silverContent
      String sSQLStatement = "INSERT INTO " +
          m_sSilverContentTable +
          "(silverContentId, internalContentId, contentInstanceid, authorId, creationDate, " +
          "beginDate, endDate, isVisible) ";
      sSQLStatement +=
          "VALUES (" + newSilverContentId + ",'" + sInternalContentId + "'," + nContentInstanceId +
              "," + Integer.parseInt(sAuthorId) + ",?, ? , ? , ? )";

      // Execute the insertion
      SilverTrace
          .info("contentManager", "ContentManager.addSilverContent", "root.MSG_GEN_PARAM_VALUE",
              "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.setDate(1, systemDate);
      prepStmt.setString(2, scv.getBeginDate());
      prepStmt.setString(3, scv.getEndDate());
      prepStmt.setInt(4, scv.isVisible());
      prepStmt.executeUpdate();

      return newSilverContentId;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.addSilverContent",
          SilverpeasException.ERROR, "contentManager.EX_CANT_ADD_SILVER_CONTENT",
          "sInternalContentId: " + sInternalContentId + "    sComponentId: " + sComponentId, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        closeConnection(connection);
      }
    }
  }

  /**
   * Remove a silver content Called when a content remove a document
   */
  public void removeSilverContent(Connection connection, int nSilverContentId, String sComponentId)
      throws ContentManagerException {
    PreparedStatement prepStmt = null;
    try {
      // delete the silverContent
      String sSQLStatement =
          "DELETE FROM " + m_sSilverContentTable + " WHERE (silverContentId = " + nSilverContentId +
              ")";

      // Execute the delete
      SilverTrace
          .info("contentManager", "ContentManager.removeSilverContent", "root.MSG_GEN_PARAM_VALUE",
              "sSQLStatement= " + sSQLStatement);
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.removeSilverContent",
          SilverpeasException.ERROR, "contentManager.EX_CANT_REMOVE_SILVER_CONTENT",
          "nSilverContentId: " + nSilverContentId + "   sComponentId: " + sComponentId, e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private int getSilverContentId(Statement stmt, String sInternalContentId, String sComponentId,
      boolean isGlobalSearch) throws ContentManagerException {
    ResultSet resSet = null;
    StringBuffer sSQLStatement = new StringBuffer();
    int nSilverContentId = -1;

    try {
      // Get the SilverContentId
      sSQLStatement.append("SELECT silverContentId FROM ").append(m_sSilverContentTable);
      sSQLStatement.append(" WHERE (internalContentId = '").append(sInternalContentId)
          .append("') AND (contentInstanceId = ");
      if (isGlobalSearch) {
        sSQLStatement.append(" (select instanceId from ").append(m_sInstanceTable).
            append(" where componentId='").append(sComponentId).append("') ) ");
      } else {
        sSQLStatement.append(this.getContentInstanceId(sComponentId)).append(") ");
      }

      // Execute the search
      SilverTrace
          .info("contentManager", "ContentManager.getSilverContentId", "root.MSG_GEN_PARAM_VALUE",
              "sSQLStatement= " + sSQLStatement);
      resSet = stmt.executeQuery(sSQLStatement.toString());
      // Fetch the result

      if (resSet.next()) {
        nSilverContentId = resSet.getInt(1);
      }
    } catch (SQLException excep_select) {
      SilverTrace
          .warn("contentManager", "ContentManager.getSilverContentId", "root.MSG_GEN_PARAM_VALUE",
              "sSQLStatement= " + sSQLStatement);
    } finally {
      DBUtil.close(resSet);
    }

    return nSilverContentId;
  }

  /**
   * Return the SilverContentId corresponding to the given internalContentId Called when a content
   * remove a document
   */
  public int getSilverContentId(String sInternalContentId, String sComponentId)
      throws ContentManagerException {
    Connection connection = null;
    Statement stmt = null;
    int silverContentId;
    try {
      // Open connection
      connection = DBUtil.openConnection();
      stmt = connection.createStatement();
      silverContentId = getSilverContentId(stmt, sInternalContentId, sComponentId, false);

      return silverContentId;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.getSilverContentId",
          SilverpeasException.ERROR, "contentManager.EX_CANT_GET_SILVERCONTENTID",
          "sComponentId: " + sComponentId + "    sInternalContentId: " + sInternalContentId, e);
    } finally {
      DBUtil.close(stmt);
      closeConnection(connection);
    }
  }

  /**
   * Return the sorted list containing SilverContentIds corresponding to the list containing id et
   * instanceId The list is not null and not empty !! Called when a content remove a document
   */
  public SortedSet<Integer> getSilverContentId(List<String> documentFeature)
      throws ContentManagerException {
    Connection connection = null;
    Statement stmt = null;
    SortedSet<Integer> alSilverContentId = new TreeSet<>();
    String sInternalContentId = "";
    String sComponentId = "";
    int silverContentId;
    try {
      // Open connection
      connection = DBUtil.openConnection();
      stmt = connection.createStatement();
      // main loop to build sql queries
      for (int i = 0; i < documentFeature.size(); i = i + 2) {
        // Get the internamContentId and theinstanceId from the list
        sInternalContentId = documentFeature.get(i);
        sComponentId = documentFeature.get(i + 1);
        // Get the SilverContentId
        silverContentId = getSilverContentId(stmt, sInternalContentId, sComponentId, true);

        // add the result into the sortedSet
        if (silverContentId != -1) {
          // le composant dont instanceId et objectId courant fait partie du PDC
          alSilverContentId.add(silverContentId);
        }

      }

      return alSilverContentId;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.getSilverContentId",
          SilverpeasException.ERROR, "contentManager.EX_CANT_GET_SILVERCONTENTID",
          "sComponentId: " + sComponentId + "    sInternalContentId: " + sInternalContentId, e);
    } finally {
      try {
        DBUtil.close(stmt);
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        SilverTrace.error("contentManager", "ContentManager.getSilverContentId",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private String getInternalContentIdFromCache(String sSilverContentId) {
    return assoSilverContentIdInternalComponentId.get(sSilverContentId);
  }

  private void putInternalContentIdIntoCache(String sSilverContentId, String sInternalContentId) {
    assoSilverContentIdInternalComponentId.put(sSilverContentId, sInternalContentId);
  }

  /**
   * Return the InternalContentId corresponding to the given SilverContentId Called when a content
   * remove a document
   */
  public String getInternalContentId(int nSilverContentId) throws ContentManagerException {
    String sSilverContentId = String.valueOf(nSilverContentId);
    String sInternalContentId = getInternalContentIdFromCache(sSilverContentId);
    if (sInternalContentId == null) {
      Connection connection = null;
      PreparedStatement prepStmt = null;
      ResultSet resSet = null;
      try {
        // Open connection
        connection = DBUtil.openConnection();

        // Get the InternalContentId
        String sSQLStatement = "SELECT internalContentId FROM " + m_sSilverContentTable +
            " WHERE (silverContentId = " + nSilverContentId + ")";

        // Execute the search

        prepStmt = connection.prepareStatement(sSQLStatement);
        resSet = prepStmt.executeQuery();

        // Fetch the result
        if (resSet.next()) {
          sInternalContentId = resSet.getString(1);
        }

        putInternalContentIdIntoCache(sSilverContentId, sInternalContentId);
      } catch (Exception e) {
        throw new ContentManagerException("ContentManager.getInternalContentId",
            SilverpeasException.ERROR, "contentManager.EX_CANT_GET_INTERNALCONTENTID",
            "nSilverContentId: " + nSilverContentId, e);
      } finally {
        DBUtil.close(resSet, prepStmt);
        closeConnection(connection);
      }
    }
    return sInternalContentId;
  }

  /**
   * Return the content instance Id corresponding to the componentId
   */
  public int getContentInstanceId(String sComponentId) throws ContentManagerException {
    int contentInstanceId = -1;

    String sContentInstanceId = getInstanceId(sComponentId);
    if (sContentInstanceId != null) {
      contentInstanceId = Integer.parseInt(sContentInstanceId);
    } else {
      // the given instance is not registered. This code is used to maintains
      // compatibility with previous versions.
      String componentName = extractComponentNameFromInstanceId(sComponentId);
      contentInstanceId =
          registerNewContentInstance(null, sComponentId, "containerPDC", componentName);
    }

    return contentInstanceId;
  }

  private String extractComponentNameFromInstanceId(String instanceId) {
    char character;
    for (int i = 0; i < instanceId.length(); i++) {
      character = instanceId.charAt(i);
      if (character == '0' || character == '1' || character == '2' || character == '3' ||
          character == '4' || character == '5' || character == '6' || character == '7' ||
          character == '8' || character == '9') {

        return instanceId.substring(0, i);
      }
    }
    return instanceId;
  }

  private Map<String, String> getAsso() {
    return assoComponentIdInstanceId;
  }

  private String getInstanceId(String componentId) {
    return getAsso().get(componentId);
  }

  private void addAsso(String componentId, int instanceId) {
    getAsso().put(componentId, java.lang.Integer.toString(instanceId));
  }

  private void removeAsso(String componentId) {
    getAsso().remove(componentId);
  }

  // Load the cache instanceId-componentId
  private static Map<String, String> loadAsso(Connection connection)
      throws ContentManagerException {
    boolean bCloseConnection = false;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    Map<String, String> tempAsso = new HashMap<>();
    try {
      if (connection == null) {
        // Open connection
        connection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Get the instanceId
      String sSQLStatement = "SELECT instanceId, componentId FROM " + m_sInstanceTable;

      // Execute the insertion

      prepStmt = connection.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      while (resSet.next()) {
        tempAsso.put(resSet.getString(2), String.valueOf(resSet.getInt(1)));
      }
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.loadAsso", SilverpeasException.ERROR,
          "contentManager.EX_CANT_LOAD_ASSO_CACHE", "", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      try {
        if (bCloseConnection && connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        SilverTrace
            .error("contentManager", "ContentManager.loadAsso", "root.EX_CONNECTION_CLOSE_FAILED",
                "", e);
      }
    }
    return tempAsso;
  }

  public JoinStatement getPositionsByGenericSearch(String authorId, String afterDate,
      String beforeDate) {
    StringBuilder sSQLStatement = new StringBuilder(1000);

    JoinStatement joinStatement = new JoinStatement();
    List<String> alGivenTables = new ArrayList<>();
    List<String> alGivenKeys = new ArrayList<>();
    alGivenTables.add(m_sSilverContentTable);
    alGivenKeys.add("silverContentId");

    joinStatement.setTables(alGivenTables);
    joinStatement.setJoinKeys(alGivenKeys);

    // works on the author
    if (authorId != null && !authorId.equals("")) {
      sSQLStatement.append(" CMC.authorId = ").append(authorId);
    }

    // works on the beforeDate
    if (beforeDate != null && beforeDate.length() > 0) {
      if (sSQLStatement.length() > 0) {
        sSQLStatement.append(" AND ");
      }
      sSQLStatement.append(" CMC.creationDate < ? ");
    }

    // works on the afterDate
    if (afterDate != null && afterDate.length() > 0) {
      if (sSQLStatement.length() > 0) {
        sSQLStatement.append(" AND ");
      }
      sSQLStatement.append(" CMC.creationDate > ? ");
    }

    joinStatement.setWhere(sSQLStatement.toString());

    return joinStatement;
  }

  /**
   * retourne une liste d'instanceID a partir d'une Liste de silvercontentId
   * @param alSilverContentId - la liste de silvercontentId silvercontentId
   * @return la liste contenant les instances
   */
  public List<String> getInstanceId(List<Integer> alSilverContentId)
      throws ContentManagerException {
    Connection connection = null;
    StringBuffer sSQLStatement = new StringBuffer();
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    List<String> alInstanceIds = new ArrayList<>();
    try {
      // Open connection
      connection = DBUtil.openConnection();

      sSQLStatement.append("select I.componentId from ").append(m_sInstanceTable).append(" I, ").
          append(m_sSilverContentTable).append(" C ");
      sSQLStatement.append(" where I.instanceId = C.contentInstanceId ");
      sSQLStatement.append(" and C.silverContentId = ?");

      // Execute the search
      prepStmt = connection.prepareStatement(sSQLStatement.toString());

      // Loop on the alSilverContentId
      String instanceId = "";
      for (Integer oneSilverContentId : alSilverContentId) {
        prepStmt.setInt(1, oneSilverContentId);
        SilverTrace
            .info("contentManager", "ContentManager.getInstanceId", "root.MSG_GEN_PARAM_VALUE",
                "sSQLStatement= " + sSQLStatement + " silverContentId=" + oneSilverContentId);
        resSet = prepStmt.executeQuery();
        if (resSet.next()) {
          instanceId = resSet.getString(1);
        }
        if (!alInstanceIds.contains(instanceId)) {
          alInstanceIds.add(instanceId);
        }

        DBUtil.close(resSet);
        resSet = null;
      }

      return alInstanceIds;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.getInstanceId", SilverpeasException.ERROR,
          "contentManager.EX_CANT_GET_INSTANCEID", "", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      closeConnection(connection);
    }
  }

  /**
   * Cette méthode retourne une liste de SilverContentId qui se trouve sous une instance de
   * jobPeas.
   * @param instanceId - l'id de l'instance (trucsAstuces978)
   * @return une liste de silvercontentId
   */
  public List<Integer> getSilverContentIdByInstanceId(String instanceId)
      throws ContentManagerException {
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    List<Integer> allSilverContentIds = new ArrayList<>();
    try {
      // Open connection
      con = DBUtil.openConnection();

      String sSQLStatement =
          "select C.silverContentId from " + m_sInstanceTable + " I, " + m_sSilverContentTable +
              " C ";
      sSQLStatement += " where I.instanceId = C.contentInstanceId ";
      sSQLStatement += " and I.componentId like ? ";

      // Execute the search
      prepStmt = con.prepareStatement(sSQLStatement);
      prepStmt.setString(1, instanceId);

      resSet = prepStmt.executeQuery();

      // Fetch the result
      while (resSet.next()) {
        allSilverContentIds.add(resSet.getInt(1));
      }

      return allSilverContentIds;
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.getSilverContentIdByInstanceId",
          SilverpeasException.ERROR, "contentManager.EX_CANT_GET_INSTANCEID", "", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      closeConnection(con);
    }
  }

  public void updateSilverContentVisibilityAttributes(SilverContentVisibility scv,
      String instanceId, int silverObjectId) throws ContentManagerException {
    Connection con = null;
    PreparedStatement prepStmt = null;
    try {
      if (scv != null) {
        // Open connection
        con = DBUtil.openConnection();

        // update the silverContent
        StringBuffer sSQLStatement = new StringBuffer();
        sSQLStatement.append("UPDATE ").append(m_sSilverContentTable);
        sSQLStatement.append(" SET beginDate = ? , endDate = ? , isVisible = ? ");
        sSQLStatement.append(" WHERE silverContentId = ").append(silverObjectId);

        // Execute the update

        prepStmt = con.prepareStatement(sSQLStatement.toString());

        prepStmt.setString(1, scv.getBeginDate());
        prepStmt.setString(2, scv.getEndDate());
        prepStmt.setInt(3, scv.isVisible());
        prepStmt.executeUpdate();
      }
    } catch (Exception e) {
      throw new ContentManagerException("ContentManager.updateSilverContentVisibilityAttributes",
          SilverpeasException.ERROR, "contentManager.EX_CANT_UPDATE_SILVER_CONTENT", e);
    } finally {
      DBUtil.close(prepStmt);
      closeConnection(con);
    }
  }

  public SilverContentVisibility getSilverContentVisibility(int silverObjectId)
      throws ContentManagerException {
    Connection connection = null;
    StringBuffer sSQLStatement = new StringBuffer();
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    SilverContentVisibility scv = null;

    try {
      // Open connection
      connection = DBUtil.openConnection();

      // Get the SilverContentVisibility
      sSQLStatement.append("SELECT beginDate, endDate, isVisible FROM ")
          .append(m_sSilverContentTable);
      sSQLStatement.append(" WHERE silverContentId = '").append(silverObjectId).append("'");


      prepStmt = connection.prepareStatement(sSQLStatement.toString());
      resSet = prepStmt.executeQuery();

      // Fetch the result
      if (resSet.next()) {
        String beginDate = resSet.getString(1);
        String endDate = resSet.getString(2);
        int visibility = resSet.getInt(3);
        boolean isVisible = true;
        if (visibility == 0) {
          isVisible = false;
        }
        scv = new SilverContentVisibility(beginDate, endDate, isVisible);
      }
    } catch (SQLException e) {
      throw new ContentManagerException("ContentManager.getSilverContentVisibility",
          SilverpeasException.ERROR, "contentManager.EX_CANT_QUERY_DATABASE",
          "sSQLStatement: " + sSQLStatement, e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      closeConnection(connection);
    }
    return scv;
  }

  private ContentManager() {

  }
}
